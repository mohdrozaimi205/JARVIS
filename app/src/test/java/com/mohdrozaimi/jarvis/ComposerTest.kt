package com.mohdrozaimi.jarvis

import com.mohdrozaimi.jarvis.air.AirQuality
import com.mohdrozaimi.jarvis.briefing.BriefingInput
import com.mohdrozaimi.jarvis.briefing.compose
import com.mohdrozaimi.jarvis.briefing.safe
import com.mohdrozaimi.jarvis.calendar.Meeting
import com.mohdrozaimi.jarvis.health.HealthSnapshot
import com.mohdrozaimi.jarvis.news.Headline
import com.mohdrozaimi.jarvis.weather.Forecast
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ComposerTest {
    private val date = LocalDate.of(2026, 11, 20)
    private val full = BriefingInput(
        date = date,
        weather = Forecast("Petaling", date, "a", "b", "c", "Ribut petir", 24, 33),
        air = AirQuality(57, 14.0, "Sederhana"),
        meetings = listOf(Meeting("Mesyuarat stesen", LocalDateTime.of(2026, 11, 20, 10, 0), LocalDateTime.of(2026, 11, 20, 11, 0), false, "Kerja")),
        health = HealthSnapshot(8200, 6.5, 62),
        nutrition = "Kurangkan gula.",
        news = listOf(Headline("Berita satu", "Malaysia")),
    )

    @Test fun greetingIsEnglish() = assertEquals("Good morning, sir.", compose(full).greetingEn)

    @Test fun orderFollowsSpec() {
        val text = compose(full).bodyMs.joinToString(" | ")
        val order = listOf("Hari ini", "Cuaca di Petaling", "(anggaran) 57", "Pukul 10:00", "8200 langkah", "Cadangan pemakanan", "Berita satu")
        val idx = order.map { text.indexOf(it) }
        assertTrue("hilang: $text", idx.none { it < 0 })
        assertEquals(idx.sorted(), idx)
    }

    @Test fun missingSourcesSaySo() {
        val b = compose(BriefingInput(date, null, null, null, null, null, null))
        val t = b.bodyMs.joinToString(" ")
        assertTrue(t.contains("Maklumat cuaca tidak tersedia"))
        assertTrue(t.contains("IPU tidak tersedia"))
        assertTrue(t.contains("Kalendar tidak tersedia"))
        assertTrue(t.contains("Data kesihatan tidak tersedia"))
        assertTrue(t.contains("Berita tidak tersedia"))
        assertEquals(6, b.cards.size)
    }

    @Test fun noMeetingsIsSpoken() {
        val t = compose(full.copy(meetings = emptyList())).bodyMs.joinToString(" ")
        assertTrue(t.contains("Tiada mesyuarat hari ini"))
    }

    @Test fun safeTurnsErrorsIntoNull() = runBlocking {
        assertNull(safe<Int> { error("rosak") })
        assertEquals(1, safe { 1 })
    }
}
