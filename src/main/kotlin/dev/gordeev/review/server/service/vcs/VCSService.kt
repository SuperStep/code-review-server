package dev.gordeev.review.server.service.vcs

import dev.gordeev.review.server.model.PullRequestPage

/**
 * Interface for Version Control System operations.
 * Provides abstraction for different VCS providers (Bitbucket, GitHub, GitLab, etc.)
 */
interface VCSService {
    /**
     * Retrieves open pull requests from the VCS.
     *
     * @param start The starting index for pagination
     * @param limit The maximum number of results to return
     * @return List of open pull requests
     */
    fun getOpenPullRequests(start: Int = 0, limit: Int = 25): PullRequestPage

    /**
     * Retrieves the diff content for a specific pull request.
     *
     * @param prId The pull request ID
     * @return The diff content as a string
     */
    fun getPullRequestDiff(prId: Long): String

    /**
     * Posts a review comment on a pull request.
     *
     * @param prId The pull request ID
     * @param comment The comment text to post
     * @return Boolean indicating if the comment was posted successfully
     */
    fun postReviewComment(prId: Long, comment: String): Boolean
}
