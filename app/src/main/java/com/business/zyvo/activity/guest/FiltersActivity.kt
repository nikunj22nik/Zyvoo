package com.business.zyvo.activity.guest

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.github.mikephil.charting.data.BarEntry
import com.google.android.libraries.places.api.net.PlacesClient
import com.business.zyvo.DateManager.DateManager
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.adapter.guest.ActivitiesAdapter
import com.business.zyvo.adapter.guest.AmenitiesAdapter
import com.business.zyvo.databinding.ActivityFiltersBinding
import com.business.zyvo.fragment.guest.FullScreenDialogFragment
import com.business.zyvo.fragment.guest.home.viewModel.FilterViewModel
import com.business.zyvo.locationManager.LocationManager
import com.business.zyvo.model.ActivityModel
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.NetworkMonitorCheck
import com.business.zyvo.utils.PrepareData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
@AndroidEntryPoint
class FiltersActivity : AppCompatActivity(), AmenitiesAdapter.onItemClickListener , View.OnClickListener {

    lateinit var binding: ActivityFiltersBinding
    private lateinit var selectedItemTextView: TextView
    private lateinit var popupWindow: PopupWindow
    private val items = listOf("1 Hour", "2 Hour", "3 Hour", "4 Hour", "5 Hour")
    private lateinit var placesClient: PlacesClient
    private val filterViewModel: FilterViewModel by lazy {
        ViewModelProvider(this)[FilterViewModel::class.java]
    }
    private lateinit var autocompleteTextView: AutoCompleteTextView
    private lateinit var locationManager: LocationManager
    private lateinit var activityList : MutableList<ActivityModel>
    private lateinit var amenitiesList :MutableList<Pair<String,Boolean>>
    private lateinit var adapterActivity :ActivitiesAdapter
    private lateinit var adapterActivity2 :ActivitiesAdapter
    private lateinit var amenitiesAdapter :AmenitiesAdapter
    private lateinit var languageAdapter:AmenitiesAdapter
    private lateinit var dateManager :DateManager
    private var selectedOption: String? = null
    private var availOption = "any"
    private var propertySize: String = "any"
    private var bedroomCount: String = "any"
    private var bathroomCount: String = "any"
    private var instantBookingCount = 0
    private var selfCheckIn = 0
    private var petCheckIn = 0
    private var selectedAmenities = mutableListOf<String>()
    private var selectedActivities = mutableListOf<ActivityModel>()
    private var selectedActivityName = listOf<String>()
    private var selectedLanguages = listOf<String>()
    private lateinit var sessionManager: SessionManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFiltersBinding.inflate(LayoutInflater.from(this))
        dateManager = DateManager(this)
        setContentView(binding.root)
        settingDataToActivityModel()
        adapterActivity = ActivitiesAdapter(this,activityList.subList(0,3))
        adapterActivity2 = ActivitiesAdapter(this,activityList.subList(3,activityList.size))
        amenitiesAdapter = AmenitiesAdapter(this, mutableListOf())
        languageAdapter = AmenitiesAdapter(this, mutableListOf())
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        selectedItemTextView = binding.tvHour
        settingDataToActivityModel()
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.llLocation, InputMethodManager.SHOW_IMPLICIT)

        clickListenerCalls()
        callingPriceRangeGraphSelection()
        setUpRecyclerView()

        val locationManager = LocationManager(this,this)
        locationManager.autoCompleteLocationWork(binding.autocompleteLocation)


        byDefaultSelectAvailability()
        settingBackgroundTaskToPeople()
        settingBackgroundTaskToProperty()
        settingBackgroundTaskToParking()
        settingBackgroundTaskToBedroom()
        settingBackgroundTaskToBathroom()
        binding.allowPets.setOnClickListener {
            showPopupWindowForPets(it)
        }
        showingMoreText()
        showingMoreAmText()

    }

    private fun showingMoreText() {
        val text = "Show More"
        val spannableString = SpannableString(text).apply {
            setSpan(UnderlineSpan(), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.underlinedTextView.text = spannableString
        binding.underlinedTextView.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        binding.underlinedTextView.paint.isAntiAlias = true
        binding.underlinedTextView.setOnClickListener {
            languageAdapter.updateAdapter(getNationalLanguages())
           // binding.underlinedTextView.visibility =View.GONE
            showingLessText()
        }
    }

    private fun showingLessText() {
        val text = "Show Less"
        val spannableString = SpannableString(text).apply {
            setSpan(UnderlineSpan(), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.underlinedTextView.text = spannableString
        binding.underlinedTextView.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        binding.underlinedTextView.paint.isAntiAlias = true
        binding.underlinedTextView.setOnClickListener {
            languageAdapter.updateAdapter(getNationalLanguages().subList(0,6))
            showingMoreText()
        }
    }

    private fun showingMoreAmText() {
        val text = "Show More"
        val spannableString = SpannableString(text).apply {
            setSpan(UnderlineSpan(), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.underlinedTextView1.text = spannableString
        binding.underlinedTextView1.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        binding.underlinedTextView1.paint.isAntiAlias = true
        binding.underlinedTextView1.setOnClickListener {
            amenitiesAdapter.updateAdapter(amenitiesList)
            // binding.underlinedTextView.visibility =View.GONE
            showingLessAmText()
        }
    }

    private fun showingLessAmText() {
        val text = "Show Less"
        val spannableString = SpannableString(text).apply {
            setSpan(UnderlineSpan(), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.underlinedTextView1.text = spannableString
        binding.underlinedTextView1.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        binding.underlinedTextView1.paint.isAntiAlias = true
        binding.underlinedTextView1.setOnClickListener {
            amenitiesAdapter.updateAdapter(amenitiesList.subList(0,6))
            showingMoreAmText()
        }

    }

    private fun settingBackgroundTaskToBedroom() {
        val bedroomOptions = listOf(
            binding.tvAnyBedrooms to "any",
            binding.tv1Bedroom to "1",
            binding.tv2Bedroom to "2",
            binding.tv3Bedroom to "3",
            binding.tv4Bedroom to "4",
            binding.tv5Bedroom to "5",
            binding.tv7Bedroom to "7",
            binding.tv8Bedroom to "8"
        )

        bedroomOptions.forEach { (textView, value) ->
            textView.setOnClickListener {
                clearSelections(bedroomOptions)
                textView.setBackgroundResource(R.drawable.bg_inner_select_white)
                bedroomCount = value
                binding.etCustomBedroom.text.clear()

                Log.d("bedroomCount", "Selected: $bedroomCount")
            }
        }
        binding.etCustomBedroom.setOnClickListener {
            clearSelections(bedroomOptions)
        }
        binding.etCustomBedroom.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input.isNotEmpty()) {
                    bedroomCount = input
                    Log.d("bedroomCount", "Manual Input: $bedroomCount")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun settingBackgroundTaskToBathroom(){

        val bathroomOptions = listOf(
            binding.tvAnyBathroom to "any",
            binding.tv1Bathrooms to "1",
            binding.tv2Bathroom to "2",
            binding.tv3Bathroom to "3",
            binding.tv4Bathroom to "4",
            binding.tv5Bathroom to "5",
            binding.tv7Bathroom to "7",
            binding.tv8Bathroom to "8",
        )
        bathroomOptions.forEach { (textView, value) ->
            textView.setOnClickListener {
                clearSelections(bathroomOptions)
                textView.setBackgroundResource(R.drawable.bg_inner_select_white)
                bathroomCount = value
                binding.etBathroom.text.clear()

                Log.d("bathroomCount", "Selected: $bathroomCount")
            }
        }

        binding.etBathroom.setOnClickListener {
            clearSelections(bathroomOptions)
        }

        binding.etBathroom.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input.isNotEmpty()) {
                    bathroomCount = input
                    Log.d("bathroomCount", "Manual Input: $bathroomCount")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun settingBackgroundTaskToProperty() {
        val propertyOptions = listOf(
            binding.tvAny to "any",
            binding.tv250 to "250",
            binding.tv350 to "350",
            binding.tv450 to "450",
            binding.tv550 to "550",
            binding.tv650 to "650",
            binding.tv750 to "750"
        )

        propertyOptions.forEach { (textView, value) ->
            textView.setOnClickListener {
                clearSelections(propertyOptions)
                textView.setBackgroundResource(R.drawable.bg_inner_select_white)
                propertySize = value
                binding.etCustomPropertySize.text.clear()

                Log.d("propertySize", "Selected: $propertySize")
            }
        }

        binding.etCustomPropertySize.setOnClickListener {
            clearSelections(propertyOptions)
        }

        binding.etCustomPropertySize.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input.isNotEmpty()) {
                    propertySize = input
                    Log.d("propertySize", "Manual Input: $propertySize")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun clearSelections(options: List<Pair<TextView, String>>) {
        options.forEach { (textView, _) ->
            textView.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }
    }

    private fun byDefaultSelectAvailability() {
        binding.tvAny.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tvAnyParkingSpace.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_inner_select_white)

    }

    private fun settingBackgroundTaskToPeople() {
        // Predefined Options ke liye click listeners
        binding.tv1.setOnClickListener { updateSelection(1) }
        binding.tv2.setOnClickListener { updateSelection(2) }
        binding.tv3.setOnClickListener { updateSelection(3) }
        binding.tv5.setOnClickListener { updateSelection(5) }
        binding.tv7.setOnClickListener { updateSelection(7) }
        binding.tvAnyPeople.setOnClickListener { updateSelection(0) }

        binding.availEditText.setOnClickListener {
            resetBackgrounds()
        }
        binding.availEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input.isNotEmpty()) {
                    availOption = input
                    Log.d("availOption", "Manual Input: $availOption")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun updateSelection(value: Int) {
        resetBackgrounds()

        when (value) {
            1 -> binding.tv1.setBackgroundResource(R.drawable.bg_inner_select_white)
            2 -> binding.tv2.setBackgroundResource(R.drawable.bg_inner_select_white)
            3 -> binding.tv3.setBackgroundResource(R.drawable.bg_inner_select_white)
            5 -> binding.tv5.setBackgroundResource(R.drawable.bg_inner_select_white)
            7 -> binding.tv7.setBackgroundResource(R.drawable.bg_inner_select_white)
            0 -> binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_inner_select_white)
        }
        // availOption update karo
        availOption = if (value == 0) "any" else value.toString()
        binding.availEditText.text.clear()
        Log.d("availOption", "Selected: $availOption")
    }

    private fun resetBackgrounds() {
        binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv1.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv2.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv3.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv5.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv7.setBackgroundResource(R.drawable.bg_outer_manage_place)
    }

    private fun settingBackgroundTaskToParking(){
        binding.tvAnyParkingSpace.setOnClickListener {
            binding.tvAnyParkingSpace.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv1Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv1Parking.setOnClickListener {
            binding.tvAnyParkingSpace.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Parking.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv2Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv2Parking.setOnClickListener {
            binding.tvAnyParkingSpace.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Parking.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv3Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv3Parking.setOnClickListener {
            binding.tvAnyParkingSpace.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Parking.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv4Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }


        binding.tv4Parking.setOnClickListener {
            binding.tvAnyParkingSpace.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Parking.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv5Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv5Parking.setOnClickListener {
            binding.tvAnyParkingSpace.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Parking.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv7Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv7Parking.setOnClickListener {
            binding.tvAnyParkingSpace.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Parking.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv8Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv8Parking.setOnClickListener {
            binding.tvAnyParkingSpace.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Parking.setBackgroundResource(R.drawable.bg_inner_select_white)
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    fun setUpRecyclerView() {
        // Grid Layout Managers
        val gridLayoutManager = GridLayoutManager(this, 3) // Activity (Main) - 3 columns
        val gridLayoutManager2 = GridLayoutManager(this, 3) // Activity (Other) - 3 columns
        val gridLayoutManager3 = GridLayoutManager(this, 2) // Languages - 2 columns
        val gridLayoutManager4 = GridLayoutManager(this, 2) // Amenities - 2 columns

        // RecyclerView Setup - Activity (Main)
        binding.recyclerActivity.apply {
            layoutManager = gridLayoutManager
            adapter = adapterActivity
            isNestedScrollingEnabled = false
        }

        // RecyclerView Setup - Activity (Other)
        binding.recyclerActivity2.apply {
            layoutManager = gridLayoutManager2
            adapter = adapterActivity2
            isNestedScrollingEnabled = false
            visibility = View.GONE
        }

        // RecyclerView Setup - Languages
        binding.recyclerLanguage.apply {
            layoutManager = gridLayoutManager3
            adapter = languageAdapter
            isNestedScrollingEnabled = false
        }

        // Update language adapter and capture selected languages
        languageAdapter.updateAdapter(getNationalLanguages().subList(0, 6))

        languageAdapter.setOnItemClickListener(object : AmenitiesAdapter.onItemClickListener {
            override fun onItemClick(list: MutableList<Pair<String, Boolean>>) {
                // Capture selected languages
                selectedLanguages = listOf(list.filter { it.second }.map { it.first }.toString())
                Log.d("SelectedLanguages", selectedLanguages.toString())
            }
        })

        // RecyclerView Setup - Amenities
        binding.recyclerAmenties.apply {
            layoutManager = gridLayoutManager4
            adapter = amenitiesAdapter
            isNestedScrollingEnabled = false
        }

        // Update amenities adapter and capture selected amenities
        amenitiesAdapter.updateAdapter(amenitiesList.subList(0, 6))

        amenitiesAdapter.setOnItemClickListener(object : AmenitiesAdapter.onItemClickListener {
            override fun onItemClick(list: MutableList<Pair<String, Boolean>>) {
                selectedAmenities = list.filter { it.second }.map { it.first }.toMutableList()
                Log.d("Selected Amenities", selectedAmenities.toString())
            }
        })

        // Capture selected activities
        adapterActivity.setOnItemClickListener { list, _ ->
            selectedActivities = list.filter { it.checked }.toMutableList()
            selectedActivities.forEach { activity ->
                selectedActivityName = listOf(activity.name)
                Log.d("Selected Activity", "Name: ${activity.name}, Checked: ${activity.checked}")
            }
        }

        // Toggle Other Activities RecyclerView
        binding.tvOtherActivity.setOnClickListener {
            with(binding.recyclerActivity2) {
                visibility = if (visibility == View.VISIBLE) {
                    View.GONE
                } else {
                    View.VISIBLE.also {
                        scrollToPosition(adapterActivity2.itemCount - 1) // Scroll to last item
                    }
                }
            }
        }

        // Date Manager Integration
        val (currentMonth, currentYear) = dateManager.getCurrentMonthAndYear()
        binding.tvMonthName.text = currentMonth
        binding.tvYear.text = currentYear.toString()

        // Date Selector Toggle
        binding.llDate.setOnClickListener {
            binding.rlDateSelection.visibility =
                if (binding.rlDateSelection.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        // Month Selector Dialog
        binding.rlMonth.setOnClickListener {
            dateManager.showMonthSelectorDialog { selectedMonth ->
                binding.tvMonthName.text = selectedMonth
            }
        }
        // Year Picker Dialog
        binding.rlYearView.setOnClickListener {
            dateManager.showYearPickerDialog { year ->
                binding.tvYear.text = year.toString()
            }
        }
        // Save Date Action
        binding.rlSave.setOnClickListener {
            binding.tvDateSelect.text = "${binding.tvMonthName.text} / ${binding.tvYear.text}"
            binding.rlDateSelection.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun callingPriceRangeGraphSelection(){
        val barEntrys = ArrayList<BarEntry>()
        var seekBar = binding.seekBar

        val heights = arrayOf(5f, 3f, 4f, 7f, 8f, 15f, 13f, 12f, 10f, 5f, 17f, 16f, 13f, 12f, 8f,
            13f,10f,6f,9f,11f,7f)

        for (i in heights.indices) {
            barEntrys.add(BarEntry(i.toFloat(), heights[i]))
        }
        seekBar.setEntries(barEntrys)

        seekBar.onRangeChanged = { leftPinValue, rightPinValue ->
            val leftVal = (leftPinValue?.toInt()?.div(2))?.times(10)
            val rightVal = (rightPinValue?.toInt()?.div(2))?.times(10)
            binding.tvMinimumVal.text = "$"+leftVal.toString()
            binding.tvMaximumValue.text = "$"+rightVal.toString()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun clickListenerCalls() {

        binding.apply {
            tvHomeSetup.setOnClickListener(this@FiltersActivity)
            tvRoom.setOnClickListener(this@FiltersActivity)
            tvEntireHome.setOnClickListener(this@FiltersActivity)
            llDate.setOnClickListener(this@FiltersActivity)
            llTime.setOnClickListener {
                DateManager(this@FiltersActivity).showHourSelectionDialog(this@FiltersActivity) { selectedHour ->
                    tvHour.text = selectedHour
                }
            }
            byDefaultSelect()

            imageFilter.setOnClickListener {
                if (NetworkMonitorCheck._isConnected.value){
                    filterClick()
//                    val dialog = FullScreenDialogFragment()
//                    dialog.show(supportFragmentManager, "FullScreenDialog")
                }else{
                    showErrorDialog(this@FiltersActivity,
                        resources.getString(R.string.no_internet_dialog_msg)
                    )
                }

            }

            clearAllBtn.setOnClickListener {
                tvMinimum.text = ""
                tvMaximum.text = ""
                autocompleteLocation.text.clear()
                tvHomeSetup.performClick()
                tvRoom.performClick()
                tvEntireHome.performClick()
            }

            instantBookingSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                instantBookingCount = if (isChecked) {
                    1
                }else{
                    0
                }
            }

            selfCheckinToggle.setOnCheckedChangeListener { buttonView, isChecked ->
                selfCheckIn = if (isChecked) {
                    1
                }else{
                    0
                }
            }

            petToggle.setOnCheckedChangeListener { buttonView, isChecked ->
                petCheckIn = if (isChecked){
                    1
                }else{
                    0
                }
            }

        }
    }

    private fun filterClick() {
        lifecycleScope.launch(Dispatchers.IO) {
            filterViewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConnected ->
                    withContext(Dispatchers.Main) {
                        if (!isConnected) {
                            showErrorDialog(this@FiltersActivity, resources.getString(R.string.no_internet_dialog_msg))
                        } else {
                            try {
                                // Fetch location asynchronously
                                val location = locationManager.getCurrentLocation()
                                if (location != null) {
                                    val lat = location.latitude.toString()
                                    val lng = location.longitude.toString()

                                    filterViewModel.getFilterHomeDataApi(
                                        sessionManager.getUserId().toString(),
                                        lat, lng, selectedOption!!, binding.tvMinimum.text.toString(),
                                        binding.tvMaximum.text.toString(), binding.autocompleteLocation.text.toString(),
                                        binding.tvDateSelect.text.toString(), binding.tvHour.text.toString(),
                                        availOption, propertySize, bedroomCount, bathroomCount, instantBookingCount.toString(),
                                        selfCheckIn.toString(), petCheckIn.toString(), selectedActivityName, selectedAmenities, selectedLanguages
                                    ).collect { result ->
                                        when (result) {
                                            is NetworkResult.Success -> {
                                                result.data?.let {
                                                    Toast.makeText(this@FiltersActivity, "successfully", Toast.LENGTH_SHORT).show()
                                                }
                                            }

                                            is NetworkResult.Error -> {
                                                showErrorDialog(this@FiltersActivity, result.message ?: "Unknown error")
                                            }
                                            else -> {
                                                Log.v(ErrorDialog.TAG, "error::${result.message}")
                                            }
                                        }
                                    }
                                } else {
                                    showErrorDialog(this@FiltersActivity, "Unable to fetch location")
                                }
                            } catch (e: Exception) {
                                showErrorDialog(this@FiltersActivity, "Error: ${e.message}")
                            }
                        }
                    }
                }
        }
    }


    private fun byDefaultSelect() {
        binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_inner_manage_place)
    }

    private fun showDropdown(anchorView: View) {
        // Inflate the dropdown layout

//        Log.d("TESTING_ZYVOO","Here is Layout")
//        val popupView = LayoutInflater.from(this).inflate(R.layout.dropdown_item_time, null)
//        val dropdownLayout = LinearLayout(this)
//        dropdownLayout.orientation = LinearLayout.VERTICAL
//
//        // Create TextViews for each item
//        for (item in items) {
//            val textView = LayoutInflater.from(this).inflate(R.layout.dropdown_item_time, dropdownLayout, false) as TextView
//            textView.text = item
//            textView.setOnClickListener {
//                selectedItemTextView.text = item
//                popupWindow.dismiss()
//                Toast.makeText(this, "Selected: $item", Toast.LENGTH_SHORT).show()
//            }
//            dropdownLayout.addView(textView)
//        }
//
//        // Create the PopupWindow
//       // popupWindow = PopupWindow(dropdownLayout, dropdownLayout.measuredWidth, ViewGroup.LayoutParams.WRAP_CONTENT, true)
//
//
//        popupWindow = PopupWindow(dropdownLayout,
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            true)
//      //  var borderDrawable: Drawable = ContextCompat.getDrawable(this, R.drawable.bg_four_side_grey_corner)!!
//       // popupWindow.background = borderDrawable
//
//
//        popupWindow.isFocusable = true
//
//        // Show the PopupWindow
//        popupWindow.showAsDropDown(anchorView, 0, 0)


        val dropdownView = LayoutInflater.from(this).inflate(R.layout.dropdown_item_time, null)

        // Create the PopupWindow
        popupWindow = PopupWindow(dropdownView,
           250,
           400,
            true)

        // Set up click listeners for each item in the dropdown
        dropdownView.findViewById<TextView>(R.id.item_1).setOnClickListener {
            selectedItemTextView.text = "1 Hour"
            popupWindow.dismiss()

        }

        dropdownView.findViewById<TextView>(R.id.item_2).setOnClickListener {
            selectedItemTextView.text = "2 Hour"
            popupWindow.dismiss()

        }

        dropdownView.findViewById<TextView>(R.id.item_3).setOnClickListener {
            selectedItemTextView.text = "3 Hour"
            popupWindow.dismiss()

        }
        dropdownView.findViewById<TextView>(R.id.item_4).setOnClickListener{
            selectedItemTextView.text = "4 Hour"
            popupWindow.dismiss()

        }
        dropdownView.findViewById<TextView>(R.id.item_5).setOnClickListener {
            selectedItemTextView.text = "5 Hour"
            popupWindow.dismiss()

        }
        dropdownView.findViewById<TextView>(R.id.item_6).setOnClickListener {
            selectedItemTextView.text = "6 Hour"
            popupWindow.dismiss()

        }

        // Show the PopupWindow
//        popupWindow.showAsDropDown(anchorView, 0,anchorView.height)




        dropdownView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val dropdownHeight = dropdownView.measuredHeight

        // Get the location of the anchor view on the screen
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        // Calculate the Y position to show the PopupWindow below the anchor view
        val yPosition = location[1] + anchorView.height // Bottom of the anchor view

        // Show the PopupWindow at the calculated position
        popupWindow.showAtLocation(anchorView.rootView, Gravity.NO_GRAVITY, location[0], yPosition)

//        dropdownView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
//        val dropdownHeight = dropdownView.measuredHeight
//
//        // Show the PopupWindow at the bottom of the anchor view
//        popupWindow.showAtLocation()

     }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Prevent closing of suggestions when pressing the down arrow key
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            autocompleteTextView.showDropDown()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.tv_home_setup -> {
               Log.d("TESTING_VOOPON","Here in the home setup")
                binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_inner_manage_place)
                binding.tvRoom.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tvEntireHome.setBackgroundResource(R.drawable.bg_outer_manage_place)

                selectedOption = "any_type"
            }
            R.id.tv_room -> {
                binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tvRoom.setBackgroundResource(R.drawable.bg_inner_manage_place)
                binding.tvEntireHome.setBackgroundResource(R.drawable.bg_outer_manage_place)
                selectedOption = "room"
            }

            R.id.tv_entire_home -> {
                binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tvRoom.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tvEntireHome.setBackgroundResource(R.drawable.bg_inner_manage_place)
                selectedOption = "entire_home"
            }
        }
    }

    fun settingDataToActivityModel(){
        activityList = mutableListOf<ActivityModel>()
        amenitiesList = PrepareData.getOnlyAmenitiesList()



        val model1 = ActivityModel()
        model1.name = "Stays"
        model1.image = R.drawable.ic_stays
        activityList.add(model1)

        val model2 = ActivityModel()
        model2.name = "Event Space"
        model2.image = R.drawable.ic_event_space
        activityList.add(model2)

        val model3 = ActivityModel()
        model3.name = "Photo shoot"
        model3.image = R.drawable.ic_photo_shoot
        activityList.add(model3)

        val model4 = ActivityModel()
        model4.name = "Meeting"
        model4.image = R.drawable.ic_meeting
        activityList.add(model4)



        val model5 = ActivityModel()
        model5.name = "Party"
        model5.image = R.drawable.ic_party
        activityList.add(model5)


        var model6 = ActivityModel()
        model6.name = "Film Shoot"
        model6.image = R.drawable.ic_film_shoot
        activityList.add(model6)

        var model7 = ActivityModel()
        model7.name = "Performance"
        model7.image = R.drawable.ic_performance
        activityList.add(model7)

        var model8 = ActivityModel()
        model8.name = "Workshop"
        model8.image = R.drawable.ic_workshop
        activityList.add(model8)

        var model9 = ActivityModel()
        model9.name = "Corporate Event"
        model9.image = R.drawable.ic_corporate_event
        activityList.add(model9)

        var model10 = ActivityModel()
        model10.name = "Wedding"
        model10.image = R.drawable.ic_weding
        activityList.add(model10)

        var model11 = ActivityModel()
        model11.name = "Dinner"
        model11.image = R.drawable.ic_dinner
        activityList.add(model11)

        var model12 = ActivityModel()
        model12.name = "Retreat"
        model12.image = R.drawable.ic_retreat
        activityList.add(model12)


        var model13 = ActivityModel()
        model13.name = "Pop-up"
        model13.image = R.drawable.ic_popup_people
        activityList.add(model13)

        var model14 = ActivityModel()
        model14.name = "Networking"
        model14.image = R.drawable.ic_networking
        activityList.add(model14)

        var model15 = ActivityModel()
        model15.name = "Fitness Class"
        model15.image = R.drawable.ic_fitness_class
        activityList.add(model15)

        var model16 = ActivityModel()
        model16.name = "Audio Recording"
        model16.image = R.drawable.ic_audio_recording
        activityList.add(model16)

    }

        private fun getNationalLanguages(): MutableList<Pair<String,Boolean>> {
            return PrepareData.getLanguagePairs()
        }

    private fun showPopupWindowForPets(anchorView: View) {
        // Inflate the popup layout
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_layout_pets, null)

        // Create the PopupWindow
        val popupWindow = PopupWindow(popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT
            ,
            ViewGroup.LayoutParams.WRAP_CONTENT)

        // Show the popup window at the bottom right of the TextView

        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.showAsDropDown(anchorView, anchorView.width, 0)
    }

    override fun onItemClick(list: MutableList<Pair<String, Boolean>>) {
        val selectedItems = list.filter { it.second }
        Log.d("FilterActivity", "Selected Amenities: $selectedItems")
    }

}