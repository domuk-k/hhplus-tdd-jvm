package io.hhplus.tdd.common

/**
 * 공통 응답 객체를 정했다면 이런 식
 * 응답에 컨텍스트 넣기 위해 한번 감싸 놓을 순 있겠다
 */
data class Response<T>(
    val data: T? = null,
    val message: String? = null,
)

fun <T> T.toSuccessResponse(message: String = ""): Response<T> {
    return Response(this, message)
}