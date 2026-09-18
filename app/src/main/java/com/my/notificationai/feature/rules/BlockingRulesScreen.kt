package com.my.notificationai.feature.rules

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun BlockingRulesScreen(
    viewModel: RulesViewModel,
    innerPadding: PaddingValues,
    onNavigateToSocialApps: () -> Unit,
    onNavigateToSelectedApps: () -> Unit,
    onNavigateToKeywords: () -> Unit,
    onNavigateToSchedules: () -> Unit,
    onNavigateToWhitelistedApps: () -> Unit,
    onNavigateToRuleBuilder: () -> Unit,
    onNavigateToRulePriority: () -> Unit,
    canGoBack: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    val rules by viewModel.rules.collectAsState()
    val blockedApps by viewModel.blockedApps.collectAsState()
    val whitelistedKeywords by viewModel.whitelistedKeywords.collectAsState()
    val schedules by viewModel.schedules.collectAsState()

    val rulesMap = rules.associateBy { it.ruleType }
    val socialRule = rulesMap["SOCIAL_MEDIA"]
    val selectedRule = rulesMap["SELECTED_APPS"]
    val keywordsRule = rulesMap["ALLOW_IMPORTANT_KEYWORDS"]
    val scheduleRule = rulesMap["SCHEDULE_BLOCKING"]
    val everythingRule = rulesMap["BLOCK_EVERYTHING"]
    val customRules = rules.filter { it.ruleType == "CUSTOM" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(innerPadding)
    ) {
        AppTopBar(
            title = "Blocking Rules",
            canGoBack = canGoBack,
            onBackClick = onBackClick,
            trailingContent = {
                IconButton(
                    onClick = onNavigateToRuleBuilder,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Rule",
                        tint = AppTheme.colors.primary
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Card 1: Block Social Media
            RuleMasterCard(
                title = "Block Social Media",
                subtitle = "Instagram, Facebook, TikTok...",
                icon = Icons.Default.Share,
                iconBg = Color(0xFFFF8A65).copy(alpha = 0.18f),
                iconTint = Color(0xFFFF7043),
                isEnabled = socialRule?.isEnabled ?: true,
                onToggle = { isEnabled ->
                    if (socialRule != null) viewModel.toggleRule(socialRule.id, isEnabled)
                    else viewModel.toggleRuleByType("SOCIAL_MEDIA", isEnabled)
                },
                onClick = onNavigateToSocialApps
            )

            // Card 2: Block Selected Apps
            RuleMasterCard(
                title = "Block Selected Apps",
                subtitle = "${blockedApps.count { it.isBlocked }} apps selected",
                icon = Icons.Default.Apps,
                iconBg = AppTheme.colors.success.copy(alpha = 0.15f),
                iconTint = AppTheme.colors.success,
                isEnabled = selectedRule?.isEnabled ?: false,
                onToggle = { isEnabled ->
                    if (selectedRule != null) viewModel.toggleRule(selectedRule.id, isEnabled)
                    else viewModel.toggleRuleByType("SELECTED_APPS", isEnabled)
                },
                onClick = onNavigateToSelectedApps
            )

            // Card 3: Allow Important Keywords
            RuleMasterCard(
                title = "Allow Important Keywords",
                subtitle = "OTP, verification, payment (${whitelistedKeywords.size} keywords)",
                icon = Icons.Default.Key,
                iconBg = AppTheme.colors.security.copy(alpha = 0.15f),
                iconTint = AppTheme.colors.security,
                isEnabled = keywordsRule?.isEnabled ?: true,
                onToggle = { isEnabled ->
                    if (keywordsRule != null) viewModel.toggleRule(keywordsRule.id, isEnabled)
                    else viewModel.toggleRuleByType("ALLOW_IMPORTANT_KEYWORDS", isEnabled)
                },
                onClick = onNavigateToKeywords
            )

            // Card 4: Schedule Blocking
            val activeSchedule = schedules.firstOrNull { it.isEnabled }
            val scheduleSub = if (activeSchedule != null) {
                "${String.format("%02d:%02d", activeSchedule.startHour, activeSchedule.startMinute)} – ${String.format("%02d:%02d", activeSchedule.endHour, activeSchedule.endMinute)}"
            } else {
                "10:00 PM – 7:00 AM"
            }
            RuleMasterCard(
                title = "Schedule Blocking",
                subtitle = scheduleSub,
                icon = Icons.Default.Schedule,
                iconBg = AppTheme.colors.primary.copy(alpha = 0.15f),
                iconTint = AppTheme.colors.primary,
                isEnabled = scheduleRule?.isEnabled ?: false,
                onToggle = { isEnabled ->
                    if (scheduleRule != null) viewModel.toggleRule(scheduleRule.id, isEnabled)
                    else viewModel.toggleRuleByType("SCHEDULE_BLOCKING", isEnabled)
                },
                onClick = onNavigateToSchedules
            )

            // Card 5: Block Everything
            RuleMasterCard(
                title = "Block Everything",
                subtitle = "Except whitelisted apps",
                icon = Icons.Default.Block,
                iconBg = AppTheme.colors.error.copy(alpha = 0.15f),
                iconTint = AppTheme.colors.error,
                isEnabled = everythingRule?.isEnabled ?: false,
                onToggle = { isEnabled ->
                    if (everythingRule != null) viewModel.toggleRule(everythingRule.id, isEnabled)
                    else viewModel.toggleRuleByType("BLOCK_EVERYTHING", isEnabled)
                },
                onClick = onNavigateToWhitelistedApps
            )

            // Card 6: Custom Rules
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
                    .clickable { onNavigateToRuleBuilder() }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFBA68C8).copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color(0xFFBA68C8),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Custom Rules",
                            style = AppTheme.typography.titleMedium,
                            color = AppTheme.colors.textPrimary
                        )
                        Text(
                            text = "${customRules.size} custom rules configured",
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.textSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = AppTheme.colors.textTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Rule Hierarchy Visualizer Link Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppTheme.colors.surfaceElevated)
                    .clickable { onNavigateToRulePriority() }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "View Rule Evaluation Priority Hierarchy →",
                    style = AppTheme.typography.labelMedium,
                    color = AppTheme.colors.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RuleMasterCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.surface)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary
                )
                Text(
                    text = subtitle,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            AppSwitch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}
