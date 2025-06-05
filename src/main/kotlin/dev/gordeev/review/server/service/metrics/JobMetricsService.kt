package dev.gordeev.review.server.service.metrics

import dev.gordeev.review.server.model.JobExecutionMetrics
import dev.gordeev.review.server.model.JobMetrics
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.quartz.*
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap

@Service
class JobMetricsService(
    @Lazy private val scheduler: Scheduler,
    private val meterRegistry: MeterRegistry
) {

    private val jobExecutionHistory = ConcurrentHashMap<String, MutableList<JobExecutionMetrics>>()
    private val jobCounters = ConcurrentHashMap<String, Counter>()
    private val jobTimers = ConcurrentHashMap<String, Timer>()

    init {
        // Initialize metrics for known jobs
        initializeJobMetrics()
    }

    private fun initializeJobMetrics() {
        val jobNames = listOf(
            "pullRequestFetchJob",
            "repositoryIndexerJob",
            "reviewProcessJob",
            "reviewResultProcessJob"
        )

        jobNames.forEach { jobName ->
            jobCounters["${jobName}_success"] = Counter.builder("job_executions_total")
                .tag("job", jobName)
                .tag("status", "success")
                .register(meterRegistry)

            jobCounters["${jobName}_failure"] = Counter.builder("job_executions_total")
                .tag("job", jobName)
                .tag("status", "failure")
                .register(meterRegistry)

            jobTimers[jobName] = Timer.builder("job_execution_duration")
                .tag("job", jobName)
                .register(meterRegistry)
        }
    }

    fun recordJobExecution(jobName: String, duration: Long, success: Boolean, errorMessage: String? = null) {
        val execution = JobExecutionMetrics(
            jobName = jobName,
            executionTime = LocalDateTime.now(),
            duration = duration,
            success = success,
            errorMessage = errorMessage
        )

        // Store in history (keep last 100 executions per job)
        jobExecutionHistory.computeIfAbsent(jobName) { mutableListOf() }.apply {
            add(execution)
            if (size > 100) removeAt(0)
        }

        // Update metrics
        if (success) {
            jobCounters["${jobName}_success"]?.increment()
        } else {
            jobCounters["${jobName}_failure"]?.increment()
        }

        jobTimers[jobName]?.record(duration, java.util.concurrent.TimeUnit.MILLISECONDS)
    }

    fun getAllJobMetrics(): List<JobMetrics> {
        return try {
            val jobKeys = scheduler.getJobKeys(GroupMatcher.anyGroup())

            jobKeys.map { jobKey ->
                val jobDetail = scheduler.getJobDetail(jobKey)
                val triggers = scheduler.getTriggersOfJob(jobKey)
                val trigger = triggers.firstOrNull()

                val executions = jobExecutionHistory[jobKey.name] ?: emptyList()
                val successCount = executions.count { it.success }
                val failureCount = executions.count { !it.success }
                val avgDuration = executions.takeIf { it.isNotEmpty() }?.map { it.duration }?.average()

                val isRunning = scheduler.getCurrentlyExecutingJobs().any {
                    it.jobDetail.key == jobKey
                }

                JobMetrics(
                    jobName = jobKey.name,
                    jobGroup = jobKey.group,
                    jobClass = jobDetail.jobClass.simpleName,
                    triggerName = trigger?.key?.name ?: "N/A",
                    triggerGroup = trigger?.key?.group ?: "N/A",
                    triggerState = trigger?.let { scheduler.getTriggerState(it.key).name } ?: "N/A",
                    nextFireTime = trigger?.nextFireTime?.toInstant()
                        ?.atZone(ZoneId.systemDefault())?.toLocalDateTime(),
                    previousFireTime = trigger?.previousFireTime?.toInstant()
                        ?.atZone(ZoneId.systemDefault())?.toLocalDateTime(),
                    startTime = trigger?.startTime?.toInstant()
                        ?.atZone(ZoneId.systemDefault())?.toLocalDateTime(),
                    endTime = trigger?.endTime?.toInstant()
                        ?.atZone(ZoneId.systemDefault())?.toLocalDateTime(),
                    executionCount = executions.size.toLong(),
                    lastExecutionDuration = executions.lastOrNull()?.duration,
                    averageExecutionDuration = avgDuration,
                    successCount = successCount.toLong(),
                    failureCount = failureCount.toLong(),
                    isRunning = isRunning,
                    description = jobDetail.description
                )
            }
        } catch (e: Exception) {
            // Return empty list if scheduler is not ready
            emptyList()
        }
    }

    fun getJobExecutionHistory(jobName: String): List<JobExecutionMetrics> {
        return jobExecutionHistory[jobName]?.toList() ?: emptyList()
    }

    fun pauseJob(jobName: String, jobGroup: String = "DEFAULT") {
        scheduler.pauseJob(JobKey.jobKey(jobName, jobGroup))
    }

    fun resumeJob(jobName: String, jobGroup: String = "DEFAULT") {
        scheduler.resumeJob(JobKey.jobKey(jobName, jobGroup))
    }

    fun triggerJob(jobName: String, jobGroup: String = "DEFAULT") {
        scheduler.triggerJob(JobKey.jobKey(jobName, jobGroup))
    }
}