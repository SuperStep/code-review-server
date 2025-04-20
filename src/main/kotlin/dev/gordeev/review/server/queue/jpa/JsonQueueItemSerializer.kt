package dev.gordeev.review.server.queue.jpa

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

// Example implementation using Jackson for JSON serialization
@Service
class JsonQueueItemSerializer<T>(
    private val objectMapper: ObjectMapper,
    private val typeReference: TypeReference<T>
) : QueueItemSerializer<T> {
    
    override fun serialize(item: T): String {
        return objectMapper.writeValueAsString(item)
    }
    
    override fun deserialize(serialized: String): T {
        return objectMapper.readValue(serialized, typeReference)
    }
}