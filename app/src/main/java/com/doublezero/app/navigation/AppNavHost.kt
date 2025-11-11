package com.doublezero.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.doublezero.feature_home.entry.SplashScreen
import com.doublezero.feature_home.HomeScreen
import com.doublezero.feature_mypage.HistoryScreen
import com.doublezero.feature_mypage.MyPageScreen
import com.doublezero.feature_mypage.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash,
        modifier = modifier
    ) {
        composable<Screen.Splash> {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Splash> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Home> {
            HomeScreen(
                onNavigate = { screen -> navController.navigate(screen) }
            )
        }

        composable<Screen.MyPage> {
            MyPageScreen(
                onNavigateToHome = { },
                onNavigateToHistory = { navController.navigate(Screen.History) },
                onNavigateToSettings = { navController.navigate(Screen.Settings) },
                onSearchClick = { }
            )
        }

        composable<Screen.History> {
            HistoryScreen(
                onBackClicked = { navController.popBackStack() }
            )
        }

        composable<Screen.Settings> {
            SettingsScreen(
                onBackClicked = { navController.popBackStack() }
            )
        }
    }
}

