package com.qianrenni.reading.data.api

import com.qianrenni.reading.data.model.ApiResponse
import com.qianrenni.reading.data.model.ForgotPasswordRequest
import com.qianrenni.reading.data.model.UpdatePasswordRequest
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

object UserService {
    suspend fun updatePassword(
        request: UpdatePasswordRequest
    ): ApiResponse<Nothing> {
        return NetworkClient.client.patch("${NetworkClient.getBaseUrl()}user/update-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun sendForgotPasswordCode(
        userAccount: String
    ): ApiResponse<Nothing> {
        return NetworkClient.client.get("${NetworkClient.getBaseUrl()}user/forgot-password") {
            parameter("user_account", userAccount)
            contentType(ContentType.Application.Json)
        }.body()
    }
    
    suspend fun resetPassword(
        request: ForgotPasswordRequest
    ): ApiResponse<Nothing> {
        return NetworkClient.client.patch("${NetworkClient.getBaseUrl()}user/forgot-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}