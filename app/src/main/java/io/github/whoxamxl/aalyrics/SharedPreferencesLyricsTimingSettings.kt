package io.github.whoxamxl.aalyrics

import android.content.Context

internal fun createLyricsTimingSettings(context: Context): LyricsTimingSettings {
    val preferences = context.getSharedPreferences("lyrics_timing_settings", Context.MODE_PRIVATE)
    return LyricsTimingSettings(
        readOffsets = {
            LyricsTimingOffsets(
                globalMs = preferences.getLong("lyrics_offset_ms", 0L),
                androidAutoMs = preferences.getLong("aa_offset_ms", 0L),
            )
        },
        writeOffsets = { value ->
            preferences.edit()
                .putLong("lyrics_offset_ms", value.globalMs)
                .putLong("aa_offset_ms", value.androidAutoMs)
                .apply()
        },
    )
}
