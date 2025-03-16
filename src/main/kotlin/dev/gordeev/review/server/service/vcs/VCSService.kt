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
     * Posts a review comment on a pull request.
     *
     * @param prId The pull request ID
     * @param comment The comment text to post
     * @return Boolean indicating if the comment was posted successfully
     */
    fun postReviewComment(prId: Long, comment: String): Boolean

    /**
     * Gets all comments for a specific pull request
     * @param prId The pull request ID
     * @return List of comments as a JSON string
     */
    fun getPullRequestComments(prId: Long): String

    /**
     * Searches for a specific pattern in comments of a pull request
     * @param prId The pull request ID
     * @param pattern The string pattern to search for
     * @return Map of comment IDs to comment text that match the pattern
     */
    fun searchPatternInPullRequestComments(prId: Long, pattern: String): Map<Long, String>
}