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
import com.business.zyvo.AppConstant
import com.business.zyvo.R
import com.business.zyvo.databinding.CustomDialogBinding
import com.business.zyvo.model.LocationDetails
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
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
        // Create and build the AlertDialog
        val dialog = AlertDialog.Builder(activity)
            .setTitle("Error") // Title of the dialog
            .setMessage(message) // Error message to show
            .setCancelable(false) // Set cancelable to false to prevent dismissing by tapping outside
            .setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss() // Dismiss the dialog when 'OK' is pressed
            }
            .create()

        // Show the dialog
        dialog.show()
    }

    fun customDialog(string: String?, context: Context? = null) {
        try {
            string?.let {
                val dialogBinding: CustomDialogBinding? =
                    DataBindingUtil.inflate(
                        LayoutInflater.from(context),
                        R.layout.custom_dialog,
                        null,
                        false
                    )
                val customDialog = AlertDialog.Builder(context!!, 0).create()
                customDialog.apply {
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    setView(dialogBinding?.root)
                    setCancelable(false)
                }.show()
                val timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        try {
                            if (customDialog.isShowing) {
                                customDialog.dismissSafe()
                            }
                            timer.cancel()
                        } catch (e: Exception) { }
                    }
                }, 3500)
                dialogBinding?.textViewcustomdialog?.text = string
                dialogBinding?.imageView29?.setSafeOnClickListener {
                    if (customDialog.isShowing) {
                        customDialog.dismissSafe()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("dialog","dialog exception ${e.message}")
        }
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
     //   val fixedDateStr = dateStr.replace(",", ", ") // Ensure space after comma

        val inputFormatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)
        } else {
            TODO("VERSION.SDK_INT < O")
        } // Input format
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertDateToTimeFormat1(time: String): String {
        var value  = ""

        if (!time.equals("")) {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a", Locale.ENGLISH) // Input format
            val outputFormatter = DateTimeFormatter.ofPattern("HH:mm:ss") // Output format
            value =  LocalTime.parse(time, formatter).format(outputFormatter)
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
       /* val startInstant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant.parse(startTime)
        } else {
            TODO("VERSION.SDK_INT < O")
        }  // Parse ISO 8601 timestamp
        val endInstant = Instant.parse(endTime)      // Parse ISO 8601 timestamp

        return Duration.between(startInstant, endInstant).toMinutes()  // Get difference in seconds*/

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

        // Parse the target time and convert to Instant
        val targetInstant = LocalDateTime.parse(targetTime, formatter)
            .atZone(ZoneId.systemDefault()) // Convert to system default timezone
            .toInstant()
        // Get the current time as Instant
        val currentInstant = Instant.now()

        // Calculate the difference in minutes
        return Duration.between(targetInstant, currentInstant).toMinutes()
    }


    /*fun truncateToTwoDecimalPlaces(value: String): String {
        return try {
            BigDecimal(value)
                .setScale(2, RoundingMode.DOWN) // Truncate without rounding
                .toPlainString() // Ensures no scientific notation
        } catch (e: NumberFormatException) {
            "0.00" // Default value if input is invalid
        }
    }*/
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

    fun isFutureOrToday(date: LocalDate): Boolean {
        val today = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        return date.isEqual(today) || date.isAfter(today)
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


    fun convertTo24HourFormat(inputDate: String): String? {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateObj = inputFormat.parse(inputDate)
            outputFormat.format(dateObj)
        } catch (e: ParseException) {
            Log.e("DateFormatter", "Error parsing date: ${e.message}")
            null
        }


    }
    fun convertTo24HourFormatSecond(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return try {
            val date = inputFormat.parse(inputDate)
            outputFormat.format(date)
        } catch (e: Exception) {
            Log.e("DateConvert", "Error: ${e.message}")
            "" // Return original if error occurs
        }
    }


}