package io.hhplus.tdd.point

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserPointTest {
    @Test
    fun `포인트를 사용할 때, 잔고보다 부족하면 예외를 발생시킨다`() {
        val previousDeposit = 100L
        val userPoint = UserPoint(1, previousDeposit, 0)

        assertThrows<IllegalArgumentException> {
            userPoint.use(previousDeposit + 1L)
        }
    }
}