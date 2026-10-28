package com.dailyrunner.drivertracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = OnPurpleContainer,
    secondary = PaidGreen,
    onSecondary = Color.White,
    secondaryContainer = DarkCardBg,
    onSecondaryContainer = Color.White,
    background = DarkBg,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = DarkCardBg,
    onSurfaceVariant = TextSecondary,
    outline = OutlineBorder
)

private val LightColorScheme = lightColorScheme(
    primary = LightPurplePrimary,
    onPrimary = Color.White,
    primaryContainer = LightPurpleContainer,
    onPrimaryContainer = LightOnPurpleContainer,
    secondary = PaidGreen,
    onSecondary = Color.White,
    secondaryContainer = LightCardBg,
    onSecondaryContainer = TextPrimaryLight,
    background = LightBg,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightOutlineBorder
)

@Composable
fun DailyRunnerTheme(
    themeMode: String = "SYSTEM",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> systemInDark
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
