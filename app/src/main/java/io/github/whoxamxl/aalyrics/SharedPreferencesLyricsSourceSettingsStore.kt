package io.github.whoxamxl.aalyrics

import android.content.Context
import android.content.SharedPreferences
import java.io.Closeable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class SharedPreferencesLyricsSourceSettingsStore(context: Context) : Closeable {
    private val preferences = context.applicationContext.getSharedPreferences(
        "aalyrics_preferences", Context.MODE_PRIVATE,
    )
    private val persistence = LyricsSourceSettingsPersistence(
        readBoolean = preferences::getBoolean,
        writeBooleans = { values ->
            preferences.edit().also { editor ->
                values.forEach { (key, value) -> editor.putBoolean(key, value) }
            }.apply()
        },
    )
    private val mutableSettings = MutableStateFlow(persistence.read())
    val settings: StateFlow<LyricsSourceSettings> = mutableSettings.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key in LyricsSourceSettingsPersistence.KEYS) {
            mutableSettings.value = persistence.read()
        }
    }

    init {
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun setNeteaseEnabled(enabled: Boolean) {
        persistence.setNeteaseEnabled(enabled)
        mutableSettings.value = persistence.read()
    }

    fun acknowledgeAndEnableNetease() {
        persistence.acknowledgeAndEnableNetease()
        mutableSettings.value = persistence.read()
    }

    fun resetToDefaults() {
        persistence.resetToDefaults()
        mutableSettings.value = persistence.read()
    }

    override fun close() {
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
