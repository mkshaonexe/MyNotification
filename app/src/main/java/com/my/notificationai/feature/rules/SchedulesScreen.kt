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
import com.my.notificationai.core.database.entities.Schedule
import com.my.notificationai.core.designsystem.components.AppSwitch
import com.my.notificationai.core.designsystem.components.AppTopBar
import com.my.notificationai.core.designsystem.theme.AppTheme

@Composable
fun SchedulesScreen(
    viewModel: RulesViewModel,
    innerPadding: PaddingValues,
    onBackClick: () -> Unit
) {
    val schedules by viewModel.schedules.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(innerPadding)
    ) {
        AppTopBar(
            title = "Schedules",
            canGoBack = true,
            onBackClick = onBackClick,
            trailingContent = {
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Schedule",
                        tint = AppTheme.colors.primary
                    )
                }
            }
        )

        if (schedules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No schedules created yet. Tap + to add one.",
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textTertiary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(schedules, key = { it.id }) { schedule ->
                    ScheduleItemCard(
                        schedule = schedule,
                        onToggle = { viewModel.toggleSchedule(schedule.id, it) },
                        onDelete = { viewModel.deleteSchedule(schedule.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddScheduleDialog(
            onDismiss = { showAddDialog = false },
            onSave = { schedule ->
                viewModel.saveSchedule(schedule)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ScheduleItemCard(
    schedule: Schedule,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val timeFormatted = "${String.format("%02d:%02d", schedule.startHour, schedule.startMinute)} – ${String.format("%02d:%02d", schedule.endHour, schedule.endMinute)}"

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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.title,
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary
                )
                Text(
                    text = timeFormatted,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.primary,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "Repeat: ${schedule.repeatDays} · Exceptions: ${schedule.exceptions}",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = AppTheme.colors.textTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            AppSwitch(
                checked = schedule.isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
private fun AddScheduleDialog(
    onDismiss: () -> Unit,
    onSave: (Schedule) -> Unit
) {
    var title by remember { mutableStateOf("Focus Mode") }
    var startHour by remember { mutableStateOf(22) }
    var startMinute by remember { mutableStateOf(0) }
    var endHour by remember { mutableStateOf(7) }
    var endMinute by remember { mutableStateOf(0) }
    var repeatDays by remember { mutableStateOf("ALL") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.surface,
        title = {
            Text("Add Schedule", style = AppTheme.typography.titleMedium, color = AppTheme.colors.textPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Schedule Name:", style = AppTheme.typography.labelSmall, color = AppTheme.colors.textSecondary)
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
                        value = title,
                        onValueChange = { title = it },
                        textStyle = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.textPrimary),
                        cursorBrush = SolidColor(AppTheme.colors.primary),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text("Active Hours: 10:00 PM – 7:00 AM", style = AppTheme.typography.bodySmall, color = AppTheme.colors.textSecondary)
                Text("Exceptions: OTP, Financial, Calls", style = AppTheme.typography.bodySmall, color = AppTheme.colors.textSecondary)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        Schedule(
                            title = title,
                            startHour = startHour,
                            startMinute = startMinute,
                            endHour = endHour,
                            endMinute = endMinute,
                            repeatDays = repeatDays,
                            action = "BLOCK_ALL",
                            isEnabled = true,
                            exceptions = "OTP,Financial,Calls",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primary)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppTheme.colors.textSecondary)
            }
        }
    )
}
