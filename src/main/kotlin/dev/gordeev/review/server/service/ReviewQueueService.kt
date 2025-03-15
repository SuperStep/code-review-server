package dev.gordeev.review.server.service

import dev.gordeev.review.server.model.PullRequest
import dev.gordeev.review.server.model.PullRequestToReview
import dev.gordeev.review.server.model.ReviewResult
import dev.gordeev.review.server.queue.InMemoryReviewQueue
import dev.gordeev.review.server.queue.ReviewQueue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ReviewQueueService(
    private val vcsService: VCSService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    private val pullRequestQueue: ReviewQueue<PullRequest> = InMemoryReviewQueue()
    private val reviewResultQueue: ReviewQueue<ReviewResult> = InMemoryReviewQueue()
    
    fun enqueuePullRequestForReview(pullRequest: PullRequest) {
        pullRequestQueue.enqueue(pullRequest)
        // Log current PR queue entries in beautiful format
        val queueEntries = pullRequestQueue.getAll()
        logger.info("Current Pull Request Queue (${queueEntries.size} items):")
        if (queueEntries.isEmpty()) {
            logger.info("  Queue is empty")
        } else {
            queueEntries.forEachIndexed { index, pr ->
                logger.info("  ${index + 1}. PR #${pr.id}: ${pr.title} by ${pr.author}")
                logger.info("     Branch: ${pr.fromRef.displayId} → ${pr.toRef.displayId}")
                logger.info("     Created: ${pr.createdDate}")
                logger.info("     Updated: ${pr.updatedDate}")
            }
        }
    }

    fun genNextToReview(): PullRequest? {
        return pullRequestQueue.dequeue()
    }

    fun enqueueReviewResult(reviewResult: ReviewResult) {
        reviewResultQueue.enqueue(reviewResult)
    }

    fun genNextReviewResult(): ReviewResult? {
        return reviewResultQueue.dequeue()
    }
}
