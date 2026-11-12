package io.github.whoxamxl.aalyrics.provider.selection

import io.github.whoxamxl.aalyrics.core.lyrics.CandidateSelectionPreferences
import io.github.whoxamxl.aalyrics.core.model.*
import io.github.whoxamxl.aalyrics.provider.api.*
import kotlin.test.*

class OriginalWritingSelectionTest {
    private val selector = CrossProviderCandidateSelector()
    private val track = Track("Evening Light", listOf("Example Artist"), "Album", 240_000)
    private val preferences = CandidateSelectionPreferences(preferredTranslationLanguage = "zh")

    @Test fun mixedJapaneseAndEnglishOriginalBeatsRomanizationWithPartialSuppliedTranslation() {
        val romanized = candidate("lrclib", List(45) { "kimi no kokoro ni yume ga aru" })
        val native = candidate("netease", List(45) { if (it % 3 == 0) "We follow the evening light" else "星の道を歩いている" }, true)
        assertEquals(1.0, selector.lyricsQualityScore(native, track.durationMs))
        assertSame(native, selector.select(track, listOf(romanized, native), preferences))
        assertEquals(30, native.lyrics.suppliedTranslation!!.lines.count { it != null })
        val report = selector.selectWithDiagnostics(track, listOf(romanized, native), preferences)
        assertEquals("original-writing-preferred", report.diagnostics.first { it.providerId == "lrclib" }.decision)
        assertSame(native, selector.select(track, listOf(native, romanized), preferences))
        val nativeAlternative = candidate("lrclib", native.lyrics.lines.map { it.text })
            .copy(matchedTrack = track.copy(album = "Film - Album"))
        assertSame(native, selector.select(track, listOf(romanized, nativeAlternative, native), preferences))
    }

    @Test fun latinWritingAloneDoesNotImplyRomanization() {
        val english = candidate("lrclib", List(45) { "We follow the evening light" })
        val japanese = candidate("netease", List(45) { "星の道を歩いている" })
        assertSame(english, selector.select(track, listOf(english, japanese)))
        assertSame(english, selector.select(track, listOf(english)))
    }

    @Test fun wrongVersionCannotBeRescuedByNativeWritingOrTranslations() {
        val englishTrack = track.copy(title = "Evening Light - English Version")
        val english = candidate("lrclib", List(45) { "We follow the evening light" }).copy(matchedTrack = englishTrack)
        val japanese = candidate("netease", List(45) { "星の道を歩いている" }, true)
        assertSame(english, selector.select(englishTrack, listOf(japanese, english), preferences))
        val live = japanese.copy(matchedTrack = track.copy(title = "Evening Light (Live)"))
        assertNull(selector.select(track, listOf(live), preferences))
    }

    @Test fun romanizationRemainsFallbackWhenOriginalIsMissingOrWeak() {
        val romanized = candidate("lrclib", List(45) { "kimi no kokoro ni yume ga aru" })
        assertSame(romanized, selector.select(track, listOf(romanized)))
        val unrelated = candidate("netease", List(45) { "星の道を歩いている" }, true)
            .copy(matchedTrack = track.copy(artists = listOf("Unrelated"), album = null))
        assertSame(romanized, selector.select(track, listOf(romanized, unrelated), preferences))
    }

    @Test fun russianAndKoreanOriginalsDoNotDependOnArtistScript() {
        for ((nativeText, romanizedText) in listOf(
            "Свет над рекой" to "tebya lyublyu moya zhizn",
            "별이 빛나는 길" to "neoga naega saranghae",
        )) {
            val romanized = candidate("lrclib", List(45) { romanizedText })
            val native = candidate("netease", List(45) { nativeText })
            assertSame(native, selector.select(track, listOf(romanized, native)))
        }
    }

    private fun candidate(provider: String, text: List<String>, translated: Boolean = false): LyricsCandidate {
        val attribution = LyricsAttribution(provider, sourceId = provider)
        return LyricsCandidate(LyricsProviderId(provider), track, LyricsDocument(
            text.mapIndexed { i, row -> TimedLyricLine(row, i * 5_000L) }, attribution = attribution,
            suppliedTranslation = if (translated) SuppliedLyricsTranslation("zh",
                text.mapIndexed { i, _ -> if (i % 3 == 0) null else "测试译文" }, attribution) else null,
        ))
    }
}
