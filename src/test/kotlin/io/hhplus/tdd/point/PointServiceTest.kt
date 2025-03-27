package io.hhplus.tdd.point

import io.hhplus.tdd.common.util.ConcurrencyHandler
import io.hhplus.tdd.common.util.ReentrantLockConcurrencyHandler
import io.hhplus.tdd.common.util.ReentrantLockImplTest
import io.hhplus.tdd.point.domain.command.PointCommand
import io.hhplus.tdd.point.domain.policy.ChargePointPolicy
import io.hhplus.tdd.point.domain.repository.PointHistoryRepository
import io.hhplus.tdd.point.domain.repository.PointRepository
import io.hhplus.tdd.point.domain.PointService
import io.hhplus.tdd.point.domain.entity.PointHistory
import io.hhplus.tdd.point.domain.entity.TransactionType
import io.hhplus.tdd.point.domain.entity.UserPoint
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class PointServiceTest {
    private val pointRepository: PointRepository = mockk<PointRepository>(relaxed = true)
    private val pointHistoryRepository: PointHistoryRepository = mockk<PointHistoryRepository>(relaxed = true)
    private val chargePolicy: ChargePointPolicy = mockk<ChargePointPolicy>(relaxed = true)
    private val concurrencyHandler: ConcurrencyHandler = ReentrantLockConcurrencyHandler()

    private val pointService: PointService =
        PointService(pointRepository, pointHistoryRepository, arrayOf(chargePolicy), concurrencyHandler)

    private val id = 1L
    private val arbitraryNow = System.currentTimeMillis()

    @Test
    fun `임의 유저의 포인트 조회할 수 있다`() {
        val point = pointService.point(id)
        assertThat(point.point).isEqualTo(0L)
    }

    @Nested
    inner class 포인트_변동내역 {
        @Test
        fun `포인트 충전 또는 사용하고, 포인트 변동 내역을 조회하면 해당 내역을 `() {
            every { pointHistoryRepository.findAllByUserId(id) } returns listOf(
                PointHistory(1, id, TransactionType.CHARGE, 1000L, arbitraryNow),
                PointHistory(2, id, TransactionType.USE, 200L, arbitraryNow)
            )
            every { pointRepository.save(any(), any()) } answers { UserPoint(id, 800L, arbitraryNow) }
            every { pointRepository.findByUserId(any()) } answers { UserPoint(id, 800L, arbitraryNow) }

            pointService.charge(id, PointCommand.Charge(1000L))
            pointService.use(id, PointCommand.Use(200L))

            val histories = pointService.history(id)

            assertThat(histories).hasSize(2)
            assertThat(histories).contains(PointHistory(1, id, TransactionType.CHARGE, 1000L, arbitraryNow))
            assertThat(histories).contains(PointHistory(2, id, TransactionType.USE, 200L, arbitraryNow))
        }
    }


    @Nested
    inner class 포인트_충전 {
        @Test
        fun `충천하면 그 만큼 잔액에 증가한다`() {
            val previousDeposit = 1000L
            val toAdd = 100L

            every { pointRepository.findByUserId(any()) } answers { UserPoint(id, previousDeposit, arbitraryNow) }
            every { pointRepository.save(any(), previousDeposit + toAdd) } answers {
                UserPoint(id, secondArg(), arbitraryNow)
            }

            val updated = pointService.charge(id, PointCommand.Charge(toAdd))
            assertEquals(previousDeposit + toAdd, updated.point)
        }

        @Test
        fun `정책에서 정한 충전 정책을 위반하면 예외가 발생한다`() {
            every { chargePolicy.validate(any(), any()) } throws IllegalArgumentException()

            assertThrows<IllegalArgumentException> {
                pointService.charge(id, PointCommand.Charge(1L))
            }
        }
    }

    @Nested
    inner class 포인트_사용 {
        @Test
        fun `포인트를 충전한 후 사용하면 해당 포인트 만큼 차감된다`() {
            every { pointRepository.save(any(), any()) } answers { UserPoint(id, 1500L, arbitraryNow) }

            pointService.charge(id, PointCommand.Charge(2000L))

            val updated = pointService.use(id, PointCommand.Use(500L))
            assertThat(updated.point).isEqualTo(1500L)
        }

        @Test
        fun `포인트가 없는 유저가 포인트 사용 시 예외가 발생한다`() {
            val deposit = 0L
            val toUse = 1L

            every { pointRepository.findByUserId(id) } answers { UserPoint(id, deposit, arbitraryNow) }

            assertThrows<IllegalArgumentException> {
                pointService.use(id, PointCommand.Use(toUse))
            }
        }


        @Test
        fun `일정 잔고를 가진 유저가 잔고 이상으로 포인트 사용 시 예외가 발생한다`() {
            val deposit = 300L
            val exceedsDeposit = deposit + 1L

            every { pointRepository.findByUserId(id) } answers { UserPoint(id, deposit, arbitraryNow) }

            pointService.charge(id, PointCommand.Charge(deposit))

            assertThrows<IllegalArgumentException> {
                pointService.use(id, PointCommand.Use(exceedsDeposit))
            }
        }
    }

}