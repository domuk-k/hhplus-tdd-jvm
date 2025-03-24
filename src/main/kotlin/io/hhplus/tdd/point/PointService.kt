package io.hhplus.tdd.point

import io.hhplus.tdd.point.dto.PointUpdateDto
import io.hhplus.tdd.point.policy.ChargePointPolicy
import io.hhplus.tdd.point.repository.PointHistoryRepository
import io.hhplus.tdd.point.repository.PointRepository
import org.springframework.stereotype.Service

@Service
class PointService(
    private val pointRepository: PointRepository,
    private val pointHistoryRepository: PointHistoryRepository,
    private val chargePointPolicies: Array<ChargePointPolicy>
) {
    fun point(userId: Long): UserPoint {
        return pointRepository.findByUserId(userId)
    }

    fun history(userId: Long): List<PointHistory> {
        return pointHistoryRepository.findAllByUserId(userId)
    }

    fun charge(userId: Long, request: PointUpdateDto): UserPoint {
        val current = pointRepository.findByUserId(userId)
        // 충전 정책은 "포인트" 도메인 자체보다, 외부(비즈니스) 상황에 의존한다. 외부에서 주입받은 정책들로만 검증한다.
        chargePointPolicies.forEach { it.validate(current, request.amount) }
        val charged = current.charge(request.amount)

        pointHistoryRepository.save(userId, request.amount, TransactionType.CHARGE)
        return pointRepository.save(userId, charged.point)
    }

    fun use(userId: Long, request: PointUpdateDto): UserPoint {
        val current = pointRepository.findByUserId(userId)
        val leftover = current.use(request.amount)

        pointHistoryRepository.save(userId, request.amount, TransactionType.USE)
        return pointRepository.save(userId, leftover.point)
    }
}