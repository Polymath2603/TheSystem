package com.neuraknight.thesystem.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.neuraknight.thesystem.data.models.AppData
import com.neuraknight.thesystem.data.models.CustomTask
import com.neuraknight.thesystem.data.models.Exercise
import com.neuraknight.thesystem.data.models.QuestExercise
import com.neuraknight.thesystem.data.models.Settings
import com.neuraknight.thesystem.data.repository.DataRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DataRepository(application)
    var appData by mutableStateOf(AppData())
        private set

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    private val timerJobs = mutableMapOf<Int, Job>()

    init {
        appData = repository.loadData()
    }

    private fun saveData() {
        appData = appData.copy() // Ensures Compose recomposition
        repository.saveData(appData)
    }

    fun completeSetup(name: String, workoutLevel: String, goal: String, color: String) {
        val user = appData.user.copy()
        val scaling = appData.scaling.copy()
        var startingLevel = 0
        if (workoutLevel == "intermediate") startingLevel = 5
        if (workoutLevel == "advanced") startingLevel = 10

        var progressSpeed = 60
        if (goal == "quick") progressSpeed = 30
        if (goal == "longterm") progressSpeed = 90

        user.name = name.ifEmpty { "Unknown" }
        user.level = startingLevel
        scaling.repsProgressSpeed = progressSpeed

        if (startingLevel > 0) {
            val multiplier = 1 + startingLevel * 0.1
            user.stats = user.stats.copy(
                STR = floor(user.stats.STR * multiplier),
                AGI = floor(user.stats.AGI * multiplier),
                VIT = floor(user.stats.VIT * multiplier),
                END = floor(user.stats.END * multiplier)
            )
        }

        appData = appData.copy(
            user = user,
            scaling = scaling,
            settings = appData.settings.copy(color = color),
            setupComplete = true,
            quest = appData.quest.copy(nextReset = calculateNextReset(System.currentTimeMillis()))
        )
        generateNewQuest()
    }

    fun toggleQuestExercise(index: Int, isDone: Boolean) {
        val updatedExercises = appData.quest.exercises.toMutableList()
        if (index in updatedExercises.indices) {
            updatedExercises[index] = updatedExercises[index].copy(done = isDone)
            appData = appData.copy(quest = appData.quest.copy(exercises = updatedExercises))
            checkQuestCompletion()
        }
    }

    fun startTimedExercise(index: Int) {
        val exercise = appData.quest.exercises.getOrNull(index) ?: return
        if (timerJobs[index]?.isActive == true) return

        timerJobs[index] = viewModelScope.launch {
            for (time in exercise.amount downTo 1) {
                delay(1000)
                val currentExercises = appData.quest.exercises.toMutableList()
                if (index in currentExercises.indices) {
                    currentExercises[index] = currentExercises[index].copy(amount = time - 1)
                    appData = appData.copy(quest = appData.quest.copy(exercises = currentExercises))
                }
            }
            toggleQuestExercise(index, true)
        }
    }

    private fun checkQuestCompletion() {
        val allDone = appData.quest.exercises.all { it.done }
        if (allDone && !appData.quest.completed) {
            var xpGain = 0.0
            val statGains = mutableMapOf("STR" to 0.0, "AGI" to 0.0, "VIT" to 0.0, "END" to 0.0)

            appData.quest.exercises.forEach { qe ->
                val exData = appData.exercises.find { it.name == qe.name }
                if (exData != null) {
                    xpGain += qe.amount * exData.xpPerRep
                    statGains["STR"] = statGains["STR"]!! + (exData.statGain.STR * qe.amount)
                    statGains["AGI"] = statGains["AGI"]!! + (exData.statGain.AGI * qe.amount)
                    statGains["VIT"] = statGains["VIT"]!! + (exData.statGain.VIT * qe.amount)
                    statGains["END"] = statGains["END"]!! + (exData.statGain.END * qe.amount)
                }
            }

            val user = appData.user.copy(
                totalXp = appData.user.totalXp + xpGain,
                stats = appData.user.stats.copy(
                    STR = appData.user.stats.STR + statGains["STR"]!!,
                    AGI = appData.user.stats.AGI + statGains["AGI"]!!,
                    VIT = appData.user.stats.VIT + statGains["VIT"]!!,
                    END = appData.user.stats.END + statGains["END"]!!
                )
            )

            appData = appData.copy(user = user, quest = appData.quest.copy(completed = true))
            viewModelScope.launch { _toastMessage.emit("Quest Completed!") }
            calculateLevelFromTotalXp()
        } else {
            saveData()
        }
    }

    private fun calculateLevelFromTotalXp() {
        val oldLevel = appData.user.level
        var cumulative = 0.0
        var lvl = 0
        var next = calculateXpForLevel(1)

        while (cumulative + next <= appData.user.totalXp) {
            cumulative += next
            lvl++
            next = calculateXpForLevel(lvl + 1)
        }

        val apGained = (lvl - oldLevel) * appData.scaling.apPerLevel

        appData = appData.copy(
            user = appData.user.copy(
                level = lvl,
                xpProgress = appData.user.totalXp - cumulative,
                xpNeeded = next,
                stats = appData.user.stats.copy(AP = appData.user.stats.AP + apGained)
            )
        )

        if (lvl > oldLevel) {
            viewModelScope.launch { _toastMessage.emit("You Leveled Up!") }
        }
        saveData()
    }

    private fun calculateXpForLevel(lvl: Int): Double =
        floor(appData.scaling.baseXP * lvl.toDouble().pow(appData.scaling.exponent))

    private fun checkStreakRewards() {
        val streak = appData.user.streak
        var apBonus = 0
        var passcardBonus = 0
        
        // Every 7 days: +1 passcard
        if (streak % 7 == 0 && streak > 0) {
            passcardBonus = 1
        }
        
        // Every 30 days: +10 AP
        if (streak % 30 == 0 && streak > 0) {
            apBonus = 10
        }
        
        if (apBonus > 0 || passcardBonus > 0) {
            val newStats = appData.user.stats.copy(
                AP = appData.user.stats.AP + apBonus
            )
            appData = appData.copy(
                user = appData.user.copy(
                    stats = newStats,
                    passcards = appData.user.passcards + passcardBonus
                )
            )
            
            val message = buildString {
                if (passcardBonus > 0) append("Passcard +1 ")
                if (apBonus > 0) append("AP +$apBonus ")
                append("(Streak: ${streak} days)")
            }
            viewModelScope.launch { _toastMessage.emit(message.trim()) }
        }
    }

    fun usePasscard() {
        if (appData.user.passcards > 0) {
            appData = appData.copy(
                user = appData.user.copy(passcards = appData.user.passcards - 1),
                quest = appData.quest.copy(completed = true)
            )
            viewModelScope.launch { _toastMessage.emit("Day passed with a passcard!") }
            saveData()
        }
    }

    fun resetQuest(force: Boolean) {
        if (force) {
            if (appData.quest.completed) {
                val newStreak = appData.user.streak + 1
                appData = appData.copy(
                    user = appData.user.copy(streak = newStreak),
                    quest = appData.quest.copy(nextReset = calculateNextReset(appData.quest.nextReset))
                )
                checkStreakRewards()
                generateNewQuest()
            } else {
                viewModelScope.launch { _toastMessage.emit("Quest not completed yet.") }
            }
        } else {
            if (!appData.quest.completed) {
                // Apply penalty: lose 10% of total XP
                appData = appData.copy(
                    user = appData.user.copy(
                        totalXp = floor(appData.user.totalXp * 0.9),
                        streak = 0
                    )
                )
                calculateLevelFromTotalXp()
            } else {
                val newStreak = appData.user.streak + 1
                appData = appData.copy(user = appData.user.copy(streak = newStreak))
                checkStreakRewards()
            }
            appData = appData.copy(quest = appData.quest.copy(nextReset = calculateNextReset(System.currentTimeMillis())))
            generateNewQuest()
        }
    }

    fun toggleHabitDone(index: Int, isDone: Boolean) {
        val updatedHabits = appData.habits.toMutableList()
        if (index in updatedHabits.indices) {
            updatedHabits[index] = updatedHabits[index].copy(done = isDone)
            appData = appData.copy(habits = updatedHabits)
            saveData()
        }
    }

    fun addCustomTask(name: String) {
        if (name.isNotBlank()) {
            val updatedTasks = appData.customTasks.toMutableList()
            updatedTasks.add(CustomTask(name = name, done = false))
            appData = appData.copy(customTasks = updatedTasks)
            saveData()
        }
    }

    fun deleteCustomTask(index: Int) {
        val updatedTasks = appData.customTasks.toMutableList()
        if (index in updatedTasks.indices) {
            updatedTasks.removeAt(index)
            appData = appData.copy(customTasks = updatedTasks)
            saveData()
        }
    }

    fun toggleCustomTaskDone(index: Int, isDone: Boolean) {
        val updatedTasks = appData.customTasks.toMutableList()
        if (index in updatedTasks.indices) {
            updatedTasks[index] = updatedTasks[index].copy(done = isDone)
            appData = appData.copy(customTasks = updatedTasks)
            saveData()
        }
    }

    fun upgradeStat(statName: String) {
        if (appData.user.stats.AP > 0) {
            val currentStats = appData.user.stats
            val newStats = when(statName) {
                "STR" -> currentStats.copy(STR = currentStats.STR + 1, AP = currentStats.AP - 1)
                "AGI" -> currentStats.copy(AGI = currentStats.AGI + 1, AP = currentStats.AP - 1)
                "VIT" -> currentStats.copy(VIT = currentStats.VIT + 1, AP = currentStats.AP - 1)
                "END" -> currentStats.copy(END = currentStats.END + 1, AP = currentStats.AP - 1)
                else -> currentStats
            }
            appData = appData.copy(user = appData.user.copy(stats = newStats))
            saveData()
        }
    }

    fun saveSettings(name: String, color: String, showHabits: Boolean, showCustom: Boolean) {
        appData = appData.copy(
            user = appData.user.copy(name = name.ifEmpty { "Unknown" }),
            settings = Settings(
                color = color,
                showHabits = showHabits,
                showCustom = showCustom
            )
        )
        saveData()
    }

    private fun calculateNextReset(from: Long): Long {
        val next = Calendar.getInstance().apply { timeInMillis = from }
        next.set(Calendar.HOUR_OF_DAY, 3)
        next.set(Calendar.MINUTE, 30)
        next.set(Calendar.SECOND, 0)
        next.set(Calendar.MILLISECOND, 0)
        if (next.timeInMillis <= from) {
            next.add(Calendar.DATE, 1)
        }
        return next.timeInMillis
    }

    private fun generateNewQuest() {
        val level = appData.user.level
        val unlocked = appData.exercises.filter { it.requiredLevel <= level }.toMutableList()
        val selected = mutableListOf<Exercise>()
        val numExercises = min(4, unlocked.size)

        repeat(numExercises) {
            if (unlocked.isNotEmpty()) {
                val idx = unlocked.indices.random()
                selected.add(unlocked.removeAt(idx))
            }
        }

        val newQuestExercises = selected.map { ex ->
            val base = if (ex.timed) appData.scaling.baseTime else appData.scaling.baseReps
            val maxV = if (ex.timed) appData.scaling.maxTime else appData.scaling.maxReps
            val exp = if (ex.timed) appData.scaling.exponent2 else appData.scaling.exponent3
            val fraction = (level.toDouble() / appData.scaling.repsProgressSpeed).pow(exp)
            val amount = min(maxV.toDouble(), floor(ex.baseScale * (base + (maxV - base) * fraction))).toInt()
            QuestExercise(name = ex.name, amount = amount, done = false, timed = ex.timed)
        }

        appData.habits.forEach { it.done = false }
        appData.customTasks.forEach { it.done = false }

        appData = appData.copy(
            quest = appData.quest.copy(exercises = newQuestExercises, completed = false)
        )
        saveData()
    }
}
