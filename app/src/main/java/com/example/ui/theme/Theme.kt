package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlueAccent,
    onPrimary = SurfaceLight,
    primaryContainer = DarkBlueSecondary,
    onPrimaryContainer = SkyBlueLight,
    secondary = SkyBlueLight,
    onSecondary = DarkBluePrimary,
    tertiary = AmberGold,
    background = BackgroundDark,
    onBackground = TextLight,
    surface = SurfaceDark,
    onSurface = TextLight,
    surfaceVariant = SurfaceCardDark,
    onSurfaceVariant = TextMutedDark,
    error = ErrorRed,
    onError = SurfaceLight
)

private val LightColorScheme = lightColorScheme(
    primary = DarkBluePrimary,
    onPrimary = SurfaceLight,
    primaryContainer = SkyBlueContainer,
    onPrimaryContainer = DarkBluePrimary,
    secondary = SkyBlueAccent,
    onSecondary = SurfaceLight,
    tertiary = AmberGold,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = SurfaceLight,
    onSurface = TextDark,
    surfaceVariant = SurfaceCardLight,
    onSurfaceVariant = TextMuted,
    error = ErrorRed,
    onError = SurfaceLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent branding colors
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

