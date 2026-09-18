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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.my.notificationai.core.designsystem.components.AppTopBar
import com.my.notificationai.core.designsystem.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun BackupRestoreScreen(
    viewModel: SettingsViewModel,
    innerPadding: PaddingValues,
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var backupStatus by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(innerPadding)
    ) {
        AppTopBar(
            title = "Backup & Restore",
            subtitle = "Export and restore your rules and schedules",
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
            // Export Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppTheme.colors.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = AppTheme.colors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Export Configuration Backup",
                                style = AppTheme.typography.titleMedium,
                                color = AppTheme.colors.textPrimary
                            )
                            Text(
                                text = "Saves all blocking rules, whitelists, categories, and schedules to a JSON format",
                                style = AppTheme.typography.bodySmall,
                                color = AppTheme.colors.textSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                val json = viewModel.exportBackup()
                                backupStatus = "Export successful! ${json.length} bytes generated."
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Generate Backup JSON", color = Color.White)
                    }
                }
            }

            // Restore Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppTheme.colors.success.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = AppTheme.colors.success,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Restore Configuration",
                                style = AppTheme.typography.titleMedium,
                                color = AppTheme.colors.textPrimary
                            )
                            Text(
                                text = "Import rules, schedules, and whitelists from a previously saved backup",
                                style = AppTheme.typography.bodySmall,
                                color = AppTheme.colors.textSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                backupStatus = "Ready to restore backup JSON file."
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.surfaceElevated),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Select Backup to Restore", color = AppTheme.colors.textPrimary)
                    }
                }
            }

            if (backupStatus != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppTheme.colors.surfaceElevated)
                        .padding(14.dp)
                ) {
                    Text(
                        text = backupStatus!!,
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
