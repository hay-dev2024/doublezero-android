package com.doublezero.feature_mypage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.* // Import everything from layout
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
// Import only necessary Material 3 components
import androidx.compose.material3.* // Keep M3 imports
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// No Scaffold imports needed

/**
 * MyPageLoggedOut.tsx converted Composable.
 * Scaffold is completely removed, relying on MainActivity's Scaffold.
 */
@Composable
fun SignedOutMyPageScreen(
    onLoginClick: () -> Unit // Provided by MyPageScreen (from ViewModel)
    // Removed onNavigateToHome and onSearchClick parameters
) {
    var showLoginPopup by remember { mutableStateOf(false) }

    // Removed the Scaffold wrapper.
    // Box provides the background and centering.
    // Padding from MainActivity's Scaffold (passed via NavHost modifier) handles Top/Bottom bars.

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Apply the background color here
            .background(Color(0xFFF8F9FA))
            // The padding from MainActivity's Scaffold is applied by NavHost.
            // Apply additional content padding specific to this screen.
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Placeholder Icon
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
                // TODO: Replace 'G' with actual Google logo resource
                Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Sign in with Google",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    // Mock Login Popup
    if (showLoginPopup) {
        LoginPopupDialog(
            onConfirmLogin = {
                onLoginClick() // Trigger login logic in ViewModel
                showLoginPopup = false
            },
            onDismiss = { showLoginPopup = false }
        )
    }
}

// --- Helper Composable (LoginPopupDialog) ---
// --- remains unchanged. It does not contain Scaffold. ---

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
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SignedOutMyPageScreenPreview() {
    MaterialTheme {
        // Preview still works without Scaffold
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