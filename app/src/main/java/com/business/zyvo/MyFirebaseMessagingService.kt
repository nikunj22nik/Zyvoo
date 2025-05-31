package com.business.zyvo

import android.Manifest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.business.zyvo.service.CountdownService
import com.business.zyvo.service.ReminderReceiver
import com.business.zyvo.utils.AppContextProvider
import com.business.zyvo.utils.ErrorDialog
import com.google.firebase.crashlytics.internal.common.CommonUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle FCM messages here.
        Log.d(ErrorDialog.TAG,"remoteMessage")
        if(remoteMessage.data.containsKey("unread_booking_count")){
            var str =  remoteMessage.data.get("unread_booking_count")
            val intent = Intent("com.example.broadcast.ACTION_SEND_MESSAGE")
            intent.putExtra("message", str.toString())
            Log.d("BOOKING_COUNT",str.toString()+" Booking count is")
            // Send the broadcast using LocalBroadcastManager
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent)
        }

            remoteMessage.data?.let {
                Log.d(ErrorDialog.TAG,it.toString())
                val title = it.get("title")
                val body = it.get("body")
                if (!title.isNullOrEmpty() && !body.isNullOrEmpty()) {
                    sendNotification(title, body)
                } else {
                    Log.w(ErrorDialog.TAG, "Notification data missing title or body: title=$title, body=$body")
                }
            }

        }



    private fun sendNotification(title: String?, messageBody: String?) {
        val channelId = "com.business.zyvo"
     /*    if(messageBody =="Your booking has been approved."*//*"Your booking  will start in 30 minutes."*//*){
             //startCountdownService()
           //  startCountTwo()
             val countdownDuration = 30 * 60 * 1000L
             val endTime = System.currentTimeMillis() + countdownDuration
             Log.d(ErrorDialog.TAG,"Yes"+messageBody)
            val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.notification_icon)
                .setWhen(endTime)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setOngoing(true)
                .setContentText(messageBody)
                .setAutoCancel(true)

             val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                 val channel = NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT)
                 notificationManager.createNotificationChannel(channel)
             }

             var con= AppContextProvider.getContext()

             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                 if (ContextCompat.checkSelfPermission(con, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                     notificationManager.notify(getRandomNumber(), notificationBuilder.build())
                 }
             }
             else{
                 notificationManager.notify(getRandomNumber(), notificationBuilder.build())
             }
         } else{*/
            val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentText(messageBody)
                .setAutoCancel(true)

             val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                 val channel = NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT)
                 notificationManager.createNotificationChannel(channel)
             }

             val con= AppContextProvider.getContext()

             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                 if (ContextCompat.checkSelfPermission(con, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                     notificationManager.notify(getRandomNumber(), notificationBuilder.build())
                 }
             }
             else{
                 notificationManager.notify(getRandomNumber(), notificationBuilder.build())
             }
     //   }
    }


    private fun startCountdownService() {
        Log.d(ErrorDialog.TAG,"Yes")
        val countdownDuration = 30 * 60 * 1000L // 30 minutes
        // Start the countdown service (CountdownService)
       // val intent = Intent(this, CountdownService::class.java)
      //  intent.putExtra("duration", countdownDuration)
        //   ContextCompat.startForegroundService(this, intent)
        ContextCompat.startForegroundService(this, Intent(this, CountdownService::class.java).apply {
            putExtra("duration", 30 * 60 * 1000L)
        })
    }

    fun getRandomNumber(): Int {
        val rand = Random()
        return rand.nextInt(1000)
    }

}