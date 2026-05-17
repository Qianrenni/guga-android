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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.qianrenni.reading.components.BottomNavigationBar
import com.qianrenni.reading.util.SnackBarManager
import com.qianrenni.reading.viewmodels.auth.AuthViewModel
import com.qianrenni.reading.views.HomeView
import com.qianrenni.reading.views.auth.LoginView
import com.qianrenni.reading.views.book.BookInfoView
import com.qianrenni.reading.views.book.BookShelfView
import com.qianrenni.reading.views.book.ReadingHistoryView
import com.qianrenni.reading.views.user.ProfileView

@Composable
fun AppNavigation(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()
    val excludeRoutes = listOf("login")
    val isLogin by authViewModel.isLogin.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    // 定义需要显示底部导航栏的路由
    val routesWithBottomBar = listOf("home", "bookshelf", "history", "profile")

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

    // 获取当前路由以决定是否显示底部导航栏
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry.value?.destination?.route
    val showBottomBar = currentRoute in routesWithBottomBar

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            // 登录页
            composable(
                route = "login",
            ) {
                LoginView()
            }

            // 首页 - 书城
            composable(
                route = "home"
            ) {
                HomeView(navController = navController)
            }

            // 书籍详情
            composable(
                route = "book/{bookId}"
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId")?.toIntOrNull() ?: 0
                BookInfoView(
                    navController = navController,
                    bookId = bookId
                )
            }

            // 书架
            composable(
                route = "bookshelf"
            ) {
                BookShelfView(navController = navController)
            }

            // 阅读历史
            composable(
                route = "history"
            ) {
                ReadingHistoryView(navController = navController)
            }

            // 个人中心
            composable(
                route = "profile"
            ) {
                ProfileView(navController = navController)
            }
        }
    }
}