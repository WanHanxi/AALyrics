package io.github.whoxamxl.aalyrics.provider.selection

import io.github.whoxamxl.aalyrics.core.lyrics.CandidateSelectionPreferences
import io.github.whoxamxl.aalyrics.core.model.*
import io.github.whoxamxl.aalyrics.provider.api.*

/** Opt-in benchmark; synthetic text only. No timing assertions. */
object MatchingBenchmark {
    @Volatile private var sink: LyricsCandidate? = null

    @JvmStatic fun main(args: Array<String>) {
        val selector = CrossProviderCandidateSelector()
        val mixedTiming = "--mixed-timing" in args || "--karaoke" in args
        val preferences = CandidateSelectionPreferences(
            preferredSyncType = LyricsSyncType.WORD.takeIf { "--karaoke" in args },
            preferredTranslationLanguage = "zh",
        )
        val fixtures = listOf(
            Track("Evening Light", listOf("Example Band"), "First Album", 240_000),
            Track("星の道", listOf("架空歌手"), "夜", 240_000),
            Track("夜裡的風", listOf("樂隊 Example Band"), "夢", 240_000),
            Track("Echo (feat. Guest)", listOf("Singer", "Guest"), "Second Album", 240_000),
            Track("Blue - English Version", listOf("Example Band"), "Versions", 240_000),
            Track("Northern Light", listOf("Svet & Mir", "Novy"), "Album", 240_000),
        ).mapIndexed { fixtureIndex, track ->
            val candidates = (0 until 8).map { candidateIndex ->
                val provider = when {
                    mixedTiming && candidateIndex % 4 == 2 -> "musixmatch"
                    mixedTiming && candidateIndex % 4 == 3 -> "synclrc"
                    candidateIndex % 2 == 0 -> "lrclib"
                    else -> "netease"
                }
                val attribution = LyricsAttribution(provider, sourceId = "$candidateIndex")
                val lines = (0 until 60).map { line ->
                    val text = when {
                        fixtureIndex == 1 && candidateIndex == 0 -> "kimi no kokoro ni yume ga aru"
                        fixtureIndex == 1 && line % 3 != 0 -> "星の道を歩いている"
                        fixtureIndex == 2 -> "夜里的风轻轻吹过"
                        fixtureIndex == 5 -> "Свет над рекой"
                        else -> "We follow the evening light"
                    }
                    // 75% distinct rows; repeated choruses must not dominate the CPU sample.
                    val row = if (line % 4 == 0) text else "$text $line"
                    TimedLyricLine(row, line * 3_800L, words = if (provider in setOf("musixmatch", "synclrc")) {
                        row.split(" ").mapIndexed { index, token -> TimedWord(token, line * 3_800L + index * 300) }
                    } else emptyList())
                }
                val matched = track.copy(
                    title = when {
                        candidateIndex >= 6 -> "${track.title} (Live)"
                        fixtureIndex == 2 -> "夜里的风"
                        fixtureIndex == 3 -> "Echo"
                        fixtureIndex == 4 -> "Blue (English ver.)"
                        else -> track.title
                    },
                    durationMs = 240_000L + candidateIndex * 400,
                )
                LyricsCandidate(LyricsProviderId(provider), matched, LyricsDocument(
                    lines, attribution = attribution,
                    suppliedTranslation = if (provider == "netease") SuppliedLyricsTranslation(
                        "zh", lines.mapIndexed { i, _ -> if (i % 3 == 0) null else "测试译文" }, attribution,
                    ) else null,
                ))
            }
            track to candidates
        }
        val coldStart = System.nanoTime()
        sink = selector.select(fixtures[2].first, fixtures[2].second, preferences)
        println("COLD case=2 first_selection_ms=${(System.nanoTime() - coldStart) / 1_000_000.0}")
        repeat(2_400) { i ->
            val (track, candidates) = fixtures[i % fixtures.size]
            sink = selector.select(track, candidates, preferences)
        }
        fixtures.forEachIndexed { index, (track, candidates) ->
            val nanos = LongArray(1_000) {
                val start = System.nanoTime()
                sink = selector.select(track, candidates, preferences)
                System.nanoTime() - start
            }.sorted()
            println("CPU case=$index candidates=${candidates.size} rows=60 p50_us=${nanos[500] / 1_000.0} p95_us=${nanos[950] / 1_000.0} winner=${sink?.providerId}")
        }
    }
}
