package com.doublezero.feature_mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel


@Composable
fun MyPageScreen(
    viewModel: MyPageViewModel = hiltViewModel(),

    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSearchClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()


    if (uiState.isLoggedIn) {
        SignedInMyPageScreen(
            userProfile = uiState.userProfile,
            onLogout = viewModel::onLogout,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToSettings = onNavigateToSettings
        )
    } else {
        SignedOutMyPageScreen(
            onLoginClick = viewModel::onLogin
        )
    }
}