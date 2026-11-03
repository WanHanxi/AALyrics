package io.github.whoxamxl.aalyrics.ui.phone.lyrics

import io.github.whoxamxl.aalyrics.core.model.LyricsSyncType

/** Selects neighboring rows from authoritative Phone timing; it never derives a clock. */
internal data class PerformanceLyricsFrame(
    val currentIndex: Int?,
    val previous: LyricsViewportLineUiState?,
    val current: LyricsViewportLineUiState?,
    val next: LyricsViewportLineUiState?,
)

internal fun performanceLyricsFrame(state: LyricsViewportUiState): PerformanceLyricsFrame? {
    if (state.syncType == LyricsSyncType.PLAIN || state.lines.isEmpty()) return null
    val index = state.currentLineIndex?.takeIf { it in state.lines.indices }
    return PerformanceLyricsFrame(
        currentIndex = index,
        previous = index?.let { state.lines.getOrNull(it - 1) },
        current = index?.let(state.lines::get),
        next = state.lines.getOrNull(index?.plus(1) ?: 0),
    )
}
