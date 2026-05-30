package com.neuraknight.thesystem.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.neuraknight.thesystem.data.models.AppData
import com.neuraknight.thesystem.data.models.Exercise
import com.neuraknight.thesystem.data.models.Habit
import com.neuraknight.thesystem.data.models.Prayer
import com.neuraknight.thesystem.data.models.QuestExercise
import com.neuraknight.thesystem.data.models.Settings
import com.neuraknight.thesystem.data.repository.DataRepository
import com.neuraknight.thesystem.notifications.NotificationHelper
import com.neuraknight.thesystem.notifications.NotificationScheduler
import com.neuraknight.thesystem.utils.SunCalc
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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
    private val originalTimedAmounts = mutableMapOf<Int, Int>()

    fun reloadData() {
        appData = repository.loadData()
        if (appData.setupComplete && appData.settings.showPrayers) {
            calculatePrayers()
        }
    }

    init {
        appData = repository.loadData()
        if (appData.setupComplete) {
            if (appData.settings.showPrayers) calculatePrayers()
            initNotifications()
            checkPendingQuestReset()
        }
    }

    private fun initNotifications() {
        val context = getApplication<Application>()
        NotificationHelper.createChannels(context)
        scheduleNotifications()
    }

    fun scheduleNotifications() {
        val context = getApplication<Application>()
        NotificationScheduler.scheduleAll(context, appData.settings)
    }

    private fun checkPendingQuestReset() {
        val context = getApplication<Application>()
        val prefs = context.getSharedPreferences("TheSystemApp", android.content.Context.MODE_PRIVATE)
        if (prefs.getBoolean("quest_reset_pending", false)) {
            prefs.edit().putBoolean("quest_reset_pending", false).apply()
            // Guard against double reset: only reset if nextReset is in the past
            if (appData.quest.nextReset <= System.currentTimeMillis()) {
                resetQuest(force = false)
            }
        }
    }

    private fun saveData() {
        appData = appData.copy() // Ensures Compose recomposition
        repository.saveData(appData)
    }

    fun completeSetup(name: String, workoutLevel: String, goal: String, color: String, gender: String = "male", showPrayers: Boolean = true, showHabits: Boolean = true, profileImagePath: String = "") {
        val user = appData.user.copy()
        val scaling = appData.scaling.copy()
        var startingLevel = 0
        if (workoutLevel == "intermediate") startingLevel = 5
        if (workoutLevel == "advanced") startingLevel = 10

        var progressSpeed = 60
        if (goal == "quick") progressSpeed = 30
        if (goal == "longterm") progressSpeed = 90

        user.name = name.ifEmpty { "Unknown" }
        user.gender = gender
        user.level = startingLevel
        if (profileImagePath.isNotEmpty()) user.profileImg = profileImagePath
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
            settings = appData.settings.copy(color = color, showPrayers = showPrayers, showHabits = showHabits),
            setupComplete = true,
            quest = appData.quest.copy(nextReset = calculateNextReset(System.currentTimeMillis()))
        )
        calculatePrayers()
        generateNewQuest()
        initNotifications()
    }

    fun calculatePrayers() {
        val cal = Calendar.getInstance()
        val lat = appData.settings.prayerLatitude
        val lng = appData.settings.prayerLongitude
        
        val method = when (appData.settings.prayerAlgorithm) {
            "mwl" -> SunCalc.PrayerMethod.MWL
            "isna" -> SunCalc.PrayerMethod.ISNA
            "egypto" -> SunCalc.PrayerMethod.EGYPTO
            "makkah" -> SunCalc.PrayerMethod.MAKKAH
            "karachi" -> SunCalc.PrayerMethod.KARACHI
            "tehran" -> SunCalc.PrayerMethod.TEHRAN
            "jafari" -> SunCalc.PrayerMethod.JAFARI
            else -> SunCalc.PrayerMethod.DEFAULT
        }
        
        val sunTimes = SunCalc.getPrayerTimes(cal, lat, lng, method)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        val newPrayers = mutableListOf(
            Prayer("Fajr", sunTimes.fajr?.let { sdf.format(it) } ?: "--:--", false, sunTimes.fajr),
            Prayer("Dhuhr", sunTimes.dhuhr?.let { sdf.format(it) } ?: "--:--", false, sunTimes.dhuhr),
            Prayer("Asr", sunTimes.asr?.let { sdf.format(it) } ?: "--:--", false, sunTimes.asr),
            Prayer("Maghrib", sunTimes.maghrib?.let { sdf.format(it) } ?: "--:--", false, sunTimes.maghrib),
            Prayer("Isha", sunTimes.isha?.let { sdf.format(it) } ?: "--:--", false, sunTimes.isha)
        )
        
        appData = appData.copy(prayers = newPrayers)
        saveData()
    }

    fun togglePrayerDone(index: Int, isDone: Boolean) {
        val updatedPrayers = appData.prayers.toMutableList()
        if (index in updatedPrayers.indices) {
            updatedPrayers[index] = updatedPrayers[index].copy(done = isDone)
            appData = appData.copy(prayers = updatedPrayers)
            saveData()
        }
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

        originalTimedAmounts[index] = exercise.amount
        timerJobs[index] = viewModelScope.launch {
            for (time in exercise.amount downTo 1) {
                delay(1000)
                val currentExercises = appData.quest.exercises.toMutableList()
                if (index in currentExercises.indices) {
                    currentExercises[index] = currentExercises[index].copy(amount = time - 1)
                    appData = appData.copy(quest = appData.quest.copy(exercises = currentExercises))
                }
            }
            // Restore original amount before marking done so XP is calculated correctly
            val originalAmount = originalTimedAmounts.remove(index) ?: exercise.amount
            val currentExercises = appData.quest.exercises.toMutableList()
            if (index in currentExercises.indices) {
                currentExercises[index] = currentExercises[index].copy(amount = originalAmount)
                appData = appData.copy(quest = appData.quest.copy(exercises = currentExercises))
            }
            toggleQuestExercise(index, true)
        }
    }

    fun skipTimedExercise(index: Int) {
        timerJobs[index]?.cancel()
        val originalAmount = originalTimedAmounts.remove(index)
        if (originalAmount != null) {
            val exercises = appData.quest.exercises.toMutableList()
            if (index in exercises.indices) {
                exercises[index] = exercises[index].copy(amount = originalAmount)
                appData = appData.copy(quest = appData.quest.copy(exercises = exercises))
            }
        }
        toggleQuestExercise(index, true)
    }

    private val extraTimerJobs = mutableMapOf<Int, Job>()
    private val originalExtraTimedAmounts = mutableMapOf<Int, Int>()

    fun startExtraTimedExercise(index: Int) {
        val exercise = appData.quest.extraExercises.getOrNull(index) ?: return
        if (extraTimerJobs[index]?.isActive == true) return

        originalExtraTimedAmounts[index] = exercise.amount
        extraTimerJobs[index] = viewModelScope.launch {
            for (time in exercise.amount downTo 1) {
                delay(1000)
                val currentExtras = appData.quest.extraExercises.toMutableList()
                if (index in currentExtras.indices) {
                    currentExtras[index] = currentExtras[index].copy(amount = time - 1)
                    appData = appData.copy(quest = appData.quest.copy(extraExercises = currentExtras))
                }
            }
            val originalAmount = originalExtraTimedAmounts.remove(index) ?: exercise.amount
            val currentExtras = appData.quest.extraExercises.toMutableList()
            if (index in currentExtras.indices) {
                currentExtras[index] = currentExtras[index].copy(amount = originalAmount)
                appData = appData.copy(quest = appData.quest.copy(extraExercises = currentExtras))
            }
            toggleExtraExercise(index, true)
        }
    }

    fun skipExtraTimedExercise(index: Int) {
        extraTimerJobs[index]?.cancel()
        val originalAmount = originalExtraTimedAmounts.remove(index)
        if (originalAmount != null) {
            val extras = appData.quest.extraExercises.toMutableList()
            if (index in extras.indices) {
                extras[index] = extras[index].copy(amount = originalAmount)
                appData = appData.copy(quest = appData.quest.copy(extraExercises = extras))
            }
        }
        toggleExtraExercise(index, true)
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
                    val reps = qe.amount.toDouble()
                    statGains["STR"] = statGains["STR"]!! + exData.statGain.STR * reps / (1 + appData.user.stats.STR / 20.0)
                    statGains["AGI"] = statGains["AGI"]!! + exData.statGain.AGI * reps / (1 + appData.user.stats.AGI / 20.0)
                    statGains["VIT"] = statGains["VIT"]!! + exData.statGain.VIT * reps / (1 + appData.user.stats.VIT / 20.0)
                    statGains["END"] = statGains["END"]!! + exData.statGain.END * reps / (1 + appData.user.stats.END / 20.0)
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
            generateExtraExercises() // Generate bonus exercises when main completed
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

        // Cap at level 31 for display purposes but allow XP to accumulate
        val displayLevel = min(lvl, 31)
        
        val apGained = if (lvl > oldLevel) {
            (lvl - oldLevel) * appData.scaling.apPerLevel
        } else 0

        val rank = when {
            displayLevel >= 20 -> "Legend"
            displayLevel >= 15 -> "Diamond"
            displayLevel >= 10 -> "Platinum"
            displayLevel >= 6 -> "Gold"
            displayLevel >= 3 -> "Silver"
            else -> "Bronze"
        }
        val title = when {
            displayLevel >= 20 -> "Legend"
            displayLevel >= 15 -> "Master"
            displayLevel >= 10 -> "Elite"
            displayLevel >= 6 -> "Warrior"
            displayLevel >= 3 -> "Apprentice"
            displayLevel == 0 -> "Newbie"
            else -> "Beginner"
        }
        val charClass = when {
            displayLevel >= 15 -> "S"
            displayLevel >= 10 -> "A"
            displayLevel >= 6 -> "B"
            displayLevel >= 3 -> "C"
            displayLevel >= 1 -> "D"
            else -> "F"
        }

        appData = appData.copy(
            user = appData.user.copy(
                level = displayLevel,
                totalXp = appData.user.totalXp,
                xpProgress = appData.user.totalXp - cumulative,
                xpNeeded = next,
                rank = rank,
                currentTitle = title,
                characterClass = charClass,
                stats = appData.user.stats.copy(AP = appData.user.stats.AP + apGained)
            )
        )

        if (displayLevel > oldLevel) {
            viewModelScope.launch { _toastMessage.emit("You Leveled Up!") }
            NotificationHelper.showLevelUp(getApplication(), displayLevel)
        }
        saveData()
    }

    private fun calculateXpForLevel(lvl: Int): Double {
        // Fast progression for levels 1-10, then normal
        return if (lvl <= appData.scaling.fastLevelCap) {
            floor(appData.scaling.baseXP * lvl.toDouble().pow(appData.scaling.exponentFast))
        } else {
            val baseAfterFast = floor(appData.scaling.baseXP * appData.scaling.fastLevelCap.toDouble().pow(appData.scaling.exponentFast))
            val additional = (lvl - appData.scaling.fastLevelCap).toDouble()
            baseAfterFast + floor(additional * 100 * (1 + additional * 0.1))
        }
    }

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
                quest = appData.quest.copy(
                    completed = true,
                    usedPasscard = true
                )
            )
            viewModelScope.launch { _toastMessage.emit("Day passed with a passcard!") }
            saveData()
        }
    }

    fun resetQuest(force: Boolean) {
        // Cancel all running timers to prevent corruption
        timerJobs.values.forEach { it.cancel() }
        timerJobs.clear()
        originalTimedAmounts.clear()
        extraTimerJobs.values.forEach { it.cancel() }
        extraTimerJobs.clear()
        originalExtraTimedAmounts.clear()

        if (force) {
            if (appData.quest.completed || appData.quest.usedPasscard) {
                val newStreak = appData.user.streak + 1
                appData = appData.copy(
                    user = appData.user.copy(streak = newStreak),
                    quest = appData.quest.copy(
                        nextReset = calculateNextResetMidnight(),
                        completed = false,
                        usedPasscard = false
                    )
                )
                checkStreakRewards()
                generateNewQuest()
            } else {
                viewModelScope.launch { _toastMessage.emit("Quest not completed yet.") }
            }
        } else {
            if (!appData.quest.completed && !appData.quest.usedPasscard) {
                // Flat penalty: lose 50 XP (the value of ~1 exercise)
                appData = appData.copy(
                    user = appData.user.copy(
                        totalXp = (appData.user.totalXp - 50.0).coerceAtLeast(0.0),
                        streak = 0
                    )
                )
                calculateLevelFromTotalXp()
            } else if (appData.quest.completed || appData.quest.usedPasscard) {
                // Increment streak on automatic reset when quest was completed
                appData = appData.copy(
                    user = appData.user.copy(streak = appData.user.streak + 1)
                )
                checkStreakRewards()
            }
            // Reset quest at midnight - but keep habits, prayers, and passcards intact
            appData = appData.copy(
                quest = appData.quest.copy(
                    nextReset = calculateNextResetMidnight(),
                    completed = false,
                    usedPasscard = false
                )
            )
            generateNewQuest()
        }
        NotificationScheduler.scheduleQuestReset(getApplication())
    }

    private fun calculateNextResetMidnight(): Long {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = now
        calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun toggleExtraExercise(index: Int, isDone: Boolean) {
        val currentExtras = appData.quest.extraExercises.toMutableList()
        if (index in currentExtras.indices) {
            currentExtras[index] = currentExtras[index].copy(done = isDone)
            appData = appData.copy(quest = appData.quest.copy(extraExercises = currentExtras))

            if (isDone) {
                val exercise = currentExtras[index]
                val exData = appData.exercises.find { it.name == exercise.name }
                if (exData != null) {
                    val xpGain = exercise.amount * exData.xpPerRep * 0.5
                    val user = appData.user.copy(
                        totalXp = appData.user.totalXp + xpGain
                    )
                    appData = appData.copy(user = user)
                    calculateLevelFromTotalXp()
                }
            }
            saveData()

            if (currentExtras.all { it.done } && appData.quest.extraSetsRemaining > 0) {
                generateExtraExercises()
            }
        }
    }

    fun requestExtraSet() {
        if (appData.quest.completed && appData.quest.extraSetsRemaining > 0) {
            generateExtraExercises()
        }
    }

    private fun generateExtraExercises() {
        if (!appData.quest.completed) {
            appData = appData.copy(quest = appData.quest.copy(extraExercises = listOf()))
            return
        }
        if (appData.quest.extraSetsRemaining <= 0) return

        val level = appData.user.level
        val settings = appData.settings
        
        val available = appData.exercises.filter { ex ->
            ex.requiredLevel <= level && 
            settings.equipmentTypes.contains(ex.equipment) &&
            (settings.trainingGoals.isEmpty() || 
             settings.trainingGoals.contains(ex.muscleGroup) ||
             ex.muscleGroup == "full" ||
             ex.muscleGroup == "cardio")
        }
        
        val numExtra = (2..3).random()
        val selected = available.shuffled().take(numExtra).map { ex ->
            val base = if (ex.timed) appData.scaling.baseTime else appData.scaling.baseReps
            val amount = (base * 0.3).toInt().coerceAtLeast(5)
            QuestExercise(name = ex.name, amount = amount, done = false, timed = ex.timed, muscleGroup = ex.muscleGroup, equipment = ex.equipment)
        }
        
        appData = appData.copy(
            quest = appData.quest.copy(
                extraExercises = selected,
                extraSetsRemaining = appData.quest.extraSetsRemaining - 1
            )
        )
    }

    fun toggleHabitDone(index: Int, isDone: Boolean) {
        val updatedHabits = appData.habits.toMutableList()
        if (index in updatedHabits.indices) {
            updatedHabits[index] = updatedHabits[index].copy(done = isDone)
            appData = appData.copy(habits = updatedHabits)
            saveData()
        }
    }

    fun addHabit(name: String, isCustom: Boolean = true) {
        if (name.isNotBlank()) {
            val updatedHabits = appData.habits.toMutableList()
            updatedHabits.add(Habit(name = name, done = false, isCustom = isCustom))
            appData = appData.copy(habits = updatedHabits)
            saveData()
        }
    }

    fun deleteHabit(index: Int) {
        val updatedHabits = appData.habits.toMutableList()
        if (index in updatedHabits.indices) {
            updatedHabits.removeAt(index)
            appData = appData.copy(habits = updatedHabits)
            saveData()
        }
    }

    fun editHabit(index: Int, newName: String) {
        val updatedHabits = appData.habits.toMutableList()
        if (index in updatedHabits.indices && newName.isNotBlank()) {
            updatedHabits[index] = updatedHabits[index].copy(name = newName)
            appData = appData.copy(habits = updatedHabits)
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
        val settings = appData.settings

        // Respect workoutDays: if today is not a scheduled day, minimal quest
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1 // 1=Sun → 0
        val todayIndex = if (today == 0) 7 else today // convert Sun=1 to 7
        val isWorkoutDay = settings.workoutDays.isEmpty() || settings.workoutDays.contains(todayIndex)

        val baseCount = when {
            !isWorkoutDay -> 1 // rest day: 1 exercise maintenance
            settings.difficulty == "intermediate" -> 3
            settings.difficulty == "advanced" -> 4
            else -> 2
        }
        val levelBonus = if (isWorkoutDay) level / 5 else 0
        val numExercises = min(baseCount + levelBonus, 8).coerceAtMost(appData.exercises.size)

        val difficultyMultiplier = when (settings.difficulty) {
            "advanced" -> 1.2
            "beginner" -> 0.8
            else -> 1.0
        }

        // Prioritize exercises matching training goals; cap full/cardio at 1
        val goals = settings.trainingGoals
        val filtered = appData.exercises.filter { ex ->
            ex.requiredLevel <= level &&
            settings.equipmentTypes.contains(ex.equipment) &&
            (goals.isEmpty() ||
             goals.contains(ex.muscleGroup) ||
             ex.muscleGroup == "full" ||
             ex.muscleGroup == "cardio")
        }
        val (fullCardio, others) = filtered.partition { it.muscleGroup == "full" || it.muscleGroup == "cardio" }
        val capped = others + fullCardio.take(1)
        val sorted = capped.sortedByDescending { ex ->
            var score = 0
            if (goals.contains(ex.muscleGroup)) score += 10
            if (ex.difficulty <= 3 && settings.difficulty == "beginner") score += 5
            if (ex.difficulty >= 4 && settings.difficulty == "advanced") score += 5
            score
        }

        val selected = sorted.take(numExercises).shuffled()

        val newQuestExercises = selected.map { ex ->
            val base = if (ex.timed) appData.scaling.baseTime else appData.scaling.baseReps
            val maxV = if (ex.timed) appData.scaling.maxTime else appData.scaling.maxReps
            val exp = if (ex.timed) appData.scaling.exponent2 else appData.scaling.exponent3
            val fraction = (level.toDouble() / appData.scaling.repsProgressSpeed).pow(exp)
            val rawAmount = floor(ex.baseScale * (base + (maxV - base) * fraction))
            val amount = min(maxV.toDouble(), rawAmount * difficultyMultiplier).toInt().coerceAtLeast(1)
            QuestExercise(name = ex.name, amount = amount, done = false, timed = ex.timed, muscleGroup = ex.muscleGroup, equipment = ex.equipment)
        }

        val resetHabits = appData.habits.map { it.copy(done = false) }.toMutableList()

        appData = appData.copy(
            quest = appData.quest.copy(
                exercises = newQuestExercises,
                extraExercises = listOf(),
                completed = false,
                usedPasscard = false,
                extraSetsRemaining = 3
            ),
            habits = resetHabits
        )
        calculatePrayers()
        saveData()
    }
}
