package io.github.whoxamxl.aalyrics

import io.github.whoxamxl.aalyrics.translation.api.TranslationLanguages
import io.github.whoxamxl.aalyrics.translation.api.TranslationSettings

/** Explicit preferences/reset policy, independent of Android's storage adapter. */
internal class TranslationSettingsPersistence(
    private val readBoolean: (String, Boolean) -> Boolean,
    private val readString: (String, String) -> String?,
    private val writeValues: (Map<String, Any>) -> Unit,
) {
    fun read(): TranslationSettings = TranslationSettings(
        enabled = readBoolean(ENABLED_KEY, false),
        targetLanguage = TranslationLanguages.normalizeTargetLanguage(
            readString(TARGET_LANGUAGE_KEY, TranslationLanguages.DEFAULT_TARGET_LANGUAGE),
        ),
        machineTranslationFallbackEnabled = readBoolean(MACHINE_FALLBACK_KEY, true),
    )

    fun setEnabled(enabled: Boolean) = writeValues(mapOf(ENABLED_KEY to enabled))
    fun setTargetLanguage(languageTag: String) = writeValues(mapOf(
        TARGET_LANGUAGE_KEY to TranslationLanguages.normalizeTargetLanguage(languageTag),
    ))
    fun setMachineTranslationFallbackEnabled(enabled: Boolean) =
        writeValues(mapOf(MACHINE_FALLBACK_KEY to enabled))

    fun resetToDefaults() = writeValues(mapOf(
        ENABLED_KEY to false,
        TARGET_LANGUAGE_KEY to TranslationLanguages.DEFAULT_TARGET_LANGUAGE,
        MACHINE_FALLBACK_KEY to true,
    ))

    companion object {
        const val ENABLED_KEY = "translation_enabled"
        const val TARGET_LANGUAGE_KEY = "translation_target_language"
        const val MACHINE_FALLBACK_KEY = "translation_machine_fallback_enabled"
        val KEYS = setOf(ENABLED_KEY, TARGET_LANGUAGE_KEY, MACHINE_FALLBACK_KEY)
    }
}
