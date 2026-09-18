package com.my.notificationai.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

object AppTheme {
    val colors: AppColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColorScheme.current

    val typography: androidx.compose.material3.Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography
}

@Composable
fun MyNotificationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkColorScheme else LightColorScheme

    val m3Colors = if (darkTheme) {
        darkColorScheme(
            primary = appColors.primary,
            onPrimary = DarkTextPrimary,
            primaryContainer = appColors.primaryVariant,
            onPrimaryContainer = DarkTextPrimary,
            background = appColors.background,
            onBackground = appColors.textPrimary,
            surface = appColors.surface,
            onSurface = appColors.textPrimary,
            surfaceVariant = appColors.surfaceElevated,
            onSurfaceVariant = appColors.textSecondary,
            outline = appColors.border,
            error = appColors.error,
            onError = DarkTextPrimary
        )
    } else {
        lightColorScheme(
            primary = appColors.primary,
            onPrimary = LightSurface,
            primaryContainer = appColors.primaryVariant,
            onPrimaryContainer = LightSurface,
            background = appColors.background,
            onBackground = appColors.textPrimary,
            surface = appColors.surface,
            onSurface = appColors.textPrimary,
            surfaceVariant = appColors.surfaceStrong,
            onSurfaceVariant = appColors.textSecondary,
            outline = appColors.border,
            error = appColors.error,
            onError = LightSurface
        )
    }

    CompositionLocalProvider(
        LocalAppColorScheme provides appColors
    ) {
        MaterialTheme(
            colorScheme = m3Colors,
            typography = Typography,
            content = content
        )
    }
}
