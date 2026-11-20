package com.mohdrozaimi.jarvis.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class HealthSnapshot(
    val stepsYesterday: Long?,
    val sleepHours: Double?,
    val restingHr: Int?,
) {
    val isEmpty get() = stepsYesterday == null && sleepHours == null && restingHr == null
}

/** Data kesihatan dari Health Connect (baca sahaja). */
class HealthSource(private val ctx: Context) {

    companion object {
        val STEPS = HealthPermission.getReadPermission(StepsRecord::class)
        val SLEEP = HealthPermission.getReadPermission(SleepSessionRecord::class)
        val RESTING_HR = HealthPermission.getReadPermission(RestingHeartRateRecord::class)
        val PERMISSIONS = setOf(STEPS, SLEEP, RESTING_HR)

        fun status(ctx: Context): Int = HealthConnectClient.getSdkStatus(ctx)
    }

    suspend fun snapshot(): HealthSnapshot {
        check(status(ctx) == HealthConnectClient.SDK_AVAILABLE) { "Health Connect tiada" }
        val client = HealthConnectClient.getOrCreate(ctx)
        val granted = client.permissionController.getGrantedPermissions()

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val startYesterday = today.minusDays(1).atStartOfDay(zone).toInstant()
        val startToday = today.atStartOfDay(zone).toInstant()
        val now = Instant.now()

        val steps = if (STEPS in granted) {
            client.aggregate(
                AggregateRequest(
                    setOf(StepsRecord.COUNT_TOTAL),
                    TimeRangeFilter.between(startYesterday, startToday),
                ),
            )[StepsRecord.COUNT_TOTAL]
        } else null

        val sleep = if (SLEEP in granted) {
            val minutes = client.readRecords(
                ReadRecordsRequest(
                    SleepSessionRecord::class,
                    TimeRangeFilter.between(now.minus(Duration.ofHours(24)), now),
                ),
            ).records.sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }
            if (minutes > 0) minutes / 60.0 else null
        } else null

        val hr = if (RESTING_HR in granted) {
            client.readRecords(
                ReadRecordsRequest(
                    RestingHeartRateRecord::class,
                    TimeRangeFilter.between(now.minus(Duration.ofDays(7)), now),
                ),
            ).records.maxByOrNull { it.time }?.beatsPerMinute?.toInt()
        } else null

        return HealthSnapshot(steps, sleep, hr)
    }
}
