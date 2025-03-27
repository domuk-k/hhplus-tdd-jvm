package io.hhplus.tdd.point.domain

import io.hhplus.tdd.common.util.ConcurrencyHandler
import io.hhplus.tdd.point.domain.entity.PointHistory
import io.hhplus.tdd.point.domain.entity.TransactionType
import io.hhplus.tdd.point.domain.entity.UserPoint
import io.hhplus.tdd.point.domain.command.PointCommand
import io.hhplus.tdd.point.domain.policy.ChargePointPolicy
import io.hhplus.tdd.point.domain.repository.PointHistoryRepository
import io.hhplus.tdd.point.domain.repository.PointRepository
import org.springframework.stereotype.Service

@Service
class PointService(
    private val pointRepository: PointRepository,
    private val pointHistoryRepository: PointHistoryRepository,
    private val chargePointPolicies: Array<ChargePointPolicy>, // 충전 정책은 "포인트" 도메인 자체보다, 외부(비즈니스) 상황에 의존한다. 외부에서 주입받은 정책들로만 검증한다.
    private val concurrencyHandler: ConcurrencyHandler
) {
    fun point(userId: Long): UserPoint {
        return pointRepository.findByUserId(userId)
    }

    fun history(userId: Long): List<PointHistory> {
        return pointHistoryRepository.findAllByUserId(userId)
    }

    fun charge(userId: Long, request: PointCommand.Charge): UserPoint {
        return concurrencyHandler.withLock(userId) {
            val current = pointRepository.findByUserId(userId)
            chargePointPolicies.forEach { it.validate(current, request.amount) }
            val charged = current.charge(request.amount)
            pointHistoryRepository.save(userId, request.amount, TransactionType.CHARGE)
            pointRepository.save(userId, charged.point)
        }
    }

    fun use(userId: Long, request: PointCommand.Use): UserPoint {
        return concurrencyHandler.withLock(userId) {
            val current = pointRepository.findByUserId(userId)
            val leftover = current.use(request.amount)
            pointHistoryRepository.save(userId, request.amount, TransactionType.USE)
            pointRepository.save(userId, leftover.point)
        }
    }

}