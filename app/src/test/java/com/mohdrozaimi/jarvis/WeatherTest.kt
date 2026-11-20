package com.mohdrozaimi.jarvis

import com.mohdrozaimi.jarvis.weather.DEFAULT_PLACE
import com.mohdrozaimi.jarvis.weather.matchLocation
import com.mohdrozaimi.jarvis.weather.parseForecast
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WeatherTest {
    private val json = """
        [
          {"location":{"location_id":"Ds041","location_name":"Petaling"},"date":"2026-11-20",
           "morning_forecast":"Tiada hujan","afternoon_forecast":"Ribut petir","night_forecast":"Tiada hujan",
           "summary_forecast":"Ribut petir di beberapa tempat","summary_when":"Petang","min_temp":24,"max_temp":33},
          {"location":{"location_id":"Ds041","location_name":"Petaling"},"date":"2026-11-21",
           "morning_forecast":"Tiada hujan","afternoon_forecast":"Hujan","night_forecast":"Tiada hujan",
           "summary_forecast":"Hujan","summary_when":"Petang","min_temp":25,"max_temp":32}
        ]
    """.trimIndent()

    @Test fun parsesForecastFields() {
        val f = parseForecast(json)
        assertEquals(2, f.size)
        assertEquals("Petaling", f[0].place)
        assertEquals(LocalDate.of(2026, 11, 20), f[0].date)
        assertEquals("Ribut petir", f[0].afternoon)
        assertEquals("Ribut petir di beberapa tempat", f[0].summary)
        assertEquals(24, f[0].minC)
        assertEquals(33, f[0].maxC)
    }

    @Test fun parsesWrappedDataObject() {
        assertEquals(2, parseForecast("""{"data":$json}""").size)
    }

    @Test fun badJsonGivesEmpty() {
        assertTrue(parseForecast("oops").isEmpty())
    }

    private val names = listOf("Kuala Lumpur", "Petaling", "Hulu Langat", "Johor Bahru")

    @Test fun matchExactIgnoringCase() = assertEquals("Petaling", matchLocation("petaling", names))

    @Test fun matchContains() = assertEquals("Johor Bahru", matchLocation("Daerah Johor Bahru", names))

    @Test fun unknownFallsBackToKl() {
        assertEquals(DEFAULT_PLACE, matchLocation("Xyz", names))
        assertEquals(DEFAULT_PLACE, matchLocation(null, names))
        assertEquals(DEFAULT_PLACE, matchLocation("  ", names))
    }
}
