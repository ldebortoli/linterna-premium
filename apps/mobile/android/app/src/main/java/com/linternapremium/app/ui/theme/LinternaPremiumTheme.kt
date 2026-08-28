package com.linternapremium.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LinternaColors = darkColorScheme(
    primary = Color(0xFFF3D27A),
    onPrimary = Color(0xFF29210D),
    primaryContainer = Color(0xFF3A321E),
    onPrimaryContainer = Color(0xFFF9E5AA),
    secondary = Color(0xFF9CAAB7),
    onSecondary = Color(0xFF151B21),
    background = Color(0xFF0B0D10),
    onBackground = Color(0xFFF2F3F5),
    surface = Color(0xFF15191E),
    onSurface = Color(0xFFF2F3F5),
    surfaceVariant = Color(0xFF20262D),
    onSurfaceVariant = Color(0xFFB8C0C8),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

@Composable
fun LinternaPremiumTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LinternaColors,
        content = content,
    )
}

