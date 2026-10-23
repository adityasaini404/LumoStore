package com.lumo.store.data

import android.content.Context

object TokenManager {
    private const val PREF_NAME = "lumo_settings"
    private const val KEY_TOKEN = "user_github_token"

    fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val token = prefs.getString(KEY_TOKEN, null)
        return if (token.isNullOrBlank()) null else token
    }

    fun saveToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun clearToken(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    // ✅ नया helper
    fun hasUserToken(context: Context): Boolean = getToken(context) != null
}