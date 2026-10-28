package com.lumo.store.data

object VersionUtils {
    fun normalizeVersion(version: String): String {
        var v = version.trim().lowercase()
        v = v.removePrefix("v")
        v = v.removePrefix("release-")
        v = v.removePrefix("version-")
        v = v.replace(Regex("\\(.*?\\)"), "")
        v = v.substringBefore("-")
        v = v.substringBefore("+")
        v = v.replace(Regex("[^0-9.]"), "")
        v = v.trim('.')
        return v
    }

    fun isNewerVersion(installed: String, remote: String): Boolean {
        if (remote == "N/A" || remote.isBlank()) return false
        if (installed.isBlank() || installed == "0") return false

        val ni = normalizeVersion(installed)
        val nr = normalizeVersion(remote)

        if (ni.isEmpty() || nr.isEmpty()) return false
        if (ni == nr) return false

        val iParts = ni.split(".").mapNotNull { it.toIntOrNull() }
        val rParts = nr.split(".").mapNotNull { it.toIntOrNull() }

        if (iParts.isEmpty() || rParts.isEmpty()) return false

        val maxLen = maxOf(iParts.size, rParts.size)
        for (i in 0 until maxLen) {
            val a = iParts.getOrElse(i) { 0 }
            val b = rParts.getOrElse(i) { 0 }
            if (b > a) return true
            if (b < a) return false
        }
        return false
    }
}