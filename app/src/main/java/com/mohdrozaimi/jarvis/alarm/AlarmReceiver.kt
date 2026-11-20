package com.mohdrozaimi.jarvis.alarm

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mohdrozaimi.jarvis.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Kejut pagi → notifikasi "Taklimat pagi sedia"; juga jadual semula selepas telefon dihidupkan. */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == MorningAlarm.ACTION_MORNING) notify(ctx)
        val done = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                MorningAlarm.scheduleFromSettings(ctx)
            } finally {
                done.finish()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun notify(ctx: Context) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Taklimat pagi", NotificationManager.IMPORTANCE_HIGH),
        )
        val open = Intent(ctx, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra(MainActivity.EXTRA_AUTO_BRIEF, true)
        val pi = PendingIntent.getActivity(
            ctx, 2, open, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val n = NotificationCompat.Builder(ctx, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Good morning, sir.")
            .setContentText("Taklimat pagi anda sudah sedia. Tekan untuk dengar.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        val mgr = NotificationManagerCompat.from(ctx)
        if (mgr.areNotificationsEnabled()) mgr.notify(10, n)
    }

    private companion object {
        const val CHANNEL = "taklimat"
    }
}
