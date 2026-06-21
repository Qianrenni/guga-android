package com.qianrenni.reading.viewmodels.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qianrenni.reading.data.store.AuthStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


class AuthViewModel : ViewModel() {

    //1. 响应式状态：直接从 AuthStore 映射过来
    // stateIn 将 Flow 转换为 Hot Flow，并在 ViewModel 生命周期内共享
    val isLogin = AuthStore.user.map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly, // 立即开始监听
            initialValue = false
        )


    fun setRedirectUrl(url: String) {
        AuthStore.redirectUrl = url
    }

    fun getRedirectUrl(): String? {
        val res = AuthStore.redirectUrl
        AuthStore.redirectUrl = null
        return res
    }

    fun getUser() = AuthStore.user
    fun clear() {
        AuthStore.clear()
    }
}