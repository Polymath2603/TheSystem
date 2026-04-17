---
tags: [project, android, design, ui]
status: in-progress
---

# The System — Design 🎨

> UI/UX specifications for all screens and tabs.

## Screen Layout

### Header (Top)
- Profile picture (left)
- Name · Level · Rank · Class (beside picture)
- Title · Passcards count (below)
- XP progress bar (full width, below header)

### Body
- Task list (changes per active tab)

### Footer
- Timer (countdown to daily reset)
- Warning indicators

---

## Tabs

| Tab | Color | Content |
|---|---|---|
| 🔵 Fitness | Blue | Daily exercises (5/day) |
| 🟢 Prayers | Green | 5 daily prayers |
| ⚪ Personal Quest | — | Custom tasks |
| 👤 Profile & Stats | — | Stats, XP, class, history |

---

## Profile Screen
- Profile picture, name, level, XP progress
- Stats dashboard (show all tracked data)
- Spend stat points (future feature)

## Settings Screen
- Edit profile details (name, picture)
- Change tab accent colors (7 themes)
- Notification preferences

---

## Themes (All Fixed ✅)

| Theme | Text Color |
|---|---|
| 🔵 Blue | White |
| 🔴 Red | White |
| 🟢 Green | White |
| 🟡 Yellow | Black *(for readability)* |
| 🟣 Purple | White |
| 🩵 Cyan | White |
| ⚫ Grey | White |

> Themes apply **instantly** — no restart required.

---

## Links
- [[Overview]] — project summary & concept
- [[Backend]] — server & logic specs
