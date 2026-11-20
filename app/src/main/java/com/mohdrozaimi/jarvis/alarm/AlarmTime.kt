package com.mohdrozaimi.jarvis.alarm

import java.time.LocalDateTime

/** Masa kejut seterusnya: hari ini jika belum sampai, jika tidak esok. */
fun nextTrigger(now: LocalDateTime, hour: Int, minute: Int): LocalDateTime {
    val t = now.toLocalDate().atTime(hour, minute)
    return if (now.isBefore(t)) t else t.plusDays(1)
}
