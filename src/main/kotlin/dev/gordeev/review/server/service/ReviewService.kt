package dev.gordeev.review.server.service

import dev.gordeev.review.server.model.PullRequest
import dev.gordeev.review.server.service.git.GitDiffService
import gordeev.dev.aicodereview.provider.AiReviewProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class ReviewService(
    val gitDiffService: GitDiffService,
    val atReviewProvider: AiReviewProvider
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun review(pullRequest: PullRequest): String{

        logger.info("Starting diff retrieval for PR #${pullRequest.id}")
        val startTime = System.nanoTime()

        val diff = gitDiffService.getDiffBetweenBranches(
            "http://localhost:7990/scm/swtr/swtr-core.git",
            pullRequest.fromRef.displayId,
            pullRequest.toRef.displayId
        )

        val endTime = System.nanoTime()
        val durationSeconds = (endTime - startTime) / 1_000_000_000.0
        logger.info("Diff retrieval completed for PR #${pullRequest.id} in $durationSeconds seconds")

        val prompt = """
            You are a code reviewer. Review the following code diff and provide constructive feedback:
            
            Pull Request: ${pullRequest.title}
            
            Diff:
            ```
            ${diff}
            ```
            
            Please provide:
            1. Overall assessment
            2. Key issues or concerns
            3. Style and best practice suggestions
            4. Security considerations if applicable
        """.trimIndent()

        try {
            atReviewProvider.getReview(prompt).let {
                return it.toString()
            }
        } catch (e: Exception) {
            logger.error("Error generating AI review for PR #${pullRequest.id}", e)
            throw e
        }
    }
}