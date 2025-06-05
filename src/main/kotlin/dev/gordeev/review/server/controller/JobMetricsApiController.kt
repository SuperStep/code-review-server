package dev.gordeev.review.server.controller

import dev.gordeev.review.server.service.metrics.JobMetricsService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = ["*"])
class JobMetricsApiController(
    private val jobMetricsService: JobMetricsService
) {

    @GetMapping("/metrics")
    fun getAllJobMetrics(): Mono<List<dev.gordeev.review.server.model.JobMetrics>> {
        return Mono.just(jobMetricsService.getAllJobMetrics())
    }

    @GetMapping("/metrics/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getJobMetricsStream(): Flux<List<dev.gordeev.review.server.model.JobMetrics>> {
        return Flux.interval(Duration.ofSeconds(5))
            .map { jobMetricsService.getAllJobMetrics() }
    }

    @GetMapping("/{jobName}/history")
    fun getJobHistory(@PathVariable jobName: String): Mono<List<dev.gordeev.review.server.model.JobExecutionMetrics>> {
        return Mono.just(jobMetricsService.getJobExecutionHistory(jobName))
    }

    @PostMapping("/{jobName}/pause")
    fun pauseJob(@PathVariable jobName: String, @RequestParam(defaultValue = "DEFAULT") jobGroup: String): Mono<Map<String, String>> {
        return try {
            jobMetricsService.pauseJob(jobName, jobGroup)
            Mono.just(mapOf("status" to "success", "message" to "Job paused successfully"))
        } catch (e: Exception) {
            Mono.just(mapOf("status" to "error", "message" to e.message.orEmpty()))
        }
    }

    @PostMapping("/{jobName}/resume")
    fun resumeJob(@PathVariable jobName: String, @RequestParam(defaultValue = "DEFAULT") jobGroup: String): Mono<Map<String, String>> {
        return try {
            jobMetricsService.resumeJob(jobName, jobGroup)
            Mono.just(mapOf("status" to "success", "message" to "Job resumed successfully"))
        } catch (e: Exception) {
            Mono.just(mapOf("status" to "error", "message" to e.message.orEmpty()))
        }
    }

    @PostMapping("/{jobName}/trigger")
    fun triggerJob(@PathVariable jobName: String, @RequestParam(defaultValue = "DEFAULT") jobGroup: String): Mono<Map<String, String>> {
        return try {
            jobMetricsService.triggerJob(jobName, jobGroup)
            Mono.just(mapOf("status" to "success", "message" to "Job triggered successfully"))
        } catch (e: Exception) {
            Mono.just(mapOf("status" to "error", "message" to e.message.orEmpty()))
        }
    }
}