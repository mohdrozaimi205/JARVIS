package com.mohdrozaimi.jarvis.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

object LocationProvider {

    fun hasPermission(ctx: Context) =
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /** Lokasi terkini (lokasi terakhir diketahui, atau minta baru pada Android 11+). */
    @SuppressLint("MissingPermission")
    suspend fun current(ctx: Context): Location? {
        if (!hasPermission(ctx)) return null
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val last = lm.getProviders(true)
            .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
        val fresh = last != null && System.currentTimeMillis() - last.time < 30 * 60_000L
        if (fresh) return last
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val provider = when {
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                else -> return last
            }
            val now = withTimeoutOrNull(8_000) {
                suspendCancellableCoroutine<Location?> { cont ->
                    lm.getCurrentLocation(provider, null, ctx.mainExecutor) { cont.resume(it) }
                }
            }
            return now ?: last
        }
        return last
    }

    /** Nama daerah/bandar untuk padanan lokasi MET. */
    @Suppress("DEPRECATION")
    suspend fun district(ctx: Context, loc: Location?): String? {
        if (loc == null || !Geocoder.isPresent()) return null
        return withContext(Dispatchers.IO) {
            runCatching {
                Geocoder(ctx, Locale("ms", "MY"))
                    .getFromLocation(loc.latitude, loc.longitude, 1)
                    ?.firstOrNull()
                    ?.let { it.subAdminArea ?: it.locality }
            }.getOrNull()
        }
    }
}
