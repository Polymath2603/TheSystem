package com.neuraknight.thesystem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neuraknight.thesystem.ui.screens.common.DropdownSelector

@Composable
fun SetupScreen(onSetupComplete: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    val workoutOptions = listOf("beginner", "intermediate", "advanced")
    var workoutLevel by remember { mutableStateOf(workoutOptions[0]) }
    val goalOptions = listOf("balanced", "quick", "longterm")
    var goal by remember { mutableStateOf(goalOptions[0]) }
    val colorOptions = listOf("blue", "red", "green", "yellow", "purple", "cyan", "grey")
    var color by remember { mutableStateOf(colorOptions[0]) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to The System", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        Spacer(modifier = Modifier.height(16.dp))

        DropdownSelector("Workout Level", workoutOptions, workoutLevel) { workoutLevel = it }
        Spacer(modifier = Modifier.height(16.dp))

        DropdownSelector("Goals", goalOptions, goal) { goal = it }
        Spacer(modifier = Modifier.height(16.dp))

        DropdownSelector("Color", colorOptions, color) { color = it }
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { onSetupComplete(name, workoutLevel, goal, color) }) {
            Text("Start")
        }
    }
}
