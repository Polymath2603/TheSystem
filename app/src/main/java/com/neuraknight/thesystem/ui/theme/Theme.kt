package com.neuraknight.thesystem.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun TheSystemTheme(
    colorTheme: String = "blue",
    darkTheme: Boolean = true,  // Force dark theme for now
    content: @Composable () -> Unit
) {
    val colorScheme = when (colorTheme) {
        "blue" -> darkColorScheme(
            primary = ThemeColors.BluePrimary,
            secondary = ThemeColors.BlueSecondary,
            background = ThemeColors.BlueBackground,
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )
        "red" -> darkColorScheme(
            primary = ThemeColors.RedPrimary,
            secondary = ThemeColors.RedSecondary,
            background = ThemeColors.RedBackground,
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )
        "green" -> darkColorScheme(
            primary = ThemeColors.GreenPrimary,
            secondary = ThemeColors.GreenSecondary,
            background = ThemeColors.GreenBackground,
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )
        "yellow" -> darkColorScheme(
            primary = ThemeColors.YellowPrimary,
            secondary = ThemeColors.YellowSecondary,
            background = ThemeColors.YellowBackground,
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
        "purple" -> darkColorScheme(
            primary = ThemeColors.PurplePrimary,
            secondary = ThemeColors.PurpleSecondary,
            background = ThemeColors.PurpleBackground,
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )
        "cyan" -> darkColorScheme(
            primary = ThemeColors.CyanPrimary,
            secondary = ThemeColors.CyanSecondary,
            background = ThemeColors.CyanBackground,
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )
        "grey" -> darkColorScheme(
            primary = ThemeColors.GreyPrimary,
            secondary = ThemeColors.GreySecondary,
            background = ThemeColors.GreyBackground,
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )
        else -> darkColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}