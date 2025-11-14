package com.business.zyvo

import android.util.Log
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    data class DateComponents(val year: Long, val month: Long, val day: Long, val hour: Long, val minute: Long)

    fun updateLastMsgTime(time: String): String {

        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")

            val pastDate = dateFormat.parse(time) ?: return "now"
            val now = System.currentTimeMillis()
            val diff = now - pastDate.time

            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            val months = days / 30
            val years = days / 365

            when {
                years > 0 -> "${years}y ago"
                months > 0 -> "${months}month ago"
                days > 0 -> "${days}d ago"
                hours > 0 -> "${hours}h ago"
                minutes > 0 -> "${minutes}minutes ago"
                else -> "now"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "now"
        }
    }


}