package io.github.whoxamxl.aalyrics.ui.phone.preview

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsTheme
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.LyricsViewportLineUiState
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncAvailability
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncScreen
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncScreenUiState

@Preview(name = "Timed lyrics and translation", group = "Sync", widthDp = 412, heightDp = 820)
@Preview(name = "Narrow and large font", group = "Sync", widthDp = 320, heightDp = 640, fontScale = 1.3f)
@Preview(name = "Landscape", group = "Sync", widthDp = 800, heightDp = 360)
@Composable
private fun SyncTimedPreview() = SyncPreview(
    SyncScreenUiState(
        offsetMs = 300L,
        availability = SyncAvailability.TIMED,
        trackTitle = "Evening lights",
        canAdvance = true,
        canDelay = true,
        previousLine = LyricsViewportLineUiState("A quiet road"),
        currentLine = LyricsViewportLineUiState("The lights are turning blue", translatedText = "灯光渐渐变蓝"),
        nextLine = LyricsViewportLineUiState("We walk toward the morning"),
    ),
)

@Preview(name = "No media with saved offset", group = "Sync", widthDp = 360, heightDp = 740)
@Composable
private fun SyncNoMediaPreview() = SyncPreview(SyncScreenUiState(offsetMs = -500L))

@Preview(name = "Plain lyrics", group = "Sync", widthDp = 360, heightDp = 740)
@Composable
private fun SyncPlainPreview() = SyncPreview(
    SyncScreenUiState(availability = SyncAvailability.PLAIN, trackTitle = "Plain lyrics"),
)

@Preview(name = "Advance limit", group = "Sync", widthDp = 360, heightDp = 740)
@Composable
private fun SyncLimitPreview() = SyncPreview(
    SyncScreenUiState(offsetMs = 30_000L, availability = SyncAvailability.TIMED, canDelay = true),
)

@Composable
private fun SyncPreview(state: SyncScreenUiState) {
    AALyricsTheme {
        Surface {
            SyncScreen(state = state, onAdjust = {}, onReset = {}, bottomOverlayInset = 80.dp)
        }
    }
}
