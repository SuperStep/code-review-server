package dev.gordeev.review.server.model

data class PullRequestToReview(
    val pullRequest: PullRequest,
    val reviewComment: String
)
