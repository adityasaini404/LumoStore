package com.lumo.store.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubRelease(
    @SerialName("tag_name") val tagName: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("body") val body: String = "",
    @SerialName("html_url") val htmlUrl: String = "",
    @SerialName("published_at") val publishedAt: String = "",
    @SerialName("assets") val assets: List<GitHubAsset> = emptyList()
)

@Serializable
data class GitHubAsset(
    @SerialName("name") val name: String = "",
    @SerialName("size") val size: Long = 0L,
    @SerialName("browser_download_url") val downloadUrl: String = "",
    @SerialName("download_count") val downloadCount: Long = 0L   // ✅ NEW
)