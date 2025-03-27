package io.hhplus.tdd.point.domain.repository

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.point.domain.entity.PointHistory
import io.hhplus.tdd.point.domain.entity.TransactionType
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