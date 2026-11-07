package io.github.whoxamxl.aalyrics

import io.github.whoxamxl.aalyrics.ui.phone.settings.HelpFeedbackDestination

internal fun HelpFeedbackDestination.githubUrl(): String =
    when (this) {
        HelpFeedbackDestination.REPORT_BUG ->
            "https://github.com/WanHanxi/AALyrics/issues/new"
        HelpFeedbackDestination.ASK_QUESTION ->
            "https://github.com/WanHanxi/AALyrics/discussions/categories/q-a"
        HelpFeedbackDestination.SUGGEST_IDEA ->
            "https://github.com/WanHanxi/AALyrics/discussions/categories/ideas"
        HelpFeedbackDestination.GENERAL_DISCUSSION ->
            "https://github.com/WanHanxi/AALyrics/discussions/categories/general"
        HelpFeedbackDestination.REPORT_SECURITY_ISSUE ->
            "https://github.com/WanHanxi/AALyrics/security/advisories/new"
    }
