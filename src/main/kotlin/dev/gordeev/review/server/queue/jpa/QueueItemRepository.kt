package dev.gordeev.review.server.queue.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface QueueItemRepository : JpaRepository<QueueItem, Long> {
    fun findByItemId(itemId: String): QueueItem?
    fun existsByItemId(itemId: String): Boolean
    fun findFirstByProcessingFalseOrderByCreatedAt(): QueueItem?
}
