package com.business.zyvo.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class ReminderReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val duration = intent?.getLongExtra("duration", 0L) ?: 0L
        val serviceIntent = Intent(context, CountdownService::class.java).apply {
            putExtra("duration", duration)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}