package io.github.whoxamxl.aalyrics.ui.phone.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import io.github.whoxamxl.aalyrics.core.model.LyricsSyncType
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsTheme
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.LyricsScreen
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.LyricsScreenUiState
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.LyricsViewportLineUiState
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.LyricsViewportUiState
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.PerformanceLyricsScreen
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.TrackCardLyricsStatus
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.TrackCardUiState

@Preview(name = "Album · portrait", group = "Performance", widthDp = 412, heightDp = 892)
@Preview(name = "Album · landscape", group = "Performance", widthDp = 892, heightDp = 412)
@Composable
internal fun PerformanceAlbumPreview() {
    PerformancePreview(performanceAlbumState, rememberPerformanceCover(PerformanceCover.NIGHT))
}

@Preview(name = "White cover · contrast", group = "Performance", widthDp = 412, heightDp = 892)
@Preview(name = "White cover · landscape", group = "Performance", widthDp = 740, heightDp = 360)
@Composable
internal fun PerformanceWhiteCoverPreview() {
    PerformancePreview(performanceAlbumState, rememberPerformanceCover(PerformanceCover.WHITE))
}

@Preview(name = "Busy cover · narrow", group = "Performance", widthDp = 320, heightDp = 640)
@Composable
internal fun PerformanceBusyCoverPreview() {
    PerformancePreview(performanceAlbumState, rememberPerformanceCover(PerformanceCover.BUSY))
}

@Preview(name = "Artwork change and removal · interactive", group = "Performance", widthDp = 412, heightDp = 892)
@Composable
internal fun PerformanceArtworkChangePreview() {
    // Use Preview's interactive mode to cycle cover -> bright cover -> no artwork with the exit icon.
    var index by remember { mutableStateOf(0) }
    val artwork = when (index % 3) {
        0 -> rememberPerformanceCover(PerformanceCover.NIGHT)
        1 -> rememberPerformanceCover(PerformanceCover.WHITE)
        else -> null
    }
    AALyricsTheme {
        PerformanceLyricsScreen(performanceAlbumState, onExit = { index++ }, artwork = artwork)
    }
}

private val performanceAlbumState = LyricsScreenUiState(
    trackCard = TrackCardUiState(
        title = "晚风片段",
        artist = "排版示例",
        providerLabel = "Musixmatch",
        lyricsStatus = TrackCardLyricsStatus.READY,
    ),
    viewport = LyricsViewportUiState(
        syncType = LyricsSyncType.LINE,
        currentLineIndex = 1,
        lines = listOf(
            LyricsViewportLineUiState("街灯在身后亮起"),
            LyricsViewportLineUiState("就让晚风慢一点", translatedText = "Let the evening breeze linger."),
            LyricsViewportLineUiState("把这一刻留在眼前"),
        ),
    ),
)

@Preview(name = "Three rows · translated", group = "Performance", widthDp = 412, heightDp = 892)
@Preview(name = "Landscape", group = "Performance", widthDp = 892, heightDp = 412)
@Composable
internal fun PerformanceTranslatedPreview() {
    PerformancePreview(PhonePreviewFixtures.lyricsScreenTranslatedLine)
}

@Preview(name = "WORD sweep", group = "Performance", widthDp = 412, heightDp = 892)
@Composable
internal fun PerformanceKaraokePreview() {
    PerformancePreview(
        PhonePreviewFixtures.lyricsScreenWord.let {
            it.copy(viewport = it.viewport.copy(syncType = LyricsSyncType.WORD))
        },
        rememberPerformanceCover(PerformanceCover.NIGHT),
    )
}

@Preview(name = "Intro", group = "Performance", widthDp = 412, heightDp = 892)
@Composable
private fun PerformanceIntroPreview() {
    PerformancePreview(
        PhonePreviewFixtures.lyricsScreenLine.let {
            it.copy(viewport = it.viewport.copy(currentLineIndex = null))
        },
        rememberPerformanceCover(PerformanceCover.BUSY),
    )
}

@Preview(name = "Last line", group = "Performance", widthDp = 412, heightDp = 892)
@Composable
private fun PerformanceLastLinePreview() {
    PerformancePreview(
        PhonePreviewFixtures.lyricsScreenLine.let {
            it.copy(viewport = it.viewport.copy(currentLineIndex = it.viewport.lines.lastIndex))
        },
    )
}

@Preview(name = "Long lines · large font", group = "Performance", widthDp = 360, heightDp = 640, fontScale = 1.8f)
@Preview(name = "Long lines · landscape", group = "Performance", widthDp = 740, heightDp = 360, fontScale = 1.3f)
@Composable
internal fun PerformanceLongLinesPreview() {
    PerformancePreview(
        PhonePreviewFixtures.lyricsScreenLine.let {
            it.copy(viewport = it.viewport.copy(
                currentLineIndex = 1,
                lines = listOf(
                    LyricsViewportLineUiState("We keep the rhythm while the city moves"),
                    LyricsViewportLineUiState(
                        "Every light across the water carries a memory of the places we have been",
                        translatedText = "水面に映る一つひとつの光が、私たちが歩んできた場所の記憶を運んでくる",
                    ),
                    LyricsViewportLineUiState("And the morning finds us here again"),
                ),
            ))
        },
        rememberPerformanceCover(PerformanceCover.NIGHT),
    )
}

@Preview(name = "PLAIN · scrollable", group = "Performance", widthDp = 412, heightDp = 892)
@Composable
private fun PerformancePlainPreview() {
    PerformancePreview(PhonePreviewFixtures.lyricsScreenTranslatedPlain)
}

@Preview(name = "Loading", group = "Performance", widthDp = 412, heightDp = 892)
@Composable
private fun PerformanceLoadingPreview() {
    PerformanceStatusPreview(TrackCardLyricsStatus.LOADING)
}

@Preview(name = "No media", group = "Performance", widthDp = 412, heightDp = 892)
@Composable
private fun PerformanceIdlePreview() {
    PerformanceStatusPreview(TrackCardLyricsStatus.IDLE)
}

@Preview(name = "Not found", group = "Performance", widthDp = 412, heightDp = 892)
@Composable
private fun PerformanceNotFoundPreview() {
    PerformanceStatusPreview(TrackCardLyricsStatus.NOT_FOUND)
}

@Preview(name = "Failure", group = "Performance", widthDp = 412, heightDp = 892)
@Composable
private fun PerformanceFailedPreview() {
    PerformanceStatusPreview(TrackCardLyricsStatus.FAILED)
}

@Composable
private fun PerformanceStatusPreview(status: TrackCardLyricsStatus) {
    PerformancePreview(
        PhonePreviewFixtures.lyricsScreenLine.let {
            it.copy(trackCard = it.trackCard.copy(lyricsStatus = status), viewport = it.viewport.copy(lines = emptyList()))
        },
    )
}

@Preview(name = "Entry and exit · interactive", group = "Performance", widthDp = 412, heightDp = 892)
@Composable
internal fun PerformanceEntryExitPreview() {
    AALyricsTheme {
        var fullscreen by remember { mutableStateOf(false) }
        if (fullscreen) {
            PerformanceLyricsScreen(PhonePreviewFixtures.lyricsScreenTranslatedLine, onExit = { fullscreen = false })
        } else {
            LyricsScreen(PhonePreviewFixtures.lyricsScreenTranslatedLine, onEnterPerformanceMode = { fullscreen = true })
        }
    }
}

@Composable
private fun PerformancePreview(initialState: LyricsScreenUiState, artwork: ImageBitmap? = null) {
    AALyricsTheme {
        var state by remember(initialState) { mutableStateOf(initialState) }
        PerformanceLyricsScreen(
            state = state,
            onExit = {},
            artwork = artwork,
            onViewportInteractionModeChange = {
                state = state.copy(viewport = state.viewport.copy(interactionMode = it))
            },
        )
    }
}
