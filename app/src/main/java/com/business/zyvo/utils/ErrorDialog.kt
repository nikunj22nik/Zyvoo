package com.business.zyvo.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
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
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
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

    fun showToast(context: Context?,string: String?){
        Toast.makeText(context,string, Toast.LENGTH_SHORT).show()
    }

    fun convertHoursToHrMin(timeInHours: Double): String {
        return try {
            val hours = timeInHours.toInt() // Get whole hours
            val minutes = ((timeInHours - hours) * 60).toInt() // Convert decimal part to minutes
            "$hours hr $minutes min"
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
        } // Input format
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Output format
        return LocalDate.parse(dateStr, inputFormatter).format(outputFormatter)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun convertToTimeFormat(time: String): String {
        val formatter = DateTimeFormatter.ofPattern("hh:mm a",Locale.ENGLISH) // Input format
        val outputFormatter = DateTimeFormatter.ofPattern("HH:mm:ss") // Output format
        return LocalTime.parse(time, formatter).format(outputFormatter)
    }

    fun calculatePercentage(value: Double?, percentage: Double?): Double  {
        return if (value != null && percentage != null) {
            value * (percentage / 100)
        } else {
            0.0 // Return 0.0 if any value is null
        }
    }





}