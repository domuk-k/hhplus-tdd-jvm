package io.hhplus.tdd.point.domain.request

import io.hhplus.tdd.point.domain.command.PointCommand
import jakarta.validation.constraints.Positive

class PointRequest {
    data class Charge(
        @field:Positive(message = "포인트는 0보다 커야 합니다.")
        val amount: Long,
    )

    data class Use(
        @field:Positive(message = "포인트는 0보다 커야 합니다.")
        val amount: Long,
    )
}

fun PointRequest.Charge.toCommand() = PointCommand.Charge(this.amount)
fun PointRequest.Use.toCommand() = PointCommand.Use(this.amount)
