package io.github.whoxamxl.aalyrics.ui.phone.lyrics

import kotlin.math.roundToInt

/** Placement of measured rows. Null neighbors are omitted, never clipped into the current row. */
internal data class PerformanceLyricsPlacement(
    val previousTop: Int?,
    val currentTop: Int,
    val nextTop: Int?,
)

internal fun performanceLyricsPlacement(
    height: Int,
    previousHeight: Int,
    currentHeight: Int,
    nextHeight: Int,
    gap: Int,
): PerformanceLyricsPlacement {
    val before = if (previousHeight > 0) previousHeight + gap else 0
    val after = if (nextHeight > 0) nextHeight + gap else 0
    // Preserve upcoming context first. The current block has already been constrained to the
    // viewport by its scroll container, so even oversized original/translated text stays reachable.
    val showPrevious = before > 0 && before + currentHeight + after <= height
    val showNext = after > 0 && currentHeight + after <= height
    val minimumTop = if (showPrevious) before else 0
    val maximumTop = (height - currentHeight - if (showNext) after else 0)
        .coerceAtLeast(minimumTop)
    val currentTop = (height * 0.44f - currentHeight / 2f).roundToInt()
        .coerceIn(minimumTop, maximumTop)
    return PerformanceLyricsPlacement(
        previousTop = if (showPrevious) currentTop - before else null,
        currentTop = currentTop,
        nextTop = if (showNext) currentTop + currentHeight + gap else null,
    )
}
