package com.neuraknight.thesystem.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuraknight.thesystem.R
import com.neuraknight.thesystem.data.models.User
import com.neuraknight.thesystem.ui.screens.tabs.CustomTasksTab
import com.neuraknight.thesystem.ui.screens.tabs.HabitsTab
import com.neuraknight.thesystem.ui.screens.tabs.ProfileTab
import com.neuraknight.thesystem.ui.screens.tabs.WorkoutTab
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val appData = viewModel.appData
    var selectedTabIndex by remember { mutableStateOf(0) }

    val tabs = remember(appData.settings.showHabits, appData.settings.showCustom) {
        buildList {
            add("Workout")
            if (appData.settings.showHabits) add("Habits")
            if (appData.settings.showCustom) add("Custom")
            add("Profile")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("The System") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            UserInfoHeader(user = appData.user)
            Spacer(modifier = Modifier.height(8.dp))
            XPBar(progress = appData.user.xpProgress, needed = appData.user.xpNeeded)
            Spacer(modifier = Modifier.height(16.dp))

            val correctedIndex = selectedTabIndex.coerceIn(0, tabs.lastIndex)
            TabRow(selectedTabIndex = correctedIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = correctedIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            when (tabs.getOrNull(correctedIndex)) {
                "Workout" -> WorkoutTab(viewModel)
                "Habits" -> HabitsTab(viewModel)
                "Custom" -> CustomTasksTab(viewModel)
                "Profile" -> ProfileTab(viewModel)
            }
        }
    }
}

@Composable
fun UserInfoHeader(user: User) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "User Profile",
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(user.name, fontWeight = FontWeight.Bold)
            Text("Lv: ${user.level} | Class: ${user.`class`}")
            Text("Type: ${user.type}")
        }
    }
}

@Composable
fun XPBar(progress: Double, needed: Double) {
    val progressFraction = if (needed > 0) (progress / needed).toFloat() else 0f
    Column {
        Text("XP: ${progress.roundToInt()} / ${needed.roundToInt()}", fontSize = 14.sp)
        LinearProgressIndicator(
            progress = progressFraction,
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
        )
    }
}
