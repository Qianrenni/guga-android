package com.qianrenni.reading.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val username: String,
    val email: String,
    val avatar: String = "",
    val is_active: Boolean,
    val right: List<Int> = emptyList()
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
    val captcha: String
)

@Serializable
data class LoginResponse(
    val access_token: String,
    val refresh_token: String,
    val token_type: String = "Bearer",
    val user: User
)

@Serializable
data class TokenResponse(
    val access_token: String,
    val refresh_token: String,
    val token_type: String = "Bearer"
)

@Serializable
data class UserResponse(
    val user: User
)

@Serializable
data class UserRegister(
    val username: String,
    val password: String,
    val email: String,
    val avatar: String = ""
)

@Serializable
data class RegisterRequest(
    val user: UserRegister,
    val captcha: String
)

@Serializable
data class EmailVerifyRequest(
    val email: String
)

@Serializable
data class UpdatePasswordRequest(
    val username: String,
    val old_password: String,
    val new_password: String
)

@Serializable
data class ForgotPasswordRequest(
    val user_account: String,
    val verify_code: String,
    val password: String
)