package io.hhplus.tdd.point

import io.hhplus.tdd.point.domain.entity.UserPoint
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserPointTest {
    @Test
    fun `잔고 이상의 포인트를, 사용할 때, 예외를 발생시킨다`() {
        val previousDeposit = 100L

        val userPoint = UserPoint(1, previousDeposit, 0)

        assertThrows<IllegalArgumentException> {
            userPoint.use(previousDeposit + 1L)
        }
    }
}