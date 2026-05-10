package com.example.smartpetain.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    secondary = TealPrimary,
    tertiary = PinkPrimary,
    background = Background,
    surface = White
)

private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimary,
    secondary = TealPrimary,
    tertiary = PinkPrimary
)

@Composable
fun SmartPetAInTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}