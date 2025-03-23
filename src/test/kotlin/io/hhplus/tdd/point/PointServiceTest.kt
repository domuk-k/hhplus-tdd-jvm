package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class PointServiceTest {

    private lateinit var pointService: PointService
    private val id = 1L

    @BeforeEach
    fun setUp() {
        pointService = PointService(UserPointTable(), PointHistoryTable())
    }

    @Test
    fun `포인트 조회`() {
        pointService.charge(id, 1000L)
        val point = pointService.point(id)
        assertEquals(1000L, point.point)
    }

    @Test
    fun `포인트 충전 및 사용 내역 조회`() {
        pointService.charge(id, 1000L)
        val remainingPoint = pointService.use(id, 200L)
        val histories = pointService.history(id)
        assertEquals(2, histories.size)
        assertTrue(histories.any { it.type.name == "CHARGE" && it.amount == 1000L })
        assertTrue(histories.any { it.type.name == "USE" && it.amount == 200L })
        assertEquals(pointService.point(id), remainingPoint)
    }

    @Test
    fun `포인트 충전`() {
        val updated = pointService.charge(id, 1500L)
        assertEquals(1500L, updated.point)
    }

    @Test
    fun `포인트 사용`() {
        pointService.charge(id, 2000L)
        val updated = pointService.use(id, 500L)
        assertEquals(1500L, updated.point)
    }

    @Test
    fun `충전 없이 포인트 사용 시 예외 발생`() {
        assertThrows<NoSuchElementException> {
            pointService.use(id, 500L)
        }
    }

    @Test
    fun `보유 포인트 초과 사용 시 예외 발생`() {
        pointService.charge(id, 300L)
        assertThrows<IllegalArgumentException> {
            pointService.use(id, 500L)
        }
    }
}