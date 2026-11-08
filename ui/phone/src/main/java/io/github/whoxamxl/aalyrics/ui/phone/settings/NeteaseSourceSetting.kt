package io.github.whoxamxl.aalyrics.ui.phone.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import io.github.whoxamxl.aalyrics.ui.phone.R

@Composable
internal fun NeteaseSourceSetting(
    enabled: Boolean,
    noticeAcknowledged: Boolean,
    onEnabledChanged: (Boolean) -> Unit,
    onNoticeAccepted: () -> Unit,
) {
    var noticeVisible by rememberSaveable(enabled, noticeAcknowledged) { mutableStateOf(false) }

    SettingsSwitchRow(
        title = stringResource(R.string.settings_netease),
        checked = enabled,
        onCheckedChange = { requested ->
            if (requested && !noticeAcknowledged) {
                noticeVisible = true
            } else {
                onEnabledChanged(requested)
            }
        },
        infoText = stringResource(R.string.settings_netease_notice_body),
        infoContentDescription = stringResource(R.string.settings_netease_info_description),
    )

    if (noticeVisible) {
        NeteaseSourceNoticeDialog(
            onConfirm = {
                noticeVisible = false
                onNoticeAccepted()
            },
            onDismissRequest = { noticeVisible = false },
        )
    }
}

@Composable
internal fun NeteaseSourceNoticeDialog(onConfirm: () -> Unit, onDismissRequest: () -> Unit) {
    SettingsConfirmationDialog(
        title = stringResource(R.string.settings_netease_notice_title),
        text = stringResource(R.string.settings_netease_notice_body),
        confirmLabel = stringResource(R.string.settings_netease_enable),
        dismissLabel = stringResource(R.string.settings_cancel),
        onConfirm = onConfirm,
        onDismissRequest = onDismissRequest,
    )
}
