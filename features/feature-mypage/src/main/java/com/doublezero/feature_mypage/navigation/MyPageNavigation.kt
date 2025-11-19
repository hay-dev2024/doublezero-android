package com.doublezero.feature_mypage.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.doublezero.navigation.Screen
import com.doublezero.feature_mypage.MyPageScreen

fun NavGraphBuilder.myPageScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSearchClick: () -> Unit
) {
    composable<Screen.MyPage> {
        MyPageScreen(
            onNavigateToHome = onNavigateToHome,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToSettings = onNavigateToSettings,
            onSearchClick = onSearchClick
        )
    }
}

