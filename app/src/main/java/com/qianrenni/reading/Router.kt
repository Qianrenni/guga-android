package com.qianrenni.reading
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.qianrenni.reading.views.HomeView
import com.qianrenni.reading.views.auth.LoginView

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        // 详情页，带参数
        composable(
            route = "login",
        ) { backStackEntry ->
            LoginView(navController = navController)
        }
        composable(
            route = "home"
        ) {
            HomeView(navController=navController)
        }
    }
}