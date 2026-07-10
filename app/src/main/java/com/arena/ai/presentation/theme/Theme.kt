package com.arena.ai.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ArenaBackground = Color(0xFF0A0A0A)
val ArenaSurface = Color(0xFF121212)
val ArenaSurfaceElevated = Color(0xFF1A1A1A)
val ArenaSurfaceTertiary = Color(0xFF242424)
val ArenaBorder = Color(0xFF2A2A2A)
val ArenaTextPrimary = Color(0xFFFFFFFF)
val ArenaTextSecondary = Color(0xFFB0B0B0)
val ArenaTextTertiary = Color(0xFF666666)
val ArenaAccentPrimary = Color(0xFF10B981)
val ArenaAccentSecondary = Color(0xFF6366F1)
val ArenaAccentWarning = Color(0xFFF59E0B)
val ArenaAccentError = Color(0xFFEF4444)
val ArenaSidebarBackground = Color(0xFF0F0F0F)

private val DarkColorScheme = darkColorScheme(
    primary = ArenaAccentPrimary,
    secondary = ArenaAccentSecondary,
    background = ArenaBackground,
    surface = ArenaSurfaceElevated,
    onPrimary = ArenaBackground,
    onSecondary = ArenaTextPrimary,
    onBackground = ArenaTextPrimary,
    onSurface = ArenaTextPrimary
)

@Composable
fun ArenaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColorScheme, content = content)
}
