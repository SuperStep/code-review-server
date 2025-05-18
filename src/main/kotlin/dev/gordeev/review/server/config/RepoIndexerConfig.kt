package dev.gordeev.review.server.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "indexer")
class RepoIndexerConfig(
    val repositories: List<RepositoryConfig> = emptyList(),
    val clone: CloneConfig = CloneConfig()
)

data class RepositoryConfig(
    val name: String,
    val url: String,
    val branch: String? = null, // Optional: specific branch to clone
    val includedExtensions: List<String>? = null // Optional: list of file extensions to include (e.g., [".txt", ".md"])
)

data class CloneConfig(
    val basePath: String = "./cloned_repos_temp"
)