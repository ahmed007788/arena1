package com.arena.ai.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ArenaAccentPrimary,
    onPrimary = ArenaBackground,
    primaryContainer = ArenaAccentPrimary.copy(alpha = 0.2f),
    onPrimaryContainer = ArenaAccentPrimary,
    secondary = ArenaAccentSecondary,
    onSecondary = ArenaBackground,
    background = ArenaBackground,
    onBackground = ArenaTextPrimary,
    surface = ArenaSurface,
    onSurface = ArenaTextPrimary,
    surfaceVariant = ArenaSurfaceElevated,
    onSurfaceVariant = ArenaTextSecondary,
    outline = ArenaBorder,
    error = ArenaAccentError,
    onError = ArenaBackground
)

@Composable
fun ArenaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}