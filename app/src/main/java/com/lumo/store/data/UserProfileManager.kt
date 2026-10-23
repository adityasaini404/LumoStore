package com.lumo.store.data

import android.content.Context

/**
 * Simple SharedPreferences-based storage for the user's display name
 * and selected avatar index.
 */
object UserProfileManager {
    private const val PREF = "lumo_profile"
    private const val KEY_NAME = "display_name"
    private const val KEY_AVATAR = "avatar_index"

    fun getName(context: Context): String {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_NAME, "") ?: ""
    }

    fun setName(context: Context, name: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY_NAME, name.trim()).apply()
    }

    /** Returns the saved avatar index (0..3). Defaults to 0 (Robot). */
    fun getAvatarIndex(context: Context): Int {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(KEY_AVATAR, 0).coerceIn(0, 3)
    }

    fun setAvatarIndex(context: Context, index: Int) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putInt(KEY_AVATAR, index.coerceIn(0, 3)).apply()
    }
}