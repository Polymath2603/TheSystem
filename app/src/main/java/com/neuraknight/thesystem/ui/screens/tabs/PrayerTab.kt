package com.neuraknight.thesystem.ui.screens.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun PrayerTab(viewModel: MainViewModel) {
    val prayers = viewModel.appData.prayers
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(60000) // Update every minute
        }
    }

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
            contentPadding = PaddingValues(bottom = 24.dp)
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
                        text = "[SYSTEM: SPIRITUAL CALIBRATION ACTIVE]",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            itemsIndexed(prayers) { index, prayer ->
                val prayerTime = prayer.prayerTime
                val isPast = prayerTime != null && prayerTime.time < currentTime
                val isFuture = prayerTime != null && prayerTime.time > currentTime
                
                val timeDisplay = when {
                    prayer.done -> "Past at ${prayer.time}"
                    isPast -> "Past at ${prayer.time}"
                    isFuture && prayerTime != null -> {
                        val timeLeft = prayerTime.time - currentTime
                        val hours = TimeUnit.MILLISECONDS.toHours(timeLeft)
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeft) % 60
                        if (hours > 0) "Coming in ${hours}h ${minutes}m" else "Coming in ${minutes}m"
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
                    icon = icon,
                    onToggle = { isDone ->
                        lastToggledIndex = index
                        lastToggledState = isDone
                        showUndoSnackbar = true
                        viewModel.togglePrayerDone(index, isDone)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun PrayerCard(
    name: String,
    timeDisplay: String,
    isDone: Boolean,
    icon: ImageVector,
    onToggle: (Boolean) -> Unit
) {
    val borderColor = when {
        isDone -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }
    val bgColor = when {
        isDone -> MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(32.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name.uppercase(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = timeDisplay,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        if (isDone) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onToggle(false) }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Undo",
                        tint = MaterialTheme.colorScheme.primary
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