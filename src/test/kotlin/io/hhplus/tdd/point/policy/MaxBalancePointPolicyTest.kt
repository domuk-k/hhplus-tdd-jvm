package io.hhplus.tdd.point.policy

import io.hhplus.tdd.point.domain.entity.UserPoint
import io.hhplus.tdd.point.domain.policy.MaxBalancePointPolicy
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MaxBalancePointPolicyTest {
    @Test
    fun `최대 잔고를 초과하는 금액을 충전하고자하면 예외를 발생시킨다`() {
        val policy = MaxBalancePointPolicy(500L)
        val currentBalance = mockk<UserPoint>(relaxed = true)
        val chargeAmount = 501L

        assertThrows<IllegalArgumentException> {
            policy.validate(currentBalance, chargeAmount)
        }
    }
}