---
tags: [project, android, backend, server]
status: in-progress
---

# The System — Backend ⚙️

> Server logic, data models, and API endpoints.

## Architecture

- **Client:** Android app (Kotlin / WebView)
- **Server:** Local backend — handles task generation, XP, levels
- **Data:** Local + (future) cloud sync

---

## Exercise Generation ✅

- 5 exercises per day, per user
- Difficulty scales with user's current level
- Completed: `ListAdapters.kt` checkbox fix (infinite loop resolved)

## Prayer Module 🔄

- 5 daily prayers with notification triggers
- Streak tracked separately from fitness
- No XP gain/loss from prayers
- Note format: `1st prayer (+2 missed)`
- [ ] Prayer time detection (auto local timezone)

## Streak & Pass Card System

- 5-day streak → earn 1 pass card
- Pass cards can skip exercise day without penalty
- Cards expire after 30 days
- User can hold multiple cards

## XP & Level System ✅

- Completing exercises → XP gain
- Missing tasks → -10% XP penalty (24h window)
- Level ups trigger notification
- Rank progression: F → D → C → B → A → S → SS → SSS
- Character classes: Warrior / Mage / Rogue / Monk

## Account System 🔴 Not started

- [ ] Signup
- [ ] Login
- [ ] Save user data (cloud)
- [ ] Multi-device sync

## Notification System 🔄

- [x] Level up notifications
- [ ] Push notifications (client-side handler)
- [ ] Prayer reminders
- [ ] Streak warnings

## Data Models (Implemented)

- Exercise / Habit / Task history
- Streak calendar
- Character class system
- Rank system
- Title system
- Notification settings structure

---

## Key Files

| File | Purpose |
|---|---|
| `ListAdapters.kt` | Checkbox fix — listener detach before setState |
| `MainActivity.kt` | Profile population on main screen |
| `MainViewModel.kt` | New features: history, class, rank |
| `AppDataModels.kt` | All data models |
| `SettingsDialog.kt` | Instant theme change via `recreate()` |

---

## Links
- [[Overview]] — project summary
- [[Design]] — UI/UX specs
