package com.neuraknight.thesystem.ui.screens.dialogs

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val settings = viewModel.appData.settings
    val user = viewModel.appData.user

    var name by remember { mutableStateOf(user.name) }
    var selectedColor by remember { mutableStateOf(settings.color) }
    var gender by remember { mutableStateOf(user.gender) }
    var prayerAlgorithm by remember { mutableStateOf(settings.prayerAlgorithm) }
    var difficulty by remember { mutableStateOf(settings.difficulty) }
    var daysPerWeek by remember { mutableStateOf(settings.daysPerWeek) }
    var selectedGoals by remember { mutableStateOf(settings.trainingGoals.toMutableList()) }
    var selectedEquipment by remember { mutableStateOf(settings.equipmentTypes.toMutableList()) }
    var showPrayers by remember { mutableStateOf(settings.showPrayers) }
    var notificationsEnabled by remember { mutableStateOf(settings.notificationsEnabled) }
    var workoutReminderEnabled by remember { mutableStateOf(settings.workoutReminderEnabled) }
    var workoutReminderHour by remember { mutableIntStateOf(settings.workoutReminderHour) }
    var workoutReminderMinute by remember { mutableIntStateOf(settings.workoutReminderMinute) }
    var prayerNotificationsEnabled by remember { mutableStateOf(settings.prayerNotificationsEnabled) }
    var prayerNotificationLeadMinutes by remember { mutableIntStateOf(settings.prayerNotificationLeadMinutes) }
    var streakWarningEnabled by remember { mutableStateOf(settings.streakWarningEnabled) }
    var streakWarningHour by remember { mutableIntStateOf(settings.streakWarningHour) }
    var streakWarningMinute by remember { mutableIntStateOf(settings.streakWarningMinute) }

    var showWorkoutTimePicker by remember { mutableStateOf(false) }
    var showStreakTimePicker by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) notificationsEnabled = false
    }

    val colorOptions = listOf(
        "blue" to Color(0xFF2196F3),
        "red" to Color(0xFFF44336),
        "green" to Color(0xFF4CAF50),
        "yellow" to Color(0xFFFFEB3B),
        "purple" to Color(0xFF9C27B0),
        "cyan" to Color(0xFF00BCD4),
        "pink" to Color(0xFFE91E63),
        "orange" to Color(0xFFFF9800),
        "teal" to Color(0xFF009688),
        "indigo" to Color(0xFF3F51B5),
        "grey" to Color(0xFF607D8B)
    )

    val algorithmOptions = listOf(
        "default" to "Default (18°)",
        "mwl" to "Muslim World League",
        "isna" to "ISNA (North America)",
        "egypto" to "Egyptian Authority",
        "makkah" to "Umm Al-Qura",
        "karachi" to "Karachi",
        "tehran" to "Tehran (Iran)",
        "jafari" to "Jafari (Wilayah)"
    )

    val difficultyOptions = listOf("beginner", "intermediate", "advanced")
    val daysOptions = listOf(1, 2, 3, 4, 5, 6, 7)
    val goalOptions = listOf("strength", "cardio", "durability", "biceps", "triceps", "legs", "chest", "back", "shoulders", "abs")
    val equipmentOptions = listOf("bodyweight", "dumbbell", "bar")

    val sheetState = rememberModalBottomSheetState()

    var imageError by remember { mutableStateOf<String?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                if (inputStream != null) {
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                    if (bitmap != null) {
                        val file = java.io.File(context.filesDir, "profile_image.jpg")
                        java.io.FileOutputStream(file).use { outputStream ->
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream)
                        }
                        viewModel.appData.user.profileImg = file.absolutePath
                        imageError = null
                    } else {
                        imageError = "Could not decode image"
                    }
                } else {
                    imageError = "Could not open image"
                }
            } catch (e: Exception) {
                imageError = "Failed to save image: ${e.message}"
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SettingsSection(title = "Profile") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .clickable { pickImageLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Profile Picture", style = MaterialTheme.typography.bodyLarge)
                        Text("Tap to change", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        if (imageError != null) {
                            Text(imageError!!, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = gender == "male", onClick = { gender = "male" }, label = { Text("Male") }, leadingIcon = if (gender == "male") {{ Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }} else null, modifier = Modifier.weight(1f))
                    FilterChip(selected = gender == "female", onClick = { gender = "female" }, label = { Text("Female") }, leadingIcon = if (gender == "female") {{ Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }} else null, modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(title = "Appearance") {
                Text("Theme Color", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colorOptions.forEach { (colorName, colorValue) ->
                        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(colorValue).border(width = if (selectedColor == colorName) 2.dp else 1.dp, color = if (selectedColor == colorName) MaterialTheme.colorScheme.onSurface else Color.Gray.copy(alpha = 0.3f), shape = CircleShape).clickable { selectedColor = colorName }, contentAlignment = Alignment.Center) {
                            if (selectedColor == colorName) { Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Show Prayers Tab", modifier = Modifier.weight(1f))
                    Switch(checked = showPrayers, onCheckedChange = { showPrayers = it })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(title = "Notifications") {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Notifications", style = MaterialTheme.typography.bodyLarge)
                        Text("Workout, prayer, and streak alerts", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                    Switch(checked = notificationsEnabled, onCheckedChange = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                        notificationsEnabled = enabled
                    })
                }

                if (notificationsEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Workout Reminder
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Workout Reminder", style = MaterialTheme.typography.bodyLarge)
                            Text("Daily quest notification", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        Switch(checked = workoutReminderEnabled, onCheckedChange = { workoutReminderEnabled = it })
                    }

                    if (workoutReminderEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth().clickable { showWorkoutTimePicker = true }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reminder Time", modifier = Modifier.weight(1f))
                            Text("${workoutReminderHour.toString().padStart(2, '0')}:${workoutReminderMinute.toString().padStart(2, '0')}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Prayer Notifications
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Prayer Alerts", style = MaterialTheme.typography.bodyLarge)
                            Text("Notify before each prayer", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        Switch(checked = prayerNotificationsEnabled, onCheckedChange = { prayerNotificationsEnabled = it })
                    }

                    if (prayerNotificationsEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Lead Time", modifier = Modifier.weight(1f))
                            Text("${prayerNotificationLeadMinutes} min", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Slider(value = prayerNotificationLeadMinutes.toFloat(), onValueChange = { prayerNotificationLeadMinutes = it.toInt() }, valueRange = 0f..30f, steps = 5, modifier = Modifier.fillMaxWidth())
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Streak Warning
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Streak Warning", style = MaterialTheme.typography.bodyLarge)
                            Text("Evening reminder if streak at risk", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        Switch(checked = streakWarningEnabled, onCheckedChange = { streakWarningEnabled = it })
                    }

                    if (streakWarningEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth().clickable { showStreakTimePicker = true }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Warning Time", modifier = Modifier.weight(1f))
                            Text("${streakWarningHour.toString().padStart(2, '0')}:${streakWarningMinute.toString().padStart(2, '0')}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Time Picker Dialogs
            if (showWorkoutTimePicker) {
                val timePickerState = rememberTimePickerState(initialHour = workoutReminderHour, initialMinute = workoutReminderMinute)
                AlertDialog(
                    onDismissRequest = { showWorkoutTimePicker = false },
                    confirmButton = { TextButton(onClick = { workoutReminderHour = timePickerState.hour; workoutReminderMinute = timePickerState.minute; showWorkoutTimePicker = false }) { Text("OK") } },
                    dismissButton = { TextButton(onClick = { showWorkoutTimePicker = false }) { Text("Cancel") } },
                    text = { TimePicker(state = timePickerState) }
                )
            }

            if (showStreakTimePicker) {
                val timePickerState = rememberTimePickerState(initialHour = streakWarningHour, initialMinute = streakWarningMinute)
                AlertDialog(
                    onDismissRequest = { showStreakTimePicker = false },
                    confirmButton = { TextButton(onClick = { streakWarningHour = timePickerState.hour; streakWarningMinute = timePickerState.minute; showStreakTimePicker = false }) { Text("OK") } },
                    dismissButton = { TextButton(onClick = { showStreakTimePicker = false }) { Text("Cancel") } },
                    text = { TimePicker(state = timePickerState) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(title = "Training") {
                Text("Difficulty", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    difficultyOptions.forEach { diff ->
                        FilterChip(selected = difficulty == diff, onClick = { difficulty = diff }, label = { Text(diff.replaceFirstChar { it.uppercase() }) }, leadingIcon = if (difficulty == diff) {{ Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }} else null, modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Days per Week", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    daysOptions.forEach { day ->
                        FilterChip(selected = daysPerWeek == day, onClick = { daysPerWeek = day }, label = { Text("$day") }, modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Training Goals (muscle focus)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    goalOptions.forEach { goal ->
                        FilterChip(selected = selectedGoals.contains(goal), onClick = {
                            selectedGoals = if (selectedGoals.contains(goal)) selectedGoals.toMutableList().apply { remove(goal) } else selectedGoals.toMutableList().apply { add(goal) }
                        }, label = { Text(goal.replaceFirstChar { it.uppercase() }) })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Equipment Available", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    equipmentOptions.forEach { equipment ->
                        FilterChip(selected = selectedEquipment.contains(equipment), onClick = {
                            selectedEquipment = if (selectedEquipment.contains(equipment)) selectedEquipment.toMutableList().apply { remove(equipment) } else selectedEquipment.toMutableList().apply { add(equipment) }
                        }, label = { Text(equipment.replaceFirstChar { it.uppercase() }) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(title = "Prayer Times") {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(value = algorithmOptions.find { it.first == prayerAlgorithm }?.second ?: "Default", onValueChange = {}, readOnly = true, label = { Text("Calculation Method") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        algorithmOptions.forEach { (value, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = { prayerAlgorithm = value; expanded = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Prayer times use default coordinates. Location detection coming in a future update.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                viewModel.saveSettings(name = name, color = selectedColor, gender = gender, prayerAlgorithm = prayerAlgorithm, difficulty = difficulty, daysPerWeek = daysPerWeek, trainingGoals = selectedGoals, equipmentTypes = selectedEquipment, showPrayers = showPrayers, notificationsEnabled = notificationsEnabled, workoutReminderEnabled = workoutReminderEnabled, workoutReminderHour = workoutReminderHour, workoutReminderMinute = workoutReminderMinute, prayerNotificationsEnabled = prayerNotificationsEnabled, prayerNotificationLeadMinutes = prayerNotificationLeadMinutes, streakWarningEnabled = streakWarningEnabled, streakWarningHour = streakWarningHour, streakWarningMinute = streakWarningMinute)
                if (selectedColor != settings.color) { (context as? Activity)?.recreate() }
                onDismiss()
            }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Settings", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(12.dp)) { content() }
        }
    }
}