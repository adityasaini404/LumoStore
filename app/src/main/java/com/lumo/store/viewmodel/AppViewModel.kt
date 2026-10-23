package com.lumo.store.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lumo.store.data.AppInfo
import com.lumo.store.data.CacheManager
import com.lumo.store.data.GitHubApi
import com.lumo.store.data.RateLimitException
import com.lumo.store.data.RateLimitState
import com.lumo.store.data.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError

    private val _rateLimitMessage = MutableStateFlow<String?>(null)
    val rateLimitMessage: StateFlow<String?> = _rateLimitMessage

    private val catalogUrl = "https://raw.githubusercontent.com/adityasaini404/lumo-store-apps/main/apps.json"
    private val mirrorUrl  = "https://raw.githubusercontent.com/adityasaini404/lumo-store-apps/main/mirror.json"

    init { loadApps() }

    fun loadApps() {
        viewModelScope.launch {
            try {
                _rateLimitMessage.value = null
                _isError.value = false

                val userToken = TokenManager.getToken(getApplication())

                // ✅ mirror एक ही बार fetch करें (cache और network दोनों के लिए reuse)
                val mirror = withContext(Dispatchers.IO) {
                    GitHubApi.fetchMirror(mirrorUrl)
                }
                android.util.Log.i("LumoStore", "mirror entries: ${mirror?.size ?: 0}")

                // 1) cache दिखाओ (instant UI)
                val cachedJson = CacheManager.getCatalog(getApplication())
                if (cachedJson != null) {
                    val cachedCatalog = GitHubApi.parseCatalog(cachedJson)
                    if (cachedCatalog.isNotEmpty()) {
                        _apps.value = processCatalog(cachedCatalog, userToken, mirror)
                        _loading.value = false
                    }
                }

                // 2) network से fresh catalog
                withContext(Dispatchers.IO) {
                    val networkJson = GitHubApi.fetchCatalogRaw(catalogUrl)
                    if (networkJson != null) {
                        CacheManager.saveCatalog(getApplication(), networkJson)
                        val networkCatalog = GitHubApi.parseCatalog(networkJson)
                        if (networkCatalog.isNotEmpty()) {
                            _apps.value = processCatalog(networkCatalog, userToken, mirror)
                        }
                    } else if (_apps.value.isEmpty()) {
                        _isError.value = true
                    }
                }
            } catch (e: RateLimitException) {
                // ✅ dialog open when it not get and apps
                if (_apps.value.isEmpty()) {
                    RateLimitState.notify(e)
                    _rateLimitMessage.value = if (e.isUsingDefaultToken)
                        "Free limit reached. Resets at ${e.resetTime}"
                    else
                        "Your token limit reached. Resets at ${e.resetTime}"
                    _isError.value = true
                } else {
                    android.util.Log.w("LumoStore", "Rate limit (non-fatal): ${e.resetTime}")
                }
            } catch (e: Exception) {
                android.util.Log.e("LumoStore", "FATAL", e)
                if (_apps.value.isEmpty()) _isError.value = true
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun processCatalog(
        catalog: List<com.lumo.store.data.CatalogItem>,
        userToken: String?,
        mirror: Map<String, com.lumo.store.data.MirrorEntry>?
    ): List<AppInfo> {
        return withContext(Dispatchers.IO) {
            val list = mutableListOf<AppInfo>()
            val app = getApplication<Application>()
            val pm = app.packageManager

            var rateLimitSkipCount = 0

            catalog.forEach { item ->
                var version = "N/A"
                var sizeStr = ""
                var actualDownloadUrl = item.downloadUrl
                var isInstalled = false
                var isUpdateAvailable = false
                var releaseNotes = "No release notes available."

                // ✅ 1) mirror से लो (0 API calls)
                val mEntry = mirror?.get(item.githubRepo)
                if (mEntry != null) {
                    if (mEntry.version.isNotBlank()) version = mEntry.version
                    if (mEntry.size > 0L) {
                        val mb = mEntry.size / (1024.0 * 1024.0)
                        sizeStr = if (mb >= 1.0) String.format("%.1f MB", mb) else "${mEntry.size / 1024} KB"
                    }
                    if (mEntry.releaseNotes.isNotBlank()) releaseNotes = mEntry.releaseNotes
                    if (actualDownloadUrl.isBlank()) actualDownloadUrl = mEntry.downloadUrl
                } else if (item.githubRepo.isNotBlank() && item.githubRepo.contains("/")) {
                    // ✅ 2) mirror में नहीं → API fallback (पर अगर rate limit हो, सिर्फ़ skip)
                    try {
                        val parts = item.githubRepo.split("/")
                        if (parts.size == 2) {
                            val release = GitHubApi.getLatestRelease(parts[0], parts[1], userToken)
                            if (release != null) {
                                if (release.tagName.isNotBlank()) version = release.tagName
                                releaseNotes = if (release.body.isNotBlank()) release.body else "No release notes available."
                                val apkAsset = release.assets?.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
                                if (apkAsset != null) {
                                    val mb = apkAsset.size / (1024.0 * 1024.0)
                                    sizeStr = if (mb >= 1.0) String.format("%.1f MB", mb) else "${apkAsset.size / 1024} KB"
                                    if (actualDownloadUrl.isBlank()) actualDownloadUrl = apkAsset.downloadUrl
                                }
                            }
                        }
                    } catch (e: RateLimitException) {
                        // ✅ अब throw नहीं — बस skip और आगे बढ़ो
                        rateLimitSkipCount++
                        android.util.Log.w("LumoStore", "Rate limit for ${item.name} — skipped")
                    } catch (e: Exception) {
                        android.util.Log.w("LumoStore", "Fallback failed for ${item.name}: ${e.message}")
                    }
                }

                if (item.packageName.isNotBlank()) {
                    try {
                        val pInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            pm.getPackageInfo(item.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
                        } else {
                            @Suppress("DEPRECATION")
                            pm.getPackageInfo(item.packageName, 0)
                        }
                        isInstalled = true
                        val installedVersion = pInfo.versionName ?: "0"
                        if (version != "N/A" && installedVersion != version && !version.contains(installedVersion)) {
                            isUpdateAvailable = true
                        }
                    } catch (e: Exception) { }
                }

                list.add(
                    AppInfo(
                        name = item.name, id = item.id, description = item.description,
                        icon = item.icon.ifBlank {
                            if (item.githubRepo.contains("/"))
                                "https://github.com/${item.githubRepo.split("/")[0]}.png"
                            else ""
                        },
                        repoUrl = item.repoUrl, githubRepo = item.githubRepo, category = item.category,
                        author = item.author, screenshots = item.screenshots, version = version, size = sizeStr,
                        downloadUrl = actualDownloadUrl, packageName = item.packageName,
                        isInstalled = isInstalled, isUpdateAvailable = isUpdateAvailable, releaseNotes = releaseNotes
                    )
                )
            }

            if (rateLimitSkipCount > 0) {
                android.util.Log.w("LumoStore", "Total $rateLimitSkipCount apps skipped due to rate limit — mirror missing them")
            }

            list
        }
    }
}