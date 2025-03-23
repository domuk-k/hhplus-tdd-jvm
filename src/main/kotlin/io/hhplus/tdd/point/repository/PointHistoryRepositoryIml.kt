package io.hhplus.tdd.point.repository

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.TransactionType
import org.springframework.stereotype.Repository

@Repository
class PointHistoryRepositoryIml(
    private val pointHistoryTable: PointHistoryTable
) : PointHistoryRepository {
    override fun findAllByUserId(userId: Long): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(userId)
    }

    override fun save(userId: Long, amount: Long, type: TransactionType): PointHistory {
        return pointHistoryTable.insert(userId, amount, type, System.currentTimeMillis())
    }
}