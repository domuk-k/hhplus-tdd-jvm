package io.hhplus.tdd.point.dto

import jakarta.validation.constraints.Positive

data class PointUpdateDto(
    @field:Positive(message = "포인트는 0보다 커야 합니다.")
    val amount: Long,
)