package com.neuraknight.thesystem.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.neuraknight.thesystem.data.models.*
import com.neuraknight.thesystem.data.repository.DataRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DataRepository(application)
    
    // StateFlow for reactive state management
    private val _appData = MutableStateFlow(AppData())
    val appData: StateFlow<AppData> = _appData.asStateFlow()
    
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()
    
    private val timerJobs = mutableMapOf<Int, Job>()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        _appData.value = repository.loadData()
    }
    
    private fun saveData() {
        repository.saveData(_appData.value)
    }
    
    fun updateProfileImage(path: String) {
        _appData.update { it.copy(user = it.user.copy(profileImg = path)) }
        saveData()
    }
    
    fun completeSetup(name: String, workoutLevel: String, goal: String, color: String) {
        val startingLevel = when (workoutLevel) {
            "intermediate" -> 5
            "advanced" -> 10
            else -> 0
        }
        
        val progressSpeed = when (goal) {
            "quick" -> 30
            "longterm" -> 90
            else -> 60
        }
        
        _appData.update { currentData ->
            val multiplier = 1 + startingLevel * 0.1
            val newStats = if (startingLevel > 0) {
                currentData.user.stats.copy(
                    STR = floor(currentData.user.stats.STR * multiplier),
                    AGI = floor(currentData.user.stats.AGI * multiplier),
                    VIT = floor(currentData.user.stats.VIT * multiplier),
                    END = floor(currentData.user.stats.END * multiplier)
                )
            } else {
                currentData.user.stats
            }
            
            currentData.copy(
                user = currentData.user.copy(
                    name = name.ifEmpty { "Unknown" },
                    level = startingLevel,
                    stats = newStats
                ),
                scaling = currentData.scaling.copy(repsProgressSpeed = progressSpeed),
                settings = currentData.settings.copy(color = color),
                setupComplete = true,
                quest = currentData.quest.copy(nextReset = calculateNextReset(System.currentTimeMillis()))
            )
        }
        generateNewQuest()
    }
    
    // Fixed checkbox logic - no longer triggers on state update
    fun toggleQuestExercise(index: Int, isDone: Boolean) {
        _appData.update { currentData ->
            val updatedExercises = currentData.quest.exercises.toMutableList()
            if (index in updatedExercises.indices) {
                updatedExercises[index] = updatedExercises[index].copy(done = isDone)
                currentData.copy(quest = currentData.quest.copy(exercises = updatedExercises))
            } else {
                currentData
            }
        }
        checkQuestCompletion()
    }
    
    // Fixed timer logic - proper job management and cancellation
    fun startTimedExercise(index: Int) {
        val exercise = _appData.value.quest.exercises.getOrNull(index) ?: return
        if (timerJobs[index]?.isActive == true) return
        
        timerJobs[index] = viewModelScope.launch {
            var remainingTime = exercise.amount
            while (remainingTime > 0) {
                delay(1000)
                remainingTime--
                
                _appData.update { currentData ->
                    val updatedExercises = currentData.quest.exercises.toMutableList()
                    if (index in updatedExercises.indices) {
                        updatedExercises[index] = updatedExercises[index].copy(amount = remainingTime)
                        currentData.copy(quest = currentData.quest.copy(exercises = updatedExercises))
                    } else {
                        currentData
                    }
                }
            }
            toggleQuestExercise(index, true)
        }
    }
    
    fun cancelTimedExercise(index: Int) {
        timerJobs[index]?.cancel()
        timerJobs.remove(index)
    }
    
    private fun checkQuestCompletion() {
        val currentData = _appData.value
        val allDone = currentData.quest.exercises.all { it.done }
        
        if (allDone && !currentData.quest.completed) {
            var xpGain = 0.0
            val statGains = mutableMapOf("STR" to 0.0, "AGI" to 0.0, "VIT" to 0.0, "END" to 0.0)
            
            currentData.quest.exercises.forEach { qe ->
                currentData.exercises.find { it.name == qe.name }?.let { ex ->
                    xpGain += qe.amount * ex.xpPerRep
                    statGains["STR"] = statGains["STR"]!! + (ex.statGain.STR * qe.amount)
                    statGains["AGI"] = statGains["AGI"]!! + (ex.statGain.AGI * qe.amount)
                    statGains["VIT"] = statGains["VIT"]!! + (ex.statGain.VIT * qe.amount)
                    statGains["END"] = statGains["END"]!! + (ex.statGain.END * qe.amount)
                }
            }
            
            _appData.update {
                it.copy(
                    user = it.user.copy(
                        totalXp = it.user.totalXp + xpGain,
                        stats = it.user.stats.copy(
                            STR = it.user.stats.STR + statGains["STR"]!!,
                            AGI = it.user.stats.AGI + statGains["AGI"]!!,
                            VIT = it.user.stats.VIT + statGains["VIT"]!!,
                            END = it.user.stats.END + statGains["END"]!!
                        )
                    ),
                    quest = it.quest.copy(completed = true)
                )
            }
            
            viewModelScope.launch { _toastMessage.emit("Quest Completed! 🎉") }
            calculateLevelFromTotalXp()
        } else {
            saveData()
        }
    }
    
    private fun calculateLevelFromTotalXp() {
        val currentData = _appData.value
        val oldLevel = currentData.user.level
        var cumulative = 0.0
        var lvl = 0
        var next = calculateXpForLevel(1)
        
        while (cumulative + next <= currentData.user.totalXp) {
            cumulative += next
            lvl++
            next = calculateXpForLevel(lvl + 1)
        }
        
        val apGained = (lvl - oldLevel) * currentData.scaling.apPerLevel
        
        _appData.update {
            it.copy(
                user = it.user.copy(
                    level = lvl,
                    xpProgress = it.user.totalXp - cumulative,
                    xpNeeded = next,
                    stats = it.user.stats.copy(AP = it.user.stats.AP + apGained)
                )
            )
        }
        
        if (lvl > oldLevel) {
            viewModelScope.launch { _toastMessage.emit("Level Up! You're now level $lvl! ⬆️") }
        }
        saveData()
    }
    
    private fun calculateXpForLevel(lvl: Int): Double {
        val scaling = _appData.value.scaling
        return floor(scaling.baseXP * lvl.toDouble().pow(scaling.exponent))
    }
    
    private fun checkStreakRewards() {
        val streak = _appData.value.user.streak
        var apBonus = 0
        var passcardBonus = 0
        
        if (streak % 7 == 0 && streak > 0) passcardBonus = 1
        if (streak % 30 == 0 && streak > 0) apBonus = 10
        
        if (apBonus > 0 || passcardBonus > 0) {
            _appData.update {
                it.copy(
                    user = it.user.copy(
                        stats = it.user.stats.copy(AP = it.user.stats.AP + apBonus),
                        passcards = it.user.passcards + passcardBonus
                    )
                )
            }
            
            val message = buildString {
                if (passcardBonus > 0) append("🎫 Passcard +1 ")
                if (apBonus > 0) append("⚡ AP +$apBonus ")
                append("(${streak} day streak!)")
            }
            viewModelScope.launch { _toastMessage.emit(message.trim()) }
        }
    }
    
    fun usePasscard() {
        if (_appData.value.user.passcards > 0) {
            _appData.update {
                it.copy(
                    user = it.user.copy(passcards = it.user.passcards - 1),
                    quest = it.quest.copy(completed = true)
                )
            }
            viewModelScope.launch { _toastMessage.emit("Day passed with a passcard! 🎫") }
            saveData()
        }
    }
    
    fun resetQuest(force: Boolean = false) {
        val currentData = _appData.value
        
        if (force) {
            if (currentData.quest.completed) {
                _appData.update {
                    it.copy(
                        user = it.user.copy(streak = it.user.streak + 1),
                        quest = it.quest.copy(nextReset = calculateNextReset(it.quest.nextReset))
                    )
                }
                checkStreakRewards()
                generateNewQuest()
            } else {
                viewModelScope.launch { _toastMessage.emit("Complete the quest first!") }
            }
        } else {
            if (!currentData.quest.completed) {
                _appData.update {
                    it.copy(
                        user = it.user.copy(
                            totalXp = floor(it.user.totalXp * 0.9),
                            streak = 0
                        )
                    )
                }
                calculateLevelFromTotalXp()
                viewModelScope.launch { _toastMessage.emit("Quest failed! 10% XP lost. Streak reset. 😢") }
            } else {
                _appData.update {
                    it.copy(user = it.user.copy(streak = it.user.streak + 1))
                }
                checkStreakRewards()
            }
            
            _appData.update {
                it.copy(quest = it.quest.copy(nextReset = calculateNextReset(System.currentTimeMillis())))
            }
            generateNewQuest()
        }
    }
    
    fun toggleHabitDone(index: Int, isDone: Boolean) {
        _appData.update { currentData ->
            val updatedHabits = currentData.habits.toMutableList()
            if (index in updatedHabits.indices) {
                updatedHabits[index] = updatedHabits[index].copy(done = isDone)
                currentData.copy(habits = updatedHabits)
            } else {
                currentData
            }
        }
        saveData()
    }
    
    fun addCustomTask(name: String) {
        if (name.isNotBlank()) {
            _appData.update {
                val updatedTasks = it.customTasks.toMutableList()
                updatedTasks.add(CustomTask(name = name.trim(), done = false))
                it.copy(customTasks = updatedTasks)
            }
            saveData()
        }
    }
    
    fun deleteCustomTask(index: Int) {
        _appData.update { currentData ->
            val updatedTasks = currentData.customTasks.toMutableList()
            if (index in updatedTasks.indices) {
                updatedTasks.removeAt(index)
                currentData.copy(customTasks = updatedTasks)
            } else {
                currentData
            }
        }
        saveData()
    }
    
    fun toggleCustomTaskDone(index: Int, isDone: Boolean) {
        _appData.update { currentData ->
            val updatedTasks = currentData.customTasks.toMutableList()
            if (index in updatedTasks.indices) {
                updatedTasks[index] = updatedTasks[index].copy(done = isDone)
                currentData.copy(customTasks = updatedTasks)
            } else {
                currentData
            }
        }
        saveData()
    }
    
    fun upgradeStat(statName: String) {
        if (_appData.value.user.stats.AP > 0) {
            _appData.update { currentData ->
                val currentStats = currentData.user.stats
                val newStats = when (statName) {
                    "STR" -> currentStats.copy(STR = currentStats.STR + 1, AP = currentStats.AP - 1)
                    "AGI" -> currentStats.copy(AGI = currentStats.AGI + 1, AP = currentStats.AP - 1)
                    "VIT" -> currentStats.copy(VIT = currentStats.VIT + 1, AP = currentStats.AP - 1)
                    "END" -> currentStats.copy(END = currentStats.END + 1, AP = currentStats.AP - 1)
                    else -> currentStats
                }
                currentData.copy(user = currentData.user.copy(stats = newStats))
            }
            saveData()
        }
    }
    
    // Fixed profile/settings logic - proper state updates
    fun saveSettings(name: String, color: String, showHabits: Boolean, showCustom: Boolean) {
        _appData.update {
            it.copy(
                user = it.user.copy(name = name.ifEmpty { "Unknown" }),
                settings = Settings(
                    color = color,
                    showHabits = showHabits,
                    showCustom = showCustom
                )
            )
        }
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
        val currentData = _appData.value
        val level = currentData.user.level
        val unlocked = currentData.exercises.filter { it.requiredLevel <= level }.toMutableList()
        val selected = mutableListOf<Exercise>()
        val numExercises = min(4, unlocked.size)
        
        repeat(numExercises) {
            if (unlocked.isNotEmpty()) {
                val idx = unlocked.indices.random()
                selected.add(unlocked.removeAt(idx))
            }
        }
        
        val newQuestExercises = selected.map { ex ->
            val base = if (ex.timed) currentData.scaling.baseTime else currentData.scaling.baseReps
            val maxV = if (ex.timed) currentData.scaling.maxTime else currentData.scaling.maxReps
            val exp = if (ex.timed) currentData.scaling.exponent2 else currentData.scaling.exponent3
            val fraction = (level.toDouble() / currentData.scaling.repsProgressSpeed).pow(exp)
            val amount = min(maxV.toDouble(), floor(ex.baseScale * (base + (maxV - base) * fraction))).toInt()
            QuestExercise(name = ex.name, amount = amount, done = false, timed = ex.timed)
        }
        
        val resetHabits = currentData.habits.map { it.copy(done = false) }
        val resetTasks = currentData.customTasks.map { it.copy(done = false) }
        
        _appData.update {
            it.copy(
                quest = it.quest.copy(exercises = newQuestExercises, completed = false),
                habits = resetHabits,
                customTasks = resetTasks
            )
        }
        saveData()
    }
}
