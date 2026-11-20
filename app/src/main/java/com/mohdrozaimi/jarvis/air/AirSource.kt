package com.mohdrozaimi.jarvis.air

import com.mohdrozaimi.jarvis.data.Http
import okhttp3.OkHttpClient
import org.json.JSONObject

data class AirQuality(val usAqi: Int, val pm25: Double, val category: String)

/** Kualiti udara (anggaran IPU) dari Open-Meteo. */
class AirSource(
    private val baseUrl: String = "https://air-quality-api.open-meteo.com",
    private val client: OkHttpClient = Http.client,
) {
    suspend fun now(lat: Double, lon: Double): AirQuality =
        parseAir(Http.get("$baseUrl/v1/air-quality?latitude=$lat&longitude=$lon&current=us_aqi,pm2_5", client))
}

/** Petakan US AQI ke kategori IPU Malaysia. */
fun aqiToIpu(aqi: Int): String = when {
    aqi <= 50 -> "Baik"
    aqi <= 100 -> "Sederhana"
    aqi <= 200 -> "Tidak Sihat"
    aqi <= 300 -> "Sangat Tidak Sihat"
    else -> "Berbahaya"
}

fun parseAir(json: String): AirQuality {
    val cur = JSONObject(json).getJSONObject("current")
    val aqi = cur.optInt("us_aqi", -1)
    require(aqi >= 0) { "us_aqi tiada" }
    return AirQuality(aqi, cur.optDouble("pm2_5", 0.0), aqiToIpu(aqi))
}
