package io.hhplus.tdd.point.domain.repository

import io.hhplus.tdd.point.domain.entity.UserPoint

/**
 * 포인트 저장소 인터페이스
 * @see /database 디렉토리에 있는 UserPointTable 클래스 구현은 임시 데이터베이스로 사용됩니다.
 */
interface PointRepository {
    fun findByUserId(id: Long): UserPoint
    fun save(id: Long, amount: Long): UserPoint
}