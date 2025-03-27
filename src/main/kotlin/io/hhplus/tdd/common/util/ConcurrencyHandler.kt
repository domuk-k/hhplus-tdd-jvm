package io.hhplus.tdd.common.util

import java.util.concurrent.ConcurrentMap

interface ConcurrencyHandler {
    fun <T> withLock(key: Long, block: () -> T): T
}