package com.neuraknight.thesystem.ui.screens.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neuraknight.thesystem.ui.components.CheckboxListItem
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel

@Composable
fun HabitsTab(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val appData by viewModel.appData.collectAsStateWithLifecycle()
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Daily Habits",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // ✅ Fixed: Proper checkbox state management
        itemsIndexed(appData.habits) { index, habit ->
            CheckboxListItem(
                text = habit.name,
                checked = habit.done,
                onCheckedChange = { isChecked ->
                    viewModel.toggleHabitDone(index, isChecked)
                }
            )
        }
    }
}