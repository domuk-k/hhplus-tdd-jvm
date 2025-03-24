package io.hhplus.tdd.point

import io.hhplus.tdd.point.dto.PointUpdateDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

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
    fun point(@PathVariable id: Long): UserPoint {
        return pointService.point(id)
    }

    /**
     * 포인트 충전/이용 내역을 조회
     */
    @GetMapping("{id}/histories")
    fun history(@PathVariable id: Long): List<PointHistory> {
        return pointService.history(id)
    }

    /**
     * 포인트 충전
     */
    @PatchMapping("{id}/charge")
    fun charge(
        @PathVariable id: Long,
        @Valid @RequestBody request: PointUpdateDto,
    ): UserPoint {
        return pointService.charge(id, request)
    }

    /**
     * 포인트 사용
     */
    @PatchMapping("{id}/use")
    fun use(
        @PathVariable id: Long,
        @Valid @RequestBody request: PointUpdateDto,
    ): UserPoint {
        return pointService.use(id, request)
    }
}

