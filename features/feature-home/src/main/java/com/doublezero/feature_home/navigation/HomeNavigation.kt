package com.doublezero.feature_home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.doublezero.navigation.Screen
import com.doublezero.feature_home.HomeScreen

fun NavGraphBuilder.homeScreen(
    onNavigateToMyPage: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    composable<Screen.Home> { backStackEntry ->
        val route = backStackEntry.toRoute<Screen.Home>()

        HomeScreen(
            openSearch = route.openSearch,
            onNavigateToMyPage = onNavigateToMyPage,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToSettings = onNavigateToSettings
        )
    }
}

