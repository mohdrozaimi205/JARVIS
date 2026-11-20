package com.mohdrozaimi.jarvis

import com.mohdrozaimi.jarvis.route.DistanceQuery
import com.mohdrozaimi.jarvis.route.RouteSource
import com.mohdrozaimi.jarvis.route.parseDistanceCommand
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DistanceTest {
    @Test fun malayWithDari() = assertEquals(DistanceQuery("Ipoh", "KL"), parseDistanceCommand("jarak dari Ipoh ke KL"))
    @Test fun malayWithoutDari() = assertEquals(DistanceQuery("Ipoh", "Kuala Lumpur"), parseDistanceCommand("Jarak Ipoh ke Kuala Lumpur?"))
    @Test fun english() = assertEquals(DistanceQuery("Ipoh", "KL"), parseDistanceCommand("distance from Ipoh to KL"))
    @Test fun berapaKm() = assertEquals(DistanceQuery("Ipoh", "Johor Bahru"), parseDistanceCommand("berapa km Ipoh ke Johor Bahru"))
    @Test fun berapaJauh() = assertEquals(DistanceQuery("Shah Alam", "Melaka"), parseDistanceCommand("berapa jauh dari Shah Alam ke Melaka"))
    @Test fun notADistance() = assertNull(parseDistanceCommand("cuaca hari ini"))

    @Test fun routeSourceChainsGeocodeAndDirections() = runBlocking {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("""{"features":[{"geometry":{"coordinates":[101.08,4.59]}}]}"""))
        server.enqueue(MockResponse().setBody("""{"features":[{"geometry":{"coordinates":[101.69,3.14]}}]}"""))
        server.enqueue(MockResponse().setBody("""{"features":[{"properties":{"summary":{"distance":205349.0,"duration":7790.0}}}]}"""))
        server.start()
        val r = RouteSource("K", server.url("/").toString().trimEnd('/'), OkHttpClient())
            .route(DistanceQuery("Ipoh", "KL"))!!
        assertEquals(205.3, r.km, 0.0001)
        assertEquals(130, r.minutes)
        server.takeRequest(); server.takeRequest()
        val dir = server.takeRequest().path!!
        assertTrue(dir.contains("start=101.08,4.59"))
        assertTrue(dir.contains("end=101.69,3.14"))
        server.shutdown()
    }

    @Test fun unknownPlaceGivesNull() = runBlocking {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("""{"features":[]}"""))
        server.start()
        assertNull(RouteSource("K", server.url("/").toString().trimEnd('/'), OkHttpClient()).route(DistanceQuery("Xyz", "KL")))
        server.shutdown()
    }
}
