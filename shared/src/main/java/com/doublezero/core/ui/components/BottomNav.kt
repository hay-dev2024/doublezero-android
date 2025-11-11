package com.doublezero.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.doublezero.core.ui.color.Blue
import com.doublezero.core.ui.color.Grey
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 공용 하단 네비게이션 컴포넌트
 **/
@Composable
fun DoubleZeroBottomNav(
    activeTab: String,
    onNavigate: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    val navItems = listOf(
        BottomNavItem("search", "Search", Icons.Default.Search),
        BottomNavItem("home", "Home", Icons.Default.Home),
        BottomNavItem("mypage", "MyPage", Icons.Default.Person)
    )

    Surface(
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            navItems.forEach { item ->
                val isActive = activeTab == item.id
                val color = if (isActive) Blue else Grey

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .clickable {
                            if (item.id == "search" && onSearchClick != null) {
                                onSearchClick()
                            } else {
                                onNavigate(item.id)
                            }
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = item.label,
                        color = color,
                        fontSize = 11.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

private data class BottomNavItem(
    val id: String,
    val label: String,
    val icon: ImageVector
)

@Preview(showBackground = true)
@Composable
private fun BottomNavPreview() {
    MaterialTheme {
        DoubleZeroBottomNav(
            activeTab = "home",
            onNavigate = {},
            onSearchClick = {}
        )
    }
}

// 임시 Nav
@Composable
fun MockBottomNav(
    activeTab: String,
    onNavigate: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    val items = listOf(
        "search" to Icons.Default.Search,
        "home" to Icons.Default.Home,
        "mypage" to Icons.Default.Person
    )

    Surface(
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.height(64.dp) // h-16
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.forEach { (screen, icon) ->
                val isActive = activeTab == screen
                val color = if (isActive) Blue else Grey

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .clickable {
                            if (screen == "search") {
                                onSearchClick()
                            } else {
                                onNavigate(screen)
                            }
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = screen,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = screen.replaceFirstChar { it.titlecase() },
                        color = color,
                        fontSize = 11.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
