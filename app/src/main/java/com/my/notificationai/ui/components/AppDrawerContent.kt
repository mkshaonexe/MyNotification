package com.my.notificationai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DrawerItem(
    val icon: ImageVector,
    val label: String,
    val badge: String? = null,
    val route: String
)

@Composable
fun AppDrawerContent(
    currentRoute: String?,
    blockedCount: Int,
    vaultCount: Int,
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val drawerItems = listOf(
        DrawerItem(Icons.Default.Home, "Dashboard", null, "dashboard"),
        DrawerItem(Icons.Default.Apps, "App Control List", null, "app_list"),
        DrawerItem(
            Icons.Default.Inbox,
            "Secure Vault Inbox",
            if (vaultCount > 0) vaultCount.toString() else null,
            "vault"
        ),
        DrawerItem(Icons.Default.Settings, "Settings", null, "settings"),
        DrawerItem(Icons.Default.Person, "Profile", null, "profile"),
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(Color.White)
    ) {
        // --- Drawer Header ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // App logo avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Column {
                    Text(
                        text = "MyNotification",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Text(
                        text = "$blockedCount blocked total",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- Drawer Navigation Items ---
        drawerItems.forEach { item ->
            val isSelected = currentRoute == item.route
            DrawerNavItem(
                item = item,
                isSelected = isSelected,
                onClick = {
                    onNavigate(item.route)
                    onCloseDrawer()
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- Footer ---
        HorizontalDivider(color = Color(0xFFF3F4F6))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Privacy-First • On-Device Only",
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DrawerNavItem(
    item: DrawerItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) Color(0xFFEEF2FF) else Color.Transparent
    val contentColor = if (isSelected) Color(0xFF6366F1) else Color(0xFF4B5563)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = item.label,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 15.sp,
            color = contentColor,
            modifier = Modifier.weight(1f)
        )
        // Badge (e.g. vault unread count)
        item.badge?.let { badge ->
            Box(
                modifier = Modifier
                    .background(Color(0xFF6366F1), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badge,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
