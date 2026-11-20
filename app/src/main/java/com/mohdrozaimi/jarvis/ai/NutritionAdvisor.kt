package com.mohdrozaimi.jarvis.ai

import com.mohdrozaimi.jarvis.health.HealthSnapshot
import java.util.Locale

fun buildNutritionPrompt(h: HealthSnapshot): String {
    val facts = buildList {
        h.stepsYesterday?.let { add("langkah semalam: $it") }
        h.sleepHours?.let { add("tidur malam tadi: ${String.format(Locale.US, "%.1f", it)} jam") }
        h.restingHr?.let { add("nadi rehat: $it bpm") }
    }
    val data = if (facts.isEmpty()) "tiada data kesihatan hari ini" else facts.joinToString(", ")
    return "Anda ialah JARVIS, pembantu peribadi. Data pengguna: $data. " +
        "Beri cadangan pemakanan untuk hari ini dalam Bahasa Melayu, maksimum 3 ayat pendek, " +
        "guna contoh makanan Malaysia. Ini bukan nasihat perubatan: jangan beri diagnosis atau dos ubat."
}
