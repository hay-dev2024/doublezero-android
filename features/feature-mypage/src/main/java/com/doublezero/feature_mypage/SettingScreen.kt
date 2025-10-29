package com.doublezero.feature_mypage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // Keep if needed by dropdowns etc.
import androidx.compose.foundation.layout.* // Keep layout imports
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
// Keep necessary Material Icons imports
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info // AlertCircle 대체
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Speed // Gauge 대체
import androidx.compose.material.icons.filled.VolumeUp // Volume2 대체
// Import only necessary Material 3 components
import androidx.compose.material3.* // Keep M3 imports
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
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class) // Needed for ExposedDropdownMenuBox
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    // onBackClicked is provided by NavHost, used by TopAppBar in MainActivity
    onBackClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Removed the Scaffold wrapper.
    // Box provides a scope for alignment (like for the toast).
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Apply the background color here
            .background(Color(0xFFF8F9FA))
        // Padding from MainActivity's Scaffold is handled by NavHost modifier.
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Apply additional content padding specific to this screen.
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(scrollState)
        ) {
            // Settings Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                SettingSwitchItem(
                    icon = Icons.Default.VolumeUp,
                    iconBgColor = Color(0xFFE3F2FD),
                    iconTint = Color(0xFF2196F3),
                    title = "TTS Voice Guidance",
                    description = "Enable voice navigation alerts",
                    checked = uiState.ttsEnabled,
                    onCheckedChange = viewModel::setTtsEnabled
                )
                SettingSwitchItem(
                    icon = Icons.Default.Info,
                    iconBgColor = Color(0xFFFFF3E0),
                    iconTint = Color(0xFFFF9800),
                    title = "AI Risk Display",
                    description = "Show real-time risk indicators",
                    checked = uiState.riskDisplayEnabled,
                    onCheckedChange = viewModel::setRiskDisplayEnabled
                )
                SettingDropdownItem(
                    icon = Icons.Default.Speed,
                    iconBgColor = Color(0xFFE8F5E9),
                    iconTint = Color(0xFF4CAF50),
                    title = "Speed Unit",
                    description = "Choose your preferred speed measurement",
                    selectedUnit = uiState.selectedSpeedUnit,
                    onUnitSelected = viewModel::setSelectedSpeedUnit
                )
            }

            // Save Button
            Button(
                onClick = viewModel::saveSettings,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Save Settings", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(20.dp)) // Bottom padding after button
        }

        // Save Confirmation Toast - Aligned within the parent Box
        AnimatedVisibility(
            visible = uiState.showSavedConfirmation,
            modifier = Modifier
                .align(Alignment.TopCenter)
                // Adjust top padding considering potential TopAppBar height from MainActivity
                .padding(top = 16.dp), // Reduced padding assuming TopAppBar pushes content down
            enter = fadeIn(tween(300)) + slideInVertically(tween(300)),
            exit = fadeOut(tween(300)) + slideOutVertically(tween(300))
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF4CAF50),
                contentColor = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Settings saved", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// --- Helper Composables (SettingCard, SettingSwitchItem, SettingDropdownItem) ---
// --- remain unchanged from the previous version. ---
// --- They do not contain Scaffold. ---

@Composable
private fun SettingCard(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    description: String,
    content: @Composable RowScope.() -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(
                    description,
                    fontSize = 13.sp,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingSwitchItem(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingCard(
        icon = icon,
        iconBgColor = iconBgColor,
        iconTint = iconTint,
        title = title,
        description = description
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF2196F3),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE0E0E0)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingDropdownItem(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    description: String,
    selectedUnit: SpeedUnit,
    onUnitSelected: (SpeedUnit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBgColor)
                    .padding(top = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 4.dp))
                Text(
                    description,
                    fontSize = 13.sp,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedUnit.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedContainerColor = Color(0xFFF8F9FA),
                            unfocusedContainerColor = Color(0xFFF8F9FA),
                            focusedBorderColor = Color(0xFF2196F3),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        SpeedUnit.entries.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit.displayName) },
                                onClick = {
                                    onUnitSelected(unit)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SettingsScreenPreview() {
    MaterialTheme {
        // Preview still works without Scaffold, showing just the settings content
        SettingsScreen(
            viewModel = SettingsViewModel(), // Use temporary ViewModel for preview
            onBackClicked = {}
        )
    }
}