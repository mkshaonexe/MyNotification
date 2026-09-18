package com.my.notificationai.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.notificationai.core.database.entities.NotificationEvent
import com.my.notificationai.data.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter

    val notifications: StateFlow<List<NotificationEvent>> = combine(
        _searchQuery,
        _selectedFilter
    ) { query, filter ->
        query to filter
    }.flatMapLatest { (query, filter) ->
        repository.getFilteredEvents(query, filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun deleteNotification(event: NotificationEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.deleteAllEvents()
        }
    }
}
