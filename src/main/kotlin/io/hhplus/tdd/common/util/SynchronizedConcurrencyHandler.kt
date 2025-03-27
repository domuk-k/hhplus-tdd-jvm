package io.hhplus.tdd.common.util

import io.hhplus.tdd.common.util.ConcurrencyHandler
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class SynchronizedConcurrencyHandler : ConcurrencyHandler {
    private val map = ConcurrentHashMap<Long, Any>()

    override fun <T> withLock(key: Long, block: () -> T): T {
        val lock = map.computeIfAbsent(key) { Any() }
        return synchronized(lock) {
            block()
        }
    }
}