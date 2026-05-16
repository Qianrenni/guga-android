package com.qianrenni.reading.data.api

import com.qianrenni.reading.data.model.ApiResponse
import com.qianrenni.reading.data.model.EmailVerifyRequest
import com.qianrenni.reading.data.model.LoginRequest
import com.qianrenni.reading.data.model.LoginResponse
import com.qianrenni.reading.data.model.RegisterRequest
import com.qianrenni.reading.data.model.User
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

object AuthService {
    private var lastXCaptchaId: String? = ""
    suspend fun getCaptcha(): ByteArray {
        val response = NetworkClient.client.get("${NetworkClient.getBaseUrl()}captcha/get")
        this.lastXCaptchaId = response.headers["X-Captcha-Id"]
        return response.body<ByteArray>()
    }

    suspend fun login(
        request: LoginRequest,
        captchaId: String = this.lastXCaptchaId ?: ""
    ): ApiResponse<LoginResponse> {
        return NetworkClient.client.post("${NetworkClient.getBaseUrl()}token/get") {
            header("X-Captcha-Id", captchaId)
            setBody(request)
        }.body()
    }

    suspend fun refreshToken(): ApiResponse<LoginResponse> {
        return NetworkClient.client.post("${NetworkClient.getBaseUrl()}token/refresh") {
        }.body()
    }

    suspend fun getCurrentUser(): ApiResponse<User> {
        return NetworkClient.client.get("${NetworkClient.getBaseUrl()}token/auth/me") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    suspend fun register(
        request: RegisterRequest,
        captchaId: String
    ): ApiResponse<Nothing> {
        return NetworkClient.client.post("${NetworkClient.getBaseUrl()}user/register") {
            header("X-Captcha-Id", captchaId)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun verifyEmail(
        request: EmailVerifyRequest
    ): ApiResponse<Nothing> {
        return NetworkClient.client.post("${NetworkClient.getBaseUrl()}token/verify_email") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
