package io.hhplus.tdd.point.interfaces

import io.hhplus.tdd.common.Response
import io.hhplus.tdd.common.toSuccessResponse
import io.hhplus.tdd.point.domain.entity.PointHistory
import io.hhplus.tdd.point.domain.PointService
import io.hhplus.tdd.point.domain.entity.UserPoint
import io.hhplus.tdd.point.domain.request.PointRequest
import io.hhplus.tdd.point.domain.request.toCommand
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/point")
class PointController(
    private val pointService: PointService,
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * 포인트 조회
     */
    @GetMapping("{id}")
    fun point(@PathVariable id: Long): Response<UserPoint> {
        println("??")

        return pointService.point(id).toSuccessResponse()
    }

    /**
     * 포인트 충전/이용 내역을 조회
     */
    @GetMapping("{id}/histories")
    fun history(@PathVariable id: Long): Response<List<PointHistory>> {
        return pointService.history(id).toSuccessResponse()
    }

    /**
     * 포인트 충전
     */
    @PatchMapping("{id}/charge")
    fun charge(
        @PathVariable id: Long,
        @Valid @RequestBody request: PointRequest.Charge,
    ): Response<UserPoint> {
        return pointService.charge(id, request.toCommand()).toSuccessResponse()
    }

    /**
     * 포인트 사용
     */
    @PatchMapping("{id}/use")
    fun use(
        @PathVariable id: Long,
        @Valid @RequestBody request: PointRequest.Use,
    ): Response<UserPoint> {
        return pointService.use(id, request.toCommand()).toSuccessResponse()
    }
}