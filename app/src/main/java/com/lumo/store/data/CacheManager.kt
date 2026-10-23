package com.lumo.store.data

import android.content.Context
import java.io.File

object CacheManager {
    private const val CACHE_FILE_NAME = "lumo_catalog_cache.json"

    // डेटा को फोन में सेव करना
    fun saveCatalog(context: Context, jsonString: String) {
        try {
            val file = File(context.filesDir, CACHE_FILE_NAME)
            file.writeText(jsonString)
        } catch (e: Exception) {
            android.util.Log.e("LumoStore", "Cache save failed: ${e.message}")
        }
    }

    // सेव किया हुआ डेटा वापस लाना
    fun getCatalog(context: Context): String? {
        return try {
            val file = File(context.filesDir, CACHE_FILE_NAME)
            if (file.exists()) file.readText() else null
        } catch (e: Exception) {
            null
        }
    }
}