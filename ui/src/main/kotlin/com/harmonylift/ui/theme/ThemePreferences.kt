package com.harmonylift.ui.theme

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val TAG = "HarmonyLiftDebug"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

class ThemePreferences(private val context: Context) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_mode")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[THEME_KEY] ?: ThemeMode.SYSTEM.name
            val mode = ThemeMode.valueOf(themeName)
            Log.d(TAG, "[Theme] DataStore flow emission: storedKey=\"$themeName\" resolvedMode=$mode")
            mode
        }

    suspend fun saveThemeMode(mode: ThemeMode) {
        Log.d(TAG, "[Theme] saveThemeMode() called. Writing mode=$mode to DataStore.")
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = mode.name
            Log.d(TAG, "[Theme] DataStore write complete. key=theme_mode value=${mode.name}")
        }
    }
}

