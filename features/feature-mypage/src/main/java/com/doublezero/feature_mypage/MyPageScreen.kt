package com.doublezero.feature_mypage

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun MyPageScreen(
    viewModel: MyPageViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSearchClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 로그인 결과 처리 런처
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { token ->
                    // 성공 시 ViewModel로 토큰 전달 (UI는 비즈니스 로직 모름)
                    viewModel.onGoogleLoginSuccess(token)
                }
            } catch (e: ApiException) {
                // 실패 로그 처리 (필요시 viewModel.onLoginFailed(e) 호출)
                e.printStackTrace()
            }
        }
    }

    // 로그인 트리거 함수
    // DI로 주입받은 옵션을 사용할 수도 있지만, Compose 환경에서는
    // BuildConfig를 사용하여 여기서 명시적으로 클라이언트를 생성하는 것이
    // Context 누수를 막고 가장 깔끔한 경우가 많습니다.
    val onLoginClick = remember {
        {
            // BuildConfig.WEB_CLIENT_ID는 Gradle 설정 덕분에 안전하게 주입됨
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.WEB_CLIENT_ID)
                .requestEmail()
                .build()

            val client = GoogleSignIn.getClient(context, gso)
            googleSignInLauncher.launch(client.signInIntent)
        }
    }

    if (uiState.isLoggedIn) {
        SignedInMyPageScreen(
            userProfile = uiState.userProfile,
            onLogout = viewModel::onLogout,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToSettings = onNavigateToSettings
        )
    } else {
        SignedOutMyPageScreen(
            onLoginClick = onLoginClick
        )
    }
}