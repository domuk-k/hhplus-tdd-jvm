package io.hhplus.tdd.point.policy

interface PointPolicy {
    fun validateCharge(current: Long, amount: Long)
    fun validateUse(current: Long, amount: Long)
}