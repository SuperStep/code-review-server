package dev.gordeev.review.server.model

import java.time.LocalDateTime

data class JobMetrics(
    val jobName: String,
    val jobGroup: String,
    val jobClass: String,
    val triggerName: String,
    val triggerGroup: String,
    val triggerState: String,
    val nextFireTime: LocalDateTime?,
    val previousFireTime: LocalDateTime?,
    val startTime: LocalDateTime?,
    val endTime: LocalDateTime?,
    val executionCount: Long,
    val lastExecutionDuration: Long?,
    val averageExecutionDuration: Double?,
    val successCount: Long,
    val failureCount: Long,
    val isRunning: Boolean,
    val description: String?
)

data class JobExecutionMetrics(
    val jobName: String,
    val executionTime: LocalDateTime,
    val duration: Long,
    val success: Boolean,
    val errorMessage: String?
)