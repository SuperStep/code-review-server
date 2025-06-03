package dev.gordeev.review.server.service.review

import dev.gordeev.review.server.config.AiReviewProperties
import dev.gordeev.review.server.config.GiteaProperties
import dev.gordeev.review.server.model.PullRequest
import dev.gordeev.review.server.model.PullRequestToReview
import dev.gordeev.review.server.service.database.RagService
import dev.gordeev.review.server.service.git.GitService
import gordeev.dev.aicodereview.provider.AiReviewProvider
import org.apache.commons.text.StringSubstitutor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class ReviewService(
    val gitDiffService: GitService,
    val atReviewProvider: AiReviewProvider,
    val ragService: RagService,
    val aiReviewProperties: AiReviewProperties,
    val giteaProperties: GiteaProperties
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun review(pullRequestToReview: PullRequestToReview): String{

        val pullRequest = pullRequestToReview.pullRequest

        logger.info("Starting diff retrieval for PR #${pullRequest.id}")
        val startTime = System.nanoTime()

        val diff = gitDiffService.getDiffBetweenBranches(
            "${giteaProperties.baseUrl}/${pullRequest.base.repo.fullName}.git",
            pullRequest.base.ref,
            pullRequest.head.ref
        )

        val endTime = System.nanoTime()
        val durationSeconds = (endTime - startTime) / 1_000_000_000.0
        logger.info("Diff retrieval completed for PR #${pullRequest.id} in $durationSeconds seconds")

        var searchResults = emptyList<Map<String, String>>()
        try {
            logger.info("Starting semantic search for PR #${pullRequest.id}")
            searchResults = ragService.semanticSearch(pullRequest.base.repo.name, diff, 10)
        } catch (e: Exception) {
            // Do not throw exception, continue with review without context
            logger.error("Error performing semantic search for PR #${pullRequest.id}", e)
        }

        var prompt = buildPromptFromTemplate(
            aiReviewProperties.review.prompt.template,
            pullRequest,
            diff,
            pullRequestToReview,
            searchResults.map { it.values }.flatten()
        )

        logger.info("Starting AI review generation for PR #${pullRequest.id}")

        try {
            atReviewProvider.getReview(prompt).let {
                logger.info("AI review generated for PR #${pullRequest.id}")
                return it ?: "No review generated"
            }
        } catch (e: Exception) {
            logger.error("Error generating AI review for PR #${pullRequest.id}", e)
            throw e
        }
    }

    private fun buildPromptFromTemplate(
        template: String,
        pullRequest: PullRequest,
        diff: String,
        pullRequestToReview: PullRequestToReview,
        searchResults: List<String>
    ): String {
        val variables = mapOf(
            "title" to pullRequest.title,
            "diff" to diff,
            "reviewComment" to pullRequestToReview.reviewComment,
            "context" to searchResults.joinToString("\n\n")
        )

        val substitutor = StringSubstitutor(variables)
        return substitutor.replace(template)
    }
}