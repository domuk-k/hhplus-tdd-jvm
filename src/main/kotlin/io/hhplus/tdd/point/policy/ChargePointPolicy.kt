package io.hhplus.tdd.point.policy

import io.hhplus.tdd.point.UserPoint

/**
 * 포인트 충전 정책
 * e.g 최대 잔고 제한, 최소 충전 금액, 충전 금액 단위 등
 * @see io.hhplus.tdd.point.PointService
 * PointService 에서 주입받아 충전 관련 시나리오에 적용함
 */
interface ChargePointPolicy {
    fun validate(current: UserPoint, amount: Long)
}