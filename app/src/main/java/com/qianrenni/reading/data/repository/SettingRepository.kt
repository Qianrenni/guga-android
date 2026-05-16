package com.qianrenni.reading.data.repository


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.qianrenni.reading.data.model.ReadSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "read_settings")

class SettingsRepository(private val context: Context) {
    private object PreferencesKeys {
        val FONT_SIZE = intPreferencesKey("font_size")
        val LINE_HEIGHT = intPreferencesKey("line_height")
        val LETTER_SPACING = intPreferencesKey("letter_spacing")
        val FONT_FAMILY = stringPreferencesKey("font_family")
        val TEXT_COLOR = stringPreferencesKey("text_color")
        val BACKGROUND_COLOR = stringPreferencesKey("background_color")
    }

    val readSettings: Flow<ReadSettings> = context.dataStore.data.map { preferences ->
        ReadSettings(
            fontSize = preferences[PreferencesKeys.FONT_SIZE] ?: 18,
            lineHeight = preferences[PreferencesKeys.LINE_HEIGHT] ?: 40,
            letterSpacing = preferences[PreferencesKeys.LETTER_SPACING] ?: 2,
            fontFamily = preferences[PreferencesKeys.FONT_FAMILY] ?: "default",
            textColor = preferences[PreferencesKeys.TEXT_COLOR] ?: "#333333",
            backgroundColor = preferences[PreferencesKeys.BACKGROUND_COLOR] ?: "#ffffff"
        )
    }

    suspend fun updateFontSize(size: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE] = size
        }
    }

    suspend fun updateLineHeight(height: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LINE_HEIGHT] = height
        }
    }

    suspend fun updateLetterSpacing(spacing: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LETTER_SPACING] = spacing
        }
    }

    suspend fun updateFontFamily(family: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_FAMILY] = family
        }
    }

    suspend fun updateTheme(textColor: String, backgroundColor: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEXT_COLOR] = textColor
            preferences[PreferencesKeys.BACKGROUND_COLOR] = backgroundColor
        }
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