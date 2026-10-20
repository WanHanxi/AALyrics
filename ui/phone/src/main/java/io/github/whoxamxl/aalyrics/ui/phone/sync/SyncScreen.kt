package io.github.whoxamxl.aalyrics.ui.phone.sync

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsColors
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsTypography
import androidx.compose.ui.res.stringResource
import io.github.whoxamxl.aalyrics.ui.phone.R

/** Manual calibration controls with a preview driven by the shared lyrics clock. */
@Composable
fun SyncScreen(
    state: SyncScreenUiState,
    onAdjust: (SyncAdjustment) -> Unit,
    onReset: () -> Unit,
    onSetOffset: (Long) -> Unit,
    maxOffsetMs: Long,
    onStartTapSync: () -> Unit = {},
    onSyncTap: () -> Unit = {},
    onCancelTapSync: () -> Unit = {},
    modifier: Modifier = Modifier,
    bottomOverlayInset: Dp = 0.dp,
) {
    var editingOffset by rememberSaveable { mutableStateOf(false) }
    if (editingOffset) {
        SyncOffsetEditor(
            initialOffsetMs = state.offsetMs,
            maxOffsetMs = maxOffsetMs,
            onSave = {
                onSetOffset(it)
                editingOffset = false
            },
            onDismissRequest = { editingOffset = false },
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
            onStart = onStartTapSync,
            onTap = onSyncTap,
            onCancel = onCancelTapSync,
        )
        if (!state.tapSync.active) {
            Text(
                text = stringResource(R.string.sync_help),
                style = AALyricsTypography.TrackArtist,
                color = AALyricsColors.TextSecondary,
            )
        }
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AALyricsColors.BackgroundSurface,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.sync_offset_label),
                    style = AALyricsTypography.Label,
                    color = AALyricsColors.TextSecondary,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            role = Role.Button,
                            onClickLabel = stringResource(R.string.sync_edit_offset),
                        ) {
                            onCancelTapSync()
                            editingOffset = true
                        },
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(
                            R.string.sync_offset_value,
                            if (state.offsetMs > 0) "+${state.offsetMs}" else state.offsetMs.toString(),
                        ),
                        style = AALyricsTypography.LyricsCurrent,
                        color = AALyricsColors.AccentCyan,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        stringResource(R.string.sync_offset_edit),
                        style = AALyricsTypography.TrackArtist,
                        color = AALyricsColors.AccentCyan,
                    )
                }
                Text(
                    text = stringResource(R.string.sync_scope),
                    style = AALyricsTypography.TrackArtist,
                    color = AALyricsColors.TextSecondary,
                )
                SyncAdjustmentRow(state, onAdjust, coarse = false)
                SyncAdjustmentRow(state, onAdjust, coarse = true)
                TextButton(
                    onClick = onReset,
                    enabled = state.offsetMs != 0L,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(stringResource(R.string.sync_reset))
                }
            }
        }
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AALyricsColors.BackgroundSurface,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.sync_preview),
                    style = AALyricsTypography.Label,
                    color = AALyricsColors.TextSecondary,
                )
                state.trackTitle?.let {
                    Text(it, style = AALyricsTypography.TrackArtist, color = AALyricsColors.TextSecondary)
                }
                if (state.availability == SyncAvailability.TIMED) {
                    state.previousLine?.let {
                        Text(it.text.ifBlank { "♪" }, color = AALyricsColors.TextSecondary)
                    }
                    Text(
                        text = state.currentLine?.text?.ifBlank { "♪" }
                            ?: stringResource(R.string.sync_before_first_line),
                        style = AALyricsTypography.LyricsSupporting,
                        color = AALyricsColors.TextPrimary,
                    )
                    state.currentLine?.translatedText?.let {
                        Text(it, style = AALyricsTypography.TrackArtist, color = AALyricsColors.AccentCyan)
                    }
                    state.nextLine?.let {
                        Text(it.text.ifBlank { "♪" }, color = AALyricsColors.TextSecondary)
                    }
                } else {
                    Text(
                        text = stringResource(when (state.availability) {
                            SyncAvailability.NO_MEDIA -> R.string.sync_no_media
                            SyncAvailability.LOADING -> R.string.sync_loading
                            SyncAvailability.PLAIN -> R.string.sync_plain
                            else -> R.string.sync_unavailable
                        }),
                        style = AALyricsTypography.TrackArtist,
                        color = AALyricsColors.TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncAdjustmentRow(
    state: SyncScreenUiState,
    onAdjust: (SyncAdjustment) -> Unit,
    coarse: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = { onAdjust(if (coarse) SyncAdjustment.DELAY_COARSE else SyncAdjustment.DELAY_FINE) },
            enabled = state.canDelay,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
        ) {
            Text(
                stringResource(if (coarse) R.string.sync_delay_500 else R.string.sync_delay_50),
                style = AALyricsTypography.TrackArtist,
                textAlign = TextAlign.Center,
            )
        }
        Button(
            onClick = { onAdjust(if (coarse) SyncAdjustment.ADVANCE_COARSE else SyncAdjustment.ADVANCE_FINE) },
            enabled = state.canAdvance,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
        ) {
            Text(
                stringResource(if (coarse) R.string.sync_advance_500 else R.string.sync_advance_50),
                style = AALyricsTypography.TrackArtist,
                textAlign = TextAlign.Center,
            )
        }
    }
}
