package dev.gordeev.review.server.service.queue

import dev.gordeev.review.server.model.PullRequestToReview
import dev.gordeev.review.server.model.ReviewResult
import dev.gordeev.review.server.queue.InMemoryReviewQueue
import dev.gordeev.review.server.queue.ReviewQueue
import dev.gordeev.review.server.service.vcs.VCSService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ReviewQueueService(
    private val vcsService: VCSService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    private val pullRequestQueue: ReviewQueue<PullRequestToReview> = InMemoryReviewQueue { it.pullRequest.id }
    private val reviewResultQueue: ReviewQueue<ReviewResult> = InMemoryReviewQueue { it.pullRequest.id }

    fun enqueuePullRequestForReview(pullRequestToReview: PullRequestToReview) {
        // Check if the pull request already exists in the queue
        if (pullRequestQueue.exists(pullRequestToReview)) {
            logger.info("Pull request #${pullRequestToReview.pullRequest.id} already exists in the queue. Skipping...")
            return
        }
        // Enqueue the pull request if it doesn't exist
        pullRequestQueue.enqueue(pullRequestToReview)

        // Log current PR queue entries in beautiful format
        val queueEntries = pullRequestQueue.getAll()
        logger.info("Current Pull Request Queue (${queueEntries.size} items):")
        if (queueEntries.isEmpty()) {
            logger.info("  Queue is empty")
        } else {
            queueEntries.map { it.pullRequest }.forEachIndexed { index, pr ->
                logger.info("  ${index + 1}. PR #${pr.id}: ${pr.title} by ${pr.author}")
                logger.info("     Branch: ${pr.fromRef.displayId} → ${pr.toRef.displayId}")
                logger.info("     Created: ${pr.createdDate}")
                logger.info("     Updated: ${pr.updatedDate}")
            }
        }
    }

    fun genNextToReview(): PullRequestToReview? {
        return pullRequestQueue.dequeue()
    }

    fun enqueueReviewResult(reviewResult: ReviewResult) {
        reviewResultQueue.enqueue(reviewResult)
    }

    fun genNextReviewResult(): ReviewResult? {
        return reviewResultQueue.dequeue()
    }
}
