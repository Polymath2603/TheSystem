package com.neuraknight.thesystem.data.models

import java.util.Date
import kotlin.math.floor
import kotlin.math.pow

data class DaySnapshot(
    val date: String = "",
    val totalXp: Double = 0.0,
    val level: Int = 0,
    val streak: Int = 0,
    val questCompleted: Boolean = false,
    val stats: Stats = Stats()
)

data class AppData(
    var scaling: Scaling = Scaling(),
    val exercises: List<Exercise> = getDefaultExercises(),
    var user: User = User(),
    var quest: Quest = Quest(),
    var history: List<DaySnapshot> = listOf(),
    var habits: MutableList<Habit> = mutableListOf(
        Habit(name = "wake up early", done = false, isCustom = false),
        Habit(name = "eat healthy", done = false, isCustom = false),
        Habit(name = "brush teeth", done = false, isCustom = false)
    ),
    var prayers: MutableList<Prayer> = mutableListOf(),
    var settings: Settings = Settings(),
    var setupComplete: Boolean = false
)

data class Prayer(
    val name: String = "",
    val time: String = "",
    var done: Boolean = false,
    val prayerTime: Date? = null
)


data class Scaling(
    var maxTime: Int = 700,
    var baseReps: Int = 10,
    var maxReps: Int = 250,
    var exponent3: Double = 1.35,
    var baseTime: Int = 10,
    var exponent2: Double = 1.8,
    var baseXP: Int = 80,
    var exponentFast: Double = 1.5,
    var fastLevelCap: Int = 10,
    var repsProgressSpeed: Int = 60,
    var apPerLevel: Int = 5
)

data class Exercise(
    val name: String = "",
    val difficulty: Int = 0,
    val requiredLevel: Int = 0,
    val baseScale: Double = 1.0,
    val xpPerRep: Double = 1.0,
    val statGain: StatGain = StatGain(),
    val timed: Boolean = false,
    val muscleGroups: List<String> = listOf("full"),
    val equipment: String = "bodyweight"
)

data class StatGain(
    val STR: Double = 0.0,
    val AGI: Double = 0.0,
    val VIT: Double = 0.0,
    val END: Double = 0.0,
    val AP: Double = 0.0
)

data class User(
    var level: Int = 0,
    var stats: Stats = Stats(),
    var name: String = "",
    var totalXp: Double = 0.0,
    var xpProgress: Double = 0.0,
    var xpNeeded: Double = 1.0,
    var characterClass: String = "F",
    var rank: String = "Bronze",
    var currentTitle: String = "Newbie",
    var streak: Int = 0,
    var passcards: Int = 0,
    var profileImg: String = "",
    var gender: String = "male"
)

data class Stats(
    var STR: Double = 10.0,
    var VIT: Double = 10.0,
    var AGI: Double = 10.0,
    var END: Double = 10.0,
    var AP: Int = 0
)

data class Quest(
    var nextReset: Long = System.currentTimeMillis(),
    var completed: Boolean = false,
    var exercises: List<QuestExercise> = listOf(),
    var extraExercises: List<QuestExercise> = listOf(),
    var usedPasscard: Boolean = false,
    var extraSetsRemaining: Int = 3
)

data class QuestExercise(
    val name: String = "",
    val amount: Int = 0,
    var done: Boolean = false,
    val timed: Boolean = false,
    val muscleGroups: List<String> = listOf("full"),
    val equipment: String = "bodyweight"
)

data class Habit(
    val name: String,
    var done: Boolean,
    var isCustom: Boolean = false
)

data class Settings(
    var color: String = "blue",
    var showPrayers: Boolean = false,
    var showHabits: Boolean = true,
    var prayerAlgorithm: String = "default",
    var prayerLatitude: Double = 51.5074,
    var prayerLongitude: Double = -0.1278,
    var workoutDays: List<Int> = listOf(1, 3, 5),
    var trainingGoals: List<String> = listOf("strength"),
    var equipmentTypes: List<String> = listOf("bodyweight", "dumbbell", "bar", "kettlebell", "resistance_band", "cable", "bench", "plate"),
    // Notification settings
    var notificationsEnabled: Boolean = true,
    var workoutReminderEnabled: Boolean = true,
    var workoutReminderHour: Int = 8,
    var workoutReminderMinute: Int = 0,
    var prayerNotificationsEnabled: Boolean = true,
    var prayerNotificationLeadMinutes: Int = 10,
    var streakWarningEnabled: Boolean = true,
    var streakWarningHour: Int = 21,
    var streakWarningMinute: Int = 0
)

fun getDefaultExercises(): List<Exercise> {
    return listOf(
        // Chest exercises
        Exercise(name = "pushups", difficulty = 2, requiredLevel = 0, baseScale = 1.0, xpPerRep = 2.0, statGain = StatGain(STR = 0.1, AGI = 0.0, VIT = 0.0, END = 0.05, AP = 0.0), muscleGroups = listOf("chest", "arms"), equipment = "bodyweight"),
        Exercise(name = "wide_pushups", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.0, VIT = 0.0, END = 0.06, AP = 0.0), muscleGroups = listOf("chest"), equipment = "bodyweight"),
        Exercise(name = "diamond_pushups", difficulty = 4, requiredLevel = 10, baseScale = 0.6, xpPerRep = 3.0, statGain = StatGain(STR = 0.15, AGI = 0.0, VIT = 0.0, END = 0.07, AP = 0.0), muscleGroups = listOf("chest", "arms"), equipment = "bodyweight"),
        Exercise(name = "handstand_pushups", difficulty = 5, requiredLevel = 15, baseScale = 0.5, xpPerRep = 4.0, statGain = StatGain(STR = 0.2, AGI = 0.1, VIT = 0.0, END = 0.1, AP = 0.0), muscleGroups = listOf("shoulders", "arms"), equipment = "bodyweight"),
        Exercise(name = "dips", difficulty = 4, requiredLevel = 5, baseScale = 0.6, xpPerRep = 2.5, statGain = StatGain(STR = 0.1, AGI = 0.0, VIT = 0.0, END = 0.05, AP = 0.0), muscleGroups = listOf("chest", "arms"), equipment = "bar"),
        Exercise(name = "incline_pushups", difficulty = 2, requiredLevel = 0, baseScale = 1.2, xpPerRep = 1.5, statGain = StatGain(STR = 0.08, AGI = 0.0, VIT = 0.0, END = 0.04, AP = 0.0), muscleGroups = listOf("chest"), equipment = "bodyweight"),
        Exercise(name = "decline_pushups", difficulty = 4, requiredLevel = 8, baseScale = 0.7, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.0, VIT = 0.0, END = 0.06, AP = 0.0), muscleGroups = listOf("chest", "shoulders"), equipment = "bodyweight"),
        Exercise(name = "dumbbell_press", difficulty = 3, requiredLevel = 0, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.1, AGI = 0.0, VIT = 0.0, END = 0.05, AP = 0.0), muscleGroups = listOf("chest"), equipment = "dumbbell"),
        Exercise(name = "dumbbell_flyes", difficulty = 3, requiredLevel = 5, baseScale = 0.7, xpPerRep = 2.2, statGain = StatGain(STR = 0.08, AGI = 0.0, VIT = 0.0, END = 0.04, AP = 0.0), muscleGroups = listOf("chest"), equipment = "dumbbell"),
        Exercise(name = "chest_press", difficulty = 4, requiredLevel = 10, baseScale = 0.6, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.0, VIT = 0.0, END = 0.06, AP = 0.0), muscleGroups = listOf("chest", "arms"), equipment = "bar"),
        Exercise(name = "cable_crossover", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.2, statGain = StatGain(STR = 0.1, AGI = 0.0, VIT = 0.0, END = 0.04, AP = 0.0), muscleGroups = listOf("chest"), equipment = "cable"),
        Exercise(name = "dumbbell_pullover", difficulty = 4, requiredLevel = 10, baseScale = 0.6, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.05, VIT = 0.0, END = 0.06, AP = 0.0), muscleGroups = listOf("chest", "back"), equipment = "dumbbell"),

        // Abs exercises
        Exercise(name = "situps", difficulty = 2, requiredLevel = 0, baseScale = 1.0, xpPerRep = 1.8, statGain = StatGain(STR = 0.08, AGI = 0.0, VIT = 0.0, END = 0.05, AP = 0.0), muscleGroups = listOf("abs"), equipment = "bodyweight"),
        Exercise(name = "crunches", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.1, AGI = 0.0, VIT = 0.0, END = 0.06, AP = 0.0), muscleGroups = listOf("abs"), equipment = "bodyweight"),
        Exercise(name = "leg_raises", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.0, AGI = 0.1, VIT = 0.05, END = 0.05, AP = 0.0), muscleGroups = listOf("abs"), equipment = "bodyweight"),
        Exercise(name = "bicycle_crunches", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.0, AGI = 0.12, VIT = 0.0, END = 0.06, AP = 0.0), muscleGroups = listOf("abs"), equipment = "bodyweight"),
        Exercise(name = "plank", difficulty = 3, requiredLevel = 0, baseScale = 1.0, xpPerRep = 0.2, statGain = StatGain(STR = 0.0, AGI = 0.0, VIT = 0.1, END = 0.1, AP = 0.0), timed = true, muscleGroups = listOf("abs"), equipment = "bodyweight"),
        Exercise(name = "side_plank", difficulty = 4, requiredLevel = 8, baseScale = 0.7, xpPerRep = 0.3, statGain = StatGain(STR = 0.0, AGI = 0.05, VIT = 0.12, END = 0.12, AP = 0.0), timed = true, muscleGroups = listOf("abs"), equipment = "bodyweight"),
        Exercise(name = "russian_twists", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 1.8, statGain = StatGain(STR = 0.0, AGI = 0.1, VIT = 0.0, END = 0.05, AP = 0.0), muscleGroups = listOf("abs"), equipment = "bodyweight"),
        Exercise(name = "mountain_climbers", difficulty = 3, requiredLevel = 0, baseScale = 1.0, xpPerRep = 1.8, statGain = StatGain(STR = 0.05, AGI = 0.1, VIT = 0.05, END = 0.1, AP = 0.0), muscleGroups = listOf("abs", "cardio"), equipment = "bodyweight"),
        Exercise(name = "hanging_knee_raises", difficulty = 4, requiredLevel = 10, baseScale = 0.6, xpPerRep = 2.5, statGain = StatGain(STR = 0.0, AGI = 0.12, VIT = 0.05, END = 0.05, AP = 0.0), muscleGroups = listOf("abs"), equipment = "bar"),
        Exercise(name = "v_ups", difficulty = 4, requiredLevel = 8, baseScale = 0.7, xpPerRep = 2.2, statGain = StatGain(STR = 0.0, AGI = 0.1, VIT = 0.05, END = 0.06, AP = 0.0), muscleGroups = listOf("abs"), equipment = "bodyweight"),
        Exercise(name = "flutter_kicks", difficulty = 3, requiredLevel = 5, baseScale = 0.9, xpPerRep = 1.8, statGain = StatGain(STR = 0.0, AGI = 0.1, VIT = 0.05, END = 0.08, AP = 0.0), muscleGroups = listOf("abs", "legs"), equipment = "bodyweight"),
        Exercise(name = "cable_crunches", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.08, AGI = 0.0, VIT = 0.0, END = 0.05, AP = 0.0), muscleGroups = listOf("abs"), equipment = "cable"),

        // Legs exercises
        Exercise(name = "squats", difficulty = 2, requiredLevel = 0, baseScale = 1.0, xpPerRep = 1.5, statGain = StatGain(STR = 0.08, AGI = 0.0, VIT = 0.07, END = 0.0, AP = 0.0), muscleGroups = listOf("legs"), equipment = "bodyweight"),
        Exercise(name = "lunges", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.1, AGI = 0.05, VIT = 0.05, END = 0.0, AP = 0.0), muscleGroups = listOf("legs"), equipment = "bodyweight"),
        Exercise(name = "calf_raises", difficulty = 1, requiredLevel = 0, baseScale = 1.2, xpPerRep = 1.0, statGain = StatGain(STR = 0.0, AGI = 0.0, VIT = 0.05, END = 0.1, AP = 0.0), muscleGroups = listOf("legs"), equipment = "bodyweight"),
        Exercise(name = "wall_sit", difficulty = 3, requiredLevel = 0, baseScale = 1.0, xpPerRep = 0.25, statGain = StatGain(STR = 0.05, AGI = 0.0, VIT = 0.05, END = 0.1, AP = 0.0), timed = true, muscleGroups = listOf("legs"), equipment = "bodyweight"),
        Exercise(name = "jump_squats", difficulty = 4, requiredLevel = 5, baseScale = 0.7, xpPerRep = 2.5, statGain = StatGain(STR = 0.1, AGI = 0.05, VIT = 0.05, END = 0.05, AP = 0.0), muscleGroups = listOf("legs", "cardio"), equipment = "bodyweight"),
        Exercise(name = "dumbbell_squats", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.1, AGI = 0.0, VIT = 0.08, END = 0.02, AP = 0.0), muscleGroups = listOf("legs"), equipment = "dumbbell"),
        Exercise(name = "romanian_deadlift", difficulty = 4, requiredLevel = 10, baseScale = 0.6, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.0, VIT = 0.05, END = 0.08, AP = 0.0), muscleGroups = listOf("legs", "back"), equipment = "bar"),
        Exercise(name = "leg_press", difficulty = 4, requiredLevel = 10, baseScale = 0.6, xpPerRep = 2.2, statGain = StatGain(STR = 0.1, AGI = 0.0, VIT = 0.06, END = 0.04, AP = 0.0), muscleGroups = listOf("legs"), equipment = "bar"),
        Exercise(name = "bulgarian_split_squats", difficulty = 4, requiredLevel = 8, baseScale = 0.7, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.05, VIT = 0.05, END = 0.03, AP = 0.0), muscleGroups = listOf("legs"), equipment = "dumbbell"),
        Exercise(name = "goblet_squats", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.1, AGI = 0.0, VIT = 0.08, END = 0.02, AP = 0.0), muscleGroups = listOf("legs"), equipment = "kettlebell"),
        Exercise(name = "hip_thrusts", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.12, AGI = 0.0, VIT = 0.05, END = 0.03, AP = 0.0), muscleGroups = listOf("legs"), equipment = "bodyweight"),
        Exercise(name = "step_ups", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.1, AGI = 0.05, VIT = 0.05, END = 0.02, AP = 0.0), muscleGroups = listOf("legs"), equipment = "bench"),
        Exercise(name = "weighted_calf_raises", difficulty = 2, requiredLevel = 5, baseScale = 1.0, xpPerRep = 1.5, statGain = StatGain(STR = 0.0, AGI = 0.0, VIT = 0.05, END = 0.12, AP = 0.0), muscleGroups = listOf("legs"), equipment = "dumbbell"),
        Exercise(name = "good_mornings", difficulty = 4, requiredLevel = 10, baseScale = 0.6, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.0, VIT = 0.05, END = 0.06, AP = 0.0), muscleGroups = listOf("legs", "back"), equipment = "bar"),

        // Back exercises
        Exercise(name = "pull_ups", difficulty = 5, requiredLevel = 10, baseScale = 0.6, xpPerRep = 3.5, statGain = StatGain(STR = 0.15, AGI = 0.05, VIT = 0.0, END = 0.1, AP = 0.0), muscleGroups = listOf("back", "arms"), equipment = "bar"),
        Exercise(name = "chin_ups", difficulty = 5, requiredLevel = 10, baseScale = 0.6, xpPerRep = 3.5, statGain = StatGain(STR = 0.15, AGI = 0.05, VIT = 0.0, END = 0.1, AP = 0.0), muscleGroups = listOf("back", "arms"), equipment = "bar"),
        Exercise(name = "deadlift", difficulty = 5, requiredLevel = 15, baseScale = 0.5, xpPerRep = 4.0, statGain = StatGain(STR = 0.2, AGI = 0.0, VIT = 0.08, END = 0.12, AP = 0.0), muscleGroups = listOf("back", "legs"), equipment = "bar"),
        Exercise(name = "rows", difficulty = 4, requiredLevel = 5, baseScale = 0.7, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.05, VIT = 0.0, END = 0.08, AP = 0.0), muscleGroups = listOf("back", "arms"), equipment = "dumbbell"),
        Exercise(name = "lat_pulldown", difficulty = 4, requiredLevel = 10, baseScale = 0.6, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.03, VIT = 0.0, END = 0.05, AP = 0.0), muscleGroups = listOf("back", "arms"), equipment = "bar"),
        Exercise(name = "cable_row", difficulty = 4, requiredLevel = 8, baseScale = 0.7, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.05, VIT = 0.0, END = 0.06, AP = 0.0), muscleGroups = listOf("back", "arms"), equipment = "cable"),
        Exercise(name = "superman", difficulty = 2, requiredLevel = 0, baseScale = 1.0, xpPerRep = 1.2, statGain = StatGain(STR = 0.08, AGI = 0.05, VIT = 0.05, END = 0.05, AP = 0.0), muscleGroups = listOf("back"), equipment = "bodyweight"),
        Exercise(name = "face_pull", difficulty = 3, requiredLevel = 8, baseScale = 0.7, xpPerRep = 2.0, statGain = StatGain(STR = 0.08, AGI = 0.05, VIT = 0.0, END = 0.05, AP = 0.0), muscleGroups = listOf("back", "shoulders"), equipment = "cable"),
        Exercise(name = "reverse_flyes", difficulty = 3, requiredLevel = 5, baseScale = 0.7, xpPerRep = 2.0, statGain = StatGain(STR = 0.05, AGI = 0.08, VIT = 0.0, END = 0.04, AP = 0.0), muscleGroups = listOf("back", "shoulders"), equipment = "dumbbell"),
        Exercise(name = "shrugs", difficulty = 2, requiredLevel = 5, baseScale = 1.0, xpPerRep = 1.5, statGain = StatGain(STR = 0.08, AGI = 0.0, VIT = 0.0, END = 0.03, AP = 0.0), muscleGroups = listOf("back", "shoulders"), equipment = "dumbbell"),

        // Shoulders exercises
        Exercise(name = "pike_pushups", difficulty = 4, requiredLevel = 5, baseScale = 0.7, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.05, VIT = 0.0, END = 0.08, AP = 0.0), muscleGroups = listOf("shoulders", "arms"), equipment = "bodyweight"),
        Exercise(name = "dumbbell_shoulder_press", difficulty = 4, requiredLevel = 5, baseScale = 0.7, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.03, VIT = 0.0, END = 0.05, AP = 0.0), muscleGroups = listOf("shoulders", "arms"), equipment = "dumbbell"),
        Exercise(name = "lateral_raises", difficulty = 3, requiredLevel = 0, baseScale = 0.8, xpPerRep = 1.8, statGain = StatGain(STR = 0.05, AGI = 0.05, VIT = 0.0, END = 0.04, AP = 0.0), muscleGroups = listOf("shoulders"), equipment = "dumbbell"),
        Exercise(name = "front_raises", difficulty = 3, requiredLevel = 0, baseScale = 0.8, xpPerRep = 1.8, statGain = StatGain(STR = 0.05, AGI = 0.05, VIT = 0.0, END = 0.04, AP = 0.0), muscleGroups = listOf("shoulders"), equipment = "dumbbell"),
        Exercise(name = "arnold_press", difficulty = 5, requiredLevel = 10, baseScale = 0.5, xpPerRep = 3.0, statGain = StatGain(STR = 0.15, AGI = 0.05, VIT = 0.0, END = 0.06, AP = 0.0), muscleGroups = listOf("shoulders", "arms"), equipment = "dumbbell"),
        Exercise(name = "upright_rows", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.2, statGain = StatGain(STR = 0.08, AGI = 0.08, VIT = 0.0, END = 0.04, AP = 0.0), muscleGroups = listOf("shoulders", "back"), equipment = "dumbbell"),
        Exercise(name = "overhead_press", difficulty = 4, requiredLevel = 10, baseScale = 0.6, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.05, VIT = 0.0, END = 0.06, AP = 0.0), muscleGroups = listOf("shoulders", "arms"), equipment = "bar"),

        // Arms exercises
        Exercise(name = "hand_grip", difficulty = 2, requiredLevel = 0, baseScale = 1.0, xpPerRep = 0.5, statGain = StatGain(STR = 0.05, AGI = 0.0, VIT = 0.0, END = 0.0, AP = 0.0), muscleGroups = listOf("arms"), equipment = "bodyweight"),
        Exercise(name = "tricep_pushdowns", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.08, AGI = 0.0, VIT = 0.0, END = 0.04, AP = 0.0), muscleGroups = listOf("arms"), equipment = "cable"),
        Exercise(name = "bicep_curls", difficulty = 3, requiredLevel = 0, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.08, AGI = 0.02, VIT = 0.0, END = 0.02, AP = 0.0), muscleGroups = listOf("arms"), equipment = "dumbbell"),
        Exercise(name = "hammer_curls", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.2, statGain = StatGain(STR = 0.1, AGI = 0.03, VIT = 0.0, END = 0.02, AP = 0.0), muscleGroups = listOf("arms"), equipment = "dumbbell"),
        Exercise(name = "skull_crushers", difficulty = 4, requiredLevel = 10, baseScale = 0.6, xpPerRep = 2.5, statGain = StatGain(STR = 0.1, AGI = 0.0, VIT = 0.0, END = 0.05, AP = 0.0), muscleGroups = listOf("arms"), equipment = "dumbbell"),
        Exercise(name = "concentration_curls", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.08, AGI = 0.02, VIT = 0.0, END = 0.02, AP = 0.0), muscleGroups = listOf("arms"), equipment = "dumbbell"),
        Exercise(name = "overhead_tricep_extension", difficulty = 3, requiredLevel = 5, baseScale = 0.7, xpPerRep = 2.0, statGain = StatGain(STR = 0.08, AGI = 0.0, VIT = 0.0, END = 0.04, AP = 0.0), muscleGroups = listOf("arms"), equipment = "dumbbell"),
        Exercise(name = "wrist_curls", difficulty = 2, requiredLevel = 0, baseScale = 1.0, xpPerRep = 1.0, statGain = StatGain(STR = 0.05, AGI = 0.0, VIT = 0.0, END = 0.0, AP = 0.0), muscleGroups = listOf("arms"), equipment = "dumbbell"),
        Exercise(name = "reverse_curls", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 1.8, statGain = StatGain(STR = 0.06, AGI = 0.03, VIT = 0.0, END = 0.02, AP = 0.0), muscleGroups = listOf("arms"), equipment = "dumbbell"),
        Exercise(name = "tricep_dips", difficulty = 4, requiredLevel = 5, baseScale = 0.7, xpPerRep = 2.5, statGain = StatGain(STR = 0.1, AGI = 0.0, VIT = 0.0, END = 0.05, AP = 0.0), muscleGroups = listOf("arms"), equipment = "bench"),

        // Cardio/Full body
        Exercise(name = "burpees", difficulty = 4, requiredLevel = 5, baseScale = 0.7, xpPerRep = 3.0, statGain = StatGain(STR = 0.1, AGI = 0.1, VIT = 0.05, END = 0.1, AP = 0.0), muscleGroups = listOf("full", "cardio"), equipment = "bodyweight"),
        Exercise(name = "jumping_jacks", difficulty = 2, requiredLevel = 0, baseScale = 1.2, xpPerRep = 1.0, statGain = StatGain(STR = 0.0, AGI = 0.05, VIT = 0.05, END = 0.1, AP = 0.0), muscleGroups = listOf("full", "cardio"), equipment = "bodyweight"),
        Exercise(name = "high_knees", difficulty = 3, requiredLevel = 0, baseScale = 1.0, xpPerRep = 1.5, statGain = StatGain(STR = 0.0, AGI = 0.1, VIT = 0.05, END = 0.08, AP = 0.0), muscleGroups = listOf("cardio", "legs"), equipment = "bodyweight"),
        Exercise(name = "box_jumps", difficulty = 4, requiredLevel = 5, baseScale = 0.7, xpPerRep = 2.5, statGain = StatGain(STR = 0.1, AGI = 0.1, VIT = 0.05, END = 0.1, AP = 0.0), muscleGroups = listOf("legs", "cardio"), equipment = "bench"),
        Exercise(name = "running_in_place", difficulty = 2, requiredLevel = 0, baseScale = 1.0, xpPerRep = 0.5, statGain = StatGain(STR = 0.0, AGI = 0.08, VIT = 0.08, END = 0.12, AP = 0.0), timed = true, muscleGroups = listOf("cardio"), equipment = "bodyweight"),
        Exercise(name = "jump_rope", difficulty = 4, requiredLevel = 5, baseScale = 0.8, xpPerRep = 1.5, statGain = StatGain(STR = 0.0, AGI = 0.15, VIT = 0.05, END = 0.1, AP = 0.0), muscleGroups = listOf("cardio", "legs"), equipment = "bodyweight"),
        Exercise(name = "kettlebell_swings", difficulty = 4, requiredLevel = 8, baseScale = 0.7, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.05, VIT = 0.05, END = 0.1, AP = 0.0), muscleGroups = listOf("full", "cardio"), equipment = "kettlebell"),
        Exercise(name = "burpee_pull_ups", difficulty = 5, requiredLevel = 15, baseScale = 0.5, xpPerRep = 4.0, statGain = StatGain(STR = 0.15, AGI = 0.15, VIT = 0.05, END = 0.15, AP = 0.0), muscleGroups = listOf("full", "cardio", "back", "arms"), equipment = "bar"),
        Exercise(name = "bear_crawls", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.08, AGI = 0.1, VIT = 0.05, END = 0.1, AP = 0.0), muscleGroups = listOf("full", "cardio"), equipment = "bodyweight"),
        Exercise(name = "battle_ropes", difficulty = 4, requiredLevel = 8, baseScale = 0.7, xpPerRep = 0.3, statGain = StatGain(STR = 0.1, AGI = 0.08, VIT = 0.05, END = 0.12, AP = 0.0), timed = true, muscleGroups = listOf("arms", "cardio", "full"), equipment = "cable"),
        Exercise(name = "rowing", difficulty = 4, requiredLevel = 8, baseScale = 0.7, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.05, VIT = 0.08, END = 0.15, AP = 0.0), muscleGroups = listOf("back", "legs", "cardio"), equipment = "cable"),
        Exercise(name = "resistance_band_rows", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.1, AGI = 0.03, VIT = 0.0, END = 0.05, AP = 0.0), muscleGroups = listOf("back", "arms"), equipment = "resistance_band"),
        Exercise(name = "band_pull_apart", difficulty = 2, requiredLevel = 0, baseScale = 1.0, xpPerRep = 1.5, statGain = StatGain(STR = 0.05, AGI = 0.03, VIT = 0.0, END = 0.03, AP = 0.0), muscleGroups = listOf("back", "shoulders"), equipment = "resistance_band"),

        // Flexibility/Balance
        Exercise(name = "bridge", difficulty = 3, requiredLevel = 0, baseScale = 1.0, xpPerRep = 0.2, statGain = StatGain(STR = 0.05, AGI = 0.0, VIT = 0.05, END = 0.05, AP = 0.0), timed = true, muscleGroups = listOf("back"), equipment = "bodyweight"),
        Exercise(name = "cat_cow", difficulty = 1, requiredLevel = 0, baseScale = 1.2, xpPerRep = 0.5, statGain = StatGain(STR = 0.0, AGI = 0.05, VIT = 0.08, END = 0.05, AP = 0.0), muscleGroups = listOf("back"), equipment = "bodyweight"),
        Exercise(name = "cobra_stretch", difficulty = 1, requiredLevel = 0, baseScale = 1.2, xpPerRep = 0.5, statGain = StatGain(STR = 0.0, AGI = 0.03, VIT = 0.08, END = 0.05, AP = 0.0), muscleGroups = listOf("abs"), equipment = "bodyweight")
    )
}
