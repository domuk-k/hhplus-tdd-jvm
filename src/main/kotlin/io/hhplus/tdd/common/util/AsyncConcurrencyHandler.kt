package io.hhplus.tdd.common.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

interface AsyncConcurrencyHandler {
    suspend fun <T> withLock(block: suspend () -> T): T
}

object MutexAsyncConcurrencyHandler : AsyncConcurrencyHandler {
    private val map = ConcurrentHashMap<String, Mutex>()

    override suspend fun <T> withLock(block: suspend () -> T): T {
        val mutex = map.computeIfAbsent(block.toString()) { Mutex() }
        return mutex.withLock { block() }
    }
}

object ActorAsyncConcurrencyHandler : AsyncConcurrencyHandler {
    private val scope = CoroutineScope(kotlinx.coroutines.Dispatchers.Default)
    private val actor = scope.actor<suspend () -> Unit> {
        for (block in channel) block()
    }

    override suspend fun <T> withLock(block: suspend () -> T): T = coroutineScope {
        var result: T? = null
        val job = actor.send { result = block() }
        while (result == null) {
            delay(1)
        }
        result!!
    }
}