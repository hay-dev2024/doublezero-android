package com.doublezero.feature_home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.doublezero.core.ui.components.DoubleZeroBottomNav
import com.doublezero.core.ui.components.MockBottomNav

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    var isSearchOpen by remember { mutableStateOf(false) }
    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }

    fun handleCloseSheet() {
        isSearchOpen = false
        // TODO: React 코드처럼 300ms 딜레이 후 상태 초기화가 필요하다면 Coroutine/LaunchedEffect 사용
        showResult = false
        origin = ""
        destination = ""
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            DoubleZeroBottomNav(
                activeTab = "home",
                onNavigate = onNavigate,
                onSearchClick = { isSearchOpen = true }
            )

            // --- 임시 BottomNavigation (프리뷰 및 테스트용) ---
//            MockBottomNav(
//                activeTab = "home",
//                onNavigate = onNavigate,
//                onSearchClick = { isSearchOpen = true }
//            )
            // --- 임시 코드 끝 ---
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Scaffold의 bottomBar 영역을 제외한 나머지 공간
        ) {
            // 맵 뷰 플레이스홀더
            MapPlaceholder(Modifier.fillMaxSize())

            // 검색창이 열렸을 때만 보임
            AnimatedVisibility(
                visible = isSearchOpen,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300)),
                modifier = Modifier.zIndex(2f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { handleCloseSheet() }
                )
            }

            // Scaffold의 content 영역 하단에 배치
            SearchBottomSheet(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(3f), // 오버레이보다 위에 보이도록 zIndex 설정
                isSearchOpen = isSearchOpen,
                origin = origin,
                destination = destination,
                showResult = showResult,
                onOriginChange = { origin = it },
                onDestinationChange = { destination = it },
                onFindRoute = {
                    if (origin.isNotBlank() && destination.isNotBlank()) {
                        showResult = true
                    }
                },
                onConfirmRoute = { handleCloseSheet() },
                onClose = { handleCloseSheet() },
                onStartNavigation = { isSearchOpen = true }
            )
        }
    }
}

//Bottom Sheet div (애니메이션 포함)
@Composable
private fun SearchBottomSheet(
    modifier: Modifier = Modifier,
    isSearchOpen: Boolean,
    origin: String,
    destination: String,
    showResult: Boolean,
    onOriginChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onFindRoute: () -> Unit,
    onConfirmRoute: () -> Unit,
    onClose: () -> Unit,
    onStartNavigation: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), // rounded-t-3xl
        color = Color.White,
        shadowElevation = 8.dp // shadow-2xl
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0))
            )

            // Close Button (검색창 열렸을 때만 보임)
            if (isSearchOpen) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp, end = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF757575)
                    )
                }
            } else {
                Spacer(Modifier.height(20.dp)) // 닫혔을 때의 기본 상단 여백
            }

            // Scrollable content area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // content가 많아질 경우를 대비해 verticalScroll 추가
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                // Info Cards (항상 보임)
                InfoCardsRow(modifier = Modifier.padding(bottom = 16.dp))

                // Conditional Content
                if (isSearchOpen) {
                    // Search Form - Expanded State
                    SearchForm(
                        origin = origin,
                        destination = destination,
                        showResult = showResult,
                        onOriginChange = onOriginChange,
                        onDestinationChange = onDestinationChange,
                        onFindRoute = onFindRoute,
                        onConfirmRoute = onConfirmRoute
                    )
                } else {
                    // Start Navigation Button - Default State
                    Button(
                        onClick = onStartNavigation,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Start Navigation", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}


// Info Cards
@Composable
private fun InfoCardsRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InfoCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Speed,
            iconTint = Color(0xFF2196F3),
            label = "Speed",
            value = "0 km/h",
            bgColor = Color(0xFFF8F9FA)
        )
        InfoCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Cloud,
            iconTint = Color(0xFF2196F3),
            label = "Weather",
            value = "Clear",
            bgColor = Color(0xFFF8F9FA)
        )
        InfoCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Warning,
            iconTint = Color(0xFF4CAF50),
            label = "Risk",
            value = "Safe",
            valueColor = Color(0xFF4CAF50),
            bgColor = Color(0xFFE8F5E9)
        )
    }
}

// Info Card (재사용 컴포넌트)
@Composable
private fun InfoCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    bgColor: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = Color(0xFF757575))
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

// Search Form (Expanded State)
@Composable
private fun SearchForm(
    origin: String,
    destination: String,
    showResult: Boolean,
    onOriginChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onFindRoute: () -> Unit,
    onConfirmRoute: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Search Route",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Input Fields
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            SearchInput(
                value = origin,
                onValueChange = onOriginChange,
                placeholder = "Origin",
                icon = Icons.Default.LocationOn,
                iconTint = Color(0xFF4CAF50)
            )
            SearchInput(
                value = destination,
                onValueChange = onDestinationChange,
                placeholder = "Destination",
                icon = Icons.Default.LocationOn,
                iconTint = Color(0xFFF44336)
            )
        }

        // Find Route Button
        Button(
            onClick = onFindRoute,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Find Route", fontWeight = FontWeight.SemiBold)
        }

        // Result Card
        AnimatedVisibility(
            visible = showResult,
            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                animationSpec = tween(300),
                initialOffsetY = { it / 2 }
            ),
            exit = fadeOut()
        ) {
            RouteSummaryCard(onConfirmRoute = onConfirmRoute)
        }
    }
}

//Input Field (재사용 컴포넌트)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    iconTint: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color(0xFF2196F3),
            unfocusedIndicatorColor = Color(0xFFE0E0E0),
        )
    )
}

 // Result Card
@Composable
private fun RouteSummaryCard(onConfirmRoute: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Route Summary", fontWeight = FontWeight.SemiBold)

            RouteSummaryInfoRow(
                icon = Icons.Default.Schedule,
                iconBgColor = Color(0xFFE3F2FD),
                iconTint = Color(0xFF2196F3),
                label = "Estimated Arrival",
                value = "25 minutes"
            )
            RouteSummaryInfoRow(
                icon = Icons.Default.Map,
                iconBgColor = Color(0xFFE3F2FD),
                iconTint = Color(0xFF2196F3),
                label = "Total Distance",
                value = "12.5 km"
            )
            RouteSummaryInfoRow(
                icon = Icons.Default.CheckCircle,
                iconBgColor = Color(0xFFE8F5E9),
                iconTint = Color(0xFF4CAF50),
                label = "AI Risk Assessment",
                value = "Safe Route ✓",
                valueColor = Color(0xFF4CAF50)
            )

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onConfirmRoute,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text("Confirm Route", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// Result Card 내부 정보 행 (재사용 컴포넌트)
@Composable
private fun RouteSummaryInfoRow(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Column {
            Text(label, fontSize = 12.sp, color = Color(0xFF757575))
            Text(value, fontWeight = FontWeight.SemiBold, color = valueColor)
        }
    }
}

// Map Placeholder
@Composable
private fun MapPlaceholder(modifier: Modifier = Modifier) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE8F5E9), Color(0xFFE3F2FD))
    )
    Box(
        modifier = modifier.background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .alpha(0.4f),
                tint = Color(0xFF9E9E9E)
            )
            Text(
                text = "Map View",
                color = Color(0xFF9E9E9E),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                "Google Maps SDK Integration",
                color = Color(0xFF9E9E9E),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}


@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(onNavigate = {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenSearchOpenPreview() {
    MaterialTheme {
        // 'isSearchOpen' 상태를 직접 제어할 수 없으므로,
        // 프리뷰용 Composable을 만들거나, SearchBottomSheet만 따로 프리뷰합니다.
        SearchBottomSheet(
            isSearchOpen = true,
            origin = "Seoul Station",
            destination = "Gangnam",
            showResult = true,
            onOriginChange = {},
            onDestinationChange = {},
            onFindRoute = {},
            onConfirmRoute = {},
            onClose = {},
            onStartNavigation = {}
        )
    }
}