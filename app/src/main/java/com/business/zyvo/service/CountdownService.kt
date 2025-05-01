package com.business.zyvo.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.business.zyvo.R

class CountdownService : Service() {

    private lateinit var notificationManager: NotificationManager
    private lateinit var countDownTimer: CountDownTimer
    private val notificationId = 1
    private val channelId = "countdown_channel"

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val durationMillis = intent?.getLongExtra("duration", 30 * 60 * 1000L) ?: return START_NOT_STICKY

        startForeground(notificationId, buildNotification("Starting countdown..."))

        countDownTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                val time = String.format("%02d:%02d", minutes, seconds)
                updateNotification("$time")
            }

            override fun onFinish() {
                updateNotification("Countdown finished")
                stopSelf()
            }
        }.start()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Countdown Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(content: String): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Zyvo")
            .setContentText("Your booking  will start in $content")
            .setSmallIcon(R.drawable.notification_icon)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(content: String) {
        val notification = buildNotification(content)
        notificationManager.notify(notificationId, notification)
    }
}