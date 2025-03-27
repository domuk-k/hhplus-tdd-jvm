package io.hhplus.tdd.point.domain.command

class PointCommand {
    data class Charge(
        val amount: Long
    )

    data class Use(
        val amount: Long
    )
}