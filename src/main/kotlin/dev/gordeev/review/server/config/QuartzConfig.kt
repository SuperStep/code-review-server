package dev.gordeev.review.server.config

import dev.gordeev.review.server.job.PullRequestFetchJob
import dev.gordeev.review.server.job.ReviewProcessJob
import dev.gordeev.review.server.job.ReviewResultProcessJob
import org.quartz.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QuartzConfig {

    @Bean
    fun pullRequestFetchJobDetail(): JobDetail {
        return JobBuilder.newJob(PullRequestFetchJob::class.java)
            .withIdentity("pullRequestFetchJob")
            .storeDurably()
            .build()
    }

    @Bean
    fun pullRequestFetchTrigger(pullRequestFetchJobDetail: JobDetail): Trigger {
        return TriggerBuilder.newTrigger()
            .forJob(pullRequestFetchJobDetail)
            .withIdentity("pullRequestFetchTrigger")
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInSeconds(1)
                    .repeatForever()
            )
            .build()
    }

    @Bean
    fun reviewProcessJobDetail(): JobDetail {
        return JobBuilder.newJob(ReviewProcessJob::class.java)
            .withIdentity("reviewProcessJob")
            .storeDurably()
            .build()
    }

    @Bean
    fun reviewProcessTrigger(reviewProcessJobDetail: JobDetail): Trigger {
        return TriggerBuilder.newTrigger()
            .forJob(reviewProcessJobDetail)
            .withIdentity("reviewProcessTrigger")
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInSeconds(1)
                    .repeatForever()
            )
            .build()
    }

    @Bean
    fun reviewResultProcessJobDetail(): JobDetail {
        return JobBuilder.newJob(ReviewResultProcessJob::class.java)
            .withIdentity("reviewResultProcessJob")
            .storeDurably()
            .build()
    }

    @Bean
    fun reviewResultProcessTrigger(reviewResultProcessJobDetail: JobDetail): Trigger {
        return TriggerBuilder.newTrigger()
            .forJob(reviewResultProcessJobDetail)
            .withIdentity("reviewResultProcessTrigger")
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInSeconds(1)
                    .repeatForever()
            )
            .build()
    }
}
