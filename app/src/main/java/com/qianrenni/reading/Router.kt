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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastAny
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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

private const val TAG = "AppNavigation"

class AuthNavController(
    private val navController: NavController,
    private val isLoginProvider: () -> Boolean,
    private val onAuthRequired: (String) -> Unit
) {
    // 不需要登录就能访问的路由前缀
    private val publicRoutes = listOf(
        "login",
        "register",
        "forget-password",
        "home",
        "book/"
    )

    // ========== 拦截 navigate ==========

    fun navigate(route: String, builder: NavOptionsBuilder.() -> Unit = {}) {
        if (interceptIfNeeded(route)) return
        navController.navigate(route, builder)
    }

    fun popBackStack(): Boolean = navController.popBackStack()

    fun popBackStack(
        route: String,
        inclusive: Boolean,
        saveState: Boolean = false
    ): Boolean = navController.popBackStack(route, inclusive, saveState)

    fun navigateUp(): Boolean = navController.navigateUp()

    val graph get() = navController.graph

    @Composable
    fun currentBackStackEntryAsState() = navController.currentBackStackEntryAsState()

    /**
     * 返回 true 表示已被拦截（跳转到了登录页），调用方应直接 return
     */
    private fun interceptIfNeeded(route: String): Boolean {
        val isPublic = publicRoutes.any { route.startsWith(it) }
        if (!isLoginProvider() && !isPublic) {
            // 1. 保存原始目标路由，登录后可以跳回来
            onAuthRequired(route)
            // 2. 跳转到登录页（受保护页面根本不会被加入栈）
            navController.navigate("login")
            return true
        }
        return false
    }
}

@Composable
fun AppNavigation(context: Context, authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()
    val isLogin by authViewModel.isLogin.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    // 定义需要显示底部导航栏的路由
    val routesWithBottomBar = listOf("home", "bookshelf", "history", "profile")
    val routesWithoutPadding = listOf("read")
    // 获取当前路由以决定是否显示底部导航栏
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry.value?.destination?.route
    val showBottomBar = currentRoute in routesWithBottomBar
    val withOutPadding = routesWithoutPadding.fastAny { currentRoute?.startsWith(it) ?: false }
    val authNavController = remember(navController, isLogin) {
        AuthNavController(
            navController = navController,
            isLoginProvider = { isLogin },
            onAuthRequired = { route ->
                // 将真实参数替换后的 URL 保存，登录成功后跳回
                authViewModel.setRedirectUrl(route)
            }
        )
    }
    // 2. 如果未登录，执行跳转
    LaunchedEffect(isLogin) {
        if (isLogin) {
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


    Scaffold(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = authNavController)
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
                LoginView(navController = authNavController)
            }

            // 注册页
            composable(
                route = "register",
            ) {
                RegisterView(navController = authNavController)
            }

            // 忘记密码页
            composable(
                route = "forget-password",
            ) {
                ForgetPasswordView(navController = authNavController)
            }

            // 修改密码页
            composable(
                route = "update-password",
            ) {
                UpdatePasswordView(navController = authNavController)
            }

            // 首页 - 书城
            composable(
                route = "home"
            ) {
                HomeView(navController = authNavController)
            }

            // 书籍详情
            composable(
                route = "book/{bookId}"
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId")?.toIntOrNull()
                if (bookId == null) {
                    authNavController.popBackStack()
                }
                BookInfoView(
                    navController = authNavController,
                    bookId = bookId!!
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
                    navController = authNavController,
                    bookId = bookId,
                    chapterId = chapterId
                )
            }

            // 书架
            composable(
                route = "bookshelf"
            ) {
                BookShelfView(navController = authNavController)
            }

            // 阅读历史
            composable(
                route = "history"
            ) {
                ReadingHistoryView(navController = authNavController)
            }

            // 个人中心
            composable(
                route = "profile"
            ) {
                ProfileView(navController = authNavController)
            }
        }
    }
}