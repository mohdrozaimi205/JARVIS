package com.mohdrozaimi.jarvis

import com.mohdrozaimi.jarvis.ai.GEMINI_MODEL
import com.mohdrozaimi.jarvis.ai.GeminiClient
import com.mohdrozaimi.jarvis.ai.buildNutritionPrompt
import com.mohdrozaimi.jarvis.ai.parseGeminiText
import com.mohdrozaimi.jarvis.health.HealthSnapshot
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionTest {
    @Test fun promptSkipsNulls() {
        val p = buildNutritionPrompt(HealthSnapshot(8200, null, null))
        assertFalse(p.contains("null"))
        assertTrue(p.contains("8200"))
        assertTrue(p.contains("Bahasa Melayu"))
        assertTrue(p.contains("3 ayat"))
        assertTrue(p.contains("bukan nasihat perubatan"))
    }

    @Test fun promptWithNoData() {
        assertTrue(buildNutritionPrompt(HealthSnapshot(null, null, null)).contains("tiada data kesihatan"))
    }

    private val reply = """{"candidates":[{"content":{"parts":[{"text":"Makan nasi lemak "},{"text":"separuh pinggan."}]}}]}"""

    @Test fun parsesText() = assertEquals("Makan nasi lemak separuh pinggan.", parseGeminiText(reply))

    @Test fun noCandidatesGivesEmpty() {
        assertEquals("", parseGeminiText("""{"promptFeedback":{}}"""))
        assertEquals("", parseGeminiText("bukan json"))
    }

    @Test fun clientCallsModelWithKey() = runBlocking {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(reply))
        server.start()
        val text = GeminiClient("KUNCI123", server.url("/").toString().trimEnd('/'), OkHttpClient()).ask("hai")
        val req = server.takeRequest()
        assertEquals("Makan nasi lemak separuh pinggan.", text)
        assertTrue(req.path!!.contains(GEMINI_MODEL))
        assertTrue(req.path!!.contains("key=KUNCI123"))
        assertTrue(req.body.readUtf8().contains("hai"))
        server.shutdown()
    }
}
