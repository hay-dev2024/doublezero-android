package com.doublezero.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun NavHostController.currentScreenAsState(): State<Screen?> {
    val backStackEntry = currentBackStackEntryAsState()
    return remember(backStackEntry) {
        derivedStateOf { backStackEntry.value?.toScreen() }
    }
}

fun NavBackStackEntry.toScreen(): Screen? {
    return when (destination.route?.substringBefore("?")?.substringBefore("/")) {
        Screen.Splash::class.qualifiedName -> Screen.Splash
        Screen.Home::class.qualifiedName -> Screen.Home
        Screen.MyPage::class.qualifiedName -> Screen.MyPage
        Screen.History::class.qualifiedName -> Screen.History
        Screen.Settings::class.qualifiedName -> Screen.Settings
        else -> null
    }
}

fun Screen.shouldShowTopBar(): Boolean = when (this) {
    Screen.Splash, Screen.Home -> false
    Screen.MyPage, Screen.History, Screen.Settings -> true
}

fun Screen.shouldShowBottomNav(): Boolean = when (this) {
    Screen.Home, Screen.MyPage -> true
    Screen.Splash, Screen.History, Screen.Settings -> false
}

fun Screen.getTitle(): String? = when (this) {
    Screen.MyPage -> "My Page"
    Screen.History -> "Driving History"
    Screen.Settings -> "Settings"
    else -> null
}

fun Screen.canNavigateBack(): Boolean = when (this) {
    Screen.History, Screen.Settings -> true
    else -> false
}

