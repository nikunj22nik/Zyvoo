package com.yesitlab.zyvo.DateManager

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.widget.NumberPicker
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

class DateManager(var context : Context) {

    private val months = arrayOf(
        "January", "February", "March", "April",
        "May", "June", "July", "August",
        "September", "October", "November", "December"
    )

     fun showMonthSelectorDialog(onMonthSelected: (String) -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Select Month")
            .setItems(months) { dialog, which ->
                onMonthSelected(months[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

     fun showYearPickerDialog(onYearSelected: (Int) -> Unit) {
        val yearPicker = NumberPicker(context).apply {
            minValue = 1900
            maxValue = 2100
            value = 2023 // Set default year
        }

        AlertDialog.Builder(context)
            .setTitle("Select Year")
            .setView(yearPicker)
            .setPositiveButton("OK") { _, _ ->
                onYearSelected(yearPicker.value)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentMonthAndYear(): Pair<String, Int> {
        val currentDate = LocalDate.now()
        val monthName = currentDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val year = currentDate.year

        return Pair(monthName, year)
    }



    fun showTimePickerDialog(context: Context, onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                // Convert to AM/PM format
                val amPm = if (hourOfDay < 12) "AM" else "PM"
                val hourIn12HourFormat = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                val formattedTime = String.format("%02d:%02d %s", hourIn12HourFormat, minute, amPm)

                // Return the formatted time via the callback
                onTimeSelected(formattedTime)
            },
            currentHour,
            currentMinute,
            false // Use 12-hour format
        )
        timePickerDialog.show()
    }

    fun showHourSelectionDialog(context: Context, onHourSelected: (String) -> Unit) {
        val numberPicker = NumberPicker(context).apply {
            minValue = 1
            maxValue = 24
            wrapSelectorWheel = true
        }

        AlertDialog.Builder(context)
            .setTitle("Select Hours")
            .setView(numberPicker)
            .setPositiveButton("OK") { _, _ ->
                val selectedHour = numberPicker.value
                val result = "$selectedHour hour${if (selectedHour > 1) "s" else ""}"
                onHourSelected(result) // Return the selected hour as a string
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}