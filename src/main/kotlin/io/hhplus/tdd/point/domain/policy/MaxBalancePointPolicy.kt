package io.hhplus.tdd.point.domain.policy

import io.hhplus.tdd.point.domain.entity.UserPoint
import org.springframework.stereotype.Component

@Component
class MaxBalancePointPolicy(private val maxBalance: Long = Long.MAX_VALUE) : ChargePointPolicy {
    override fun validate(current: UserPoint, amount: Long) {
        if (current.point + amount > maxBalance) {
            throw IllegalArgumentException("Exceed max balance")
        }
    }
}