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
    // runtime fields
    val version: String = "",
    val size: String = "",
    val isInstalled: Boolean = false,
    val isUpdateAvailable: Boolean = false,
    val releaseNotes: String = ""
)