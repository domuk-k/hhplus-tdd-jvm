package io.hhplus.tdd.point.repository

import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.UserPoint
import org.springframework.stereotype.Repository

@Repository
class PointRepositoryImpl(
    private val userPointTable: UserPointTable,
) : PointRepository {
    override fun findByUserId(id: Long): UserPoint {
        return userPointTable.selectById(id)
    }

    override fun save(id: Long, amount: Long): UserPoint {
        return userPointTable.insertOrUpdate(id, amount)
    }
}