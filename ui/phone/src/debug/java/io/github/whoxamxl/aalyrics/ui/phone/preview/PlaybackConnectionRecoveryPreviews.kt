package io.github.whoxamxl.aalyrics.ui.phone.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsTheme
import io.github.whoxamxl.aalyrics.ui.phone.shell.PhoneTopBar
import io.github.whoxamxl.aalyrics.ui.phone.shell.PlaybackConnectionRecoveryDialog
import io.github.whoxamxl.aalyrics.ui.phone.state.PlaybackConnectionRecoveryUiState
import io.github.whoxamxl.aalyrics.ui.phone.state.PlaybackSourceConnectionUiState
import io.github.whoxamxl.aalyrics.ui.phone.state.PlaybackSourceErrorUiReason

@Preview(name = "Timeout · narrow", group = "ConnectionRecovery", widthDp = 320)
@Composable
private fun TimeoutTopBarPreview() {
    AALyricsTheme {
        PhoneTopBar(
            mediaSourceLabel = null,
            mediaSourceConnectionState = PlaybackSourceConnectionUiState.ERROR,
            mediaSourceErrorReason = PlaybackSourceErrorUiReason.LISTENER_CONNECTION_TIMEOUT,
            connectionRecovery = PlaybackConnectionRecoveryUiState.TIMED_OUT,
        )
    }
}

@Preview(name = "Connecting", group = "ConnectionRecovery", widthDp = 412, heightDp = 700)
@Composable
private fun ConnectingDialogPreview() = RecoveryDialogPreview(PlaybackConnectionRecoveryUiState.CONNECTING)

@Preview(name = "Timed out", group = "ConnectionRecovery", widthDp = 412, heightDp = 700)
@Preview(name = "Timed out · narrow large text", group = "ConnectionRecovery", widthDp = 320, heightDp = 568, fontScale = 1.3f)
@Preview(name = "Timed out · landscape", group = "ConnectionRecovery", widthDp = 720, heightDp = 360)
@Composable
private fun TimeoutDialogPreview() = RecoveryDialogPreview(PlaybackConnectionRecoveryUiState.TIMED_OUT)

@Preview(name = "Access required", group = "ConnectionRecovery", widthDp = 412, heightDp = 700)
@Composable
private fun AccessRequiredDialogPreview() = RecoveryDialogPreview(PlaybackConnectionRecoveryUiState.ACCESS_REQUIRED)

@Composable
private fun RecoveryDialogPreview(state: PlaybackConnectionRecoveryUiState) {
    AALyricsTheme {
        PlaybackConnectionRecoveryDialog(
            state = state,
            onReconnect = {},
            onOpenNotificationAccess = {},
            onDismissRequest = {},
        )
    }
}
