package com.neuraknight.thesystem.ui.screens

import android.net.Uri
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.neuraknight.thesystem.ui.screens.common.DropdownSelector

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetupScreen(onSetupComplete: (String, String, String, Int, String, Boolean, Boolean, String) -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    val goalOptions = listOf("balanced", "quick", "longterm")
    var goal by remember { mutableStateOf(goalOptions[0]) }
    var repCount by remember { mutableStateOf("") }
    val colorOptions = listOf("blue", "red", "green", "yellow", "purple", "cyan", "grey")
    var color by remember { mutableStateOf(colorOptions[0]) }
    var gender by remember { mutableStateOf("male") }
    var showPrayers by remember { mutableStateOf(false) }
    var showHabits by remember { mutableStateOf(true) }
    var pendingProfileImg by remember { mutableStateOf("") }
    var useLocation by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            useLocation = true
            context.getSharedPreferences("TheSystemApp", 0)
                .edit()
                .putBoolean("use_location", true)
                .apply()
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                if (inputStream != null) {
                    val bitmap = inputStream.use { stream ->
                        android.graphics.BitmapFactory.decodeStream(stream)
                    }
                    if (bitmap != null) {
                        val file = java.io.File(context.filesDir, "profile_image.jpg")
                        java.io.FileOutputStream(file).use { outputStream ->
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream)
                        }
                        pendingProfileImg = file.absolutePath
                    }
                }
            } catch (_: Exception) { }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("Welcome to The System", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .clickable { pickImageLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("Tap to set profile picture", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = gender == "male", onClick = { gender = "male" }, label = { Text("Male") }, leadingIcon = if (gender == "male") {{ Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }} else null, modifier = Modifier.weight(1f))
            FilterChip(selected = gender == "female", onClick = { gender = "female" }, label = { Text("Female") }, leadingIcon = if (gender == "female") {{ Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }} else null, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Enable Prayers", modifier = Modifier.weight(1f))
            Switch(checked = showPrayers, onCheckedChange = { showPrayers = it })
        }
        if (showPrayers) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (useLocation) {
                    Text(
                        "Location enabled",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        "Use my location for accurate prayer times?",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = {
                            locationPermissionLauncher.launch(
                                android.Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        }
                    ) {
                        Text("Use My Location", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Enable Habits", modifier = Modifier.weight(1f))
            Switch(checked = showHabits, onCheckedChange = { showHabits = it })
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = repCount,
            onValueChange = { repCount = it.filter { c -> c.isDigit() }.take(3) },
            label = { Text("How many pushups can you do in one set?") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            suffix = { Text("reps") }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text("This sets your starting level — don't guess, do a quick max set!", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(12.dp))

        DropdownSelector(label = "Goals", options = goalOptions, selected = goal, onSelected = { goal = it })
        Spacer(modifier = Modifier.height(12.dp))

        DropdownSelector(label = "Color", options = colorOptions, selected = color, onSelected = { color = it })
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onSetupComplete(name, goal, color, repCount.toIntOrNull() ?: 0, gender, showPrayers, showHabits, pendingProfileImg) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Start", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
