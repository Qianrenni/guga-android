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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.qianrenni.reading.components.BottomNavigationBar
import com.qianrenni.reading.state.Navigator
import com.qianrenni.reading.state.rememberNavigationState
import com.qianrenni.reading.state.toEntries
import com.qianrenni.reading.util.SnackBarManager
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


@Composable
fun AppNavigation(context: Context) {
    val snackBarHostState = remember { SnackbarHostState() }
    // 1. 创建状态（内部持有多个 backStacks）
    val navigationState = rememberNavigationState(
        startRoute = Login,
        topLevelRoutes = setOf(Login, Home, History, Profile, Bookshelf)
    )
    val navigator = remember { Navigator(navigationState) }
    // 定义需要显示底部导航栏的路由
    val routesWithBottomBar = listOf(Home::class, Bookshelf::class, History::class, Profile::class)
    val routesWithoutPadding = listOf(BookRead::class)
    LaunchedEffect(Unit) {
        SnackBarManager.messages.collect { message ->
            snackBarHostState.showSnackbar(message)
        }
    }

    // 获取当前路由以决定是否显示底部导航栏
    val showBottomBar = navigator.currentState?.let { route ->
        routesWithBottomBar.any { route::class == it }
    } ?: false

    val withOutPadding = navigator.currentState?.let { route ->
        routesWithoutPadding.any { route::class == it }
    } ?: false
    val entryProvider = entryProvider {
        entry<Login> { LoginView(navigator = navigator) }
        entry<Register> { RegisterView(navigator = navigator) }
        entry<ForgetPassword> { ForgetPasswordView(navigator = navigator) }
        entry<UpdatePassword> { UpdatePasswordView(navigator = navigator) }
        entry<Home> { HomeView(navigator = navigator) }
        entry<History> { ReadingHistoryView(navigator = navigator) }
        entry<Profile> { ProfileView(navigator = navigator) }
        entry<Bookshelf> { BookShelfView(navigator = navigator) }
        entry<BookRead> { key ->
            BookReadView(
                context = context,
                bookId = key.bookId,
                chapterId = key.chapterId,
                navigator = navigator
            )
        }
        entry<BookInfo> { key -> BookInfoView(navigator = navigator, bookId = key.bookId) }
    }
    Scaffold(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navigator = navigator)
            }
        }
    ) { innerPadding ->
        NavDisplay(
            entries = navigationState.toEntries(entryProvider),
            onBack = { navigator.goBack() },
            modifier = if (withOutPadding) Modifier else Modifier.padding(innerPadding),
        )
    }
}