package com.mohdrozaimi.jarvis.route

import com.mohdrozaimi.jarvis.data.Http
import com.mohdrozaimi.jarvis.data.enc
import okhttp3.OkHttpClient
import org.json.JSONObject
import kotlin.math.roundToInt

data class Route(val km: Double, val minutes: Int)

/** Jarak memandu melalui OpenRouteService. */
class RouteSource(
    private val key: String,
    private val baseUrl: String = "https://api.openrouteservice.org",
    private val client: OkHttpClient = Http.client,
) {
    /** Pulang (lon, lat) atau null jika tempat tak dijumpai. */
    suspend fun geocode(text: String): Pair<Double, Double>? {
        val json = Http.get(
            "$baseUrl/geocode/search?api_key=$key&text=${enc(text)}&boundary.country=MY&size=1",
            client,
        )
        val features = JSONObject(json).optJSONArray("features")
        if (features == null || features.length() == 0) return null
        val c = features.getJSONObject(0).getJSONObject("geometry").getJSONArray("coordinates")
        return c.getDouble(0) to c.getDouble(1)
    }

    suspend fun route(q: DistanceQuery): Route? {
        val a = geocode(q.from) ?: return null
        val b = geocode(q.to) ?: return null
        val json = Http.get(
            "$baseUrl/v2/directions/driving-car?api_key=$key" +
                "&start=${a.first},${a.second}&end=${b.first},${b.second}",
            client,
        )
        return parseRoute(json)
    }
}

fun parseRoute(json: String): Route {
    val s = JSONObject(json).getJSONArray("features").getJSONObject(0)
        .getJSONObject("properties").getJSONObject("summary")
    val km = (s.getDouble("distance") / 100.0).roundToInt() / 10.0
    val minutes = (s.getDouble("duration") / 60.0).roundToInt()
    return Route(km, minutes)
}
