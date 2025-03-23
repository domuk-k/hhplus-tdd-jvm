package io.hhplus.tdd.point.policy

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PointPolicyImplTest {
    @Test
    fun `충전금이 양수이고, 최대 잔고를 초과하지 않으면 충전 검증이 성공한다`() {
        // given
        val policy = PointPolicyImpl(1501L)
        val currentBalance = 1000L
        val chargeAmount = 500L

        // when, then
        assertDoesNotThrow {
            policy.validateCharge(currentBalance, chargeAmount)
        }
    }

    @Test
    fun `충전금이 0이하라면 충전 검증에서 예외가 발생한다`() {
        // given
        val policy = PointPolicyImpl()
        val currentBalance = 1000L

        // when, then
        val exception = assertThrows<IllegalArgumentException> {
            policy.validateCharge(currentBalance, 0L)
        }
        assertEquals("충전 금액은 0보다 커야 합니다", exception.message)

        val negativeException = assertThrows<IllegalArgumentException> {
            policy.validateCharge(currentBalance, -100L)
        }
        assertEquals("충전 금액은 0보다 커야 합니다", negativeException.message)
    }

    @Test
    fun `충전 결과가 될 잔고가 최대 잔고에 초과한다면 충전 검증에서 예외가 발생한다`() {
        // given
        val maxBalance = 10000L
        val policy = PointPolicyImpl(maxBalance)
        val currentBalance = 9000L
        val chargeAmount = 1001L

        // when, then
        val exception = assertThrows<IllegalArgumentException> {
            policy.validateCharge(currentBalance, chargeAmount)
        }
        assertEquals("최대 잔액을 초과하여 충전할 수 없습니다", exception.message)
    }

    @Test
    fun `충전 결과가 최대잔고와 같다면 충전 검증은 성공한다`() {
        // given
        val maxBalance = 10000L
        val policy = PointPolicyImpl(maxBalance)
        val currentBalance = 9000L
        val chargeAmount = 1000L

        // when, then
        assertDoesNotThrow {
            policy.validateCharge(currentBalance, chargeAmount)
        }
    }


    @Test
    fun `충전금이 양수이고, 최대 잔고를 초과하지 않으면 검증이 성공한다`() {
        // given
        val policy = PointPolicyImpl()
        val currentBalance = 1000L
        val useAmount = 500L

        // when, then
        assertDoesNotThrow {
            policy.validateUse(currentBalance, useAmount)
        }
    }

    @Test
    fun `충전금이 0이하라면 사용 검증에서 예외가 발생한다`() {

        // given
        val policy = PointPolicyImpl()
        val currentBalance = 1000L

        // when, then
        val exception = assertThrows<IllegalArgumentException> {
            policy.validateUse(currentBalance, 0L)
        }
        assertEquals("사용 금액은 0보다 커야 합니다", exception.message)

        val negativeException = assertThrows<IllegalArgumentException> {
            policy.validateUse(currentBalance, -100L)
        }
        assertEquals("사용 금액은 0보다 커야 합니다", negativeException.message)
    }

    @Test
    fun `사용할 포인트가 잔액보다 크다면 예외가 발생한다`() {
        // given
        val policy = PointPolicyImpl()
        val currentBalance = 1000L
        val useAmount = 1001L

        // when, then
        val exception = assertThrows<IllegalArgumentException> {
            policy.validateUse(currentBalance, useAmount)
        }
        assertEquals("잔액이 부족합니다", exception.message)
    }

    @Test
    fun `사용할 포인트가 잔액과 같다면 검증이 성공한다`() {
        // given
        val policy = PointPolicyImpl()
        val currentBalance = 1000L
        val useAmount = 1000L

        // when, then
        assertDoesNotThrow {
            policy.validateUse(currentBalance, useAmount)
        }
    }
}