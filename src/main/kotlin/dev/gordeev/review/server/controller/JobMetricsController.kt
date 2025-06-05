package dev.gordeev.review.server.controller

import dev.gordeev.review.server.model.JobMetrics
import dev.gordeev.review.server.service.metrics.JobMetricsService
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/jobs")
class JobMetricsController(
    private val jobMetricsService: JobMetricsService
) {

    @GetMapping("/dashboard", produces = [MediaType.TEXT_HTML_VALUE])
    fun dashboard(): Mono<String> {
        val jobMetrics = jobMetricsService.getAllJobMetrics()

        val html = createHTML().html {
            head {
                title("Job Metrics Dashboard")
                meta(charset = "UTF-8")
                meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
                style {
                    unsafe {
                        raw("""
                            body { 
                                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                                margin: 0; padding: 20px; background-color: #f5f5f5;
                            }
                            .container { max-width: 1200px; margin: 0 auto; }
                            .header { background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                            .job-card { 
                                background: white; margin-bottom: 20px; border-radius: 8px; 
                                box-shadow: 0 2px 4px rgba(0,0,0,0.1); overflow: hidden;
                            }
                            .job-header { 
                                padding: 20px; background: #f8f9fa; border-bottom: 1px solid #dee2e6;
                                display: flex; justify-content: space-between; align-items: center;
                            }
                            .job-title { font-size: 1.25rem; font-weight: 600; margin: 0; }
                            .job-status { 
                                padding: 4px 12px; border-radius: 16px; font-size: 0.875rem; font-weight: 500;
                            }
                            .status-running { background: #d1ecf1; color: #0c5460; }
                            .status-normal { background: #d4edda; color: #155724; }
                            .status-paused { background: #f8d7da; color: #721c24; }
                            .job-content { padding: 20px; }
                            .metrics-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; }
                            .metric { text-align: center; }
                            .metric-value { font-size: 2rem; font-weight: 600; color: #007bff; }
                            .metric-label { font-size: 0.875rem; color: #6c757d; text-transform: uppercase; }
                            .actions { display: flex; gap: 10px; margin-top: 15px; }
                            .btn { 
                                padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; 
                                font-size: 0.875rem; text-decoration: none; display: inline-block;
                            }
                            .btn-primary { background: #007bff; color: white; }
                            .btn-success { background: #28a745; color: white; }
                            .btn-warning { background: #ffc107; color: #212529; }
                            .btn-info { background: #17a2b8; color: white; }
                            .refresh-btn { 
                                position: fixed; bottom: 20px; right: 20px; 
                                background: #007bff; color: white; border: none; 
                                border-radius: 50px; padding: 15px 20px; cursor: pointer;
                                box-shadow: 0 4px 12px rgba(0,123,255,0.3);
                            }
                            .job-details { margin-top: 15px; font-size: 0.875rem; color: #6c757d; }
                            .job-details dt { display: inline-block; font-weight: 600; width: 140px; }
                            .job-details dd { display: inline-block; margin: 0 0 5px 0; }
                        """)
                    }
                }
                script {
                    unsafe {
                        raw("""
                            function refreshPage() {
                                window.location.reload();
                            }
                            
                            function executeAction(action, jobName, jobGroup) {
                                fetch('/jobs/' + action, {
                                    method: 'POST',
                                    headers: { 'Content-Type': 'application/json' },
                                    body: JSON.stringify({ jobName: jobName, jobGroup: jobGroup })
                                }).then(() => {
                                    setTimeout(refreshPage, 1000);
                                });
                            }
                            
                            // Auto-refresh every 30 seconds
                            setInterval(refreshPage, 30000);
                        """)
                    }
                }
            }
            body {
                div(classes = "container") {
                    div(classes = "header") {
                        h1 { +"Code Review Server - Job Metrics Dashboard" }
                        p { +"Monitor and manage Quartz job execution metrics and status" }
                    }

                    jobMetrics.forEach { job ->
                        div(classes = "job-card") {
                            div(classes = "job-header") {
                                h2(classes = "job-title") { +job.jobClass }
                                span(classes = "job-status ${getStatusClass(job)}") {
                                    +getStatusText(job)
                                }
                            }

                            div(classes = "job-content") {
                                div(classes = "metrics-grid") {
                                    div(classes = "metric") {
                                        div(classes = "metric-value") { +"${job.executionCount}" }
                                        div(classes = "metric-label") { +"Total Executions" }
                                    }

                                    div(classes = "metric") {
                                        div(classes = "metric-value") { +"${job.successCount}" }
                                        div(classes = "metric-label") { +"Successful" }
                                    }

                                    div(classes = "metric") {
                                        div(classes = "metric-value") { +"${job.failureCount}" }
                                        div(classes = "metric-label") { +"Failed" }
                                    }

                                    div(classes = "metric") {
                                        div(classes = "metric-value") {
                                            +formatDuration(job.averageExecutionDuration)
                                        }
                                        div(classes = "metric-label") { +"Avg Duration" }
                                    }

                                    div(classes = "metric") {
                                        div(classes = "metric-value") {
                                            +formatDuration(job.lastExecutionDuration?.toDouble())
                                        }
                                        div(classes = "metric-label") { +"Last Duration" }
                                    }
                                }

                                dl(classes = "job-details") {
                                    dt { +"Job Name:" }
                                    dd { +job.jobName }

                                    dt { +"Trigger:" }
                                    dd { +"${job.triggerName} (${job.triggerState})" }

                                    dt { +"Next Fire:" }
                                    dd { +(job.nextFireTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) ?: "N/A") }

                                    dt { +"Previous Fire:" }
                                    dd { +(job.previousFireTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) ?: "N/A") }

                                    if (job.description != null) {
                                        dt { +"Description:" }
                                        dd { +job.description }
                                    }
                                }

                                div(classes = "actions") {
                                    if (job.triggerState == "PAUSED") {
                                        button(classes = "btn btn-success") {
                                            onClick = "executeAction('resume', '${job.jobName}', '${job.jobGroup}')"
                                            +"Resume Job"
                                        }
                                    } else {
                                        button(classes = "btn btn-warning") {
                                            onClick = "executeAction('pause', '${job.jobName}', '${job.jobGroup}')"
                                            +"Pause Job"
                                        }
                                    }

                                    button(classes = "btn btn-primary") {
                                        onClick = "executeAction('trigger', '${job.jobName}', '${job.jobGroup}')"
                                        +"Trigger Now"
                                    }

                                    a(href = "/jobs/${job.jobName}/history", classes = "btn btn-info") {
                                        +"View History"
                                    }
                                }
                            }
                        }
                    }
                }

                button(classes = "refresh-btn") {
                    onClick = "refreshPage()"
                    +"⟳ Refresh"
                }
            }
        }

        return Mono.just(html)
    }

    @GetMapping("/{jobName}/history", produces = [MediaType.TEXT_HTML_VALUE])
    fun jobHistory(@PathVariable jobName
                   : String): Mono<String> {
    val executionHistory = jobMetricsService.getJobExecutionHistory(jobName)

    val html = createHTML().html {
        head {
            title("Job History - $jobName")
            meta(charset = "UTF-8")
            meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
            style {
                unsafe {
                    raw("""
                            body { 
                                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                                margin: 0; padding: 20px; background-color: #f5f5f5;
                            }
                            .container { max-width: 1000px; margin: 0 auto; }
                            .header { 
                                background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; 
                                box-shadow: 0 2px 4px rgba(0,0,0,0.1); display: flex; justify-content: space-between; align-items: center;
                            }
                            .back-btn { 
                                background: #6c757d; color: white; padding: 8px 16px; 
                                border-radius: 4px; text-decoration: none; font-size: 0.875rem;
                            }
                            .history-table { 
                                background: white; border-radius: 8px; overflow: hidden;
                                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                            }
                            table { width: 100%; border-collapse: collapse; }
                            th { 
                                background: #f8f9fa; padding: 12px; text-align: left; 
                                font-weight: 600; border-bottom: 1px solid #dee2e6;
                            }
                            td { padding: 12px; border-bottom: 1px solid #f8f9fa; }
                            .status-success { color: #28a745; font-weight: 600; }
                            .status-failure { color: #dc3545; font-weight: 600; }
                            .duration { font-family: monospace; }
                            .error-message { 
                                background: #f8d7da; color: #721c24; padding: 8px; 
                                border-radius: 4px; font-size: 0.875rem; margin-top: 5px;
                            }
                            .empty-state { 
                                text-align: center; padding: 40px; color: #6c757d;
                                background: white; border-radius: 8px;
                            }
                        """)
                }
            }
        }
        body {
            div(classes = "container") {
                div(classes = "header") {
                    div {
                        h1 { +"Job Execution History" }
                        p { +"Job: $jobName" }
                    }
                    a(href = "/jobs/dashboard", classes = "back-btn") {
                        +"← Back to Dashboard"
                    }
                }

                if (executionHistory.isEmpty()) {
                    div(classes = "empty-state") {
                        h3 { +"No execution history available" }
                        p { +"This job hasn't been executed yet or history data is not available." }
                    }
                } else {
                    div(classes = "history-table") {
                        table {
                            thead {
                                tr {
                                    th { +"Execution Time" }
                                    th { +"Duration" }
                                    th { +"Status" }
                                    th { +"Details" }
                                }
                            }
                            tbody {
                                executionHistory.sortedByDescending { it.executionTime }.forEach { execution ->
                                    tr {
                                        td { +execution.executionTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) }
                                        td(classes = "duration") { +formatDuration(execution.duration.toDouble()) }
                                        td {
                                            span(classes = if (execution.success) "status-success" else "status-failure") {
                                                +(if (execution.success) "SUCCESS" else "FAILURE")
                                            }
                                        }
                                        td {
                                            if (!execution.success && execution.errorMessage != null) {
                                                div(classes = "error-message") {
                                                    +execution.errorMessage
                                                }
                                            } else {
                                                +"—"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    return Mono.just(html)
}

@PostMapping("/pause")
fun pauseJob(@RequestBody request: JobActionRequest): Mono<ServerResponse> {
    return try {
        jobMetricsService.pauseJob(request.jobName, request.jobGroup)
        ServerResponse.ok().build()
    } catch (e: Exception) {
        ServerResponse.badRequest().build()
    }
}

@PostMapping("/resume")
fun resumeJob(@RequestBody request: JobActionRequest): Mono<ServerResponse> {
    return try {
        jobMetricsService.resumeJob(request.jobName, request.jobGroup)
        ServerResponse.ok().build()
    } catch (e: Exception) {
        ServerResponse.badRequest().build()
    }
}

@PostMapping("/trigger")
fun triggerJob(@RequestBody request: JobActionRequest): Mono<ServerResponse> {
    return try {
        jobMetricsService.triggerJob(request.jobName, request.jobGroup)
        ServerResponse.ok().build()
    } catch (e: Exception) {
        ServerResponse.badRequest().build()
    }
}

@GetMapping("/api/metrics")
fun getMetricsApi(): Mono<List<JobMetrics>> {
    return Mono.just(jobMetricsService.getAllJobMetrics())
}

private fun getStatusClass(job: dev.gordeev.review.server.model.JobMetrics): String {
    return when {
        job.isRunning -> "status-running"
        job.triggerState == "PAUSED" -> "status-paused"
        else -> "status-normal"
    }
}

private fun getStatusText(job: dev.gordeev.review.server.model.JobMetrics): String {
    return when {
        job.isRunning -> "RUNNING"
        job.triggerState == "PAUSED" -> "PAUSED"
        job.triggerState == "NORMAL" -> "ACTIVE"
        else -> job.triggerState
    }
}

private fun formatDuration(durationMs: Double?): String {
    if (durationMs == null) return "N/A"

    val seconds = durationMs / 1000
    return when {
        seconds < 1 -> String.format("%.0f ms", durationMs)
        seconds < 60 -> String.format("%.1f s", seconds)
        seconds < 3600 -> String.format("%.1f m", seconds / 60)
        else -> String.format("%.1f h", seconds / 3600)
    }
}

data class JobActionRequest(
    val jobName: String,
    val jobGroup: String = "DEFAULT"
)
}
