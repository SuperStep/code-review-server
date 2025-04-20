package dev.gordeev.review.server.queue.jpa

// Interface for serializing/deserializing queue items
interface QueueItemSerializer<T> {
    fun serialize(item: T): String
    fun deserialize(serialized: String): T
}
