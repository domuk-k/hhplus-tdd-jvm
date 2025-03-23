package io.hhplus.tdd.point

import io.hhplus.tdd.point.policy.PointPolicy
import io.hhplus.tdd.point.repository.PointHistoryRepository
import io.hhplus.tdd.point.repository.PointRepository
import org.springframework.stereotype.Service

@Service
class PointService(
    private val pointRepository: PointRepository,
    private val pointHistoryRepository: PointHistoryRepository,
    private val pointPolicy: PointPolicy
) {
    fun point(userId: Long): UserPoint {
        return pointRepository.findByUserId(userId)
    }

    fun history(userId: Long): List<PointHistory> {
        return pointHistoryRepository.findAllByUserId(userId)
    }

    fun charge(userId: Long, amount: Long): UserPoint {
        val current = pointRepository.findByUserId(userId)
        pointPolicy.validateCharge(current.point, amount)

        pointHistoryRepository.save(userId, amount, TransactionType.CHARGE)
        return pointRepository.save(userId, amount)
    }

    fun use(userId: Long, amount: Long): UserPoint {
        val current = pointRepository.findByUserId(userId)
        pointPolicy.validateUse(current.point, amount)

        pointHistoryRepository.save(userId, amount, TransactionType.USE)
        return pointRepository.save(userId, current.point - amount)
    }
}