package dev.gordeev.review.server.queue

interface ReviewQueue<T> {
    fun enqueue(item: T)
    fun enqueueAll(items: Collection<T>)
    fun dequeue(): T?
    fun getAll(): List<T>
    fun peek(): T?
    fun isEmpty(): Boolean
    fun size(): Int
}