package com.my.notificationai.feature.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.notificationai.core.database.entities.BlockedApp
import com.my.notificationai.core.database.entities.BlockingRule
import com.my.notificationai.core.database.entities.NotificationCategory
import com.my.notificationai.core.database.entities.RuleCondition
import com.my.notificationai.core.database.entities.Schedule
import com.my.notificationai.core.database.entities.WhitelistedApp
import com.my.notificationai.core.database.entities.WhitelistedKeyword
import com.my.notificationai.data.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RulesViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    val rules: StateFlow<List<BlockingRule>> = repository.allRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val whitelistedApps: StateFlow<List<WhitelistedApp>> = repository.allWhitelistedApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val whitelistedKeywords: StateFlow<List<WhitelistedKeyword>> = repository.allWhitelistedKeywords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedApps: StateFlow<List<BlockedApp>> = repository.allBlockedApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val schedules: StateFlow<List<Schedule>> = repository.allSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<NotificationCategory>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleRule(ruleId: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setRuleEnabled(ruleId, isEnabled)
        }
    }

    fun toggleRuleByType(ruleType: String, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setRuleEnabledByType(ruleType, isEnabled)
        }
    }

    fun addWhitelistedKeyword(keyword: String, category: String = "OTP") {
        if (keyword.isBlank()) return
        viewModelScope.launch {
            repository.insertWhitelistedKeyword(
                WhitelistedKeyword(keyword = keyword.trim().lowercase(), category = category)
            )
        }
    }

    fun removeWhitelistedKeyword(keyword: String) {
        viewModelScope.launch {
            repository.deleteWhitelistedKeyword(keyword)
        }
    }

    fun addWhitelistedApp(packageName: String, appLabel: String) {
        viewModelScope.launch {
            repository.insertWhitelistedApp(
                WhitelistedApp(packageName = packageName, appLabel = appLabel)
            )
        }
    }

    fun removeWhitelistedApp(packageName: String) {
        viewModelScope.launch {
            repository.deleteWhitelistedApp(packageName)
        }
    }

    fun toggleAppBlock(packageName: String, appLabel: String, isBlocked: Boolean) {
        viewModelScope.launch {
            val existing = repository.getBlockedApp(packageName)
            if (existing == null) {
                repository.insertBlockedApp(
                    BlockedApp(packageName = packageName, appLabel = appLabel, isBlocked = isBlocked)
                )
            } else {
                repository.updateBlockedApp(
                    existing.copy(isBlocked = isBlocked, updatedAt = System.currentTimeMillis())
                )
            }
        }
    }

    fun toggleSchedule(scheduleId: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setScheduleEnabled(scheduleId, isEnabled)
        }
    }

    fun saveSchedule(schedule: Schedule) {
        viewModelScope.launch {
            if (schedule.id == 0L) {
                repository.insertSchedule(schedule)
            } else {
                repository.updateSchedule(schedule)
            }
        }
    }

    fun deleteSchedule(scheduleId: Long) {
        viewModelScope.launch {
            repository.deleteSchedule(scheduleId)
        }
    }

    fun addCustomRule(
        name: String,
        description: String,
        action: String,
        conditionType: String,
        operator: String,
        value: String
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val ruleId = repository.insertRule(
                BlockingRule(
                    name = name,
                    description = description,
                    ruleType = "CUSTOM",
                    action = action,
                    isEnabled = true,
                    priority = 25,
                    createdAt = now,
                    updatedAt = now
                )
            )
            repository.insertCondition(
                RuleCondition(
                    ruleId = ruleId,
                    conditionType = conditionType,
                    operator = operator,
                    value = value
                )
            )
        }
    }
}
