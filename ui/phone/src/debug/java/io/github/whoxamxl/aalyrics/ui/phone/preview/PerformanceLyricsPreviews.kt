package io.github.whoxamxl.aalyrics.ui.phone.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import io.github.whoxamxl.aalyrics.core.model.LyricsSyncType
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsTheme
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.LyricsScreen
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.LyricsScreenUiState
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.LyricsViewportLineUiState
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.PerformanceLyricsScreen
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.TrackCardLyricsStatus

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
    )
}

@Preview(name = "Intro", group = "Performance", widthDp = 412, heightDp = 892)
@Composable
private fun PerformanceIntroPreview() {
    PerformancePreview(
        PhonePreviewFixtures.lyricsScreenLine.let {
            it.copy(viewport = it.viewport.copy(currentLineIndex = null))
        },
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
private fun PerformancePreview(initialState: LyricsScreenUiState) {
    AALyricsTheme {
        var state by remember(initialState) { mutableStateOf(initialState) }
        PerformanceLyricsScreen(
            state = state,
            onExit = {},
            onViewportInteractionModeChange = {
                state = state.copy(viewport = state.viewport.copy(interactionMode = it))
            },
        )
    }
}
