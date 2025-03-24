package io.hhplus.tdd.point

import io.hhplus.tdd.point.policy.PointPolicyImpl
import io.hhplus.tdd.point.repository.PointHistoryRepository
import io.hhplus.tdd.point.repository.PointRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class PointServiceTest {

    private lateinit var pointService: PointService
    private val id = 1L
    private val arbitraryNow = System.currentTimeMillis()
    private val MAX_BALANCE = 1234560L

    private lateinit var pointHistoryRepository: PointHistoryRepository
    private lateinit var pointRepository: PointRepository

    @BeforeEach
    fun setUp() {
        pointRepository = mockk<PointRepository>(relaxed = true)
        pointHistoryRepository = mockk<PointHistoryRepository>(relaxed = true)

        val now = System.currentTimeMillis()

        every { pointRepository.findByUserId(id) } answers { UserPoint(firstArg(), 1000L, now) }

        val pointPolicy = PointPolicyImpl(MAX_BALANCE)
        pointService = PointService(pointRepository, pointHistoryRepository, pointPolicy)
    }

    @Test
    fun `임의 유저의 포인트 조회할 수 있다`() {
        val point = pointService.point(id)
        assertEquals(1000L, point.point)
    }

    @Test
    fun `포인트 충전,사용 후 포인트 변동 내역을 조회할 수 있다`() {
        every { pointHistoryRepository.findAllByUserId(id) } returns listOf(
            PointHistory(1, id, TransactionType.CHARGE, 1000L, arbitraryNow),
            PointHistory(2, id, TransactionType.USE, 200L, arbitraryNow)
        )
        every { pointRepository.save(any(), any()) } answers { UserPoint(id, 800L, arbitraryNow) }
        every { pointRepository.findByUserId(any()) } answers { UserPoint(id, 800L, arbitraryNow) }

        pointService.charge(id, 1000L)

        val remainingPoint = pointService.use(id, 200L)
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
        fun `충천 후 결과를 반환한다`() {
            every { pointRepository.save(any(), any()) } answers { UserPoint(firstArg(), secondArg(), arbitraryNow) }

            val updated = pointService.charge(id, 1000L)
            assertEquals(1000L, updated.point)
        }

        @Test
        fun `정책에서 정한 최대 잔고 이상으로 충전을 시도하면 예외가 발생한다`() {
            assertThrows<IllegalArgumentException> {
                pointService.charge(id, MAX_BALANCE + 1L)
            }
        }

    }

    @Nested
    inner class 포인트_사용 {
        @Test
        fun `포인트 사용 후 결과를 반환한다`() {
            every { pointRepository.save(any(), any()) } answers { UserPoint(id, 1500L, arbitraryNow) }

            pointService.charge(id, 2000L)
            val updated = pointService.use(id, 500L)
            assertEquals(1500L, updated.point)
        }

        @Test
        fun `충전 없이 포인트 사용 시 예외가 발생한다`() {
            val deposit = 0L
            val toUse = 1L

            every { pointRepository.findByUserId(id) } answers { UserPoint(id, deposit, arbitraryNow) }

            assertThrows<IllegalArgumentException> {
                pointService.use(id, toUse)
            }
        }


        @Test
        fun `보유 포인트 초과 사용 시 예외가 발생한다`() {
            val deposit = 300L
            val exceedsDeposit = deposit + 1L

            every { pointRepository.findByUserId(id) } answers { UserPoint(id, deposit, arbitraryNow) }

            pointService.charge(id, deposit)

            assertThrows<IllegalArgumentException> {
                pointService.use(id, exceedsDeposit)
            }
        }
    }

}