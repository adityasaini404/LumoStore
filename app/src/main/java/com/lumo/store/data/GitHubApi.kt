package com.lumo.store.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class CatalogItem(
    val name: String,
    val id: String = "",
    val description: String = "",
    val icon: String = "",
    val repoUrl: String = "",
    val githubRepo: String = "",
    val category: String = "Utility",
    val author: String = "",
    val packageName: String = "",
    val screenshots: List<String> = emptyList(),
    val downloadUrl: String = ""
)

@Serializable
data class MirrorEntry(
    val version: String = "",
    val releaseNotes: String = "",
    val size: Long = 0L,
    val downloadUrl: String = ""
)

class RateLimitException(
    val resetTime: String,
    val isUsingDefaultToken: Boolean
) : Exception("Rate limit exceeded. Resets at $resetTime")

object GitHubApi {

    // Leave empty to use anonymous free tier (60/hr)
    // Paste "ghp_..." here for a developer token (5,000/hr)
    private const val DEFAULT_TOKEN = ""

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

    // ---------- URL NORMALIZERS ----------

    /**
     * Converts GitHub "blob" or "raw" URLs into direct raw.githubusercontent.com URLs
     * that serve the actual image bytes instead of an HTML page.
     *
     * Example:
     *  github.com/user/repo/blob/main/icon.png
     *      -> raw.githubusercontent.com/user/repo/main/icon.png
     */
    fun normalizeImageUrl(url: String?): String {
        if (url.isNullOrBlank()) return ""
        var u = url.trim()

        // Strip any existing "?raw=true" or similar query suffixes
        u = u.substringBefore("?")

        // github.com/user/repo/blob/branch/path -> raw.githubusercontent.com/user/repo/branch/path
        u = u.replace(
            Regex("""^https?://(?:www\.)?github\.com/([^/]+)/([^/]+)/blob/(.+)$"""),
            "https://raw.githubusercontent.com/$1/$2/$3"
        )

        // github.com/user/repo/raw/branch/path -> raw.githubusercontent.com/user/repo/branch/path
        u = u.replace(
            Regex("""^https?://(?:www\.)?github\.com/([^/]+)/([^/]+)/raw/(.+)$"""),
            "https://raw.githubusercontent.com/$1/$2/$3"
        )

        return u
    }

    /**
     * Converts any form of GitHub repo reference into "owner/repo".
     *
     * Accepted inputs:
     *  - "owner/repo"
     *  - "https://github.com/owner/repo"
     *  - "https://github.com/owner/repo/tree/main"
     *  - "https://github.com/https://github.com/owner/repo"  (double prefix)
     */
    fun normalizeGithubRepo(repo: String?): String {
        if (repo.isNullOrBlank()) return ""
        var r = repo.trim()

        // Strip multiple "https://github.com/" prefixes (handles the double-prefix bug)
        while (r.contains("github.com/github.com/")) {
            r = r.replace("github.com/github.com/", "github.com/")
        }
        while (r.contains("github.com/https://github.com/")) {
            r = r.replace("github.com/https://github.com/", "github.com/")
        }
        while (r.contains("github.com/http://github.com/")) {
            r = r.replace("github.com/http://github.com/", "github.com/")
        }

        r = r.removePrefix("https://github.com/")
        r = r.removePrefix("http://github.com/")
        r = r.removePrefix("https://www.github.com/")
        r = r.removePrefix("http://www.github.com/")
        r = r.removePrefix("www.github.com/")
        r = r.removePrefix("github.com/")

        // Strip any path that follows the owner/repo
        r = r.substringBefore("/blob/")
        r = r.substringBefore("/tree/")
        r = r.substringBefore("/releases/")
        r = r.substringBefore("/raw/")
        r = r.removeSuffix("/")

        return r
    }

    /**
     * Returns a clean https://github.com/owner/repo URL.
     * Handles the case where repoUrl was stored with a double prefix or is empty.
     */
    fun normalizeRepoUrl(url: String?, githubRepo: String?): String {
        // If the URL is present and doesn't have the double-prefix bug, keep it
        if (!url.isNullOrBlank()
            && !url.contains("github.com/https")
            && !url.contains("github.com/http")
            && !url.contains("github.com/github.com")
        ) {
            return url.trim().substringBefore("?").removeSuffix("/")
        }
        val cleanRepo = normalizeGithubRepo(githubRepo)
        return if (cleanRepo.isNotBlank()) "https://github.com/$cleanRepo" else ""
    }

    // ---------- TOKEN + HEADERS ----------

    private fun getEffectiveToken(userToken: String?): String? {
        if (!userToken.isNullOrBlank()) return userToken.trim()
        val d = DEFAULT_TOKEN.trim()
        return when {
            d.isEmpty() -> null
            d.contains("YOUR", ignoreCase = true) -> null
            d.startsWith("ghp_") || d.startsWith("github_pat_") -> d
            else -> null
        }
    }

    private fun HttpRequestBuilder.applyGitHubHeaders(token: String?) {
        header("Accept", "application/vnd.github+json")
        header("User-Agent", "LumoStore-App")
        if (!token.isNullOrBlank()) header("Authorization", "Bearer $token")
    }

    private fun checkRateLimit(
        status: HttpStatusCode,
        headers: io.ktor.http.Headers,
        userToken: String?
    ) {
        val remaining = headers["x-ratelimit-remaining"]?.toIntOrNull() ?: -1
        val isLimit = (status == HttpStatusCode.Forbidden ||
                status == HttpStatusCode.TooManyRequests) && remaining == 0
        if (!isLimit) return

        val reset = headers["x-ratelimit-reset"] ?: ""
        val formatted = try {
            val epoch = reset.toLongOrNull()
            if (epoch != null) java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(epoch * 1000))
            else "जल्द"
        } catch (e: Exception) { "जल्द" }

        throw RateLimitException(
            resetTime = formatted,
            isUsingDefaultToken = userToken.isNullOrBlank()
        )
    }

    private fun HttpStatusCode.isOk(): Boolean = value in 200..299

    private suspend fun tryGet(url: String, token: String?): Pair<HttpResponse, Boolean> {
        val first = client.get(url) { applyGitHubHeaders(token) }
        if (first.status.value == 401 && token != null) {
            val anon = client.get(url) { applyGitHubHeaders(null) }
            return anon to true
        }
        return first to false
    }

    // ---------- API CALLS ----------

    suspend fun fetchMirror(mirrorUrl: String): Map<String, MirrorEntry>? {
        return try {
            val url = "$mirrorUrl?t=${System.currentTimeMillis()}"
            val text = client.get(url) {
                header("User-Agent", "LumoStore-App")
            }.bodyAsText()

            if (text.isBlank() || text.trimStart().startsWith("<")) return null

            val map: Map<String, MirrorEntry> = jsonParser.decodeFromString(text)
            if (map.isEmpty()) null else map
        } catch (e: Exception) {
            android.util.Log.w("LumoStore", "mirror fetch failed: ${e.message}")
            null
        }
    }

    suspend fun getLatestRelease(owner: String, repo: String, userToken: String?): GitHubRelease? {
        val token = getEffectiveToken(userToken)
        val latestUrl = "https://api.github.com/repos/$owner/$repo/releases/latest"

        val (resp, anon1) = tryGet(latestUrl, token)
        checkRateLimit(resp.status, resp.headers, if (anon1) null else userToken)
        if (resp.status.isOk()) {
            return try { resp.body<GitHubRelease>() } catch (e: Exception) { null }
        }

        if (resp.status == HttpStatusCode.NotFound) {
            val listUrl = "https://api.github.com/repos/$owner/$repo/releases"
            val (listResp, anon2) = tryGet(listUrl, token)
            checkRateLimit(listResp.status, listResp.headers, if (anon2) null else userToken)
            if (listResp.status.isOk()) {
                return try {
                    val releases: List<GitHubRelease> = listResp.body()
                    releases.firstOrNull()
                } catch (e: Exception) { null }
            }
        }
        return null
    }

    suspend fun fetchCatalogRaw(url: String): String? {
        return try {
            val timestamp = System.currentTimeMillis()
            client.get("$url?t=$timestamp") {
                header("User-Agent", "LumoStore-App")
            }.bodyAsText()
        } catch (e: Exception) { null }
    }

    /**
     * Parses the raw catalog JSON and normalizes every URL and repo reference.
     * This fixes entries that were submitted with github.com/blob URLs,
     * double-prefixed repo URLs, or githubRepo fields that contain full URLs.
     */
    fun parseCatalog(jsonString: String): List<CatalogItem> {
        return try {
            val items: List<CatalogItem> = jsonParser.decodeFromString(jsonString)
            items.map { item ->
                item.copy(
                    icon = normalizeImageUrl(item.icon),
                    screenshots = item.screenshots.map { normalizeImageUrl(it) },
                    githubRepo = normalizeGithubRepo(item.githubRepo),
                    repoUrl = normalizeRepoUrl(item.repoUrl, item.githubRepo)
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("LumoStore", "parseCatalog error: ${e.message}")
            emptyList()
        }
    }
}