package io.hhplus.tdd.point

data class UserPoint(
    val id: Long,
    val point: Long,
    val updateMillis: Long,
) {
    /**
     * 포인트 사용, 그 중에서도 "잔고" 이상으로 사용하는 시도를 제한하는 것은,
     * "포인트" 도메인 자체에서 다룰 만큼 직관적이고 견고한 정책이라 할 만 하다
     */
    fun use(amount: Long): UserPoint {
        val newPoint = this.point - amount
        if (newPoint < 0) throw IllegalArgumentException("보유 포인트가 부족합니다")
        return this.copy(point = newPoint)
    }

    fun charge(amount: Long): UserPoint {
        return this.copy(point = this.point + amount)
    }
}
