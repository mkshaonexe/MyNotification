package com.my.notificationai.core.domain.engine

import com.my.notificationai.core.database.entities.Schedule
import java.util.Calendar

class ScheduleEngine {

    fun isScheduleActive(schedule: Schedule, currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        if (!schedule.isEnabled) return false

        val calendar = Calendar.getInstance().apply { timeInMillis = currentTimeMs }
        val currentDay = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            Calendar.SATURDAY -> "SAT"
            Calendar.SUNDAY -> "SUN"
            else -> ""
        }

        // Check day match
        if (schedule.repeatDays != "ALL" && !schedule.repeatDays.contains(currentDay, ignoreCase = true)) {
            return false
        }

        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentTotalMinutes = currentHour * 60 + currentMinute

        val startTotalMinutes = schedule.startHour * 60 + schedule.startMinute
        val endTotalMinutes = schedule.endHour * 60 + schedule.endMinute

        return if (startTotalMinutes <= endTotalMinutes) {
            // Same day range (e.g. 09:00 to 17:00)
            currentTotalMinutes in startTotalMinutes..endTotalMinutes
        } else {
            // Overnight range (e.g. 22:00 to 07:00)
            currentTotalMinutes >= startTotalMinutes || currentTotalMinutes <= endTotalMinutes
        }
    }
}
