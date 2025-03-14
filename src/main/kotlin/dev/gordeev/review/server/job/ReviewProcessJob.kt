package dev.gordeev.review.server.job

import dev.gordeev.review.server.model.ReviewResult
import dev.gordeev.review.server.model.ReviewStatus
import dev.gordeev.review.server.service.ReviewQueueService
import dev.gordeev.review.server.service.ReviewService
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

@Component
class ReviewProcessJob(
    private val reviewQueueService: ReviewQueueService,
    private val reviewService: ReviewService,
) : Job {
    override fun execute(context: JobExecutionContext) {
        while (true) {
            reviewQueueService.genNextToReview()?.let { pullRequest ->
                logger.info("Processing PR #${pullRequest.id}")
                val reviewResult = reviewService.review(pullRequest)
                reviewQueueService.enqueueReviewResult(
                    ReviewResult(
                        pullRequest,
                        reviewResult,
                        ReviewStatus.COMPLETED
                    )
                )
                logger.info("Finished processing PR #${pullRequest.id}")
            }
        }
    }

    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(ReviewProcessJob::class.java)
    }
}
