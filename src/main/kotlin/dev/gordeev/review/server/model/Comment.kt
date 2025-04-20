package dev.gordeev.review.server.model

import java.time.OffsetDateTime

data class Comment(val id: Long, val content: String, val updatedAt: OffsetDateTime)
