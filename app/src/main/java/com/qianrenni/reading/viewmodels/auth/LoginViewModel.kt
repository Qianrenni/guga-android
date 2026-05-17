package com.qianrenni.reading.viewmodels.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qianrenni.reading.data.api.AuthService
import com.qianrenni.reading.data.api.action
import com.qianrenni.reading.data.model.LoginRequest
import com.qianrenni.reading.data.store.AuthStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginState(
    val username: String = "",
    val password: String = "",
    val captcha: String = "",
    val rememberMe: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)

class LoginViewModel : ViewModel() {
    private val _loginState = MutableStateFlow(LoginState())
    val loginState = _loginState.asStateFlow()
    private val _captchaBytes = MutableStateFlow<ByteArray?>(null)
    val captchaBytes = _captchaBytes.asStateFlow()

    init {
        viewModelScope.launch {
            AuthStore.initial()
        }
        refreshCaptcha()

    }

    fun refreshCaptcha() {
        viewModelScope.launch {
            try {
                val result = AuthService.getCaptcha()
                result.action(
                    onSuccess = { res ->
                        _captchaBytes.update { res.data }
                    },
                    onFailure = { res ->
                        // 可以在这里处理错误，比如显示提示信息
                        println("获取验证码失败: ${res.message}")
                    }
                )
            } catch (e: Exception) {
                println("获取验证码异常: ${e.message}")
            }
        }
    }

    // 更新输入字段 (辅助函数，简化 UI 调用)
    fun onUsernameChange(newName: String) {
        _loginState.update { it.copy(username = newName, error = null) } // 输入时清除错误
    }

    fun onPasswordChange(newPass: String) {
        _loginState.update { it.copy(password = newPass, error = null) }
    }

    fun onCaptchaChange(newCaptcha: String) {
        _loginState.update { it.copy(captcha = newCaptcha, error = null) }
    }

    fun onRememberMeChange(checked: Boolean) {
        _loginState.update { it.copy(rememberMe = checked) }
    }

    // 执行登录
    fun login(onLoginSuccess: () -> Unit = {}, onLoginError: (String) -> Unit = {}) {
        val (username, password, captcha, rememberMe) = loginState.value

        if (username.isEmpty() || password.isEmpty() || captcha.isEmpty()) {
            _loginState.update { it.copy(error = "输入格式错误") }
            return
        }
        _loginState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = AuthService.login(
                LoginRequest(
                    username = username,
                    password = password,
                    captcha = captcha
                )
            )
            result.action(
                {
                    AuthStore.setUser(it.data?.user)
                    AuthStore.setToken(
                        it.data?.access_token ?: "",
                        it.data?.refresh_token ?: "",
                        it.data?.token_type ?: "",
                        rememberMe
                    )
                    _loginState.update { it.copy(isLoading = false, error = null) }
                    onLoginSuccess()
                },
                { res ->
                    _loginState.update { it.copy(isLoading = false, error = res.message) }
                    onLoginError(res.message)
                }
            )

        }

    }
}
