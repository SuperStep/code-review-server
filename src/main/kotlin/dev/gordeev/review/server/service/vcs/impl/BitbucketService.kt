package dev.gordeev.review.server.service.vcs.impl

import com.fasterxml.jackson.databind.ObjectMapper
import dev.gordeev.review.server.config.BitbucketProperties
import dev.gordeev.review.server.model.PullRequestPage
import dev.gordeev.review.server.service.vcs.VCSService
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.stereotype.Service

@Service
class BitbucketService(
    private val bitbucketProperties: BitbucketProperties,
    private val okHttpClient: OkHttpClient,
    private val objectMapper: ObjectMapper
) : VCSService {
    override fun getOpenPullRequests(start: Int, limit: Int): PullRequestPage {
        val url = "${bitbucketProperties.baseUrl}/projects/${bitbucketProperties.project}/repos/${bitbucketProperties.repository}/pull-requests?state=OPEN&start=$start&limit=$limit"

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer ${bitbucketProperties.token}")
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .get()
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Failed to get pull requests: ${response.code}")
            }

            val responseBody = response.body?.string() ?: return PullRequestPage(listOf(), true, start, limit)
            return objectMapper.readValue(responseBody, PullRequestPage::class.java)
        }
    }

    override fun getPullRequestDiff(prId: Long): String {
        val url = "${bitbucketProperties.baseUrl}/projects/${bitbucketProperties.project}/repos/${bitbucketProperties.repository}/pull-requests/${prId}/diff"

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer ${bitbucketProperties.token}")
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .get()
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Failed to get pull request diff: ${response.code}")
            }

            return response.body?.string() ?: ""
        }
    }

    override fun postReviewComment(prId: Long, comment: String): Boolean {
        val url = "${bitbucketProperties.baseUrl}/projects/${bitbucketProperties.project}/repos/${bitbucketProperties.repository}/pull-requests/${prId}/comments"

        val commentMap = mapOf("text" to comment)
        val requestBody = objectMapper.writeValueAsString(commentMap)

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer ${bitbucketProperties.token}")
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .post(requestBody.toRequestBody())
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            return response.isSuccessful
        }
    }
}