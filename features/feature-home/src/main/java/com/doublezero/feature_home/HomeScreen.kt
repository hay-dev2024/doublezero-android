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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.google.android.gms.maps.CameraUpdateFactory // Added import

// New lifecycle/hilt imports
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.IntrinsicSize
import kotlinx.coroutines.delay
import kotlin.math.*


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

    // Simulation / turn-by-turn state
    var isSimulating by remember { mutableStateOf(false) }
    var simPosition by remember { mutableStateOf<LatLng?>(null) }
    var simStepIndex by remember { mutableStateOf(-1) }

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

    // Simulation: move along selected route polyline periodically
    LaunchedEffect(isSimulating, state.routes, state.selectedRouteIndex) {
        if (!isSimulating) return@LaunchedEffect
        val route = state.routes.getOrNull(state.selectedRouteIndex)
        if (route == null || route.polyline.isNullOrBlank()) {
            isSimulating = false
            return@LaunchedEffect
        }
        // decode polyline to points
        val pathPoints = try {
            PolyUtil.decode(route.polyline!!).map { LatLng(it.latitude, it.longitude) }
        } catch (e: Exception) {
            emptyList()
        }
        if (pathPoints.isEmpty()) {
            isSimulating = false
            return@LaunchedEffect
        }

        // iterate along the polyline points to simulate motion
        for (i in pathPoints.indices) {
            if (!isSimulating) break
            val p = pathPoints[i]
            simPosition = p
            // update current step by finding nearest step center
            val steps = route.steps ?: emptyList()
            var nearest = -1
            var nearestDist = Double.MAX_VALUE
            steps.forEachIndexed { sIdx, step ->
                val stepCenter = step.polyline?.takeIf { it.isNotBlank() }?.let { pl ->
                    try {
                        val pts = PolyUtil.decode(pl)
                        if (pts.isNotEmpty()) LatLng(pts[pts.size / 2].latitude, pts[pts.size / 2].longitude) else null
                    } catch (_: Exception) { null }
                }
                stepCenter?.let { sc ->
                    val d = distanceBetweenMeters(p.latitude, p.longitude, sc.latitude, sc.longitude)
                    if (d < nearestDist) {
                        nearest = sIdx
                        nearestDist = d
                    }
                }
            }
            simStepIndex = nearest

            // animate camera a bit to follow the simulated position
            try {
                // cameraPositionState is available in this scope (declared later) — we'll animate via a snapshot effect below after camera defined
            } catch (_: Exception) { }

            // sleep between points (speed tuning)
            delay(600L)
        }

        // finished
        isSimulating = false
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
        // create shared camera state here so HomeScreen can control camera movements
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(40.7128, -74.0060), 12f)
        }

        // keep camera follow effect for simPosition
        LaunchedEffect(simPosition) {
            simPosition?.let { sp ->
                try {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(sp, 16f))
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
            simulatedPosition = simPosition
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
                    state.selectedOrigin?.let {
                        cameraPositionState.animate(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(LatLng(it.lat, it.lon), 14f)))
                    }
                }
            } else {
                // no routes: if origin exists, center to origin
                state.selectedOrigin?.let {
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

        // Debug overlay: show how many routes are currently in state
        Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = BrightWhite)
        ) {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Routes: ", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Text(state.routes.size.toString(), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Blue)
            }
        }

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
                            Icon(Icons.Default.Close, "Close", tint = Grey)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    InfoCardsRow(modifier = Modifier.padding(bottom = 16.dp))

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
                            LazyColumn(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                                items(state.suggestions) { suggestion ->
                                    Card(modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            if (selectingForOrigin) {
                                                viewModel.selectSuggestionAsOrigin(suggestion.placeId)
                                                origin = suggestion.mainText
                                            } else {
                                                viewModel.selectSuggestionAsDestination(suggestion.placeId)
                                                destination = suggestion.mainText
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

                        AnimatedVisibility(
                            visible = showResult,
                            enter = fadeIn(tween(300)) + slideInVertically(tween(300), initialOffsetY = { it / 2 }),
                            exit = fadeOut()
                        ) {
                            val selectedRoute = state.routes.getOrNull(state.selectedRouteIndex)
                            RouteSummaryCard(route = selectedRoute, onConfirmRoute = { handleCloseSheet() })
                        }
                    }
                }
            }
        }

        // Route options bar: show only when NOT simulating
        if (state.routes.isNotEmpty() && !isSimulating) {
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

        // Simulation control FAB
        if (state.routes.isNotEmpty()) {
            val hasRoute = state.routes.getOrNull(state.selectedRouteIndex) != null
            if (hasRoute) {
                androidx.compose.material3.FloatingActionButton(
                    onClick = {
                        if (isSimulating) {
                            isSimulating = false
                        } else {
                            simStepIndex = -1
                            simPosition = null
                            isSimulating = true
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 140.dp),
                    containerColor = if (isSimulating) Color.Red else Blue
                ) {
                    Icon(Icons.Default.Navigation, null, tint = Color.White)
                }
            }
        }

        // Simulation instruction overlay
        if (isSimulating) {
            val selRoute = state.routes.getOrNull(state.selectedRouteIndex)
            val step = selRoute?.steps?.getOrNull(simStepIndex)
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
                            items(steps) { step ->
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


@Composable
private fun InfoCardsRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InfoCard(Modifier.weight(1f), Icons.Default.Speed, Blue, "Speed", "0 km/h", bgColor = BrightWhite)
        InfoCard(Modifier.weight(1f), Icons.Default.Cloud, Blue, "Weather", "Clear", bgColor = BrightWhite)
        InfoCard(Modifier.weight(1f), Icons.Default.Warning, DarkGreen, "Risk", "Safe", DarkGreen, GreenishGrey)
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
        Text(label, fontSize = 11.sp, color = Grey)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
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
            focusedIndicatorColor = Blue, unfocusedIndicatorColor = LightGrey,
        )
    )
}

@Composable
private fun RouteSummaryCard(route: com.doublezero.data.network.RouteDto?, onConfirmRoute: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrightWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Route Summary", fontWeight = FontWeight.SemiBold)
            val eta = route?.duration ?: "--"
            val dist = route?.distance ?: "--"
            val summary = route?.summary ?: "--"
            RouteSummaryInfoRow(Icons.Default.Schedule, BlueishWhite, Blue, "Estimated Arrival", eta)
            RouteSummaryInfoRow(Icons.Default.Map, BlueishWhite, Blue, "Total Distance", dist)
            RouteSummaryInfoRow(Icons.Default.CheckCircle, GreenishGrey, DarkGreen, "Route Summary", summary)
            Spacer(Modifier.height(4.dp))
            Button(onConfirmRoute, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = DarkGreen), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
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
        isMyLocationEnabled = locationPermissionGranted
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
            Polyline(points = path)
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
        isMyLocationEnabled = locationPermissionGranted
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
        // draw each route
        routes.forEachIndexed { idx, route ->
            val enc = route.polyline ?: return@forEachIndexed
            val path = remember(enc) { PolyUtil.decode(enc).map { LatLng(it.latitude, it.longitude) } }
            if (path.isNotEmpty()) {
                val isSelected = idx == selectedIndex
                val hasAlternatives = routes.size > 1
                val color = when {
                    isSelected -> Color(0xFF0D47A1) // dark blue
                    idx == 0 && hasAlternatives && selectedIndex != 0 -> Color(0xFF616161) // primary becomes gray when alt selected
                    idx == 0 -> Color(0xFF90CAF9) // primary unselected (no alt selected)
                    else -> Color(0xFFBDBDBD) // alternative unselected (lighter gray)
                }
                val width = if (isSelected) 12f else 6f
                Polyline(points = path, color = color, width = width, clickable = true, onClick = { onSelect(idx) })
            }
        }

        // draw simulated position marker if present
        simulatedPosition?.let { sp ->
            Marker(state = rememberUpdatedMarkerState(position = sp), title = "You (sim)")
        }
    }
}

// Bottom bar displaying primary and alternative route summaries (duration + distance)
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
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (idx == 0) "Primary" else "Alternate", fontSize = 12.sp, color = if (selected) Color.White else Grey)
                    Spacer(Modifier.height(4.dp))
                    Text(route.duration ?: "--", fontWeight = FontWeight.SemiBold, color = if (selected) Color.White else Blue)
                    Text(route.distance ?: "--", fontSize = 12.sp, color = if (selected) Color.White else Grey)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Details button opens steps sheet
                        Button(onClick = { onShowSteps(idx) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE))) {
                            Text("Details", fontSize = 12.sp)
                        }
                        Spacer(Modifier.width(6.dp))
                    }
                }
            }
        }
    }
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

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenSearchOpenPreview() {
    MaterialTheme {
        HomeScreen(
            openSearch = true,
            onNavigateToMyPage = {},
            onNavigateToHistory = {},
            onNavigateToSettings = {}
        )
    }
}

// helper: distance in meters between two lat/lon points
private fun distanceBetweenMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371000.0 // earth radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}
