package com.neuraknight.thesystem.ui.screens.dialogs

import android.app.Activity
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
        "indigo" to Color(0xFF3F51B5)
    )

    val algorithmOptions = listOf(
        "default" to "Default (18°)",
        "mwl" to "Muslim World League",
        "isna" to "ISNA (North America)",
        "egypto" to "Egyptian Authority",
        "makkkah" to "Umm Al-Qura",
        "karachi" to "Karachi",
        "tehran" to "Tehran (Iran)",
        "jafari" to "Jafari (Wilayah)"
    )

    val difficultyOptions = listOf("beginner", "intermediate", "advanced")
    val daysOptions = listOf(1, 2, 3, 4, 5, 6, 7)
    val goalOptions = listOf("strength", "cardio", "durability", "biceps", "triceps", "legs", "chest", "back", "shoulders", "abs")
    val equipmentOptions = listOf("bodyweight", "dumbbell", "bar")

    val sheetState = rememberModalBottomSheetState()

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                val file = java.io.File(context.filesDir, "profile_image.jpg")
                val outputStream = java.io.FileOutputStream(file)
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.close()
                viewModel.appData.user.profileImg = file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
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
                Text("Location is auto-detected for accurate prayer times", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                viewModel.saveSettings(name = name, color = selectedColor, gender = gender, prayerAlgorithm = prayerAlgorithm, difficulty = difficulty, daysPerWeek = daysPerWeek, trainingGoals = selectedGoals, equipmentTypes = selectedEquipment, showPrayers = showPrayers)
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