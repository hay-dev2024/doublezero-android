package com.doublezero.feature_home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    openSearch: Boolean = false,
    onNavigate: (String) -> Unit
) {
    var showSheet by remember { mutableStateOf(openSearch) }
    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }

    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    LaunchedEffect(openSearch) {
        if (openSearch) {
            showSheet = true
        }
    }

    fun handleCloseSheet() {
        showSheet = false
        showResult = false
        origin = ""
        destination = ""
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        MapPlaceholder(Modifier.fillMaxSize())

        if (showSheet) {
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { handleCloseSheet() },
                sheetState = sheetState,
                dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Search Route",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(onClick = { handleCloseSheet() }) {
                            Icon(Icons.Default.Close, "Close", tint = Color(0xFF757575))
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    InfoCardsRow(modifier = Modifier.padding(bottom = 16.dp))

                    SearchForm(
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
                        onConfirmRoute = { handleCloseSheet() }
                    )
                }
            }
        }
    }
}


@Composable
private fun InfoCardsRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InfoCard(Modifier.weight(1f), Icons.Default.Speed, Color(0xFF2196F3), "Speed", "0 km/h", bgColor = Color(0xFFF8F9FA))
        InfoCard(Modifier.weight(1f), Icons.Default.Cloud, Color(0xFF2196F3), "Weather", "Clear", bgColor = Color(0xFFF8F9FA))
        InfoCard(Modifier.weight(1f), Icons.Default.Warning, Color(0xFF4CAF50), "Risk", "Safe", Color(0xFF4CAF50), Color(0xFFE8F5E9))
    }
}

@Composable
private fun InfoCard(modifier: Modifier = Modifier, icon: ImageVector, iconTint: Color, label: String, value: String, valueColor: Color = LocalContentColor.current, bgColor: Color) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, label, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = Color(0xFF757575))
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
private fun SearchForm(origin: String, destination: String, showResult: Boolean, onOriginChange: (String) -> Unit, onDestinationChange: (String) -> Unit, onFindRoute: () -> Unit, onConfirmRoute: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(bottom = 24.dp)) {
            SearchInput(origin, onOriginChange, "Origin", Icons.Default.LocationOn, Color(0xFF4CAF50))
            SearchInput(destination, onDestinationChange, "Destination", Icons.Default.LocationOn, Color(0xFFF44336))
        }
        Button(onFindRoute, Modifier.fillMaxWidth().padding(bottom = 24.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(vertical = 14.dp)) {
            Icon(Icons.Default.Search, null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Find Route", fontWeight = FontWeight.SemiBold)
        }
        AnimatedVisibility(
            visible = showResult,
            enter = fadeIn(tween(300)) + slideInVertically(tween(300), initialOffsetY = { it / 2 }),
            exit = fadeOut()
        ) {
            RouteSummaryCard(onConfirmRoute = onConfirmRoute)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchInput(value: String, onValueChange: (String) -> Unit, placeholder: String, icon: ImageVector, iconTint: Color) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color(0xFF2196F3), unfocusedIndicatorColor = Color(0xFFE0E0E0),
        )
    )
}

@Composable
private fun RouteSummaryCard(onConfirmRoute: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Route Summary", fontWeight = FontWeight.SemiBold)
            RouteSummaryInfoRow(Icons.Default.Schedule, Color(0xFFE3F2FD), Color(0xFF2196F3), "Estimated Arrival", "25 minutes")
            RouteSummaryInfoRow(Icons.Default.Map, Color(0xFFE3F2FD), Color(0xFF2196F3), "Total Distance", "12.5 km")
            RouteSummaryInfoRow(Icons.Default.CheckCircle, Color(0xFFE8F5E9), Color(0xFF4CAF50), "AI Risk Assessment", "Safe Route ✓", Color(0xFF4CAF50))
            Spacer(Modifier.height(4.dp))
            Button(onConfirmRoute, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
                Text("Confirm Route", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun RouteSummaryInfoRow(icon: ImageVector, iconBgColor: Color, iconTint: Color, label: String, value: String, valueColor: Color = LocalContentColor.current) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(iconBgColor), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(label, fontSize = 12.sp, color = Color(0xFF757575))
            Text(value, fontWeight = FontWeight.SemiBold, color = valueColor)
        }
    }
}

@Composable
private fun MapPlaceholder(modifier: Modifier = Modifier) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFE3F2FD)))
    Box(modifier = modifier.background(gradient), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Navigation, null, Modifier.size(64.dp).alpha(0.4f), tint = Color(0xFF9E9E9E))
            Text("Map View", color = Color(0xFF9E9E9E), fontSize = 14.sp, modifier = Modifier.padding(top = 12.dp))
            Text("Google Maps SDK Integration", color = Color(0xFF9E9E9E), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
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
        HomeScreen(openSearch = true, onNavigate = {})
    }
}