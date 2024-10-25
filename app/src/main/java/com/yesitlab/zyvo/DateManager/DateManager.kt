package com.yesitlab.zyvo.DateManager

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.widget.NumberPicker
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.TextStyle
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

}