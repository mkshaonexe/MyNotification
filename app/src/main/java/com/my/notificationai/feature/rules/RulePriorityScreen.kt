package com.my.notificationai.feature.rules

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.my.notificationai.core.designsystem.components.AppTopBar
import com.my.notificationai.core.designsystem.components.StatusBadge
import com.my.notificationai.core.designsystem.theme.AppTheme

data class HierarchyTier(
    val priority: Int,
    val name: String,
    val description: String,
    val action: String,
    val color: Color
)

@Composable
fun RulePriorityScreen(
    innerPadding: PaddingValues,
    onBackClick: () -> Unit
) {
    val hierarchy = listOf(
        HierarchyTier(1, "Emergency & Calls Bypass", "Phone calls, alarms, and emergency alerts are never blocked", "ALLOW", AppTheme.colors.success),
        HierarchyTier(2, "OTP & Security Protection", "Two-factor auth and verification codes are unconditionally allowed", "ALLOW", AppTheme.colors.security),
        HierarchyTier(3, "Financial Alerts Protection", "Banking apps, transactions, balances, and payment confirmations", "ALLOW", AppTheme.colors.security),
        HierarchyTier(4, "App Whitelist", "Explicitly whitelisted applications by user", "ALLOW", AppTheme.colors.success),
        HierarchyTier(5, "Keyword Whitelist", "Notifications containing whitelisted keywords", "ALLOW", AppTheme.colors.success),
        HierarchyTier(6, "Promotional SMS Spam Filter", "Telecom bundles and marketing spam from SMS apps", "BLOCK", AppTheme.colors.error),
        HierarchyTier(7, "Recurring Schedules", "Active schedule time windows (e.g. Night Focus)", "BLOCK", AppTheme.colors.primary),
        HierarchyTier(8, "Master Blocker Modes", "Social Media, Selected Apps, or Block Everything mode", "BLOCK", AppTheme.colors.error),
        HierarchyTier(9, "Custom Rule Predicates", "User-defined multi-condition rules", "CUSTOM", Color(0xFFBA68C8)),
        HierarchyTier(10, "Default Fallback Policy", "When no blocking rule matches", "ALLOW", AppTheme.colors.success)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(innerPadding)
    ) {
        AppTopBar(
            title = "Rule Hierarchy",
            subtitle = "Evaluation priority order from highest to lowest",
            canGoBack = true,
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(hierarchy) { index, tier ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppTheme.colors.surface)
                        .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.surfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = AppTheme.typography.labelMedium,
                                color = AppTheme.colors.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tier.name,
                                style = AppTheme.typography.titleSmall,
                                color = AppTheme.colors.textPrimary
                            )
                            Text(
                                text = tier.description,
                                style = AppTheme.typography.bodySmall,
                                color = AppTheme.colors.textSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        StatusBadge(
                            text = tier.action,
                            textColor = tier.color,
                            bgColor = tier.color.copy(alpha = 0.15f)
                        )
                    }
                }
            }
        }
    }
}
