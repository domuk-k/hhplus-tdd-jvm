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
        val userId = 1L

        mockMvc.get("/point/$userId")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.id") { value(1L) }
                jsonPath("$.data.point") { value(instanceOf<Long>(Long::class.java)) }
            }
    }

    @Test
    fun `포인트 충전 엔드포인트`() {
        val request = PointRequest.Charge(amount = 50)
        val requestJson = objectMapper.writeValueAsString(request)

        mockMvc.patch("/point/1/charge") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(1) }
                jsonPath("$.username") { value("testUser") }
                // 100 + 50 = 150
                jsonPath("$.points") { value(150) }
            }
    }

    @Test
    fun `GET histories returns list of PointHistory`() {
        mockMvc.get("/point/1/histories")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].id") { value(1) }
                jsonPath("$[0].type") { value("charge") }
                jsonPath("$[0].amount") { value(50) }
                jsonPath("$[1].id") { value(1) }
                jsonPath("$[1].type") { value("use") }
                jsonPath("$[1].amount") { value(30) }
            }
    }

    @Test
    fun `PATCH use updates UserPoint`() {
        val request = PointRequest.Use(amount = 30)
        val requestJson = objectMapper.writeValueAsString(request)

        mockMvc.patch("/point/1/use") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(1) }
                jsonPath("$.username") { value("testUser") }
                // 100 - 30 = 70
                jsonPath("$.points") { value(70) }
            }
    }


}