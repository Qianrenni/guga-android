package com.qianrenni.reading.viewmodels.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qianrenni.reading.data.api.AuthService
import com.qianrenni.reading.data.api.action
import com.qianrenni.reading.data.model.LoginRequest
import com.qianrenni.reading.data.store.AuthStore
import com.qianrenni.reading.util.SnackBarManager
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    // UI状态
    val username = mutableStateOf("")
    val password = mutableStateOf("")
    val captcha = mutableStateOf("")
    val rememberMe = mutableStateOf(true)
    val isLoading = mutableStateOf(false)
    val captchaBytes = mutableStateOf<ByteArray?>(null)

    init {
        viewModelScope.launch {
            AuthStore.initial()
        }
        loadCaptcha()
    }

    // 加载验证码
    fun loadCaptcha() {
        viewModelScope.launch {
            try {
                val result = AuthService.getCaptcha()
                if (result.success && result.data != null) {
                    captchaBytes.value = result.data
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 执行登录
    fun login(onLoginSuccess: () -> Unit = {}, onLoginError: (String) -> Unit = {}) {

        if (username.value.isEmpty() || password.value.isEmpty() || captcha.value.isEmpty()) {
            viewModelScope.launch {
                SnackBarManager.showMessage("请填写完整信息")
            }
            return
        }

        isLoading.value = true
        viewModelScope.launch {
            val result = AuthService.login(
                LoginRequest(
                    username = username.value,
                    password = password.value,
                    captcha = captcha.value
                )
            )
            result.action(
                {
                    AuthStore.setUser(it.data?.user)
                    AuthStore.setToken(
                        it.data?.access_token ?: "",
                        it.data?.refresh_token ?: "",
                        it.data?.token_type ?: "",
                        rememberMe.value
                    )
                    onLoginSuccess()
                }
            )
            isLoading.value = false

        }

    }
}
