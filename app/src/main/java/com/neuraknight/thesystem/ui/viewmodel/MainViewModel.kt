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

    init {
        appData = repository.loadData()
        if (appData.setupComplete && appData.prayers.isEmpty()) {
            calculatePrayers()
        }
        if (appData.setupComplete) {
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
            resetQuest(force = false)
        }
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
            // Calculate AP differently for first 10 levels (faster)
            val normalLevels = maxOf(0, min(lvl, appData.scaling.fastLevelCap) - oldLevel)
            val extraLevels = maxOf(0, lvl - appData.scaling.fastLevelCap)
            (normalLevels + extraLevels) * appData.scaling.apPerLevel
        } else 0

        appData = appData.copy(
            user = appData.user.copy(
                level = displayLevel,
                totalXp = appData.user.totalXp,
                xpProgress = appData.user.totalXp - cumulative,
                xpNeeded = next,
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
                // Apply penalty: lose 10% of total XP
                appData = appData.copy(
                    user = appData.user.copy(
                        totalXp = floor(appData.user.totalXp * 0.9),
                        streak = 0
                    )
                )
                calculateLevelFromTotalXp()
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
            
            // Give bonus XP for extra exercises (no penalty for not doing them)
            if (isDone) {
                val exercise = currentExtras[index]
                val exData = appData.exercises.find { it.name == exercise.name }
                if (exData != null) {
                    val xpGain = exercise.amount * exData.xpPerRep * 0.5 // 50% XP for bonus exercises
                    val user = appData.user.copy(
                        totalXp = appData.user.totalXp + xpGain
                    )
                    appData = appData.copy(user = user)
                    calculateLevelFromTotalXp()
                }
            }
            saveData()
        }
    }

    private fun generateExtraExercises() {
        if (!appData.quest.completed) {
            appData = appData.copy(quest = appData.quest.copy(extraExercises = listOf()))
            return
        }
        
        val level = appData.user.level
        val settings = appData.settings
        
        // Filter exercises based on settings
        val available = appData.exercises.filter { ex ->
            ex.requiredLevel <= level && 
            settings.equipmentTypes.contains(ex.equipment) &&
            (settings.trainingGoals.isEmpty() || 
             settings.trainingGoals.contains(ex.muscleGroup) ||
             ex.muscleGroup == "full" ||
             ex.muscleGroup == "cardio")
        }
        
        // Pick 2-3 random bonus exercises
        val numExtra = (2..3).random()
        val selected = available.shuffled().take(numExtra).map { ex ->
            val base = if (ex.timed) appData.scaling.baseTime else appData.scaling.baseReps
            val amount = (base * 0.3).toInt().coerceAtLeast(5)
            QuestExercise(name = ex.name, amount = amount, done = false, timed = ex.timed, muscleGroup = ex.muscleGroup, equipment = ex.equipment)
        }
        
        appData = appData.copy(quest = appData.quest.copy(extraExercises = selected))
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

    fun saveSettings(
        name: String,
        color: String,
        gender: String = "male",
        prayerAlgorithm: String = "default",
        difficulty: String = "beginner",
        daysPerWeek: Int = 3,
        trainingGoals: List<String> = listOf("strength"),
        equipmentTypes: List<String> = listOf("bodyweight"),
        showPrayers: Boolean = true,
        notificationsEnabled: Boolean = true,
        workoutReminderEnabled: Boolean = true,
        workoutReminderHour: Int = 8,
        workoutReminderMinute: Int = 0,
        prayerNotificationsEnabled: Boolean = true,
        prayerNotificationLeadMinutes: Int = 10,
        streakWarningEnabled: Boolean = true,
        streakWarningHour: Int = 21,
        streakWarningMinute: Int = 0
    ) {
        appData = appData.copy(
            user = appData.user.copy(
                name = name.ifEmpty { "Unknown" },
                gender = gender
            ),
            settings = Settings(
                color = color,
                showHabits = true,
                gender = gender,
                prayerAlgorithm = prayerAlgorithm,
                difficulty = difficulty,
                daysPerWeek = daysPerWeek,
                trainingGoals = trainingGoals,
                equipmentTypes = equipmentTypes,
                showPrayers = showPrayers,
                prayerLatitude = appData.settings.prayerLatitude,
                prayerLongitude = appData.settings.prayerLongitude,
                notificationsEnabled = notificationsEnabled,
                workoutReminderEnabled = workoutReminderEnabled,
                workoutReminderHour = workoutReminderHour,
                workoutReminderMinute = workoutReminderMinute,
                prayerNotificationsEnabled = prayerNotificationsEnabled,
                prayerNotificationLeadMinutes = prayerNotificationLeadMinutes,
                streakWarningEnabled = streakWarningEnabled,
                streakWarningHour = streakWarningHour,
                streakWarningMinute = streakWarningMinute
            )
        )
        saveData()
        scheduleNotifications()
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
            QuestExercise(name = ex.name, amount = amount, done = false, timed = ex.timed, muscleGroup = ex.muscleGroup, equipment = ex.equipment)
        }

        // Don't reset habits, prayers - keep them for the day
        // Only reset quest exercises
        
        appData = appData.copy(
            quest = appData.quest.copy(exercises = newQuestExercises, extraExercises = listOf(), completed = false, usedPasscard = false)
        )
        calculatePrayers() // Refresh times for the new day
        saveData()
    }
}
