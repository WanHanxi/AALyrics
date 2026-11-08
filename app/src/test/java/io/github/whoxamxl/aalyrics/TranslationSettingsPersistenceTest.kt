package io.github.whoxamxl.aalyrics

import io.github.whoxamxl.aalyrics.translation.api.TranslationSettings
import kotlin.test.*

class TranslationSettingsPersistenceTest {
    private val values = mutableMapOf<String, Any>("unrelated" to "keep")
    private fun persistence() = TranslationSettingsPersistence(
        readBoolean = { key, default -> values[key] as? Boolean ?: default },
        readString = { key, default -> values[key] as? String ?: default },
        writeValues = values::putAll,
    )

    @Test fun `existing opt-in enabled users retain machine fallback and new installs remain disabled`() {
        assertEquals(TranslationSettings(enabled = false), persistence().read())
        values[TranslationSettingsPersistence.ENABLED_KEY] = true
        assertEquals(TranslationSettings(enabled = true), persistence().read())
        persistence().setMachineTranslationFallbackEnabled(false)
        assertFalse(persistence().read().machineTranslationFallbackEnabled)
        assertTrue(persistence().read().enabled)
    }

    @Test fun `explicit reset restores fallback on target English translation off without clearing other state`() {
        val store = persistence()
        store.setEnabled(true)
        store.setTargetLanguage("zh-CN")
        store.setMachineTranslationFallbackEnabled(false)
        assertEquals(TranslationSettings(true, "zh", false), persistence().read())
        store.resetToDefaults()
        assertEquals(TranslationSettings(enabled = false), persistence().read())
        assertEquals("keep", values["unrelated"])
    }
}
