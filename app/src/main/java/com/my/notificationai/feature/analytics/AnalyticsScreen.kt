package com.my.notificationai.feature.analytics

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.my.notificationai.core.designsystem.components.AppRankingRow
import com.my.notificationai.core.designsystem.components.AppTopBar
import com.my.notificationai.core.designsystem.components.BarChart
import com.my.notificationai.core.designsystem.theme.AppTheme

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    innerPadding: PaddingValues,
    canGoBack: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    val selectedRange by viewModel.selectedTimeRange.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val blockedCount by viewModel.blockedCount.collectAsState()
    val allowedCount by viewModel.allowedCount.collectAsState()
    val importantCount by viewModel.importantCount.collectAsState()
    val barChartData by viewModel.barChartData.collectAsState()
    val topApps by viewModel.topApps.collectAsState()

    var showDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(innerPadding)
    ) {
        AppTopBar(
            title = "Analytics",
            canGoBack = canGoBack,
            onBackClick = onBackClick,
            trailingContent = {
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(AppTheme.colors.surfaceElevated)
                            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(999.dp))
                            .clickable { showDropdown = true }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedRange.label,
                                style = AppTheme.typography.labelSmall,
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = AppTheme.colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        modifier = Modifier.background(AppTheme.colors.surface)
                    ) {
                        TimeRange.entries.forEach { range ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = range.label,
                                        style = AppTheme.typography.bodyMedium,
                                        color = if (selectedRange == range) AppTheme.colors.primary else AppTheme.colors.textPrimary
                                    )
                                },
                                onClick = {
                                    viewModel.setTimeRange(range)
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Analytics Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = totalCount.toString(),
                        style = AppTheme.typography.displayLarge,
                        color = AppTheme.colors.textPrimary
                    )
                    Text(
                        text = "Total Notifications",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                    )

                    // Canvas Bar Chart
                    BarChart(
                        items = barChartData,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = AppTheme.colors.borderSubtle)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Metric Split Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = blockedCount.toString(),
                                style = AppTheme.typography.titleMedium,
                                color = AppTheme.colors.error
                            )
                            Text(
                                text = "Blocked",
                                style = AppTheme.typography.labelSmall,
                                color = AppTheme.colors.textSecondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = allowedCount.toString(),
                                style = AppTheme.typography.titleMedium,
                                color = AppTheme.colors.success
                            )
                            Text(
                                text = "Allowed",
                                style = AppTheme.typography.labelSmall,
                                color = AppTheme.colors.textSecondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = importantCount.toString(),
                                style = AppTheme.typography.titleMedium,
                                color = AppTheme.colors.security
                            )
                            Text(
                                text = "Important",
                                style = AppTheme.typography.labelSmall,
                                color = AppTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }

            // Top Apps Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Top Apps",
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.colors.textPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (topApps.isEmpty()) {
                        Text(
                            text = "No app notifications recorded for this period",
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.textTertiary,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        val maxCount = topApps.maxOfOrNull { it.totalCount } ?: 1
                        topApps.forEach { appCount ->
                            AppRankingRow(
                                packageName = appCount.packageName,
                                appLabel = appCount.appLabel,
                                count = appCount.totalCount,
                                maxCount = maxCount
                            )
                        }
                    }
                }
            }

            // Smart Insights Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.surfaceElevated)
                    .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = AppTheme.colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Smart Insights",
                            style = AppTheme.typography.titleSmall,
                            color = AppTheme.colors.textPrimary
                        )
                        Text(
                            text = if (topApps.isNotEmpty()) {
                                "${topApps.first().appLabel} is generating the highest notification volume."
                            } else {
                                "As notifications arrive, insights regarding peak hours and distraction sources will appear here."
                            },
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.textSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
