package com.doublezero.feature_mypage

import com.doublezero.core.ui.color.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import coil.compose.AsyncImage
import com.doublezero.feature_mypage.uistate.UserProfile

@Composable
fun SignedInMyPageScreen(
    userProfile: UserProfile,
    onLogout: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BrightWhite)
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
                iconBgColor = BlueishWhite,
                iconTint = Blue,
                onClick = onNavigateToHistory
            )
        }
        item {
            MenuButton(
                text = "Settings",
                icon = Icons.Default.Settings,
                iconBgColor = WarmishWhite,
                iconTint = Orange,
                onClick = onNavigateToSettings
            )
        }
        item {
            MenuButton(
                text = "Logout",
                icon = Icons.AutoMirrored.Filled.Logout,
                iconBgColor = ReddishWhite,
                iconTint = Red,
                onClick = onLogout
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
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
            AsyncImage(
                model = userProfile.photoUrl,
                contentDescription = userProfile.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Column {
                Text(userProfile.name, fontWeight = FontWeight.SemiBold)
                Text(
                    "john.doe@gmail.com",
                    fontSize = 14.sp,
                    color = Grey,
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
                tint = SomewhatGrey
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
            onNavigateToSettings = {}
        )
    }
}