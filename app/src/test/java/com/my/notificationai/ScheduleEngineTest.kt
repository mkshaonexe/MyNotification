package com.my.notificationai

import com.my.notificationai.core.database.entities.Schedule
import com.my.notificationai.core.domain.engine.ScheduleEngine
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class ScheduleEngineTest {

    private lateinit var scheduleEngine: ScheduleEngine

    @Before
    fun setup() {
        scheduleEngine = ScheduleEngine()
    }

    @Test
    fun `overnight schedule active at 23-00`() {
        val schedule = Schedule(
            title = "Sleep Focus",
            startHour = 22,
            startMinute = 0,
            endHour = 7,
            endMinute = 0,
            repeatDays = "ALL",
            isEnabled = true
        )

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 30)
        }

        assertTrue(scheduleEngine.isScheduleActive(schedule, cal.timeInMillis))
    }

    @Test
    fun `overnight schedule active at 05-00 next day`() {
        val schedule = Schedule(
            title = "Sleep Focus",
            startHour = 22,
            startMinute = 0,
            endHour = 7,
            endMinute = 0,
            repeatDays = "ALL",
            isEnabled = true
        )

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 5)
            set(Calendar.MINUTE, 15)
        }

        assertTrue(scheduleEngine.isScheduleActive(schedule, cal.timeInMillis))
    }

    @Test
    fun `overnight schedule inactive at 14-00 afternoon`() {
        val schedule = Schedule(
            title = "Sleep Focus",
            startHour = 22,
            startMinute = 0,
            endHour = 7,
            endMinute = 0,
            repeatDays = "ALL",
            isEnabled = true
        )

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 0)
        }

        assertFalse(scheduleEngine.isScheduleActive(schedule, cal.timeInMillis))
    }

    @Test
    fun `disabled schedule is never active`() {
        val schedule = Schedule(
            title = "Sleep Focus",
            startHour = 0,
            startMinute = 0,
            endHour = 23,
            endMinute = 59,
            repeatDays = "ALL",
            isEnabled = false
        )

        assertFalse(scheduleEngine.isScheduleActive(schedule, System.currentTimeMillis()))
    }
}
