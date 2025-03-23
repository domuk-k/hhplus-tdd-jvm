package io.hhplus.tdd.point.policy

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
class PointPolicyImpl(private val maxBalance: Long = Long.MAX_VALUE) : PointPolicy {
    override fun validateCharge(current: Long, amount: Long) {
        require(amount > 0) { "충전 금액은 0보다 커야 합니다" }
        require(current + amount <= maxBalance) { "최대 잔액을 초과하여 충전할 수 없습니다" }
    }

    override fun validateUse(current: Long, amount: Long) {
        require(amount > 0) { "사용 금액은 0보다 커야 합니다" }
        require(current >= amount) { "잔액이 부족합니다" }
    }
}