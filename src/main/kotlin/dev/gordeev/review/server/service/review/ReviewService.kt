package dev.gordeev.review.server.service.review

import dev.gordeev.review.server.config.AiReviewProperties
import dev.gordeev.review.server.config.GiteaProperties
import dev.gordeev.review.server.model.PullRequestToReview
import dev.gordeev.review.server.service.git.GitDiffService
import gordeev.dev.aicodereview.provider.AiReviewProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class ReviewService(
    val gitDiffService: GitDiffService,
    val atReviewProvider: AiReviewProvider,
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


        logger.info("Starting AI review generation for PR #${pullRequest.id}")
        val prompt = aiReviewProperties.review.prompt.start +
                pullRequest.title +
                aiReviewProperties.review.prompt.diff +
                diff +
                aiReviewProperties.review.prompt.additional +
                pullRequestToReview.reviewComment

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
}