package io.hhplus.tdd.point

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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class PointServiceTest {
    private val pointRepository: PointRepository = mockk<PointRepository>(relaxed = true)
    private val pointHistoryRepository: PointHistoryRepository = mockk<PointHistoryRepository>(relaxed = true)
    private val chargePolicy: ChargePointPolicy = mockk<ChargePointPolicy>(relaxed = true)

    private val pointService: PointService =
        PointService(pointRepository, pointHistoryRepository, arrayOf(chargePolicy))

    private val id = 1L
    private val arbitraryNow = System.currentTimeMillis()

    @Test
    fun `임의 유저의 포인트 조회할 수 있다`() {
        val point = pointService.point(id)
        assertEquals(0L, point.point)
    }

    @Test
    fun `포인트 충전,사용 후 포인트 변동 내역을 조회할 수 있다`() {
        every { pointHistoryRepository.findAllByUserId(id) } returns listOf(
            PointHistory(1, id, TransactionType.CHARGE, 1000L, arbitraryNow),
            PointHistory(2, id, TransactionType.USE, 200L, arbitraryNow)
        )
        every { pointRepository.save(any(), any()) } answers { UserPoint(id, 800L, arbitraryNow) }
        every { pointRepository.findByUserId(any()) } answers { UserPoint(id, 800L, arbitraryNow) }

        pointService.charge(id, PointCommand.Charge(1000L))

        val remainingPoint = pointService.use(id, PointCommand.Use(200L))
        val histories = pointService.history(id)
        assertEquals(2, histories.size)
        assertTrue(histories.any { it.type.name == "CHARGE" && it.amount == 1000L })
        assertTrue(histories.any { it.type.name == "USE" && it.amount == 200L })
        println(remainingPoint)
        assertEquals(pointService.point(id), remainingPoint)
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
        fun `포인트 사용 후 결과를 반환한다`() {
            every { pointRepository.save(any(), any()) } answers { UserPoint(id, 1500L, arbitraryNow) }

            pointService.charge(id, PointCommand.Charge(2000L))
            val updated = pointService.use(id, PointCommand.Use(500L))
            assertEquals(1500L, updated.point)
        }

        @Test
        fun `충전 없이 포인트 사용 시 예외가 발생한다`() {
            val deposit = 0L
            val toUse = 1L

            every { pointRepository.findByUserId(id) } answers { UserPoint(id, deposit, arbitraryNow) }

            assertThrows<IllegalArgumentException> {
                pointService.use(id, PointCommand.Use(toUse))
            }
        }


        @Test
        fun `보유 포인트 초과 사용 시 예외가 발생한다`() {
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