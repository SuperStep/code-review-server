package dev.gordeev.review.server.job

import dev.gordeev.review.server.service.queue.ReviewQueueService
import dev.gordeev.review.server.service.vcs.VCSService
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

@Component
class PullRequestFetchJob(
    private val reviewQueueService: ReviewQueueService,
    private val vcsService: VCSService
) : Job {

    // Current page for pull request fetching
    private var currentPage = 0
    private val pageSize = 10

    override fun execute(context: JobExecutionContext) {
        logger.info("Fetching pull requests page $currentPage")

        fetchPullRequestsPage(currentPage * pageSize)
        currentPage++

        // TODO Reset page counter after 10 pages to avoid going too far
        if (currentPage >= 10) {
            currentPage = 0
            logger.info("Resetting page counter")
        }
        logger.info("Finished fetching pull requests page $currentPage")
    }

    fun fetchPullRequestsPage(pageStart: Int = 0) {
        try {
            val pullRequestsPage = vcsService.getOpenPullRequests(pageStart, pageSize)
            if (pullRequestsPage.values.isNotEmpty()) {
                logger.info("Fetched ${pullRequestsPage.size} pull requests, adding to queue")
                for (pullRequest in pullRequestsPage.values) {
                    reviewQueueService.enqueuePullRequestForReview(pullRequest)
                    logger.info("Added PR #${pullRequest.id} to queue")
                }
            } else {
                logger.info("No more pull requests to fetch")
            }
        } catch (e: Exception) {
            logger.error("Error fetching pull requests", e)
        }
    }

    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(PullRequestFetchJob::class.java)
    }
}