package com.business.zyvo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.business.zyvo.R

class ReminderReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
       /* // Show reminder notification
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "reminder_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Event Reminder")
            .setContentText("Your event starts in 30 minutes")
            .setSmallIcon(R.drawable.notification_icon)
            .build()

        notificationManager.notify(100, notification)

        // Start the countdown service (30 minutes = 30 * 60 * 1000 ms)
        val serviceIntent = Intent(context, CountdownService::class.java)
        serviceIntent.putExtra("duration", 30 * 60 * 1000L)
        ContextCompat.startForegroundService(context, serviceIntent) */
        val duration = intent?.getLongExtra("duration", 0L) ?: 0L
        val serviceIntent = Intent(context, CountdownService::class.java).apply {
            putExtra("duration", duration)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}