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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.doublezero.core.ui.components.CommonTopAppBar
import com.doublezero.core.ui.components.DoubleZeroBottomNav
import com.doublezero.feature_home.entry.SplashScreen
import com.doublezero.feature_home.HomeScreen
import com.doublezero.feature_mypage.HistoryScreen
import com.doublezero.feature_mypage.MyPageScreen
import com.doublezero.feature_mypage.SettingsScreen

private const val ROUTE_SPLASH = "splash"
private const val ROUTE_HOME = "home"
private const val ROUTE_MYPAGE = "mypage"
private const val ROUTE_HISTORY = "history"
private const val ROUTE_SETTINGS = "settings"

/**
 * Scaffold, TopAppBar, BottomNav, and NavHost.
 */
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

/**
 * TopAppBar visibility
 */
@Composable
private fun TopAppBarManager(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topAppBarHiddenRoutes = setOf(
        ROUTE_SPLASH,
        ROUTE_HOME
    )

    val title: String?
    val canNavigateBack: Boolean

    when (currentRoute) {
        ROUTE_MYPAGE -> {
            title = "My Page"
            canNavigateBack = false
        }
        ROUTE_HISTORY -> {
            title = "Driving History"
            canNavigateBack = true
        }
        ROUTE_SETTINGS -> {
            title = "Settings"
            canNavigateBack = true
        }
        else -> {
            title = null
            canNavigateBack = false
        }
    }

    val showTopAppBar = title != null && currentRoute !in topAppBarHiddenRoutes

    if (showTopAppBar) {
        CommonTopAppBar(
            title = title,
            canNavigateBack = canNavigateBack,
            onNavigateUp = { navController.popBackStack() }
        )
    }
}

/**
 * BottomNav visibility
 */
@Composable
private fun BottomNavManager(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavHiddenRoutes = setOf(
        ROUTE_SPLASH,
        ROUTE_HISTORY,
        ROUTE_SETTINGS
    )

    AnimatedVisibility(
        visible = currentRoute != null && currentRoute !in bottomNavHiddenRoutes,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        DoubleZeroBottomNav(
            activeTab = when (currentRoute) {
                ROUTE_HOME -> "home"
                ROUTE_MYPAGE -> "mypage"
                else -> "home" // Default
            },
            onNavigate = { route ->
                if (currentRoute != route) {
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            onSearchClick = {
                // TODO: Handle search click, potentially by navigating to home
                // with an argument, which we'll address in the Navigation step.
                if (currentRoute != ROUTE_HOME) {
                    navController.navigate(ROUTE_HOME) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )
    }
}

/**
 * navigation routes
 */
@Composable
private fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = ROUTE_SPLASH,
        modifier = modifier // Apply padding from the parent Scaffold
    ) {
        composable(ROUTE_SPLASH) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(ROUTE_HOME) {
                        popUpTo(ROUTE_SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(ROUTE_HOME) {
            HomeScreen(
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(ROUTE_MYPAGE) {
            MyPageScreen(
                onNavigateToHome = { /* Handled by BottomNavManager */ },
                onNavigateToHistory = { navController.navigate(ROUTE_HISTORY) },
                onNavigateToSettings = { navController.navigate(ROUTE_SETTINGS) },
                onSearchClick = { /* Handled by BottomNavManager */ }
            )
        }

        composable(ROUTE_HISTORY) {
            HistoryScreen(
                onBackClicked = { navController.popBackStack() }
            )
        }

        composable(ROUTE_SETTINGS) {
            SettingsScreen(
                onBackClicked = { navController.popBackStack() }
            )
        }
    }
}