package com.neuraknight.thesystem.ui.screens.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neuraknight.thesystem.ui.screens.dialogs.SettingsDialog
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel
import kotlin.math.roundToInt

@Composable
fun ProfileTab(viewModel: MainViewModel) {
    val user = viewModel.appData.user
    val knownExercises = viewModel.appData.exercises.filter { it.requiredLevel <= user.level }
    var showSettingsDialog by remember { mutableStateOf(false) }

    if (showSettingsDialog) {
        // Use the newly created Composable SettingsDialog
        com.neuraknight.thesystem.ui.screens.dialogs.SettingsDialog(
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }

    LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
        item {
            Text("Profile", style = MaterialTheme.typography.headlineSmall)
            Text("Level: ${user.level}")
            Text("Total XP: ${user.totalXp.roundToInt()}")
            Text("Streak: ${user.streak} days")
            Text("Passcards: ${user.passcards}")

            Spacer(Modifier.height(16.dp))
            Text("Stats (AP: ${user.stats.AP})", style = MaterialTheme.typography.titleLarge)
        }

        item { StatRow("STR", user.stats.STR.roundToInt(), user.stats.AP > 0) { viewModel.upgradeStat("STR") } }
        item { StatRow("AGI", user.stats.AGI.roundToInt(), user.stats.AP > 0) { viewModel.upgradeStat("AGI") } }
        item { StatRow("VIT", user.stats.VIT.roundToInt(), user.stats.AP > 0) { viewModel.upgradeStat("VIT") } }
        item { StatRow("END", user.stats.END.roundToInt(), user.stats.AP > 0) { viewModel.upgradeStat("END") } }

        item {
            Spacer(Modifier.height(24.dp))
            Button(onClick = { showSettingsDialog = true }) {
                Text("Settings")
            }
            Spacer(Modifier.height(24.dp))
            Text("Available Exercises", style = MaterialTheme.typography.titleLarge)
        }

        items(knownExercises) { exercise ->
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(exercise.name.uppercase(), fontWeight = FontWeight.Bold)
                Text("XP/rep ${exercise.xpPerRep}, Stats: ${formatStatGains(exercise.statGain)}")
            }
        }
    }
}

@Composable
fun StatRow(name: String, value: Int, canUpgrade: Boolean, onUpgrade: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$name: $value")
        Button(onClick = onUpgrade, enabled = canUpgrade) {
            Text("+")
        }
    }
}

fun formatStatGains(gain: com.neuraknight.thesystem.data.models.StatGain): String {
    return listOfNotNull(
        if (gain.STR > 0) "STR: +${gain.STR}" else null,
        if (gain.AGI > 0) "AGI: +${gain.AGI}" else null,
        if (gain.VIT > 0) "VIT: +${gain.VIT}" else null,
        if (gain.END > 0) "END: +${gain.END}" else null
    ).joinToString(", ")
}
