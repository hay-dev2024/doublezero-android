package com.doublezero.feature_mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

/**
 * MyPage feature의 메인 엔트리 스크린.
 * ViewModel의 상태에 따라 로그인/로그아웃 UI를 분기
 */
@Composable
fun MyPageScreen(
    viewModel: MyPageViewModel = hiltViewModel(),

    // 네비게이션 이벤트 콜백
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSearchClick: () -> Unit,
) {
    // ViewModel의 UI 상태 관찰
    val uiState by viewModel.uiState.collectAsState()


    // 로그인 상태에 따라 화면 표시
    if (uiState.isLoggedIn) {
        // 로그인 O: SignedInMyPageScreen 호출
        SignedInMyPageScreen(
            userProfile = uiState.userProfile,
            onLogout = viewModel::onLogout,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToSettings = onNavigateToSettings
        )
    } else {
        // 로그인 X: SignedOutMyPageScreen 호출
        SignedOutMyPageScreen(
            onLoginClick = viewModel::onLogin // ViewModel의 함수 연결
        )
    }
}