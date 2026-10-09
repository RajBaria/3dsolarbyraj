package com.example.solarsystem.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00FFCC),
    onPrimary = Color(0xFF00382B),
    primaryContainer = Color(0xFF00513F),
    onPrimaryContainer = Color(0xFF73FFD8),
    secondary = Color(0xFFFFD700),
    onSecondary = Color(0xFF3F2E00),
    secondaryContainer = Color(0xFF5B4300),
    onSecondaryContainer = Color(0xFFFFE088),
    tertiary = Color(0xFF88AAFF),
    background = Color(0xFF000000),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF0A0F1E),
    onSurface = Color(0xFFE2E8F0)
)

@Composable
fun SolarSystemTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
