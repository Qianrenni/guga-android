package com.qianrenni.reading

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastAny
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.qianrenni.reading.components.BottomNavigationBar
import com.qianrenni.reading.util.SnackBarManager
import com.qianrenni.reading.viewmodels.auth.AuthViewModel
import com.qianrenni.reading.views.HomeView
import com.qianrenni.reading.views.auth.ForgetPasswordView
import com.qianrenni.reading.views.auth.LoginView
import com.qianrenni.reading.views.auth.RegisterView
import com.qianrenni.reading.views.auth.UpdatePasswordView
import com.qianrenni.reading.views.book.BookInfoView
import com.qianrenni.reading.views.book.BookReadView
import com.qianrenni.reading.views.book.BookShelfView
import com.qianrenni.reading.views.book.ReadingHistoryView
import com.qianrenni.reading.views.user.ProfileView
import kotlinx.serialization.Serializable

@Serializable
data object Home : NavKey

@Composable
fun AppNavigation(context: Context, authViewModel: AuthViewModel = viewModel()) {
    val backStack = rememberNavBackStack(Home)
    val excludeRoutes = listOf("login", "register", "forget-password")
    val isLogin by authViewModel.isLogin.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    // 定义需要显示底部导航栏的路由
    val routesWithBottomBar = listOf("home", "bookshelf", "history", "profile")
    val routesWithoutPadding = listOf("read")
    LaunchedEffect(Unit) {
        SnackBarManager.messages.collect { message ->
            snackBarHostState.showSnackbar(message)
        }
    }

    // 获取当前路由以决定是否显示底部导航栏
    val currentRoute by remember {
        derivedStateOf {
            backStack.last()
        }
    }
    val showBottomBar = currentRoute in routesWithBottomBar
    val withOutPadding = routesWithoutPadding.fastAny { currentRoute }
    Scaffold(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
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
            modifier = if (withOutPadding) Modifier else Modifier.padding(innerPadding)
        ) {
            // 登录页
            composable(
                route = "login",
            ) {
                LoginView(navController = navController)
            }

            // 注册页
            composable(
                route = "register",
            ) {
                RegisterView(navController = navController)
            }

            // 忘记密码页
            composable(
                route = "forget-password",
            ) {
                ForgetPasswordView(navController = navController)
            }

            // 修改密码页
            composable(
                route = "update-password",
            ) {
                UpdatePasswordView(navController = navController)
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

            // 阅读页面
            composable(
                route = "read/{bookId}/{chapterId}"
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId")?.toIntOrNull()
                val chapterId =
                    backStackEntry.arguments?.getString("chapterId")?.toIntOrNull()
                check(bookId != null)
                check(chapterId != null)
                BookReadView(
                    context = context,
                    navController = navController,
                    bookId = bookId,
                    chapterId = chapterId
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