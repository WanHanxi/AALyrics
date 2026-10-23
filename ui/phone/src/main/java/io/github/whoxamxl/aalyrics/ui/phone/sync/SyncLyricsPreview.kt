package io.github.whoxamxl.aalyrics.ui.phone.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsColors
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsTypography
import io.github.whoxamxl.aalyrics.ui.phone.R

@Composable
internal fun SyncLyricsPreview(
    state: SyncScreenUiState,
    androidAuto: Boolean,
    onSelectSurface: (Boolean) -> Unit,
) {
    val preview = if (androidAuto) state.androidAutoPreview else state.phonePreview
    Surface(shape = RoundedCornerShape(16.dp), color = AALyricsColors.BackgroundSurface) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.sync_preview),
                style = AALyricsTypography.Label,
                color = AALyricsColors.TextSecondary,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !androidAuto,
                    onClick = { onSelectSurface(false) },
                    label = { Text(stringResource(R.string.sync_preview_phone)) },
                )
                FilterChip(
                    selected = androidAuto,
                    onClick = { onSelectSurface(true) },
                    label = { Text(stringResource(R.string.sync_preview_aa)) },
                )
            }
            state.trackTitle?.let {
                Text(it, style = AALyricsTypography.TrackArtist, color = AALyricsColors.TextSecondary)
            }
            if (state.availability == SyncAvailability.TIMED) {
                preview.previousLine?.let {
                    Text(it.text.ifBlank { "♪" }, color = AALyricsColors.TextSecondary)
                }
                Text(
                    preview.currentLine?.text?.ifBlank { "♪" }
                        ?: stringResource(R.string.sync_before_first_line),
                    style = AALyricsTypography.LyricsSupporting,
                    color = AALyricsColors.TextPrimary,
                )
                preview.currentLine?.translatedText?.let {
                    Text(it, style = AALyricsTypography.TrackArtist, color = AALyricsColors.AccentCyan)
                }
                preview.nextLine?.let {
                    Text(it.text.ifBlank { "♪" }, color = AALyricsColors.TextSecondary)
                }
            } else {
                Text(
                    stringResource(when (state.availability) {
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
