package com.qianrenni.reading.viewmodels.auth

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qianrenni.reading.data.api.AuthService
import com.qianrenni.reading.data.model.LoginRequest
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
    fun login(onLoginSuccess: () -> Unit, onLoginError: (String) -> Unit) {
        Log.d(
            "Login",
            "login: Start - username: ${username.value}, password: ${password.value.take(1)}***, captcha: ${captcha.value}"
        )

        if (username.value.isEmpty() || password.value.isEmpty() || captcha.value.isEmpty()) {
            Log.d("Login", "login: Empty fields")
            onLoginError("请填写所有字段")
            return
        }

        Log.d("Login", "login: Setting isLoading to true")
        isLoading.value = true
        Log.d("Login", "login: isLoading value is now ${isLoading.value}")

        viewModelScope.launch {
            try {
                Log.d("Login", "login: Creating LoginRequest")
                val request = LoginRequest(
                    username = username.value,
                    password = password.value,
                    captcha = captcha.value
                )

                Log.d("Login", "login: Calling AuthService.login")
                val result = AuthService.login(request)
                Log.d("RESPONSE", "login: success=${result.success}, message=${result.message}")

                if (result.success && result.data != null) {
                    Log.d("Login", "login: Success")
                    // 登录成功
                    onLoginSuccess()
                } else {
                    Log.d("Login", "login: Failed - message: ${result.message}")
                    // 登录失败
                    onLoginError(result.message ?: "登录失败")
                }
            } catch (e: Exception) {
                Log.e("Login", "login: Exception", e)
                onLoginError("网络错误: ${e.message}")
            } finally {
                Log.d("Login", "login: Setting isLoading to false in finally block")
                isLoading.value = false
                Log.d("Login", "login: isLoading value is now ${isLoading.value}")
            }
        }

        Log.d("Login", "login: Function completed, coroutine launched")
    }
}
