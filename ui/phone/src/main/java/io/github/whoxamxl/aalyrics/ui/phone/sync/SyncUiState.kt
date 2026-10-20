package io.github.whoxamxl.aalyrics.ui.phone.sync

import androidx.compose.runtime.Immutable
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.LyricsViewportLineUiState

enum class SyncAvailability { NO_MEDIA, LOADING, UNAVAILABLE, PLAIN, TIMED }

/** Presentation facts only; application ownership supplies the calibrated current line. */
@Immutable
data class SyncScreenUiState(
    val offsetMs: Long = 0L,
    val availability: SyncAvailability = SyncAvailability.NO_MEDIA,
    val trackTitle: String? = null,
    val canAdvance: Boolean = false,
    val canDelay: Boolean = false,
    val previousLine: LyricsViewportLineUiState? = null,
    val currentLine: LyricsViewportLineUiState? = null,
    val nextLine: LyricsViewportLineUiState? = null,
)

enum class SyncAdjustment { ADVANCE_FINE, DELAY_FINE, ADVANCE_COARSE, DELAY_COARSE }
