package com.mohdrozaimi.jarvis

import com.mohdrozaimi.jarvis.calendar.Meeting
import com.mohdrozaimi.jarvis.calendar.filterToday
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class MeetingFilterTest {
    private val now = LocalDateTime.of(2026, 11, 20, 9, 0)
    private fun m(title: String, h: Int, durH: Long = 1, allDay: Boolean = false, dayOffset: Long = 0): Meeting {
        val start = now.toLocalDate().plusDays(dayOffset).atTime(if (allDay) 0 else h, 0)
        return Meeting(title, start, if (allDay) start.plusDays(1) else start.plusHours(durH), allDay, "Kerja")
    }

    @Test fun dropsFinishedAndSorts() {
        val out = filterToday(listOf(m("Petang", 15), m("Awal", 7), m("Pagi", 10), m("Sedang", 8, durH = 2)), now)
        assertEquals(listOf("Sedang", "Pagi", "Petang"), out.map { it.title })
    }

    @Test fun allDayOnTopAndOnlyToday() {
        val out = filterToday(listOf(m("Pagi", 10), m("Cuti", 0, allDay = true), m("Semalam", 0, allDay = true, dayOffset = -1)), now)
        assertEquals(listOf("Cuti", "Pagi"), out.map { it.title })
    }

    @Test fun emptyStaysEmpty() = assertTrue(filterToday(emptyList(), now).isEmpty())
}
