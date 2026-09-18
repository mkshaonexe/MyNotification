package com.my.notificationai.feature.rules

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.my.notificationai.core.designsystem.components.AppSearchBar
import com.my.notificationai.core.designsystem.components.AppTopBar
import com.my.notificationai.core.designsystem.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledAppInfo(
    val packageName: String,
    val appLabel: String
)

@Composable
fun AppPickerScreen(
    viewModel: RulesViewModel,
    innerPadding: PaddingValues,
    title: String = "Block Selected Apps",
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val blockedApps by viewModel.blockedApps.collectAsState()
    val blockedPkgSet = remember(blockedApps) {
        blockedApps.filter { it.isBlocked }.map { it.packageName }.toSet()
    }

    var installedApps by remember { mutableStateOf<List<InstalledAppInfo>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            val pm = context.packageManager
            val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)
            val list = packages.mapNotNull { pkg ->
                val appInfo = pkg.applicationInfo ?: return@mapNotNull null
                if (pkg.packageName == context.packageName) return@mapNotNull null
                val label = pm.getApplicationLabel(appInfo).toString()
                InstalledAppInfo(packageName = pkg.packageName, appLabel = label)
            }.sortedBy { it.appLabel.lowercase() }
            installedApps = list
        }
    }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter {
            it.appLabel.contains(searchQuery, ignoreCase = true) ||
            it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(innerPadding)
    ) {
        AppTopBar(
            title = title,
            subtitle = "${blockedPkgSet.size} apps blocked",
            canGoBack = true,
            onBackClick = onBackClick
        )

        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
            AppSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search installed apps..."
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredApps, key = { it.packageName }) { app ->
                val isBlocked = blockedPkgSet.contains(app.packageName)
                val appIcon = remember(app.packageName) {
                    try {
                        context.packageManager.getApplicationIcon(app.packageName)
                    } catch (e: Exception) {
                        null
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppTheme.colors.surface)
                        .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.toggleAppBlock(app.packageName, app.appLabel, !isBlocked)
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppTheme.colors.surfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            if (appIcon != null) {
                                Image(
                                    bitmap = appIcon.toBitmap(72, 72).asImageBitmap(),
                                    contentDescription = app.appLabel,
                                    modifier = Modifier.size(28.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = AppTheme.colors.textTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

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

                        Checkbox(
                            checked = isBlocked,
                            onCheckedChange = { checked ->
                                viewModel.toggleAppBlock(app.packageName, app.appLabel, checked)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AppTheme.colors.error,
                                checkmarkColor = androidx.compose.ui.graphics.Color.White,
                                uncheckedColor = AppTheme.colors.border
                            )
                        )
                    }
                }
            }
        }
    }
}
