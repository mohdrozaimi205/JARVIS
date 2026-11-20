package com.mohdrozaimi.jarvis.calendar

import java.time.LocalDateTime

data class Meeting(
    val title: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val allDay: Boolean,
    val calendar: String,
)

/**
 * Mesyuarat yang masih relevan hari ini:
 * - acara sepanjang hari (tarikh = hari ini) di atas,
 * - acara biasa yang belum tamat, disusun ikut masa mula.
 */
fun filterToday(events: List<Meeting>, now: LocalDateTime): List<Meeting> {
    val today = now.toLocalDate()
    val allDay = events.filter { it.allDay && it.start.toLocalDate() == today }
    val timed = events
        .filter { !it.allDay && it.end.isAfter(now) && !it.start.toLocalDate().isAfter(today) }
        .sortedBy { it.start }
    return allDay.sortedBy { it.title } + timed
}
