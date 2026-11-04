package io.github.whoxamxl.aalyrics.ui.phone.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsColors
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsTheme
import io.github.whoxamxl.aalyrics.ui.phone.R
import io.github.whoxamxl.aalyrics.ui.phone.settings.SettingsBrandFooter

@Preview(name = "Typical", group = "SettingsBrandFooter", widthDp = 412, heightDp = 440)
@Preview(name = "Narrow", group = "SettingsBrandFooter", widthDp = 320, heightDp = 480)
@Preview(
    name = "Narrow · enlarged font",
    group = "SettingsBrandFooter",
    widthDp = 320,
    heightDp = 560,
    fontScale = 1.4f,
)
@Composable
private fun SettingsBrandFooterPreview() {
    AALyricsTheme {
        Box(Modifier.fillMaxSize().background(AALyricsColors.BackgroundBase)) {
            SettingsBrandFooter(
                appName = stringResource(R.string.settings_brand_name),
                versionLabel = stringResource(R.string.settings_version),
                versionName = "1.1.0-beta.1-dev+abcdef0",
                originalProjectCredit = stringResource(R.string.settings_brand_original_project, 2026),
                forkMaintainerCredit = stringResource(R.string.settings_brand_fork_maintainer),
                logoContentDescription = stringResource(R.string.settings_brand_logo_description),
                onOpenGitHub = {},
                onOpenOriginalProject = {},
            )
        }
    }
}
