package com.qianrenni.reading.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val code: Int = 0,
    val message: String = "",
    val data: T? = null
) {
    val isSuccess: Boolean
        get() = code == 0
}