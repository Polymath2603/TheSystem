package com.neuraknight.thesystem.ui.screens.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel

@Composable
fun HabitsTab(viewModel: MainViewModel) {
    val habits = viewModel.appData.habits

    LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
        itemsIndexed(habits) { index, habit ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(habit.name.replaceFirstChar { it.uppercase() })
                Checkbox(
                    checked = habit.done,
                    onCheckedChange = { isChecked ->
                        viewModel.toggleHabitDone(index, isChecked)
                    }
                )
            }
        }
    }
}
