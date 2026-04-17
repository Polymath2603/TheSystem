# Changelog

## [1.0.0] - 2025-02-04

### Added
- Initial release of The System
- Daily quest system with random exercises
- RPG-style progression (XP, levels, stats)
- Habit tracking functionality
- Custom task management
- Streak system with rewards
- Passcard system for day skipping
- 7 color themes (blue, red, green, yellow, purple, cyan, grey)
- Profile customization with image upload
- Timed exercises with countdown
- Stat upgrades using ability points

### Features
- Complete Compose UI (no XML layouts)
- Clean MVVM architecture
- Reactive state management with StateFlow
- Data persistence with GSON
- Smooth animations and transitions
- Responsive design

### Technical
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 36
- Kotlin 1.9+
- Jetpack Compose
- Material 3 Design

---

## [1.1.0] - 2026-04-17

### Added
- **Habits Tab**: Now fully customizable - add, delete, reorder habits
- **Prayer Times**: Location-based calculation with multiple Islamic methods
  - Default, Muslim World League (MWL), ISNA, Egyptian Authority, Umm Al-Qura, Karachi
- **Prayer Countdown**: Visual countdown to next prayer with circular progress
- **Settings**: Full-featured settings as bottom sheet with:
  - Profile: name, gender (male/female), profile picture
  - Theme: 10 colors (added pink, orange, teal, indigo)
  - Prayer: algorithm selection, location (auto/manual), auto-refresh
  - Notifications: exercise and prayer reminders
  - Equipment: bodyweight, dumbbell, bar checkboxes
- **Profile**: Shows gender, improved stats display
- **Exercise Timer**: Double-click to skip and mark complete
- **Day Countdown**: Circular progress showing day progress until reset

### Changed
- **Removed Custom Tab**: Merged custom tasks into Habits tab
- **Tab Layout**: Equal-width tabs (width / number of tabs)
- **Level Progression**: Fast progression for levels 1-10, capped at 31
- **Profile**: Removed available exercises list
- **Prayer Tab**: Can only check off current/past prayers, not future ones

### Technical
- Updated data models with new fields (gender, prayer settings, equipment)
- Added SunCalc.PrayerMethod enum for calculation algorithms
- Modal bottom sheet for settings instead of dialog
- Equipment tagging for exercises

### Bug Fixes
- Fixed level calculation for fast early progression
- Fixed exercise timer skip functionality
- Fixed settings compatibility with old dialog

---

## [1.2.0] - 2026-04-17

### Added
- **Habits**: 
  - Done items move to end of list
  - System message at top
  - Add input at bottom
  - Long-press for edit/delete/reorder with drag & drop
- **Prayers**:
  - Undo functionality for checking off prayers
  - Removed countdown display
  - Shows "Past at HH:MM" or "Coming in Xh Ym" format
  - Uses default Material color scheme
- **Workout**:
  - Countdown moved to bottom as "Deadline" with penalty warning
  - Duration formatted as Xh Ym Zs
  - Reset only affects workout/quest, not habits/prayers/passcards
  - Extra exercises instead of reset (rewarding, no penalty)
  - Reset at midnight
  - Extra exercises only available after main quest completed
  - Passcard shows day-off message, hides exercises
- **Profile**: Improved UI with better layout
- **Settings**:
  - Profile picture change
  - Show/hide prayer tab toggle
  - Theme tabs with padding consideration
  - Auto-fetch location (removed manual)
  - More prayer calculation methods
  - Difficulty setting (beginner/intermediate/advanced)
  - Days per week selection (work days)
  - Training goals (cardio/strength/durability/biceps/legs/etc - all checkbox)
  - Quest generator reacts to goals
  - Sub-settings sections for organization
  - Activity instead of fragment
- **Exercises**:
  - Added more exercises
  - Updated metadata (XP, stats)
  - Equipment tags added
  - Equipment filter affects quest generation
- **Leveling**: Improved progression system
  - Analyzed exponential vs step progression
  - Quest generator distributes work over muscle groups
  - Focus goals affect exercise selection

### Changed
- Prayer notifications no longer require manual refresh (auto daily)
- Settings organized into collapsible sections
- Tab width calculation accounts for padding

### Technical
- Full quest reset logic separates workout from daily items
- New exercise equipment tags for filtering
- Muscle group tagging for balanced training
- Location permissions for auto-detect