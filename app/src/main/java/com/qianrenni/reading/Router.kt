package com.qianrenni.reading

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.qianrenni.reading.util.SnackBarManager
import com.qianrenni.reading.viewmodels.auth.AuthViewModel
import com.qianrenni.reading.views.HomeView
import com.qianrenni.reading.views.auth.LoginView

@Composable
fun AppNavigation(authViewModel: AuthViewModel = AuthViewModel()) {
    val navController = rememberNavController()
    val excludeRoutes = listOf("login")
    val isLogin by authViewModel.isLogin.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    // 2. 如果未登录，执行跳转
    LaunchedEffect(isLogin) {
        if (!isLogin) {
            // 避免重复跳转导致栈溢出，可以检查当前目的地
            navController.currentBackStackEntry?.destination?.route?.let {
                if (!excludeRoutes.contains(it)) {
                    authViewModel.setRedirectUrl(it)
                    navController.navigate("login") {
                        // 清除返回栈，防止用户按后退键回到受保护页面
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
        } else {
            navController.navigate(authViewModel.getRedirectUrl() ?: "home") {
                popUpTo("login") {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }
    LaunchedEffect(Unit) {
        SnackBarManager.messages.collect { message ->
            snackBarHostState.showSnackbar(message)
        }
    }
    Scaffold(snackbarHost = { SnackbarHost(hostState = snackBarHostState) }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {

            // 详情页，带参数
            composable(
                route = "login",
            ) {
                LoginView()
            }
            composable(
                route = "home"
            ) {
                HomeView(navController = navController)
            }
        }
    }
}