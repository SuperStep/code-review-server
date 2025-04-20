package dev.gordeev.review.server.queue.jpa

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "review_queue_items")
class QueueItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "item_id", nullable = false)
    val itemId: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "item_data", columnDefinition = "TEXT", nullable = false)
    val serializedItem: String,

    @Column(name = "processing", nullable = false)
    var processing: Boolean = false
)