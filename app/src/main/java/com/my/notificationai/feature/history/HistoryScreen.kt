package com.my.notificationai.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.my.notificationai.core.database.entities.NotificationEvent
import com.my.notificationai.core.designsystem.components.AppSearchBar
import com.my.notificationai.core.designsystem.components.AppTopBar
import com.my.notificationai.core.designsystem.components.FilterPillChip
import com.my.notificationai.core.designsystem.components.NotificationRow
import com.my.notificationai.core.designsystem.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    innerPadding: PaddingValues,
    onNotificationClick: (Long) -> Unit,
    canGoBack: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    val query by viewModel.searchQuery.collectAsState()
    val filter by viewModel.selectedFilter.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    // Group notifications by date
    val groupedNotifications = remember(notifications) {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)

        notifications.groupBy { event ->
            calendar.timeInMillis = event.lastUpdatedAt
            val eventDay = calendar.get(Calendar.DAY_OF_YEAR)
            val eventYear = calendar.get(Calendar.YEAR)

            if (year == eventYear) {
                when {
                    today == eventDay -> "Today"
                    today - eventDay == 1 -> "Yesterday"
                    else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(event.lastUpdatedAt))
                }
            } else {
                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(event.lastUpdatedAt))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(innerPadding)
    ) {
        AppTopBar(
            title = "Notification History",
            canGoBack = canGoBack,
            onBackClick = onBackClick
        )

        // Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            AppSearchBar(
                query = query,
                onQueryChange = { viewModel.setSearchQuery(it) }
            )
        }

        // Horizontal Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL" to "All", "ALLOWED" to "Allowed", "BLOCKED" to "Blocked", "IMPORTANT" to "Important").forEach { (key, label) ->
                FilterPillChip(
                    text = label,
                    isSelected = filter == key,
                    onClick = { viewModel.setFilter(key) }
                )
            }
        }

        // Notification List
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (query.isNotBlank()) "No notifications match '$query'" else "No notifications recorded yet",
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textTertiary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                groupedNotifications.forEach { (dateHeader, events) ->
                    item(key = "header_$dateHeader") {
                        Text(
                            text = dateHeader,
                            style = AppTheme.typography.labelLarge,
                            color = AppTheme.colors.textSecondary,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }

                    items(events, key = { it.id }) { event ->
                        NotificationRow(
                            event = event,
                            onClick = { onNotificationClick(event.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
