package dev.gordeev.review.server.config

import dev.gordeev.review.server.service.metrics.JobMetricsService
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobListener
import org.springframework.stereotype.Component

@Component
class JobMetricsListener(
    private val jobMetricsService: JobMetricsService
) : JobListener {

    private val jobStartTimes = mutableMapOf<String, Long>()

    override fun getName(): String = "JobMetricsListener"

    override fun jobToBeExecuted(context: JobExecutionContext) {
        val jobKey = context.jobDetail.key.toString()
        jobStartTimes[jobKey] = System.currentTimeMillis()
    }

    override fun jobExecutionVetoed(context: JobExecutionContext) {
        val jobKey = context.jobDetail.key.toString()
        jobStartTimes.remove(jobKey)
    }

    override fun jobWasExecuted(context: JobExecutionContext, jobException: JobExecutionException?) {
        val jobKey = context.jobDetail.key.toString()
        val jobName = context.jobDetail.key.name
        val startTime = jobStartTimes.remove(jobKey) ?: System.currentTimeMillis()
        val duration = System.currentTimeMillis() - startTime

        val success = jobException == null
        val errorMessage = jobException?.message

        jobMetricsService.recordJobExecution(jobName, duration, success, errorMessage)
    }
}