package com.neuraknight.thesystem.ui.screens.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun WorkoutTab(viewModel: MainViewModel) {
    val quest = viewModel.appData.quest
    val user = viewModel.appData.user

    var timeLeft by remember { mutableStateOf(0L) }
    LaunchedEffect(key1 = quest.nextReset) {
        while (true) {
            timeLeft = (quest.nextReset - System.currentTimeMillis()).coerceAtLeast(0)
            if (timeLeft <= 0) {
                viewModel.resetQuest(force = false)
            }
            delay(1000)
        }
    }

    LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
        item {
            Text(
                "[Daily Quest: strength training has arrived.]",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }

        itemsIndexed(quest.exercises) { index, exercise ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(exercise.name.replaceFirstChar { it.uppercase() }, modifier = Modifier.weight(1f))
                Text("[${if (exercise.done) exercise.amount else 0}/${exercise.amount}]")
                Spacer(Modifier.width(8.dp))

                if (exercise.timed) {
                    Button(
                        onClick = { viewModel.startTimedExercise(index) },
                        enabled = !exercise.done
                    ) {
                        Text(if (exercise.done) "Completed" else if (exercise.amount > 0) "${exercise.amount}s" else "Start")
                    }
                } else {
                    Checkbox(
                        checked = exercise.done,
                        onCheckedChange = { isChecked ->
                            viewModel.toggleQuestExercise(index, isChecked)
                        }
                    )
                }
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Time until reset: ${formatTime(timeLeft)}")
                Text(
                    "Warning: failure to complete the daily quest will result in an appropriate penalty.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(16.dp))

                if (quest.completed) {
                    Button(onClick = { viewModel.resetQuest(force = true) }) {
                        Text("Reset Now")
                    }
                } else if (user.passcards > 0) {
                    Button(onClick = { viewModel.usePasscard() }) {
                        Text("Use Passcard (${user.passcards})")
                    }
                }
            }
        }
    }
}

fun formatTime(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
