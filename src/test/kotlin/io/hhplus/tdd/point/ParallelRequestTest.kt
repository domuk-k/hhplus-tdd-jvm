package io.hhplus.tdd.point

import io.hhplus.tdd.point.domain.command.PointCommand
import io.hhplus.tdd.point.domain.PointService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest
//@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL) // "IntegrationTestSupport" 같은 슈퍼클래스 만들고 가져다 쓸 떄.
class ParallelRequestTest(
    @Autowired private val pointService: PointService
) {
    @Test
    fun `Spring 환경에서 동시성 이슈 테스트`() {
        val id = 1L
        val threadCount = 5 // 실행할 스레드 개수
        val repeatPerThread = 2 // 각 스레드가 증가시킬 횟수
        // 스레드풀 생성
        val executorService = Executors.newFixedThreadPool(threadCount)

        // 여러 스레드에서 공유 자원을 동시에 수정
        repeat(threadCount) {
            executorService.submit {
                repeat(repeatPerThread) {
                    pointService.charge(id, PointCommand.Charge(1)) // 공유 자원 증가
                }
            }
        }

        // 스레드풀 종료 및 작업 완료 대기
        executorService.shutdown()
        while (!executorService.isTerminated) {
            Thread.sleep(10)
        }

        // 결과 확인
        val expectedValue = threadCount * repeatPerThread
        println("기대 값: ${expectedValue}")
        println("최종 값: ${pointService.point(id)}")
        assert(
            pointService.point(id).point != expectedValue.toLong()
        )
    }

    @Test
    fun `CountdownLatch로 동시성 이슈 테스트`() {
        val userId = 2L
        val threadCount = 5
        val repeatPerThread = 2
        val latch = CountDownLatch(threadCount)
        val executorService = Executors.newFixedThreadPool(threadCount)

        for (i in 0 until threadCount) {
            executorService.submit {
                try {
                    for (j in 0 until repeatPerThread) {
                        pointService.charge(userId, PointCommand.Charge(1))
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await() // 모든 스레드가 작업을 완료할 때까지 대기
        executorService.shutdown()

        val expectedValue = threadCount * repeatPerThread

        println("기대 값: $expectedValue")
        println("최종 값: ${pointService.point(userId).point}")
        assert(
            pointService.point(userId).point != expectedValue.toLong()
        )
    }

    @Test
    fun `Thread start로 동시성 이슈 테스트`() {
        val userId = 3L
        val threadCount = 5
        val repeatPerThread = 2
        val latch = CountDownLatch(threadCount)

        for (i in 0 until threadCount) {
            Thread {
                try {
                    for (j in 0 until repeatPerThread) {
                        pointService.charge(userId, PointCommand.Charge(1))
                    }
                } finally {
                    latch.countDown()
                }
            }.start()
        }

        latch.await() // 모든 스레드가 작업을 완료할 때까지 대기

        val expectedValue = threadCount * repeatPerThread

        println("기대 값: $expectedValue")
        println("최종 값: ${pointService.point(userId)}")
        assert(
            pointService.point(userId).point != expectedValue.toLong()
        )
    }

    @Test
    fun `코루틴을 활용한 병렬 요청`() = runBlocking {
        val trials = listOf<Long>(1, 2, 3, 4, 5, 6)

        withContext(Dispatchers.Default) {
            trials.map { amount ->
                async { pointService.charge(4L, PointCommand.Charge(amount)).point }
            }.awaitAll()
        }

        println("기대 값: ${trials.sum()}")
        println("실제 값: ${pointService.point(1L).point}")
        assert(trials.sum() != pointService.point(1L).point)
    }
}