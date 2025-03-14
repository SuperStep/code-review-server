package dev.gordeev.review.server.model

data class ReviewResult(
    val pullRequest: PullRequest,
    val reviewContent: String,
    val status: ReviewStatus
)

