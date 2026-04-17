package com.neuraknight.thesystem.data.models

import java.util.Date
import kotlin.math.floor
import kotlin.math.pow

data class AppData(
    var scaling: Scaling = Scaling(),
    val exercises: List<Exercise> = getDefaultExercises(),
    var user: User = User(),
    var quest: Quest = Quest(),
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
    val name: String,
    val time: String,
    var done: Boolean = false,
    val prayerTime: Date? = null
)


data class Scaling(
    var maxTime: Int = 700,
    var maxXP: Int = 3000000,
    var baseReps: Int = 10,
    var maxReps: Int = 250,
    var exponent3: Double = 1.35,
    var baseTime: Int = 10,
    var exponent2: Double = 1.8,
    var baseXP: Int = 1,
    var exponent: Double = 0.7,
    var exponentFast: Double = 0.3,
    var fastLevelCap: Int = 10,
    var repsProgressSpeed: Int = 60,
    var apPerLevel: Int = 5
)

data class Exercise(
    val name: String,
    val difficulty: Int,
    val requiredLevel: Int,
    val baseScale: Double,
    val xpPerRep: Double,
    val statGain: StatGain,
    val timed: Boolean = false,
    val muscleGroup: String = "full",
    val equipment: String = "bodyweight"
)

data class StatGain(
    val STR: Double,
    val AGI: Double,
    val VIT: Double,
    val END: Double,
    val AP: Double
)

data class User(
    var level: Int = 0,
    var stats: Stats = Stats(),
    var name: String = "",
    var totalXp: Double = 0.0,
    var xpProgress: Double = 0.0,
    var xpNeeded: Double = 1.0,
    var type: String = "Knight",
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
    var penalty: Int = 0
)

data class QuestExercise(
    val name: String,
    val amount: Int,
    var done: Boolean = false,
    val timed: Boolean,
    val muscleGroup: String = "full",
    val equipment: String = "bodyweight"
)

data class Habit(
    val name: String,
    var done: Boolean,
    var isCustom: Boolean = false
)

data class Settings(
    var color: String = "blue",
    var showHabits: Boolean = true,
    var showPrayers: Boolean = true,
    var gender: String = "male",
    var prayerAlgorithm: String = "default",
    var prayerLatitude: Double = 51.5074,
    var prayerLongitude: Double = -0.1278,
    var difficulty: String = "beginner",
    var daysPerWeek: Int = 3,
    var trainingGoals: List<String> = listOf("strength"),
    var equipmentTypes: List<String> = listOf("bodyweight")
)

fun getDefaultExercises(): List<Exercise> {
    return listOf(
        // Chest exercises
        Exercise(name = "pushups", difficulty = 2, requiredLevel = 0, baseScale = 1.0, xpPerRep = 2.0, statGain = StatGain(STR = 0.1, AGI = 0.0, VIT = 0.0, END = 0.05, AP = 0.0), muscleGroup = "chest", equipment = "bodyweight"),
        Exercise(name = "wide_pushups", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.0, VIT = 0.0, END = 0.06, AP = 0.0), muscleGroup = "chest", equipment = "bodyweight"),
        Exercise(name = "diamond_pushups", difficulty = 4, requiredLevel = 10, baseScale = 0.6, xpPerRep = 3.0, statGain = StatGain(STR = 0.15, AGI = 0.0, VIT = 0.0, END = 0.07, AP = 0.0), muscleGroup = "chest", equipment = "bodyweight"),
        Exercise(name = "handstand_pushups", difficulty = 5, requiredLevel = 15, baseScale = 0.5, xpPerRep = 4.0, statGain = StatGain(STR = 0.2, AGI = 0.1, VIT = 0.0, END = 0.1, AP = 0.0), muscleGroup = "chest", equipment = "bodyweight"),
        Exercise(name = "dips", difficulty = 4, requiredLevel = 5, baseScale = 0.6, xpPerRep = 2.5, statGain = StatGain(STR = 0.1, AGI = 0.0, VIT = 0.0, END = 0.05, AP = 0.0), muscleGroup = "chest", equipment = "bar"),
        Exercise(name = "dumbbell_press", difficulty = 3, requiredLevel = 0, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.1, AGI = 0.0, VIT = 0.0, END = 0.05, AP = 0.0), muscleGroup = "chest", equipment = "dumbbell"),
        Exercise(name = "dumbbell_flyes", difficulty = 3, requiredLevel = 5, baseScale = 0.7, xpPerRep = 2.2, statGain = StatGain(STR = 0.08, AGI = 0.0, VIT = 0.0, END = 0.04, AP = 0.0), muscleGroup = "chest", equipment = "dumbbell"),
        Exercise(name = "chest_press", difficulty = 4, requiredLevel = 10, baseScale = 0.6, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.0, VIT = 0.0, END = 0.06, AP = 0.0), muscleGroup = "chest", equipment = "bar"),
        
        // Abs exercises
        Exercise(name = "situps", difficulty = 2, requiredLevel = 0, baseScale = 1.0, xpPerRep = 1.8, statGain = StatGain(STR = 0.0, AGI = 0.1, VIT = 0.0, END = 0.05, AP = 0.0), muscleGroup = "abs", equipment = "bodyweight"),
        Exercise(name = "crunches", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.0, AGI = 0.12, VIT = 0.0, END = 0.06, AP = 0.0), muscleGroup = "abs", equipment = "bodyweight"),
        Exercise(name = "leg_raises", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.0, AGI = 0.1, VIT = 0.05, END = 0.05, AP = 0.0), muscleGroup = "abs", equipment = "bodyweight"),
        Exercise(name = "bicycle_crunches", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.0, AGI = 0.12, VIT = 0.0, END = 0.06, AP = 0.0), muscleGroup = "abs", equipment = "bodyweight"),
        Exercise(name = "plank", difficulty = 3, requiredLevel = 0, baseScale = 1.0, xpPerRep = 0.2, statGain = StatGain(STR = 0.0, AGI = 0.0, VIT = 0.1, END = 0.1, AP = 0.0), timed = true, muscleGroup = "abs", equipment = "bodyweight"),
        Exercise(name = "side_plank", difficulty = 4, requiredLevel = 8, baseScale = 0.7, xpPerRep = 0.3, statGain = StatGain(STR = 0.0, AGI = 0.05, VIT = 0.12, END = 0.12, AP = 0.0), timed = true, muscleGroup = "abs", equipment = "bodyweight"),
        Exercise(name = "russian_twists", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 1.8, statGain = StatGain(STR = 0.0, AGI = 0.1, VIT = 0.0, END = 0.05, AP = 0.0), muscleGroup = "abs", equipment = "bodyweight"),
        Exercise(name = "mountain_climbers", difficulty = 3, requiredLevel = 0, baseScale = 1.0, xpPerRep = 1.8, statGain = StatGain(STR = 0.05, AGI = 0.1, VIT = 0.05, END = 0.1, AP = 0.0), muscleGroup = "abs", equipment = "bodyweight"),
        
        // Legs exercises
        Exercise(name = "squats", difficulty = 2, requiredLevel = 0, baseScale = 1.0, xpPerRep = 1.5, statGain = StatGain(STR = 0.08, AGI = 0.0, VIT = 0.07, END = 0.0, AP = 0.0), muscleGroup = "legs", equipment = "bodyweight"),
        Exercise(name = "lunges", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.1, AGI = 0.05, VIT = 0.05, END = 0.0, AP = 0.0), muscleGroup = "legs", equipment = "bodyweight"),
        Exercise(name = "calf_raises", difficulty = 1, requiredLevel = 0, baseScale = 1.2, xpPerRep = 1.0, statGain = StatGain(STR = 0.0, AGI = 0.0, VIT = 0.05, END = 0.1, AP = 0.0), muscleGroup = "legs", equipment = "bodyweight"),
        Exercise(name = "wall_sit", difficulty = 3, requiredLevel = 0, baseScale = 1.0, xpPerRep = 0.25, statGain = StatGain(STR = 0.05, AGI = 0.0, VIT = 0.05, END = 0.1, AP = 0.0), timed = true, muscleGroup = "legs", equipment = "bodyweight"),
        Exercise(name = "jump_squats", difficulty = 4, requiredLevel = 5, baseScale = 0.7, xpPerRep = 2.5, statGain = StatGain(STR = 0.1, AGI = 0.05, VIT = 0.05, END = 0.05, AP = 0.0), muscleGroup = "legs", equipment = "bodyweight"),
        Exercise(name = "dumbbell_squats", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.1, AGI = 0.0, VIT = 0.08, END = 0.02, AP = 0.0), muscleGroup = "legs", equipment = "dumbbell"),
        Exercise(name = "romanian_deadlift", difficulty = 4, requiredLevel = 10, baseScale = 0.6, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.0, VIT = 0.05, END = 0.08, AP = 0.0), muscleGroup = "legs", equipment = "bar"),
        Exercise(name = "leg_press", difficulty = 4, requiredLevel = 10, baseScale = 0.6, xpPerRep = 2.2, statGain = StatGain(STR = 0.1, AGI = 0.0, VIT = 0.06, END = 0.04, AP = 0.0), muscleGroup = "legs", equipment = "bar"),
        
        // Back exercises
        Exercise(name = "pull_ups", difficulty = 5, requiredLevel = 10, baseScale = 0.6, xpPerRep = 3.5, statGain = StatGain(STR = 0.15, AGI = 0.05, VIT = 0.0, END = 0.1, AP = 0.0), muscleGroup = "back", equipment = "bar"),
        Exercise(name = "chin_ups", difficulty = 5, requiredLevel = 10, baseScale = 0.6, xpPerRep = 3.5, statGain = StatGain(STR = 0.15, AGI = 0.05, VIT = 0.0, END = 0.1, AP = 0.0), muscleGroup = "back", equipment = "bar"),
        Exercise(name = "deadlift", difficulty = 5, requiredLevel = 15, baseScale = 0.5, xpPerRep = 4.0, statGain = StatGain(STR = 0.2, AGI = 0.0, VIT = 0.08, END = 0.12, AP = 0.0), muscleGroup = "back", equipment = "bar"),
        Exercise(name = "rows", difficulty = 4, requiredLevel = 5, baseScale = 0.7, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.05, VIT = 0.0, END = 0.08, AP = 0.0), muscleGroup = "back", equipment = "dumbbell"),
        Exercise(name = "lat_pulldown", difficulty = 4, requiredLevel = 10, baseScale = 0.6, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.03, VIT = 0.0, END = 0.05, AP = 0.0), muscleGroup = "back", equipment = "bar"),
        Exercise(name = "superman", difficulty = 2, requiredLevel = 0, baseScale = 1.0, xpPerRep = 1.2, statGain = StatGain(STR = 0.08, AGI = 0.05, VIT = 0.05, END = 0.05, AP = 0.0), muscleGroup = "back", equipment = "bodyweight"),
        
        // Shoulders exercises
        Exercise(name = "pike_pushups", difficulty = 4, requiredLevel = 5, baseScale = 0.7, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.05, VIT = 0.0, END = 0.08, AP = 0.0), muscleGroup = "shoulders", equipment = "bodyweight"),
        Exercise(name = "dumbbell_shoulder_press", difficulty = 4, requiredLevel = 5, baseScale = 0.7, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.03, VIT = 0.0, END = 0.05, AP = 0.0), muscleGroup = "shoulders", equipment = "dumbbell"),
        Exercise(name = "lateral_raises", difficulty = 3, requiredLevel = 0, baseScale = 0.8, xpPerRep = 1.8, statGain = StatGain(STR = 0.05, AGI = 0.05, VIT = 0.0, END = 0.04, AP = 0.0), muscleGroup = "shoulders", equipment = "dumbbell"),
        Exercise(name = "front_raises", difficulty = 3, requiredLevel = 0, baseScale = 0.8, xpPerRep = 1.8, statGain = StatGain(STR = 0.05, AGI = 0.05, VIT = 0.0, END = 0.04, AP = 0.0), muscleGroup = "shoulders", equipment = "dumbbell"),
        Exercise(name = "arnold_press", difficulty = 5, requiredLevel = 10, baseScale = 0.5, xpPerRep = 3.0, statGain = StatGain(STR = 0.15, AGI = 0.05, VIT = 0.0, END = 0.06, AP = 0.0), muscleGroup = "shoulders", equipment = "dumbbell"),
        
        // Arms exercises
        Exercise(name = "hand_grip", difficulty = 2, requiredLevel = 0, baseScale = 1.0, xpPerRep = 0.5, statGain = StatGain(STR = 0.05, AGI = 0.0, VIT = 0.0, END = 0.0, AP = 0.0), muscleGroup = "arms", equipment = "bodyweight"),
        Exercise(name = "tricep_pushdowns", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.08, AGI = 0.0, VIT = 0.0, END = 0.04, AP = 0.0), muscleGroup = "arms", equipment = "dumbbell"),
        Exercise(name = "bicep_curls", difficulty = 3, requiredLevel = 0, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.08, AGI = 0.02, VIT = 0.0, END = 0.02, AP = 0.0), muscleGroup = "arms", equipment = "dumbbell"),
        Exercise(name = "hammer_curls", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.2, statGain = StatGain(STR = 0.1, AGI = 0.03, VIT = 0.0, END = 0.02, AP = 0.0), muscleGroup = "arms", equipment = "dumbbell"),
        Exercise(name = "skull_crushers", difficulty = 4, requiredLevel = 10, baseScale = 0.6, xpPerRep = 2.5, statGain = StatGain(STR = 0.1, AGI = 0.0, VIT = 0.0, END = 0.05, AP = 0.0), muscleGroup = "arms", equipment = "dumbbell"),
        
        // Cardio/Full body
        Exercise(name = "burpees", difficulty = 4, requiredLevel = 5, baseScale = 0.7, xpPerRep = 3.0, statGain = StatGain(STR = 0.1, AGI = 0.1, VIT = 0.05, END = 0.1, AP = 0.0), muscleGroup = "full", equipment = "bodyweight"),
        Exercise(name = "jumping_jacks", difficulty = 2, requiredLevel = 0, baseScale = 1.2, xpPerRep = 1.0, statGain = StatGain(STR = 0.0, AGI = 0.05, VIT = 0.05, END = 0.1, AP = 0.0), muscleGroup = "full", equipment = "bodyweight"),
        Exercise(name = "high_knees", difficulty = 3, requiredLevel = 0, baseScale = 1.0, xpPerRep = 1.5, statGain = StatGain(STR = 0.0, AGI = 0.1, VIT = 0.05, END = 0.08, AP = 0.0), muscleGroup = "full", equipment = "bodyweight"),
        Exercise(name = "box_jumps", difficulty = 4, requiredLevel = 5, baseScale = 0.7, xpPerRep = 2.5, statGain = StatGain(STR = 0.1, AGI = 0.1, VIT = 0.05, END = 0.1, AP = 0.0), muscleGroup = "legs", equipment = "bodyweight"),
        Exercise(name = "running_in_place", difficulty = 2, requiredLevel = 0, baseScale = 1.0, xpPerRep = 0.5, statGain = StatGain(STR = 0.0, AGI = 0.08, VIT = 0.08, END = 0.12, AP = 0.0), timed = true, muscleGroup = "cardio", equipment = "bodyweight"),
        Exercise(name = "jump_rope", difficulty = 4, requiredLevel = 5, baseScale = 0.8, xpPerRep = 1.5, statGain = StatGain(STR = 0.0, AGI = 0.15, VIT = 0.05, END = 0.1, AP = 0.0), muscleGroup = "cardio", equipment = "bodyweight"),
        
        // Flexibility/Balance
        Exercise(name = "bridge", difficulty = 3, requiredLevel = 0, baseScale = 1.0, xpPerRep = 0.2, statGain = StatGain(STR = 0.05, AGI = 0.0, VIT = 0.05, END = 0.05, AP = 0.0), timed = true, muscleGroup = "back", equipment = "bodyweight"),
        Exercise(name = "cat_cow", difficulty = 1, requiredLevel = 0, baseScale = 1.2, xpPerRep = 0.5, statGain = StatGain(STR = 0.0, AGI = 0.05, VIT = 0.08, END = 0.05, AP = 0.0), muscleGroup = "back", equipment = "bodyweight"),
        Exercise(name = "cobra_stretch", difficulty = 1, requiredLevel = 0, baseScale = 1.2, xpPerRep = 0.5, statGain = StatGain(STR = 0.0, AGI = 0.03, VIT = 0.08, END = 0.05, AP = 0.0), muscleGroup = "abs", equipment = "bodyweight")
    )
}
