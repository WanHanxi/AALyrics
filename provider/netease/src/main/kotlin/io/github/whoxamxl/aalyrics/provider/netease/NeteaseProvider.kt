package io.github.whoxamxl.aalyrics.provider.netease

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.whoxamxl.aalyrics.core.model.*
import io.github.whoxamxl.aalyrics.provider.api.*
import io.github.whoxamxl.aalyrics.provider.lrc.LrcParser
import io.github.whoxamxl.aalyrics.provider.matching.MetadataMatchScore
import kotlinx.coroutines.CancellationException
import okhttp3.OkHttpClient

/** Independent adapter for the anonymous web endpoints verified in November 2026. */
class NeteaseProvider(
    client: OkHttpClient = NeteaseTransport.defaultClient(),
    baseUrl: String = "https://music.163.com/",
) : LyricsProvider {
    private val transport = NeteaseTransport(client, baseUrl)
    override val descriptor = LyricsProviderDescriptor(
        LyricsProviderId("netease"), "NetEase Cloud Music", setOf(LyricsSyncType.LINE),
    )

    override suspend fun search(request: LyricsRequest): List<LyricsCandidate> {
        val track = request.track
        val query = (listOf(track.title) + track.artists).joinToString(" ")
        var songs = discover(query, track)
        if (songs.isEmpty() && query != track.title) songs = discover(track.title, track)
        val candidates = mutableListOf<LyricsCandidate>()
        var failure: Exception? = null
        // Preserve healthy siblings if one lyric request fails. The coordinator isolates total failure.
        for (song in songs.take(3)) {
            try {
                val payload = transport.lyrics(song.id)
                if (payload.boolean("nolyric") == true || payload.boolean("uncollected") == true) continue
                val original = payload.obj("lrc")?.text("lyric").orEmpty()
                val lines = LrcParser.parse(original)
                if (lines.none { it.text.any(Char::isLetterOrDigit) }) continue
                val attribution = attribution(song.id, payload.obj("lyricUser"), original)
                val translated = payload.obj("tlyric")?.text("lyric").orEmpty()
                val translationsByTime = LrcParser.parse(translated).groupBy { it.startMs }
                val originalCounts = lines.groupingBy { it.startMs }.eachCount()
                val paired = lines.map { line ->
                    if (originalCounts[line.startMs] != 1 || !line.text.any(Char::isLetterOrDigit)) null
                    else translationsByTime[line.startMs]?.singleOrNull()?.text?.takeIf {
                        it.any(Char::isLetterOrDigit) && it != line.text
                    }
                }
                candidates += LyricsCandidate(
                    providerId = descriptor.id,
                    matchedTrack = song.track,
                    lyrics = LyricsDocument(
                        lines = lines,
                        attribution = attribution,
                        suppliedTranslation = if (paired.any { it != null }) SuppliedLyricsTranslation(
                            languageTag = "zh",
                            lines = paired,
                            attribution = attribution(song.id, payload.obj("transUser"), translated).let { credits ->
                                // Rights/translator notices can have timestamps without an original counterpart.
                                val notices = LrcParser.parse(translated).filter {
                                    Regex("版权|译者|翻译|copyright|translated by|translation by", RegexOption.IGNORE_CASE)
                                        .containsMatchIn(it.text)
                                }.map { it.text }
                                credits.copy(notices = (credits.notices + notices).distinct())
                            },
                        ) else null,
                    ),
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                failure = error
            }
        }
        if (candidates.isEmpty()) failure?.let { throw it }
        return candidates
    }

    private suspend fun discover(query: String, requested: Track): List<Song> {
        val payload = transport.search(query)
        val result = payload.obj("result") ?: return emptyList()
        return result.array("songs").mapNotNull { value ->
            if (!value.isJsonObject) return@mapNotNull null
            val song = value.asJsonObject
            val id = song.number("id")?.takeIf { it > 0 }?.toString() ?: return@mapNotNull null
            val title = song.text("name")?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val artists = (song.array("ar").ifEmpty { song.array("artists") }).mapNotNull {
                if (it.isJsonObject) it.asJsonObject.text("name")?.takeIf(String::isNotBlank) else null
            }
            val matched = Track(
                title = title,
                artists = artists,
                album = (song.obj("al") ?: song.obj("album"))?.text("name")?.takeIf(String::isNotBlank),
                durationMs = (song.number("dt") ?: song.number("duration"))?.takeIf { it > 0 },
            )
            val score = MetadataMatchScore.score(
                requestedTitle = requested.title, candidateTitle = matched.title,
                requestedArtist = requested.artists.joinToString(", "), candidateArtist = artists.joinToString(", "),
                requestedAlbum = requested.album.orEmpty(), candidateAlbum = matched.album.orEmpty(),
                requestedDurationMs = requested.durationMs, candidateDurationMs = matched.durationMs,
            ) ?: return@mapNotNull null
            if (score < 0.70) null else Song(id, matched, score)
        }.distinctBy { it.id }.sortedWith(compareByDescending<Song> { it.score }.thenBy { it.id })
    }

    private fun attribution(id: String, contributor: JsonObject?, lrc: String): LyricsAttribution =
        LyricsAttribution(
            providerId = descriptor.id.value,
            displayName = descriptor.displayName,
            sourceId = id,
            contributors = listOfNotNull(contributor?.text("nickname")?.takeIf(String::isNotBlank)),
            // Timed credits remain in the lyric rows. Retain non-timed headers/JSON credits too.
            notices = lrc.lines().filter { it.isNotBlank() && LrcParser.parse(it).isEmpty() }.map { credit ->
                try {
                    JsonParser.parseString(credit).asJsonObject.array("c").joinToString("") {
                        if (it.isJsonObject) it.asJsonObject.text("tx").orEmpty() else ""
                    }.ifBlank { credit }
                } catch (_: Exception) { credit }
            },
        )

    private data class Song(val id: String, val track: Track, val score: Double)
}
