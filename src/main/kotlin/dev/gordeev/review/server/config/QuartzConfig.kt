package dev.gordeev.review.server.config

import dev.gordeev.review.server.job.PullRequestFetchJob
import dev.gordeev.review.server.job.RepositoryIndexerJob
import dev.gordeev.review.server.job.ReviewProcessJob
import dev.gordeev.review.server.job.ReviewResultProcessJob
import org.quartz.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QuartzConfig(
    private val jobProperties: JobProperties
) {

    @Bean
    fun pullRequestFetchJobDetail(): JobDetail {
        return JobBuilder.newJob(PullRequestFetchJob::class.java)
            .withIdentity("pullRequestFetchJob")
            .withDescription("Fetches pull requests and queues them for review")
            .storeDurably()
            .build()
    }

    @Bean
    @ConditionalOnProperty(name = ["app.jobs.pull-request-fetch.enabled"], havingValue = "true", matchIfMissing = true)
    fun pullRequestFetchTrigger(pullRequestFetchJobDetail: JobDetail): Trigger {
        return TriggerBuilder.newTrigger()
            .forJob(pullRequestFetchJobDetail)
            .withIdentity("pullRequestFetchTrigger")
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInSeconds(jobProperties.pullRequestFetch.intervalInSeconds)
                    .repeatForever()
            )
            .build()
    }

    @Bean
    fun reviewProcessJobDetail(): JobDetail {
        return JobBuilder.newJob(ReviewProcessJob::class.java)
            .withIdentity("reviewProcessJob")
            .withDescription("Processes queued pull requests for review")
            .storeDurably()
            .build()
    }

    @Bean
    @ConditionalOnProperty(name = ["app.jobs.review-process.enabled"], havingValue = "true", matchIfMissing = true)
    fun reviewProcessTrigger(reviewProcessJobDetail: JobDetail): Trigger {
        return TriggerBuilder.newTrigger()
            .forJob(reviewProcessJobDetail)
            .withIdentity("reviewProcessTrigger")
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInSeconds(jobProperties.reviewProcess.intervalInSeconds)
                    .repeatForever()
            )
            .build()
    }

    @Bean
    fun reviewResultProcessJobDetail(): JobDetail {
        return JobBuilder.newJob(ReviewResultProcessJob::class.java)
            .withIdentity("reviewResultProcessJob")
            .withDescription("Processes completed review results and posts comments")
            .storeDurably()
            .build()
    }

    @Bean
    @ConditionalOnProperty(name = ["app.jobs.review-result-process.enabled"], havingValue = "true", matchIfMissing = true)
    fun reviewResultProcessTrigger(reviewResultProcessJobDetail: JobDetail): Trigger {
        return TriggerBuilder.newTrigger()
            .forJob(reviewResultProcessJobDetail)
            .withIdentity("reviewResultProcessTrigger")
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInSeconds(jobProperties.reviewResultProcess.intervalInSeconds)
                    .repeatForever()
            )
            .build()
    }

    @Bean
    fun repositoryIndexerJobDetail(): JobDetail {
        return JobBuilder.newJob(RepositoryIndexerJob::class.java)
            .withIdentity("repositoryIndexerJob")
            .withDescription("Indexes repositories for RAG processing")
            .storeDurably()
            .build()
    }

    @Bean
    @ConditionalOnProperty(name = ["app.jobs.repository-indexer.enabled"], havingValue = "true", matchIfMissing = true)
    fun repositoryIndexerTrigger(repositoryIndexerJobDetail: JobDetail): Trigger {
        return TriggerBuilder.newTrigger()
            .forJob(repositoryIndexerJobDetail)
            .withIdentity("repositoryIndexerTrigger")
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInSeconds(jobProperties.repositoryIndexer.intervalInSeconds)
                    .repeatForever()
            )
            .build()
    }
}