package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlue,
    onPrimary = CrispWhite,
    primaryContainer = DeepRoyalBlue,
    onPrimaryContainer = CrispWhite,
    secondary = VibrantBlue,
    onSecondary = CrispWhite,
    background = Color(0xFF0B1120),
    surface = Color(0xFF1E293B),
    onBackground = CrispWhite,
    onSurface = CrispWhite,
    outline = SlateMuted
)

private val LightColorScheme = lightColorScheme(
    primary = DeepRoyalBlue,
    onPrimary = CrispWhite,
    primaryContainer = LightBluePastel,
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = VibrantBlue,
    onSecondary = CrispWhite,
    secondaryContainer = LightBlueBg,
    onSecondaryContainer = DeepRoyalBlue,
    background = SoftGrayBg,
    surface = CrispWhite,
    onBackground = DarkSlate,
    onSurface = DarkSlate,
    surfaceVariant = LightBluePastel,
    onSurfaceVariant = Color(0xFF0369A1),
    outline = CardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

