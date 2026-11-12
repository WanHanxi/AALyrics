package io.github.whoxamxl.aalyrics.provider.matching

import kotlin.test.*

class LyricsWritingTest {
    @Test fun phonotacticsNeedRepeatedGrammaticalOrLexicalEvidence() {
        val profile = LyricsWriting.profile(List(6) { "chiisana mori no naka de asobu" } + List(6) { "We follow the evening light" })
        assertEquals("ja", profile.suspectedRomanization)
        assertNull(LyricsWriting.profile(listOf("kimi no kokoro ni yume ga aru")).suspectedRomanization)
        assertNull(LyricsWriting.profile(List(6) { "mi casa es tu casa" }).suspectedRomanization)
        assertNull(LyricsWriting.profile(List(6) { "la vita e amore" }).suspectedRomanization)
        assertNull(LyricsWriting.profile(List(6) { "You and I see the sky" }).suspectedRomanization)
    }

    @Test fun realMixedLanguageOriginalIsNotPureRomanization() {
        assertNull(LyricsWriting.profile(List(12) { if (it % 2 == 0) "星の道を歩いている" else "kimi no kokoro ni yume ga aru" }).suspectedRomanization)
    }
}
