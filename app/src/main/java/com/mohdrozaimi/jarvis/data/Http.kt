package com.mohdrozaimi.jarvis.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/** Satu OkHttpClient untuk seluruh app + pembantu GET/POST ringkas. */
object Http {
    val client: OkHttpClient by lazy {
        OkHttpClient.Builder().callTimeout(25, TimeUnit.SECONDS).build()
    }

    suspend fun get(url: String, client: OkHttpClient = Http.client): String =
        withContext(Dispatchers.IO) {
            exec(client, Request.Builder().url(url).header("User-Agent", UA).build())
        }

    suspend fun postJson(url: String, json: String, client: OkHttpClient = Http.client): String =
        withContext(Dispatchers.IO) {
            val body = json.toRequestBody("application/json".toMediaType())
            exec(client, Request.Builder().url(url).header("User-Agent", UA).post(body).build())
        }

    private fun exec(client: OkHttpClient, req: Request): String =
        client.newCall(req).execute().use { r ->
            if (!r.isSuccessful) throw IOException("HTTP ${r.code}")
            r.body?.string() ?: ""
        }

    private const val UA = "JARVIS-Android/1.0"
}

fun enc(s: String): String = URLEncoder.encode(s, "UTF-8")
