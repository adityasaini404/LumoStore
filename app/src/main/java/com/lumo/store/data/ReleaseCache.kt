package com.lumo.store.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString          // ⬅️ यह import ज़रूरी है
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class CachedRelease(
    val version: String = "",
    val releaseNotes: String = "",
    val sizeStr: String = "",
    val downloadUrl: String = "",
    val cachedAt: Long = 0L
)

object ReleaseCache {
    private const val CACHE_FILE = "release_cache.json"
    private const val TTL_MS = 24 * 60 * 60 * 1000L  // 24 hours

    private fun cacheFile(context: Context) = File(context.filesDir, CACHE_FILE)
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun loadAll(context: Context): MutableMap<String, CachedRelease> {
        return try {
            val file = cacheFile(context)
            if (!file.exists()) return mutableMapOf()
            val text = file.readText()
            val map: Map<String, CachedRelease> = json.decodeFromString(text)
            map.toMutableMap()
        } catch (e: Exception) { mutableMapOf() }
    }

    private fun saveAll(context: Context, map: Map<String, CachedRelease>) {
        try {
            // ✅ Explicit type parameter — infer नहीं हो रहा था
            val text: String = json.encodeToString<Map<String, CachedRelease>>(map)
            cacheFile(context).writeText(text)
        } catch (e: Exception) {}
    }

    /** अगर cache 24h से कम पुराना है तो return करेगा, वरना null */
    fun get(context: Context, repo: String): CachedRelease? {
        val cached = loadAll(context)[repo] ?: return null
        val age = System.currentTimeMillis() - cached.cachedAt
        return if (age < TTL_MS) cached else null
    }

    fun put(context: Context, repo: String, release: CachedRelease) {
        val all = loadAll(context)
        all[repo] = release.copy(cachedAt = System.currentTimeMillis())
        saveAll(context, all)
    }

    fun clear(context: Context) {
        try { cacheFile(context).delete() } catch (e: Exception) {}
    }
}