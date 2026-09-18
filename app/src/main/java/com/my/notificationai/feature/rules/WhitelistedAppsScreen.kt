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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.my.notificationai.core.designsystem.components.AppTopBar
import com.my.notificationai.core.designsystem.theme.AppTheme

@Composable
fun WhitelistedAppsScreen(
    viewModel: RulesViewModel,
    innerPadding: PaddingValues,
    onBackClick: () -> Unit
) {
    val whitelistedApps by viewModel.whitelistedApps.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(innerPadding)
    ) {
        AppTopBar(
            title = "Whitelisted Apps",
            subtitle = "Always allowed to bypass blockers",
            canGoBack = true,
            onBackClick = onBackClick,
            trailingContent = {
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Whitelist App",
                        tint = AppTheme.colors.primary
                    )
                }
            }
        )

        if (whitelistedApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No apps in whitelist yet. Tap + to add one.",
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textTertiary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(whitelistedApps, key = { it.packageName }) { app ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppTheme.colors.surface)
                            .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.appLabel,
                                    style = AppTheme.typography.titleSmall,
                                    color = AppTheme.colors.textPrimary
                                )
                                Text(
                                    text = app.packageName,
                                    style = AppTheme.typography.bodySmall,
                                    color = AppTheme.colors.textTertiary
                                )
                            }
                            IconButton(
                                onClick = { viewModel.removeWhitelistedApp(app.packageName) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = AppTheme.colors.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var label by remember { mutableStateOf("") }
        var pkg by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = AppTheme.colors.surface,
            title = {
                Text("Add Whitelisted App", style = AppTheme.typography.titleMedium, color = AppTheme.colors.textPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("App Name:", style = AppTheme.typography.labelSmall, color = AppTheme.colors.textSecondary)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppTheme.colors.surfaceElevated)
                            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = label,
                            onValueChange = { label = it },
                            textStyle = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.textPrimary),
                            cursorBrush = SolidColor(AppTheme.colors.primary),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Text("Package Name:", style = AppTheme.typography.labelSmall, color = AppTheme.colors.textSecondary)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppTheme.colors.surfaceElevated)
                            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = pkg,
                            onValueChange = { pkg = it },
                            textStyle = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.textPrimary),
                            cursorBrush = SolidColor(AppTheme.colors.primary),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pkg.isNotBlank()) {
                            viewModel.addWhitelistedApp(pkg.trim(), if (label.isNotBlank()) label else pkg)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primary)
                ) {
                    Text("Add", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = AppTheme.colors.textSecondary)
                }
            }
        )
    }
}
