package com.business.zyvo

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.business.zyvo.utils.AppContextProvider
import com.google.firebase.crashlytics.internal.common.CommonUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private lateinit var commonUtils: CommonUtils

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle FCM messages here.

        var con= AppContextProvider.getContext()

        Log.d("CIRCLEIT_TOEK","HERE IN A NOTIFICATION")


        if(remoteMessage.data.containsKey("unread_booking_count")){
            var str =  remoteMessage.data.get("unread_booking_count")
            val intent = Intent("com.example.broadcast.ACTION_SEND_MESSAGE")
            intent.putExtra("message", str.toString())
            Log.d("BOOKING_COUNT",str.toString()+" Booking count is")

            // Send the broadcast using LocalBroadcastManager
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent)
        }
        else{

            Log.d("TESTING","Inside of else")

        }

//        if(commonUtils.getNotificationStatus()){
            remoteMessage.notification?.let {
                Log.d("CIRCLEIT_TOEK","Inside Notification")
                Log.d("CIRCLEIT_TOEK",it.title.toString() +" "+it.body.toString())
                sendNotification(it.title.toString(), it.body.toString())
            }

        }



    private fun sendNotification(title: String?, messageBody: String?) {
        val channelId = "default_channel_id"

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentText(messageBody)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        var con= AppContextProvider.getContext()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(con, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(0, notificationBuilder.build())
            }
        }
        else{
            notificationManager.notify(0, notificationBuilder.build())
        }
    }

}