package com.doublezero.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

fun NavController.navigateTo(
    screen: Screen,
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate(screen, builder)
}

fun NavController.safePopBackStack(): Boolean {
    return if (currentBackStackEntry != null) {
        popBackStack()
    } else {
        false
    }
}

