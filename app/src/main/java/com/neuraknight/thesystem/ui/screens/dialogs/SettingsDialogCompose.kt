package com.neuraknight.thesystem.ui.screens.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.neuraknight.thesystem.ui.screens.common.DropdownSelector
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel

@Composable
fun SettingsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(viewModel.appData.user.name) }
    var selectedColor by remember { mutableStateOf(viewModel.appData.settings.color) }
    var showHabits by remember { mutableStateOf(viewModel.appData.settings.showHabits) }
    var showCustom by remember { mutableStateOf(viewModel.appData.settings.showCustom) }

    val colors = listOf("blue", "red", "green", "yellow", "purple", "cyan", "grey")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                DropdownSelector(
                    label = "Theme Color",
                    options = colors,
                    selected = selectedColor,
                    onSelected = { selectedColor = it }
                )
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = showHabits, onCheckedChange = { showHabits = it })
                    Text("Show Habits")
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = showCustom, onCheckedChange = { showCustom = it })
                    Text("Show Custom Exercises")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.saveSettings(name, selectedColor, showHabits, showCustom)
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
