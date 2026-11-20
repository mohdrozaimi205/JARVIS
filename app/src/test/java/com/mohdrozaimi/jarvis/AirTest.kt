package com.mohdrozaimi.jarvis

import com.mohdrozaimi.jarvis.air.aqiToIpu
import com.mohdrozaimi.jarvis.air.parseAir
import org.junit.Assert.assertEquals
import org.junit.Test

class AirTest {
    @Test fun boundaries() {
        assertEquals("Baik", aqiToIpu(0))
        assertEquals("Baik", aqiToIpu(50))
        assertEquals("Sederhana", aqiToIpu(51))
        assertEquals("Sederhana", aqiToIpu(100))
        assertEquals("Tidak Sihat", aqiToIpu(101))
        assertEquals("Tidak Sihat", aqiToIpu(200))
        assertEquals("Sangat Tidak Sihat", aqiToIpu(201))
        assertEquals("Sangat Tidak Sihat", aqiToIpu(300))
        assertEquals("Berbahaya", aqiToIpu(301))
    }

    @Test fun parsesCurrent() {
        val a = parseAir("""{"latitude":3.1,"current":{"time":"2026-11-20T08:00","us_aqi":57,"pm2_5":14.2}}""")
        assertEquals(57, a.usAqi)
        assertEquals(14.2, a.pm25, 0.001)
        assertEquals("Sederhana", a.category)
    }

    @Test(expected = Exception::class)
    fun missingAqiThrows() {
        parseAir("""{"current":{"us_aqi":null}}""")
    }
}
