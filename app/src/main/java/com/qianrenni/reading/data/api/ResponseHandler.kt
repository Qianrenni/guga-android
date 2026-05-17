package com.qianrenni.reading.data.api

import android.util.Log
import com.qianrenni.reading.data.model.ApiResponse
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.contentType

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Failure(
        val message: String,
        val code: Int? = null,
        val exception: Throwable? = null
    ) : NetworkResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    // 便捷获取 data（失败时返回 null）
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }

    // 函数式风格处理
    fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (String, Int?, Throwable?) -> R
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Failure -> onFailure(message, code, exception)
    }

    fun <R> onSuccess(block: (T) -> R): R? = when (this) {
        is Success -> block(data)
        is Failure -> null
    }

    fun <R> onFailure(block: (String, Int?, Throwable?) -> R): R? = when (this) {
        is Success -> null
        is Failure -> block(message, code, exception)
    }
}

/**
 * 统一的网络响应处理器，responseHandler设计
 */
object ResponseHandler {

    const val SUCCESS_CODE = 0
    const val FAILURE_CODE = 1

    /**
     * 处理HTTP响应，返回标准化的结果
     */
    suspend inline fun <reified T> handleResponse(response: HttpResponse): NetworkResult<T> {
        val contentType = response.contentType()
        Log.d(
            "handleResponse", """
            Content-Type: ${contentType.toString()}
            Url: ${response.request.url}
            Status Code: ${response.status.value}
            Body: ${response.bodyAsText()}
        """.trimIndent()
        )
        return if (contentType?.match(ContentType.Application.Json) == true) {
            try {
                val apiResponse = response.body<ApiResponse<T>>()

                if (apiResponse.code == SUCCESS_CODE) {
                    // 成功保证 data 非空（如果后端可能返回 null，需额外校验）
                    apiResponse.data?.let {
                        NetworkResult.Success(it)
                    } ?: NetworkResult.Failure("数据为空", 0)
                } else {
                    NetworkResult.Failure(
                        message = apiResponse.message ?: "操作失败",
                        code = apiResponse.code
                    )
                }
            } catch (e: Exception) {
                Log.e("NETWORK ERROR", e.message.orEmpty())
                NetworkResult.Failure(
                    message = e.message ?: "解析失败",
                    exception = e
                )
            }
        } else {
            val success = isHttpSuccess(response.status.value)
            if (success) {
                NetworkResult.Failure("非JSON响应但状态成功", response.status.value)
            } else {
                NetworkResult.Failure("请求失败", response.status.value)
            }
        }
    }

    /**
     * 判断HTTP状态码是否表示成功
     */
    fun isHttpSuccess(statusCode: Int): Boolean {
        return statusCode in 200..299
    }
}