package com.lumo.store.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lumo.store.data.AppInfo
import com.lumo.store.data.ApkVariant
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

                val mirror = withContext(Dispatchers.IO) {
                    GitHubApi.fetchMirror(mirrorUrl)
                }
                android.util.Log.i("LumoStore", "mirror entries: ${mirror?.size ?: 0}")

                val cachedJson = CacheManager.getCatalog(getApplication())
                if (cachedJson != null) {
                    val cachedCatalog = GitHubApi.parseCatalog(cachedJson)
                    if (cachedCatalog.isNotEmpty()) {
                        _apps.value = processCatalog(cachedCatalog, userToken, mirror)
                        _loading.value = false
                    }
                }

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
                var downloadCount = 0L
                var variants: List<ApkVariant> = emptyList()

                val mEntry = mirror?.get(item.githubRepo)
                if (mEntry != null) {
                    if (mEntry.version.isNotBlank()) version = mEntry.version
                    if (mEntry.size > 0L) {
                        val mb = mEntry.size / (1024.0 * 1024.0)
                        sizeStr = if (mb >= 1.0) String.format("%.1f MB", mb) else "${mEntry.size / 1024} KB"
                    }
                    if (mEntry.releaseNotes.isNotBlank()) releaseNotes = mEntry.releaseNotes
                    if (actualDownloadUrl.isBlank()) actualDownloadUrl = mEntry.downloadUrl

                    downloadCount = mEntry.downloadCount
                    variants = mEntry.variants.map { v ->
                        ApkVariant(
                            filename = v.filename,
                            arch = v.arch,
                            size = v.size,
                            downloadUrl = v.downloadUrl,
                            downloadCount = v.downloadCount
                        )
                    }
                } else if (item.githubRepo.isNotBlank() && item.githubRepo.contains("/")) {
                    try {
                        val parts = item.githubRepo.split("/")
                        if (parts.size == 2) {
                            val release = GitHubApi.getLatestRelease(parts[0], parts[1], userToken)
                            if (release != null) {
                                if (release.tagName.isNotBlank()) version = release.tagName
                                releaseNotes = if (release.body.isNotBlank()) release.body else "No release notes available."

                                val apkAssets = release.assets?.filter {
                                    it.name.endsWith(".apk", ignoreCase = true)
                                } ?: emptyList()

                                variants = apkAssets.map { asset ->
                                    ApkVariant(
                                        filename = asset.name,
                                        arch = detectArch(asset.name),
                                        size = asset.size,
                                        downloadUrl = asset.downloadUrl,
                                        downloadCount = asset.downloadCount
                                    )
                                }

                                downloadCount = apkAssets.sumOf { it.downloadCount }

                                val mainAsset = apkAssets.firstOrNull { detectArch(it.name) == "arm64-v8a" }
                                    ?: apkAssets.firstOrNull()

                                if (mainAsset != null) {
                                    val mb = mainAsset.size / (1024.0 * 1024.0)
                                    sizeStr = if (mb >= 1.0) String.format("%.1f MB", mb) else "${mainAsset.size / 1024} KB"
                                    if (actualDownloadUrl.isBlank()) actualDownloadUrl = mainAsset.downloadUrl
                                }
                            }
                        }
                    } catch (e: RateLimitException) {
                        rateLimitSkipCount++
                        android.util.Log.w("LumoStore", "Rate limit for ${item.name} — skipped")
                    } catch (e: Exception) {
                        android.util.Log.w("LumoStore", "Fallback failed for ${item.name}: ${e.message}")
                    }
                }

                if (item.packageName.isNotBlank()) {
                    val (installed, installedVersion, _) = checkInstalled(
                        pm,
                        item.packageName,
                        appName = item.name
                    )
                    isInstalled = installed

                    if (installed && installedVersion != null) {
                        isUpdateAvailable = isNewerVersion(installedVersion, version)
                    }
                }

                list.add(
                    AppInfo(
                        name = item.name,
                        id = item.id,
                        description = item.description,
                        icon = item.icon.ifBlank {
                            if (item.githubRepo.contains("/"))
                                "https://github.com/${item.githubRepo.split("/")[0]}.png"
                            else ""
                        },
                        repoUrl = item.repoUrl,
                        githubRepo = item.githubRepo,
                        category = item.category,
                        author = item.author,
                        screenshots = item.screenshots,
                        version = version,
                        size = sizeStr,
                        downloadUrl = actualDownloadUrl,
                        packageName = item.packageName,
                        isInstalled = isInstalled,
                        isUpdateAvailable = isUpdateAvailable,
                        releaseNotes = releaseNotes,
                        downloadCount = downloadCount,
                        variants = variants
                    )
                )
            }

            if (rateLimitSkipCount > 0) {
                android.util.Log.w("LumoStore", "Total $rateLimitSkipCount apps skipped due to rate limit")
            }

            list
        }
    }

    // ---------- ROBUST INSTALLED CHECK ----------
    private fun checkInstalled(
        pm: PackageManager,
        packageName: String,
        appName: String = ""
    ): Triple<Boolean, String?, Boolean> {
        // ---------- Method 1: Exact package name (fastest, 95% cases) ----------
        try {
            val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, 0)
            }
            val installedVersion = pInfo.versionName ?: "0"
            val hasLaunchIntent = try {
                pm.getLaunchIntentForPackage(packageName) != null
            } catch (_: Exception) { false }

            android.util.Log.d(
                "LumoStore",
                "✅ INSTALLED: $packageName v$installedVersion (launchable: $hasLaunchIntent)"
            )
            return Triple(true, installedVersion, hasLaunchIntent)
        } catch (_: PackageManager.NameNotFoundException) {
            // Continue to fallbacks
        } catch (e: Exception) {
            android.util.Log.w("LumoStore", "⚠️ Error checking $packageName: ${e.message}")
        }

        // ---------- Method 2: Fuzzy package name match (case/trim variants) ----------
        val variants = listOf(
            packageName.trim(),
            packageName.lowercase(),
            packageName.trim().lowercase()
        ).distinct().filter { it != packageName }

        for (variant in variants) {
            try {
                val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getPackageInfo(variant, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(variant, 0)
                }
                android.util.Log.w("LumoStore", "⚠️ Found via variant: $variant (original: $packageName)")
                return Triple(true, pInfo.versionName ?: "0", false)
            } catch (_: Exception) { }
        }

        // ---------- Method 3: Search installed packages for partial match ----------
        try {
            val installedPackages = pm.getInstalledPackages(0)
            val partialMatch = installedPackages.firstOrNull { pkg ->
                pkg.packageName.equals(packageName, ignoreCase = true) ||
                        pkg.packageName.contains(packageName, ignoreCase = true) ||
                        packageName.contains(pkg.packageName, ignoreCase = true)
            }
            if (partialMatch != null) {
                val installedVersion = partialMatch.versionName ?: "0"
                android.util.Log.w(
                    "LumoStore",
                    "⚠️ Found via partial match: ${partialMatch.packageName} v$installedVersion (catalog: $packageName)"
                )
                return Triple(true, installedVersion, false)
            }
        } catch (_: Exception) { }

        // ---------- Method 4: Match by app display name (safest for renamed packages) ----------
        if (appName.isNotBlank()) {
            try {
                val installedApps = pm.getInstalledApplications(0)
                val labelMatch = installedApps.firstOrNull { app ->
                    val label = try { pm.getApplicationLabel(app).toString() } catch (_: Exception) { "" }
                    val cleanLabel = label.trim().lowercase()
                    val cleanName = appName.trim().lowercase()
                    cleanLabel == cleanName ||
                            (cleanLabel.isNotBlank() && cleanName.isNotBlank() &&
                                    (cleanLabel.contains(cleanName) || cleanName.contains(cleanLabel)))
                }
                if (labelMatch != null) {
                    val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pm.getPackageInfo(labelMatch.packageName, PackageManager.PackageInfoFlags.of(0))
                    } else {
                        @Suppress("DEPRECATION")
                        pm.getPackageInfo(labelMatch.packageName, 0)
                    }
                    android.util.Log.w(
                        "LumoStore",
                        "⚠️ Found by name '$appName': ${labelMatch.packageName} v${pInfo.versionName} (catalog: $packageName)"
                    )
                    return Triple(true, pInfo.versionName ?: "0", false)
                }
            } catch (_: Exception) { }
        }

        android.util.Log.d("LumoStore", "❌ NOT INSTALLED: $packageName ($appName)")
        return Triple(false, null, false)
    }

    // ---------- VERSION NORMALIZE (FIXED) ----------
    private fun normalizeVersion(version: String): String {
        var v = version.trim().lowercase()
        v = v.removePrefix("v")
        v = v.removePrefix("release-")
        v = v.removePrefix("version-")
        // 🔥 CRITICAL FIX: strip parentheses FIRST (e.g. "1.5.0 (100)" → "1.5.0")
        v = v.replace(Regex("\\(.*?\\)"), "")
        v = v.substringBefore("-")
        v = v.substringBefore("+")
        v = v.replace(Regex("[^0-9.]"), "")
        v = v.trim('.')
        return v
    }

    // ---------- VERSION COMPARE (WITH LOGGING) ----------
    private fun isNewerVersion(installed: String, remote: String): Boolean {
        if (remote == "N/A" || remote.isBlank()) return false
        if (installed.isBlank() || installed == "0") return false

        val ni = normalizeVersion(installed)
        val nr = normalizeVersion(remote)

        if (ni.isEmpty() || nr.isEmpty()) return false

        if (ni == nr) {
            android.util.Log.d("LumoStore", "version-match: installed=$installed($ni) remote=$remote($nr)")
            return false
        }

        val iParts = ni.split(".").mapNotNull { it.toIntOrNull() }
        val rParts = nr.split(".").mapNotNull { it.toIntOrNull() }

        if (iParts.isEmpty() || rParts.isEmpty()) return false

        val maxLen = maxOf(iParts.size, rParts.size)
        for (i in 0 until maxLen) {
            val a = iParts.getOrElse(i) { 0 }
            val b = rParts.getOrElse(i) { 0 }
            if (b > a) {
                android.util.Log.d("LumoStore", "version-update: installed=$installed($ni) remote=$remote($nr) → TRUE")
                return true
            }
            if (b < a) {
                android.util.Log.d("LumoStore", "version-older: installed=$installed($ni) remote=$remote($nr) → FALSE")
                return false
            }
        }
        return false
    }

    private fun detectArch(filename: String): String {
        val lower = filename.lowercase()
        return when {
            lower.contains("arm64-v8a") || lower.contains("arm64") || lower.contains("aarch64") -> "arm64-v8a"
            lower.contains("armeabi-v7a") || lower.contains("armv7") || lower.contains("armeabi") -> "armeabi-v7a"
            lower.contains("x86_64") -> "x86_64"
            lower.contains("x86") -> "x86"
            lower.contains("universal") -> "universal"
            else -> "universal"
        }
    }
}