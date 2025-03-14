package dev.gordeev.review.server.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PullRequestResponse(
    val values: List<PullRequest>,
    val isLastPage: Boolean,
    val start: Int,
    val size: Int
)