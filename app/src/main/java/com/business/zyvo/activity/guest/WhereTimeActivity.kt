package com.business.zyvo.activity.guest

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.business.zyvo.AppConstant
import com.business.zyvo.CircularSeekBar.OnSeekBarChangeListener
import com.business.zyvo.DateManager.DateManager
import com.business.zyvo.ErrorMessage
import com.business.zyvo.LoadingUtils
import com.business.zyvo.R
import com.business.zyvo.adapter.AdapterActivityText
import com.business.zyvo.adapter.AdapterLocationSearch
import com.business.zyvo.databinding.ActivityWhereTimeBinding
import com.business.zyvo.fragment.guest.FullScreenDialogFragment
import com.business.zyvo.model.FilterRequest
import com.business.zyvo.model.SearchFilterRequest
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog
import com.google.gson.Gson
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.LocalTime

class WhereTimeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWhereTimeBinding
    private lateinit var adapterLocationSearch: AdapterLocationSearch
    private var count =0
    private lateinit var displayTextView: RelativeLayout
    private lateinit var displayEditTextView: RelativeLayout
    private lateinit var editText: EditText
    private lateinit var adapterActivitivity : AdapterActivityText
    private lateinit var placesClient: PlacesClient
    private lateinit  var actList : MutableList<String>
    private lateinit var sessionManager: SessionManager
    private var selectedLatitude: Double = 0.0
    private var selectedLongitude: Double = 0.0
    private var date = ""
    private var hour = ""
    private var start_time = ""
    private var end_time = ""
    private var activity = ""
    private var property_price = ""
    private var selectdate = false
    private lateinit var appLocationManager: com.business.zyvo.locationManager.LocationManager
    private var currentMonth: YearMonth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        YearMonth.now()
    } else {
        TODO("VERSION.SDK_INT < O")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private var selectedDate: LocalDate? = LocalDate.now()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWhereTimeBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        Places.initialize(applicationContext, AppConstant.PLACES_INITIALIZE_ID)
        placesClient = Places.createClient(applicationContext)
        actList = mutableListOf()
        sessionManager = SessionManager(this)
        adapterIntialization()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        callingWhereSeach()
        displayTextView.setOnClickListener {
            showEditText()
        }
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
        binding.rlTiming.setOnClickListener {
            if(binding.llTime.visibility == View.GONE){
                binding.llTime.visibility = View.VISIBLE
                selectdate = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    date = selectedDate.toString()
                }
            }
            else{
                binding.llTime.visibility = View.GONE
            }
        }
        binding.circularSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener
        {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(progress: String) {
                try {
                    hour = progress

                }catch (e:Exception){
                    Log.d(ErrorDialog.TAG,e.message!!)
                }
            }

        })

        binding.imgSearch.setOnClickListener {
            if (!binding.text1.text.toString().equals(AppConstant.PM_00_00)){
                start_time   = binding.text1.text.toString()
                start_time = "$date $start_time"

            }else{
                start_time = ""
            }
            if (!binding.text2.text.toString().equals(AppConstant.PM_00_00)){
                Log.d("checkDateFormate",end_time)
                end_time   = binding.text2.text.toString()
                Log.d("checkDateFormate",end_time)
                end_time = "$date $end_time"

            }else{
                end_time = ""
            }
            if (!selectdate){
                date = ""
            }
            property_price = binding.etprice.text.toString()
            val requestData = SearchFilterRequest(
                user_id = sessionManager.getUserId().toString(),
                latitude = selectedLatitude.toString(),
                longitude = selectedLongitude.toString(),
                date = date,
                hour = hour,
                start_time = start_time,
                end_time = end_time,
                location = binding.etSearchLocation.text.toString(),
                activity = activity,
                property_price = property_price)
            sessionManager.setSearchFilterRequest(Gson().toJson(requestData))
            val intent = Intent()
            intent.putExtra(AppConstant.type, AppConstant.FILTER)
            intent.putExtra(AppConstant.SEARCH_REQUEST_DATA,Gson().toJson(requestData))
            setResult(Activity.RESULT_OK, intent)
            finish() // Close the activity
        }

        binding.clearAllBtn.setOnClickListener {
            val requestData = SearchFilterRequest(
                user_id = sessionManager.getUserId().toString(),
                latitude = "",
                longitude = "",
                date = "",
                hour = "",
                start_time = "",
                end_time = "",
                location = "",
                activity = "",
                property_price = "")
            sessionManager.setSearchFilterRequest(Gson().toJson(requestData))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                selectedDate = LocalDate.now()
            }
            // Clear time fields only when date is changed from calendarLayout1

            updateCalendar()
            updateCalendar1()
             selectedLatitude = 0.0
             selectedLongitude = 0.0
               date = ""
               hour = ""
              start_time = ""
              end_time = ""
             activity = ""
            binding.circularSeekBar.endHours = 2f
            binding.text1.text = AppConstant.PM_00_00
            binding.text2.text = AppConstant.PM_00_00
            binding.etSearchLocation.post {
                binding.etSearchLocation.text.clear()
            }
            binding.tvActivityName.text = AppConstant.ACTIVITY_TEXT
            sessionManager.setSearchFilterRequest(Gson().toJson(requestData))
            val intent = Intent()
            intent.putExtra(AppConstant.type,"clearAllBtn")
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        updateCalendar()
        updateCalendar1()
        selectingClickListener()
        bydefaultSelect()
        bydefaultOpenScreen()
        selectTime()
        try {
            setSearchFilterData()
        }catch (e:Exception){
            Log.e(ErrorDialog.TAG,e.message!!)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setSearchFilterData() {
        val filterdata = sessionManager.getSearchFilterRequest()
        if (!filterdata.equals("")){
            val value:SearchFilterRequest = Gson().fromJson(filterdata,SearchFilterRequest::class.java)
            value?.let {
                Log.d(ErrorDialog.TAG,Gson().toJson(value))
                // Set Location values
                binding.textLocationName.setText(it.location)
                binding.etSearchLocation.post {
                    binding.etSearchLocation.setText(it.location)
                }
                selectedLatitude = it.latitude.toDouble()
                selectedLongitude = it.longitude.toDouble()
                if (!it.date.equals("")){
                    val dateString = it.date
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        selectedDate = LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE)
                        updateCalendar()
                        updateCalendar1()
                    }
                }
                if (!it.hour.equals("")){
                    hour = it.hour
                    binding.circularSeekBar.endHours = it.hour.toFloat()
                }
                if (!it.start_time.equals("")){
                    start_time = it.start_time
                    binding.text1.text = ErrorDialog.convertDateToTimeFormat(start_time)
                }
                if (!it.end_time.equals("")){
                    end_time = it.end_time
                    binding.text2.text = ErrorDialog.convertDateToTimeFormat(end_time)
                }
                if (!it.activity.equals("")){
                    activity = it.activity
                    binding.tvActivityName.text = activity
                }
                if (!it.property_price.equals("")){
                    property_price = it.property_price
                    binding.etprice.setText(property_price)
                }
            }
        }
    }

    fun selectTime(){
        binding.rlView1.setOnClickListener {
                DateManager(this).showTimePickerDialog1(this) { selectedTime ->
                    if (binding.text2.text.toString() != AppConstant.PM_00_00 && binding.text2.text.toString().isNotEmpty()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (!isTimeRangeValid(selectedTime, binding.text2.text.toString() )) {
                                LoadingUtils.showErrorDialog(this@WhereTimeActivity, ErrorMessage.SELECT_TIME_RANGE_MINIMUM_2_HOURS)
                                return@showTimePickerDialog1
                            }
                        }
                    }
                    binding.text1.setText(selectedTime)
                    val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
                    } else {
                        TODO("VERSION.SDK_INT < O")
                    }
                    Log.d(ErrorDialog.TAG,selectedTime)

                    val startTime = LocalTime.parse(selectedTime, formatter)

                    Log.d(ErrorDialog.TAG,binding.textTime.text.toString())
                    if (binding.textTime.text.toString().isNotEmpty()){
                        val endTime = startTime.plusHours(binding.textTime.text.toString().replace(" hour","")
                            .toLong())

                        val formattedEndTime = endTime.format(formatter)
                        binding.text2.text = formattedEndTime.uppercase()
                    }
                 }
              }

        binding.rlView2.setOnClickListener {
            DateManager(this).showTimePickerDialog1(this) { selectedTime ->
                // Check if current date is selected and time is in past
                if (selectdate && isCurrentDateSelected() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!isTimeValid(selectedTime)) {
                        // Show error for previous time selection
                        LoadingUtils.showErrorDialog(this@WhereTimeActivity, ErrorMessage.CANNOT_SELECT_PREVIOUS_TIME)
                        return@showTimePickerDialog1
                    }
                }

                // Validate time range when both times are set
                if (binding.text1.text.toString() != AppConstant.PM_00_00  && binding.text1.text.toString().isNotEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (!isTimeRangeValid(binding.text1.text.toString(), selectedTime)) {
                            LoadingUtils.showErrorDialog(this@WhereTimeActivity, ErrorMessage.SELECT_TIME_RANGE_MINIMUM_2_HOURS )
                            return@showTimePickerDialog1
                        }
                    }
                }
                binding.text2.setText(selectedTime)
            }
        }



    }

    private fun bydefaultOpenScreen(){
        intent?.let {
            if(it.hasExtra(AppConstant.TIME)){
                binding.llTime.visibility = View.VISIBLE
                selectdate = true
                binding.rlActivityRecy.visibility = View.GONE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    date = selectedDate.toString()
                }
            }
            else if(it.hasExtra(AppConstant.WHERE)){
                binding.llTime.visibility = View.GONE
                binding.rlActivityRecy.visibility = View.GONE
            }
            else if(it.hasExtra(AppConstant.ACTIVITY)){
                binding.llTime.visibility = View.GONE
                adapterActivitivity.updateAdapter(getActivityData())

                if(binding.rlActivityRecy.visibility == View.VISIBLE){
                    binding.rlActivityRecy.visibility = View.GONE
                }
                else{
                    binding.rlActivityRecy.visibility = View.VISIBLE
                }

            }
        }
    }

    private fun adapterIntialization() {
        adapterActivitivity= AdapterActivityText(this, mutableListOf())
        adapterActivitivity.updateAdapter(getActivityData())
        adapterActivitivity.notifyDataSetChanged()
        adapterLocationSearch = AdapterLocationSearch(this, mutableListOf())
        binding.recyclerLocation.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        binding.recyclerActivity.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        binding.recyclerActivity.adapter = adapterActivitivity
        binding.recyclerLocation.adapter = adapterLocationSearch
        displayTextView = binding.rlWhere
        displayEditTextView = binding.rlTypingView
        editText = binding.etSearchLocation

        adapterActivitivity.setOnItemClickListener(object : AdapterActivityText.onItemClickListener{
            override fun onItemClick(position: Int) {
                binding.tvActivityName.setText(actList.get(position))
                activity = actList.get(position)
               binding.rlActivityRecy.visibility = View.GONE
            }
        })

        binding.rlActivity.setOnClickListener {
            adapterActivitivity.updateAdapter(getActivityData())
            adapterActivitivity.notifyDataSetChanged()

            binding.recyclerActivity.visibility = View.VISIBLE
            if(binding.rlActivityRecy.visibility == View.VISIBLE){
                binding.rlActivityRecy.visibility = View.GONE
            } else if(binding.rlActivityRecy.visibility == View.GONE) {
                binding.rlActivityRecy.visibility = View.VISIBLE
            }
        }
    }

    private fun bydefaultSelect(){
        binding.tvDate.setBackgroundResource(R.drawable.bg_inner_manage_place)
        binding.tvHourly.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tvFlexible.setBackgroundResource(R.drawable.bg_outer_manage_place)
    }


    private fun selectingClickListener(){
        binding.tvDate.setOnClickListener {

            binding.tvDate.setBackgroundResource(R.drawable.bg_inner_manage_place)
            binding.tvHourly.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvFlexible.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.calendarLayout.visibility = View.VISIBLE
            binding.layoutFlexible.visibility = View.GONE
            binding.cv1.visibility = View.GONE
            binding.rlPrice.visibility = View.GONE
            binding.rlActivity.visibility = View.VISIBLE


        }
        binding.tvHourly.setOnClickListener {
            hour = "2"
            binding.tvDate.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvHourly.setBackgroundResource(R.drawable.bg_inner_manage_place)
            binding.tvFlexible.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.calendarLayout.visibility = View.GONE
            binding.layoutFlexible.visibility = View.GONE
            binding.cv1.visibility = View.VISIBLE
            binding.rlPrice.visibility = View.VISIBLE
            binding.rlActivity.visibility = View.GONE
        }
        binding.tvFlexible.setOnClickListener {

            binding.tvDate.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvHourly.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvFlexible.setBackgroundResource(R.drawable.bg_inner_manage_place)

            binding.calendarLayout.visibility = View.GONE
            binding.layoutFlexible.visibility = View.VISIBLE
            binding.cv1.visibility = View.GONE
            binding.rlPrice.visibility = View.GONE
            binding.rlActivity.visibility = View.VISIBLE

        }
        binding.rlActivity.setOnClickListener {
            if(binding.rlActivityRecy.visibility == View.VISIBLE){
                binding.rlActivityRecy.visibility = View.GONE
            } else if(binding.rlActivityRecy.visibility == View.GONE) {
                binding.rlActivityRecy.visibility = View.VISIBLE
            }
        }


    }

    private fun showEditText() {
        displayTextView.visibility = View.GONE
        Log.d("TESTING_ZYVOO", "Show Edit text View")
        editText.visibility = View.VISIBLE
        binding.rlTypingView.visibility = View.VISIBLE
        editText.requestFocus() // Focus on the EditText
        // Set the existing text if needed
    }


    private fun handleEditTextInput() {
        val input = editText.text.toString().trim()
        if (input.isEmpty()) {
            // If the input is empty, revert to the original view
            editText.visibility = View.GONE
            displayTextView.visibility = View.VISIBLE
        } else {
            // If there's input, update the TextView
            editText.visibility = View.VISIBLE
            displayTextView.visibility = View.GONE
        }
    }


    private fun callingWhereSeach() {
        appLocationManager = com.business.zyvo.locationManager.LocationManager(this, this)

        appLocationManager.autoCompleteLocationWork(binding.etSearchLocation)

        binding.etSearchLocation.setOnItemClickListener { parent, _, position, _ ->
            val selectedLocation = parent.getItemAtPosition(position) as String

            // Fetch location details
            appLocationManager.fetchPlaceDetails(selectedLocation) { latitude, longitude ->
                selectedLatitude = latitude
                selectedLongitude = longitude
                Log.d("FilterActivity", "Selected Location: Lat=$latitude, Lng=$longitude")
            }
        }
    }

    private fun fetchAutocompleteSuggestions(query: String, context: Context) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            val suggestions =
                response.autocompletePredictions.map { it.getPrimaryText(null).toString() }
            Log.d(
                "TESTING_ZYVOO_LOCATION",
                "Suggestions For Location :- " + suggestions.size.toString()
            )

            adapterLocationSearch.updateAdapter(suggestions.toMutableList())

            adapterLocationSearch.setOnItemClickListener { selectedLocation ->
                binding.textLocationName.text = "$selectedLocation"
                binding.rlLocation.visibility = View.GONE
                binding.rlTypingView.visibility =View.GONE
                binding.rlWhere.visibility =View.VISIBLE
                binding.etSearchLocation.clearFocus()
                binding.etSearchLocation.setText("")
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateCalendar() {
        // Updates the calendar layout with the current and next month views.
        val calendarLayout = binding.calendarLayout
        calendarLayout.removeAllViews()
        val topMonths = mutableListOf<YearMonth>()
        val bottomMonths = mutableListOf<YearMonth>()

        // Separate months into top and bottom lists
        val allMonths = (1..12).map { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            currentMonth.plusMonths(it.toLong())
        } else {
            TODO("VERSION.SDK_INT < O")
        }

        }
        allMonths.forEachIndexed { index, month ->
            if (index % 2 == 0) {
                topMonths.add(month)
            }
        }

        addMonthView(calendarLayout, currentMonth)
    }

    private fun updateCalendar1() {
        val calendarLayout1 = binding.calendarLayout1
        calendarLayout1.removeAllViews()
        val topMonths = mutableListOf<YearMonth>()
        val bottomMonths = mutableListOf<YearMonth>()

        // Separate months into top and bottom lists
        val allMonths = (1..12).map { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            currentMonth.plusMonths(it.toLong())
        } else {
            TODO("VERSION.SDK_INT < O")
        }

        }
        allMonths.forEachIndexed { index, month ->
            if (index % 2 == 0) {
                topMonths.add(month)

            }
        }

        addMonthView(calendarLayout1, currentMonth)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun addMonthView(parentLayout: LinearLayout, yearMonth: YearMonth) {
         val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(3, 3, 3, 3)
        val monthView = layoutInflater.inflate(R.layout.calendar_month, parentLayout, false)

        val monthTitle = monthView.findViewById<TextView>(R.id.month_title)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            monthTitle.text = "${yearMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }}"
            //${yearMonth.year}"
        }

        val daysLayout = monthView.findViewById<LinearLayout>(R.id.days_layout)
        daysLayout.removeAllViews()
        listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
            val dayView = layoutInflater.inflate(R.layout.calendar_day_name, daysLayout, false) as TextView
            dayView.text = day
            daysLayout.addView(dayView)
        }

        val weeksLayout = monthView.findViewById<LinearLayout>(R.id.weeks_layout)
        weeksLayout.removeAllViews()
        val weeks = generateCalendarWeeks(yearMonth)
        weeks.forEach { week ->
            val weekLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            week.forEach { date1 ->
                val dateView = layoutInflater.inflate(R.layout.calendar_day, weekLayout, false) as TextView
                if (date1 != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // dateView.text = date.dayOfMonth.toString()
                        dateView.text = date1.dayOfMonth.toString().padStart(2, '0')

                    }
                    dateView.setOnClickListener {
                        val previousSelectedDate = selectedDate
                        selectedDate = date1
                        if(!SessionManager(this).isDateGreaterOrEqual(selectedDate.toString())){
                           LoadingUtils.showErrorDialog(this@WhereTimeActivity, ErrorMessage.CANNOT_SELECT_PAST_DATE )
                           return@setOnClickListener
                        }

                        Log.d("TESTING_DATE",selectedDate.toString())
                        if (previousSelectedDate != selectedDate) {
                            clearTimeFields()
                        }
                        updateCalendar()
                        updateCalendar1()
                        // Toast.makeText(requireContext(), "Selected Date: ${date.dayOfMonth} ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.year}", Toast.LENGTH_SHORT).show()
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        when (date1) {
                            selectedDate -> {
                                dateView.setBackgroundResource(R.drawable.current_bg_date)
                                date = selectedDate.toString()
                            }
                            else -> dateView.setBackgroundResource(R.drawable.date_bg2)
                        }
                    }
                    dateView.setTextColor(
                        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                date1.month == yearMonth.month
                            } else {
                                TODO("VERSION.SDK_INT < O")
                            }
                        ) Color.BLACK
                        else Color.GRAY
                    )
                }
                weekLayout.addView(dateView)
            }
            weeksLayout.addView(weekLayout)
        }
        // Display llPreviousAndNextMonth only for the current month
        if (yearMonth == currentMonth) {
            val previous = monthView.findViewById<ImageButton>(R.id.button_previous)
            previous.setOnClickListener {
                currentMonth = currentMonth.minusMonths(1)
                updateCalendar()
                updateCalendar1()
            }
            val next = monthView.findViewById<ImageButton>(R.id.button_next)
            next.setOnClickListener {
                currentMonth = currentMonth.plusMonths(1)
                updateCalendar()
                updateCalendar1()
            }
            parentLayout.addView(monthView)
        }
    }

    private fun generateCalendarWeeks(yearMonth: YearMonth): List<List<LocalDate?>> {
        // Generates a list of weeks, each containing dates for the specified month.
        val weeks = ArrayList<List<LocalDate?>>()
        var week = ArrayList<LocalDate?>()

        val firstDayOfMonth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            yearMonth.atDay(1).dayOfWeek.value % 7
        } else {
            TODO("VERSION.SDK_INT < O")
        }

        // Add null values for the days before the start of the month
        for (i in 0 until firstDayOfMonth) {
            week.add(null)
        }

        val daysInMonth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            yearMonth.lengthOfMonth()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        for (day in 1..daysInMonth) {
            week.add(yearMonth.atDay(day))
            if (week.size == 7) {
                weeks.add(week)
                week = ArrayList()
            }
        }

        // Add null values for the days after the end of the month
        while (week.size < 7) {
            week.add(null)
        }
        if (week.isNotEmpty()) {
            weeks.add(week)
        }
        return weeks
    }


    private fun getActivityData() :MutableList<String>{
        actList = mutableListOf<String>()
        actList.add(AppConstant.STAYS)
        actList.add(AppConstant.EVENT_SPACE)
        actList.add(AppConstant.PHOTO_SHOOT_TEXT)
        actList.add(AppConstant.MEETING)
        actList.add(AppConstant.PARTY)
        actList.add(AppConstant.POOL)
        actList.add(AppConstant.FILM_SHOOT)
        actList.add(AppConstant.WEDDING)

        return actList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isTimeValid(selectedTime: String): Boolean {
        try {
            val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
            val currentTime = LocalTime.now()
            val selectedLocalTime = LocalTime.parse(selectedTime, formatter)

            return !selectedLocalTime.isBefore(currentTime)
        } catch (e: Exception) {
            Log.e(ErrorDialog.TAG, "Time parsing error: ${e.message}")
            return true // Default to valid if parsing fails
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun isCurrentDateSelected(): Boolean {
        return selectedDate?.equals(LocalDate.now()) == true
    }
    @SuppressLint("SetTextI18n")
    private fun clearTimeFields() {
        binding.text1.text = AppConstant.PM_00_00
        binding.text2.text = AppConstant.PM_00_00
        start_time = ""
        end_time = ""
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isTimeRangeValid(startTime: String, endTime: String): Boolean {
        return try {
            val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

            val start = LocalTime.parse(startTime, formatter)
            val end = LocalTime.parse(endTime, formatter)

            // Calculate duration between start and end time
            val duration = java.time.Duration.between(start, end)

            // Check if duration is at least 2 hours
            duration.toHours() >= 2
        } catch (e: Exception) {
            Log.e(ErrorDialog.TAG, "Time range validation error: ${e.message}")
            false
        }
    }
}