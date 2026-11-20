package com.mohdrozaimi.jarvis

import com.mohdrozaimi.jarvis.alarm.nextTrigger
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class AlarmTimeTest {
    private val base = LocalDateTime.of(2026, 11, 20, 6, 0)

    @Test fun beforeTimeIsToday() = assertEquals(LocalDateTime.of(2026, 11, 20, 6, 30), nextTrigger(base, 6, 30))
    @Test fun afterTimeIsTomorrow() = assertEquals(LocalDateTime.of(2026, 11, 21, 5, 30), nextTrigger(base, 5, 30))
    @Test fun exactTimeIsTomorrow() = assertEquals(LocalDateTime.of(2026, 11, 21, 6, 0), nextTrigger(base, 6, 0))
}
