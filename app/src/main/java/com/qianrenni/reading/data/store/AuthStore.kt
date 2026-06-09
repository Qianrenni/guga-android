package com.qianrenni.reading.data.store

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.qianrenni.reading.data.api.AuthService
import com.qianrenni.reading.data.api.NetworkClient
import com.qianrenni.reading.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthStore {
    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()
    var redirectUrl: String? = null
    private lateinit var prefs: SharedPreferences

    // 初始化方法，在 Application onCreate 中调用
    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    }

    // Actions
    suspend fun initial() {
        val savedAccessToken = prefs.getString("access_token", null)
        val savedRefreshToken = prefs.getString("refresh_token", null)
        val savedTokenType = prefs.getString("token_type", null)
        Log.d("TOKEN", "setToken:${savedAccessToken} $savedRefreshToken $savedTokenType ")
        if (savedAccessToken != null && savedRefreshToken != null && savedTokenType != null) {
            // 尝试 1：用旧 token 获取用户
            NetworkClient.setToken(savedAccessToken, savedTokenType)
            run {
                AuthService.getCurrentUser().onSuccess {
                    setUser(it)
                    true
                }
            } ?: run {
                // 尝试 2：用旧 refresh token 获取用户
                NetworkClient.setToken(savedRefreshToken, savedTokenType)
                val result = AuthService.refreshToken()
                result.onSuccess {
                    setUser(it.user)
                    saveToken(prefs, it.accessToken, it.refreshToken, it.tokenType)
                }
                result.onFailure { _, _, _ ->
                    NetworkClient.setToken("", "")
                }
            }
        }
    }

    fun setToken(
        accessToken: String,
        refreshToken: String,
        tokenType: String,
        isSave: Boolean = true
    ) {
        Log.d("TOKEN", "setToken:${accessToken} ${refreshToken} ${tokenType} ")
        NetworkClient.setToken(accessToken, tokenType)
        if (isSave) {
            saveToken(prefs, accessToken, refreshToken, tokenType)
        }
    }

    fun saveToken(
        prefs: SharedPreferences,
        accessToken: String,
        refreshToken: String,
        tokenType: String
    ) {
        // Save tokens to SharedPreferences
        prefs.edit().apply {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            putString("token_type", tokenType)
            apply()
        }
    }

    fun setUser(user: User?) {
        _user.value = user
    }

    fun clear() {
        setUser(null)
        prefs.edit().apply {
            remove("access_token")
            remove("refresh_token")
            remove("token_type")
            apply()
        }
    }
}