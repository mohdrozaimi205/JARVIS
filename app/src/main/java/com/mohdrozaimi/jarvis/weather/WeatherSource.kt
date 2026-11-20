package com.mohdrozaimi.jarvis.weather

import com.mohdrozaimi.jarvis.data.Http
import com.mohdrozaimi.jarvis.data.enc
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class Forecast(
    val place: String,
    val date: LocalDate,
    val morning: String,
    val afternoon: String,
    val night: String,
    val summary: String,
    val minC: Int,
    val maxC: Int,
)

/** Ramalan cuaca rasmi MET Malaysia melalui api.data.gov.my. */
class WeatherSource(
    private val baseUrl: String = "https://api.data.gov.my",
    private val client: OkHttpClient = Http.client,
) {
    suspend fun today(place: String, today: LocalDate = LocalDate.now()): Forecast? {
        val json = Http.get("$baseUrl/weather/forecast?contains=${enc(place)}@location__location_name", client)
        val all = parseForecast(json)
        val samePlace = all.filter { it.place.equals(place, ignoreCase = true) }.ifEmpty { all }
        return samePlace.firstOrNull { it.date == today } ?: samePlace.minByOrNull { it.date }
    }

    /** Senarai nama lokasi daerah (location_id bermula "Ds"). */
    suspend fun districtNames(): List<String> {
        val json = Http.get("$baseUrl/weather/forecast?contains=Ds@location__location_id", client)
        return parseForecast(json).map { it.place }.distinct()
    }
}

fun parseForecast(json: String): List<Forecast> {
    val text = json.trim()
    val arr = when {
        text.startsWith("[") -> JSONArray(text)
        text.startsWith("{") -> JSONObject(text).optJSONArray("data") ?: JSONArray()
        else -> JSONArray()
    }
    val out = mutableListOf<Forecast>()
    for (i in 0 until arr.length()) {
        val o = arr.optJSONObject(i) ?: continue
        val loc = o.optJSONObject("location") ?: continue
        val date = runCatching { LocalDate.parse(o.getString("date")) }.getOrNull() ?: continue
        out += Forecast(
            place = loc.optString("location_name"),
            date = date,
            morning = o.optString("morning_forecast"),
            afternoon = o.optString("afternoon_forecast"),
            night = o.optString("night_forecast"),
            summary = o.optString("summary_forecast"),
            minC = o.optInt("min_temp"),
            maxC = o.optInt("max_temp"),
        )
    }
    return out
}
