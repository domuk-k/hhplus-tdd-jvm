package io.hhplus.tdd.common.util

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

@Component
@Primary
class ReentrantLockConcurrencyHandler : ConcurrencyHandler {
    private val map = ConcurrentHashMap<Long, ReentrantLock>()

    override fun <T> withLock(key: Long, block: () -> T): T {
        val lock = map.computeIfAbsent(key) { ReentrantLock() }
        lock.lock()
        try {
            return block()
        } finally {
            lock.unlock()
        }
    }
}