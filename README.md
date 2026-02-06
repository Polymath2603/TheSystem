# The System - Fitness RPG Tracker

A gamified fitness and productivity tracker with RPG-style progression. Complete daily quests, level up your character, and build healthy habits!

## Features

- **Daily Quest System**: Random workout exercises based on your level
- **RPG Progression**: Gain XP, level up, and increase stats (STR, AGI, VIT, END)
- **Habit Tracking**: Build and maintain daily habits
- **Custom Tasks**: Add your own daily tasks
- **Streak System**: Maintain streaks for bonus rewards
- **Passcard System**: Skip days without penalty (earned through streaks)
- **Multiple Themes**: Choose from 7 color themes

## Requirements

- Android 7.0 (API 24) or higher
- Android Studio Hedgehog or newer
- Kotlin 1.9+
- Gradle 8.0+

## Getting Started

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on device or emulator

## Architecture

- **MVVM Pattern**: Clean separation of concerns
- **Jetpack Compose**: Modern declarative UI
- **StateFlow**: Reactive state management
- **Coroutines**: Async operations
- **GSON**: Data persistence

## Project Structure

```
app/
├── data/
│   ├── models/         # Data classes
│   └── repository/     # Data persistence
├── ui/
│   ├── screens/        # App screens
│   ├── components/     # Reusable UI components
│   ├── theme/          # App theming
│   └── viewmodel/      # Business logic
└── MainActivity.kt     # App entry point
```

## How It Works

1. **Setup**: Choose workout level, goal, and theme
2. **Daily Quest**: Complete assigned exercises
3. **Progression**: Earn XP and level up
4. **Stats**: Upgrade STR, AGI, VIT, END using AP
5. **Streaks**: Build streaks for passcard rewards

## Rewards

- **Every 7 days**: +1 Passcard
- **Every 30 days**: +10 Ability Points
- **Quest completion**: XP and stat gains
- **Level up**: +5 Ability Points per level

## License

MIT License - see LICENSE file for details

## Version

1.0.0 - Initial Release
