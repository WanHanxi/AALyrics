package io.github.whoxamxl.aalyrics

import io.github.whoxamxl.aalyrics.core.model.LyricsSyncType
import io.github.whoxamxl.aalyrics.core.timing.LyricsTimingOffset
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.LyricsScreenUiState
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.TrackCardLyricsStatus
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncAdjustment
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncAvailability
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncScreenUiState

internal fun mapPhoneSyncState(
    lyrics: LyricsScreenUiState,
    offset: LyricsTimingOffset,
): SyncScreenUiState {
    val viewport = lyrics.viewport
    val availability = when (lyrics.trackCard.lyricsStatus) {
        TrackCardLyricsStatus.IDLE -> SyncAvailability.NO_MEDIA
        TrackCardLyricsStatus.LOADING -> SyncAvailability.LOADING
        TrackCardLyricsStatus.NOT_FOUND, TrackCardLyricsStatus.FAILED -> SyncAvailability.UNAVAILABLE
        TrackCardLyricsStatus.READY -> when {
            viewport.lines.isEmpty() -> SyncAvailability.UNAVAILABLE
            viewport.syncType == LyricsSyncType.PLAIN -> SyncAvailability.PLAIN
            else -> SyncAvailability.TIMED
        }
    }
    val timed = availability == SyncAvailability.TIMED
    val index = viewport.currentLineIndex
    return SyncScreenUiState(
        offsetMs = offset.milliseconds,
        availability = availability,
        trackTitle = lyrics.trackCard.title.takeUnless { availability == SyncAvailability.NO_MEDIA },
        canAdvance = timed && offset.milliseconds < LyricsTimingSettings.MAX_OFFSET_MS,
        canDelay = timed && offset.milliseconds > -LyricsTimingSettings.MAX_OFFSET_MS,
        previousLine = if (timed && index != null) viewport.lines.getOrNull(index - 1) else null,
        currentLine = if (timed && index != null) viewport.lines.getOrNull(index) else null,
        nextLine = if (timed) viewport.lines.getOrNull(index?.plus(1) ?: 0) else null,
    )
}

internal fun SyncAdjustment.deltaMs(): Long = when (this) {
    SyncAdjustment.ADVANCE_FINE -> 100L
    SyncAdjustment.DELAY_FINE -> -100L
    SyncAdjustment.ADVANCE_COARSE -> 500L
    SyncAdjustment.DELAY_COARSE -> -500L
}
