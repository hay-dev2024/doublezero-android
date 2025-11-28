package com.doublezero.feature_home

import androidx.compose.animation.AnimatedVisibility
import com.doublezero.core.ui.color.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

// New imports for Maps and permissions
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.provider.Settings

import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.CameraPositionState

// New lifecycle/hilt imports
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.IntrinsicSize
import kotlinx.coroutines.delay
import kotlin.math.*
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.rememberTileOverlayState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    openSearch: Boolean = false,
    onNavigateToMyPage: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showSheet by remember { mutableStateOf(openSearch) }
    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }
    var selectingForOrigin by remember { mutableStateOf(true) }

    // Map related state
    var locationPermissionGranted by remember { mutableStateOf(false) }
    val permissionStatusMessage = remember { mutableStateOf("") }
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        locationPermissionGranted = granted
        permissionStatusMessage.value = if (granted) "Permission: GRANTED" else "Permission: DENIED"
    }

    // Snackbar state to show arrival notification
    val snackbarHostState = remember { SnackbarHostState() }
    var wasSimulating by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSimulating) {
        // detect transition from simulating -> not simulating and show arrival snackbar once
        if (wasSimulating && !state.isSimulating) {
            // simple English message for US audience
            snackbarHostState.showSnackbar("Arrived at destination")
        }
        wasSimulating = state.isSimulating
    }

    LaunchedEffect(Unit) {
        // check current permission
        locationPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        permissionStatusMessage.value = if (locationPermissionGranted) "Permission: GRANTED" else "Permission: DENIED"
    }

    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    LaunchedEffect(openSearch) {
        if (openSearch) {
            showSheet = true
        }
    }

    // react to selected origin/destination from VM to update text fields
    LaunchedEffect(state.selectedOrigin) {
        state.selectedOrigin?.let { origin = it.name }
    }
    LaunchedEffect(state.selectedDestination) {
        state.selectedDestination?.let { destination = it.name }
    }
    LaunchedEffect(state.routes) {
        showResult = state.routes.isNotEmpty()
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
        if (state.userLocation == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // create shared camera state here so HomeScreen can control camera movements
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(state.userLocation!!, 15f)
            }

            // keep camera follow effect for simPosition from ViewModel
            LaunchedEffect(state.simPosition) {
                state.simPosition?.let { sp ->
                    try {
                        // Use immediate move instead of animate to avoid slow camera animation when simulation starts
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(sp, 16f))
                    } catch (_: Exception) { }
                }
            }

            var showStepsSheet by remember { mutableStateOf(false) }

            // Always show the Map (so tiles load even if user hasn't granted location permission).
            MapScreenRoutes(
                routes = state.routes,
                selectedIndex = state.selectedRouteIndex,
                onSelect = { idx -> viewModel.selectRoute(idx) },
                locationPermissionGranted = locationPermissionGranted,
                cameraPositionState = cameraPositionState,
                modifier = Modifier.fillMaxSize(),
                simulatedPosition = state.simPosition
            )

            // When routes change, animate camera to fit the selected route (if possible)
            LaunchedEffect(state.routes, state.selectedRouteIndex) {
                if (state.routes.isNotEmpty()) {
                    val idx = state.selectedRouteIndex.takeIf { it >= 0 } ?: 0
                    val selected = state.routes.getOrNull(idx)
                    selected?.polyline?.takeIf { it.isNotBlank() }?.let { enc ->
                        try {
                            val points = PolyUtil.decode(enc).map { LatLng(it.latitude, it.longitude) }
                            if (points.isNotEmpty()) {
                                val lats = points.map { it.latitude }
                                val lons = points.map { it.longitude }
                                val north = lats.maxOrNull() ?: 0.0
                                val south = lats.minOrNull() ?: 0.0
                                val east = lons.maxOrNull() ?: 0.0
                                val west = lons.minOrNull() ?: 0.0
                                val centerLat = (north + south) / 2.0
                                val centerLon = (east + west) / 2.0
                                val latSpan = north - south
                                val lonSpan = east - west
                                val span = maxOf(latSpan, lonSpan)
                                // simple heuristic for zoom based on span
                                val zoom = when {
                                    span < 0.01 -> 15f
                                    span < 0.05 -> 14f
                                    span < 0.25 -> 12f
                                    span < 1.0 -> 10f
                                    else -> 8f
                                }
                                cameraPositionState.animate(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(LatLng(centerLat, centerLon), zoom)))
                            }
                        } catch (_: Exception) {
                            // ignore camera centering errors
                        }
                    } ?: run {
                        // no polyline -> if origin selected, center to origin
                        (state.selectedOrigin as? com.doublezero.data.network.PlaceDto)?.let {
                            cameraPositionState.animate(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(LatLng(it.lat, it.lon), 14f)))
                        }
                    }
                }
                else {
                    // no routes: if origin exists, center to origin
                    (state.selectedOrigin as? com.doublezero.data.network.PlaceDto)?.let {
                        cameraPositionState.animate(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(LatLng(it.lat, it.lon), 14f)))
                    }
                }
            }

            // If permission is not granted, show a small overlay with a button to request it.
            if (!locationPermissionGranted) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .width(320.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BrightWhite)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Location access needed", fontWeight = FontWeight.SemiBold)
                            Text("Allow location to enable better routing and centering.", fontSize = 12.sp, color = SomewhatGrey)
                            Spacer(Modifier.height(6.dp))
                            Text(permissionStatusMessage.value, fontSize = 12.sp, color = SomewhatGrey)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
                                Text("Allow")
                            }
                            Spacer(Modifier.height(6.dp))
                            TextButton(onClick = {
                                // open app settings so user can manually enable permission
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            }) {
                                Text("Open Settings")
                            }
                        }
                    }
                }
            }

            // Snackbar host to show arrival notification when simulation ends
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 86.dp)
            )

            if (showSheet) {
                androidx.compose.material3.ModalBottomSheet(
                    onDismissRequest = { handleCloseSheet() },
                    sheetState = sheetState,
                    dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
                ) {
                    // Branch: if we have results (showResult), show only the summary+Drive button.
                    when (showResult) {
                        // Minimal summary view so map remains visible
                        true -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 12.dp, bottom = 24.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Route Summary",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    IconButton(onClick = { handleCloseSheet() }) {
                                        Icon(Icons.Default.Close, "Close", tint = Grey)
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                val selectedRoute = state.routes.getOrNull(state.selectedRouteIndex)
                                RouteSummaryCard(
                                    route = selectedRoute,
                                    originName = (state.selectedOrigin as? com.doublezero.data.network.PlaceDto)?.name ?: origin,
                                    destinationName = (state.selectedDestination as? com.doublezero.data.network.PlaceDto)?.name ?: destination,
                                    onDrive = {
                                        viewModel.startSimulation()
                                        handleCloseSheet()
                                    },
                                    onReset = {
                                        viewModel.startNewSearch()
                                        handleCloseSheet()
                                    }
                                )
                            }
                        }
                        // Original search UI (keeps suggestions/inputs)
                        false -> {
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
                                        Icon(Icons.Default.Close, "Close", tint = Grey)
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

//                            InfoCardsRow(modifier = Modifier.padding(bottom = 16.dp))

                                // Search inputs
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                                        SearchInput(origin, onValueChange = {
                                            origin = it
                                            selectingForOrigin = true
                                            viewModel.onQueryChanged(it)
                                        }, placeholder = "Origin", icon = Icons.Default.LocationOn, iconTint = DarkGreen)

                                        SearchInput(destination, onValueChange = {
                                            destination = it
                                            selectingForOrigin = false
                                            viewModel.onQueryChanged(it)
                                        }, placeholder = "Destination", icon = Icons.Default.LocationOn, iconTint = Red)
                                    }

                                    // Suggestions list
                                    if (state.suggestions.isNotEmpty()) {
                                        LazyColumn(modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)) {
                                            items(state.suggestions) { suggestion: com.doublezero.data.network.PlaceSuggestionDto ->
                                                Card(modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clickable {
                                                        when (selectingForOrigin) {
                                                            true -> {
                                                                viewModel.selectSuggestionAsOrigin(suggestion.placeId)
                                                                origin = suggestion.mainText
                                                            }
                                                            false -> {
                                                                viewModel.selectSuggestionAsDestination(suggestion.placeId)
                                                                destination = suggestion.mainText
                                                            }
                                                        }
                                                        viewModel.onQueryChanged("")
                                                    }
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                        Text(suggestion.mainText, fontWeight = FontWeight.SemiBold)
                                                        Spacer(Modifier.height(4.dp))
                                                        Text(suggestion.description, fontSize = 12.sp, color = Grey)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Button(onClick = { viewModel.findRoute() }, Modifier.fillMaxWidth().padding(bottom = 24.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(vertical = 14.dp)) {
                                        Icon(Icons.Default.Search, null, Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Find Route", fontWeight = FontWeight.SemiBold)
                                    }

                                }
                            }
                        }
                    }
                }
            }

            // Route options bar: show only when NOT simulating and steps sheet not visible
            if (state.routes.isNotEmpty() && !state.isSimulating && !showStepsSheet) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                    RouteOptionsBar(
                        routes = state.routes,
                        selectedIndex = state.selectedRouteIndex,
                        onSelect = { idx -> viewModel.selectRoute(idx) },
                        onShowSteps = { idx ->
                            viewModel.selectRoute(idx)
                            showStepsSheet = true
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 86.dp) // above bottom nav
                    )
                }
            }

            // Simulation instruction overlay
            if (state.isSimulating) {
                val selRoute = state.routes.getOrNull(state.selectedRouteIndex)
                val step = selRoute?.steps?.getOrNull(state.simStepIndex)
                Card(modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
                    .width(340.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF0D47A1))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(step?.instruction ?: "Driving...", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(step?.distance ?: "--", color = Color.White, fontSize = 12.sp)
                            Text(step?.duration ?: "--", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Steps bottom sheet (shows step-by-step instructions for currently selected route)
            if (showStepsSheet) {
                androidx.compose.material3.ModalBottomSheet(
                    onDismissRequest = { showStepsSheet = false },
                    sheetState = sheetState
                ) {
                    val sel = state.routes.getOrNull(state.selectedRouteIndex)
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)) {
                        Text("Route Steps", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        sel?.steps?.let { steps ->
                            LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                                items(steps) { step: com.doublezero.data.network.StepDto ->
                                    Column(modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)) {
                                        Text(step.instruction ?: "", fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text(step.distance ?: "--", fontSize = 12.sp, color = Grey)
                                            Text(step.duration ?: "--", fontSize = 12.sp, color = Grey)
                                        }
                                    }
                                }
                            }
                        } ?: Text("No steps available", color = Grey)
                    }
                }
            }
        }
    }
}


//@Composable
//private fun InfoCardsRow(modifier: Modifier = Modifier) {
//    Row(
//        modifier = modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        InfoCard(Modifier.weight(1f), Icons.Default.Speed, Blue, "Speed", "0 km/h", bgColor = BrightWhite)
//        InfoCard(Modifier.weight(1f), Icons.Default.Cloud, Blue, "Weather", "Clear", bgColor = BrightWhite)
//        InfoCard(Modifier.weight(1f), Icons.Default.Warning, DarkGreen, "Risk", "Safe", DarkGreen, GreenishGrey)
//    }
//}


//@Composable
//private fun InfoCard(modifier: Modifier = Modifier, icon: ImageVector, iconTint: Color, label: String, value: String, valueColor: Color = LocalContentColor.current, bgColor: Color) {
//    Column(
//        modifier = modifier
//            .clip(RoundedCornerShape(12.dp))
//            .background(bgColor)
//            .padding(12.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Icon(icon, label, tint = iconTint, modifier = Modifier.size(20.dp))
//        Spacer(Modifier.height(4.dp))
//        Text(label, fontSize = 11.sp, color = Grey)
//        Spacer(Modifier.height(2.dp))
//        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
//    }
//}

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
            focusedIndicatorColor = Blue, unfocusedIndicatorColor = LightGrey,
        )
    )
}

@Composable
private fun RouteSummaryCard(route: com.doublezero.data.network.RouteDto?, originName: String, destinationName: String, onDrive: () -> Unit, onReset: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrightWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Show origin and destination
            Column {
                Text("From", fontSize = 12.sp, color = Grey)
                Text(originName.ifBlank { "Unknown" }, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text("To", fontSize = 12.sp, color = Grey)
                Text(destinationName.ifBlank { "Unknown" }, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(8.dp))

            val eta = route?.duration ?: "--"
            val dist = route?.distance ?: "--"

            // Compute risk summary from route.riskPoints (fallback when backend doesn't provide a precomputed summary)
            val riskText = computeRiskSummary(route)

            RouteSummaryInfoRow(Icons.Default.Schedule, BlueishWhite, Blue, "Estimated Arrival", eta)
            RouteSummaryInfoRow(Icons.Default.Map, BlueishWhite, Blue, "Total Distance", dist)
            RouteSummaryInfoRow(Icons.Default.Warning, GreenishGrey, DarkGreen, "Risk Summary", riskText)
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Drive button starts simulation
                Button(onClick = { onDrive() }, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = DarkGreen), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
                    Icon(Icons.Default.Navigation, null, Modifier.size(18.dp), tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Drive", fontWeight = FontWeight.SemiBold, color = Color.White)
                }

                // Secondary button to allow changing/resetting the route/search
                androidx.compose.material3.OutlinedButton(onClick = { onReset() }, Modifier.align(Alignment.CenterVertically)) {
                    Icon(Icons.Default.Close, null, Modifier.size(16.dp), tint = Grey)
                    Spacer(Modifier.width(6.dp))
                    Text("Change Route", fontWeight = FontWeight.SemiBold, color = Grey)
                }
            }
        }
    }
}

// Helper to compute a short risk summary string from riskPoints or server-provided summary
private fun computeRiskSummary(route: com.doublezero.data.network.RouteDto?): String {
    if (route == null) return "Risk data not available"

    // 1) Prefer structured server-provided summary if present
    route.riskSummary?.let { rs ->
        val level = rs.level ?: "Unknown"
        val maxW = rs.maxWeight ?: rs.avgWeight ?: 0.0
        val hotspots = rs.hotspotCount ?: 0
        val msg = rs.message?.takeIf { it.isNotBlank() }
        return msg ?: "$level risk — $hotspots hotspot(s) (max ${"%.2f".format(maxW)})"
    }

    // 2) Prefer server-provided text summary if available
    route.riskSummaryText?.takeIf { it.isNotBlank() }?.let { return it }

    // 3) Fallback: compute from riskPoints array (client-side)
    val rps = route.riskPoints
    if (rps.isNullOrEmpty()) return "Risk data not available"

    val weights = rps.map { it.weight.coerceIn(0.0, 1.0) }
    val avg = weights.average()
    val max = weights.maxOrNull() ?: 0.0
    val highCount = weights.count { it > 0.66 }

    val level = when {
        avg <= 0.33 -> "Low"
        avg <= 0.66 -> "Medium"
        else -> "High"
    }

    val hotspotsText = if (highCount > 0) "$highCount hotspot(s)" else "No major hotspots"
    return "$level risk — $hotspotsText (max ${"%.2f".format(max)})"
}

@Composable
private fun RouteSummaryInfoRow(icon: ImageVector, iconBgColor: Color, iconTint: Color, label: String, value: String, valueColor: Color = LocalContentColor.current) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(iconBgColor), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(label, fontSize = 12.sp, color = Grey)
            Text(value, fontWeight = FontWeight.SemiBold, color = valueColor)
        }
    }
}

@Composable
private fun MapPlaceholder(modifier: Modifier = Modifier) {
    val gradient = Brush.verticalGradient(listOf(GreenishGrey, BlueishWhite))
    Box(modifier = modifier.background(gradient), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Navigation, null, Modifier.size(64.dp).alpha(0.4f), tint = SomewhatGrey)
            Text("Map View", color = SomewhatGrey, fontSize = 14.sp, modifier = Modifier.padding(top = 12.dp))
            Text("Google Maps SDK Integration", color = SomewhatGrey, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

// New MapScreen composable using Maps Compose
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    encodedPolyline: String? = null,
    markers: List<LatLng> = emptyList(),
    // cameraPositionState is provided by the parent so it can control camera movements
    cameraPositionState: com.google.maps.android.compose.CameraPositionState,
    locationPermissionGranted: Boolean = false
) {

    val properties = com.google.maps.android.compose.MapProperties(
        isMyLocationEnabled = locationPermissionGranted,
        // Enable Google Traffic Layer by default
        isTrafficEnabled = true
    )
    val uiSettings = com.google.maps.android.compose.MapUiSettings(
        myLocationButtonEnabled = locationPermissionGranted
    )

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = properties,
        uiSettings = uiSettings
    ) {
        markers.forEach { latLng ->
            Marker(state = rememberUpdatedMarkerState(position = latLng), title = "Place")
        }

        encodedPolyline?.takeIf { it.isNotBlank() }?.let { enc ->
             val path = remember(enc) {
                PolyUtil.decode(enc).map { LatLng(it.latitude, it.longitude) }
            }
            // use same lighter gray for single encoded polyline when shown
            val darkGray = Color(0xFF9E9E9E)
             Polyline(points = path, color = darkGray, width = 6f)
        }
    }
}

// New helper to draw multiple routes and handle selection
@Composable
private fun MapScreenRoutes(
    modifier: Modifier = Modifier,
    routes: List<com.doublezero.data.network.RouteDto> = emptyList(),
    selectedIndex: Int = -1,
    onSelect: (Int) -> Unit = {},
    cameraPositionState: com.google.maps.android.compose.CameraPositionState,
    locationPermissionGranted: Boolean = false,
    simulatedPosition: LatLng? = null
) {
    val properties = com.google.maps.android.compose.MapProperties(
        isMyLocationEnabled = locationPermissionGranted,
        isTrafficEnabled = true
    )
    val uiSettings = com.google.maps.android.compose.MapUiSettings(
        myLocationButtonEnabled = locationPermissionGranted
    )

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = properties,
        uiSettings = uiSettings
    ) {
        routes.forEachIndexed { idx, route ->
            val enc = route.polyline ?: return@forEachIndexed
            val path = remember(enc) { PolyUtil.decode(enc).map { LatLng(it.latitude, it.longitude) } }

            if (path.isNotEmpty()) {
                val isSelected = idx == selectedIndex

                // Restore original color behavior: only the selected route is navy; others are gray
                val navy = Color(0xFF0D47A1)
                // lighter gray for alternate/unselected routes (requested)
                val gray = Color(0xFF9E9E9E)
                val defaultColor = if (isSelected) navy else gray
                val defaultWidth = if (isSelected) 12f else 8f
                val defaultZ = if (isSelected) 2f else 1f

                // If this route is selected and has riskPoints, render colored segments using risk weights
                val hasRisk = isSelected && (route.riskPoints?.isNotEmpty() == true)

                if (hasRisk) {
                    // draw base polyline in the default route color (navy for selected, gray otherwise)
                    Polyline(points = path, color = defaultColor, width = defaultWidth, zIndex = 1f)

                    // Prepare interpolated weights per path index
                    val rpList = route.riskPoints!!.sortedBy { it.pointIndex ?: Int.MAX_VALUE }
                    val n = path.size
                    val weights = DoubleArray(n) { 0.0 }

                    if (rpList.isNotEmpty()) {
                        // Map risk points to indices (use provided pointIndex when available)
                        val known = rpList.map { rp ->
                            val idx = rp.pointIndex?.coerceIn(0, n - 1) ?: findNearestIndex(path, rp.lat, rp.lon)
                            idx to rp.weight.coerceIn(0.0, 1.0)
                        }.sortedBy { it.first }

                        // Fill before first known with first weight
                        val firstIdx = known.first().first
                        val firstW = known.first().second
                        for (i in 0..firstIdx) weights[i] = firstW

                        // Interpolate between known points
                        for (k in 0 until known.size - 1) {
                            val (i1, w1) = known[k]
                            val (i2, w2) = known[k + 1]
                            if (i2 == i1) {
                                weights[i1] = (w1 + w2) / 2.0
                                continue
                            }
                            for (j in i1..i2) {
                                val t = (j - i1).toDouble() / (i2 - i1)
                                weights[j] = w1 * (1.0 - t) + w2 * t
                            }
                        }

                        // Fill after last known with last weight
                        val lastIdx = known.last().first
                        val lastW = known.last().second
                        for (i in lastIdx until n) weights[i] = lastW
                    }

                    // Now build contiguous polylines per color bucket for visual smoothness
                    if (n >= 2) {
                        var segStart = 0
                        var currentColor = mapWeightToColor((weights[0] + weights[1]) / 2.0)
                        val segWidth = (defaultWidth + 1f)

                        for (iSeg in 0 until n - 1) {
                            val segWeight = (weights[iSeg] + weights[iSeg + 1]) / 2.0
                            // Only show colored overlay for medium+ risk (weight > 0.33)
                            val segColor = mapWeightToColor(segWeight)
                            // Show overlay only for HIGH risk now to keep base route color dominant
                            val showOverlay = segWeight > 0.66
                            if (showOverlay && segColor != currentColor) {
                                // draw current segment batch
                                val seg = path.subList(segStart, iSeg + 1 + 1) // inclusive end
                                if (seg.size >= 2) {
                                    // Use a thin, semi-transparent HIGH-risk overlay so base route color remains dominant
                                    Polyline(points = seg, color = currentColor.copy(alpha = 0.25f), width = segWidth, zIndex = 3f, clickable = true, onClick = { onSelect(idx) })
                                }
                                segStart = iSeg + 1
                                currentColor = segColor
                            } else if (!showOverlay) {
                                // advance segStart when overlay isn't shown to avoid repeating old color batches
                                segStart = iSeg + 1
                                currentColor = mapWeightToColor((weights[segStart.coerceAtMost(n-1)] + weights[segStart.coerceAtMost(n-1)]) / 2.0)
                            }
                        }

                        // draw remaining
                        if (segStart < n - 1) {
                            val seg = path.subList(segStart, n)
                            if (seg.size >= 2) {
                                // Only draw final overlay if it's medium+ risk
                                val next = weights.getOrNull(segStart + 1) ?: weights[segStart]
                                val finalWeight = (weights[segStart] + next) / 2.0
                                if (finalWeight > 0.66) {
                                    Polyline(points = seg, color = currentColor.copy(alpha = 0.25f), width = segWidth, zIndex = 3f, clickable = true, onClick = { onSelect(idx) })
                                }
                            }
                        }
                    }
                } else {
                    Polyline(
                        points = path,
                        color = defaultColor,
                        width = defaultWidth,
                        zIndex = defaultZ,
                        clickable = true,
                        onClick = { onSelect(idx) }
                    )
                }

                if (isSelected) {
                    // keep existing tile overlay behavior as fallback or additional layer
                    val segmentedProviders = remember(path) {
                        RiskHeatmapUtils.createSegmentedHeatmapProviders(path)
                    }
                    segmentedProviders.forEach { (provider, _) ->
                        TileOverlay(
                            tileProvider = provider,
                            transparency = 0.6f,
                            zIndex = 3f
                        )
                    }
                }
            }
        }

        simulatedPosition?.let { sp ->
            Marker(state = rememberUpdatedMarkerState(position = sp), title = "You (sim)")
        }
    }
}

// Helper: map normalized weight (0..1) to heatmap color
private fun mapWeightToColor(weight: Double): Color {
    val w = weight.coerceIn(0.0, 1.0)
    return when {
        w <= 0.33 -> Color(0xFF00C853) // Green
        w <= 0.66 -> Color(0xFFFFEB3B) // Yellow
        else -> Color(0xFFFF5252) // Red
    }
}

// Helper: find nearest path index for a given lat/lon
private fun findNearestIndex(path: List<LatLng>, lat: Double, lon: Double): Int {
    if (path.isEmpty()) return 0
    var bestIdx = 0
    var bestDist = Double.MAX_VALUE
    for (i in path.indices) {
        val p = path[i]
        val dLat = p.latitude - lat
        val dLon = p.longitude - lon
        val distSq = dLat * dLat + dLon * dLon
        if (distSq < bestDist) {
            bestDist = distSq
            bestIdx = i
        }
    }
    return bestIdx
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            onNavigateToMyPage = {},
            onNavigateToHistory = {},
            onNavigateToSettings = {}
        )
    }
}

@Composable
private fun RouteOptionsBar(
    routes: List<com.doublezero.data.network.RouteDto>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onShowSteps: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (routes.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        routes.forEachIndexed { idx, route ->
            val selected = idx == selectedIndex
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(idx) },
                shape = RoundedCornerShape(12.dp),
                colors = if (selected) CardDefaults.cardColors(containerColor = Color(0xFF0D47A1)) else CardDefaults.cardColors(containerColor = BrightWhite)
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 10.dp, horizontal = 10.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(if (idx == 0) "Primary" else "Alternate", fontSize = 12.sp, color = if (selected) Color.White else Grey)
                    Spacer(Modifier.height(4.dp))
                    Text(route.duration ?: "--", fontWeight = FontWeight.SemiBold, color = if (selected) Color.White else Blue)
                    Text(route.distance ?: "--", fontSize = 12.sp, color = if (selected) Color.White else Grey)
                }
            }
        }
    }
}
