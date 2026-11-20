package com.mohdrozaimi.jarvis.ai

import com.mohdrozaimi.jarvis.data.Http
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject

/** Alias Google yang sentiasa menunjuk ke model Flash terkini. */
const val GEMINI_MODEL = "gemini-flash-latest"

class GeminiClient(
    private val key: String,
    private val baseUrl: String = "https://generativelanguage.googleapis.com",
    private val client: OkHttpClient = Http.client,
) {
    suspend fun ask(prompt: String): String {
        val body = JSONObject().put(
            "contents",
            JSONArray().put(
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", prompt))),
            ),
        ).toString()
        val res = Http.postJson("$baseUrl/v1beta/models/$GEMINI_MODEL:generateContent?key=$key", body, client)
        return parseGeminiText(res)
    }
}

fun parseGeminiText(json: String): String = try {
    val candidates = JSONObject(json).optJSONArray("candidates")
    if (candidates == null || candidates.length() == 0) {
        ""
    } else {
        val parts = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts")
        (0 until parts.length()).joinToString("") { parts.getJSONObject(it).optString("text") }.trim()
    }
} catch (e: Exception) {
    ""
}
