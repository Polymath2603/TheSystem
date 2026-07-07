---
tags: [project, android, backend]
status: completed
---

# The System — Backend ⚙️

> Data models, logic, and key implementation details.

## Architecture

- **Client:** Android app (Kotlin / Jetpack Compose)
- **Architecture:** MVVM with AndroidViewModel + mutableStateOf
- **Data:** Local persistence via SharedPreferences + Gson
- **Target:** Minimum SDK 24, Target SDK 36

---

## Exercise Generation

- Daily quest with 5 exercises, per user
- Difficulty scales with user's current level
- Exercises filtered by available equipment and training goal
- Repetitions scale non-linearly with level: `floor(baseScale * (baseReps + (maxReps - baseReps) * (level/60)^1.35))`
- Timed exercises use separate scaling formula

## XP & Level System

- Fast scaling (levels 1-10): `floor(baseXP * level^exponentFast)` — baseXP=100, exponentFast=2.0
- Slow scaling (levels 11+): `baseAfterFast + floor(additional * 100 * (1 + additional * 0.1))`
- Level thresholds: F(1), D(5), C(10), B(20), A(30), S(40), SS(50), SSS(60)
- Character classes: Warrior(1), Mage(5), Rogue(10), Monk(20), Berserker(30), Paladin(40), Assassin(50), Necromancer(60)
- Titles: Novice(1), Apprentice(5), Adept(10), Expert(20), Master(30), Grandmaster(40), Legend(50), Mythic(60)

## Prayer Module

- 5 daily prayers with notification triggers
- Location-based calculation using SunCalc astronomical algorithm
- 8 calculation methods: MWL, ISNA, Egypt, Makkah, Karachi, Tehran, Jafari, Default
- Streak tracked separately (no XP gain/loss from prayers)
- All times computed in UTC, corrected for offset from solar noon

## Streak & Pass Card System

- 7-day streak → earn 1 pass card
- 30-day streak → earn 10 AP
- Pass cards can skip exercise day without penalty
- Passcards expire at quest reset

## AP & Stats

- `(newLevel - oldLevel) * apPerLevel` AP earned on level up
- Stats: STR, AGI, END, INT, WIS, CHA, LUK
- Stat upgrades cost `stat.allocatedPoints + 1` AP per point

## Notification System

- Prayer time alerts (5 unique notification IDs)
- Exercise reminders
- Level-up notifications
- Uses AlarmManager with `setExactAndAllowWhileIdle`
- Schedule exact alarm permission requested on Android 12+

## Key Files

| File | Purpose |
|---|---|
| `MainViewModel.kt` | Core logic — XP, levels, quests, prayers, stats |
| `AppDataModels.kt` | All data classes — AppData, Exercise, Prayer, etc. |
| `MainActivity.kt` | Entry point, permission requests |
| `MainScreen.kt` | Tab navigation + profile header |
| `NotificationScheduler.kt` | Alarm scheduling for prayers |

---

## Links
- [Overview](Overview.md) — project summary & concept
- [Design](Design.md) — UI/UX specs
