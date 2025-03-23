package io.hhplus.tdd.point.repository

import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.TransactionType

/**
 * 포인트 히스토리 저장소 인터페이스
 * @see /database 디렉토리에 있는 PointHistoryTable 클래스 구현은 임시 데이터베이스로 사용됩니다.
 */
interface PointHistoryRepository {
    fun save(userId: Long, amount: Long, type: TransactionType): PointHistory
    fun findAllByUserId(userId: Long): List<PointHistory>
}