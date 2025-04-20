package dev.gordeev.review.server.model

import java.time.OffsetDateTime

data class PullRequestToReview(
    val pullRequest: PullRequest,
    val reviewComment: String,
    val commentRequestDateTime: OffsetDateTime,
)
