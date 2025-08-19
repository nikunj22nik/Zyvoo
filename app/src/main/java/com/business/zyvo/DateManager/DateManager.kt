package com.business.zyvo.DateManager

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Log
import android.widget.NumberPicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import com.business.zyvo.R
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class DateManager(var context: Context) {

    private val months = arrayOf(
        "January", "February", "March", "April",
        "May", "June", "July", "August",
        "September", "October", "November", "December"
    )

    fun getHoursAndMinutes(timeString: String): String {
        // Split the time string using ':' as the separator
        val timeParts = timeString.split(":")

        // Return the hours and minutes part (ignoring seconds)
        return "${timeParts[0]}:${timeParts[1]}"
    }

    fun convert24HourToAMPM(time24: String): String {
        val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())  // 24-hour format
        val outputFormat =
            SimpleDateFormat("h:mm a", Locale.getDefault())  // 12-hour format with AM/PM
        val date: Date = inputFormat.parse(time24)  // Parse the input time
        return outputFormat.format(date)  // Format the date to 12-hour format
    }


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

        val formatter24Hour = DateTimeFormatter.ofPattern("HH:mm") // 24-hour format
        val formatter12Hour = DateTimeFormatter.ofPattern("hh:mm a") // 12-hour format (AM/PM)

        // Trim any extra spaces from the input time strings
        val trimmedFromTime = fromTime.trim()
        val trimmedToTime = toTime.trim()

        // Declare LocalTime variables
        val from: LocalTime
        val to: LocalTime

        try {
            // Try parsing "from" time in 12-hour format (AM/PM)
            from =
                if (trimmedFromTime.contains("AM", true) || trimmedFromTime.contains("PM", true)) {
                    LocalTime.parse(trimmedFromTime, formatter12Hour)
                } else {
                    // Otherwise, try 24-hour format
                    LocalTime.parse(trimmedFromTime, formatter24Hour)
                }

            // Try parsing "to" time in 12-hour format (AM/PM)
            to = if (trimmedToTime.contains("AM", true) || trimmedToTime.contains("PM", true)) {
                LocalTime.parse(trimmedToTime, formatter12Hour)
            } else {
                // Otherwise, try 24-hour format
                LocalTime.parse(trimmedToTime, formatter24Hour)
            }

            // If no exception is thrown, compare times
            return from.isBefore(to)

        } catch (e: DateTimeParseException) {
            // Catch and handle parsing errors if the time format is not valid

            Log.d("TESTING_ZYVOO", "Inside the error of time")
            return false // You can return false or handle it according to your needs
        }
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
        val startOfWeek =
            today.minusDays(today.dayOfWeek.ordinal.toLong()) // Get Monday of the current week
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return DayOfWeek.values().mapIndexed { index, dayOfWeek ->
            val date = startOfWeek.plusDays(index.toLong())
            val formattedDate = date.format(formatter)
            val shortDayName =
                dayOfWeek.name.take(3) // Get the first three characters of the day name
            "${dayOfWeek.ordinal + 1} - " + "${shortDayName}"
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

            //val dateFormat = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
            //val dateFormat1 = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
            val dateFormat = java.text.SimpleDateFormat("MMM dd yyyy", java.util.Locale.getDefault())
            val dateFormat1 = java.text.SimpleDateFormat("MMM dd yyyy", java.util.Locale.getDefault())
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
           // Toast.makeText(context, "Date Picker Cancelled", Toast.LENGTH_LONG).show()
            onDateRangeSelected(null)
        }



        datePicker.addOnCancelListener {
       //     Toast.makeText(context, "Date Picker Cancelled", Toast.LENGTH_LONG).show()
            onDateRangeSelected(null)
        }
    }


    fun getMonthNumber(monthName: String): Int? {
        return months.indexOf(monthName).takeIf { it >= 0 }?.plus(1)
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

    fun showTimePickerDialog1(context: Context, onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            context,
            android.R.style.Theme_Holo_Light_Dialog_MinWidth,
            { _, hourOfDay, minute1 ->
                var formattedHour = hourOfDay
                var period = "AM"

                if (hourOfDay >= 12) {
                    period = "PM"
                    if (hourOfDay > 12) {
                        formattedHour = hourOfDay % 12
                    }
                } else if (hourOfDay == 0) {
                    formattedHour = 12 // Midnight edge case
                }

                //   val formattedTime = "$formattedHour:${convertDate(minute1)} $period"
                //   val formattedTime = String.format("%02d:%02d %s", formattedHour, minute1, period)
                val formattedTime = "${convertDate(formattedHour)}:${convertDate(minute1)} $period"


                // ✅ Send selected time to caller
                onTimeSelected(formattedTime)

            },
            hour,
            minute,
            false
        )

        timePickerDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        timePickerDialog.show()
    }

    fun convertDate(minute: Int): String {
        return if (minute < 10) "0$minute" else "$minute"
    }


    fun showHourSelectionDialog(context: Context, onHourSelected: (String) -> Unit) {
        val timeOptions = mutableListOf("2 hours").apply {
            addAll((3..24).map { "$it hour${if (it >= 3) "s" else ""}" })
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
    fun getTimeDifferenceInHrFormat(startTime: String, endTime: String): String {
        val inputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        return try {
            val startDate = inputFormat.parse(startTime)
            val endDate = inputFormat.parse(endTime)

            if (startDate != null && endDate != null) {
                var diffInMillis = endDate.time - startDate.time

                if (diffInMillis < 0) {
                  //  return ""
                    diffInMillis += 24 * 60 * 60 * 1000 // add 24 hours
                }

                val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis) % 60

               return hours.toString()
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
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

    fun selectDateManager(onDateSelected: (String) -> Unit) {
        val today = Calendar.getInstance()
        val c = Calendar.getInstance()

        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        // Create DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            context, R.style.DialogTheme,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format the selected date with leading zeros for month and day
                val formattedDate =
                    String.format("%02d-%02d-%04d", selectedMonth + 1, selectedDay, selectedYear)
                onDateSelected(formattedDate) // Pass the selected date to the callback
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
           /* year,   // ✅ Year first
            month,  // ✅ Then month
            day     // ✅ Then day*/
            /*  month,
              day,
              year*/
        )
      /*  // ✅ Set max date to today (no future dates)
        datePickerDialog.datePicker.maxDate = today.timeInMillis
        // Set minimum year to 100 years ago (e.g., 1925 if current year is 2025)
        val minCalendar = Calendar.getInstance()
        minCalendar.set(1900, 0, 1) // Jan 1, (year - 100)
        // 🔒 Disable past dates
        datePickerDialog.datePicker.minDate = c.timeInMillis*/

        // Set min date to Jan 1, 1900
        val minDate = Calendar.getInstance()
        minDate.set(1900, Calendar.JANUARY, 1)

        // ❗ Set the allowed range: 1900 - today
        datePickerDialog.datePicker.minDate = minDate.timeInMillis
        datePickerDialog.datePicker.maxDate = today.timeInMillis

        // Restrict date range to only past years (birth year style)
      //  datePickerDialog.datePicker.minDate = minCalendar.timeInMillis
        datePickerDialog.show() // Show the dialog
    }

}