package com.neuraknight.thesystem.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.neuraknight.thesystem.data.models.AppData
import com.neuraknight.thesystem.data.models.Settings
import com.neuraknight.thesystem.data.repository.DataRepository
import com.neuraknight.thesystem.ui.theme.TheSystemTheme
import java.io.File
import java.io.FileOutputStream

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = DataRepository(this)
        val initialData = repository.loadData()

        setContent {
            TheSystemTheme(themeColor = initialData.settings.color) {
                SettingsScreen(
                    initialData = initialData,
                    onSave = { updated ->
                        repository.saveData(updated)
                        setResult(Activity.RESULT_OK)
                        finish()
                    },
                    onCancel = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SettingsScreen(
    initialData: AppData,
    onSave: (AppData) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(initialData.user.name) }
    var selectedColor by remember { mutableStateOf(initialData.settings.color) }
    var gender by remember { mutableStateOf(initialData.user.gender) }
    var prayerAlgorithm by remember { mutableStateOf(initialData.settings.prayerAlgorithm) }

    var workoutDays by remember { mutableStateOf(initialData.settings.workoutDays.toMutableList()) }
    var selectedGoals by remember { mutableStateOf(initialData.settings.trainingGoals.toMutableList()) }
    var selectedEquipment by remember { mutableStateOf(initialData.settings.equipmentTypes.toMutableList()) }
    var showPrayers by remember { mutableStateOf(initialData.settings.showPrayers) }
    var showHabits by remember { mutableStateOf(initialData.settings.showHabits) }
    var notificationsEnabled by remember { mutableStateOf(initialData.settings.notificationsEnabled) }
    var workoutReminderEnabled by remember { mutableStateOf(initialData.settings.workoutReminderEnabled) }
    var workoutReminderHour by remember { mutableIntStateOf(initialData.settings.workoutReminderHour) }
    var workoutReminderMinute by remember { mutableIntStateOf(initialData.settings.workoutReminderMinute) }
    var prayerLatitude by remember { mutableStateOf(initialData.settings.prayerLatitude.toString()) }
    var prayerLongitude by remember { mutableStateOf(initialData.settings.prayerLongitude.toString()) }
    var prayerNotificationsEnabled by remember { mutableStateOf(initialData.settings.prayerNotificationsEnabled) }
    var prayerNotificationLeadMinutes by remember { mutableIntStateOf(initialData.settings.prayerNotificationLeadMinutes) }
    var streakWarningEnabled by remember { mutableStateOf(initialData.settings.streakWarningEnabled) }
    var streakWarningHour by remember { mutableIntStateOf(initialData.settings.streakWarningHour) }
    var streakWarningMinute by remember { mutableIntStateOf(initialData.settings.streakWarningMinute) }
    var imageError by remember { mutableStateOf<String?>(null) }
    var pendingProfileImg by remember { mutableStateOf<String?>(null) }
    var profileBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var showWorkoutTimePicker by remember { mutableStateOf(false) }
    var showStreakTimePicker by remember { mutableStateOf(false) }

    // Load initial profile image
    LaunchedEffect(Unit) {
        val path = initialData.user.profileImg
        if (path.isNotEmpty() && File(path).exists()) {
            profileBitmap = android.graphics.BitmapFactory.decodeFile(path)
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) notificationsEnabled = false
    }

    // Image picker – after picking, launch crop
    val cropImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val bitmap = inputStream.use { android.graphics.BitmapFactory.decodeStream(it) }
                    if (bitmap != null) {
                        val file = File(context.filesDir, "profile_image.jpg")
                        FileOutputStream(file).use { out ->
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                        }
                        pendingProfileImg = file.absolutePath
                        profileBitmap = bitmap
                        imageError = null
                    } else {
                        imageError = "Could not decode image"
                    }
                }
            } catch (e: Exception) {
                imageError = "Failed: ${e.message}"
            }
        }
    }

    val colorOptions = listOf(
        "blue" to Color(0xFF00E5FF),
        "red" to Color(0xFFFF1744),
        "green" to Color(0xFF00E676),
        "yellow" to Color(0xFFFFEA00),
        "purple" to Color(0xFFD500F9),
        "cyan" to Color(0xFF00B8D4),
        "pink" to Color(0xFFFF4081),
        "orange" to Color(0xFFFF6D00),
        "teal" to Color(0xFF1DE9B6),
        "indigo" to Color(0xFF536DFE),
        "grey" to Color(0xFF607D8B)
    )

    val algorithmOptions = listOf(
        "default" to "Default (18\u00b0)",
        "mwl" to "Muslim World League",
        "isna" to "ISNA (North America)",
        "egypto" to "Egyptian Authority",
        "makkah" to "Umm Al-Qura",
        "karachi" to "Karachi",
        "tehran" to "Tehran (Iran)",
        "jafari" to "Jafari (Wilayah)"
    )

    val dayLabels = listOf("Mon" to 1, "Tue" to 2, "Wed" to 3, "Thu" to 4, "Fri" to 5, "Sat" to 6, "Sun" to 7)
    val goalOptions = listOf("strength", "cardio", "durability", "biceps", "triceps", "legs", "chest", "back", "shoulders", "abs")
    val equipmentOptions = listOf("bodyweight", "dumbbell", "bar", "kettlebell", "resistance_band", "cable", "bench", "plate")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SETTINGS", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

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
                            .clickable { cropImageLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileBitmap != null) {
                            Image(
                                bitmap = profileBitmap!!.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
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
                        val checkTint = when (colorName) {
                            "blue", "green", "yellow", "cyan", "teal" -> Color.Black
                            else -> Color.White
                        }
                        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(colorValue).border(width = if (selectedColor == colorName) 2.dp else 1.dp, color = if (selectedColor == colorName) MaterialTheme.colorScheme.onSurface else Color.Gray.copy(alpha = 0.3f), shape = CircleShape).clickable { selectedColor = colorName }, contentAlignment = Alignment.Center) {
                            if (selectedColor == colorName) { Icon(Icons.Default.Check, contentDescription = null, tint = checkTint, modifier = Modifier.size(16.dp)) }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Show Habits Tab", modifier = Modifier.weight(1f))
                    Switch(checked = showHabits, onCheckedChange = { showHabits = it })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(title = "Prayer Times") {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show Prayers Tab", style = MaterialTheme.typography.bodyLarge)
                        Text("Fajr, Dhuhr, Asr, Maghrib, Isha tracking", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                    Switch(checked = showPrayers, onCheckedChange = { showPrayers = it })
                }
                if (showPrayers) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
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
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = prayerLatitude, onValueChange = { prayerLatitude = it }, label = { Text("Latitude") }, modifier = Modifier.weight(1f), singleLine = true)
                        Spacer(modifier = Modifier.width(4.dp))
                        OutlinedTextField(value = prayerLongitude, onValueChange = { prayerLongitude = it }, label = { Text("Longitude") }, modifier = Modifier.weight(1f), singleLine = true)
                        Spacer(modifier = Modifier.width(4.dp))
                        LocationButton(
                            onLocation = { lat, lng ->
                                prayerLatitude = lat.toString()
                                prayerLongitude = lng.toString()
                            },
                            onError = { imageError = it }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Use your current location coordinates for accurate prayer times.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(title = "Training") {
                Text("Workout Days", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    dayLabels.forEach { (label, dayNum) ->
                        FilterChip(
                            selected = workoutDays.contains(dayNum),
                            onClick = {
                                workoutDays = if (workoutDays.contains(dayNum))
                                    workoutDays.toMutableList().apply { remove(dayNum) }
                                else
                                    workoutDays.toMutableList().apply { add(dayNum) }
                            },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f)
                        )
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
                    if (showPrayers) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
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
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
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

            Spacer(modifier = Modifier.height(24.dp))

            val currentProfilePath = pendingProfileImg ?: initialData.user.profileImg

            Button(onClick = {
                val lat = prayerLatitude.toDoubleOrNull() ?: initialData.settings.prayerLatitude
                val lng = prayerLongitude.toDoubleOrNull() ?: initialData.settings.prayerLongitude
                val updated = initialData.copy(
                    user = initialData.user.copy(
                        name = name.ifEmpty { "Unknown" },
                        gender = gender,
                        profileImg = currentProfilePath
                    ),
                    settings = Settings(
                        color = selectedColor,
                        prayerAlgorithm = prayerAlgorithm,
                        workoutDays = workoutDays,
                        trainingGoals = selectedGoals,
                        equipmentTypes = selectedEquipment,
                        showPrayers = showPrayers,
                        showHabits = showHabits,
                        prayerLatitude = lat,
                        prayerLongitude = lng,
                        notificationsEnabled = notificationsEnabled,
                        workoutReminderEnabled = workoutReminderEnabled,
                        workoutReminderHour = workoutReminderHour,
                        workoutReminderMinute = workoutReminderMinute,
                        prayerNotificationsEnabled = prayerNotificationsEnabled,
                        prayerNotificationLeadMinutes = prayerNotificationLeadMinutes,
                        streakWarningEnabled = streakWarningEnabled,
                        streakWarningHour = streakWarningHour,
                        streakWarningMinute = streakWarningMinute
                    )
                )
                onSave(updated)
            }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Settings", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LocationButton(onLocation: (Double, Double) -> Unit, onError: (String) -> Unit) {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.all { it.value }) {
            fetchLocation(context, onLocation, onError)
        } else {
            onError("Location permission denied")
        }
    }

    IconButton(
        onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                if (fineGranted == PackageManager.PERMISSION_GRANTED || coarseGranted == PackageManager.PERMISSION_GRANTED) {
                    fetchLocation(context, onLocation, onError)
                } else {
                    permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }
            } else {
                fetchLocation(context, onLocation, onError)
            }
        }
    ) {
        Icon(Icons.Default.MyLocation, contentDescription = "Fetch location", tint = MaterialTheme.colorScheme.primary)
    }
}

private fun fetchLocation(context: android.content.Context, onLocation: (Double, Double) -> Unit, onError: (String) -> Unit) {
    try {
        val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        val providers = locationManager.getProviders(true)
        var location: android.location.Location? = null
        for (provider in providers) {
            location = locationManager.getLastKnownLocation(provider)
            if (location != null) break
        }
        if (location != null) {
            onLocation(location.latitude, location.longitude)
        } else {
            onError("No recent location found. Open Maps and try again.")
        }
    } catch (e: SecurityException) {
        onError("Location permission required")
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
