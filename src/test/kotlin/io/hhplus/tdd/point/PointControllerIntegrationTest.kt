package io.hhplus.tdd.point

import com.fasterxml.jackson.databind.ObjectMapper
import io.hhplus.tdd.point.domain.request.PointRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.hamcrest.Matchers.instanceOf

@SpringBootTest
@AutoConfigureMockMvc
class PointControllerIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper
) {
    @Test
    fun `포인트 조회 엔드포인트`() {
        mockMvc.get("/point/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.id") { value(1L) }
                jsonPath("$.data.point") { value(instanceOf<Long>(Long::class.java)) }
            }
    }

    @Test
    fun `포인트 충전 엔드포인트`() {
        val request = PointRequest.Charge(amount = 150)
        val requestJson = objectMapper.writeValueAsString(request)

        mockMvc.patch("/point/1/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.data.id") { value(1) }
                jsonPath("$.data.point") { value(150) }
            }
    }

    @Test
    fun `포인트 사용 엔드포인트`() {
        val toCharge = 50L
        val toUse = 30L
        val chargeRequest = PointRequest.Charge(amount = toCharge)
        val chargeRequestJson = objectMapper.writeValueAsString(chargeRequest)

        mockMvc.patch("/point/3/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = chargeRequestJson
        }

        val request = PointRequest.Use(amount = toUse)
        val requestJson = objectMapper.writeValueAsString(request)

        mockMvc.patch("/point/3/use") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.data.id") { value(3) }
                jsonPath("$.data.point") { value(toCharge - toUse) }
            }
    }

    @Test
    fun `포인트 변경내역 목록조회 엔드포인트`() {

        val chargeRequest = PointRequest.Charge(amount = 50)
        val chargeRequestJson = objectMapper.writeValueAsString(chargeRequest)

        mockMvc.patch("/point/4/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = chargeRequestJson
        }

        val useRequest = PointRequest.Charge(amount = 30)
        val useRequestJson = objectMapper.writeValueAsString(useRequest)

        mockMvc.patch("/point/4/use") {
            contentType = MediaType.APPLICATION_JSON
            content = useRequestJson
        }
        mockMvc.get("/point/4/histories")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.[0].userId") { value(4) }
                jsonPath("$.data.[0].type") { value("CHARGE") }
                jsonPath("$.data.[0].amount") { value(50) }
                jsonPath("$.data.[1].userId") { value(4) }
                jsonPath("$.data.[1].type") { value("USE") }
                jsonPath("$.data.[1].amount") { value(30) }
            }
    }
}