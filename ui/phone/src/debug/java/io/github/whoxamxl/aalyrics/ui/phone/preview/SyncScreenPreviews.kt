package io.github.whoxamxl.aalyrics.ui.phone.preview

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsTheme
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.LyricsViewportLineUiState
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncAvailability
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncLyricsPreview
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncLyricsPreviewUiState
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncOffsetEditorDialog
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncOffsetScope
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncOffsetUiState
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncScreen
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncScreenUiState
import io.github.whoxamxl.aalyrics.ui.phone.sync.TapSyncAvailability
import io.github.whoxamxl.aalyrics.ui.phone.sync.TapSyncUiState

private fun timedSyncState(): SyncScreenUiState {
    val first = LyricsViewportLineUiState("A quiet road", translatedText = "宁静的路")
    val second = LyricsViewportLineUiState("The lights are turning blue", translatedText = "灯光渐渐变蓝")
    val third = LyricsViewportLineUiState("We walk toward the morning")
    return SyncScreenUiState(
        globalOffset = SyncOffsetUiState(300L, canAdvance = true, canDelay = true),
        androidAutoOffset = SyncOffsetUiState(-750L, canAdvance = true, canDelay = true),
        androidAutoTotalOffsetMs = -450L,
        availability = SyncAvailability.TIMED,
        trackTitle = "Evening lights",
        phonePreview = SyncLyricsPreviewUiState(first, second, third),
        androidAutoPreview = SyncLyricsPreviewUiState(currentLine = first, nextLine = second),
        tapSync = TapSyncUiState(availability = TapSyncAvailability.READY),
    )
}

@Preview(name = "Global and Android Auto offsets", group = "Sync", widthDp = 412, heightDp = 1000)
@Preview(name = "Narrow and large font", group = "Sync", widthDp = 320, heightDp = 640, fontScale = 1.3f)
@Preview(name = "Landscape", group = "Sync", widthDp = 800, heightDp = 360)
@Composable
private fun SyncTimedPreview() = SyncPreview(timedSyncState())

@Preview(name = "No media with both saved offsets", group = "Sync", widthDp = 360, heightDp = 740)
@Composable
private fun SyncNoMediaPreview() = SyncPreview(
    SyncScreenUiState(
        globalOffset = SyncOffsetUiState(-500L),
        androidAutoOffset = SyncOffsetUiState(-250L),
        androidAutoTotalOffsetMs = -750L,
    ),
)

@Preview(name = "Plain lyrics", group = "Sync", widthDp = 360, heightDp = 740)
@Composable
private fun SyncPlainPreview() = SyncPreview(
    SyncScreenUiState(availability = SyncAvailability.PLAIN, trackTitle = "Plain lyrics"),
)

@Preview(name = "Independent opposite limits", group = "Sync", widthDp = 360, heightDp = 740)
@Composable
private fun SyncLimitPreview() = SyncPreview(
    timedSyncState().copy(
        globalOffset = SyncOffsetUiState(30_000L, canDelay = true),
        androidAutoOffset = SyncOffsetUiState(-30_000L, canAdvance = true),
        androidAutoTotalOffsetMs = 0L,
    ),
)

@Preview(name = "Tap calibration - second line", group = "Sync", widthDp = 412, heightDp = 820)
@Preview(name = "Tap calibration - narrow large font", group = "Sync", widthDp = 320, heightDp = 640, fontScale = 1.3f)
@Composable
private fun SyncTapListeningPreview() = SyncPreview(
    timedSyncState().copy(
        tapSync = TapSyncUiState(
            availability = TapSyncAvailability.READY,
            active = true,
            lines = listOf("A quiet road", "The lights are turning blue", "We walk toward the morning"),
            completedTaps = 1,
        ),
    ),
)

@Preview(name = "Tap calibration saved - AA retained", group = "Sync", widthDp = 412, heightDp = 820)
@Composable
private fun SyncTapSavedPreview() = SyncPreview(
    timedSyncState().copy(tapSync = TapSyncUiState(availability = TapSyncAvailability.READY, savedOffsetMs = 300L)),
)

@Preview(name = "Not enough remaining lines", group = "Sync", widthDp = 360, heightDp = 740)
@Composable
private fun SyncTapEndPreview() = SyncPreview(
    SyncScreenUiState(tapSync = TapSyncUiState(availability = TapSyncAvailability.NOT_ENOUGH_LINES)),
)

@Preview(name = "Phone and car preview switch", group = "Sync", widthDp = 412)
@Preview(name = "Car preview - narrow large font", group = "Sync", widthDp = 320, fontScale = 2f)
@Composable
private fun SyncCarTimingPreview() {
    var androidAuto by remember { mutableStateOf(true) }
    AALyricsTheme {
        SyncLyricsPreview(timedSyncState(), androidAuto, onSelectSurface = { androidAuto = it })
    }
}

@Preview(name = "Exact global offset editor", group = "Sync", widthDp = 412, heightDp = 820)
@Preview(name = "Offset editor - narrow large font", group = "Sync", widthDp = 320, heightDp = 640, fontScale = 1.3f)
@Preview(name = "Offset editor - landscape", group = "Sync", widthDp = 800, heightDp = 360)
@Composable
private fun SyncOffsetEditorPreview() = OffsetEditorPreview("-275")

@Preview(name = "Exact AA offset editor", group = "Sync", widthDp = 412, heightDp = 820)
@Preview(name = "AA editor - 200% font", group = "Sync", widthDp = 320, heightDp = 740, fontScale = 2f)
@Composable
private fun AndroidAutoOffsetEditorPreview() = OffsetEditorPreview("-375", SyncOffsetScope.ANDROID_AUTO)

@Preview(name = "Offset outside range", group = "Sync", widthDp = 360, heightDp = 740)
@Composable
private fun SyncOffsetRangeErrorPreview() = OffsetEditorPreview("30001")

@Preview(name = "Offset needs an integer", group = "Sync", widthDp = 360, heightDp = 740)
@Composable
private fun SyncOffsetInvalidPreview() = OffsetEditorPreview("-")

@Composable
private fun OffsetEditorPreview(text: String, scope: SyncOffsetScope = SyncOffsetScope.GLOBAL) {
    var draft by remember { mutableStateOf(TextFieldValue(text, TextRange(0, text.length))) }
    AALyricsTheme {
        SyncOffsetEditorDialog(
            draft = draft,
            maxOffsetMs = 30_000L,
            scope = scope,
            onDraftChange = { draft = it },
            onSave = {},
            onDismissRequest = {},
        )
    }
}

@Composable
private fun SyncPreview(state: SyncScreenUiState) {
    AALyricsTheme {
        Surface {
            SyncScreen(
                state = state,
                onAdjust = { _, _ -> },
                onReset = {},
                onSetOffset = { _, _ -> },
                maxOffsetMs = 30_000L,
                bottomOverlayInset = 80.dp,
            )
        }
    }
}
