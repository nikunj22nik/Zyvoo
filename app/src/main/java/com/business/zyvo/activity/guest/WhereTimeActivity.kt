package com.business.zyvo.activity.guest

import android.annotation.SuppressLint
import android.content.Context
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
import com.business.zyvo.DateManager.DateManager
import com.business.zyvo.R
import com.business.zyvo.adapter.AdapterActivityText
import com.business.zyvo.adapter.AdapterLocationSearch
import com.business.zyvo.databinding.ActivityWhereTimeBinding
import com.business.zyvo.fragment.guest.FullScreenDialogFragment
import java.time.LocalDate
import java.time.YearMonth

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
    private var currentMonth: YearMonth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        YearMonth.now()
    } else {
        TODO("VERSION.SDK_INT < O")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private var selectedDate: LocalDate? = LocalDate.now()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWhereTimeBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        Places.initialize(applicationContext, "AIzaSyC9NuN_f-wESHh3kihTvpbvdrmKlTQurxw")
        placesClient = Places.createClient(applicationContext)
        actList = mutableListOf()
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

   // Optional: Handle when the EditText loses focus
   //        editText.setOnFocusChangeListener { _, hasFocus ->
   //            if (!hasFocus) {
    //                handleEditTextInput()
    //            }
    //
   //
   //        }

        binding.imageBack.setOnClickListener {
            onBackPressed()
        }

        binding.rlTiming.setOnClickListener {
            if(binding.llTime.visibility == View.GONE){
                binding.llTime.visibility = View.VISIBLE
            }
            else{
                binding.llTime.visibility = View.GONE
            }
        }


        binding.imgSearch.setOnClickListener {
            count++;

            if(count %2==1) {
                val dialog = FullScreenDialogFragment()
                dialog.show(supportFragmentManager, "FullScreenDialog")
            }

            else{
                onBackPressed()
            }
        }

        updateCalendar()
        updateCalendar1()
        selectingClickListener()
        bydefaultSelect()
        bydefaultOpenScreen()
        selectTime()
    }

    fun selectTime(){
        binding.rlView1.setOnClickListener {
            if(binding.text1.text.toString().equals("3 hour")){
                DateManager(this).showHourSelectionDialog(this) { selectedHour ->
                    binding.text1.setText(selectedHour.toString())
                }
            }
            else{
                DateManager(this).showTimePickerDialog(this) { selectedTime ->
                    binding.text1.setText(selectedTime.toString())
                }
            }
        }
        binding.rlView2.setOnClickListener {
            if(binding.text2.text.toString().equals("$30")){

            }else{
                DateManager(this).showTimePickerDialog(this) { selectedTime ->
                    binding.text2.setText(selectedTime.toString())
                }
            }
        }
    }

    private fun bydefaultOpenScreen(){
        intent?.let {
            if(it.hasExtra(AppConstant.TIME)){
                binding.llTime.visibility = View.VISIBLE
                binding.rlActivityRecy.visibility = View.GONE
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
              //  binding.rlActivityRecy.visibility = View.VISIBLE
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

        }
        binding.tvHourly.setOnClickListener {

            binding.tvDate.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvHourly.setBackgroundResource(R.drawable.bg_inner_manage_place)
            binding.tvFlexible.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.calendarLayout.visibility = View.GONE
            binding.layoutFlexible.visibility = View.GONE
            binding.cv1.visibility = View.VISIBLE
        }
        binding.tvFlexible.setOnClickListener {

            binding.tvDate.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvHourly.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvFlexible.setBackgroundResource(R.drawable.bg_inner_manage_place)

            binding.calendarLayout.visibility = View.GONE
            binding.layoutFlexible.visibility = View.VISIBLE
            binding.cv1.visibility = View.GONE
        }
        binding.rlActivity.setOnClickListener {
//            adapterActivitivity.updateAdapter(getActivityData())
//            adapterActivitivity.notifyDataSetChanged()
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
        binding.etSearchLocation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                s?.let {
                    if (it.isNotEmpty()) {
                        binding.rlLocation.visibility = View.VISIBLE
                        binding.rlTypingView.visibility =View.VISIBLE
                        binding.rlWhere.visibility =View.GONE
                        fetchAutocompleteSuggestions(it.toString(), applicationContext)
                    } else {
                        binding.rlLocation.visibility = View.GONE
                        binding.rlTypingView.visibility =View.GONE
                        binding.rlWhere.visibility =View.VISIBLE
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
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
            // val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, suggestions)
            adapterLocationSearch.updateAdapter(suggestions.toMutableList())

            adapterLocationSearch.setOnItemClickListener { selectedLocation ->
                binding.textLocationName.text = "$selectedLocation"
               // Toast.makeText(context, "Selected Location: $selectedLocation", Toast.LENGTH_SHORT).show()
                binding.rlLocation.visibility = View.GONE
                binding.rlTypingView.visibility =View.GONE
                binding.rlWhere.visibility =View.VISIBLE
binding.etSearchLocation.clearFocus()
                binding.etSearchLocation.setText("")
            }
            //adapter.notifyDataSetChanged()
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
//            } else {
//                bottomMonths.add(month)
            }
        }

        addMonthView(calendarLayout, currentMonth)
        // addMonthView(calendarLayout, currentMonth.plusMonths(1))
    }

    private fun updateCalendar1() {
//        // Updates the calendar layout with the current and next month views.
//        val calendarLayout = binding.calendarLayout1
//        calendarLayout.removeAllViews()
//        val topMonths = mutableListOf<YearMonth>()
//        val bottomMonths = mutableListOf<YearMonth>()
//
//        // Separate months into top and bottom lists
//        val allMonths = (1..12).map { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            currentMonth.plusMonths(it.toLong())
//        } else {
//            TODO("VERSION.SDK_INT < O")
//        }
//
//        }
//        allMonths.forEachIndexed { index, month ->
//            if (index % 2 == 0) {
//                topMonths.add(month)
////            } else {
////                bottomMonths.add(month)
//            }
//        }
//
//        addMonthView(calendarLayout, currentMonth)
//        // addMonthView(calendarLayout, currentMonth.plusMonths(1))



        // Updates the calendar layout with the current and next month views.
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
//            } else {
//                bottomMonths.add(month)
            }
        }

        addMonthView(calendarLayout1, currentMonth)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun addMonthView(parentLayout: LinearLayout, yearMonth: YearMonth) {
        // Adds a view for the specified month to the parent layout.

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(10, 10, 10, 10)
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
            week.forEach { date ->
                val dateView = layoutInflater.inflate(R.layout.calendar_day, weekLayout, false) as TextView
                if (date != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // dateView.text = date.dayOfMonth.toString()
                        dateView.text = date.dayOfMonth.toString().padStart(2, '0')

                    }
                    dateView.setOnClickListener {
                        selectedDate = date
                        updateCalendar()
                        updateCalendar1()
                        // Toast.makeText(requireContext(), "Selected Date: ${date.dayOfMonth} ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.year}", Toast.LENGTH_SHORT).show()
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        when (date) {
//                            LocalDate.now() -> dateView.setBackgroundResource(R.drawable.current_bg_date)
//                          //  selectedDate -> dateView.setBackgroundResource(R.drawable.selected_bg)
//
//                            else -> dateView.setBackgroundResource(android.R.color.transparent)
//                        }

                        when (date) {
                            //LocalDate.now() -> dateView.setBackgroundResource(R.drawable.current_bg_date)
                            selectedDate -> dateView.setBackgroundResource(R.drawable.current_bg_date)
                            else -> dateView.setBackgroundResource(R.drawable.date_bg)
                        }
                    }
                    dateView.setTextColor(
                        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                date.month == yearMonth.month
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
        } else {
            // If not the current month, hide the previous and next buttons
//            val llPreviousAndNextMonth = monthView.findViewById<LinearLayout>(R.id.llPreviousAndNextMonth)
//            llPreviousAndNextMonth.visibility = View.GONE
//            parentLayout.addView(monthView)
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
        actList.add("Stays")
        actList.add("Event Space")
        actList.add("Photo Shoot")
        actList.add("Music Video")
        actList.add("Wedding")

        return actList
    }


}