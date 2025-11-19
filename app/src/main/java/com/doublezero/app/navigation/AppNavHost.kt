package com.doublezero.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.doublezero.navigation.Screen
import com.doublezero.feature_home.entry.SplashScreen
import com.doublezero.feature_home.navigation.homeScreen
import com.doublezero.feature_mypage.navigation.myPageScreen
import com.doublezero.feature_mypage.HistoryScreen
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
                    navController.navigate(Screen.Home()) {
                        popUpTo<Screen.Splash> { inclusive = true }
                    }
                }
            )
        }

        homeScreen(
            onNavigateToMyPage = { navController.navigate(Screen.MyPage) },
            onNavigateToHistory = { navController.navigate(Screen.History) },
            onNavigateToSettings = { navController.navigate(Screen.Settings) }
        )

        myPageScreen(
            onNavigateToHome = {
                navController.navigate(Screen.Home()) {
                    popUpTo(Screen.Home()) { inclusive = true }
                }
            },
            onNavigateToHistory = { navController.navigate(Screen.History) },
            onNavigateToSettings = { navController.navigate(Screen.Settings) },
            onSearchClick = { navController.navigate(Screen.Home(openSearch = true)) }
        )

        composable<Screen.History> {
            HistoryScreen(onBackClicked = { navController.popBackStack() })
        }

        composable<Screen.Settings> {
            SettingsScreen(onBackClicked = { navController.popBackStack() })
        }
    }
}

