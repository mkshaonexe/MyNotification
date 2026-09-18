package com.my.notificationai.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.my.notificationai.core.designsystem.components.AppTopBar
import com.my.notificationai.core.designsystem.components.FeatureNavCard
import com.my.notificationai.core.designsystem.components.HeroRingCard
import com.my.notificationai.core.designsystem.components.MetricCard
import com.my.notificationai.core.designsystem.theme.AppTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    innerPadding: PaddingValues,
    onNavigateToHistory: () -> Unit,
    onNavigateToRules: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val isBlockerActive by viewModel.isMasterBlockerEnabled.collectAsState()
    val blockedCount by viewModel.blockedCount.collectAsState()
    val allowedCount by viewModel.allowedCount.collectAsState()
    val importantCount by viewModel.importantCount.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(innerPadding)
    ) {
        AppTopBar(
            title = "My Notification",
            subtitle = "Less noise. More you.",
            onSettingsClick = onNavigateToSettings
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Hero Ring Card
            HeroRingCard(
                isBlockerActive = isBlockerActive,
                onToggle = { viewModel.toggleMasterBlocker() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3-Metric Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    count = blockedCount,
                    label = "Blocked",
                    countColor = AppTheme.colors.error,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    count = allowedCount,
                    label = "Allowed",
                    countColor = AppTheme.colors.success,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    count = importantCount,
                    label = "Important",
                    countColor = AppTheme.colors.security,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Feature Navigation Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureNavCard(
                    title = "Notification History",
                    subtitle = "View and search all notifications",
                    icon = Icons.Default.Notifications,
                    iconTint = AppTheme.colors.primary,
                    iconBg = AppTheme.colors.primary.copy(alpha = 0.12f),
                    onClick = onNavigateToHistory
                )

                FeatureNavCard(
                    title = "Blocking Rules",
                    subtitle = "Apps, keywords, schedules",
                    icon = Icons.Default.Shield,
                    iconTint = AppTheme.colors.security,
                    iconBg = AppTheme.colors.security.copy(alpha = 0.12f),
                    onClick = onNavigateToRules
                )

                FeatureNavCard(
                    title = "Analytics",
                    subtitle = "See your notification insights",
                    icon = Icons.Default.BarChart,
                    iconTint = AppTheme.colors.success,
                    iconBg = AppTheme.colors.success.copy(alpha = 0.12f),
                    onClick = onNavigateToAnalytics
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
