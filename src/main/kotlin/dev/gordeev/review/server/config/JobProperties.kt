package dev.gordeev.review.server.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jobs")
data class JobProperties(
    val pullRequestFetch: prFetchJobConfig = prFetchJobConfig(),
    val reviewProcess: JobConfig = JobConfig(),
    val reviewResultProcess: JobConfig = JobConfig()
)

data class JobConfig(
    val enabled: Boolean = true,
    val intervalInSeconds: Int = 1
)

data class prFetchJobConfig(
    val enabled: Boolean = true,
    val intervalInSeconds: Int = 1,
    val botName: String = "@bot",
)