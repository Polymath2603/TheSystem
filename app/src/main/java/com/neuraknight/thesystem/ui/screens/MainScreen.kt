package com.neuraknight.thesystem.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neuraknight.thesystem.ui.screens.tabs.*
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val appData by viewModel.appData.collectAsStateWithLifecycle()
    val settings = appData.settings
    
    val tabs = buildList {
        add("Workout")
        if (settings.showHabits) add("Habits")
        if (settings.showCustom) add("Custom")
        add("Profile")
    }
    
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        when (tabs[selectedTab]) {
            "Workout" -> WorkoutTab(viewModel)
            "Habits" -> HabitsTab(viewModel)
            "Custom" -> CustomTasksTab(viewModel)
            "Profile" -> ProfileTab(viewModel)
        }
    }
}