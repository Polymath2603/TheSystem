package com.neuraknight.thesystem.ui.screens.tabs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitsTab(viewModel: MainViewModel) {
    val habits = viewModel.appData.habits
    var newHabitName by remember { mutableStateOf("") }
    
    // Separate done and undone habits
    val sortedHabits = remember(habits) {
        habits.sortedBy { it.done }
    }
    
    // Long press dialog state
    var showLongPressDialog by remember { mutableStateOf(false) }
    var selectedHabitIndex by remember { mutableStateOf(-1) }
    var selectedHabitName by remember { mutableStateOf("") }
    var editHabitName by remember { mutableStateOf("") }

    if (showLongPressDialog && selectedHabitIndex >= 0) {
        AlertDialog(
            onDismissRequest = { showLongPressDialog = false },
            title = { Text("Manage Habit") },
            text = {
                Column {
                    Text("Habit: ${selectedHabitName.uppercase()}")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = editHabitName,
                        onValueChange = { editHabitName = it },
                        label = { Text("Edit Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = {
                        viewModel.deleteHabit(selectedHabitIndex)
                        showLongPressDialog = false
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (editHabitName.isNotBlank()) {
                            viewModel.editHabit(selectedHabitIndex, editHabitName)
                        }
                        showLongPressDialog = false
                    }) {
                        Text("Save")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showLongPressDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // System message at top
        item {
            Surface(
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        RoundedCornerShape(4.dp)
                    )
            ) {
                Text(
                    text = "[SYSTEM: ACTIVE HABITS UNLOCKED]",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(12.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Habits list - done items at the end
        itemsIndexed(
            items = sortedHabits,
            key = { idx, habit -> "habit_${habit.name}_$idx" }
        ) { index, habit ->
            val actualIndex = habits.indexOf(habit)
            val borderColor = if (habit.done) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            val bgColor = if (habit.done) MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor, RoundedCornerShape(8.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .padding(12.dp)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = {
                            selectedHabitIndex = actualIndex
                            selectedHabitName = habit.name
                            editHabitName = habit.name
                            showLongPressDialog = true
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name.uppercase(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (habit.done) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                    )
                    if (habit.isCustom) {
                        Text(
                            text = "[CUSTOM QUEST]",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = habit.done,
                        onCheckedChange = { isChecked ->
                            viewModel.toggleHabitDone(actualIndex, isChecked)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.secondary,
                            uncheckedColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    if (habit.isCustom) {
                        IconButton(
                            onClick = { viewModel.deleteHabit(actualIndex) },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Habit", modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Add input at bottom
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = newHabitName,
                        onValueChange = { newHabitName = it },
                        placeholder = { Text("Add new habit...", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                    IconButton(
                        onClick = {
                            if (newHabitName.isNotBlank()) {
                                viewModel.addHabit(newHabitName)
                                newHabitName = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Habit")
                    }
                }
            }
        }
    }
}