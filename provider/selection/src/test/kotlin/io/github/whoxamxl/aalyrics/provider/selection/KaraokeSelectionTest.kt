package io.github.whoxamxl.aalyrics.provider.selection

import io.github.whoxamxl.aalyrics.core.lyrics.CandidateSelectionPreferences
import io.github.whoxamxl.aalyrics.core.model.*
import io.github.whoxamxl.aalyrics.provider.api.*
import kotlin.test.Test
import kotlin.test.assertEquals

class KaraokeSelectionTest {
    private val selector = CrossProviderCandidateSelector()
    private val track = Track("Evening Light", listOf("Example Artist"), "Album", 100_000L)
    private val normal = CandidateSelectionPreferences(preferredTranslationLanguage = "zh")
    private val karaoke = normal.copy(preferredSyncType = LyricsSyncType.WORD)

    private fun candidate(provider: String, word: Boolean = false, translations: Int = 0, id: String = "1"): LyricsCandidate {
        val attribution = LyricsAttribution(provider, sourceId = id)
        val lines = (1..10).map { index ->
            TimedLyricLine("Evening light $index", index * 9_000L, words = if (word) {
                listOf(TimedWord("Evening", index * 9_000L), TimedWord(" light $index", index * 9_000L + 500))
            } else emptyList())
        }
        return LyricsCandidate(LyricsProviderId(provider), track, LyricsDocument(
            lines, attribution = attribution,
            suppliedTranslation = if (translations > 0) SuppliedLyricsTranslation(
                "zh", lines.indices.map { if (it < translations) "暮色 $it" else null }, attribution,
            ) else null,
        ))
    }

    @Test fun `karaoke off prefers partial supplied translation and on prefers comparable word timing`() {
        val paired = candidate("netease", translations = 6)
        val word = candidate("musixmatch", word = true)
        val line = candidate("lrclib")
        for (candidates in listOf(listOf(line, paired, word), listOf(word, paired, line))) {
            assertEquals(paired, selector.select(track, candidates, normal))
            assertEquals(word, selector.select(track, candidates, karaoke))
        }
        assertEquals(paired, selector.select(track, listOf(line, paired), karaoke))
    }

    @Test fun `word sources remain eligible with karaoke off without displacing a stronger ordinary source`() {
        val word = candidate("synclrc", word = true)
        val line = candidate("lrclib")
        assertEquals(word, selector.select(track, listOf(word), normal))
        assertEquals(line, selector.select(track, listOf(line, word), normal))
        assertEquals(word, selector.select(track, listOf(line, word), karaoke))
    }

    @Test fun `same provider receives no hidden word confidence advantage while karaoke is off`() {
        val line = candidate("petitlyrics", id = "z-line")
        val word = candidate("petitlyrics", word = true, id = "a-word")
        assertEquals(line, selector.select(track, listOf(line, word), normal))
        assertEquals(word, selector.select(track, listOf(line, word), karaoke))
    }

    @Test fun `karaoke preserves partial translations when comparable word lyrics provide them`() {
        val word = candidate("musixmatch", word = true)
        val pairedWord = candidate("synclrc", word = true, translations = 2)
        val pairedLine = candidate("netease", translations = 10)
        assertEquals(pairedWord, selector.select(track, listOf(word, pairedWord, pairedLine), karaoke))
    }

    @Test fun `karaoke cannot override version or timing quality for word lyrics`() {
        val paired = candidate("netease", translations = 6)
        val word = candidate("synclrc", word = true)
        val live = word.copy(matchedTrack = track.copy(title = "Evening Light (Live)"))
        val late = word.copy(lyrics = word.lyrics.copy(lines = word.lyrics.lines.map { row ->
            val timed = row as TimedLyricLine
            timed.copy(startMs = timed.startMs + 100_000, words = timed.words.map { it.copy(startMs = it.startMs + 100_000) })
        }))
        for (bad in listOf(live, late)) {
            assertEquals(paired, selector.select(track, listOf(paired, bad), karaoke))
        }
    }
}
