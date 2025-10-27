package com.doublezero.feature_mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import coil.compose.AsyncImage
import com.doublezero.core.ui.components.DoubleZeroBottomNav


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignedInMyPageScreen(
    userProfile: UserProfile,
    onLogout: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHome: () -> Unit,
    onSearchClick: () -> Unit
) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // User Profile Header
            item {
                UserProfileHeader(userProfile = userProfile)
            }

            // Menu Buttons
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
        }
    }
}

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
            // Coil의 AsyncImage
            AsyncImage(
                model = userProfile.photoUrl,
                contentDescription = userProfile.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
                // TODO: placeholder(R.drawable.ic_default_profile) 추가 예정
            )
            Column {
                Text(userProfile.name, fontWeight = FontWeight.SemiBold)
                Text(
                    "john.doe@gmail.com", // mock email
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
        SignedInMyPageScreen(
            userProfile = UserProfile("John Doe", ""),
            onLogout = {},
            onNavigateToHistory = {},
            onNavigateToSettings = {},
            onNavigateToHome = {},
            onSearchClick = {}
        )
    }
}