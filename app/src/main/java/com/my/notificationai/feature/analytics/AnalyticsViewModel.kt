package com.my.notificationai.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.notificationai.core.database.dao.AppNotificationCount
import com.my.notificationai.core.designsystem.components.BarChartItem
import com.my.notificationai.data.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

enum class TimeRange(val label: String, val days: Int) {
    TODAY("Today", 1),
    LAST_7_DAYS("Last 7 days", 7),
    LAST_30_DAYS("Last 30 days", 30)
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _selectedTimeRange = MutableStateFlow(TimeRange.LAST_7_DAYS)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange

    private val timeWindow = _selectedTimeRange.map { range ->
        val now = System.currentTimeMillis()
        val start = if (range == TimeRange.TODAY) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        } else {
            now - (range.days * 24L * 60L * 60L * 1000L)
        }
        start to now
    }

    val totalCount: StateFlow<Int> = timeWindow.flatMapLatest { (start, end) ->
        repository.getEventsCountBetween(start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val blockedCount: StateFlow<Int> = timeWindow.flatMapLatest { (start, end) ->
        repository.getBlockedEventsCountBetween(start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allowedCount: StateFlow<Int> = timeWindow.flatMapLatest { (start, end) ->
        repository.getAllowedEventsCountBetween(start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val importantCount: StateFlow<Int> = timeWindow.flatMapLatest { (start, end) ->
        repository.getImportantEventsCountBetween(start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val topApps: StateFlow<List<AppNotificationCount>> = timeWindow.flatMapLatest { (start, end) ->
        repository.getTopAppsBetween(start, end, limit = 5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 7-day distribution bar chart
    val barChartData: StateFlow<List<BarChartItem>> = repository.allEvents.map { events ->
        val calendar = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dayBuckets = mutableMapOf<String, Int>()

        // Generate past 7 days labels
        val daysList = (6 downTo 0).map { offset ->
            val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }
            val dayName = dayFormat.format(c.time)
            dayBuckets[dayName] = 0
            dayName
        }

        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24L * 60L * 60L * 1000L)
        events.filter { it.lastUpdatedAt >= sevenDaysAgo }.forEach { event ->
            calendar.timeInMillis = event.lastUpdatedAt
            val dayName = dayFormat.format(calendar.time)
            if (dayBuckets.containsKey(dayName)) {
                dayBuckets[dayName] = (dayBuckets[dayName] ?: 0) + 1
            }
        }

        daysList.map { day ->
            BarChartItem(label = day, value = dayBuckets[day] ?: 0)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
    }
}
