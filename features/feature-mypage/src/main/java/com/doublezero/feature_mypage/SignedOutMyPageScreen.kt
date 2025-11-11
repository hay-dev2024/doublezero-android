package com.doublezero.feature_mypage

import com.doublezero.core.ui.color.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignedOutMyPageScreen(
    onLoginClick: () -> Unit
) {
    var showLoginPopup by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrightWhite)
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
                    .background(LightGrey),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = SomewhatGrey
                )
            }
            Spacer(Modifier.height(24.dp))

            Text(
                text = "Please sign in to access your profile",
                color = Grey,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Sign in with Google Button
            Button(
                onClick = { showLoginPopup = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = BorderStroke(1.dp, LightGrey),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
            ) {
                Text("G", color = Blue, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Sign in with Google",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

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
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Continue as John Doe", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, LightGrey)
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
                color = Grey
            )
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SignedOutMyPageScreenPreview() {
    MaterialTheme {
        SignedOutMyPageScreen(
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SignedOutMyPageScreenPopupPreview() {
    MaterialTheme {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoginPopupDialog(onConfirmLogin = { }, onDismiss = { })
        }
    }
}