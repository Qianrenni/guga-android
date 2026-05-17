package com.qianrenni.reading

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.qianrenni.reading.viewmodels.auth.AuthViewModel
import com.qianrenni.reading.views.HomeView
import com.qianrenni.reading.views.auth.LoginView

@Composable
fun AppNavigation(authViewModel: AuthViewModel = AuthViewModel()) {
    val navController = rememberNavController()
    val excludeRoutes = listOf("login")
    val isLogin by authViewModel.isLogin.collectAsStateWithLifecycle()
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
    NavHost(navController = navController, startDestination = "home") {

        // 详情页，带参数
        composable(
            route = "login",
        ) {
            LoginView(navController = navController)
        }
        composable(
            route = "home"
        ) {
            HomeView(navController = navController)
        }
    }
}