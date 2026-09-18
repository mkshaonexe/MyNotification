package com.my.notificationai.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.my.notificationai.core.designsystem.components.AppTopBar
import com.my.notificationai.core.designsystem.theme.AppTheme

@Composable
fun AboutScreen(
    innerPadding: PaddingValues,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(innerPadding)
    ) {
        AppTopBar(
            title = "About",
            canGoBack = true,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "My Notification",
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary
                    )
                    Text(
                        text = "Version 1.0 (Production Release)",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "A personal notification operating layer for Android that brings calm, privacy, and deterministic control to your device.",
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Architecture & Privacy Highlights",
                        style = AppTheme.typography.titleSmall,
                        color = AppTheme.colors.textPrimary
                    )
                    Text(text = "• 100% Offline & Local Room SQLite Database", style = AppTheme.typography.bodySmall, color = AppTheme.colors.textSecondary)
                    Text(text = "• Zero Third-Party Trackers or Analytics SDKs", style = AppTheme.typography.bodySmall, color = AppTheme.colors.textSecondary)
                    Text(text = "• Logical Notification Lifecycle Deduplication", style = AppTheme.typography.bodySmall, color = AppTheme.colors.textSecondary)
                    Text(text = "• Multi-Signal Contextual OTP & Financial Protection", style = AppTheme.typography.bodySmall, color = AppTheme.colors.textSecondary)
                    Text(text = "• Clean Architecture (Compose · Flow · Hilt · WorkManager)", style = AppTheme.typography.bodySmall, color = AppTheme.colors.textSecondary)
                }
            }
        }
    }
}
