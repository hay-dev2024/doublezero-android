package com.doublezero.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.doublezero.app.navigation.AppNavHost
import com.doublezero.app.navigation.Screen
import com.doublezero.app.navigation.canNavigateBack
import com.doublezero.app.navigation.currentScreenAsState
import com.doublezero.app.navigation.getTitle
import com.doublezero.app.navigation.shouldShowBottomNav
import com.doublezero.app.navigation.shouldShowTopBar
import com.doublezero.core.ui.components.CommonTopAppBar
import com.doublezero.core.ui.components.DoubleZeroBottomNav


@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBarManager(navController = navController)
        },
        bottomBar = {
            BottomNavManager(navController = navController)
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun TopAppBarManager(navController: NavHostController) {
    val currentScreen by navController.currentScreenAsState()

    currentScreen?.let { screen ->
        if (screen.shouldShowTopBar()) {
            CommonTopAppBar(
                title = screen.getTitle() ?: "",
                canNavigateBack = screen.canNavigateBack(),
                onNavigateUp = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun BottomNavManager(navController: NavHostController) {
    val currentScreen by navController.currentScreenAsState()

    AnimatedVisibility(
        visible = currentScreen?.shouldShowBottomNav() == true,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        DoubleZeroBottomNav(
            activeTab = when (currentScreen) {
                is Screen.Home -> "home"
                Screen.MyPage -> "mypage"
                else -> "home"
            },
            onNavigate = { route ->
                val targetScreen = when (route) {
                    "home" -> Screen.Home()
                    "mypage" -> Screen.MyPage
                    else -> Screen.Home()
                }

                if (currentScreen != targetScreen) {
                    navController.navigate(targetScreen) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            onSearchClick = {
                navController.navigate(Screen.Home(openSearch = true)) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}


