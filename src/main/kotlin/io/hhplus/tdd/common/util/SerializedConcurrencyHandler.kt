package io.hhplus.tdd.common.util

import org.springframework.stereotype.Component
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

@Component
class SerializedConcurrencyHandler : ConcurrencyHandler {
    private val map = ConcurrentHashMap<Long, ExecutorService>()

    override fun <T> withLock(key: Long, block: () -> T): T {
        // ExecutorService (쓰레드풀)은 최소한 하나의 스레드를 유지.. lock 보다 무거움
        val pool: ExecutorService = map.computeIfAbsent(key) { Executors.newSingleThreadExecutor() }

        val future: Future<T> = pool.submit(Callable { block() })
        return future.get()
    }
}
