---
tags: [project, android, active]
status: active
language: kotlin
path: ~/Workplace/TheSystem
workplace: true
---

# The System — Overview 🏆

> A gamified daily habit Android app — fitness, prayers, personal quests. Level up your real life.

## Concept

An Android app that transforms daily habits into an RPG. Complete tasks → gain XP → level up → maintain streaks. Miss tasks → lose 10% XP. Earn pass cards every 7-day streak.

## Status: 🟢 Active

> Found in `~/Workplace/TheSystem/` — actively developed, v1.4, all major UI bugs fixed, 44 passing tests.

### What's done ✅
- All 10 themes working + instant apply
- Profile header on main screen (picture, name, level, rank, class, title, passcards, XP bar)
- Exercise system with difficulty scaling, circular deadline countdown
- Prayer times with 8 calculation methods (SunCalc-based, UTC-correct)
- Streak system with passcard rewards every 7 days, AP every 30 days
- XP progression: fast scaling (exponent 2.0) to level 10, then slow scaling
- Settings: profile, theme, prayer algorithm, location, notifications, equipment
- Completely offline — all data stored locally via SharedPreferences + Gson
- 44 unit tests covering all core logic

### What's left ❌
- Account system (signup/login/cloud sync)
- Social media blocker plugin
- First-time setup flow (profile picture, gender, toggle visibility)

## Source

- GitHub: [Polymath2603/TheSystem](https://github.com/Polymath2603/TheSystem)

## Core Mechanic

| System | Details |
|---|---|
| 📱 Tabs | Fitness 🔵 · Prayers 🟢 · Quests ⚪ · Profile 👤 |
| ⭐ XP | Gained from exercises; scaled by level |
| 🏅 Ranks | F → D → C → B → A → S → SS → SSS |
| 🎴 Passcards | 7-day streak → 1 card; skip a day penalty-free |
| 🕌 Prayers | Tracked separately — streak but no XP/penalty |

## Links

- [Design](Design.md) — UI/UX specs
- [Backend](Backend.md) — data models & logic
