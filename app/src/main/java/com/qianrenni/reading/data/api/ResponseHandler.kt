package com.qianrenni.reading.data.api

import com.qianrenni.reading.data.model.ApiResponse
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * 统一的网络响应处理器，responseHandler设计
 */
object ResponseHandler {

    private const val SUCCESS_CODE = 0
    private const val FAILURE_CODE = 1

    /**
     * 处理HTTP响应，返回标准化的结果
     */
    suspend fun <T> handleResponse(response: HttpResponse): NetworkResult<T> {
        // 检查内容类型是否为JSON
        val contentType = response.contentType()
        return if (contentType?.match(ContentType.Application.Json) == true) {
            try {
                val apiResponse = response.body<ApiResponse<T>>()
                // 如果响应包含code字段，则使用code判断成功与否
                NetworkResult(
                    success = apiResponse.code == SUCCESS_CODE,
                    data = apiResponse.data,
                    message = apiResponse.message
                        ?: if (apiResponse.code == SUCCESS_CODE) "操作成功" else "操作失败"
                )
            } catch (e: Exception) {
                // JSON解析失败时，根据HTTP状态码判断
                val success = isHttpSuccess(response.status.value)
                NetworkResult(
                    success = success,
                    data = null,
                    message = e.message ?: if (success) "操作成功" else "操作失败"
                )
            }
        } else {
            // 非JSON响应，根据HTTP状态码判断
            val success = isHttpSuccess(response.status.value)
            NetworkResult(
                success = success,
                data = null,
                message = if (success) "操作成功" else "操作失败"
            )
        }
    }

    /**
     * 判断HTTP状态码是否表示成功
     */
    private fun isHttpSuccess(statusCode: Int): Boolean {
        return statusCode in 200..299
    }
}

/**
 * 网络请求结果封装类
 */
data class NetworkResult<T>(
    val success: Boolean,
    val data: T?,
    val message: String
)
