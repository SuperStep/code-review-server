package dev.gordeev.review.server.service.queue

import dev.gordeev.review.server.model.PullRequestToReview
import dev.gordeev.review.server.model.ReviewResult
import dev.gordeev.review.server.queue.InMemoryReviewQueue
import dev.gordeev.review.server.queue.ReviewQueue
import dev.gordeev.review.server.service.vcs.VCSService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class ReviewQueueService {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    private val pullRequestQueue: ReviewQueue<PullRequestToReview> = InMemoryReviewQueue { it.pullRequest.id }
    private val reviewResultQueue: ReviewQueue<ReviewResult> = InMemoryReviewQueue { it.pullRequest.id }

    // Track reviewed PRs with their last updated date and review comment
    private val reviewedPRs = ConcurrentHashMap<Long, Pair<Long, String>>()

    fun enqueuePullRequestForReview(pullRequestToReview: PullRequestToReview) {
        val prId = pullRequestToReview.pullRequest.id
        val prUpdateDate = pullRequestToReview.pullRequest.updatedDate
        val reviewComment = pullRequestToReview.reviewComment ?: ""

        // Check if PR was already reviewed
        if (reviewedPRs.containsKey(prId)) {
            val (lastReviewedDate, lastReviewComment) = reviewedPRs[prId]!!

            // Skip if PR hasn't been updated and review comment is the same
            if (prUpdateDate <= lastReviewedDate && reviewComment == lastReviewComment) {
                logger.info("Pull request #$prId already reviewed and hasn't changed. Skipping...")
                return
            }

            logger.info("Pull request #$prId has been updated or review comment changed. Re-queuing for review.")
        }

        // Check if the pull request already exists in the queue
        if (pullRequestQueue.exists(pullRequestToReview)) {
            logger.info("Pull request #$prId already exists in the queue. Skipping...")
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
        // Store the PR as reviewed with its current update date and review comment
        val prId = reviewResult.pullRequest.id
        val prUpdateDate = reviewResult.pullRequest.updatedDate
        val reviewComment = reviewResult.originalReviewComment ?: ""

        // Update the reviewed PR tracking
        reviewedPRs[prId] = Pair(prUpdateDate, reviewComment)

        // Enqueue the review result
        reviewResultQueue.enqueue(reviewResult)
    }

    fun genNextReviewResult(): ReviewResult? {
        return reviewResultQueue.dequeue()
    }

    // For testing or maintenance purposes
    fun clearReviewedPRsHistory() {
        reviewedPRs.clear()
    }
}