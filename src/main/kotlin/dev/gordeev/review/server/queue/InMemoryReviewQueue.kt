package dev.gordeev.review.server.queue

import java.util.concurrent.ConcurrentLinkedQueue

class InMemoryReviewQueue<T> : ReviewQueue<T> {
    private val queue = ConcurrentLinkedQueue<T>()
    
    override fun enqueue(item: T) {
        queue.add(item)
    }
    
    override fun enqueueAll(items: Collection<T>) {
        queue.addAll(items)
    }
    
    override fun dequeue(): T? {
        return queue.poll()
    }
    
    override fun peek(): T? {
        return queue.peek()
    }
    
    override fun isEmpty(): Boolean {
        return queue.isEmpty()
    }
    
    override fun size(): Int {
        return queue.size
    }
}