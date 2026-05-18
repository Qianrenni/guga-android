package com.qianrenni.reading.data.store


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.qianrenni.reading.data.model.ReadSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "read_settings")

class SettingsRepository(private val context: Context) {
    private object PreferencesKeys {
        val FONT_SIZE = floatPreferencesKey("font_size")
        val LINE_HEIGHT = floatPreferencesKey("line_height")
        val LETTER_SPACING = floatPreferencesKey("letter_spacing")
        val FONT_FAMILY = stringPreferencesKey("font_family")
        val TEXT_COLOR = stringPreferencesKey("text_color")
        val BACKGROUND_COLOR = stringPreferencesKey("background_color")
    }

    val readSettings: Flow<ReadSettings> = context.dataStore.data.map { preferences ->
        ReadSettings(
            fontSize = preferences[PreferencesKeys.FONT_SIZE] ?: 18f,
            lineHeight = preferences[PreferencesKeys.LINE_HEIGHT] ?: 40f,
            letterSpacing = preferences[PreferencesKeys.LETTER_SPACING] ?: 2f,
            fontFamily = preferences[PreferencesKeys.FONT_FAMILY] ?: "default",
            textColor = preferences[PreferencesKeys.TEXT_COLOR] ?: "#333333",
            backgroundColor = preferences[PreferencesKeys.BACKGROUND_COLOR] ?: "#ffffff"
        )
    }

    suspend fun updateSettings(settings: ReadSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE] = settings.fontSize
            preferences[PreferencesKeys.LINE_HEIGHT] = settings.lineHeight
            preferences[PreferencesKeys.LETTER_SPACING] = settings.letterSpacing
            preferences[PreferencesKeys.FONT_FAMILY] = settings.fontFamily
            preferences[PreferencesKeys.TEXT_COLOR] = settings.textColor
            preferences[PreferencesKeys.BACKGROUND_COLOR] = settings.backgroundColor
        }
    }
}