package com.doublezero.feature_mypage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.doublezero.core.ui.color.*
import androidx.compose.foundation.layout.* // Keep layout imports
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.doublezero.core.model.Trip
import com.doublezero.core.ui.utils.getRiskColor

// Mock data (keep for preview/development)
private val mockTrips = listOf(
    Trip(id = 1, date = "2025-10-22", time = "14:30", origin = "Seoul Station", destination = "Gangnam Office", distance = "12.5 km", duration = "25 min", risk = "safe", riskDetails = "Clear weather, low traffic. No accidents reported on route."),
    Trip(id = 2, date = "2025-10-21", time = "09:15", origin = "Home", destination = "Yeouido Park", distance = "8.3 km", duration = "18 min", risk = "caution", riskDetails = "Light rain conditions. Moderate traffic at intersections."),
    Trip(id = 3, date = "2025-10-20", time = "18:45", origin = "Hongdae", destination = "Incheon Airport", distance = "45.2 km", duration = "1 hr 5 min", risk = "safe", riskDetails = "Highway route. Clear conditions throughout journey."),
    Trip(id = 4, date = "2025-10-19", time = "22:00", origin = "Itaewon", destination = "Bundang", distance = "28.7 km", duration = "42 min", risk = "risk", riskDetails = "Night driving. Heavy rain and reduced visibility reported.")
)

@Composable
fun HistoryScreen(
    // onBackClicked is still needed for NavHost to trigger navigation,
    // even though the UI button is now managed by MainActivity.
    // We don't need it *inside* this composable anymore if there's no UI element using it here.
    // However, keeping it in the signature is fine if NavHost provides it.
    onBackClicked: () -> Unit // Provided by NavHost in MainActivity
) {
    var expandedTripId by remember { mutableStateOf<Int?>(null) }

    // Removed the Scaffold wrapper.
    // The background color is now applied directly to the LazyColumn or a Box wrapper if needed.
    // Padding from MainActivity's Scaffold (passed via NavHost modifier) handles Top/Bottom bars.

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BrightWhite)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(mockTrips, key = { it.id }) { trip ->
            TripItem(
                trip = trip,
                isExpanded = expandedTripId == trip.id,
                onClick = {
                    expandedTripId = if (expandedTripId == trip.id) null else trip.id
                }
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp)) // Bottom padding within the list
        }
    }
}

// --- Helper Composables (TripItem, RouteInfoRow, StatInfoRow) ---
// --- remain unchanged from the previous version. ---
// --- They do not contain Scaffold. ---

@Composable
private fun TripItem(
    trip: Trip,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val riskStyle = getRiskColor(trip.risk)
    val cornerShape = RoundedCornerShape(12.dp)

    Card(
        shape = cornerShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            // Header Row (Date/Time, Risk Tag, Chevron)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "${trip.date} · ${trip.time}",
                    fontSize = 14.sp,
                    color = Grey
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(riskStyle.bg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = riskStyle.label,
                            color = riskStyle.text,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        modifier = Modifier.size(20.dp),
                        tint = SomewhatGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Route Column (Origin, Destination)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RouteInfoRow(
                    icon = Icons.Default.LocationOn,
                    text = trip.origin,
                    iconTint = DarkGreen
                )
                RouteInfoRow(
                    icon = Icons.Default.LocationOn,
                    text = trip.destination,
                    iconTint = Red
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats Row (Distance, Duration)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatInfoRow(
                    icon = Icons.Default.Map,
                    text = trip.distance
                )
                StatInfoRow(
                    icon = Icons.Default.Schedule,
                    text = trip.duration
                )
            }
        }

        // Expanded Details
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(100)) + expandVertically(tween(200)),
            exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "AI Risk Summary",
                    tint = riskStyle.text,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "AI Risk Summary",
                        fontSize = 12.sp,
                        color = Grey
                    )
                    Text(
                        text = trip.riskDetails,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteInfoRow(icon: ImageVector, text: String, iconTint: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
        Text(text = text, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun StatInfoRow(icon: ImageVector, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Grey,
            modifier = Modifier.size(16.dp)
        )
        Text(text = text, fontSize = 13.sp, color = Grey)
    }
}


@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HistoryScreenPreview() {
    MaterialTheme {
        // Preview still works without Scaffold, showing just the list content
        HistoryScreen(onBackClicked = {})
    }
}