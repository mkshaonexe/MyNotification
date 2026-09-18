package com.my.notificationai.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.my.notificationai.core.designsystem.theme.AppTheme

@Composable
fun HeroRingCard(
    isBlockerActive: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ringColor = if (isBlockerActive) AppTheme.colors.primary else AppTheme.colors.border
    val ringTrackColor = if (isBlockerActive) AppTheme.colors.primary.copy(alpha = 0.15f) else AppTheme.colors.borderSubtle
    val buttonBg = if (isBlockerActive) AppTheme.colors.primary else AppTheme.colors.surfaceElevated
    val buttonTextColor = if (isBlockerActive) AppTheme.colors.textPrimary else AppTheme.colors.textSecondary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppTheme.colors.surface)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(20.dp))
            .padding(vertical = 28.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circular Glowing Ring with Bell Icon
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(96.dp)) {
                    // Outer track
                    drawCircle(
                        color = ringTrackColor,
                        style = Stroke(width = 6.dp.toPx())
                    )
                    // Active arc
                    if (isBlockerActive) {
                        drawArc(
                            color = ringColor,
                            startAngle = -90f,
                            sweepAngle = 300f,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            if (isBlockerActive) AppTheme.colors.primary.copy(alpha = 0.12f)
                            else AppTheme.colors.surfaceElevated
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isBlockerActive) Icons.Default.NotificationsOff else Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (isBlockerActive) AppTheme.colors.primary else AppTheme.colors.textTertiary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Notification Blocker",
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary
            )

            Text(
                text = if (isBlockerActive) "Active" else "Inactive",
                style = AppTheme.typography.bodySmall,
                color = if (isBlockerActive) AppTheme.colors.success else AppTheme.colors.textTertiary,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pill action button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(buttonBg)
                    .border(
                        1.dp,
                        if (isBlockerActive) AppTheme.colors.primary else AppTheme.colors.border,
                        RoundedCornerShape(999.dp)
                    )
                    .clickable { onToggle() }
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isBlockerActive) "Tap to disable" else "Tap to enable",
                    style = AppTheme.typography.labelLarge,
                    color = buttonTextColor
                )
            }
        }
    }
}
