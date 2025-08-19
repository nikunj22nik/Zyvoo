package com.business.zyvo

import android.util.Log
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    data class DateComponents(val year: Long, val month: Long, val day: Long, val hour: Long, val minute: Long)

    fun updateLastMsgTime(time: String): String {
//        try {
//            Log.d("*******",time)
//            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault())
//            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
//
//            val secondDate = dateFormat.parse(time)
//
//            val currentCalendar = Calendar.getInstance()
//            val firstDate = currentCalendar.time
//
//            val dateComponents = calculateDateComponents(secondDate, firstDate)
//
//            return when {
//                dateComponents.year > 0 -> "${dateComponents.year}y ago"
//                dateComponents.month > 0 -> "${dateComponents.month} month ago"
//                dateComponents.day > 0 -> "${dateComponents.day}d ago"
//                dateComponents.hour > 0 -> "${dateComponents.hour}h ago"
//                dateComponents.minute > 0 -> {
//                    if (dateComponents.minute >= 1) {
//                        "${dateComponents.minute}m ago"
//                    } else {
//                        "now"
//                    }
//                }
//                else -> "now"
//            }
//        } catch (e: ParseException) {
//            e.printStackTrace()
//        }
//
//        return "now"
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

    /**
     * This function calculates the difference between two dates and returns it as a DateComponents object
     */
    fun calculateDateComponents(startDate: Date, endDate: Date): DateComponents {
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        var start = calendar.timeInMillis

        calendar.time = endDate
        var end = calendar.timeInMillis

        var diff = end - start

        val years = diff / (365 * 24 * 60 * 60 * 1000)
        diff %= (365 * 24 * 60 * 60 * 1000)

        val months = diff / (30 * 24 * 60 * 60 * 1000)
        diff %= (30 * 24 * 60 * 60 * 1000)

        val days = diff / (24 * 60 * 60 * 1000)
        diff %= (24 * 60 * 60 * 1000)

        val hours = diff / (60 * 60 * 1000)
        diff %= (60 * 60 * 1000)

        val minutes = diff / (60 * 1000)

        return DateComponents(years, months, days, hours, minutes)
    }



}