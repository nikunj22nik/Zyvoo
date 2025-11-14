package com.business.zyvo.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.business.zyvo.R
import com.business.zyvo.databinding.CustomDialogBinding
import com.business.zyvo.model.LocationDetails
import com.google.android.gms.maps.model.LatLng
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

object ErrorDialog {

    const val TAG = "*****ZYVO"
    fun showErrorDialog(activity: Context, message: String) {
        val dialog = AlertDialog.Builder(activity)
            .setTitle("Error") // Title of the dialog
            .setMessage(message) // Error message to show
            .setCancelable(false) // Set cancelable to false to prevent dismissing by tapping outside
            .setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss() // Dismiss the dialog when 'OK' is pressed
            }
            .create()

        dialog.show()
    }

    fun AlertDialog.dismissSafe() {
        window?.let {
            if (it.decorView != null && it.decorView.parent != null) {
                this.dismiss()
            }
        } ?: run {
            return
        }
    }

    class SafeClickListener(private var defaultInterval: Int = 1000,
                            private val onSafeCLick: (View) -> Unit) : View.OnClickListener {

      private var lastTimeClicked: Long = 0

      override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
                return
            }
            lastTimeClicked = SystemClock.elapsedRealtime()
            onSafeCLick(v)
        }

    }

    private fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
        val safeClickListener = SafeClickListener {
            onSafeClick(it)
        }
        setOnClickListener(safeClickListener)
    }

    fun toMultiPartFile(part: String, name: String, byteArray: ByteArray): MultipartBody.Part {
        val mediaType = "multipart/form-data".toMediaTypeOrNull()
        val reqFile = RequestBody.create(mediaType, byteArray)
        return MultipartBody.Part.createFormData(part, name, reqFile)
    }

    fun createRequestBody(name: String): RequestBody {
        val mediaType = "multipart/form-data".toMediaType()
        return name.toRequestBody(mediaType)
    }

    fun createMultipartList(items: List<String>): List<RequestBody> {
        val mediaType = "multipart/form-data".toMediaType()
        return items.map { it.toRequestBody(mediaType) }
    }


    @SuppressLint("DefaultLocale")
    fun formatConvertCount(count: String): String {
        return try {
            val num = count.toInt()
            when {
                num >= 1_000_000 -> if (num % 1_000_000 == 0) "${num / 1_000_000}M" else String.format("%.1fM", num / 1_000_000.0)
                num >= 1_000 -> if (num % 1_000 == 0) "${num / 1_000}K" else String.format("%.1fK", num / 1_000.0)
                else -> num.toString()
            }
        } catch (e: NumberFormatException) {
            count
        }
    }

    fun addHours(timeStr: String, hoursToAdd: Int): String {

        val format = SimpleDateFormat("hh:mm a", Locale.US)
        val date = format.parse(timeStr) ?: return "Invalid time"

        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.HOUR_OF_DAY, hoursToAdd)

        return format.format(calendar.time)
    }

    fun showToast(context: Context?,string: String?){
        Toast.makeText(context,string, Toast.LENGTH_SHORT).show()
    }

    fun convertHoursToHrMin(timeInHours: Double): String {
        return try {
            val hours = timeInHours.toInt() // Get whole hours
            val minutes = ((timeInHours - hours) * 60).toInt() // Convert decimal part to minutes
           // "$hours hr $minutes min"
            when {
                hours > 0 && minutes > 0 -> "$hours hr $minutes min"
                hours > 0 -> "$hours hr"
                minutes > 0 -> "$minutes min"
                else -> "0 min"
            }
        } catch (e: Exception) {
            Log.e("TimeConversion", "Error converting time: ${e.message}")
            "Invalid input"
        }
    }


    fun formatDateyyyyMMddToMMMMddyyyy(inputDate: String): String {
        val inputFormatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val outputFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)

        val date = LocalDate.parse(inputDate, inputFormatter)
        return date.format(outputFormatter)
    }

    fun convertDateFormatMMMMddyyyytoyyyyMMdd(dateStr: String): String {
        val inputFormatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Output format
        return LocalDate.parse(dateStr, inputFormatter).format(outputFormatter)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun convertToTimeFormat(time: String): String {
        var value  = ""

        if (!time.equals("")) {
            val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH) // Input format
            val outputFormatter = DateTimeFormatter.ofPattern("HH:mm:ss") // Output format
            value =  LocalTime.parse(time, formatter).format(outputFormatter)
        }
        return value
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertDateToTimeFormat(time: String): String {
        var value  = ""

        if (!time.equals("")) {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a", Locale.ENGLISH) // Input format
            val outputFormatter = DateTimeFormatter.ofPattern("hh:mm a") // Output format

            value =  LocalTime.parse(time, formatter).format(outputFormatter).uppercase()
        }
        return value
    }


    fun calculatePercentage(value: Double?, percentage: Double?): Double  {
        return if (value != null && percentage != null) {
            value * (percentage / 100)
        } else {
            0.0 // Return 0.0 if any value is null
        }
    }

    fun getCurrentDate(): String {
        var date = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val currentDate = LocalDateTime.now()
            date = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        } else {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            date = dateFormat.format(calendar.time)
        }
        return date
    }

    fun isAfterOrSame(dateTimeString: String): Boolean {
        val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val targetDateTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.parse(dateTimeString, formatter)
        } else {
           return false
        }
        val referenceDateTime = LocalDateTime.of(2025, 4, 1, 12, 15, 0)

        return targetDateTime.isAfter(referenceDateTime) || targetDateTime.isEqual(referenceDateTime)
    }

    fun getCurrentDateTime(): String {
        var date = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val currentDate = LocalDateTime.now()
            date = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        } else {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            date = dateFormat.format(calendar.time)
        }
        return date
    }


    fun calculateDifferenceInSeconds(startTime: String, endTime: String): Long {

        val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneOffset.UTC)
        } else {
            TODO("VERSION.SDK_INT < O")
        }

        val startInstant = LocalDateTime.parse(startTime, formatter).toInstant(ZoneOffset.UTC)
        val endInstant = LocalDateTime.parse(endTime, formatter).toInstant(ZoneOffset.UTC)

        return Duration.between(startInstant, endInstant).toMinutes() // Get difference in seconds
    }


    fun getMinutesPassed(targetTime: String): Long {
        val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        } else {
            TODO("VERSION.SDK_INT < O")
        }

        val targetInstant = LocalDateTime.parse(targetTime, formatter)
            .atZone(ZoneId.systemDefault()) // Convert to system default timezone
            .toInstant()
        // Get the current time as Instant
        val currentInstant = Instant.now()

        // Calculate the difference in minutes
        return Duration.between(targetInstant, currentInstant).toMinutes()
    }

    fun truncateToTwoDecimalPlaces(value: String): String {
        return try {
            val bigDecimal = BigDecimal(value).setScale(2, RoundingMode.DOWN)
            if (bigDecimal.stripTrailingZeros().scale() <= 0) {
                bigDecimal.toBigInteger().toString() // No decimal part
            } else {
                bigDecimal.toPlainString() // Show decimal part
            }
        } catch (e: NumberFormatException) {
            "0"
        }
    }


    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }


    fun getLocationDetails(context: Context, latLng: LatLng, callback: (LocationDetails?) -> Unit) {
        Thread {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

                val result = if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    LocationDetails(
                        streetAddress = address.getAddressLine(0),
                        city = address.locality,
                        state = address.adminArea,
                        zipCode = address.postalCode,
                        country = address.countryName
                    )
                } else null

                Log.d("checkResult", result.toString())
                Handler(Looper.getMainLooper()).post {
                    callback(result)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                }
            }
        }.start()
    }

    fun convertHoursToDays(hours: Int): Int {
        return hours / 24
    }

    fun isWithin24Hours(startTimeStr: String, endTimeStr: String): Boolean {
        return try {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())

            val startTime = format.parse(startTimeStr)
            val endTime = format.parse(endTimeStr)

            if (startTime != null && endTime != null) {
                val calendarStart = Calendar.getInstance().apply { time = startTime }
                val calendarEnd = Calendar.getInstance().apply { time = endTime }

                // If end time is before start time, assume it's the next day
                if (calendarEnd.before(calendarStart)) {
                    calendarEnd.add(Calendar.DATE, 1)
                }

                val diffMillis = calendarEnd.timeInMillis - calendarStart.timeInMillis
                val hoursDiff = diffMillis / (1000 * 60 * 60)

                hoursDiff <= 24
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    fun getHourDifference(startTimeStr: String, endTimeStr: String): Long {
        return try {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())

            val startTime = format.parse(startTimeStr)
            val endTime = format.parse(endTimeStr)

            if (startTime != null && endTime != null) {
                val calendarStart = Calendar.getInstance().apply { time = startTime }
                val calendarEnd = Calendar.getInstance().apply { time = endTime }

                // If end time is before start time, it means it's on the next day
                if (calendarEnd.before(calendarStart)) {
                    calendarEnd.add(Calendar.DATE, 1)
                }

                val diffMillis = calendarEnd.timeInMillis - calendarStart.timeInMillis
                val hoursDiff = diffMillis / (1000 * 60 * 60)

                hoursDiff
            } else {
                -1L  // return -1 to indicate error in parsing
            }
        } catch (e: Exception) {
            e.printStackTrace()
            -1L
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun convertTo12HourFormat(time24: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val outputFormatter = DateTimeFormatter.ofPattern("hh:mm a")

        val time = LocalTime.parse(time24, inputFormatter)
        return time.format(outputFormatter)
    }


    fun getUpdatedTimeZoneId(): String {
        val aliasMap = mapOf(
            "Asia/Calcutta" to "Asia/Kolkata",
            "Asia/Chungking" to "Asia/Shanghai",
            "Asia/Katmandu" to "Asia/Kathmandu",
            "Asia/Saigon" to "Asia/Ho_Chi_Minh",
            "Asia/Istanbul" to "Europe/Istanbul",
            "Asia/Kashgar" to "Asia/Urumqi",
            "Asia/Ujung_Pandang" to "Asia/Makassar",
            "Australia/ACT" to "Australia/Sydney",
            "Australia/NSW" to "Australia/Sydney",
            "Australia/North" to "Australia/Darwin",
            "Australia/South" to "Australia/Adelaide",
            "Australia/Tasmania" to "Australia/Hobart",
            "Australia/Victoria" to "Australia/Melbourne",
            "Australia/West" to "Australia/Perth",
            "Brazil/East" to "America/Sao_Paulo",
            "Canada/Eastern" to "America/Toronto",
            "Canada/Central" to "America/Winnipeg",
            "Canada/Mountain" to "America/Edmonton",
            "Canada/Pacific" to "America/Vancouver",
            "Canada/Atlantic" to "America/Halifax",
            "Canada/Newfoundland" to "America/St_Johns",
            "Etc/Greenwich" to "GMT",
            "Europe/Nicosia" to "Asia/Nicosia",
            "GMT" to "Etc/GMT",
            "Hongkong" to "Asia/Hong_Kong",
            "Iceland" to "Atlantic/Reykjavik",
            "Indian/Antananarivo" to "Africa/Nairobi",
            "Indian/Comoro" to "Africa/Nairobi",
            "Indian/Mayotte" to "Africa/Nairobi",
            "Iran" to "Asia/Tehran",
            "Israel" to "Asia/Jerusalem",
            "Jamaica" to "America/Jamaica",
            "Japan" to "Asia/Tokyo",
            "Mexico/General" to "America/Mexico_City",
            "Pacific/Johnston" to "Pacific/Honolulu",
            "Pacific/Ponape" to "Pacific/Pohnpei",
            "Pacific/Samoa" to "Pacific/Pago_Pago",
            "Pacific/Truk" to "Pacific/Chuuk",
            "Pacific/Yap" to "Pacific/Chuuk",
            "Poland" to "Europe/Warsaw",
            "Portugal" to "Europe/Lisbon",
            "Singapore" to "Asia/Singapore",
            "Turkey" to "Europe/Istanbul",
            "US/Alaska" to "America/Anchorage",
            "US/Aleutian" to "America/Adak",
            "US/Arizona" to "America/Phoenix",
            "US/Central" to "America/Chicago",
            "US/East-Indiana" to "America/Indiana/Indianapolis",
            "US/Eastern" to "America/New_York",
            "US/Hawaii" to "Pacific/Honolulu",
            "US/Indiana-Starke" to "America/Indiana/Knox",
            "US/Michigan" to "America/Detroit",
            "US/Mountain" to "America/Denver",
            "US/Pacific" to "America/Los_Angeles",
            "US/Samoa" to "Pacific/Pago_Pago"
        )

        return try {
            val systemZone = java.util.TimeZone.getDefault().id
            aliasMap[systemZone] ?: systemZone
        } catch (e: Exception) {
            e.printStackTrace()
            "UTC"
        }
    }

    fun isValidCardNumber(cardNumber: String): Boolean {
        val cleanedNumber = cardNumber.replace(" ", "").trim()

        if (!cleanedNumber.matches(Regex("\\d+"))) {
            return false
        }

        if (cleanedNumber.length !in 13..19) {
            return false
        }
        // Luhn algorithm check
        return isValidLuhn(cleanedNumber)
    }

    fun isValidLuhn(number: String): Boolean {
        var sum = 0
        var alternate = false

        for (i in number.length - 1 downTo 0) {
            var digit = number[i] - '0'

            if (alternate) {
                digit *= 2
                if (digit > 9) {
                    digit = digit % 10 + 1
                }
            }

            sum += digit
            alternate = !alternate
        }

        return sum % 10 == 0
    }

}