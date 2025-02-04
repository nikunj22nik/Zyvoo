package com.business.zyvo.DateManager

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.widget.NumberPicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun isFromTimeLessThanToTime(fromTime: String, toTime: String): Boolean {
        // Define the formatter for HH:mm format
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        // Parse the times into LocalTime objects
        val from = LocalTime.parse(fromTime, formatter)
        val to = LocalTime.parse(toTime, formatter)

        // Compare if the "from" time is earlier than the "to" time
        return from.isBefore(to)
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentWeek(): List<String> {
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.ordinal.toLong()) // Get Monday of the current week
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return DayOfWeek.values().mapIndexed { index, dayOfWeek ->
            val date = startOfWeek.plusDays(index.toLong())
            val formattedDate = date.format(formatter)
            val shortDayName =
                dayOfWeek.name.take(3) // Get the first three characters of the day name
            "${dayOfWeek.ordinal + 1} - "+"${shortDayName}"
        }

    }

    fun generateTimeList(): List<String> {
        val timeList = mutableListOf<String>()

        // Generate times from 12:00 AM to 11:59 PM
        for (hour in 0..23) {
            for (minute in 0..59 step 5) { // Using step 5 to have time in 5-minute intervals
                val ampm = if (hour < 12) "AM" else "PM"
                val hourIn12Format = if (hour % 12 == 0) 12 else hour % 12
                val timeString = String.format("%02d:%02d $ampm", hourIn12Format, minute)
                timeList.add(timeString)
            }
        }

        return timeList
    }


    fun generateHourList(): List<String> {
        val hourList = mutableListOf<String>()
        hourList.add("")
        // Generate times from 12:00 AM to 11:00 PM (only full hours)
        for (hour in 0..23) {
            val ampm = if (hour < 12) "AM\n" else "PM\n"
            val hourIn12Format = if (hour % 12 == 0) 12 else hour % 12
            val timeString = String.format("$ampm %02d:00", hourIn12Format)
            hourList.add(timeString)
        }

        return hourList
    }



/*
    fun  getRangeSelectedDateWithYear(
        fragmentManager: FragmentManager,
        onDateRangeSelected: (Pair<Pair<String, String>, Int>?) -> Unit
    ): Pair<Pair<String, String>, Int>? {
        // Initialize the MaterialDatePicker
        val datePicker = MaterialDatePicker.Builder.dateRangePicker().build()

        // Variable to hold the selected start and end dates and the year
        var selectedData: Pair<Pair<String, String>, Int>? = null

        // Show the DatePicker
        datePicker.show(fragmentManager, "DatePicker")

        // Set up the event for when the OK button is clicked
        datePicker.addOnPositiveButtonClickListener { selection ->
            // Extract start and end dates from the selection
            val startDate = selection?.first?.let { java.util.Date(it) }
            val endDate = selection?.second?.let { java.util.Date(it) }

            // Format the dates into readable strings
            val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            val startDateString = startDate?.let { dateFormat.format(it) }
            val endDateString = endDate?.let { dateFormat.format(it) }

            // Extract the year from the startDate (or endDate, as they should be in the same year)
            val calendar = java.util.Calendar.getInstance()
            startDate?.let { calendar.time = it }
            val year = calendar.get(java.util.Calendar.YEAR)

            // Display the selected date range
            Toast.makeText(context, "${datePicker.headerText }  $year is selected", Toast.LENGTH_LONG).show()

            // Assign the startDate, endDate, and year to the result
            if (startDateString != null && endDateString != null) {
                selectedData = Pair(Pair(startDateString, endDateString), year)
            }
        }

        // Set up the event for when the cancel button is clicked
        datePicker.addOnNegativeButtonClickListener {
            Toast.makeText(context, "Date Picker Cancelled", Toast.LENGTH_LONG).show()
        }

        // Set up the event for when the back button is pressed
        datePicker.addOnCancelListener {
            Toast.makeText(context, "Date Picker Cancelled", Toast.LENGTH_LONG).show()
        }

        return selectedData
    }

 */
fun getRangeSelectedDateWithYear(
    fragmentManager: FragmentManager,
    onDateRangeSelected: (Pair<Pair<String, String>, Int>?) -> Unit
) {
    val datePicker = MaterialDatePicker.Builder.dateRangePicker().build()




    datePicker.show(fragmentManager, "DatePicker")

    datePicker.addOnPositiveButtonClickListener { selection ->
        val startDate = selection?.first?.let { java.util.Date(it) }
        val endDate = selection?.second?.let { java.util.Date(it) }

        val dateFormat = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
        val dateFormat1 = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
        val startDateString = startDate?.let { dateFormat.format(it) }
        val endDateString = endDate?.let { dateFormat1.format(it) }

        val calendar = java.util.Calendar.getInstance()
        startDate?.let { calendar.time = it }
        val year = calendar.get(java.util.Calendar.YEAR)

        if (startDateString != null && endDateString != null) {
            val selectedData = Pair(Pair(startDateString, endDateString), year)
            onDateRangeSelected(selectedData) // Send selected data to the callback
        } else {
            onDateRangeSelected(null) // Indicate that no valid date range was selected
        }
    }

    datePicker.addOnNegativeButtonClickListener {
        Toast.makeText(context, "Date Picker Cancelled", Toast.LENGTH_LONG).show()
        onDateRangeSelected(null)
    }



    datePicker.addOnCancelListener {
        Toast.makeText(context, "Date Picker Cancelled", Toast.LENGTH_LONG).show()
        onDateRangeSelected(null)
    }
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
        val timeOptions = mutableListOf("30 minutes").apply {
            addAll((1..24).map { "$it hour${if (it > 1) "s" else ""}" })
        }.toTypedArray()

        val numberPicker = NumberPicker(context).apply {
            minValue = 0
            maxValue = timeOptions.size - 1
            displayedValues = timeOptions
            wrapSelectorWheel = true
        }

        AlertDialog.Builder(context)
            .setTitle("Select Time")
            .setView(numberPicker)
            .setPositiveButton("OK") { _, _ ->
                val selectedTime = timeOptions[numberPicker.value]
                onHourSelected(selectedTime) // Pass the selected time back to the caller
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    fun convertTo24HourFormat(time: String): String {
        val inputFormat = SimpleDateFormat("h:mm a", Locale.US)  // 12-hour format with AM/PM
        val outputFormat = SimpleDateFormat("HH:mm", Locale.US)  // 24-hour format

        val date = inputFormat.parse(time)  // Parse the 12-hour formatted time
        return outputFormat.format(date)     // Convert it to 24-hour formatted time
    }




    /*
    fun showHourSelectionDialog(context: Context, onHourSelected: (String) -> Unit) {
//        val numberPicker = NumberPicker(context).apply {
//            minValue = 1
//            maxValue = 24
//            wrapSelectorWheel = true
//        }
//
//        AlertDialog.Builder(context)
//            .setTitle("Select Hours")
//            .setView(numberPicker)
//            .setPositiveButton("OK") { _, _ ->
//                val selectedHour = numberPicker.value
//                val result = "$selectedHour hour${if (selectedHour > 1) "s" else ""}"
//                onHourSelected(result) // Return the selected hour as a string
//            }
//            .setNegativeButton("Cancel", null)
//            .show()


        val timeOptions = mutableListOf("30 minutes").apply {
            addAll((1..24).map { "$it hour${if (it > 1) "s" else ""}" })
        }.toTypedArray()

        val numberPicker = NumberPicker(context).apply {
            minValue = 0
            maxValue = timeOptions.size - 1
            displayedValues = timeOptions
            wrapSelectorWheel = true
        }

        AlertDialog.Builder(context)
            .setTitle("Select Time")
            .setView(numberPicker)
            .setPositiveButton("OK") { _, _ ->
                val selectedTime = timeOptions[numberPicker.value]
               selectedTime // Return the selected time as a string
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

     */

}