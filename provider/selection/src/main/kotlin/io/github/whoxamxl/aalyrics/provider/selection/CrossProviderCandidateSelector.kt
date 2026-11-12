package io.github.whoxamxl.aalyrics.provider.selection

import io.github.whoxamxl.aalyrics.provider.matching.MetadataMatchScore
import io.github.whoxamxl.aalyrics.provider.matching.LyricsWriting
import io.github.whoxamxl.aalyrics.provider.matching.RecordingVersionContext
import io.github.whoxamxl.aalyrics.core.lyrics.CandidateSelectionDiagnostic
import io.github.whoxamxl.aalyrics.core.lyrics.CandidateSelectionResult

import io.github.whoxamxl.aalyrics.core.lyrics.CandidateSelectionPreferences
import io.github.whoxamxl.aalyrics.core.lyrics.CandidateSelector
import io.github.whoxamxl.aalyrics.core.model.LyricLine
import io.github.whoxamxl.aalyrics.core.model.LyricsSyncType
import io.github.whoxamxl.aalyrics.core.model.TimedLyricLine
import io.github.whoxamxl.aalyrics.core.model.Track
import io.github.whoxamxl.aalyrics.provider.api.LyricsCandidate
import java.util.Locale
import kotlin.math.abs

/**
 * Production cross-provider selection policy adapted from the mature resolver in
 * the working Auto Lyrics fork.
 *
 * Provider-specific source preferences live here rather than in `:core:lyrics`.
 * The core coordinator continues to know only the [CandidateSelector] port.
 */
class CrossProviderCandidateSelector : CandidateSelector {
    override fun select(
        track: Track,
        candidates: List<LyricsCandidate>,
        preferences: CandidateSelectionPreferences,
    ): LyricsCandidate? {
        return selectWithDiagnostics(track, candidates, preferences).candidate
    }

    override fun selectWithDiagnostics(
        track: Track,
        candidates: List<LyricsCandidate>,
        preferences: CandidateSelectionPreferences,
    ): CandidateSelectionResult {
        val rejected = mutableListOf<CandidateSelectionDiagnostic>()
        val scored = scoreCandidates(track, candidates, preferences) { candidate, reason ->
            rejected += CandidateSelectionDiagnostic(candidate.providerId.value, candidate.lyrics.attribution?.sourceId, reason)
        }
        val eligible = scored.filter { romanized ->
            val language = romanized.writing.suspectedRomanization
            language == null || RecordingVersionContext.languageVersion(track.title) == "english" || scored.none { original ->
                original.writing.hasOriginal(language) && original.metadataScore >= 0.90 &&
                    original.metadataScore >= romanized.metadataScore - KARAOKE_METADATA_TOLERANCE &&
                    original.qualityScore >= romanized.qualityScore - KARAOKE_QUALITY_TOLERANCE &&
                    (original.candidate.lyrics.syncType != LyricsSyncType.PLAIN || romanized.candidate.lyrics.syncType == LyricsSyncType.PLAIN)
            }
        }
        val selected = choose(eligible, preferences)
        val diagnostics = rejected + scored.map { score ->
            CandidateSelectionDiagnostic(
                score.candidate.providerId.value, score.candidate.lyrics.attribution?.sourceId,
                when {
                    score.candidate === selected -> "selected"
                    eligible.none { it === score } -> "original-writing-preferred"
                    else -> "lower-ranked"
                },
                score.metadataScore, score.qualityScore,
                score.writing.suspectedRomanization?.let { "suspected-romanization:$it" }
                    ?: score.writing.nativeRows.keys.sorted().joinToString(",").ifBlank { "unknown" },
            )
        }
        return CandidateSelectionResult(selected, diagnostics.take(64))
    }

    private fun choose(scored: List<CandidateScore>, preferences: CandidateSelectionPreferences): LyricsCandidate? {
        val synced = scored.filter { it.candidate.lyrics.syncType != LyricsSyncType.PLAIN }
        val standardBest = best(scored = synced)

        if (standardBest != null) {
            val comparable = synced.filter { score ->
                score.metadataScore >= standardBest.metadataScore - KARAOKE_METADATA_TOLERANCE &&
                    score.qualityScore >= standardBest.qualityScore - KARAOKE_QUALITY_TOLERANCE
            }
            // Only an explicit Karaoke preference gives usable word timing priority.
            // WORD documents still participate as ordinary synchronized lyrics when it is off.
            val wordCandidates = if (preferences.preferredSyncType == LyricsSyncType.WORD) {
                comparable.filter { hasUsableWordTiming(it.candidate) }
            } else emptyList()
            val preferredCandidates = wordCandidates.ifEmpty { comparable }
            // Any useful paired translation can help; untranslated English or vocalizations
            // do not make the supplied translation ineligible. Identity/quality gates still apply.
            val bilingualBest = best(scored = preferredCandidates.filter { score ->
                val document = score.candidate.lyrics
                val translation = document.suppliedTranslation
                translation != null && preferences.preferredTranslationLanguage == translation.languageTag &&
                    document.lines.indices.any { index ->
                        document.lines[index].text.any(Char::isLetterOrDigit) && translation.lines[index] != null
                    }
            }, preferIntactTranslation = true)
            if (bilingualBest != null) return bilingualBest.candidate
            best(wordCandidates)?.let { return it.candidate }
            return standardBest.candidate
        }

        return best(
            scored = scored.filter { it.candidate.lyrics.syncType == LyricsSyncType.PLAIN },
        )?.candidate
    }

    internal fun scoreCandidates(
        track: Track,
        candidates: Collection<LyricsCandidate>,
        preferences: CandidateSelectionPreferences = CandidateSelectionPreferences(),
        onRejected: (LyricsCandidate, String) -> Unit = { _, _ -> },
    ): List<CandidateScore> {
        return candidates.mapNotNull { candidate ->
            if (!hasUsableLyricContent(candidate)) {
                onRejected(candidate, "empty-lyrics")
                return@mapNotNull null
            }
            val metadata = metadataScore(track, candidate) { onRejected(candidate, "metadata:${it.name.lowercase(Locale.ROOT)}") }
                ?: return@mapNotNull null
            if (metadata < MIN_METADATA_SCORE) {
                onRejected(candidate, "metadata:below-threshold")
                return@mapNotNull null
            }

            val quality = lyricsQualityScore(candidate, track.durationMs)
            val writing = LyricsWriting.profile(candidate.lyrics.lines.map { it.text })
            val confidence = sourceConfidence(
                track, candidate, writing, preferWordTiming = preferences.preferredSyncType == LyricsSyncType.WORD,
            )
            val finalScore = (
                metadata * METADATA_WEIGHT +
                    quality * QUALITY_WEIGHT +
                    confidence * SOURCE_WEIGHT
                ).coerceIn(0.0, 1.0)

            CandidateScore(
                candidate = candidate,
                metadataScore = metadata,
                qualityScore = quality,
                sourceConfidence = confidence,
                finalScore = finalScore,
                writing = writing,
            )
        }
    }

    internal fun metadataScore(
        track: Track, candidate: LyricsCandidate,
        onRejected: ((MetadataMatchScore.Rejection) -> Unit)? = null,
    ): Double? = MetadataMatchScore.score(
        requestedTitle = track.title,
        candidateTitle = candidate.matchedTrack.title,
        requestedArtist = track.artists.joinToString(", "),
        candidateArtist = candidate.matchedTrack.artists.joinToString(", "),
        requestedAlbum = track.album.orEmpty(),
        candidateAlbum = candidate.matchedTrack.album.orEmpty(),
        requestedDurationMs = track.durationMs,
        candidateDurationMs = candidate.matchedTrack.durationMs,
        artistQueryCorroborated = candidate.evidence.artistQueryCorroborated,
        requestedArtists = track.artists,
        candidateArtists = candidate.matchedTrack.artists,
        candidateArtistAliases = candidate.evidence.artistAliases,
        onRejected = onRejected,
    )

    internal fun lyricsQualityScore(
        candidate: LyricsCandidate,
        trackDurationMs: Long?,
    ): Double {
        val useful = candidate.lyrics.lines
            .filter { it.text.isNotBlank() && it.text.trim() != "♪" }
        if (useful.isEmpty()) return 0.0

        var quality = 1.0
        // Alternating scripts alone are normal multilingual singing. Penalize only
        // repeated native/romanized pairs sharing (nearly) the same timestamp.
        val pairedRomanization = useful.zipWithNext().count { (left, right) ->
            val leftTime = left.startTimeMsOrNull()
            val rightTime = right.startTimeMsOrNull()
            if (leftTime == null || rightTime == null || abs(leftTime - rightTime) > 750L) false
            else {
                val nativeLeft = LyricsWriting.nativeLanguage(left.text)
                val nativeRight = LyricsWriting.nativeLanguage(right.text)
                (nativeLeft != null && nativeLeft == LyricsWriting.romanizedLanguage(right.text)) ||
                    (nativeRight != null && nativeRight == LyricsWriting.romanizedLanguage(left.text))
            }
        }
        if (pairedRomanization >= 2) quality -= 0.35

        if (
            candidate.lyrics.syncType != LyricsSyncType.PLAIN &&
            trackDurationMs != null &&
            trackDurationMs > 0L
        ) {
            val timed = useful.filterIsInstance<TimedLyricLine>().sortedBy { it.startMs }
            val first = timed.firstOrNull()?.startMs
            val last = timed.lastOrNull()?.startMs

            if (last != null && last > trackDurationMs + 20_000L) quality -= 0.25
            if (last != null && timed.size >= 10 && last < trackDurationMs * 0.50) quality -= 0.15
            if (first != null && first > 75_000L) quality -= 0.08
        }

        return quality.coerceIn(0.0, 1.0)
    }

    internal fun sourceConfidence(
        track: Track, candidate: LyricsCandidate,
        writing: LyricsWriting.Profile = LyricsWriting.profile(candidate.lyrics.lines.map { it.text }),
        preferWordTiming: Boolean = false,
    ): Double {
        // Native lyric evidence works even when a playback source romanizes every metadata field.
        // Explicit English performances retain the ordinary English-source preference.
        val englishVersion = RecordingVersionContext.languageVersion(track.title) == "english" ||
            RecordingVersionContext.languageVersion(track.album.orEmpty()) == "english"
        val japaneseTrack = !englishVersion && (writing.hasOriginal("ja") || containsJapanese(track.title) ||
            track.artists.any(::containsJapanese) ||
            track.album?.let(::containsJapanese) == true)
        val provider = candidate.providerId.value
        val syncType = candidate.lyrics.syncType

        return if (japaneseTrack) {
            when {
                provider.equals(PETITLYRICS_PROVIDER_ID, ignoreCase = true) && syncType == LyricsSyncType.WORD ->
                    if (preferWordTiming) 1.00 else 0.96
                provider.equals(PETITLYRICS_PROVIDER_ID, ignoreCase = true) && syncType == LyricsSyncType.LINE -> 0.96
                provider.equals(LRCLIB_PROVIDER_ID, ignoreCase = true) && syncType != LyricsSyncType.PLAIN -> 0.78
                syncType == LyricsSyncType.PLAIN -> 0.55
                else -> 0.75
            }
        } else {
            when {
                provider.equals(LRCLIB_PROVIDER_ID, ignoreCase = true) && syncType != LyricsSyncType.PLAIN -> 1.00
                provider.equals(PETITLYRICS_PROVIDER_ID, ignoreCase = true) && syncType == LyricsSyncType.WORD ->
                    if (preferWordTiming) 0.92 else 0.88
                provider.equals(PETITLYRICS_PROVIDER_ID, ignoreCase = true) && syncType == LyricsSyncType.LINE -> 0.88
                syncType == LyricsSyncType.PLAIN -> 0.55
                else -> 0.80
            }
        }
    }

    private fun hasUsableLyricContent(candidate: LyricsCandidate): Boolean {
        return candidate.lyrics.lines.any { line ->
            line.text.isNotBlank() && line.text.trim() != "♪"
        }
    }

    private fun hasUsableWordTiming(candidate: LyricsCandidate): Boolean {
        return candidate.lyrics.syncType == LyricsSyncType.WORD &&
            candidate.lyrics.lines.any { it is TimedLyricLine && it.words.isNotEmpty() }
    }

    private fun best(
        scored: Collection<CandidateScore>,
        preferIntactTranslation: Boolean = false,
    ): CandidateScore? {
        return scored.maxWithOrNull(
            compareBy<CandidateScore> { it.finalScore }
                .thenBy { it.metadataScore }
                .thenBy { it.qualityScore }
                .thenBy { it.sourceConfidence }
                // Literal U+FFFD in source translations breaks otherwise equal bilingual ties.
                // It does not make the artifact unusable or override a stronger recording match.
                .thenBy { score ->
                    preferIntactTranslation && score.candidate.lyrics.suppliedTranslation?.lines
                        ?.none { line -> line?.contains('\uFFFD') == true } == true
                }
                .thenBy { stableCandidateKey(it.candidate) },
        )
    }

    private fun stableCandidateKey(candidate: LyricsCandidate): String {
        return buildString {
            append(candidate.providerId.value.lowercase(Locale.ROOT))
            append('|')
            append(candidate.matchedTrack.title.lowercase(Locale.ROOT))
            append('|')
            append(candidate.matchedTrack.artists.joinToString(",").lowercase(Locale.ROOT))
            append('|')
            append(candidate.matchedTrack.album.orEmpty().lowercase(Locale.ROOT))
            append('|')
            append(candidate.matchedTrack.durationMs ?: -1L)
            append('|')
            append(candidate.lyrics.attribution?.sourceId.orEmpty().lowercase(Locale.ROOT))
        }
    }

    private fun containsJapanese(value: String): Boolean = JAPANESE_SCRIPT.containsMatchIn(value)

    internal data class CandidateScore(
        val candidate: LyricsCandidate,
        val metadataScore: Double,
        val qualityScore: Double,
        val sourceConfidence: Double,
        val finalScore: Double,
        val writing: LyricsWriting.Profile,
    )

    private companion object {
        const val MIN_METADATA_SCORE = 0.70

        const val METADATA_WEIGHT = 0.82
        const val QUALITY_WEIGHT = 0.10
        const val SOURCE_WEIGHT = 0.08

        const val KARAOKE_METADATA_TOLERANCE = 0.03
        const val KARAOKE_QUALITY_TOLERANCE = 0.05

        const val LRCLIB_PROVIDER_ID = "lrclib"
        const val PETITLYRICS_PROVIDER_ID = "petitlyrics"

        val JAPANESE_SCRIPT = Regex("[\\u3040-\\u30ff\\u3400-\\u4dbf\\u4e00-\\u9fff]")
    }
}

private fun LyricLine.startTimeMsOrNull(): Long? = (this as? TimedLyricLine)?.startMs
