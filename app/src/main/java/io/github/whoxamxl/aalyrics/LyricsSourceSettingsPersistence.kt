package io.github.whoxamxl.aalyrics

internal data class LyricsSourceSettings(
    val neteaseEnabled: Boolean = false,
    val neteaseNoticeAcknowledged: Boolean = false,
)

/** Stable keys preserve an explicit choice across app upgrades; missing choices stay OFF. */
internal class LyricsSourceSettingsPersistence(
    private val readBoolean: (String, Boolean) -> Boolean,
    private val writeBooleans: (Map<String, Boolean>) -> Unit,
) {
    fun read(): LyricsSourceSettings {
        val acknowledged = readBoolean(NETEASE_NOTICE_ACKNOWLEDGED_KEY, false)
        return LyricsSourceSettings(
            neteaseEnabled = acknowledged && readBoolean(NETEASE_ENABLED_KEY, false),
            neteaseNoticeAcknowledged = acknowledged,
        )
    }

    fun setNeteaseEnabled(enabled: Boolean) {
        writeBooleans(mapOf(
            NETEASE_ENABLED_KEY to (enabled && read().neteaseNoticeAcknowledged),
        ))
    }

    fun acknowledgeAndEnableNetease() {
        writeBooleans(mapOf(
            NETEASE_ENABLED_KEY to true,
            NETEASE_NOTICE_ACKNOWLEDGED_KEY to true,
        ))
    }

    fun resetToDefaults() {
        writeBooleans(mapOf(
            NETEASE_ENABLED_KEY to false,
            NETEASE_NOTICE_ACKNOWLEDGED_KEY to false,
        ))
    }

    internal companion object {
        const val NETEASE_ENABLED_KEY = "lyrics_source_netease_enabled"
        const val NETEASE_NOTICE_ACKNOWLEDGED_KEY = "lyrics_source_netease_notice_acknowledged"
        val KEYS = setOf(NETEASE_ENABLED_KEY, NETEASE_NOTICE_ACKNOWLEDGED_KEY)
    }
}
