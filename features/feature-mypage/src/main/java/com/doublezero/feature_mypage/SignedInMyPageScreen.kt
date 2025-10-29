package com.doublezero.feature_mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* // Import everything from layout
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Settings
// Import only necessary Material 3 components
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage // Coil image loader
import com.doublezero.feature_mypage.uistate.UserProfile

// No Scaffold imports needed

/**
 * MyPageLoggedIn.tsx converted Composable.
 * Scaffold is completely removed, relying on MainActivity's Scaffold.
 */
@Composable
fun SignedInMyPageScreen(
    userProfile: UserProfile, // Provided by MyPageScreen (from ViewModel)
    onLogout: () -> Unit, // Provided by MyPageScreen (from ViewModel)
    // Navigation callbacks provided by MyPageScreen
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
    // Removed onNavigateToHome and onSearchClick parameters
) {
    // Removed the Scaffold wrapper.
    // Padding from MainActivity's Scaffold (passed via NavHost modifier) handles Top/Bottom bars.

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            // Apply the background color here
            .background(Color(0xFFF8F9FA))
            // The padding from MainActivity's Scaffold (containing TopAppBar and BottomAppBar)
            // is applied to the NavHost, which passes it down via its modifier.
            // This LazyColumn fills the space *within* those paddings.
            // Apply additional content padding specific to this screen.
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            UserProfileHeader(userProfile = userProfile)
        }
        item {
            MenuButton(
                text = "Driving History",
                icon = Icons.Default.DirectionsCar,
                iconBgColor = Color(0xFFE3F2FD),
                iconTint = Color(0xFF2196F3),
                onClick = onNavigateToHistory
            )
        }
        item {
            MenuButton(
                text = "Settings",
                icon = Icons.Default.Settings,
                iconBgColor = Color(0xFFFFF3E0),
                iconTint = Color(0xFFFF9800),
                onClick = onNavigateToSettings
            )
        }
        item {
            MenuButton(
                text = "Logout",
                icon = Icons.AutoMirrored.Filled.Logout,
                iconBgColor = Color(0xFFFFEBEE),
                iconTint = Color(0xFFF44336),
                onClick = onLogout
            )
        }
        // Add Spacer for bottom padding if content might be short
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// --- Helper Composables (UserProfileHeader, MenuButton) ---
// --- remain unchanged. They do not contain Scaffold. ---

@Composable
private fun UserProfileHeader(userProfile: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = userProfile.photoUrl,
                contentDescription = userProfile.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
                // placeholder(...) recommended
            )
            Column {
                Text(userProfile.name, fontWeight = FontWeight.SemiBold)
                Text(
                    "john.doe@gmail.com", // Mock email
                    fontSize = 14.sp,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun MenuButton(
    text: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(text, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF9E9E9E)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SignedInMyPageScreenPreview() {
    MaterialTheme {
        // Preview still works without Scaffold
        SignedInMyPageScreen(
            userProfile = UserProfile("John Doe", ""),
            onLogout = {},
            onNavigateToHistory = {},
            onNavigateToSettings = {}
        )
    }
}