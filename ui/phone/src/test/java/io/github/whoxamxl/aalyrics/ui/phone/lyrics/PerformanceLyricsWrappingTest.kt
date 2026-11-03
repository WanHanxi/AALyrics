package io.github.whoxamxl.aalyrics.ui.phone.lyrics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PerformanceLyricsWrappingTest {
    @Test
    fun `reported Chinese lyric breaks after the complete phrase`() {
        assertEquals("原來幻想中的這天\n會比幻想更唯美", wrap("原來幻想中的這天 會比幻想更唯美", 8))
    }

    @Test
    fun `a complete line that fits keeps its original spaces`() {
        val text = "原來幻想中的這天 會比幻想更唯美"
        assertEquals(text, wrap(text, 16))
    }

    @Test
    fun `short phrases share a line while the next phrase moves intact`() {
        assertEquals("迎著 風\n慢慢 向前走", wrap("迎著 風 慢慢 向前走", 6))
    }

    @Test
    fun `oversized phrases remain available for platform soft wrapping`() {
        val longPhrase = "沒有空格的很長很長的一句話"
        assertEquals(longPhrase, wrap(longPhrase, 4))
        assertEquals("前句\n$longPhrase\n後句", wrap("前句 $longPhrase 後句", 4))
    }

    @Test
    fun `existing paragraphs and blank lines retain their boundaries`() {
        assertEquals(
            "前句\r\n\n原來幻想中的這天\n會比幻想更唯美\n尾聲",
            wrap("前句\r\n\n原來幻想中的這天 會比幻想更唯美\n尾聲", 8),
        )
    }

    @Test
    fun `full width spaces and repeated separators do not indent the next line`() {
        assertEquals("原來幻想中的這天 \t\n會比幻想更唯美", wrap("原來幻想中的這天 \t\u3000會比幻想更唯美", 8))
    }

    @Test
    fun `nonbreaking spaces are not chosen as phrase boundaries`() {
        val text = "hello\u00a0world\u202fagain"
        assertEquals(text, wrap(text, 6))
    }

    @Test
    fun `empty text and outer whitespace do not create extra line breaks`() {
        assertEquals("", wrap("", 4))
        assertEquals(" \t ", wrap(" \t ", 1))
        assertEquals("  天空\n晴朗  ", wrap("  天空 晴朗  ", 4))
    }

    @Test
    fun `uses measured glyph widths rather than character counts`() {
        val text = "WWW iii iii"
        val display = performanceWrappedText(text) { candidate ->
            candidate.sumOf { if (it == 'W') 3 else 1 } <= 9
        }
        assertEquals("WWW\niii iii", display)
    }

    @Test
    fun `rotation or font size changes can select different breaks`() {
        val text = "迎著風 慢慢 向前走"
        assertEquals("迎著風\n慢慢\n向前走", wrap(text, 4))
        assertEquals("迎著風 慢慢\n向前走", wrap(text, 7))
        assertEquals(text, wrap(text, 10))
    }

    @Test
    fun `karaoke offsets and all nonseparator UTF16 units survive including emoji`() {
        val text = "抬頭看🌙 讓歌聲繼續"
        val display = wrap(text, 6)
        val phraseStart = text.indexOf('讓')
        val karaoke = KaraokeLineUiState(
            completedEnd = phraseStart,
            sweep = KaraokeSweepUiState(start = phraseStart, end = text.length, progress = 0.5f),
        )
        assertTrue(display.contains('\n'))
        assertEquals(text.length, display.length)
        text.indices.forEach { index ->
            if (text[index] != ' ') assertEquals(text[index], display[index])
        }
        assertEquals(karaoke, validKaraokeLine(display, karaoke))
        assertEquals("讓歌聲繼續", display.substring(karaoke.sweep!!.start, karaoke.sweep.end))
        val crossing = KaraokeLineUiState(
            completedEnd = 2,
            sweep = KaraokeSweepUiState(start = 2, end = phraseStart + 2, progress = 0.5f),
        )
        assertEquals(crossing, validKaraokeLine(display, crossing))
        assertEquals("看🌙\n讓歌", display.substring(crossing.sweep!!.start, crossing.sweep.end))
    }

    private fun wrap(text: String, width: Int): String = performanceWrappedText(text) { candidate ->
        candidate.lineSequence().all { it.length <= width }
    }
}
