package com.mohdrozaimi.jarvis.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mohdrozaimi.jarvis.data.AppSettings
import com.mohdrozaimi.jarvis.data.SettingsRepo
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.ZoneId

object MorningAlarm {
    const val ACTION_MORNING = "com.mohdrozaimi.jarvis.MORNING"

    fun schedule(ctx: Context, s: AppSettings) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            ctx, 1,
            Intent(ctx, AlarmReceiver::class.java).setAction(ACTION_MORNING),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        am.cancel(pi)
        if (!s.alarmEnabled) return
        val at = nextTrigger(LocalDateTime.now(), s.alarmHour, s.alarmMinute)
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val exactOk = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        if (exactOk) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }

    suspend fun scheduleFromSettings(ctx: Context) {
        schedule(ctx, SettingsRepo(ctx).flow.first())
    }
}
