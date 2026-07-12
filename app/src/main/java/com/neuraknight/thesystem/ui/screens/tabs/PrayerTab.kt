package com.neuraknight.thesystem.ui.screens.tabs

import android.Manifest
import android.content.Context
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun PrayerTab(viewModel: MainViewModel) {
    val context = LocalContext.current
    val prayers = viewModel.appData.prayers
    val settings = viewModel.appData.settings
    val isDefaultLocation =
        settings.prayerLatitude == 51.5074 && settings.prayerLongitude == -0.1278

    var showLocationPrompt by remember { mutableStateOf(isDefaultLocation) }
    var latText by remember { mutableStateOf("") }
    var lonText by remember { mutableStateOf("") }
    var locationError by remember { mutableStateOf<String?>(null) }
    var isGettingLocation by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        isGettingLocation = false
        if (granted) {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var bestLat: Double? = null
            var bestLon: Double? = null

            try {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { loc ->
                    bestLat = loc.latitude
                    bestLon = loc.longitude
                }
            } catch (_: SecurityException) {}

            if (bestLat == null) {
                try {
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.let { loc ->
                        bestLat = loc.latitude
                        bestLon = loc.longitude
                    }
                } catch (_: SecurityException) {}
            }

            if (bestLat != null && bestLon != null) {
                viewModel.updatePrayerLocation(bestLat!!, bestLon!!)
                showLocationPrompt = false
                locationError = null
            } else {
                locationError = "Could not get location. Try manual entry or USE DEFAULT."
            }
        } else {
            locationError = "Location permission denied. Enter coordinates manually or use default."
        }
    }

    if (showLocationPrompt) {
        LocationPromptContent(
            latText = latText,
            onLatChange = { latText = it },
            lonText = lonText,
            onLonChange = { lonText = it },
            locationError = locationError,
            isGettingLocation = isGettingLocation,
            onUseMyLocation = {
                isGettingLocation = true
                locationError = null
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            },
            onApplyManual = {
                locationError = null
                val lat = latText.toDoubleOrNull()
                val lon = lonText.toDoubleOrNull()
                if (lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0) {
                    viewModel.updatePrayerLocation(lat, lon)
                    showLocationPrompt = false
                } else {
                    locationError = "Invalid coordinates. Lat: -90 to 90, Lon: -180 to 180."
                }
            },
            onUseDefault = {
                showLocationPrompt = false
                locationError = null
            }
        )
    } else {
        PrayerListContent(viewModel, prayers)
    }
}

@Composable
private fun LocationPromptContent(
    latText: String,
    onLatChange: (String) -> Unit,
    lonText: String,
    onLonChange: (String) -> Unit,
    locationError: String?,
    isGettingLocation: Boolean,
    onUseMyLocation: () -> Unit,
    onApplyManual: () -> Unit,
    onUseDefault: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp)
                        )
                ) {
                    Text(
                        text = "[SYSTEM: SPIRITUAL CALIBRATION REQUIRED]",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Location card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MyLocation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Set Your Location",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Prayer times are calculated based on your location. Enable GPS or enter coordinates to get accurate times for your area.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // USE MY LOCATION button
                Button(
                    onClick = onUseMyLocation,
                    enabled = !isGettingLocation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isGettingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Getting location...")
                    } else {
                        Icon(
                            imageVector = Icons.Filled.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "USE MY LOCATION",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Divider with text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "  or enter manually  ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = latText,
                    onValueChange = onLatChange,
                    label = { Text("Latitude (-90 to 90)") },
                    placeholder = { Text("e.g. 51.5074") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = lonText,
                    onValueChange = onLonChange,
                    label = { Text("Longitude (-180 to 180)") },
                    placeholder = { Text("e.g. -0.1278") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onApplyManual,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "APPLY COORDINATES",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Error display
                if (locationError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = locationError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(onClick = onUseDefault) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "USE DEFAULT",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PrayerListContent(viewModel: MainViewModel, prayers: List<com.neuraknight.thesystem.data.models.Prayer>) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(60000) // Update every minute
        }
    }

    val completedCount = prayers.count { it.done }
    val totalCount = prayers.size

    // For undo functionality
    var lastToggledIndex by remember { mutableStateOf(-1) }
    var lastToggledState by remember { mutableStateOf(false) }
    var showUndoSnackbar by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showUndoSnackbar) {
        if (showUndoSnackbar) {
            val result = snackbarHostState.showSnackbar(
                message = "Prayer marked",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed && lastToggledIndex >= 0) {
                viewModel.togglePrayerDone(lastToggledIndex, !lastToggledState)
            }
            showUndoSnackbar = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                // System header
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp)
                        )
                ) {
                    Text(
                        text = "[SYSTEM: SPIRITUAL CALIBRATION ACTIVE]",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Progress summary card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today's Progress",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$completedCount / $totalCount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { if (totalCount > 0) completedCount.toFloat() / totalCount else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
            }

            itemsIndexed(prayers) { index, prayer ->
                val prayerTime = prayer.prayerTime
                val isPast = prayerTime != null && prayerTime.time < currentTime
                val isFuture = prayerTime != null && prayerTime.time > currentTime
                val isNext = !prayer.done && isFuture && prayers.take(index).all { it.done || (it.prayerTime?.time ?: 0) > (prayerTime?.time ?: 0) } &&
                    prayers.subList(0, index).none { !it.done && (it.prayerTime?.time ?: 0) < (prayerTime?.time ?: 0) }

                val timeDisplay = when {
                    prayer.done -> "Completed at ${prayer.time}"
                    isPast -> "Missed at ${prayer.time}"
                    isFuture -> {
                        val timeLeft = prayerTime.time - currentTime
                        val hours = TimeUnit.MILLISECONDS.toHours(timeLeft)
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeft) % 60
                        val countdown = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                        "${prayer.time}  ·  ${countdown} remaining"
                    }
                    else -> "Scheduled: ${prayer.time}"
                }

                val icon = when (prayer.name) {
                    "Fajr" -> Icons.Filled.WbTwilight
                    "Dhuhr" -> Icons.Filled.WbSunny
                    "Asr" -> Icons.Filled.WbCloudy
                    "Maghrib" -> Icons.Filled.WbSunny
                    "Isha" -> Icons.Filled.Bedtime
                    else -> Icons.Filled.WbSunny
                }

                PrayerCard(
                    name = prayer.name,
                    timeDisplay = timeDisplay,
                    isDone = prayer.done,
                    isNext = isNext,
                    icon = icon,
                    onToggle = { isDone ->
                        lastToggledIndex = index
                        lastToggledState = isDone
                        showUndoSnackbar = true
                        viewModel.togglePrayerDone(index, isDone)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun PrayerCard(
    name: String,
    timeDisplay: String,
    isDone: Boolean,
    isNext: Boolean = false,
    icon: ImageVector,
    onToggle: (Boolean) -> Unit
) {
    val borderColor = when {
        isDone -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        isNext -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }
    val bgColor = when {
        isDone -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        isNext -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.06f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(if (isNext) 2.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isDone) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name.uppercase(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isDone -> MaterialTheme.colorScheme.primary
                            isNext -> MaterialTheme.colorScheme.onSurface
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (isNext) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "NEXT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = timeDisplay,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isDone) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onToggle(false) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Undo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Checkbox(
                        checked = true,
                        onCheckedChange = null,
                        enabled = false,
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            } else {
                Checkbox(
                    checked = false,
                    onCheckedChange = { onToggle(true) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }
    }
}
