package io.github.whoxamxl.aalyrics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LyricsSourceSettingsPersistenceTest {
    private val values = mutableMapOf<String, Boolean>("unrelated" to true)
    private val writes = mutableListOf<Map<String, Boolean>>()
    private fun persistence() = LyricsSourceSettingsPersistence(
        readBoolean = { key, default -> values[key] ?: default },
        writeBooleans = { values.putAll(it); writes += it },
    )

    @Test fun `new installs and existing preferences without a choice default off`() {
        assertEquals(LyricsSourceSettings(), persistence().read())
        assertTrue(writes.isEmpty())
        persistence().setNeteaseEnabled(true)
        assertEquals(LyricsSourceSettings(), persistence().read())
    }

    @Test fun `confirmation persists choice and acknowledgement together and upgrades retain them`() {
        persistence().acknowledgeAndEnableNetease()
        assertEquals(2, writes.single().size)
        assertEquals(LyricsSourceSettings(true, true), persistence().read())
        persistence().setNeteaseEnabled(false)
        assertEquals(LyricsSourceSettings(false, true), persistence().read())
        persistence().setNeteaseEnabled(true)
        assertEquals(LyricsSourceSettings(true, true), persistence().read())
    }

    @Test fun `reset clears acknowledgement and enablement without resetting other settings`() {
        persistence().acknowledgeAndEnableNetease()
        persistence().resetToDefaults()
        assertEquals(LyricsSourceSettings(), persistence().read())
        assertEquals(true, values["unrelated"])
        persistence().setNeteaseEnabled(true)
        assertFalse(persistence().read().neteaseEnabled)
    }

    @Test fun `an enabled key alone cannot act as acknowledgement`() {
        values[LyricsSourceSettingsPersistence.NETEASE_ENABLED_KEY] = true
        assertEquals(LyricsSourceSettings(), persistence().read())
    }
}
