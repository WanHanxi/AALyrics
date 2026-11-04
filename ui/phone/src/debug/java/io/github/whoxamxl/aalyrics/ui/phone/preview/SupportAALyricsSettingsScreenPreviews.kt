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
import io.github.whoxamxl.aalyrics.ui.phone.settings.SupportAALyricsSettingsScreen

@Preview(name = "Typical", group = "SupportAALyricsSettings", widthDp = 412, heightDp = 760)
@Composable
private fun SupportAALyricsSettingsTypicalPreview() {
    SupportAALyricsSettingsPreview(animationEnabled = false)
}

@Preview(
    name = "Interactive animation",
    group = "SupportAALyricsSettings",
    widthDp = 412,
    heightDp = 760,
)
@Composable
private fun SupportAALyricsSettingsInteractivePreview() {
    SupportAALyricsSettingsPreview(animationEnabled = true)
}

@Preview(name = "Narrow · 320dp", group = "SupportAALyricsSettings", widthDp = 320, heightDp = 700)
@Composable
private fun SupportAALyricsSettingsNarrowPreview() {
    SupportAALyricsSettingsPreview(animationEnabled = false)
}

@Preview(
    name = "Enlarged font",
    group = "SupportAALyricsSettings",
    widthDp = 412,
    heightDp = 820,
    fontScale = 1.4f,
)
@Composable
private fun SupportAALyricsSettingsLargeFontPreview() {
    SupportAALyricsSettingsPreview(animationEnabled = false)
}

@Preview(name = "Typical", group = "SupportForkSettings", widthDp = 412, heightDp = 760)
@Preview(name = "Narrow", group = "SupportForkSettings", widthDp = 320, heightDp = 700)
@Preview(
    name = "Narrow · enlarged font",
    group = "SupportForkSettings",
    widthDp = 320,
    heightDp = 820,
    fontScale = 1.4f,
)
@Composable
private fun SupportForkSettingsPreview() {
    SupportAALyricsSettingsPreview(animationEnabled = false, supportsFork = true)
}

@Composable
private fun SupportAALyricsSettingsPreview(
    animationEnabled: Boolean,
    supportsFork: Boolean = false,
) {
    AALyricsTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AALyricsColors.BackgroundBase),
        ) {
            SupportAALyricsSettingsScreen(
                title = stringResource(
                    if (supportsFork) R.string.settings_support_fork else R.string.settings_support_aalyrics,
                ),
                body = stringResource(
                    if (supportsFork) R.string.settings_support_fork_body else R.string.settings_support_body,
                ),
                supportNote = stringResource(
                    if (supportsFork) R.string.settings_support_fork_subtext else R.string.settings_support_subtext,
                ),
                buttonDescription = stringResource(
                    if (supportsFork) {
                        R.string.settings_support_fork_button_description
                    } else {
                        R.string.settings_support_button_description
                    },
                ),
                onSupport = {},
                onBack = {},
                modifier = Modifier.fillMaxSize(),
                supportCtaAnimationEnabled = animationEnabled,
            )
        }
    }
}
