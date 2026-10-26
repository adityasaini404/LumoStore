package com.lumo.store.data

data class AppInfo(
    val name: String,
    val id: String = "",
    val description: String,
    val icon: String = "",
    val repoUrl: String = "",
    val githubRepo: String = "",
    val gitlabRepo: String = "",
    val gitlabDomain: String = "",
    val codebergRepo: String = "",
    val releaseKeyword: String = "apk",
    val packageName: String = "",
    val category: String = "Utility",
    val author: String = "",
    val downloadUrl: String = "",
    val screenshots: List<String> = emptyList(),

    // ---------- Runtime fields ----------
    val version: String = "",
    val size: String = "",
    val isInstalled: Boolean = false,
    val isUpdateAvailable: Boolean = false,
    val releaseNotes: String = "",

    // ---------- NEW: downloads + variants ----------
    val downloadCount: Long = 0L,
    val variants: List<ApkVariant> = emptyList()
)

/**
 * Represents a single APK variant (arm64, armv7, x86, universal, etc.)
 * Extracted from GitHub release assets.
 */
data class ApkVariant(
    val filename: String = "",
    val arch: String = "universal",          // "arm64-v8a", "armeabi-v7a", "x86", "x86_64", "universal"
    val size: Long = 0L,                     // bytes
    val downloadUrl: String = "",
    val downloadCount: Long = 0L
) {
    /** Human-readable size like "12.4 MB" */
    val sizeFormatted: String
        get() {
            if (size <= 0L) return ""
            val mb = size / (1024.0 * 1024.0)
            return if (mb >= 1.0) String.format("%.1f MB", mb)
            else "${size / 1024} KB"
        }

    /** Friendly label: "arm64-v8a" → "ARM64" */
    val archLabel: String
        get() = when (arch) {
            "arm64-v8a" -> "ARM64 (64-bit)"
            "armeabi-v7a" -> "ARMv7 (32-bit)"
            "x86_64" -> "x86_64"
            "x86" -> "x86"
            "universal" -> "Universal"
            else -> arch
        }
}