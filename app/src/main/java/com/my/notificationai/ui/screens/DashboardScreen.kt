package com.my.notificationai.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.my.notificationai.ui.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    innerPadding: PaddingValues,
    onNavigateToApps: () -> Unit,
    onNavigateToVault: () -> Unit
) {
    val context = LocalContext.current
    val isBlockAll by viewModel.isBlockAllEnabled.collectAsState()
    val quickPauseUntil by viewModel.quickPauseUntil.collectAsState()
    val notifications by viewModel.savedNotifications.collectAsState()

    var isServiceRunning by remember { mutableStateOf(false) }

    // Periodically check service health
    LaunchedEffect(Unit) {
        while (true) {
            isServiceRunning = viewModel.isServiceRunning()
            delay(2000)
        }
    }

    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    val isPaused = quickPauseUntil > currentTime

    LaunchedEffect(quickPauseUntil) {
        while (quickPauseUntil > System.currentTimeMillis()) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
        currentTime = System.currentTimeMillis()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(Color(0xFFF8F9FA))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Service Status Banner / Card
        ServiceStatusCard(
            isRunning = isServiceRunning,
            onGrantPermission = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        )

        // 2. Stats Block
        StatsCard(
            blockedToday = notifications.size,
            unreadCount = notifications.count { !it.isRead },
            onClickVault = onNavigateToVault
        )

        // 3. Master Block Toggle Card
        MasterBlockCard(
            isBlockAll = isBlockAll && !isPaused,
            isPaused = isPaused,
            onToggle = { viewModel.toggleBlockAll(!isBlockAll) }
        )

        // 4. Quick Pause Card
        QuickPauseCard(
            isPaused = isPaused,
            quickPauseUntil = quickPauseUntil,
            currentTime = currentTime,
            onPause = { mins -> viewModel.enableQuickPause(mins) },
            onCancel = { viewModel.cancelQuickPause() }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Quick Shortcut Panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ShortcutButton(
                text = "Manage Apps",
                icon = Icons.Default.List,
                color = Color(0xFF6366F1),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToApps
            )
            ShortcutButton(
                text = "View Inbox",
                icon = Icons.Default.MailOutline,
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToVault
            )
        }
    }
}

@Composable
fun ServiceStatusCard(
    isRunning: Boolean,
    onGrantPermission: () -> Unit
) {
    val bgColor = if (isRunning) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
    val borderColor = if (isRunning) Color(0xFFA7F3D0) else Color(0xFFFCA5A5)
    val textColor = if (isRunning) Color(0xFF065F46) else Color(0xFF991B1B)
    val statusText = if (isRunning) "Active & Running" else "Permission Required"
    val icon = if (isRunning) Icons.Default.CheckCircle else Icons.Default.Warning

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isRunning) Color(0xFF10B981) else Color(0xFFEF4444),
                modifier = Modifier.size(28.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Interception Service",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = statusText,
                    fontSize = 16.sp,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!isRunning) {
                Button(
                    onClick = onGrantPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Grant", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    blockedToday: Int,
    unreadCount: Int,
    onClickVault: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClickVault),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total Blocked",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = blockedToday.toString(),
                    fontSize = 36.sp,
                    color = Color(0xFF1F2937),
                    fontWeight = FontWeight.ExtraBold
                )
            }

            VerticalDivider(
                modifier = Modifier
                    .height(50.dp)
                    .width(1.dp),
                color = Color(0xFFE5E7EB)
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Unread in Vault",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFF6366F1), CircleShape)
                        )
                    }
                    Text(
                        text = unreadCount.toString(),
                        fontSize = 36.sp,
                        color = if (unreadCount > 0) Color(0xFF6366F1) else Color(0xFF1F2937),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun MasterBlockCard(
    isBlockAll: Boolean,
    isPaused: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isBlockAll) Color(0xFF6366F1) else Color.White
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isBlockAll) Color(0xFF6366F1) else Color(0xFFE5E7EB),
                RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = animatedColor),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Master Toggle",
                    fontSize = 14.sp,
                    color = if (isBlockAll) Color(0xFFC7D2FE) else Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isPaused) "Temporarily Paused" else if (isBlockAll) "Block All Active" else "Block Selected Only",
                    fontSize = 18.sp,
                    color = if (isBlockAll) Color.White else Color(0xFF1F2937),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isPaused) "System is letting all notifications pass."
                    else if (isBlockAll) "All apps are currently intercepted."
                    else "Only selected apps are intercepted.",
                    fontSize = 12.sp,
                    color = if (isBlockAll) Color(0xFFE0E7FF) else Color(0xFF9CA3AF),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Switch(
                checked = isBlockAll,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF4F46E5),
                    uncheckedThumbColor = Color(0xFF9CA3AF),
                    uncheckedTrackColor = Color(0xFFE5E7EB)
                ),
                enabled = !isPaused
            )
        }
    }
}

@Composable
fun QuickPauseCard(
    isPaused: Boolean,
    quickPauseUntil: Long,
    currentTime: Long,
    onPause: (Int) -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Quick Pause Blockers",
                fontSize = 16.sp,
                color = Color(0xFF1F2937),
                fontWeight = FontWeight.Bold
            )

            AnimatedVisibility(visible = isPaused) {
                val remainingMs = quickPauseUntil - currentTime
                val totalSecs = (remainingMs / 1000).coerceAtLeast(0)
                val mins = totalSecs / 60
                val secs = totalSecs % 60

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF3C7), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Paused: ${mins}m ${secs}s left",
                            color = Color(0xFF92400E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = "Cancel",
                        color = Color(0xFFD97706),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable(onClick = onCancel)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PausePresetButton(
                    label = "15 Mins",
                    onClick = { onPause(15) },
                    modifier = Modifier.weight(1f)
                )
                PausePresetButton(
                    label = "1 Hour",
                    onClick = { onPause(60) },
                    modifier = Modifier.weight(1f)
                )
                PausePresetButton(
                    label = "8 Hours",
                    onClick = { onPause(480) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PausePresetButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4B5563)),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ShortcutButton(
    text: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(56.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
