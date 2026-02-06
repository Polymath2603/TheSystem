package com.neuraknight.thesystem.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel
import java.io.File

@Composable
fun SetupScreen(
    viewModel: MainViewModel,
    onImagePick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var selectedWorkout by remember { mutableIntStateOf(0) }
    var selectedGoal by remember { mutableIntStateOf(0) }
    var selectedColor by remember { mutableIntStateOf(0) }
    
    val workoutLevels = listOf("Beginner", "Intermediate", "Advanced")
    val goals = listOf("Balanced", "Quick Progress", "Long-term")
    val colors = listOf("Blue", "Red", "Green", "Yellow", "Purple", "Cyan", "Grey")
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to The System",
            style = MaterialTheme.typography.headlineLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Profile Image Picker
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            onClick = onImagePick,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("Tap to add photo")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Name Input
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Your Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Workout Level
        Text("Workout Level", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            workoutLevels.forEachIndexed { index, level ->
                FilterChip(
                    selected = selectedWorkout == index,
                    onClick = { selectedWorkout = index },
                    label = { Text(level) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Goal
        Text("Goal", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            goals.forEachIndexed { index, goal ->
                FilterChip(
                    selected = selectedGoal == index,
                    onClick = { selectedGoal = index },
                    label = { Text(goal) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Color Theme
        Text("Theme Color", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            colors.take(4).forEachIndexed { index, color ->
                FilterChip(
                    selected = selectedColor == index,
                    onClick = { selectedColor = index },
                    label = { Text(color) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            colors.drop(4).forEachIndexed { index, color ->
                FilterChip(
                    selected = selectedColor == index + 4,
                    onClick = { selectedColor = index + 4 },
                    label = { Text(color) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Start Button
        Button(
            onClick = {
                viewModel.completeSetup(
                    name = name.ifEmpty { "Unknown" },
                    workoutLevel = workoutLevels[selectedWorkout].lowercase(),
                    goal = goals[selectedGoal].lowercase().replace(" ", ""),
                    color = colors[selectedColor].lowercase()
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Your Journey")
        }
    }
}