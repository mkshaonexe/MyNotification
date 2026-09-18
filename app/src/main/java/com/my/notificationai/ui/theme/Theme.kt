package com.my.notificationai.ui.theme

import androidx.compose.runtime.Composable

@Composable
fun MyNotificationTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    com.my.notificationai.core.designsystem.theme.MyNotificationTheme(
        darkTheme = darkTheme,
        content = content
    )
}