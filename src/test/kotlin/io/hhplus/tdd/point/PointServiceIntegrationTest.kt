package io.hhplus.tdd.point

import io.hhplus.tdd.point.domain.PointService
import io.hhplus.tdd.point.domain.command.PointCommand
import io.hhplus.tdd.point.domain.entity.TransactionType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors


@SpringBootTest
class PointServiceIntegrationTest(
    @Autowired private val pointService: PointService
) {
    @Test
    fun `포인트 충전하고 사용할 수 있다`() {
        val userId = 12L
        val toCharge = 100L
        val toUse = 30L

        pointService.charge(userId, PointCommand.Charge(toCharge))
        pointService.use(userId, PointCommand.Use(toUse))
        val current = pointService.point(userId).point

        assertEquals(toCharge - toUse, current)
    }

    @Test
    fun `포인트를 최대 잔고 이상으로 사용할 수 없다`() {
        val userId = 123L
        val toCharge = 100L
        val toUse = 1020L

        pointService.charge(userId, PointCommand.Charge(toCharge))
        assertThrows<IllegalArgumentException> {
            pointService.use(userId, PointCommand.Use(toUse))
        }

    }

    @Test
    fun `포인트 내역 조회 테스트`() {
        val userId = 12L
        // 충전 50, 사용 20 후 내역에 해당 내역들이 존재하는지 확인
        pointService.charge(userId, PointCommand.Charge(50))
        pointService.use(userId, PointCommand.Use(20))
        val history = pointService.history(userId)

        assertTrue(1 == history.count { it.type == TransactionType.CHARGE && it.amount == 50L })
        assertTrue(1 == history.count { it.type == TransactionType.USE && it.amount == 20L })
    }

    @Test
    fun `동일 사용자에 대한 동시성 이슈 테스트`() {
        val userId = 123L

        // 동시성 제어가 없을 경우, 여러 스레드가 동시에 충전하면 일부 충전이 누락되어 최종 값이 예상과 다르게 나와야 함.
        val threadCount = 5
        val repeatPerThread = 2
        val expectedValue = threadCount * repeatPerThread

        // 초기값 0으로 설정(위 setup 호출)
        val latch = CountDownLatch(threadCount)
        val executorService = Executors.newFixedThreadPool(threadCount)

        for (i in 0 until threadCount) {
            executorService.submit {
                try {
                    repeat(repeatPerThread) {
                        pointService.charge(userId, PointCommand.Charge(1))
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await() // 모든 스레드의 작업이 완료될 때까지 대기
        executorService.shutdown()

        val finalValue = pointService.point(userId).point

        // 동시성 문제가 발생하면 최종 값이 예상값과 다르게 나와야 함.
        assertThat(finalValue).isEqualTo(expectedValue.toLong())
    }

}

