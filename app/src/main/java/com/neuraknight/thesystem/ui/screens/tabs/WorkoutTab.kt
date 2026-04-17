package com.neuraknight.thesystem.ui.screens.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            QuestHeader()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            DayCountdown(timeLeft = timeLeft, completed = quest.completed)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Show exercises or passcard message
        if (!quest.usedPasscard) {
            itemsIndexed(quest.exercises) { index, exercise ->
                ExerciseCard(
                    name = exercise.name,
                    amount = exercise.amount,
                    isDone = exercise.done,
                    isTimed = exercise.timed,
                    onToggle = { viewModel.toggleQuestExercise(index, it) },
                    onStartTimer = { viewModel.startTimedExercise(index) },
                    onSkipTimer = { viewModel.skipTimedExercise(index) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Extra exercises only if main completed
            if (quest.completed && quest.extraExercises.isNotEmpty()) {
                item {
                    Text(
                        text = "BONUS EXERCISES (OPTIONAL)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                itemsIndexed(quest.extraExercises) { index, exercise ->
                    ExerciseCard(
                        name = exercise.name,
                        amount = exercise.amount,
                        isDone = exercise.done,
                        isTimed = exercise.timed,
                        onToggle = { viewModel.toggleExtraExercise(index, it) },
                        onStartTimer = { viewModel.startTimedExercise(quest.exercises.size + index) },
                        onSkipTimer = { viewModel.skipTimedExercise(quest.exercises.size + index) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        } else {
            // Passcard used - show rest day message
            item {
                Surface(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "DAY OFF",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Rest and recover!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Deadline at bottom with penalty warning
        item {
            Spacer(modifier = Modifier.height(16.dp))
            DeadlineInfo(
                timeLeft = timeLeft,
                completed = quest.completed,
                passcards = user.passcards,
                hasExtra = quest.extraExercises.isNotEmpty() && quest.completed,
                onUsePasscard = { viewModel.usePasscard() }
            )
        }
    }
}

@Composable
fun DeadlineInfo(
    timeLeft: Long,
    completed: Boolean,
    passcards: Int,
    hasExtra: Boolean,
    onUsePasscard: () -> Unit
) {
    val hours = TimeUnit.MILLISECONDS.toHours(timeLeft)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeft) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeft) % 60

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DEADLINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "${hours}h ${minutes}m ${seconds}s",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                if (!completed) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "WARNING: Failure will result in XP penalty!",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                } else if (hasExtra) {
                    Text(
                        text = "Main quest complete! Bonus available above.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (!completed && passcards > 0) {
            OutlinedButton(
                onClick = onUsePasscard,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("USE PASSCARD - REST DAY ($passcards available)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DayCountdown(timeLeft: Long, completed: Boolean) {
    val totalDaySeconds = 24 * 60 * 60L
    val progress = 1f - (timeLeft.toFloat() / totalDaySeconds).coerceIn(0f, 1f)
    val outlineColor = MaterialTheme.colorScheme.outline
    
    val hours = TimeUnit.MILLISECONDS.toHours(timeLeft)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeft) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeft) % 60

    Surface(
        color = if (completed) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                2.dp, 
                if (completed) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else outlineColor.copy(alpha = 0.3f), 
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DAY PROGRESS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val primaryColor = MaterialTheme.colorScheme.primary
            val primaryColorValue = primaryColor
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(140.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    
                    drawCircle(
                        color = Color.Gray.copy(alpha = 0.2f),
                        radius = radius,
                        style = Stroke(width = strokeWidth)
                    )
                    
                    drawArc(
                        color = if (completed) primaryColorValue else primaryColorValue,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (completed) "DONE" else "${hours}h ${minutes}m",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (completed) "COMPLETE" else "REMAINING",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun QuestHeader() {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
    ) {
        Text(
            text = "[SYSTEM: DAILY QUEST HAS ARRIVED]",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(12.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ExerciseCard(
    name: String,
    amount: Int,
    isDone: Boolean,
    isTimed: Boolean,
    onToggle: (Boolean) -> Unit,
    onStartTimer: () -> Unit,
    onSkipTimer: () -> Unit
) {
    val borderColor = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val bgColor = if (isDone) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    var lastClickTime by remember { mutableStateOf(0L) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name.replace("_", " ").uppercase(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isTimed) "DURATION: ${formatDuration(amount)}" else "GOAL: $amount REPS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        if (isTimed) {
            Button(
                onClick = {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastClickTime < 300) {
                        onSkipTimer()
                    } else {
                        if (!isDone) onStartTimer()
                    }
                    lastClickTime = currentTime
                },
                enabled = !isDone,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = if (isDone) "DONE" else formatDurationBtn(amount),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Checkbox(
                checked = isDone,
                onCheckedChange = onToggle,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) "${hours}h ${minutes}m ${secs}s" else if (minutes > 0) "${minutes}m ${secs}s" else "${secs}s"
}

fun formatDurationBtn(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) "${hours}h${minutes}m" else if (minutes > 0) "${minutes}m${secs}s" else "${secs}s"
}