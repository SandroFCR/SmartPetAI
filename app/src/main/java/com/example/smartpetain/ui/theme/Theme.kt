package com.example.smartpetain.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    secondary = TealPrimary,
    tertiary = PinkPrimary,
    background = Background,
    surface = White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun SmartPetAInTheme(
    darkTheme: Boolean = false, // Forzado a falso para eliminar modo oscuro
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
