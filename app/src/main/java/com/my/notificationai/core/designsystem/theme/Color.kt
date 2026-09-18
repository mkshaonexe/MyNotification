package com.my.notificationai.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Dark Mode Palette
val DarkBackground       = Color(0xFF080D14)
val DarkSurface          = Color(0xFF101720)
val DarkSurfaceElevated  = Color(0xFF151F2B)
val DarkSurfaceStrong    = Color(0xFF1B2634)
val DarkBorder           = Color(0xFF263241)
val DarkBorderSubtle     = Color(0xFF1B2530)
val DarkTextPrimary      = Color(0xFFF5F7FA)
val DarkTextSecondary    = Color(0xFFA9B3C0)
val DarkTextTertiary     = Color(0xFF748091)
val DarkTextDisabled     = Color(0xFF505B69)

// Light Mode Palette
val LightBackground      = Color(0xFFF7F9FC)
val LightSurface         = Color(0xFFFFFFFF)
val LightSurfaceElevated  = Color(0xFFFFFFFF)
val LightSurfaceSoft      = Color(0xFFF1F4F8)
val LightBorder          = Color(0xFFE4E9F0)
val LightBorderStrong    = Color(0xFFD5DCE6)
val LightTextPrimary     = Color(0xFF111827)
val LightTextSecondary   = Color(0xFF5E6877)
val LightTextTertiary    = Color(0xFF8791A0)
val LightTextDisabled    = Color(0xFFB0B7C2)

// Semantic Accents (Shared)
val PrimaryBlue          = Color(0xFF4F6BFF)
val PrimaryBlueVariant   = Color(0xFF3E57E8)
val SuccessGreen         = Color(0xFF2CCB82)
val SuccessGreenDarkBg   = Color(0xFF123025)
val SuccessGreenLightBg  = Color(0xFFDDF8EC)
val ErrorRed             = Color(0xFFF05B67)
val ErrorRedDarkBg       = Color(0xFF351B20)
val ErrorRedLightBg      = Color(0xFFFDE5E7)
val SecurityPurple       = Color(0xFF8B6CFF)
val SecurityPurpleDarkBg = Color(0xFF261D42)
val SecurityPurpleLightBg= Color(0xFFEFEAFF)
val WarningOrange        = Color(0xFFF5B84B)
val WarningOrangeDarkBg  = Color(0xFF332A17)
val WarningOrangeLightBg = Color(0xFFFFF3D6)
val InfoBlue             = Color(0xFF55A8FF)

@Immutable
data class AppColorScheme(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceStrong: Color,
    val border: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,
    val primary: Color,
    val primaryVariant: Color,
    val success: Color,
    val successContainer: Color,
    val error: Color,
    val errorContainer: Color,
    val security: Color,
    val securityContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val info: Color
)

val DarkColorScheme = AppColorScheme(
    isDark = true,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceElevated = DarkSurfaceElevated,
    surfaceStrong = DarkSurfaceStrong,
    border = DarkBorder,
    borderSubtle = DarkBorderSubtle,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    textDisabled = DarkTextDisabled,
    primary = PrimaryBlue,
    primaryVariant = PrimaryBlueVariant,
    success = SuccessGreen,
    successContainer = SuccessGreenDarkBg,
    error = ErrorRed,
    errorContainer = ErrorRedDarkBg,
    security = SecurityPurple,
    securityContainer = SecurityPurpleDarkBg,
    warning = WarningOrange,
    warningContainer = WarningOrangeDarkBg,
    info = InfoBlue
)

val LightColorScheme = AppColorScheme(
    isDark = false,
    background = LightBackground,
    surface = LightSurface,
    surfaceElevated = LightSurfaceElevated,
    surfaceStrong = LightSurfaceSoft,
    border = LightBorder,
    borderSubtle = LightBorder,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textTertiary = LightTextTertiary,
    textDisabled = LightTextDisabled,
    primary = PrimaryBlue,
    primaryVariant = PrimaryBlueVariant,
    success = SuccessGreen,
    successContainer = SuccessGreenLightBg,
    error = ErrorRed,
    errorContainer = ErrorRedLightBg,
    security = SecurityPurple,
    securityContainer = SecurityPurpleLightBg,
    warning = WarningOrange,
    warningContainer = WarningOrangeLightBg,
    info = InfoBlue
)

val LocalAppColorScheme = staticCompositionLocalOf { DarkColorScheme }
