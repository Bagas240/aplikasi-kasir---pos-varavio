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
    primaryContainer = DeepRoyalBlue,
    onPrimaryContainer = CrispWhite,
    secondary = VibrantBlue,
    onSecondary = CrispWhite,
    secondaryContainer = VibrantBlue,
    onSecondaryContainer = CrispWhite,
    background = SoftGrayBg,
    surface = CrispWhite,
    onBackground = DarkSlate,
    onSurface = DarkSlate,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = DarkSlate,
    outline = CardBorder
)

@Composable
fun VoravioTheme(
    darkTheme: Boolean = false, // Keep clean high-contrast crisp theme for POS retail registers
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    VoravioTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}

