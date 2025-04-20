package dev.gordeev.review.server.service.vcs.impl.gitea

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import dev.gordeev.review.server.config.GiteaProperties
import dev.gordeev.review.server.model.Comment
import dev.gordeev.review.server.model.PullRequest
import dev.gordeev.review.server.model.PullRequestPage
import dev.gordeev.review.server.service.vcs.VCSService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
@Primary
class GiteaService(
    private val giteaProperties: GiteaProperties,
    private val okHttpClient: OkHttpClient,
    private val objectMapper: ObjectMapper
) : VCSService {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val apiBasePath = "/api/v1" // Standard Gitea API base path

    /**
     * Fetches open pull requests from Gitea.
     * @param start The starting index (0-based) - will be converted to Gitea's 1-based page number.
     * @param limit The maximum number of items per page.
     * @return A page of pull requests.
     */
    override fun getOpenPullRequests(start: Int, limit: Int): PullRequestPage {
        // Gitea uses 1-based page numbering
        val page = (start / limit) + 1
        val url =
            "${giteaProperties.baseUrl}${apiBasePath}/repos/${giteaProperties.owner}/${giteaProperties.repository}/pulls?state=open&page=$page&limit=$limit"

        logger.debug("Fetching open pull requests from Gitea: {}", url)

        val request = Request.Builder()
            .url(url)
            .header(HttpHeaders.AUTHORIZATION, "token ${giteaProperties.token}")
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .get()
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                logger.error("Failed to get Gitea pull requests: {} - {}", response.code, errorBody)
                throw RuntimeException("Failed to get Gitea pull requests: ${response.code} - $errorBody")
            }

            val responseBody = response.body?.string()
            if (responseBody.isNullOrBlank()) {
                logger.warn("Received empty body for Gitea pull requests")
                // Assuming empty body means no more PRs on this or subsequent pages
                return PullRequestPage(listOf(), true, start, limit)
            }

            try {
                // Gitea returns a direct list of PRs in the response body
                val pullRequests: List<PullRequest> = objectMapper.readValue(responseBody, object : TypeReference<List<PullRequest>>() {})

                // Determine if it's the last page.
                // Simple check: if the number of returned items is less than the limit, it's likely the last page.
                // Gitea might also provide Link headers for pagination, but this is simpler for now.
                val isLastPage = pullRequests.size < limit

                logger.info("Fetched {} pull requests from Gitea. Page: {}, Limit: {}. isLastPage={}", pullRequests.size, page, limit, isLastPage)
                // The start parameter in PullRequestPage should reflect the original request's start index
                return PullRequestPage(pullRequests, isLastPage, start, limit)
            } catch (e: Exception) {
                logger.error("Failed to parse Gitea pull requests response: {}", e.message, e)
                throw RuntimeException("Failed to parse Gitea pull requests response: ${e.message}")
            }
        }
    }

    /**
     * Posts a comment to a specific pull request in Gitea.
     * @param prId The pull request index (treated as issue index by Gitea API).
     * @param comment The text of the comment.
     * @return True if the comment was posted successfully, false otherwise.
     */
    override fun postReviewComment(prId: Long, comment: String): Boolean {
        // Gitea uses issue endpoints for PR comments, prId corresponds to the issue index
        val url =
            "${giteaProperties.baseUrl}${apiBasePath}/repos/${giteaProperties.owner}/${giteaProperties.repository}/issues/${prId}/comments"

        logger.debug("Posting comment to Gitea PR/Issue #{}: {}", prId, url)

        val commentMap = mapOf("body" to comment) // Gitea expects "body" field for comment content
        val requestBodyJson = objectMapper.writeValueAsString(commentMap)
        val body = requestBodyJson.toRequestBody(MediaType.APPLICATION_JSON_VALUE.toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .header(HttpHeaders.AUTHORIZATION, "token ${giteaProperties.token}")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .post(body)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                logger.error(
                    "Failed to post Gitea comment for PR/Issue #{}: {} - {}",
                    prId,
                    response.code,
                    errorBody
                )
            } else {
                // Gitea returns 201 Created on success
                logger.info("Successfully posted comment to Gitea PR/Issue #{}", prId)
            }
            return response.isSuccessful // Check for 2xx status codes, specifically 201
        }
    }

    /**
     * Gets all comments for a specific pull request (via issue endpoint).
     * Gitea doesn't seem to have strong pagination guarantees on this endpoint in older versions,
     * so we fetch all comments at once. For very high comment counts, pagination might need revisiting.
     * @param prId The pull request index (treated as issue index by Gitea API)
     * @return List of comments as a JSON string
     */
    override fun getPullRequestComments(prId: Long): String {
        // Gitea uses issue endpoints for PR comments
        val url =
            "${giteaProperties.baseUrl}${apiBasePath}/repos/${giteaProperties.owner}/${giteaProperties.repository}/issues/${prId}/comments"

        logger.debug("Getting comments for Gitea PR/Issue #{}: {}", prId, url)

        val request = Request.Builder()
            .url(url)
            .header(HttpHeaders.AUTHORIZATION, "token ${giteaProperties.token}")
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .get()
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                logger.error(
                    "Failed to get Gitea comments for PR/Issue #{}: {} - {}",
                    prId,
                    response.code,
                    errorBody
                )
                throw RuntimeException("Failed to get Gitea pull request comments: ${response.code} - $errorBody")
            }

            val responseBody = response.body?.string() ?: "[]" // Return empty JSON array if body is null
            logger.debug("Received comments JSON for Gitea PR/Issue #{}: {}", prId, responseBody.substring(0, minOf(responseBody.length, 200)) + "...") // Log truncated body
            return responseBody
        }
    }

    /**
     * Searches for a specific pattern in comments of a pull request.
     * @param prId The pull request index (treated as issue index by Gitea API)
     * @param pattern The string pattern to search for
     * @return Map of comment IDs to comment text that match the pattern
     */
    override fun searchPatternInPullRequestComments(prId: Long, pattern: String): Map<Long, Comment> {
        logger.debug("Searching for pattern '{}' in comments of Gitea PR/Issue #{}", pattern, prId)
        val commentsJson = getPullRequestComments(prId)
        val commentsMap = mutableMapOf<Long, Comment>()

        try {
            // Gitea returns a direct list of Comment objects
            val comments = objectMapper.readValue(commentsJson, object : TypeReference<List<GiteaComment>>() {})

            logger.debug("Found {} comments total for Gitea PR/Issue #{}", comments.size, prId)

            comments.forEach { comment ->
                if (comment.body != null && comment.body.contains(pattern, ignoreCase = true)) { // Added ignoreCase for flexibility
                    commentsMap[comment.id] = Comment(comment.id, comment.body, comment.updatedAt)
                    logger.debug("Found matching comment ID {} in Gitea PR/Issue #{}", comment.id, prId)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to parse Gitea comments JSON for PR/Issue #{}: {}", prId, e.message, e)
            // Depending on requirements, you might want to return an empty map or throw
            throw RuntimeException("Failed to parse Gitea comments JSON: ${e.message}")
        }

        logger.info("Found {} comments matching pattern '{}' in Gitea PR/Issue #{}", commentsMap.size, pattern, prId)
        return commentsMap
    }}

/**
 * Helper data class to deserialize Gitea comment objects.
 * Only includes fields needed for searching. Add more if required.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class GiteaComment(
    val id: Long,
    val body: String?,
    // val user: GiteaUser?, // Example: Add user info if needed
    // val created_at: String?,
    @JsonProperty("updated_at")
    val updatedAt: OffsetDateTime
)

// Note: Ensure dev.gordeev.review.server.model.PullRequest aligns with Gitea's PullRequest structure
// or use Jackson annotations (@JsonProperty) if names differ significantly.
// Key fields expected based on Gitea API spec: id (int64), number (int64), title (string), html_url (string), state (string), user (User object), etc.