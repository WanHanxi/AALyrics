package io.github.whoxamxl.aalyrics

import android.content.Context
import android.content.SharedPreferences
import io.github.whoxamxl.aalyrics.translation.api.TranslationSettings
import io.github.whoxamxl.aalyrics.translation.api.TranslationSettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.Closeable

internal class SharedPreferencesTranslationSettingsStore(
    context: Context,
) : TranslationSettingsStore, Closeable {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    private val persistence = TranslationSettingsPersistence(
        readBoolean = preferences::getBoolean,
        readString = preferences::getString,
        writeValues = { values ->
            preferences.edit().apply {
                values.forEach { (key, value) ->
                    when (value) {
                        is Boolean -> putBoolean(key, value)
                        is String -> putString(key, value)
                    }
                }
            }.apply()
        },
    )
    private val _settings = MutableStateFlow(persistence.read())
    override val settings: StateFlow<TranslationSettings> = _settings.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key in TranslationSettingsPersistence.KEYS) {
            _settings.value = persistence.read()
        }
    }

    init {
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun setEnabled(enabled: Boolean) = persistence.setEnabled(enabled)
    override fun setTargetLanguage(languageTag: String) = persistence.setTargetLanguage(languageTag)
    fun setMachineTranslationFallbackEnabled(enabled: Boolean) =
        persistence.setMachineTranslationFallbackEnabled(enabled)
    fun resetToDefaults() = persistence.resetToDefaults()

    override fun close() {
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private companion object {
        const val PREFERENCES_NAME = "aalyrics_preferences"
    }
}
