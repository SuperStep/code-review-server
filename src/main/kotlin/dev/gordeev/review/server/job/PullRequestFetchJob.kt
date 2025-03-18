package dev.gordeev.review.server.job

import dev.gordeev.review.server.config.JobProperties
import dev.gordeev.review.server.model.PullRequestToReview
import dev.gordeev.review.server.service.queue.ReviewQueueService
import dev.gordeev.review.server.service.vcs.VCSService
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

@Component
class PullRequestFetchJob(
    private val reviewQueueService: ReviewQueueService,
    private val vcsService: VCSService,
    private val jobProperties: JobProperties
) : Job {

    // Current page for pull request fetching
    private var currentPage = 0
    private val pageSize = 10
    // Pattern to search for in comments
    private val commentPattern = jobProperties.pullRequestFetch.botName

    override fun execute(context: JobExecutionContext) {
        logger.info("Fetching pull requests page $currentPage")

        try {
            val pullRequestsPage = vcsService.getOpenPullRequests(currentPage * pageSize, pageSize)

            if (pullRequestsPage.values.isNotEmpty()) {
                logger.info("Fetched ${pullRequestsPage.size} pull requests, checking for review requests")

                for (pullRequest in pullRequestsPage.values) {
                    // Search for the pattern in PR comments
                    val matchingComments = vcsService.searchPatternInPullRequestComments(pullRequest.id, commentPattern)

                    if (matchingComments.isNotEmpty()) {
                        logger.info("PR #${pullRequest.id} has comments matching pattern '$commentPattern', adding to review queue")
                        reviewQueueService.enqueuePullRequestForReview(PullRequestToReview(pullRequest, matchingComments.values.joinToString { it }))
                    } else {
                        logger.debug("PR #${pullRequest.id} has no comments matching pattern '$commentPattern', skipping")
                    }
                }

                // Move to next page if we haven't reached the end
                if (!pullRequestsPage.isLastPage) {
                    currentPage++
                } else {
                    currentPage = 0
                    logger.info("Reached last page, resetting page counter")
                }
            } else {
                logger.info("No more pull requests to fetch")
                currentPage = 0
            }

            // Reset page counter after 10 pages to avoid going too far
            if (currentPage >= 10) {
                currentPage = 0
                logger.info("Resetting page counter after reaching max pages")
            }
        } catch (e: Exception) {
            logger.error("Error processing pull requests", e)
            currentPage = 0 // Reset on error
        }

        logger.info("Finished processing pull requests")
    }

    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(PullRequestFetchJob::class.java)
    }
}
