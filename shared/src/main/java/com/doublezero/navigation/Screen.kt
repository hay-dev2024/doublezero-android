package com.doublezero.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Splash : Screen()

    @Serializable
    data class Home(val openSearch: Boolean = false) : Screen()

    @Serializable
    data object MyPage : Screen()

    @Serializable
    data object History : Screen()

    @Serializable
    data object Settings : Screen()
}

