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

An Android app that transforms daily habits into an RPG. Complete tasks → gain XP → level up → maintain streaks. Miss tasks → lose 10% XP. Earn pass cards every 5-day streak.

## Status: 🟡 In Progress
> Found in `~/Workplace/TheSystem/` — actively developed, v1.1.0, all major UI bugs fixed.

### What's done ✅
- All 7 themes working + instant apply
- Profile header on main screen (picture, name, level, rank, class, title, passcards, XP bar)
- Exercise checkboxes fixed (infinite loop resolved)
- Exercise generation server
- Level-up notifications
- Data models: history, rank, class, title, streak calendar

### What's left ❌
- Prayer module (server-side prayer times)
- Account system (signup/login/cloud sync)
- Push notification handler (client)
- App packaging (WebView wrapper)
- Social media blocker plugin

## Core Mechanic

| System | Details |
|---|---|
| 📱 Tabs | Fitness 🔵 · Prayers 🟢 · Quests ⚪ · Profile 👤 |
| ⭐ XP | Gained from exercises; -10% for missing tasks |
| 🏅 Ranks | F → D → C → B → A → S → SS → SSS |
| 🎴 Passcards | 5-day streak → 1 card; skip a day penalty-free; expire in 30 days |
| 🕌 Prayers | Tracked separately — streak but no XP/penalty |

## Links
- [[Design]] — UI/UX specs
- [[Backend]] — server logic
- [[../../Backlog]] — future feature ideas
- [[../../02 - Earn/Business Hub]] — monetization
