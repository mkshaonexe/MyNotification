package com.my.notificationai.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.my.notificationai.core.designsystem.components.AppTopBar
import com.my.notificationai.core.designsystem.components.StatusBadge
import com.my.notificationai.core.designsystem.theme.AppTheme

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    innerPadding: PaddingValues,
    onNavigateToProtection: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToAbout: () -> Unit,
    canGoBack: Boolean = true,
    onBackClick: () -> Unit = {}
) {
    val themePref by viewModel.themePreference.collectAsState()
    val isAccessGranted = viewModel.isNotificationAccessGranted()
    val isBatteryOptimized = viewModel.isBatteryOptimizationIgnored()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(innerPadding)
    ) {
        AppTopBar(
            title = "Settings",
            canGoBack = canGoBack,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Appearance Section
            SettingsSection(title = "Appearance") {
                ThemeRadioRow(
                    label = "Dark Mode",
                    icon = Icons.Default.DarkMode,
                    isSelected = themePref == "DARK",
                    onClick = { viewModel.setTheme("DARK") }
                )
                HorizontalDivider(color = AppTheme.colors.borderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                ThemeRadioRow(
                    label = "Light Mode",
                    icon = Icons.Default.LightMode,
                    isSelected = themePref == "LIGHT",
                    onClick = { viewModel.setTheme("LIGHT") }
                )
                HorizontalDivider(color = AppTheme.colors.borderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                ThemeRadioRow(
                    label = "System Default",
                    icon = Icons.Default.BrightnessAuto,
                    isSelected = themePref == "SYSTEM",
                    onClick = { viewModel.setTheme("SYSTEM") }
                )
            }

            // General Section
            SettingsSection(title = "General") {
                SettingsActionRow(
                    title = "Notification Access",
                    trailingBadge = {
                        if (isAccessGranted) {
                            StatusBadge(text = "Granted", textColor = AppTheme.colors.success, bgColor = AppTheme.colors.successContainer)
                        } else {
                            StatusBadge(text = "Needs attention", textColor = AppTheme.colors.error, bgColor = AppTheme.colors.errorContainer)
                        }
                    },
                    onClick = { viewModel.openNotificationListenerSettings() }
                )
                HorizontalDivider(color = AppTheme.colors.borderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsActionRow(
                    title = "Battery Optimization",
                    trailingBadge = {
                        if (isBatteryOptimized) {
                            StatusBadge(text = "Ignored", textColor = AppTheme.colors.success, bgColor = AppTheme.colors.successContainer)
                        } else {
                            StatusBadge(text = "Fix", textColor = AppTheme.colors.warning, bgColor = AppTheme.colors.warningContainer)
                        }
                    },
                    onClick = { viewModel.openBatteryOptimizationSettings() }
                )
                HorizontalDivider(color = AppTheme.colors.borderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsNavRow(
                    title = "Protection Center (OTP & Financial)",
                    onClick = onNavigateToProtection
                )
                HorizontalDivider(color = AppTheme.colors.borderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsNavRow(
                    title = "Data & Privacy",
                    onClick = onNavigateToPrivacy
                )
                HorizontalDivider(color = AppTheme.colors.borderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsNavRow(
                    title = "Backup & Restore",
                    onClick = onNavigateToBackup
                )
                HorizontalDivider(color = AppTheme.colors.borderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsNavRow(
                    title = "Help & FAQ",
                    onClick = onNavigateToHelp
                )
                HorizontalDivider(color = AppTheme.colors.borderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsNavRow(
                    title = "About",
                    onClick = onNavigateToAbout
                )
            }

            // Quote Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "“A quieter phone,",
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.colors.textTertiary
                    )
                    Text(
                        text = "a calmer mind.”",
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.colors.textTertiary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = AppTheme.typography.labelLarge,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AppTheme.colors.surface)
                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun ThemeRadioRow(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) AppTheme.colors.primary else AppTheme.colors.textSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textPrimary
            )
        }

        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = AppTheme.colors.primary,
                unselectedColor = AppTheme.colors.border
            )
        )
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    trailingBadge: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.textPrimary
        )
        trailingBadge()
    }
}

@Composable
private fun SettingsNavRow(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.textPrimary
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = AppTheme.colors.textTertiary,
            modifier = Modifier.size(20.dp)
        )
    }
}
