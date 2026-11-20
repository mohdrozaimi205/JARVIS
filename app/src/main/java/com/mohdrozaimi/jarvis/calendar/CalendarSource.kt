package com.mohdrozaimi.jarvis.calendar

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract.Instances
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

/** Baca acara hari ini dari kalendar telefon (Google, Outlook jika di-sync, dll). */
class CalendarSource(private val ctx: Context) {

    suspend fun today(): List<Meeting> = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CALENDAR) !=
            PackageManager.PERMISSION_GRANTED
        ) throw SecurityException("Tiada kebenaran kalendar")

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val begin = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val uri = Instances.CONTENT_URI.buildUpon().also {
            ContentUris.appendId(it, begin)
            ContentUris.appendId(it, end)
        }.build()
        val projection = arrayOf(
            Instances.TITLE, Instances.BEGIN, Instances.END,
            Instances.ALL_DAY, Instances.CALENDAR_DISPLAY_NAME,
        )
        val list = mutableListOf<Meeting>()
        ctx.contentResolver.query(uri, projection, null, null, "${Instances.BEGIN} ASC")?.use { c ->
            while (c.moveToNext()) {
                val allDay = c.getInt(3) == 1
                val tz: ZoneId = if (allDay) ZoneOffset.UTC else zone
                list += Meeting(
                    title = c.getString(0)?.takeIf { it.isNotBlank() } ?: "(Tanpa tajuk)",
                    start = toLocal(c.getLong(1), tz),
                    end = toLocal(c.getLong(2), tz),
                    allDay = allDay,
                    calendar = c.getString(4) ?: "",
                )
            }
        }
        filterToday(list, LocalDateTime.now(zone))
    }

    private fun toLocal(ms: Long, tz: ZoneId): LocalDateTime =
        Instant.ofEpochMilli(ms).atZone(tz).toLocalDateTime()
}
