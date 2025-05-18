package dev.gordeev.review.server.model

data class FileData(
    val path: String,       // Relative path within the repository
    val fileName: String,
    val content: String
)
