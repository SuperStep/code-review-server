package dev.gordeev.review.server.queue

import java.util.*

class InMemoryReviewQueue<T>(private val idExtractor: (T) -> Any) : ReviewQueue<T> {
    private val queue = LinkedList<T>()
    private val idToItemMap = HashMap<Any, T>() // Secondary index for O(1) lookups

    override fun enqueue(item: T) {
        queue.add(item)
        idToItemMap[idExtractor(item)] = item
    }

    override fun enqueueAll(items: Collection<T>) {
        queue.addAll(items)
        items.forEach { idToItemMap[idExtractor(it)] = it }
    }

    override fun dequeue(): T? {
        val item = queue.poll()
        item?.let { idToItemMap.remove(idExtractor(it)) }
        return item
    }

    override fun getAll(): List<T> {
        return queue.toList()
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

    override fun findById(id: Any): T? {
        return idToItemMap[id]
    }

    override fun exists(id: Any): Boolean {
        return idToItemMap.containsKey(id)
    }
}