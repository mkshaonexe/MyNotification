package com.my.notificationai.core.domain.engine

import com.my.notificationai.core.database.entities.Schedule
import java.util.Calendar

class ScheduleEngine {

    fun isScheduleActive(schedule: Schedule, currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        if (!schedule.isEnabled) return false

        val calendar = Calendar.getInstance().apply { timeInMillis = currentTimeMs }
        val currentDay = getDayCode(calendar.get(Calendar.DAY_OF_WEEK))

        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentTotalMinutes = currentHour * 60 + currentMinute

        val startTotalMinutes = schedule.startHour * 60 + schedule.startMinute
        val endTotalMinutes = schedule.endHour * 60 + schedule.endMinute

        return if (startTotalMinutes <= endTotalMinutes) {
            // Same day range (e.g. 09:00 to 17:00)
            if (schedule.repeatDays != "ALL" && !schedule.repeatDays.contains(currentDay, ignoreCase = true)) {
                false
            } else {
                currentTotalMinutes in startTotalMinutes..endTotalMinutes
            }
        } else {
            // Overnight range (e.g. 22:00 to 07:00)
            if (currentTotalMinutes >= startTotalMinutes) {
                // First portion of overnight (e.g. 22:00 to 23:59) -> started today
                if (schedule.repeatDays != "ALL" && !schedule.repeatDays.contains(currentDay, ignoreCase = true)) {
                    false
                } else {
                    true
                }
            } else if (currentTotalMinutes <= endTotalMinutes) {
                // Second portion of overnight (e.g. 00:00 to 07:00) -> started yesterday
                val prevCal = Calendar.getInstance().apply {
                    timeInMillis = currentTimeMs
                    add(Calendar.DAY_OF_YEAR, -1)
                }
                val prevDay = getDayCode(prevCal.get(Calendar.DAY_OF_WEEK))
                if (schedule.repeatDays != "ALL" && !schedule.repeatDays.contains(prevDay, ignoreCase = true)) {
                    false
                } else {
                    true
                }
            } else {
                // Outside the overnight window
                false
            }
        }
    }

    private fun getDayCode(dayOfWeek: Int): String = when (dayOfWeek) {
        Calendar.MONDAY -> "MON"
        Calendar.TUESDAY -> "TUE"
        Calendar.WEDNESDAY -> "WED"
        Calendar.THURSDAY -> "THU"
        Calendar.FRIDAY -> "FRI"
        Calendar.SATURDAY -> "SAT"
        Calendar.SUNDAY -> "SUN"
        else -> ""
    }
}
