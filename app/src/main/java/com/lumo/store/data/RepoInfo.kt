package com.lumo.store.data

data class RepoInfo(
    val owner: String = "",
    val repoName: String = "",
    val githubRepo: String = "",
    val description: String = "",
    val authorName: String = "",
    val packageName: String = "",
    val suggestedIconUrl: String = "",
    val suggestedCategory: String = "Utility"
)