package io.github.whoxamxl.aalyrics.provider.selection

import io.github.whoxamxl.aalyrics.core.lyrics.CandidateSelectionPreferences
import io.github.whoxamxl.aalyrics.core.model.*
import io.github.whoxamxl.aalyrics.provider.api.*
import kotlin.test.*

class SuppliedTranslationSelectionTest {
    private val track = Track("Test Song", listOf("Artist"), "Album", 100_000)
    private val selector = CrossProviderCandidateSelector()
    private val chinese = CandidateSelectionPreferences(LyricsSyncType.WORD, "zh")
    private fun candidate(provider: String, translatedCount: Int = 0, track: Track = this.track): LyricsCandidate {
        val attribution = LyricsAttribution(provider, sourceId = "1")
        val lines = (1..10).map { TimedLyricLine("Test row $it", it * 9_000L) }
        return LyricsCandidate(LyricsProviderId(provider), track, LyricsDocument(
            lines, attribution = attribution, suppliedTranslation = if (translatedCount > 0) SuppliedLyricsTranslation(
                "zh", lines.indices.map { if (it < translatedCount) "测试 $it" else null }, attribution,
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
    @Test fun `sparse translations do not earn ranking preference`() {
        val standard = candidate("lrclib")
        assertEquals(standard, selector.select(track, listOf(standard, candidate("netease", 7)), chinese))
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
