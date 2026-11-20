package com.mohdrozaimi.jarvis.briefing

import com.mohdrozaimi.jarvis.air.AirQuality
import com.mohdrozaimi.jarvis.calendar.Meeting
import com.mohdrozaimi.jarvis.health.HealthSnapshot
import com.mohdrozaimi.jarvis.news.Headline
import com.mohdrozaimi.jarvis.weather.Forecast
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class BriefingInput(
    val date: LocalDate,
    val weather: Forecast?,
    val air: AirQuality?,
    /** null = kalendar tak boleh dibaca; kosong = tiada mesyuarat. */
    val meetings: List<Meeting>?,
    val health: HealthSnapshot?,
    val nutrition: String?,
    val news: List<Headline>?,
)

data class Card(val title: String, val value: String, val detail: String)

data class Briefing(
    val greetingEn: String,
    val bodyMs: List<String>,
    val cards: List<Card>,
)

private val MS = Locale("ms", "MY")
private val DATE_FMT = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", MS)
private val TIME_FMT = DateTimeFormatter.ofPattern("HH:mm")

const val GREETING = "Good morning, sir."

fun compose(i: BriefingInput): Briefing {
    val body = mutableListOf<String>()
    val cards = mutableListOf<Card>()

    // 1. Tarikh
    body += "Hari ini ${i.date.format(DATE_FMT)}."

    // 2. Cuaca
    val w = i.weather
    if (w != null) {
        body += "Cuaca di ${w.place}: ${w.summary}. Suhu ${w.minC} hingga ${w.maxC} darjah Celsius."
        cards += Card("Cuaca", "${w.minC}–${w.maxC}°C", "${w.place}\n${w.summary}")
    } else {
        body += "Maklumat cuaca tidak tersedia."
        cards += Card("Cuaca", "—", "Tidak tersedia")
    }

    // 3. IPU (anggaran)
    val a = i.air
    if (a != null) {
        body += "Indeks pencemaran udara (anggaran) ${a.usAqi}, ${a.category}."
        cards += Card("IPU (anggaran)", a.usAqi.toString(), a.category)
    } else {
        body += "Maklumat IPU tidak tersedia."
        cards += Card("IPU (anggaran)", "—", "Tidak tersedia")
    }

    // 4. Mesyuarat
    val m = i.meetings
    when {
        m == null -> {
            body += "Kalendar tidak tersedia."
            cards += Card("Mesyuarat", "—", "Kalendar tidak tersedia")
        }
        m.isEmpty() -> {
            body += "Tiada mesyuarat hari ini."
            cards += Card("Mesyuarat", "0", "Tiada mesyuarat hari ini")
        }
        else -> {
            body += "Anda ada ${m.size} mesyuarat hari ini."
            m.forEach { body += meetingLine(it) }
            cards += Card("Mesyuarat", m.size.toString(), m.joinToString("\n") { meetingShort(it) })
        }
    }

    // 5. Kesihatan + pemakanan
    val h = i.health
    if (h != null && !h.isEmpty) {
        val parts = buildList {
            h.stepsYesterday?.let { add("Semalam anda berjalan $it langkah.") }
            h.sleepHours?.let { add("Anda tidur ${fmt1(it)} jam.") }
            h.restingHr?.let { add("Nadi rehat $it denyutan seminit.") }
        }
        body += parts
        cards += Card(
            "Kesihatan",
            h.stepsYesterday?.let { "$it langkah" } ?: "—",
            listOfNotNull(
                h.sleepHours?.let { "Tidur ${fmt1(it)} j" },
                h.restingHr?.let { "Nadi rehat $it" },
            ).joinToString(" · ").ifEmpty { "—" },
        )
    } else {
        body += "Data kesihatan tidak tersedia."
        cards += Card("Kesihatan", "—", "Tidak tersedia")
    }
    val n = i.nutrition?.trim()
    if (!n.isNullOrEmpty()) {
        body += "Cadangan pemakanan: $n"
        cards += Card("Pemakanan", "", n)
    } else {
        cards += Card("Pemakanan", "", "Masukkan kunci Gemini dalam Tetapan")
    }

    // 6. Berita
    val news = i.news
    if (news.isNullOrEmpty()) {
        body += "Berita tidak tersedia."
        cards += Card("Berita", "", "Tidak tersedia")
    } else {
        body += "Berita utama."
        news.forEach { body += "${it.title}." }
        cards += Card("Berita", "", news.joinToString("\n") { "• ${it.title}" })
    }

    return Briefing(GREETING, body, cards)
}

private fun meetingLine(m: Meeting) =
    if (m.allDay) "Sepanjang hari, ${m.title}." else "Pukul ${m.start.format(TIME_FMT)}, ${m.title}."

private fun meetingShort(m: Meeting) =
    if (m.allDay) "Hari ini · ${m.title}" else "${m.start.format(TIME_FMT)} · ${m.title}"

private fun fmt1(d: Double) = String.format(Locale.US, "%.1f", d)
