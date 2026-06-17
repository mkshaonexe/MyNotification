package com.my.notificationai.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.my.notificationai.data.SavedNotification
import com.my.notificationai.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    viewModel: MainViewModel,
    innerPadding: PaddingValues
) {
    val context = LocalContext.current
    val notifications by viewModel.savedNotifications.collectAsState()
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Clear Inbox?") },
            text = { Text("Are you sure you want to permanently delete all saved notifications from your vault?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllNotifications()
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(Color(0xFFF8F9FA))
    ) {
        if (notifications.isEmpty()) {
            EmptyVaultState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp)
            ) {
                items(notifications, key = { it.id }) { notification ->
                    NotificationItem(
                        notification = notification,
                        onMarkRead = { viewModel.markNotificationAsRead(notification) },
                        onDelete = { viewModel.deleteNotification(notification) },
                        onCopyText = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Notification Content", "${notification.title}\n${notification.body}")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied content to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyVaultState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color(0xFFECFDF5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Inbox Clean",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Notifications blocked by rules will show up here.",
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NotificationItem(
    notification: SavedNotification,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit,
    onCopyText: () -> Unit
) {
    val formattedTime = remember(notification.receivedAt) {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.format(Date(notification.receivedAt))
    }

    val formattedDate = remember(notification.receivedAt) {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.format(Date(notification.receivedAt))
    }

    var isExpanded by remember { mutableStateOf(false) }

    // Auto mark read on click / expand
    LaunchedEffect(isExpanded) {
        if (isExpanded && !notification.isRead) {
            onMarkRead()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (notification.isRead) Color(0xFFE5E7EB) else Color(0xFF6366F1).copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            )
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // App details and timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dot indicating unread
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF6366F1), CircleShape)
                        )
                    }

                    Text(
                        text = notification.appLabel,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4B5563),
                        fontSize = 13.sp
                    )

                    if (notification.category == "OTP") {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFEF3C7), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "OTP",
                                color = Color(0xFFD97706),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Text(
                    text = "$formattedDate, $formattedTime",
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp
                )
            }

            // Notification Title
            Text(
                text = notification.title,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1F2937),
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Notification Body
            Text(
                text = notification.body,
                color = Color(0xFF4B5563),
                fontSize = 14.sp,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            // If OTP detected, show large OTP code inside card
            if (notification.isOtp && !notification.otpCode.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEEF2F6), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "DETECTION OPT CODE",
                            color = Color(0xFF6366F1),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                        Text(
                            text = notification.otpCode,
                            color = Color(0xFF1F2937),
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            letterSpacing = 4.sp
                        )
                    }
                }
            }

            // Expanded Actions Panel
            if (isExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onCopyText,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF6366F1))
                    ) {
                        Text("Copy Text", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}
