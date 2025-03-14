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
        logger.info("Added PR #${pullRequest.id} to queue")
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
