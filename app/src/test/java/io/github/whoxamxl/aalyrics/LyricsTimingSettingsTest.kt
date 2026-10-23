package io.github.whoxamxl.aalyrics

import io.github.whoxamxl.aalyrics.core.timing.LyricsTimingOffset
import kotlin.test.Test
import kotlin.test.assertEquals

class LyricsTimingSettingsTest {
    @Test
    fun `existing global offset remains effective with zero AA compensation by default`() {
        val settings = LyricsTimingSettings({ LyricsTimingOffsets(globalMs = -275L) }, {})
        assertEquals(-275L, settings.offsets.value.global.milliseconds)
        assertEquals(0L, settings.offsets.value.androidAutoMs)
        assertEquals(settings.offsets.value.global, settings.offsets.value.automotive)
    }

    @Test
    fun `both offsets are immediately observable and restored after process recreation`() {
        var saved = LyricsTimingOffsets()
        fun settings() = LyricsTimingSettings({ saved }, { saved = it })
        val first = settings()
        first.setOffset(125L)
        first.setAndroidAutoOffset(-750L)
        assertEquals(LyricsTimingOffsets(125L, -750L), first.offsets.value)
        assertEquals(first.offsets.value, settings().offsets.value)
        assertEquals(LyricsTimingOffset(-625L), settings().offsets.value.automotive)
    }

    @Test
    fun `exact values and fine adjustments retain precision and the other setting`() {
        var saved = LyricsTimingOffsets(500L, -750L)
        val settings = LyricsTimingSettings({ saved }, { saved = it })
        settings.setOffset(-275L)
        settings.adjustBy(50L)
        assertEquals(LyricsTimingOffsets(-225L, -750L), saved)
        settings.setAndroidAutoOffset(125L)
        settings.adjustAndroidAutoBy(-50L)
        assertEquals(LyricsTimingOffsets(-225L, 75L), saved)
        assertEquals(saved, LyricsTimingSettings({ saved }, {}).offsets.value)
    }

    @Test
    fun `individual resets preserve the other offset`() {
        var saved = LyricsTimingOffsets(500L, -750L)
        val settings = LyricsTimingSettings({ saved }, { saved = it })
        settings.setOffset(0L)
        assertEquals(LyricsTimingOffsets(0L, -750L), saved)
        settings.setOffset(500L)
        settings.setAndroidAutoOffset(0L)
        assertEquals(LyricsTimingOffsets(500L, 0L), saved)
        assertEquals(saved, settings.offsets.value)
    }

    @Test
    fun `app reset persists and publishes both defaults in one snapshot`() {
        var saved = LyricsTimingOffsets(500L, -750L)
        val writes = mutableListOf<LyricsTimingOffsets>()
        val settings = LyricsTimingSettings({ saved }, { saved = it; writes.add(it) })
        settings.resetToDefaults()
        assertEquals(listOf(LyricsTimingOffsets()), writes)
        assertEquals(LyricsTimingOffsets(), settings.offsets.value)
        assertEquals(LyricsTimingOffsets(), LyricsTimingSettings({ saved }, {}).offsets.value)
    }

    @Test
    fun `global controls stop at either limit and can move back toward zero`() {
        var saved = LyricsTimingOffsets(29_900L, -750L)
        val settings = LyricsTimingSettings({ saved }, { saved = it })
        settings.adjustBy(500L)
        assertEquals(30_000L, saved.globalMs)
        settings.adjustBy(50L)
        assertEquals(30_000L, saved.globalMs)
        settings.adjustBy(-50L)
        assertEquals(29_950L, saved.globalMs)
        repeat(120) { settings.adjustBy(-500L) }
        assertEquals(-30_000L, saved.globalMs)
        settings.adjustBy(500L)
        assertEquals(-29_500L, saved.globalMs)
        assertEquals(-750L, saved.androidAutoMs)
    }

    @Test
    fun `AA controls have independent limits and do not clamp the combined total`() {
        var saved = LyricsTimingOffsets(30_000L, 29_900L)
        val settings = LyricsTimingSettings({ saved }, { saved = it })
        settings.adjustAndroidAutoBy(500L)
        assertEquals(LyricsTimingOffset(60_000L), settings.offsets.value.automotive)
        settings.adjustAndroidAutoBy(50L)
        assertEquals(30_000L, saved.androidAutoMs)
        settings.adjustAndroidAutoBy(-50L)
        assertEquals(29_950L, saved.androidAutoMs)
        repeat(120) { settings.adjustAndroidAutoBy(-500L) }
        assertEquals(-30_000L, saved.androidAutoMs)
        assertEquals(LyricsTimingOffset.ZERO, settings.offsets.value.automotive)
        settings.adjustAndroidAutoBy(500L)
        assertEquals(-29_500L, saved.androidAutoMs)
        assertEquals(30_000L, saved.globalMs)
    }

    @Test
    fun `restored and explicitly set out of range values are bounded independently`() {
        val settings = LyricsTimingSettings({ LyricsTimingOffsets(Long.MAX_VALUE, Long.MIN_VALUE) }, {})
        assertEquals(LyricsTimingOffsets(30_000L, -30_000L), settings.offsets.value)
        settings.setOffset(Long.MIN_VALUE)
        settings.setAndroidAutoOffset(Long.MAX_VALUE)
        assertEquals(LyricsTimingOffsets(-30_000L, 30_000L), settings.offsets.value)
    }
}
