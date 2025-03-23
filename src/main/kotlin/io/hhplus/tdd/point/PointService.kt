package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.springframework.stereotype.Service

@Service
class PointService(
    private val userPointTable: UserPointTable,
    private val pointHistoryTable: PointHistoryTable,
) {
    fun point(userId: Long): UserPoint {
        return userPointTable.selectById(userId)
    }

    fun history(userId: Long): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(userId)
    }

    fun charge(userId: Long, amount: Long): UserPoint {
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis())
        return userPointTable.insertOrUpdate(userId, amount)
    }

    fun use(userId: Long, amount: Long): UserPoint {
        val current = userPointTable.selectById(userId)
        if (current.point <= 0) {
            throw NoSuchElementException("No points available to use.")
        }
        if (current.point < amount) {
            throw IllegalArgumentException("Insufficient points available.")
        }
        pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis())
        return userPointTable.insertOrUpdate(userId, current.point - amount)
    }
}