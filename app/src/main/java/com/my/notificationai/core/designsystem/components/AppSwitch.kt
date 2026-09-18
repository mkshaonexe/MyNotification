package com.my.notificationai.core.designsystem.components

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.my.notificationai.core.designsystem.theme.AppTheme

@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = AppTheme.colors.primary,
            uncheckedThumbColor = AppTheme.colors.textTertiary,
            uncheckedTrackColor = AppTheme.colors.surfaceElevated,
            uncheckedBorderColor = AppTheme.colors.border
        )
    )
}
