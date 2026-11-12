package io.github.whoxamxl.aalyrics.provider.selection

import io.github.whoxamxl.aalyrics.core.lyrics.CandidateSelectionPreferences
import io.github.whoxamxl.aalyrics.core.model.*
import io.github.whoxamxl.aalyrics.provider.api.*
import kotlin.test.*

class SuppliedTranslationSelectionTest {
    private val track = Track("Test Song", listOf("Artist"), "Album", 100_000)
    private val selector = CrossProviderCandidateSelector()
    private val chinese = CandidateSelectionPreferences(LyricsSyncType.WORD, "zh")
    private fun candidate(
        provider: String,
        translatedCount: Int = 0,
        track: Track = this.track,
        sourceId: String = "1",
        hasReplacementCharacters: Boolean = false,
    ): LyricsCandidate {
        val attribution = LyricsAttribution(provider, sourceId = sourceId)
        val lines = (1..10).map { TimedLyricLine("Test row $it", it * 9_000L) }
        return LyricsCandidate(LyricsProviderId(provider), track, LyricsDocument(
            lines, attribution = attribution, suppliedTranslation = if (translatedCount > 0) SuppliedLyricsTranslation(
                "zh", lines.indices.map {
                    when {
                        it >= translatedCount -> null
                        it == 0 && hasReplacementCharacters -> "缺字\uFFFD\uFFFD译文"
                        else -> "测试 $it"
                    }
                }, attribution,
            ) else null,
        ))
    }
    @Test fun `Chinese paired candidate wins over source confidence but not for another target or disabled preference`() {
        val standard = candidate("lrclib")
        val paired = candidate("netease", 8)
        for (candidates in listOf(listOf(standard, paired), listOf(paired, standard))) {
            assertEquals(paired, selector.select(track, candidates, chinese))
            assertEquals(standard, selector.select(track, candidates))
            assertEquals(standard, selector.select(track, candidates, chinese.copy(preferredTranslationLanguage = "en")))
        }
    }
    @Test fun `partial translations earn preference without a coverage threshold`() {
        val standard = candidate("lrclib")
        for (count in listOf(1, 3, 7)) {
            val paired = candidate("netease", count)
            assertEquals(paired, selector.select(track, listOf(standard, paired), chinese))
        }
    }
    @Test fun `replacement characters only break equal bilingual ties for the requested target`() {
        val intact = candidate("netease", 10, sourceId = "1")
        val incomplete = candidate("netease", 10, sourceId = "2", hasReplacementCharacters = true)
        for (candidates in listOf(listOf(intact, incomplete), listOf(incomplete, intact))) {
            assertEquals(intact, selector.select(track, candidates, chinese))
            assertEquals(incomplete, selector.select(track, candidates))
            assertEquals(incomplete, selector.select(track, candidates, chinese.copy(preferredTranslationLanguage = "en")))
        }
    }
    @Test fun `translation with missing characters remains preferable to original only when no equal intact pair exists`() {
        val incomplete = candidate("netease", 10, hasReplacementCharacters = true)
        val weakerMatch = candidate("netease", 10, track.copy(album = "Albums"))
        assertEquals(incomplete, selector.select(track, listOf(incomplete), chinese))
        assertEquals(incomplete, selector.select(track, listOf(candidate("lrclib"), incomplete), chinese))
        assertEquals(incomplete, selector.select(track, listOf(weakerMatch, incomplete), chinese))
    }
    @Test fun `translation cannot rescue wrong version inferior metadata or poor timing`() {
        val standard = candidate("lrclib")
        val badCandidates = listOf(
            candidate("netease", 10, track.copy(title = "Test Song (Live)")),
            candidate("netease", 10, track.copy(title = "Test Songs")),
            candidate("netease", 10).let { it.copy(lyrics = it.lyrics.copy(
                lines = it.lyrics.lines.mapIndexed { index, line -> TimedLyricLine(line.text, 80_000L + index * 9_000L) },
            )) },
        )
        badCandidates.forEach { assertEquals(standard, selector.select(track, listOf(standard, it), chinese)) }
    }
}
