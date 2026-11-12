package com.dailyrunner.drivertracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

@Immutable
data class DailyRunnerExtendedColors(
    val isDark: Boolean,
    val nestedSurface: Color,
    val cardBorder: Color,
    val inputBorder: Color,
    val paidText: Color,
    val paidBg: Color,
    val paidBorder: Color,
    val unpaidText: Color,
    val unpaidBg: Color,
    val unpaidBorder: Color,
    val prefillText: Color,
    val errorText: Color,
    val heroStart: Color,
    val heroEnd: Color
)

private val DarkExtendedColors = DailyRunnerExtendedColors(
    isDark = true,
    nestedSurface = DarkSurface,
    cardBorder = OutlineBorder,
    inputBorder = OutlineBorder.copy(alpha = 0.7f),
    paidText = OnPaidGreen,
    paidBg = PaidGreenBg,
    paidBorder = Color(0xFF1E4D31),
    unpaidText = OnUnpaidAmber,
    unpaidBg = UnpaidAmberBg,
    unpaidBorder = Color(0xFF573F1A),
    prefillText = TextPrimary,
    errorText = ErrorRed,
    heroStart = Color(0xFF6A26CD),
    heroEnd = Color(0xFF9E77ED)
)

private val LightExtendedColors = DailyRunnerExtendedColors(
    isDark = false,
    nestedSurface = LightNestedBg,
    cardBorder = LightCardBorder,
    inputBorder = LightOutlineBorder,
    paidText = PaidGreenLight,
    paidBg = PaidGreenBgLight,
    paidBorder = PaidGreenBorderLight,
    unpaidText = UnpaidAmberLight,
    unpaidBg = UnpaidAmberBgLight,
    unpaidBorder = UnpaidAmberBorderLight,
    prefillText = OnPaidGreenLight,
    errorText = ErrorRedLight,
    heroStart = LightPurpleGradientStart,
    heroEnd = LightPurpleGradientEnd
)

val LocalDailyRunnerColors = staticCompositionLocalOf { DarkExtendedColors }

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
    outline = OutlineBorder,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = LightPurplePrimary,
    onPrimary = Color.White,
    primaryContainer = LightPurpleContainer,
    onPrimaryContainer = LightOnPurpleContainer,
    secondary = PaidGreenLight,
    onSecondary = Color.White,
    secondaryContainer = LightCardBg,
    onSecondaryContainer = TextPrimaryLight,
    background = LightBg,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightOutlineBorder,
    error = ErrorRedLight
)

@Composable
fun DailyRunnerTheme(
    themeMode: String = "SYSTEM",
    fontSize: String = "MEDIUM",
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

    val extendedColors = if (isDark) DarkExtendedColors else LightExtendedColors

    val scaleFactor = when (fontSize) {
        "SMALL" -> 0.88f
        "LARGE" -> 1.15f
        else -> 1.0f
    }

    val currentDensity = LocalDensity.current
    val customDensity = Density(
        density = currentDensity.density,
        fontScale = currentDensity.fontScale * scaleFactor
    )

    CompositionLocalProvider(
        LocalDensity provides customDensity,
        LocalDailyRunnerColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
