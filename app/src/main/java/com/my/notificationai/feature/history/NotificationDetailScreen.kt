package com.my.notificationai.feature.history

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.my.notificationai.core.database.entities.NotificationEvent
import com.my.notificationai.core.designsystem.components.AppTopBar
import com.my.notificationai.core.designsystem.components.StatusBadge
import com.my.notificationai.core.designsystem.theme.AppTheme
import com.my.notificationai.data.AppRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationDetailScreen(
    eventId: Long,
    repository: AppRepository,
    innerPadding: PaddingValues,
    onBackClick: () -> Unit
) {
    val event by repository.getEventByIdFlow(eventId).collectAsState(initial = null)
    val updates by repository.getUpdatesForEvent(eventId).collectAsState(initial = emptyList())

    val sdf = remember { SimpleDateFormat("MMM d, yyyy · h:mm:ss a", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(innerPadding)
    ) {
        AppTopBar(
            title = "Notification Details",
            canGoBack = true,
            onBackClick = onBackClick
        )

        if (event == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Notification not found",
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textTertiary
                )
            }
        } else {
            val ev = event!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card with App Label & Status
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppTheme.colors.surface)
                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ev.appLabel,
                                style = AppTheme.typography.titleMedium,
                                color = AppTheme.colors.textPrimary
                            )

                            if (ev.wasBlocked) {
                                StatusBadge(
                                    text = "Blocked",
                                    textColor = AppTheme.colors.error,
                                    bgColor = AppTheme.colors.errorContainer
                                )
                            } else {
                                StatusBadge(
                                    text = "Allowed",
                                    textColor = AppTheme.colors.success,
                                    bgColor = AppTheme.colors.successContainer
                                )
                            }
                        }

                        Text(
                            text = ev.packageName,
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.textTertiary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Content Card
                DetailSectionCard(title = "Notification Content") {
                    DetailItem(label = "Title", value = ev.latestTitle.ifBlank { "(No title)" })
                    DetailItem(label = "Text", value = ev.latestText.ifBlank { "(No text)" })
                    if (!ev.bigText.isNullOrBlank() && ev.bigText != ev.latestText) {
                        DetailItem(label = "Big Text", value = ev.bigText!!)
                    }
                    if (!ev.subText.isNullOrBlank()) {
                        DetailItem(label = "Subtext", value = ev.subText!!)
                    }
                    if (ev.isOtp && !ev.otpCode.isNullOrBlank()) {
                        DetailItem(label = "Detected OTP Code", value = ev.otpCode!!)
                    }
                }

                // Rule Attribution Card
                DetailSectionCard(title = "Rule Attribution & Decision") {
                    DetailItem(
                        label = "Decision",
                        value = if (ev.wasBlocked) "Blocked from system tray" else "Allowed to display in system tray"
                    )
                    DetailItem(
                        label = "Responsible Rule / Reason",
                        value = ev.blockReason ?: "Default Policy"
                    )
                    if (!ev.matchingRuleName.isNullOrBlank()) {
                        DetailItem(label = "Rule Name", value = ev.matchingRuleName!!)
                    }
                }

                // Lifecycle Card
                DetailSectionCard(title = "Lifecycle & Deduplication") {
                    DetailItem(label = "First Seen", value = sdf.format(Date(ev.firstSeenAt)))
                    DetailItem(label = "Last Updated", value = sdf.format(Date(ev.lastUpdatedAt)))
                    if (ev.removedAt != null) {
                        DetailItem(label = "Removed At", value = sdf.format(Date(ev.removedAt!!)))
                        val seconds = ev.durationMs / 1000
                        DetailItem(label = "Active Duration", value = "${seconds}s (${ev.durationMs} ms)")
                    }
                    DetailItem(label = "Update Snapshots Count", value = "${ev.updateCount} updates")
                    if (ev.isProgress) {
                        DetailItem(label = "Progress", value = "${ev.progress} / ${ev.maxProgress}")
                    }
                }

                // Technical Metadata Card
                DetailSectionCard(title = "Technical Metadata") {
                    DetailItem(label = "Channel ID", value = ev.channelId.ifBlank { "None" })
                    if (!ev.category.isNullOrBlank()) {
                        DetailItem(label = "Category", value = ev.category!!)
                    }
                    DetailItem(label = "Ongoing Event", value = ev.isOngoing.toString())
                    DetailItem(label = "Clearable", value = ev.isClearable.toString())
                    DetailItem(label = "Notification Key", value = ev.notificationKey)
                }

                // Updates History if any
                if (updates.isNotEmpty()) {
                    DetailSectionCard(title = "Update History Snapshots (${updates.size})") {
                        updates.forEachIndexed { idx, update ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = "#${idx + 1} · ${SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date(update.timestamp))}",
                                    style = AppTheme.typography.labelSmall,
                                    color = AppTheme.colors.primary
                                )
                                Text(
                                    text = "${update.title} - ${update.text}",
                                    style = AppTheme.typography.bodySmall,
                                    color = AppTheme.colors.textSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
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
                text = title,
                style = AppTheme.typography.titleSmall,
                color = AppTheme.colors.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = AppTheme.typography.labelSmall,
            color = AppTheme.colors.textTertiary
        )
        Text(
            text = value,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.textPrimary
        )
    }
}
