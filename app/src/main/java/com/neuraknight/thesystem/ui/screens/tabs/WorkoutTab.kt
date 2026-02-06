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
import com.neuraknight.thesystem.ui.components.ExerciseCard
import com.neuraknight.thesystem.ui.components.ProgressBar
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel

@Composable
fun WorkoutTab(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val appData by viewModel.appData.collectAsStateWithLifecycle()
    val quest = appData.quest
    val user = appData.user
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // XP Progress
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Level ${user.level}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ProgressBar(
                        current = user.xpProgress,
                        max = user.xpNeeded,
                        label = "XP Progress"
                    )
                }
            }
        }
        
        // Quest Status
        item {
            Text(
                text = if (quest.completed) "Quest Completed! ✅" else "Daily Quest",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // Exercises with FIXED LOGIC
        itemsIndexed(quest.exercises) { index, exercise ->
            ExerciseCard(
                name = exercise.name,
                amount = exercise.amount,
                done = exercise.done,
                isTimed = exercise.timed,
                onToggle = { isChecked ->
                    // ✅ Fixed: Direct toggle without listener recreation
                    viewModel.toggleQuestExercise(index, isChecked)
                },
                onTimerStart = if (exercise.timed) {
                    // ✅ Fixed: Proper timer start
                    { viewModel.startTimedExercise(index) }
                } else null
            )
        }
        
        // Quest Actions
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.resetQuest(false) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset Quest")
                }
                if (user.passcards > 0) {
                    Button(
                        onClick = { viewModel.usePasscard() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Use Passcard (${user.passcards})")
                    }
                }
            }
        }
    }
}