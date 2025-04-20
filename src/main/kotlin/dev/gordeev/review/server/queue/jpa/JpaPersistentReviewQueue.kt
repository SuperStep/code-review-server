package dev.gordeev.review.server.queue.jpa

import dev.gordeev.review.server.model.PullRequestToReview
import dev.gordeev.review.server.queue.ReviewQueue
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class JpaPersistentReviewQueue<T>(
    private val repository: QueueItemRepository,
    private val serializer: QueueItemSerializer<T>
) : ReviewQueue<T> {

    override fun enqueue(item: T) {
        val itemId = getItemId(item)
        if (!repository.existsByItemId(itemId)) {
            val serialized = serializer.serialize(item)
            repository.save(QueueItem(itemId = itemId, serializedItem = serialized))
        }
    }

    override fun enqueueAll(items: Collection<T>) {
        items.forEach { enqueue(it) }
    }

    override fun dequeue(): T? {
        val item = repository.findFirstByProcessingFalseOrderByCreatedAt() ?: return null
        item.processing = true
        repository.save(item)
        val result = serializer.deserialize(item.serializedItem)
        repository.delete(item)
        return result
    }

    override fun getAll(): List<T> {
        return repository.findAll()
            .map { serializer.deserialize(it.serializedItem) }
    }

    override fun peek(): T? {
        return repository.findFirstByProcessingFalseOrderByCreatedAt()?.let {
            serializer.deserialize(it.serializedItem)
        }
    }

    override fun isEmpty(): Boolean {
        return repository.count() == 0L
    }

    override fun size(): Int {
        return repository.count().toInt()
    }

    override fun findById(id: Any): T? {
        return repository.findByItemId(id.toString())?.let {
            serializer.deserialize(it.serializedItem)
        }
    }

    override fun exists(id: Any): Boolean {
        return repository.existsByItemId(id.toString())
    }

    private fun getItemId(item: T): String {
        // This needs to be implemented based on your specific T type
        // For PullRequestToReview, you might use the PR ID
        return when (item) {
            is PullRequestToReview -> item.pullRequest.id.toString()
            else -> item.hashCode().toString()
        }
    }
}