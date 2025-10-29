package com.doublezero.feature_mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * MyPage feature의 메인 엔트리 스크린.
 * ViewModel의 상태에 따라 로그인/로그아웃 UI를 분기
 */
@Composable
fun MyPageScreen(
    // Hilt/ViewModel 주입 (NavHost에서 처리)
    viewModel: MyPageViewModel = hiltViewModel(),

    // 네비게이션 이벤트 콜백 (MainActivity의 NavHost로부터 받음)
    onNavigateToHome: () -> Unit, // 사용되진 않지만 NavHost에서 전달됨
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSearchClick: () -> Unit, // 사용되진 않지만 NavHost에서 전달됨
) {
    // ViewModel의 UI 상태 관찰
    val uiState by viewModel.uiState.collectAsState()

    // 로그인 상태에 따라 올바른 화면 표시
    if (uiState.isLoggedIn) {
        // 로그인 O: SignedInMyPageScreen 호출
        SignedInMyPageScreen(
            userProfile = uiState.userProfile,
            onLogout = viewModel::onLogout, // ViewModel의 함수 연결
            onNavigateToHistory = onNavigateToHistory, // 네비게이션 콜백 전달
            onNavigateToSettings = onNavigateToSettings // 네비게이션 콜백 전달
            // onNavigateToHome, onSearchClick은 MainActivity가 관리하므로 전달 X
        )
    } else {
        // 로그인 X: SignedOutMyPageScreen 호출
        SignedOutMyPageScreen(
            onLoginClick = viewModel::onLogin // ViewModel의 함수 연결
            // onNavigateToHome, onSearchClick은 MainActivity가 관리하므로 전달 X
        )
    }
}