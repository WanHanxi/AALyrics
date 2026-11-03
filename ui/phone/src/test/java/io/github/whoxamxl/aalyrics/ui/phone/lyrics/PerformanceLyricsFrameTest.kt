package io.github.whoxamxl.aalyrics.ui.phone.lyrics

import io.github.whoxamxl.aalyrics.core.model.LyricsSyncType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class PerformanceLyricsFrameTest {
    private val lines = listOf("First", "Second", "Third").map {
        LyricsViewportLineUiState(it, translatedText = "Translated $it")
    }

    private fun viewport(index: Int?) = LyricsViewportUiState(
        lines = lines,
        syncType = LyricsSyncType.LINE,
        currentLineIndex = index,
    )

    @Test fun `intro does not promote the first lyric before its timestamp`() {
        val frame = performanceLyricsFrame(viewport(null))!!
        assertNull(frame.previous)
        assertNull(frame.current)
        assertSame(lines[0], frame.next)
    }

    @Test fun `first and last lines have no invented neighbors`() {
        val first = performanceLyricsFrame(viewport(0))!!
        assertNull(first.previous)
        assertSame(lines[0], first.current)
        assertSame(lines[1], first.next)
        val last = performanceLyricsFrame(viewport(2))!!
        assertSame(lines[1], last.previous)
        assertSame(lines[2], last.current)
        assertNull(last.next)
    }

    @Test fun `middle line preserves canonical row and translation identity`() {
        val frame = performanceLyricsFrame(viewport(1))!!
        assertSame(lines[0], frame.previous)
        assertSame(lines[1], frame.current)
        assertSame(lines[2], frame.next)
        assertEquals("Translated Second", frame.current?.translatedText)
    }

    @Test fun `backward seek follows supplied index even while normal viewport is browsing`() {
        performanceLyricsFrame(viewport(2))
        val frame = performanceLyricsFrame(viewport(0).copy(interactionMode = LyricsViewportInteractionMode.BROWSE))!!
        assertEquals(0, frame.currentIndex)
        assertSame(lines[0], frame.current)
    }

    @Test fun `plain and empty lyrics never invent a timed three row frame`() {
        assertNull(performanceLyricsFrame(viewport(1).copy(syncType = LyricsSyncType.PLAIN)))
        assertNull(performanceLyricsFrame(viewport(null).copy(lines = emptyList())))
    }

    @Test fun `word and line sources select the same neighbors`() {
        assertEquals(
            performanceLyricsFrame(viewport(1)),
            performanceLyricsFrame(viewport(1).copy(syncType = LyricsSyncType.WORD)),
        )
    }

    @Test fun `invalid index cannot retain a row from a different document`() {
        for (index in listOf(-1, 3, Int.MAX_VALUE)) {
            val frame = performanceLyricsFrame(viewport(index).copy(lines = listOf(lines[0])))!!
            assertNull(frame.current)
            assertNull(frame.previous)
            assertSame(lines[0], frame.next)
        }
    }

    @Test fun `blank timed row is retained as an instrumental slot`() {
        val blank = LyricsViewportLineUiState("")
        val frame = performanceLyricsFrame(viewport(1).copy(lines = listOf(lines[0], blank, lines[2])))!!
        assertSame(blank, frame.current)
        assertSame(lines[2], frame.next)
    }

    @Test fun `malformed karaoke ranges fall back without crashing either surface`() {
        assertNull(validKaraokeLine("line", KaraokeLineUiState(5)))
        assertNull(validKaraokeLine("line", KaraokeLineUiState(0, KaraokeSweepUiState(-1, 2, 0.5f))))
        assertNull(validKaraokeLine("line", KaraokeLineUiState(0, KaraokeSweepUiState(1, 5, 0.5f))))
        assertNull(validKaraokeLine("line", KaraokeLineUiState(0, KaraokeSweepUiState(1, 1, 0.5f))))
        assertNull(validKaraokeLine("line", KaraokeLineUiState(0, KaraokeSweepUiState(1, 2, Float.NaN))))
        val valid = KaraokeLineUiState(1, KaraokeSweepUiState(1, 4, 0.5f))
        assertSame(valid, validKaraokeLine("line", valid))
    }
}
