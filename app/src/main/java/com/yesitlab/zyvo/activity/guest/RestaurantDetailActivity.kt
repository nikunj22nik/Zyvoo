package com.yesitlab.zyvo.activity.guest

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.yesitlab.zyvo.ObservableTextView
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.adapter.AdapterAddOn
import com.yesitlab.zyvo.adapter.guest.AdapterReview
import com.yesitlab.zyvo.databinding.ActivityRestaurantDetailBinding
import java.time.LocalDate
import java.time.YearMonth

class RestaurantDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var binding :ActivityRestaurantDetailBinding

    lateinit var adapterAddon :AdapterAddOn

    lateinit var adapterReview: AdapterReview

    private lateinit var mapView: MapView

    private var mMap: GoogleMap? = null

    private var currentMonth: YearMonth =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
           YearMonth.now()
        } else {
            TODO("VERSION.SDK_INT < O")
        }

    @RequiresApi(Build.VERSION_CODES.O)
    private var selectedDate: LocalDate? = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRestaurantDetailBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        initialization()

        updateCalendar()
    }

    fun initialization(){
        adapterAddon = AdapterAddOn(this,getAddOnList().subList(0,4))
        adapterReview = AdapterReview(this, mutableListOf())
        binding.tvReadMoreLess.setCollapsedText("Read More")
        binding.tvReadMoreLess.setCollapsedTextColor(com.yesitlab.zyvo.R.color.green_color_bar)
        binding.recyclerAddOn.adapter = adapterAddon
        binding.recyclerAddOn.layoutManager = GridLayoutManager(this,2)
        binding.recyclerAddOn.isNestedScrollingEnabled = false
        binding.recyclerReviews.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        binding.recyclerReviews.isNestedScrollingEnabled = false
        binding.recyclerReviews.adapter = adapterReview
        val textView = binding.tvShowMore
        textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.tvLocationName.paintFlags = binding.tvLocationName.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.tvShowMore.setOnClickListener {
          binding.tvShowMore.visibility = View.GONE
          adapterAddon.updateAdapter(getAddOnList())
        }

        binding.hoursTextView.setOnTextChangedListener(object : ObservableTextView.OnTextChangedListener {
            override fun onTextChanged(newText: String) {
                // This is called whenever the text changes
                binding.text1.setText(newText+" hour")
            }
        })

        clickListeners()
    }

    private fun clickListeners(){
        binding.tvDay.setBackgroundResource(R.drawable.bg_inner_manage_place)
        binding.tvHour.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.showMoreReview.setOnClickListener {
            adapterReview.updateAdapter(7)
        }
        binding.text1.setText("01:00 PM")
        binding.text2.setText("03:00 PM")
        binding.tvDay.setOnClickListener {
            binding.tvDay.setBackgroundResource(R.drawable.bg_inner_manage_place)
            binding.tvHour.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.calendarLayout.visibility = View.VISIBLE
            binding.rlCircularProgress.visibility = View.GONE
            binding.text1.setText("01:00 PM")
            binding.text2.setText("03:00 PM")
        }

        binding.tvHour.setOnClickListener {
            binding.tvDay.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvHour.setBackgroundResource(R.drawable.bg_inner_manage_place)
            binding.calendarLayout.visibility = View.GONE
            binding.rlCircularProgress.visibility = View.VISIBLE
            binding.text1.setText("3 hour")
            binding.text2.setText("$30")
        }

    }

    private fun  getAddOnList() :  MutableList<String>{
        var list = mutableListOf<String>()
        list.add("Computer Screen")
        list.add("Bed Sheets")
        list.add("Phone charger")
        list.add("Ring Light")
        list.add("Left Light")
        list.add("Water Bottle")
        return list
    }

    private fun shareText(text: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("text/plain")
        shareIntent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()  // Important to call in onResume
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()  // Important to call in onPause
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()  // Important to call in onDestroy
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()  // Important to call in onLowMemory
    }

    override fun onMapReady(p0: GoogleMap) {

        mMap = p0
        val newYork = LatLng(40.7128, -74.0060)
        mMap?.addMarker(MarkerOptions().position(newYork).title("Marker in New York"))
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(newYork, 10f))

    }

    private fun updateCalendar() {
        // Updates the calendar layout with the current and next month views.
        val calendarLayout = binding.calendarLayout
        calendarLayout.removeAllViews()
        val topMonths = mutableListOf<YearMonth>()
        val bottomMonths = mutableListOf<YearMonth>()

        // Separate months into top and bottom lists
        val allMonths = (1..12).map {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            currentMonth.plusMonths(it.toLong())
        }
            else {
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

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun addMonthView(parentLayout: LinearLayout, yearMonth: YearMonth) {
        // Adds a view for the specified month to the parent layout.
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
            }
            val next = monthView.findViewById<ImageButton>(R.id.button_next)
            next.setOnClickListener {
                currentMonth = currentMonth.plusMonths(1)
                updateCalendar()
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

 }