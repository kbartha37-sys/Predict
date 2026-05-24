package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VibrantColorScheme = lightColorScheme(
    primary = SportsBlue, // Indigo-600
    secondary = SportsGreen, // Emerald-500
    tertiary = WarningOrange,
    background = DarkBg,
    surface = DarkCard,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = SecondaryCard,
    outline = CardOutline,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Force light theme for Vibrant Palette vibe
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve high-fidelity styling
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VibrantColorScheme,
        typography = Typography,
        content = content
    )
}
