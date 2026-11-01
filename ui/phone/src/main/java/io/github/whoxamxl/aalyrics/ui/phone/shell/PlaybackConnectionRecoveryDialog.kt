package io.github.whoxamxl.aalyrics.ui.phone.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsColors
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsRadius
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsSpacing
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsTypography
import io.github.whoxamxl.aalyrics.ui.phone.R
import io.github.whoxamxl.aalyrics.ui.phone.state.PlaybackConnectionRecoveryUiState

/** User-requested connection help; the application owns retries and system-settings navigation. */
@Composable
internal fun PlaybackConnectionRecoveryDialog(
    state: PlaybackConnectionRecoveryUiState,
    onReconnect: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                stringResource(R.string.playback_connection_details),
                style = AALyricsTypography.TrackTitle,
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AALyricsSpacing.Space12),
            ) {
                Text(
                    text = stringResource(
                        when (state) {
                            PlaybackConnectionRecoveryUiState.CONNECTING -> R.string.playback_connection_waiting_message
                            PlaybackConnectionRecoveryUiState.TIMED_OUT -> R.string.playback_connection_timeout_message
                            PlaybackConnectionRecoveryUiState.ACCESS_REQUIRED -> R.string.playback_connection_access_message
                        },
                    ),
                    style = AALyricsTypography.TrackArtist,
                    color = AALyricsColors.TextSecondary,
                )
                Button(
                    onClick = onReconnect,
                    enabled = state == PlaybackConnectionRecoveryUiState.TIMED_OUT,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.playback_connection_reconnect))
                }
                TextButton(
                    onClick = onOpenNotificationAccess,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.playback_connection_notification_settings))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.playback_connection_close))
            }
        },
        containerColor = AALyricsColors.BackgroundSurfaceStrong,
        titleContentColor = AALyricsColors.TextPrimary,
        textContentColor = AALyricsColors.TextPrimary,
        shape = RoundedCornerShape(AALyricsRadius.Radius16),
    )
}
