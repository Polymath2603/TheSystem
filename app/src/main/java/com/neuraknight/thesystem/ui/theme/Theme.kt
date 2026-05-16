package com.neuraknight.thesystem.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun TheSystemTheme(

    themeColor: String = "blue",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeColor.lowercase()) {
        "blue" -> BlueColorScheme
        "red" -> RedColorScheme
        "green" -> GreenColorScheme
        "yellow" -> YellowColorScheme
        "purple" -> PurpleColorScheme
        "cyan" -> CyanColorScheme
        "grey" -> GreyColorScheme
        "pink" -> PinkColorScheme
        "orange" -> OrangeColorScheme
        "teal" -> TealColorScheme
        "indigo" -> IndigoColorScheme
        else -> BlueColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}