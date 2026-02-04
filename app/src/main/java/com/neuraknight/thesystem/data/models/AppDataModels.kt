package com.neuraknight.thesystem.data.models

data class AppData(
    var scaling: Scaling = Scaling(),
    val exercises: List<Exercise> = getDefaultExercises(),
    var user: User = User(),
    var quest: Quest = Quest(),
    var habits: MutableList<Habit> = mutableListOf(
        Habit(name = "wake up early", done = false),
        Habit(name = "eat healthy", done = false),
        Habit(name = "brush teeth", done = false)
    ),
    var customTasks: MutableList<CustomTask> = mutableListOf(),
    var settings: Settings = Settings(),
    var setupComplete: Boolean = false
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
    val timed: Boolean = false
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
    var `class`: String = "F",
    var streak: Int = 0,
    var passcards: Int = 0,
    var profileImg: String = ""
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
    var penalty: Int = 0
)

data class QuestExercise(
    val name: String,
    val amount: Int,
    var done: Boolean = false,
    val timed: Boolean
)

data class Habit(
    val name: String,
    var done: Boolean
)

data class CustomTask(
    val name: String,
    var done: Boolean
)

data class Settings(
    var color: String = "blue",
    var showHabits: Boolean = true,
    var showCustom: Boolean = true
)

fun getDefaultExercises(): List<Exercise> {
    return listOf(
        Exercise(name = "pushups", difficulty = 2, requiredLevel = 0, baseScale = 1.0, xpPerRep = 2.0, statGain = StatGain(STR = 0.1, AGI = 0.0, VIT = 0.0, END = 0.05, AP = 0.0)),
        Exercise(name = "wide_pushups", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.5, statGain = StatGain(STR = 0.12, AGI = 0.0, VIT = 0.0, END = 0.06, AP = 0.0)),
        Exercise(name = "diamond_pushups", difficulty = 4, requiredLevel = 10, baseScale = 0.6, xpPerRep = 3.0, statGain = StatGain(STR = 0.15, AGI = 0.0, VIT = 0.0, END = 0.07, AP = 0.0)),
        Exercise(name = "handstand_pushups", difficulty = 5, requiredLevel = 15, baseScale = 0.5, xpPerRep = 4.0, statGain = StatGain(STR = 0.2, AGI = 0.1, VIT = 0.0, END = 0.1, AP = 0.0)),
        Exercise(name = "situps", difficulty = 2, requiredLevel = 0, baseScale = 1.0, xpPerRep = 1.8, statGain = StatGain(STR = 0.0, AGI = 0.1, VIT = 0.0, END = 0.05, AP = 0.0)),
        Exercise(name = "crunches", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.0, AGI = 0.12, VIT = 0.0, END = 0.06, AP = 0.0)),
        Exercise(name = "squats", difficulty = 2, requiredLevel = 0, baseScale = 1.0, xpPerRep = 1.5, statGain = StatGain(STR = 0.08, AGI = 0.0, VIT = 0.07, END = 0.0, AP = 0.0)),
        Exercise(name = "lunges", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.1, AGI = 0.05, VIT = 0.05, END = 0.0, AP = 0.0)),
        Exercise(name = "plank", difficulty = 3, requiredLevel = 0, baseScale = 1.0, xpPerRep = 0.2, statGain = StatGain(STR = 0.0, AGI = 0.0, VIT = 0.1, END = 0.1, AP = 0.0), timed = true),
        Exercise(name = "side_plank", difficulty = 4, requiredLevel = 8, baseScale = 0.7, xpPerRep = 0.3, statGain = StatGain(STR = 0.0, AGI = 0.05, VIT = 0.12, END = 0.12, AP = 0.0), timed = true),
        Exercise(name = "calf_raises", difficulty = 1, requiredLevel = 0, baseScale = 1.2, xpPerRep = 1.0, statGain = StatGain(STR = 0.0, AGI = 0.0, VIT = 0.05, END = 0.1, AP = 0.0)),
        Exercise(name = "hand_grip", difficulty = 2, requiredLevel = 0, baseScale = 1.0, xpPerRep = 0.5, statGain = StatGain(STR = 0.05, AGI = 0.0, VIT = 0.0, END = 0.0, AP = 0.0)),
        Exercise(name = "dumbbell_press", difficulty = 3, requiredLevel = 0, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.1, AGI = 0.0, VIT = 0.0, END = 0.05, AP = 0.0)),
        Exercise(name = "dips", difficulty = 4, requiredLevel = 5, baseScale = 0.6, xpPerRep = 2.5, statGain = StatGain(STR = 0.1, AGI = 0.0, VIT = 0.0, END = 0.05, AP = 0.0)),
        Exercise(name = "burpees", difficulty = 4, requiredLevel = 5, baseScale = 0.7, xpPerRep = 3.0, statGain = StatGain(STR = 0.1, AGI = 0.1, VIT = 0.05, END = 0.1, AP = 0.0)),
        Exercise(name = "pull_ups", difficulty = 5, requiredLevel = 10, baseScale = 0.6, xpPerRep = 3.5, statGain = StatGain(STR = 0.15, AGI = 0.05, VIT = 0.0, END = 0.1, AP = 0.0)),
        Exercise(name = "chin_ups", difficulty = 5, requiredLevel = 10, baseScale = 0.6, xpPerRep = 3.5, statGain = StatGain(STR = 0.15, AGI = 0.05, VIT = 0.0, END = 0.1, AP = 0.0)),
        Exercise(name = "mountain_climbers", difficulty = 3, requiredLevel = 0, baseScale = 1.0, xpPerRep = 1.8, statGain = StatGain(STR = 0.05, AGI = 0.1, VIT = 0.05, END = 0.1, AP = 0.0)),
        Exercise(name = "jumping_jacks", difficulty = 2, requiredLevel = 0, baseScale = 1.2, xpPerRep = 1.0, statGain = StatGain(STR = 0.0, AGI = 0.05, VIT = 0.05, END = 0.1, AP = 0.0)),
        Exercise(name = "leg_raises", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.0, AGI = 0.1, VIT = 0.05, END = 0.05, AP = 0.0)),
        Exercise(name = "bicycle_crunches", difficulty = 3, requiredLevel = 5, baseScale = 0.8, xpPerRep = 2.0, statGain = StatGain(STR = 0.0, AGI = 0.12, VIT = 0.0, END = 0.06, AP = 0.0)),
        Exercise(name = "wall_sit", difficulty = 3, requiredLevel = 0, baseScale = 1.0, xpPerRep = 0.25, statGain = StatGain(STR = 0.05, AGI = 0.0, VIT = 0.05, END = 0.1, AP = 0.0), timed = true),
        Exercise(name = "bridge", difficulty = 3, requiredLevel = 0, baseScale = 1.0, xpPerRep = 0.2, statGain = StatGain(STR = 0.05, AGI = 0.0, VIT = 0.05, END = 0.05, AP = 0.0), timed = true)
    )
}
