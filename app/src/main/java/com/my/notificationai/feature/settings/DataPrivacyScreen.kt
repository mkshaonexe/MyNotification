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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.my.notificationai.core.designsystem.components.AppTopBar
import com.my.notificationai.core.designsystem.theme.AppTheme

@Composable
fun DataPrivacyScreen(
    viewModel: SettingsViewModel,
    innerPadding: PaddingValues,
    onBackClick: () -> Unit
) {
    val retentionDays by viewModel.dataRetentionDays.collectAsState()
    val storageMb by viewModel.storageSizeMb.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val retentionOptions = listOf(
        7 to "7 Days",
        30 to "30 Days",
        90 to "90 Days (Recommended)",
        180 to "180 Days",
        365 to "1 Year",
        -1 to "Forever (No automatic deletion)"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(innerPadding)
    ) {
        AppTopBar(
            title = "Data & Privacy",
            subtitle = "Local storage and retention controls",
            canGoBack = true,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Privacy Badge Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.surfaceElevated)
                    .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = AppTheme.colors.success,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "100% On-Device Privacy",
                            style = AppTheme.typography.titleSmall,
                            color = AppTheme.colors.textPrimary
                        )
                        Text(
                            text = "Zero cloud tracking, zero network upload. All notification events and rules remain strictly inside your local device database.",
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.textSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Storage Size Display Card
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Database Storage Usage",
                            style = AppTheme.typography.titleSmall,
                            color = AppTheme.colors.textPrimary
                        )
                        Text(
                            text = "Estimated SQLite storage footprint",
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.textTertiary
                        )
                    }
                    Text(
                        text = "${String.format("%.2f", storageMb)} MB",
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.colors.primary
                    )
                }
            }

            // Retention Period Selector
            Column {
                Text(
                    text = "Automatic Data Retention",
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
                        retentionOptions.forEachIndexed { index, (days, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setRetentionDays(days) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    style = AppTheme.typography.bodyMedium,
                                    color = AppTheme.colors.textPrimary
                                )
                                RadioButton(
                                    selected = retentionDays == days,
                                    onClick = { viewModel.setRetentionDays(days) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = AppTheme.colors.primary,
                                        unselectedColor = AppTheme.colors.border
                                    )
                                )
                            }
                            if (index < retentionOptions.size - 1) {
                                HorizontalDivider(color = AppTheme.colors.borderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Delete All History Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppTheme.colors.errorContainer)
                    .border(1.dp, AppTheme.colors.error.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .clickable { showDeleteConfirm = true }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = AppTheme.colors.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Wipe All Notification History",
                        style = AppTheme.typography.labelLarge,
                        color = AppTheme.colors.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = AppTheme.colors.surface,
            title = {
                Text("Confirm History Wipe", style = AppTheme.typography.titleMedium, color = AppTheme.colors.textPrimary)
            },
            text = {
                Text(
                    "Are you sure you want to delete all stored notification events and updates? This action is permanent and cannot be undone.",
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.error)
                ) {
                    Text("Delete Everything", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = AppTheme.colors.textSecondary)
                }
            }
        )
    }
}
