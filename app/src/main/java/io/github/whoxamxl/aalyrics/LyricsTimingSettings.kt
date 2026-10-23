package io.github.whoxamxl.aalyrics

import io.github.whoxamxl.aalyrics.core.timing.LyricsTimingOffset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** One snapshot keeps multi-setting reset and the derived car total coherent. */
internal data class LyricsTimingOffsets(
    val globalMs: Long = 0L,
    val androidAutoMs: Long = 0L,
) {
    val global: LyricsTimingOffset get() = LyricsTimingOffset(globalMs)

    // Each preference is bounded independently. Do not clamp the sum or apply it twice.
    val automotive: LyricsTimingOffset get() = LyricsTimingOffset(globalMs + androidAutoMs)
}

/** Application-owned calibration. Actions and reset are called on the main thread. */
internal class LyricsTimingSettings(
    readOffsets: () -> LyricsTimingOffsets,
    private val writeOffsets: (LyricsTimingOffsets) -> Unit,
) {
    private val mutableOffsets = MutableStateFlow(readOffsets().bounded())
    val offsets: StateFlow<LyricsTimingOffsets> = mutableOffsets.asStateFlow()

    fun adjustBy(deltaMs: Long) = setOffset(offsets.value.globalMs + deltaMs)

    fun setOffset(milliseconds: Long) = save(offsets.value.copy(globalMs = milliseconds))

    fun adjustAndroidAutoBy(deltaMs: Long) = setAndroidAutoOffset(offsets.value.androidAutoMs + deltaMs)

    fun setAndroidAutoOffset(milliseconds: Long) = save(offsets.value.copy(androidAutoMs = milliseconds))

    fun resetToDefaults() = save(LyricsTimingOffsets())

    private fun save(offsets: LyricsTimingOffsets) {
        val bounded = offsets.bounded()
        writeOffsets(bounded)
        mutableOffsets.value = bounded
    }

    private fun LyricsTimingOffsets.bounded() = copy(
        globalMs = globalMs.coerceIn(-MAX_OFFSET_MS, MAX_OFFSET_MS),
        androidAutoMs = androidAutoMs.coerceIn(-MAX_OFFSET_MS, MAX_OFFSET_MS),
    )

    companion object {
        const val MAX_OFFSET_MS = 30_000L
    }
}
