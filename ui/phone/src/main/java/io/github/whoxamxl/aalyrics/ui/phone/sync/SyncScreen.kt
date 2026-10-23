package io.github.whoxamxl.aalyrics.ui.phone.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsColors
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsTypography
import io.github.whoxamxl.aalyrics.ui.phone.R

/** Scoped controls and previews; all timing and persistence remain application-owned. */
@Composable
fun SyncScreen(
    state: SyncScreenUiState,
    onAdjust: (SyncOffsetScope, SyncAdjustment) -> Unit,
    onReset: (SyncOffsetScope) -> Unit,
    onSetOffset: (SyncOffsetScope, Long) -> Unit,
    maxOffsetMs: Long,
    onStartTapSync: () -> Unit = {},
    onSyncTap: () -> Unit = {},
    onCancelTapSync: () -> Unit = {},
    modifier: Modifier = Modifier,
    bottomOverlayInset: Dp = 0.dp,
) {
    var editingScope by rememberSaveable { mutableStateOf<SyncOffsetScope?>(null) }
    var previewAndroidAuto by rememberSaveable { mutableStateOf(false) }
    editingScope?.let { scope ->
        SyncOffsetEditor(
            initialOffsetMs = when (scope) {
                SyncOffsetScope.GLOBAL -> state.globalOffset.offsetMs
                SyncOffsetScope.ANDROID_AUTO -> state.androidAutoOffset.offsetMs
            },
            maxOffsetMs = maxOffsetMs,
            scope = scope,
            onSave = {
                onSetOffset(scope, it)
                editingScope = null
            },
            onDismissRequest = { editingScope = null },
        )
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 20.dp,
                top = 20.dp,
                end = 20.dp,
                bottom = bottomOverlayInset + 20.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.sync_title),
            style = AALyricsTypography.LyricsSupporting,
            color = AALyricsColors.TextPrimary,
        )
        TapSyncCard(
            state = state.tapSync,
            onStart = {
                previewAndroidAuto = false
                onStartTapSync()
            },
            onTap = onSyncTap,
            onCancel = onCancelTapSync,
        )
        SyncOffsetScope.entries.forEach { scope ->
            val androidAuto = scope == SyncOffsetScope.ANDROID_AUTO
            SyncOffsetCard(
                state = if (androidAuto) state.androidAutoOffset else state.globalOffset,
                scope = scope,
                totalOffsetMs = state.androidAutoTotalOffsetMs.takeIf { androidAuto },
                onAdjust = {
                    previewAndroidAuto = androidAuto
                    onAdjust(scope, it)
                },
                onReset = {
                    previewAndroidAuto = androidAuto
                    onReset(scope)
                },
                onEdit = {
                    onCancelTapSync()
                    previewAndroidAuto = androidAuto
                    editingScope = scope
                },
            )
        }
        SyncLyricsPreview(
            state = state,
            androidAuto = previewAndroidAuto,
            onSelectSurface = { previewAndroidAuto = it },
        )
    }
}
