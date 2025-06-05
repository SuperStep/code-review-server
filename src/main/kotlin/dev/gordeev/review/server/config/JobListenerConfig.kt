package dev.gordeev.review.server.config

import org.quartz.Scheduler
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class JobListenerConfig(
    private val scheduler: Scheduler,
    private val jobMetricsListener: JobMetricsListener
) {

    @EventListener(ApplicationReadyEvent::class)
    fun registerJobListener() {
        scheduler.listenerManager.addJobListener(jobMetricsListener)
    }
}