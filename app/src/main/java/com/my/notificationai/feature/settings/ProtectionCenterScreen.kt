package com.my.notificationai.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.my.notificationai.core.designsystem.components.AppSwitch
import com.my.notificationai.core.designsystem.components.AppTopBar
import com.my.notificationai.core.designsystem.theme.AppTheme

@Composable
fun ProtectionCenterScreen(
    viewModel: SettingsViewModel,
    innerPadding: PaddingValues,
    onBackClick: () -> Unit
) {
    val isOtpEnabled by viewModel.isOtpProtectionEnabled.collectAsState()
    val isFinancialEnabled by viewModel.isFinancialProtectionEnabled.collectAsState()
    val isPromoEnabled by viewModel.isPromotionalSmsFilterEnabled.collectAsState()
    val isEmergencyEnabled by viewModel.isEmergencyBypassEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(innerPadding)
    ) {
        AppTopBar(
            title = "Protection Center",
            subtitle = "Guaranteed bypass rules for vital alerts",
            canGoBack = true,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ProtectionToggleCard(
                title = "OTP & Verification Codes",
                description = "Automatically identifies 2FA, OTP, and banking tokens to prevent missed logins",
                icon = Icons.Default.Password,
                iconColor = AppTheme.colors.security,
                isEnabled = isOtpEnabled,
                onToggle = { viewModel.toggleOtpProtection(it) }
            )

            ProtectionToggleCard(
                title = "Financial & Banking Alerts",
                description = "Protects incoming money transfers, debit/credit card alerts, and statement notifications",
                icon = Icons.Default.AccountBalance,
                iconColor = AppTheme.colors.success,
                isEnabled = isFinancialEnabled,
                onToggle = { viewModel.toggleFinancialProtection(it) }
            )

            ProtectionToggleCard(
                title = "Emergency & Phone Calls",
                description = "Incoming voice calls, alarms, and emergency broadcasts always bypass blockers",
                icon = Icons.Default.Call,
                iconColor = AppTheme.colors.primary,
                isEnabled = isEmergencyEnabled,
                onToggle = { viewModel.toggleEmergencyBypass(it) }
            )

            ProtectionToggleCard(
                title = "Promotional SMS Spam Filter",
                description = "Suppresses telecom recharge bundles, advertising campaigns, and marketing text blasts",
                icon = Icons.Default.Block,
                iconColor = AppTheme.colors.error,
                isEnabled = isPromoEnabled,
                onToggle = { viewModel.togglePromotionalFilter(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProtectionToggleCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.surface)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary
                )
                Text(
                    text = description,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            AppSwitch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}
