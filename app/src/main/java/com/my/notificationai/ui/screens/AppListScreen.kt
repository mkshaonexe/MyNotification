package com.my.notificationai.ui.screens

import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.my.notificationai.ui.AppItem
import com.my.notificationai.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val appsList by viewModel.appsList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Control List", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8F9FA)
                )
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA))
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search installed apps...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6366F1),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // LazyColumn
            if (appsList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No apps found",
                        color = Color(0xFF6B7280),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(appsList, key = { it.packageName }) { app ->
                        AppListItem(
                            app = app,
                            onToggleBlock = {
                                viewModel.toggleAppBlock(app.packageName, app.appLabel, app.isBlocked)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppListItem(
    app: AppItem,
    onToggleBlock: () -> Unit
) {
    val context = LocalContext.current
    val appIcon: Drawable? = remember(app.packageName) {
        runCatching {
            context.packageManager.getApplicationIcon(app.packageName)
        }.getOrNull()
    }
    val isSystemApp = remember(app.packageName) {
        runCatching {
            val info = context.packageManager.getApplicationInfo(app.packageName, 0)
            (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        }.getOrDefault(false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (app.isBlocked) Color(0xFFEF4444).copy(alpha = 0.3f) else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (app.isBlocked) Color(0xFFFFF5F5) else Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App details
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // App Icon
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0xFFEEF2F6), RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (appIcon != null) {
                        val bitmap = remember(appIcon) {
                            appIcon.toBitmap(96, 96)
                        }
                        Image(
                            painter = BitmapPainter(bitmap.asImageBitmap()),
                            contentDescription = app.appLabel,
                            modifier = Modifier.size(36.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(
                            text = app.appLabel.take(1).uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF6366F1),
                            fontSize = 18.sp
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = app.appLabel,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937),
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                        if (isSystemApp) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFEDE9FE), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "SYS",
                                    color = Color(0xFF7C3AED),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                    Text(
                        text = app.packageName,
                        color = Color(0xFF9CA3AF),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Intercepted Badge
                if (app.notificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEEF2F6), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${app.notificationCount} blocked",
                            color = Color(0xFF4B5563),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                    }
                }

                // Switch Toggle
                Switch(
                    checked = app.isBlocked,
                    onCheckedChange = { onToggleBlock() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFEF4444),
                        uncheckedThumbColor = Color(0xFF9CA3AF),
                        uncheckedTrackColor = Color(0xFFE5E7EB)
                    )
                )
            }
        }
    }
}
