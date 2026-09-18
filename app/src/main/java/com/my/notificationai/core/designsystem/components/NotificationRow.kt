package com.my.notificationai.core.designsystem.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.my.notificationai.core.database.entities.NotificationEvent
import com.my.notificationai.core.designsystem.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationRow(
    event: NotificationEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appIcon = remember(event.packageName) {
        try {
            val pm = context.packageManager
            pm.getApplicationIcon(event.packageName)
        } catch (e: Exception) {
            null
        }
    }

    val timeString = remember(event.lastUpdatedAt) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(event.lastUpdatedAt))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppTheme.colors.surface)
            .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppTheme.colors.surfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                if (appIcon != null) {
                    Image(
                        bitmap = appIcon.toBitmap(96, 96).asImageBitmap(),
                        contentDescription = event.appLabel,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = AppTheme.colors.textTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (event.latestTitle.isNotBlank()) event.latestTitle else event.appLabel,
                        style = AppTheme.typography.titleSmall,
                        color = AppTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = timeString,
                        style = AppTheme.typography.labelSmall,
                        color = AppTheme.colors.textTertiary
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = event.latestText.ifBlank { event.initialText },
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (event.wasBlocked) {
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

                    if (event.isOtp) {
                        Spacer(modifier = Modifier.width(6.dp))
                        StatusBadge(
                            text = "OTP",
                            textColor = AppTheme.colors.security,
                            bgColor = AppTheme.colors.securityContainer
                        )
                    } else if (event.isFinancial) {
                        Spacer(modifier = Modifier.width(6.dp))
                        StatusBadge(
                            text = "Financial",
                            textColor = AppTheme.colors.security,
                            bgColor = AppTheme.colors.securityContainer
                        )
                    }

                    if (event.updateCount > 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        StatusBadge(
                            text = "${event.updateCount} updates",
                            textColor = AppTheme.colors.textSecondary,
                            bgColor = AppTheme.colors.surfaceElevated
                        )
                    }
                }
            }
        }
    }
}
