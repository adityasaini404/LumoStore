package com.lumo.store.data

import android.content.Context

object FavoritesManager {
    private const val PREF_NAME = "lumo_favorites"
    private const val KEY_FAVS = "saved_apps"

    fun toggleFavorite(context: Context, appId: String): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val currentFavs = prefs.getStringSet(KEY_FAVS, emptySet())?.toMutableSet() ?: mutableSetOf()

        val isNowFavorite: Boolean
        if (currentFavs.contains(appId)) {
            currentFavs.remove(appId)
            isNowFavorite = false
        } else {
            currentFavs.add(appId)
            isNowFavorite = true
        }

        prefs.edit().putStringSet(KEY_FAVS, currentFavs).apply()
        return isNowFavorite
    }

    fun isFavorite(context: Context, appId: String): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_FAVS, emptySet())?.contains(appId) == true
    }

    fun getFavorites(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_FAVS, emptySet()) ?: emptySet()
    }
}