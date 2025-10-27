package com.doublezero.feature_mypage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.doublezero.core.ui.components.DoubleZeroBottomNav

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignedOutMyPageScreen(
    onLoginClick: () -> Unit,
    onNavigateToHome: () -> Unit,
    onSearchClick: () -> Unit
) {

    var showLoginPopup by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Page",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF212121)
                )
            )
        },
        bottomBar = {
             DoubleZeroBottomNav(
                 activeTab = "mypage",
                 onNavigate = { if (it == "home") onNavigateToHome() else if (it == "mypage") { /* no-op */ } },
                 onSearchClick = onSearchClick
             )

            // 임시 Mock Bottom Nav
//            com.doublezero.feature_home.MockBottomNav(
//                activeTab = "mypage",
//                onNavigate = { if (it == "home") onNavigateToHome() },
//                onSearchClick = onSearchClick
//            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFF9E9E9E)
                    )
                }
                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Please sign in to access your profile",
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Sign in with Google Button
                Button(
                    onClick = { showLoginPopup = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    // TODO: Google 로고 SVG를 drawable 리소스로 추가 예정
                    // Icon(painter = painterResource(id = R.drawable.ic_google_logo), ...)
                    Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.ExtraBold) // 임시 Google 로고
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Sign in with Google",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface // 기본 텍스트 색
                    )
                }
            }
        }
    }

    // Mock Login Popup
    if (showLoginPopup) {
        LoginPopupDialog(
            onConfirmLogin = {
                onLoginClick()
                showLoginPopup = false
            },
            onDismiss = { showLoginPopup = false }
        )
    }
}

@Composable
private fun LoginPopupDialog(
    onConfirmLogin: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onConfirmLogin,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Continue as John Doe", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            // Cancel 버튼
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
            }
        },
        title = {
            Text("Google Sign In", fontWeight = FontWeight.SemiBold)
        },
        text = {
            Text(
                "Choose an account to continue to DoubleZero",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
        },
        shape = RoundedCornerShape(16.dp), // rounded-2xl
        containerColor = Color.White,
        // 버튼들을 Column으로 쌓기 위해 버튼 레이아웃을 직접 제어
        // Material3 기본 AlertDialog는 버튼을 Row로 배치
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SignedOutMyPageScreenPreview() {
    MaterialTheme {
        SignedOutMyPageScreen(
            onLoginClick = {},
            onNavigateToHome = {},
            onSearchClick = {}
        )
    }
}

// Preview 보기위해 임시로 public
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
public fun SignedOutMyPageScreenPopupPreview() {
    MaterialTheme {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoginPopupDialog(onConfirmLogin = { }, onDismiss = { })
        }
    }
}