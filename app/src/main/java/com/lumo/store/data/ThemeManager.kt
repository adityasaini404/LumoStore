package com.lumo.store.data

import android.content.Context

object ThemeManager {
    private const val PREF_NAME = "lumo_settings"
    private const val KEY_THEME = "app_theme"

    // डिफ़ॉल्ट थीम "Dark" रहेगी
    fun getTheme(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_THEME, "Dark") ?: "Dark"
    }

    fun setTheme(context: Context, themeName: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, themeName).apply()
    }
}