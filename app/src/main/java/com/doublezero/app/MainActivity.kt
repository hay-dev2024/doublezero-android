package com.doublezero.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.doublezero.app.ui.theme.DoubleZeroTheme
import com.doublezero.core.ui.components.CommonTopAppBar
import com.doublezero.core.ui.components.DoubleZeroBottomNav
import com.doublezero.feature_home.entry.SplashScreen
import com.doublezero.feature_home.HomeScreen
import com.doublezero.feature_mypage.HistoryScreen
import com.doublezero.feature_mypage.MyPageScreen
import com.doublezero.feature_mypage.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoubleZeroTheme {
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
        }
    }
}

// TopAppBar 표시/숨기기 및 내용 관리
@Composable
fun TopAppBarManager(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // TopAppBar가 필요 없는 화면 목록
    val topAppBarHiddenRoutes = setOf(
        "splash",
        "home"
    )

    // TopAppBar 내용 결정
    val title: String?
    val canNavigateBack: Boolean

    when (currentRoute) {
        "mypage" -> {
            title = "My Page"
            canNavigateBack = false // MyPage는 뒤로가기 없음
        }
        "history" -> {
            title = "Driving History"
            canNavigateBack = true
        }
        "settings" -> {
            title = "Settings"
            canNavigateBack = true
        }
        // TODO: 다른 피처의 화면 추가
        else -> {
            title = null // TopAppBar 숨김
            canNavigateBack = false
        }
    }

    // TopAppBar 표시 여부 결정
    val showTopAppBar = title != null && currentRoute !in topAppBarHiddenRoutes

    if (showTopAppBar) {
        CommonTopAppBar(
            title = title ?: "",
            canNavigateBack = canNavigateBack,
            onNavigateUp = { navController.popBackStack() }
            // actions = { /* 필요 시 화면별 액션 정의 */ }
        )
    }
}


@Composable
fun BottomNavManager(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavHiddenRoutes = setOf(
        "splash",
        "history",
        "settings"
    )

    AnimatedVisibility(
        visible = currentRoute != null && currentRoute !in bottomNavHiddenRoutes,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        DoubleZeroBottomNav(
            activeTab = when (currentRoute) {
                "home" -> "home"
                "mypage", "history", "settings" -> "mypage"
                else -> "home"
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
                if (currentRoute != "home") {
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                // TODO: HomeScreenViewModel과 연동하여 Bottom Sheet 열도록 상태 전달
            }
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier
    ) {
        composable("splash") {
            SplashScreen(
                onTimeout = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable("mypage") {
            MyPageScreen(
                onNavigateToHome = { /* BottomNavManager가 처리 */ },
                onSearchClick = { /* BottomNavManager가 처리 */ },
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable("history") {
            HistoryScreen(
                onBackClicked = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBackClicked = { navController.popBackStack() }
            )
        }
    }
}