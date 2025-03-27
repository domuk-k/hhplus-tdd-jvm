package io.hhplus.tdd.common.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class ReentrantLockImplTest {
    private val lock = ReentrantLockConcurrencyHandler()
    private val resource = SharedResource()

    @BeforeEach
    fun setup() {
        resource.write(0)
    }

    @Test
    fun `단일 동작을 락을 통해 수행할 수 있다`() {
        lock.withLock(1) {
            resource.write(1)
        }
        assertThat(resource.read()).isEqualTo(1)
    }

    @Test
    fun `여러 동작을 중첩된 락 안에서 수행할 수 있다`() {
        lock.withLock(1) {
            resource.write(1)
            lock.withLock(1) {
                resource.write(resource.read() + 1)
            }
            lock.withLock(1) {
                resource.write(resource.read() + 1)
            }

        }
        assertThat(resource.read()).isEqualTo(3)
    }

    @Test
    fun `concurrent increment test`() {
        val threadCount = 10
        val incrementsPerThread = 10
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)


        repeat(threadCount) {
            executor.submit {
                repeat(incrementsPerThread) {
                    lock.withLock(1) {
                        val current = resource.read()
                        resource.write(current + 1)
                    }
                }
                latch.countDown()
            }
        }

        latch.await()
        executor.shutdown()
        assertThat(resource.read()).isEqualTo(threadCount * incrementsPerThread)
    }

    class SharedResource {
        private var value: Int = 0

        fun read() = value

        fun write(newValue: Int) {
            value = newValue
        }
    }
}