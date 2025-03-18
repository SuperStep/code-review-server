package dev.gordeev.review.server.job

import dev.gordeev.review.server.service.queue.ReviewQueueService
import dev.gordeev.review.server.service.vcs.VCSService
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

@Component
class ReviewResultProcessJob(
    private val reviewQueueService: ReviewQueueService,
    private val vcsService: VCSService,
) : Job {
    override fun execute(context: JobExecutionContext) {
        reviewQueueService.genNextReviewResult()?.let { reviewResult ->
            logger.info("Processing review result for PR #${reviewResult.pullRequest.id}")
            vcsService.postReviewComment(reviewResult.pullRequest.id, reviewResult.reviewContent)
        }
    }

    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(ReviewResultProcessJob::class.java)
    }
}
