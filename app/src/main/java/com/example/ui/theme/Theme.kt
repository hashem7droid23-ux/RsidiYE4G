package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Yemen4GPrimaryLight,
    onPrimary = Color(0xFF003258),
    primaryContainer = Yemen4GElevatedNavy,
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Yemen4GAccentGold,
    onSecondary = Color(0xFF3F2E00),
    secondaryContainer = Color(0xFF5B4300),
    onSecondaryContainer = Color(0xFFFFDF9E),
    tertiary = Yemen4GSuccessGreen,
    onTertiary = Color.White,
    background = Yemen4GDarkNavy,
    onBackground = Yemen4GTextPrimary,
    surface = Yemen4GCardNavy,
    onSurface = Yemen4GTextPrimary,
    surfaceVariant = Yemen4GElevatedNavy,
    onSurfaceVariant = Yemen4GTextSecondary,
    outline = Yemen4GDivider
)

private val LightColorScheme = lightColorScheme(
    primary = Yemen4GPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0E8FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Yemen4GAccentAmber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFECB3),
    onSecondaryContainer = Color(0xFF261900),
    tertiary = Yemen4GSuccessGreen,
    onTertiary = Color.White,
    background = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek tech dark theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
