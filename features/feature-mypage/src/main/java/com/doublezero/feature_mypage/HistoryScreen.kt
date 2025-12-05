package com.doublezero.feature_mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.doublezero.core.ui.color.*
import com.doublezero.data.model.HistoryItem
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    viewModel: MyPageViewModel = hiltViewModel()
) {
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingHistory.collectAsStateWithLifecycle()

    // Load history when screen is shown
    LaunchedEffect(Unit) {
        android.util.Log.d("HistoryScreen", "🔄 Loading history...")
        viewModel.loadHistory(50)
    }

    // Debug log
    LaunchedEffect(isLoading, historyList.size) {
        android.util.Log.d("HistoryScreen", "📊 State: isLoading=$isLoading, items=${historyList.size}")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrightWhite)
    ) {
        when {
            isLoading -> {
                android.util.Log.d("HistoryScreen", "⏳ Showing loading indicator")
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            historyList.isEmpty() -> {
                android.util.Log.d("HistoryScreen", "📭 History list is empty")
                Text(
                    text = "No driving history yet",
                    modifier = Modifier.align(Alignment.Center),
                    color = Grey
                )
            }
            else -> {
                android.util.Log.d("HistoryScreen", "✅ Showing ${historyList.size} history items")
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(historyList) { item ->
                        HistoryItemCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(item: HistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Origin and Destination
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "From",
                        fontSize = 12.sp,
                        color = Grey
                    )
                    Text(
                        text = formatLatLon(item.originLat, item.originLon),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "→",
                    fontSize = 20.sp,
                    color = Blue,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "To",
                        fontSize = 12.sp,
                        color = Grey
                    )
                    Text(
                        text = formatLatLon(item.destinationLat, item.destinationLon),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            HorizontalDivider(color = SomewhatGrey.copy(alpha = 0.3f), thickness = 1.dp)

            // Distance and Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                item.distanceMeters?.let { distance ->
                    Column {
                        Text(
                            text = "Distance",
                            fontSize = 12.sp,
                            color = Grey
                        )
                        Text(
                            text = formatDistance(distance),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                item.durationSeconds?.let { duration ->
                    Column {
                        Text(
                            text = "Duration",
                            fontSize = 12.sp,
                            color = Grey
                        )
                        Text(
                            text = formatDuration(duration),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Risk Summary
            item.riskLevel?.let { level ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = when (level.lowercase()) {
                                "high" -> ReddishWhite
                                "medium" -> WarmishWhite
                                else -> BlueishWhite
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Risk Level:",
                        fontSize = 12.sp,
                        color = Grey
                    )
                    Text(
                        text = level,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (level.lowercase()) {
                            "high" -> Red
                            "medium" -> Orange
                            else -> Blue
                        }
                    )
                }
            }

            // Timestamp
            Text(
                text = formatTimestamp(item.createdAt),
                fontSize = 11.sp,
                color = SomewhatGrey
            )
        }
    }
}

private fun formatLatLon(lat: Double, lon: Double): String {
    return "(${String.format(Locale.US, "%.4f", lat)}, ${String.format(Locale.US, "%.4f", lon)})"
}

private fun formatDistance(meters: Int): String {
    return if (meters >= 1000) {
        String.format(Locale.US, "%.1f km", meters / 1000.0)
    } else {
        "$meters m"
    }
}

private fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}

private fun formatTimestamp(timestamp: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(timestamp)

        val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: timestamp
    } catch (e: Exception) {
        timestamp
    }
}

