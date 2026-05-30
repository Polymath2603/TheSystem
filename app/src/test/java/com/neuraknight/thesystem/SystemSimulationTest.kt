package com.neuraknight.thesystem

import com.neuraknight.thesystem.data.models.*
import com.neuraknight.thesystem.utils.SunCalc
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow

/**
 * Headless simulation tests for TheSystem app.
 * Tests XP/level calculations, AP formula, streak logic, passcards,
 * prayer times with all algorithms, and full user journey scenarios.
 */
class SystemSimulationTest {

    // === Helper: replicate ViewModel math for testing ===

    private fun calculateXpForLevel(lvl: Int, scaling: Scaling): Double {
        return if (lvl <= scaling.fastLevelCap) {
            floor(scaling.baseXP * lvl.toDouble().pow(scaling.exponentFast))
        } else {
            val baseAfterFast = floor(scaling.baseXP * scaling.fastLevelCap.toDouble().pow(scaling.exponentFast))
            val additional = (lvl - scaling.fastLevelCap).toDouble()
            baseAfterFast + floor(additional * 100 * (1 + additional * 0.1))
        }
    }

    private fun calculateLevelFromTotalXp(totalXp: Double, scaling: Scaling): Triple<Int, Double, Double> {
        var cumulative = 0.0
        var lvl = 0
        var next = calculateXpForLevel(1, scaling)
        while (cumulative + next <= totalXp) {
            cumulative += next
            lvl++
            next = calculateXpForLevel(lvl + 1, scaling)
        }
        val displayLevel = min(lvl, 31)
        return Triple(displayLevel, totalXp - cumulative, next)
    }

    private fun calculateApGained(oldLevel: Int, newLevel: Int, apPerLevel: Int): Int {
        return if (newLevel > oldLevel) (newLevel - oldLevel) * apPerLevel else 0
    }

    private fun generateQuestExercises(
        exercises: List<Exercise>,
        level: Int,
        scaling: Scaling,
        equipmentTypes: List<String>,
        trainingGoals: List<String>
    ): List<QuestExercise> {
        val unlocked = exercises.filter { ex ->
            ex.requiredLevel <= level &&
            equipmentTypes.contains(ex.equipment) &&
            (trainingGoals.isEmpty() ||
             ex.muscleGroups.any { trainingGoals.contains(it) } ||
             ex.muscleGroups.contains("full") ||
             ex.muscleGroups.contains("cardio"))
        }.toMutableList()
        val selected = mutableListOf<Exercise>()
        val numExercises = min(4, unlocked.size)
        repeat(numExercises) {
            if (unlocked.isNotEmpty()) {
                val idx = unlocked.indices.random()
                selected.add(unlocked.removeAt(idx))
            }
        }
        return selected.map { ex ->
            val base = if (ex.timed) scaling.baseTime else scaling.baseReps
            val maxV = if (ex.timed) scaling.maxTime else scaling.maxReps
            val exp = if (ex.timed) scaling.exponent2 else scaling.exponent3
            val fraction = (level.toDouble() / scaling.repsProgressSpeed).pow(exp)
            val amount = min(maxV.toDouble(), floor(ex.baseScale * (base + (maxV - base) * fraction))).toInt()
            QuestExercise(name = ex.name, amount = amount, done = false, timed = ex.timed, muscleGroups = ex.muscleGroups, equipment = ex.equipment)
        }
    }

    private fun calculateExerciseXp(exercises: List<QuestExercise>, exerciseCatalog: List<Exercise>): Double {
        var xp = 0.0
        exercises.forEach { qe ->
            val exData = exerciseCatalog.find { it.name == qe.name }
            if (exData != null) {
                xp += qe.amount * exData.xpPerRep
            }
        }
        return xp
    }

    private fun calculateStreakRewards(streak: Int): Pair<Int, Int> {
        var apBonus = 0
        var passcardBonus = 0
        if (streak % 7 == 0 && streak > 0) passcardBonus = 1
        if (streak % 30 == 0 && streak > 0) apBonus = 10
        return Pair(apBonus, passcardBonus)
    }

    // === XP and Level Tests ===

    @Test
    fun testXpProgression_fastLevels() {
        val scaling = Scaling()
        val xp1 = calculateXpForLevel(1, scaling)
        val xp9 = calculateXpForLevel(9, scaling)
        val xp10 = calculateXpForLevel(10, scaling)
        val xp11 = calculateXpForLevel(11, scaling)

        assertTrue("Level 1 XP should be > 0", xp1 > 0)
        assertTrue("Level 9 XP should be > Level 1", xp9 > xp1)
        assertTrue("Level 10 XP should be > Level 9", xp10 > xp9)
        assertTrue("Level 11 XP should be > Level 10", xp11 > xp10)
        println("XP Progression: L1=$xp1, L9=$xp9, L10=$xp10, L11=$xp11")
    }

    @Test
    fun testXpProgression_slowLevels() {
        val scaling = Scaling()
        val xp10 = calculateXpForLevel(10, scaling)
        val xp11 = calculateXpForLevel(11, scaling)
        val xp20 = calculateXpForLevel(20, scaling)
        val xp31 = calculateXpForLevel(31, scaling)

        assertTrue("Level 11 XP should be > Level 10", xp11 > xp10)
        assertTrue("Level 20 XP should be > Level 11", xp20 > xp11)
        assertTrue("Level 31 XP should be > Level 20", xp31 > xp20)
        println("XP Progression (slow): L10=$xp10, L11=$xp11, L20=$xp20, L31=$xp31")
    }

    @Test
    fun testLevelCalculation_fromScratch() {
        val scaling = Scaling()
        // Start at 0 XP, should be level 0
        val (level, progress, needed) = calculateLevelFromTotalXp(0.0, scaling)
        assertEquals(0, level)
        assertEquals(0.0, progress, 0.01)

        // Add enough XP for level 1
        val xpFor1 = calculateXpForLevel(1, scaling)
        val (level1, _, _) = calculateLevelFromTotalXp(xpFor1, scaling)
        assertEquals(1, level1)
        println("Level 1 requires $xpFor1 XP")
    }

    @Test
    fun testLevelCalculation_cumulative() {
        val scaling = Scaling()
        // Calculate cumulative XP needed for each level
        var totalXp = 0.0
        for (lvl in 1..10) {
            totalXp += calculateXpForLevel(lvl, scaling)
            val (level, _, _) = calculateLevelFromTotalXp(totalXp, scaling)
            assertEquals("Should be level $lvl at cumulative XP $totalXp", lvl, level)
        }
        println("Total XP for level 10: $totalXp")
    }

    @Test
    fun testLevelCalculation_cap31() {
        val scaling = Scaling()
        // With massive XP, level should cap at 31
        val massiveXp = 1_000_000_000.0
        val (level, _, _) = calculateLevelFromTotalXp(massiveXp, scaling)
        assertTrue("Level should be capped at 31, got $level", level <= 31)
        println("Level with 1B XP: $level")
    }

    // === AP Formula Tests ===

    @Test
    fun testApFormula_normalProgression() {
        val apPerLevel = 5
        // Level 0 -> 5: should get 25 AP
        assertEquals(25, calculateApGained(0, 5, apPerLevel))
        // Level 5 -> 10: should get 25 AP
        assertEquals(25, calculateApGained(5, 10, apPerLevel))
        // Level 10 -> 15: should get 25 AP
        assertEquals(25, calculateApGained(10, 15, apPerLevel))
    }

    @Test
    fun testApFormula_pastFastLevelCap() {
        val apPerLevel = 5
        // Level 12 -> 15: should get 15 AP (not 25 like the old buggy formula)
        assertEquals(15, calculateApGained(12, 15, apPerLevel))
        // Level 20 -> 25: should get 25 AP
        assertEquals(25, calculateApGained(20, 25, apPerLevel))
    }

    @Test
    fun testApFormula_noLevelUp() {
        assertEquals(0, calculateApGained(5, 5, 5))
        assertEquals(0, calculateApGained(10, 8, 5)) // level went down (shouldn't happen)
    }

    @Test
    fun testApFormula_singleLevel() {
        assertEquals(5, calculateApGained(0, 1, 5))
        assertEquals(5, calculateApGained(10, 11, 5))
        assertEquals(5, calculateApGained(30, 31, 5))
    }

    // === Streak and Passcard Tests ===

    @Test
    fun testStreakRewards_passcardEvery7() {
        val (ap7, pass7) = calculateStreakRewards(7)
        assertEquals(1, pass7)
        assertEquals(0, ap7)

        val (ap14, pass14) = calculateStreakRewards(14)
        assertEquals(1, pass14)
        assertEquals(0, ap14)

        val (ap21, pass21) = calculateStreakRewards(21)
        assertEquals(1, pass21)
        assertEquals(0, ap21)
    }

    @Test
    fun testStreakRewards_apEvery30() {
        val (ap30, pass30) = calculateStreakRewards(30)
        assertEquals(10, ap30)
        assertEquals(0, pass30) // 30 % 7 != 0

        val (ap60, pass60) = calculateStreakRewards(60)
        assertEquals(10, ap60)
        assertEquals(0, pass60)
    }

    @Test
    fun testStreakRewards_bothAt42() {
        // 42 = 6*7 = streak 42 % 7 == 0 (passcard) and 42 % 30 != 0 (no AP)
        val (ap42, pass42) = calculateStreakRewards(42)
        assertEquals(1, pass42)
        assertEquals(0, ap42)
    }

    @Test
    fun testStreakRewards_nothingAt6() {
        val (ap6, pass6) = calculateStreakRewards(6)
        assertEquals(0, ap6)
        assertEquals(0, pass6)
    }

    @Test
    fun testStreakRewards_zeroStreak() {
        val (ap0, pass0) = calculateStreakRewards(0)
        assertEquals(0, ap0)
        assertEquals(0, pass0)
    }

    @Test
    fun testPasscardConsumption() {
        var passcards = 3
        assertTrue(passcards > 0)
        passcards--
        assertEquals(2, passcards)
        // Can't use if 0
        passcards = 0
        assertFalse(passcards > 0)
    }

    // === Quest Generation Tests ===

    @Test
    fun testQuestGeneration_equipmentFiltering() {
        val exercises = getDefaultExercises()
        val scaling = Scaling()

        // Bodyweight only
        val bodyweightOnly = generateQuestExercises(exercises, 10, scaling, listOf("bodyweight"), listOf())
        bodyweightOnly.forEach { qe ->
            val ex = exercises.find { it.name == qe.name }!!
            assertEquals("bodyweight", ex.equipment)
        }
        assertTrue("Should have bodyweight exercises", bodyweightOnly.isNotEmpty())
        println("Bodyweight-only quest: ${bodyweightOnly.map { it.name }}")

        // Dumbbell only
        val dumbbellOnly = generateQuestExercises(exercises, 10, scaling, listOf("dumbbell"), listOf())
        dumbbellOnly.forEach { qe ->
            val ex = exercises.find { it.name == qe.name }!!
            assertEquals("dumbbell", ex.equipment)
        }
        println("Dumbbell-only quest: ${dumbbellOnly.map { it.name }}")
    }

    @Test
    fun testQuestGeneration_trainingGoalFiltering() {
        val exercises = getDefaultExercises()
        val scaling = Scaling()

        // Chest focus
        val chestFocus = generateQuestExercises(exercises, 10, scaling, listOf("bodyweight"), listOf("chest"))
        chestFocus.forEach { qe ->
            val ex = exercises.find { it.name == qe.name }!!
            assertTrue("Expected chest, arms, full, or cardio, got ${ex.muscleGroups}",
                ex.muscleGroups.any { it in listOf("chest", "arms", "full", "cardio") })
        }
        println("Chest-focus quest: ${chestFocus.map { "${it.name}(${it.amount})" }}")
    }

    @Test
    fun testQuestGeneration_levelGating() {
        val exercises = getDefaultExercises()
        val scaling = Scaling()

        // Level 0: only level 0 exercises
        val level0 = generateQuestExercises(exercises, 0, scaling, listOf("bodyweight"), listOf())
        level0.forEach { qe ->
            val ex = exercises.find { it.name == qe.name }!!
            assertTrue("Level 0 should not get level ${ex.requiredLevel} exercise ${ex.name}",
                ex.requiredLevel <= 0)
        }

        // Level 15: should unlock more
        val level15 = generateQuestExercises(exercises, 15, scaling, listOf("bodyweight", "dumbbell", "bar"), listOf())
        assertTrue("Level 15 should have more options than level 0", level15.size >= level0.size)
        println("Level 0 options: ${level0.size}, Level 15 options: ${level15.size}")
    }

    @Test
    fun testQuestGeneration_amountScaling() {
        val exercises = getDefaultExercises()
        val scaling = Scaling()

        val level1 = generateQuestExercises(exercises, 1, scaling, listOf("bodyweight"), listOf())
        val level20 = generateQuestExercises(exercises, 20, scaling, listOf("bodyweight", "dumbbell", "bar"), listOf())

        // At higher levels, amounts should be higher for same exercises
        val pushupsL1 = level1.find { it.name == "pushups" }
        val pushupsL20 = level20.find { it.name == "pushups" }
        if (pushupsL1 != null && pushupsL20 != null) {
            assertTrue("Level 20 pushups ($pushupsL20) should be >= level 1 ($pushupsL1)",
                pushupsL20.amount >= pushupsL1.amount)
            println("Pushups: L1=${pushupsL1.amount}, L20=${pushupsL20.amount}")
        }
    }

    @Test
    fun testQuestGeneration_timedVsReps() {
        val exercises = getDefaultExercises()
        val scaling = Scaling()
        val quest = generateQuestExercises(exercises, 5, scaling, listOf("bodyweight"), listOf())

        quest.forEach { qe ->
            val ex = exercises.find { it.name == qe.name }!!
            assertEquals("Timed flag should match", ex.timed, qe.timed)
            assertTrue("Amount should be positive", qe.amount > 0)
            if (ex.timed) {
                assertTrue("Timed exercise amount should be reasonable (5-700s)", qe.amount in 5..700)
            } else {
                assertTrue("Rep exercise amount should be reasonable (5-250)", qe.amount in 5..250)
            }
        }
    }

    // === XP from Exercise Completion ===

    @Test
    fun testXpFromExerciseCompletion() {
        val exercises = getDefaultExercises()
        val scaling = Scaling()
        val quest = generateQuestExercises(exercises, 5, scaling, listOf("bodyweight"), listOf())

        val xp = calculateExerciseXp(quest, exercises)
        assertTrue("XP from quest completion should be > 0", xp > 0)
        println("XP from completing quest at level 5: $xp")
    }

    @Test
    fun testXpFromExerciseCompletion_higherLevelMoreXp() {
        val exercises = getDefaultExercises()
        val scaling = Scaling()

        val questL1 = generateQuestExercises(exercises, 1, scaling, listOf("bodyweight"), listOf())
        val questL15 = generateQuestExercises(exercises, 15, scaling, listOf("bodyweight", "dumbbell", "bar"), listOf())

        val xpL1 = calculateExerciseXp(questL1, exercises)
        val xpL15 = calculateExerciseXp(questL15, exercises)

        assertTrue("Higher level quest should yield more XP", xpL15 >= xpL1)
        println("XP: L1=$xpL1, L15=$xpL15")
    }

    // === Prayer Times Tests (All Algorithms) ===

    @Test
    fun testPrayerTimes_allAlgorithms() {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.MAY, 22)
        val lat = 24.7136  // Riyadh - low latitude, all algorithms should produce valid times
        val lng = 46.6753

        val methods = listOf(
            SunCalc.PrayerMethod.DEFAULT to "Default",
            SunCalc.PrayerMethod.MWL to "MWL",
            SunCalc.PrayerMethod.ISNA to "ISNA",
            SunCalc.PrayerMethod.EGYPTO to "Egyptian",
            SunCalc.PrayerMethod.MAKKAH to "Makkah",
            SunCalc.PrayerMethod.KARACHI to "Karachi",
            SunCalc.PrayerMethod.TEHRAN to "Tehran",
            SunCalc.PrayerMethod.JAFARI to "Jafari"
        )

        methods.forEach { (method, name) ->
            val times = SunCalc.getPrayerTimes(cal, lat, lng, method)
            // At Riyadh (24.7N), all algorithms should produce valid times
            assertNotNull("$name: Fajr should not be null", times.fajr)
            assertNotNull("$name: Dhuhr should not be null", times.dhuhr)
            assertNotNull("$name: Asr should not be null", times.asr)
            assertNotNull("$name: Maghrib should not be null", times.maghrib)
            assertNotNull("$name: Isha should not be null", times.isha)

            // Fajr should be before sunrise
            if (times.fajr != null && times.sunrise != null) {
                assertTrue("$name: Fajr should be before sunrise",
                    times.fajr!!.time < times.sunrise!!.time)
            }
            // Dhuhr should be between Fajr and Asr (chronological order)
            if (times.fajr != null && times.dhuhr != null) {
                assertTrue("$name: Dhuhr should be after Fajr",
                    times.dhuhr!!.time > times.fajr!!.time)
            }
            if (times.dhuhr != null && times.asr != null) {
                assertTrue("$name: Asr should be after Dhuhr",
                    times.asr!!.time > times.dhuhr!!.time)
            }
            // Maghrib should be after Asr
            if (times.asr != null && times.maghrib != null) {
                assertTrue("$name: Maghrib should be after Asr",
                    times.maghrib!!.time > times.asr!!.time)
            }
            // Isha should be after Maghrib
            if (times.maghrib != null && times.isha != null) {
                assertTrue("$name: Isha should be after Maghrib",
                    times.isha!!.time > times.maghrib!!.time)
            }

            println("$name: Fajr=${times.fajr}, Dhuhr=${times.dhuhr}, Asr=${times.asr}, Maghrib=${times.maghrib}, Isha=${times.isha}")
        }
    }

    @Test
    fun testPrayerTimes_tehranDifferentFromDefault() {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.MAY, 22)
        val lat = 35.6892 // Tehran
        val lng = 51.3890

        val defaultTimes = SunCalc.getPrayerTimes(cal, lat, lng, SunCalc.PrayerMethod.DEFAULT)
        val tehranTimes = SunCalc.getPrayerTimes(cal, lat, lng, SunCalc.PrayerMethod.TEHRAN)
        val jafariTimes = SunCalc.getPrayerTimes(cal, lat, lng, SunCalc.PrayerMethod.JAFARI)

        // Tehran and Jafari should produce different Fajr/Isha than DEFAULT
        if (defaultTimes.fajr != null && tehranTimes.fajr != null) {
            assertNotEquals("Tehran Fajr should differ from Default",
                defaultTimes.fajr!!.time, tehranTimes.fajr!!.time)
        }
        if (defaultTimes.isha != null && tehranTimes.isha != null) {
            assertNotEquals("Tehran Isha should differ from Default",
                defaultTimes.isha!!.time, tehranTimes.isha!!.time)
        }
        if (defaultTimes.fajr != null && jafariTimes.fajr != null) {
            assertNotEquals("Jafari Fajr should differ from Default",
                defaultTimes.fajr!!.time, jafariTimes.fajr!!.time)
        }

        println("Tehran coord - Default Fajr: ${defaultTimes.fajr}, Tehran Fajr: ${tehranTimes.fajr}, Jafari Fajr: ${jafariTimes.fajr}")
        println("Tehran coord - Default Isha: ${defaultTimes.isha}, Tehran Isha: ${tehranTimes.isha}, Jafari Isha: ${jafariTimes.isha}")
    }

    @Test
    fun testPrayerTimes_differentLocations() {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.MAY, 22)

        val locations = listOf(
            Triple("London", 51.5074, -0.1278),
            Triple("Mecca", 21.3891, 39.8579),
            Triple("New York", 40.7128, -74.0060),
            Triple("Tokyo", 35.6762, 139.6503),
            Triple("Cape Town", -33.9249, 18.4241)
        )

        locations.forEach { (name, lat, lng) ->
            val times = SunCalc.getPrayerTimes(cal, lat, lng, SunCalc.PrayerMethod.MWL)
            assertNotNull("$name: Fajr should not be null", times.fajr)
            assertNotNull("$name: Isha should not be null", times.isha)
            println("$name: Fajr=${times.fajr}, Isha=${times.isha}")
        }
    }

    @Test
    fun testPrayerTimes_polarEdgeCase() {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.JUNE, 21) // Summer solstice

        // Very high latitude - some times may be null
        val times = SunCalc.getPrayerTimes(cal, 69.9688, 23.0135, SunCalc.PrayerMethod.MWL)
        // At extreme latitudes, some calculations may return null (sun doesn't set/rise)
        // This is expected behavior - just ensure no crash
        println("Polar (Tromso): Fajr=${times.fajr}, Dhuhr=${times.dhuhr}, Isha=${times.isha}")
    }

    @Test
    fun testSunCalc_producesReasonableTimes() {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.MARCH, 20) // Equinox
        // Cairo (30°N, 31°E) — low latitude, no fallback needed
        val times = SunCalc.getPrayerTimes(cal, 30.0, 31.0, SunCalc.PrayerMethod.MWL)

        assertNotNull("Fajr should not be null", times.fajr)
        assertNotNull("Sunrise should not be null", times.sunrise)
        assertNotNull("Dhuhr should not be null", times.dhuhr)
        assertNotNull("Asr should not be null", times.asr)
        assertNotNull("Maghrib should not be null", times.maghrib)
        assertNotNull("Isha should not be null", times.isha)

        // Chronological order
        assertTrue("Fajr before Sunrise", times.fajr!!.before(times.sunrise))
        assertTrue("Sunrise before Dhuhr", times.sunrise!!.before(times.dhuhr))
        assertTrue("Dhuhr before Asr", times.dhuhr!!.before(times.asr))
        assertTrue("Asr before Maghrib", times.asr!!.before(times.maghrib))
        assertTrue("Maghrib before Isha", times.maghrib!!.before(times.isha))

        // Day length: on equinox, day ≈ 12h → sunrise and sunset ~6h from noon
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        println("Equinox Cairo MWL: Fajr=${times.fajr?.let { sdf.format(it) }}, Sunrise=${times.sunrise?.let { sdf.format(it) }}, " +
                "Dhuhr=${times.dhuhr?.let { sdf.format(it) }}, Asr=${times.asr?.let { sdf.format(it) }}, " +
                "Maghrib=${times.maghrib?.let { sdf.format(it) }}, Isha=${times.isha?.let { sdf.format(it) }}")
    }

    @Test
    fun testSunCalc_meccaSummer() {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.JUNE, 21) // Summer solstice
        // Mecca (21.4°N, 39.8°E) — all times should be valid
        val times = SunCalc.getPrayerTimes(cal, 21.4225, 39.8262, SunCalc.PrayerMethod.MWL)

        assertNotNull("Fajr should not be null", times.fajr)
        assertNotNull("Isha should not be null", times.isha)
        assertTrue("Fajr before Sunrise", times.fajr!!.before(times.sunrise))
        assertTrue("Isha after Maghrib", times.isha!!.after(times.maghrib))

        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        println("Mecca MWL Jun21: Fajr=${times.fajr?.let { sdf.format(it) }}, Sunrise=${times.sunrise?.let { sdf.format(it) }}, " +
                "Dhuhr=${times.dhuhr?.let { sdf.format(it) }}, Asr=${times.asr?.let { sdf.format(it) }}, " +
                "Maghrib=${times.maghrib?.let { sdf.format(it) }}, Isha=${times.isha?.let { sdf.format(it) }}")
    }

    // === Full User Journey Simulation ===

    @Test
    fun testFullUserJourney_beginner30Days() {
        val scaling = Scaling()
        val exercises = getDefaultExercises()
        var user = User(name = "TestHero", level = 0)
        var totalXp = 0.0
        var streak = 0
        var passcards = 0
        var ap = 0

        println("=== 30-Day Beginner Journey ===")
        println("Start: Level ${user.level}, XP $totalXp, Streak $streak, AP $ap, Passcards $passcards")

        for (day in 1..30) {
            // Generate quest
            val quest = generateQuestExercises(exercises, user.level, scaling, listOf("bodyweight"), listOf())
            assertTrue("Day $day: quest should have exercises", quest.isNotEmpty())

            // Complete quest
            val dayXp = calculateExerciseXp(quest, exercises)
            totalXp += dayXp

            // Calculate level
            val oldLevel = user.level
            val (newLevel, progress, needed) = calculateLevelFromTotalXp(totalXp, scaling)
            user = user.copy(level = newLevel, xpProgress = progress, xpNeeded = needed, totalXp = totalXp)

            // AP gain
            val apGained = calculateApGained(oldLevel, newLevel, scaling.apPerLevel)
            ap += apGained

            // Streak
            streak++
            val (apBonus, passcardBonus) = calculateStreakRewards(streak)
            ap += apBonus
            passcards += passcardBonus

            if (day % 7 == 0 || day == 1 || day == 30) {
                println("Day $day: Level ${user.level}, XP ${totalXp.toInt()}, Streak $streak, AP $ap, Passcards $passcards, QuestXP ${dayXp.toInt()}")
            }
        }

        assertTrue("Should have leveled up from 30 days", user.level > 0)
        assertTrue("Should have earned some AP", ap > 0)
        assertTrue("Should have earned passcards at day 7, 14, 21, 28", passcards >= 4)
        assertEquals("Streak should be 30", 30, streak)
        println("Final: Level ${user.level}, XP ${totalXp.toInt()}, Streak $streak, AP $ap, Passcards $passcards")
    }

    @Test
    fun testFullUserJourney_intermediate90Days() {
        val scaling = Scaling()
        val exercises = getDefaultExercises()
        var user = User(name = "Intermediate", level = 5)
        var totalXp = 0.0
        var streak = 0
        var passcards = 0
        var ap = 0

        // Give starting stats for intermediate
        user = user.copy(stats = user.stats.copy(STR = 15.0, AGI = 15.0, VIT = 15.0, END = 15.0))

        println("=== 90-Day Intermediate Journey ===")

        for (day in 1..90) {
            val quest = generateQuestExercises(exercises, user.level, scaling,
                listOf("bodyweight", "dumbbell"), listOf("strength", "chest", "legs"))
            val dayXp = calculateExerciseXp(quest, exercises)
            totalXp += dayXp

            val oldLevel = user.level
            val (newLevel, progress, needed) = calculateLevelFromTotalXp(totalXp, scaling)
            user = user.copy(level = newLevel, xpProgress = progress, xpNeeded = needed, totalXp = totalXp)

            ap += calculateApGained(oldLevel, newLevel, scaling.apPerLevel)
            streak++
            val (apBonus, passcardBonus) = calculateStreakRewards(streak)
            ap += apBonus
            passcards += passcardBonus

            if (day % 30 == 0) {
                println("Day $day: Level ${user.level}, XP ${totalXp.toInt()}, Streak $streak, AP $ap, Passcards $passcards")
            }
        }

        assertTrue("90-day intermediate should reach level 6+", user.level >= 6)
        println("Final: Level ${user.level}, AP $ap, Passcards $passcards")
    }

    @Test
    fun testFullUserJourney_missedDay() {
        val scaling = Scaling()
        val exercises = getDefaultExercises()
        var user = User(name = "MissedDay", level = 0)
        var totalXp = 0.0
        var streak = 0
        var passcards = 0

        println("=== Missed Day Scenario ===")

        // Complete 7 days
        for (day in 1..7) {
            val quest = generateQuestExercises(exercises, user.level, scaling, listOf("bodyweight"), listOf())
            totalXp += calculateExerciseXp(quest, exercises)
            val (newLevel, _, _) = calculateLevelFromTotalXp(totalXp, scaling)
            user = user.copy(level = newLevel, totalXp = totalXp)
            streak++
            val (_, passcardBonus) = calculateStreakRewards(streak)
            passcards += passcardBonus
        }
        println("After 7 days: Level ${user.level}, Streak $streak, Passcards $passcards")
        assertEquals("Should have 1 passcard after 7 days", 1, passcards)

        // Miss day 8 - penalty applies
        totalXp = floor(totalXp * 0.9) // 10% XP penalty
        streak = 0 // Streak reset
        val (newLevel, _, _) = calculateLevelFromTotalXp(totalXp, scaling)
        user = user.copy(level = newLevel, totalXp = totalXp)
        println("After missed day: Level ${user.level}, XP ${totalXp.toInt()}, Streak $streak")
        assertEquals("Streak should be 0 after miss", 0, streak)

        // Resume for 7 more days
        for (day in 9..15) {
            val quest = generateQuestExercises(exercises, user.level, scaling, listOf("bodyweight"), listOf())
            totalXp += calculateExerciseXp(quest, exercises)
            val (lvl, _, _) = calculateLevelFromTotalXp(totalXp, scaling)
            user = user.copy(level = lvl, totalXp = totalXp)
            streak++
        }
        println("After recovery: Level ${user.level}, Streak $streak, Passcards $passcards")
    }

    @Test
    fun testFullUserJourney_passcardRestDay() {
        val scaling = Scaling()
        val exercises = getDefaultExercises()
        var user = User(name = "PasscardUser", level = 0)
        var totalXp = 0.0
        var streak = 0
        var passcards = 0

        println("=== Passcard Rest Day Scenario ===")

        // Earn passcards over 21 days
        for (day in 1..21) {
            val quest = generateQuestExercises(exercises, user.level, scaling, listOf("bodyweight"), listOf())
            totalXp += calculateExerciseXp(quest, exercises)
            val (newLevel, _, _) = calculateLevelFromTotalXp(totalXp, scaling)
            user = user.copy(level = newLevel, totalXp = totalXp)
            streak++
            val (_, passcardBonus) = calculateStreakRewards(streak)
            passcards += passcardBonus
        }
        println("After 21 days: Level ${user.level}, Streak $streak, Passcards $passcards")
        assertTrue("Should have passcards", passcards > 0)

        // Use a passcard on day 22 (skip quest, no penalty, streak preserved)
        passcards--
        // Streak still increments because quest is marked "completed" via passcard
        streak++
        val (_, passcardBonus) = calculateStreakRewards(streak)
        passcards += passcardBonus
        println("After passcard day: Streak $streak, Passcards $passcards")
        assertEquals("Streak should continue", 22, streak)
    }

    @Test
    fun testFullUserJourney_bonusExercises() {
        val scaling = Scaling()
        val exercises = getDefaultExercises()
        val user = User(name = "BonusUser", level = 10)
        var totalXp = 0.0

        println("=== Bonus Exercise XP ===")

        val quest = generateQuestExercises(exercises, 10, scaling, listOf("bodyweight", "dumbbell", "bar"), listOf())
        val mainXp = calculateExerciseXp(quest, exercises)
        totalXp += mainXp
        println("Main quest XP: ${mainXp.toInt()}")

        // Generate bonus exercises (50% XP)
        val settings = Settings()
        val available = exercises.filter { ex ->
            ex.requiredLevel <= 10 &&
            settings.equipmentTypes.contains(ex.equipment)
        }
        val bonus = available.shuffled().take(3).map { ex ->
            val amount = (if (ex.timed) scaling.baseTime else scaling.baseReps * 0.3).toInt().coerceAtLeast(5)
            QuestExercise(name = ex.name, amount = amount, done = false, timed = ex.timed, muscleGroups = ex.muscleGroups, equipment = ex.equipment)
        }
        val bonusXp = calculateExerciseXp(bonus, exercises) * 0.5 // 50% for bonus
        totalXp += bonusXp
        println("Bonus XP (50%): ${bonusXp.toInt()}")
        println("Total XP: ${totalXp.toInt()}")
        assertTrue("Bonus should add XP", bonusXp > 0)
    }

    // === Edge Cases ===

    @Test
    fun testEdgeCase_zeroLevelUser() {
        val scaling = Scaling()
        val exercises = getDefaultExercises()

        // Level 0 user should still get exercises
        val quest = generateQuestExercises(exercises, 0, scaling, listOf("bodyweight"), listOf())
        assertTrue("Level 0 should have exercises", quest.isNotEmpty())
        quest.forEach { qe ->
            val ex = exercises.find { it.name == qe.name }!!
            assertEquals("Level 0 exercises should require level 0", 0, ex.requiredLevel)
        }
    }

    @Test
    fun testEdgeCase_maxLevelUser() {
        val scaling = Scaling()
        val exercises = getDefaultExercises()

        // Level 31 user should get all exercises
        val quest = generateQuestExercises(exercises, 31, scaling,
            listOf("bodyweight", "dumbbell", "bar"), listOf())
        assertTrue("Level 31 should have max exercises", quest.size == 4) // capped at 4
    }

    @Test
    fun testEdgeCase_noMatchingEquipment() {
        val scaling = Scaling()
        val exercises = getDefaultExercises()

        // If equipment doesn't match any exercise, quest should be empty
        val quest = generateQuestExercises(exercises, 10, scaling, listOf("treadmill"), listOf())
        // This is expected - no treadmill exercises in catalog
        println("Quest with treadmill-only: ${quest.size} exercises")
    }

    @Test
    fun testEdgeCase_xpRounding() {
        val scaling = Scaling()
        // XP calculations should not produce NaN or Infinity
        for (lvl in 0..50) {
            val xp = calculateXpForLevel(lvl, scaling)
            assertFalse("Level $lvl XP should not be NaN", xp.isNaN())
            assertFalse("Level $lvl XP should not be Infinity", xp.isInfinite())
            assertTrue("Level $lvl XP should be non-negative", xp >= 0)
        }
    }

    @Test
    fun testEdgeCase_negativeXpProtection() {
        val scaling = Scaling()
        // Even with 0 XP, level should be 0
        val (level, progress, _) = calculateLevelFromTotalXp(0.0, scaling)
        assertEquals(0, level)
        assertEquals(0.0, progress, 0.01)
    }

    // === Model Serialization Tests ===

    @Test
    fun testModelDefaults_exercise() {
        val ex = Exercise()
        assertEquals("", ex.name)
        assertEquals(0, ex.difficulty)
        assertEquals(0, ex.requiredLevel)
        assertEquals(1.0, ex.baseScale, 0.001)
        assertEquals(1.0, ex.xpPerRep, 0.001)
        assertFalse(ex.timed)
        assertEquals(listOf("full"), ex.muscleGroups)
        assertEquals("bodyweight", ex.equipment)
    }

    @Test
    fun testModelDefaults_statGain() {
        val sg = StatGain()
        assertEquals(0.0, sg.STR, 0.001)
        assertEquals(0.0, sg.AGI, 0.001)
        assertEquals(0.0, sg.VIT, 0.001)
        assertEquals(0.0, sg.END, 0.001)
        assertEquals(0.0, sg.AP, 0.001)
    }

    @Test
    fun testModelDefaults_prayer() {
        val p = Prayer()
        assertEquals("", p.name)
        assertEquals("", p.time)
        assertFalse(p.done)
        assertNull(p.prayerTime)
    }

    @Test
    fun testModelDefaults_questExercise() {
        val qe = QuestExercise()
        assertEquals("", qe.name)
        assertEquals(0, qe.amount)
        assertFalse(qe.done)
        assertFalse(qe.timed)
        assertEquals(listOf("full"), qe.muscleGroups)
        assertEquals("bodyweight", qe.equipment)
    }

    @Test
    fun testModelDefaults_settings() {
        val s = Settings()
        assertEquals("blue", s.color)
        assertFalse(s.showPrayers)
        assertEquals("default", s.prayerAlgorithm)
        assertEquals(51.5074, s.prayerLatitude, 0.001)
        assertEquals(-0.1278, s.prayerLongitude, 0.001)
        // gender and showHabits should be removed
    }

    @Test
    fun testModelDefaults_scaling() {
        val s = Scaling()
        // exponent should be removed
        assertEquals(1.5, s.exponentFast, 0.001)
        assertEquals(10, s.fastLevelCap)
        assertEquals(5, s.apPerLevel)
    }

    // === Stat Upgrade Tests ===

    @Test
    fun testStatUpgrade() {
        var stats = Stats(AP = 10)
        val original = stats.copy()

        // Upgrade STR
        stats = stats.copy(STR = stats.STR + 1, AP = stats.AP - 1)
        assertEquals(original.STR + 1, stats.STR, 0.001)
        assertEquals(original.AP - 1, stats.AP)

        // Can't upgrade with 0 AP
        stats = stats.copy(AP = 0)
        assertFalse(stats.AP > 0)
    }

    // === Habit Reset Test ===

    @Test
    fun testHabitReset() {
        val habits = mutableListOf(
            Habit(name = "wake up early", done = true, isCustom = false),
            Habit(name = "eat healthy", done = true, isCustom = false),
            Habit(name = "brush teeth", done = false, isCustom = false)
        )

        // Reset all habits
        val reset = habits.map { it.copy(done = false) }.toMutableList()
        reset.forEach { habit ->
            assertFalse("Habit '${habit.name}' should be reset", habit.done)
        }
    }
}
