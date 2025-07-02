package com.business.zyvo.activity.guest.filter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.AppConstant
import com.business.zyvo.DateManager.DateManager
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.OnClickListener
import com.business.zyvo.R
import com.business.zyvo.activity.guest.filter.viewmodel.FiltersViewModel
import com.business.zyvo.activity.guest.propertydetails.model.Pagination
import com.business.zyvo.activity.guest.propertydetails.model.PropertyData
import com.business.zyvo.activity.guest.propertydetails.model.Review
import com.business.zyvo.activity.guest.propertydetails.viewmode.PropertyDetailsViewModel
import com.business.zyvo.adapter.guest.ActivitiesAdapter
import com.business.zyvo.adapter.guest.AmenitiesAdapter
import com.business.zyvo.adapter.host.DropDownTextAdapter
import com.business.zyvo.databinding.ActivityFiltersBinding
import com.business.zyvo.model.ActivityModel
import com.business.zyvo.model.FilterRequest
import com.business.zyvo.model.host.ItemDropDown
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.NetworkMonitorCheck
import com.business.zyvo.utils.PrepareData
import com.business.zyvo.utils.PrepareData.getNewHourMinimumList
import com.github.mikephil.charting.data.BarEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class FiltersActivity : AppCompatActivity(), AmenitiesAdapter.onItemClickListener,
    View.OnClickListener {

    lateinit var binding: ActivityFiltersBinding
    private val viewModel: FiltersViewModel by viewModels()
    private lateinit var selectedItemTextView: TextView
    private lateinit var popupWindow: PopupWindow
    private lateinit var autocompleteTextView: AutoCompleteTextView
    private lateinit var appLocationManager: com.business.zyvo.locationManager.LocationManager
    private lateinit var activityList: MutableList<ActivityModel>
    private lateinit var amenitiesList: MutableList<Pair<String, Boolean>>
    private lateinit var languageList: MutableList<Pair<String, Boolean>>
    private lateinit var adapterActivity: ActivitiesAdapter
    private lateinit var adapterActivity2: ActivitiesAdapter
    private lateinit var amenitiesAdapter: AmenitiesAdapter
    private lateinit var languageAdapter: AmenitiesAdapter
    private lateinit var dateManager: DateManager
    private var selectedOption = "any"
    private var availOption = ""
    private var propertySize: String = ""
    private var bedroomCount: String = ""
    private var parkingCount: String = ""
    private var bathroomCount: String = ""
    private var instantBookingCount = ""
    private var selfCheckIn = ""
    private var petCheckIn = ""
    private var selectedAmenities = mutableListOf<String>()
    private var selectedActivities = mutableListOf<ActivityModel>()
    private var selectedActivityName = mutableListOf<String>()
    private var selectedLanguages = listOf<String>()
    private lateinit var sessionManager: SessionManager
    private var selectedLatitude: Double = 0.0
    private var selectedLongitude: Double = 0.0
    private var min = ""
    private var max = ""
    var isExpanded = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFiltersBinding.inflate(LayoutInflater.from(this))
        dateManager = DateManager(this)
        setContentView(binding.root)
        settingDataToActivityModel()
        // Observe the isLoading state
        lifecycleScope.launch {
            viewModel.isLoading.observe(this@FiltersActivity) { isLoading ->
                if (isLoading) {
                    LoadingUtils.showDialog(this@FiltersActivity, false)
                } else {
                    LoadingUtils.hideDialog()
                }
            }
        }

        adapterActivity = ActivitiesAdapter(this, activityList.subList(0, 4))
        adapterActivity2 = ActivitiesAdapter(this, activityList.subList(4, activityList.size))
        amenitiesAdapter = AmenitiesAdapter(this, mutableListOf())
        languageAdapter = AmenitiesAdapter(this, mutableListOf())
        languageAdapter.changeDefaultCount(4)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        selectedItemTextView = binding.tvHour
        // settingDataToActivityModel()
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.llLocation, InputMethodManager.SHOW_IMPLICIT)

        clickListenerCalls()
        callingPriceRangeGraphSelection()
        setUpRecyclerView()

        appLocationManager = com.business.zyvo.locationManager.LocationManager(application, this)

        appLocationManager.autoCompleteLocationWork(binding.autocompleteLocation)

        binding.autocompleteLocation.setOnItemClickListener { parent, _, position, _ ->
            val selectedLocation = parent.getItemAtPosition(position) as String

            // Fetch location details
            appLocationManager.fetchPlaceDetails(selectedLocation) { latitude, longitude ->
                selectedLatitude = latitude
                selectedLongitude = longitude

                Log.d("FilterActivity", "Selected Location: Lat=$latitude, Lng=$longitude")

                // Yahan pe API call ya kisi aur jagah data pass kar sakte ho
            }
        }

        byDefaultSelectAvailability()
        settingBackgroundTaskToPeople()
        settingBackgroundTaskToProperty()
        settingBackgroundTaskToParkingNew()
        settingBackgroundTaskToBedroom()
        settingBackgroundTaskToBathroom()
        binding.allowPets.setOnClickListener {
            showPopupWindowForPets(it)
        }
        showingMoreText()
        showingMoreAmText()

        try {
            setFilterData()
        } catch (e: Exception) {
            Log.e(ErrorDialog.TAG, e.message!!)
        }

        binding.tvMaximumValue.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.toString().startsWith("$")) {
                    binding.tvMaximumValue.setText("$")
                    binding.tvMaximumValue.setSelection(binding.tvMaximumValue.text?.length ?: 0)
                    //Shrawan
                   /* val originalLeft = binding.tvMinimumVal.text.toString().replace("$", "")
                        .toFloat().toInt()?.div(100)?.times(2)
                    val originalRight = binding.tvMaximumValue.text.toString().replace("$", "")
                        .toFloat().toInt()?.div(100)?.times(2)
                    binding.seekBar.setSelectedEntries(
                        originalLeft!!.toInt(),
                        originalRight!!.toInt()
                    )*/
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.tvMinimumVal.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.toString().startsWith("$")) {
                    binding.tvMinimumVal.setText("$")
                    binding.tvMinimumVal.setSelection(binding.tvMinimumVal.text?.length ?: 0)
                    /* if (binding.tvMinimumVal.text.toString().replace("$","")
                         .isNotEmpty()) {
                         val originalLeft = binding.tvMinimumVal.text.toString().replace("$",
                             "")
                             .toInt()?.div(2)?.times(100)
                         val originalRight = binding.tvMaximumValue.text.toString().replace("$",
                             "")
                             .toInt()?.div(2)?.times(100)
                         binding.seekBar.setSelectedEntries(
                             originalLeft!!.toInt(),
                             originalRight!!.toInt()
                         )
                     }*/
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

    }

    @SuppressLint("SetTextI18n")
    private fun setFilterData() {
        val filterdata = sessionManager.getFilterRequest()
        if (!filterdata.equals("")) {
            val value: FilterRequest = Gson().fromJson(filterdata, FilterRequest::class.java)
            value?.let {
                Log.d(ErrorDialog.TAG, Gson().toJson(value))
                //Set place type data
                when (it.place_type) {
                    "any" -> {
                        selectedOption = "any"
                        Log.d("TESTING_VOOPON", "Here in the home setup")
                        binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_inner_manage_place)
                        binding.tvRoom.setBackgroundResource(R.drawable.bg_outer_manage_place)
                        binding.tvEntireHome.setBackgroundResource(R.drawable.bg_outer_manage_place)
                    }

                    "private_room" -> {
                        selectedOption = "private_room"
                        binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_outer_manage_place)
                        binding.tvRoom.setBackgroundResource(R.drawable.bg_inner_manage_place)
                        binding.tvEntireHome.setBackgroundResource(R.drawable.bg_outer_manage_place)
                    }

                    "entire_home" -> {
                        selectedOption = "entire_home"
                        binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_outer_manage_place)
                        binding.tvRoom.setBackgroundResource(R.drawable.bg_outer_manage_place)
                        binding.tvEntireHome.setBackgroundResource(R.drawable.bg_inner_manage_place)
                    }
                }
                // Set current values (start and end)
                min = it.minimum_price
                max = it.maximum_price
                if (!min.equals("") && !max.equals("")) {
                    //(leftPinValue?.toInt()?.div(2))?.times(100)
                    val originalLeft = min?.toInt()?.div(100)?.times(2)
                    val originalRight = max?.toInt()?.div(100)?.times(2)
                    binding.seekBar.setSelectedEntries(
                        originalLeft!!.toInt(),
                        originalRight!!.toInt()
                    )
                    binding.tvMinimumVal.setText("$$min")
                    binding.tvMaximumValue.setText("$$max")
                }/*else{
                    getPropertyPriceRange()
                }*/
                // Set Location values
                val location = it.location ?: ""
                Log.d(ErrorDialog.TAG, location)
                selectedLatitude = it.latitude.toDouble()
                selectedLongitude = it.longitude.toDouble()
                //  binding.autocompleteLocation.setText(location, false)
                binding.autocompleteLocation.post {
                    binding.autocompleteLocation.setText(location, false)
                    binding.autocompleteLocation.dismissDropDown()
                }

                //Set Date Value
                if (it.date.isNotEmpty()) {
                    binding.tvDateSelect.text = ErrorDialog.formatDateyyyyMMddToMMMMddyyyy(it.date)
                }
                //Set Time Value
                if (!it.time.equals("")) {
                    binding.tvHour.text = "${it.time} hour${if (it.time.toInt() == 1) "" else "s"}"//toString().replace(" hours","")
                }
                //Set No Of People
                when (it.people_count) {
                    "1" -> {
                        updateSelection(1)
                    }

                    "2" -> {
                        updateSelection(2)
                    }

                    "3" -> {
                        updateSelection(3)
                    }

                    "4" -> {
                        updateSelection(4)
                    }

                    "5" -> {
                        updateSelection(5)
                    }

                    "6" -> {
                        updateSelection(6)
                    }

                    "7" -> {
                        updateSelection(7)
                    }

                    else -> {
                        availOption = it.people_count
                        binding.availEditText.setText(availOption)
                    }
                }
                // Set Property Size (Sq ft)
                settingBackgroundTaskToPropertySetValue(it.property_size)
                // Set Bed Room Value
                settingBackgroundTaskToBedroomSetValue(it.bedroom)
                //Set Bath Room Value
                settingBackgroundTaskToBathroomSetValue(it.bathroom)
                //Set Parking Value
                settingBackgroundTaskToParkingNewSetValue(it.parkingcount)
                //Set Activity Value
                selectedActivityName = it.activities?.toMutableList()!!
                for (saveactivity in selectedActivityName) {
                    for (i in 0 until activityList.size) {
                        if (saveactivity.equals(activityList.get(i).name)) {
                            val activitymodel = activityList.get(i)
                            activitymodel.checked = true
                            activityList.set(i, activitymodel)
                        }
                    }
                }
                adapterActivity.updateAdapter(activityList.subList(0, 4))
                adapterActivity2.updateAdapter(activityList.subList(4, activityList.size))

                //Set Amenities Value
                selectedAmenities = it.amenities?.toMutableList()!!
                for (saveactivity in selectedAmenities) {
                    for (i in 0 until amenitiesList.size) {
                        if (saveactivity.equals(amenitiesList.get(i).first)) {
                            val updatedPair =
                                amenitiesList[i].copy(second = true) // Create a new Pair
                            amenitiesList[i] = updatedPair // Update the list with the new Pair
                        }
                    }
                }
                amenitiesAdapter.updateAdapter(amenitiesList.subList(0, 6))

                //Set Booking Value
                instantBookingCount = it.instant_booking
                if (instantBookingCount.equals("") || instantBookingCount.equals("0")) {
                    binding.instantBookingSwitch.isChecked = false
                } else {
                    binding.instantBookingSwitch.isChecked = true
                }
                selfCheckIn = it.self_check_in
                if (selfCheckIn.equals("") || selfCheckIn.equals("0")) {
                    binding.selfCheckinToggle.isChecked = false
                } else {
                    binding.selfCheckinToggle.isChecked = true
                }
                petCheckIn = it.allows_pets
                if (petCheckIn.equals("") || petCheckIn.equals("0")) {
                    binding.petToggle.isChecked = false
                } else {
                    binding.petToggle.isChecked = true
                }

                //Set Language Value
                selectedLanguages = it.languages?.toMutableList()!!
                for (saveactivity in selectedLanguages) {
                    for (i in 0 until languageList.size) {
                        if (saveactivity.equals(languageList.get(i).first)) {
                            val updatedPair =
                                languageList[i].copy(second = true) // Create a new Pair
                            languageList[i] = updatedPair // Update the list with the new Pair
                        }
                    }
                }
                languageAdapter.updateAdapter(languageList.subList(0, 4))
            }
        }
    }

    private fun settingBackgroundTaskToParkingNew() {
        val parkingOptions = listOf(
            binding.tvAnyParkingSpace to "any",
            binding.tv1Parking to "1",
            binding.tv2Parking to "2",
            binding.tv3Parking to "3",
            binding.tv4Parking to "4",
            binding.tv5Parking to "5",
            binding.tv7Parking to "7",
            binding.tv8Parking to "8"
        )

        parkingOptions.forEach { (textView, value) ->
            textView.setOnClickListener {
                clearSelections(parkingOptions)
                textView.setBackgroundResource(R.drawable.bg_inner_select_white)
                parkingCount = value
                binding.etParking.text.clear()

                Log.d("bedroomCount", "Selected: $parkingCount")
            }
        }
        binding.etParking.setOnClickListener {
            clearSelections(parkingOptions)
        }
        binding.etParking.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input.isNotEmpty()) {
                    parkingCount = input
                    Log.d("bedroomCount", "Manual Input: $parkingCount")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
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


    private fun settingBackgroundTaskToBathroom() {

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

    private fun settingBackgroundTaskToParkingNewSetValue(defaultValue: String?) {
        val parkingOptions = listOf(
            binding.tvAnyParkingSpace to "any",
            binding.tv1Parking to "1",
            binding.tv2Parking to "2",
            binding.tv3Parking to "3",
            binding.tv4Parking to "4",
            binding.tv5Parking to "5",
            binding.tv7Parking to "7",
            binding.tv8Parking to "8"
        )

        // Set the default selection based on the provided value
        parkingCount = defaultValue ?: "any" // If null, default to "any"
        setSelectedProperty(parkingCount, parkingOptions)
        binding.etParking.setOnClickListener {
            clearSelections(parkingOptions)
        }
        binding.etParking.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input.isNotEmpty()) {
                    parkingCount = input
                    Log.d("bedroomCount", "Manual Input: $parkingCount")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun settingBackgroundTaskToBathroomSetValue(defaultValue: String?) {

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

        // Set the default selection based on the provided value
        bathroomCount = defaultValue ?: "any" // If null, default to "any"
        setSelectedProperty(bathroomCount, bathroomOptions)


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

    private fun settingBackgroundTaskToBedroomSetValue(defaultValue: String?) {
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

        // Set the default selection based on the provided value
        bedroomCount = defaultValue ?: "any" // If null, default to "any"
        setSelectedProperty(bedroomCount, bedroomOptions)


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

    private fun settingBackgroundTaskToPropertySetValue(defaultValue: String?) {
        val propertyOptions = listOf(
            binding.tvAny to "any",
            binding.tv250 to "250",
            binding.tv350 to "350",
            binding.tv450 to "450",
            binding.tv550 to "550",
            binding.tv650 to "650",
            binding.tv750 to "750"
        )

        // Set the default selection based on the provided value
        propertySize = defaultValue ?: "any" // If null, default to "any"
        setSelectedProperty(propertySize, propertyOptions)

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

    // Function to highlight the selected TextView based on value
    private fun setSelectedProperty(
        selectedValue: String,
        propertyOptions: List<Pair<TextView, String>>
    ) {
        clearSelections(propertyOptions)
        // Find the matching TextView and highlight it
        propertyOptions.find { it.second == selectedValue }?.first?.setBackgroundResource(R.drawable.bg_inner_select_white)
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
        availOption = if (value == 0) "0" else value.toString()
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

    private fun settingBackgroundTaskToParking() {
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

    @SuppressLint("SetTextI18n", "DefaultLocale")
    @RequiresApi(Build.VERSION_CODES.O)
    fun setUpRecyclerView() {
        // Grid Layout Managers
        val gridLayoutManager = GridLayoutManager(this, 4) // Activity (Main) - 3 columns
        val gridLayoutManager2 = GridLayoutManager(this, 4) // Activity (Other) - 3 columns
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
        languageAdapter.updateAdapter(languageList.subList(0, 4))

        languageAdapter.setOnItemClickListener(object : AmenitiesAdapter.onItemClickListener {
            override fun onItemClick(list: MutableList<Pair<String, Boolean>>) {
                // Capture selected languages
                selectedLanguages = list.filter { it.second }.map { it.first }
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
        adapterActivity.setOnItemClickListener { list, _, status ->

            if (status) {
                //selectedActivityName.add(activity.name)
                selectedActivityName.add(list)
            } else {

                if (selectedActivityName.contains(list)) {
                    selectedActivityName.remove(list)
                }
            }
            if (selectedActivityName.contains("Stays")) {
                binding.tvbadroom.visibility = View.VISIBLE
                binding.llbadrooms.visibility = View.VISIBLE
            } else {
                binding.tvbadroom.visibility = View.GONE
                binding.llbadrooms.visibility = View.GONE
            }
            Log.d("Selected Activity", "List: " + TextUtils.join(",", selectedActivityName))

        }

        // Capture Other selected activities
        adapterActivity2.setOnItemClickListener { list, _, status ->

            if (status) {
                //selectedActivityName.add(activity.name)
                selectedActivityName.add(list)
            } else {

                if (selectedActivityName.contains(list)) {
                    selectedActivityName.remove(list)
                }
            }
            if (selectedActivityName.contains("Stays")) {
                binding.tvbadroom.visibility = View.VISIBLE
                binding.llbadrooms.visibility = View.VISIBLE
            } else {
                binding.tvbadroom.visibility = View.GONE
                binding.llbadrooms.visibility = View.GONE
            }

            Log.d("Selected Activity", "List: " + TextUtils.join(",", selectedActivityName))

        }

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


        binding.llDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                R.style.DialogTheme,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedMonth = String.format("%02d", selectedMonth + 1) // 1-based month
                    val formattedDay = String.format("%02d", selectedDay)
                    val selectedDate = "$selectedYear-$formattedMonth-$formattedDay"
                    val selectedCal = Calendar.getInstance().apply {
                        set(selectedYear, selectedMonth, selectedDay, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    val currentCal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    if (selectedCal.before(currentCal)) {
                        showErrorDialog(
                            this@FiltersActivity,
                            "You cannot select a past date from the calendar."
                        )

                    } else {
                        binding.tvDateSelect.text =
                            ErrorDialog.formatDateyyyyMMddToMMMMddyyyy(selectedDate)
                    }

                },
                year,
                month,
                day
            )

            datePickerDialog.show()
        }

        //    }
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

    private fun setCurrentDate() {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        // Set date in TextView
        binding.tvDateSelect.text = currentDate
    }
    @SuppressLint("SetTextI18n")
    private fun callingPriceRangeGraphSelection() {
        val barEntrys = ArrayList<BarEntry>()
        val seekBar = binding.seekBar
        val heights = arrayOf(
            5f,
            3f,
            4f,
            7f,
            8f,
            15f,
            13f,
            12f,
            10f,
            5f,
            17f,
            16f,
            13f,
            12f,
            8f,
            13f,
            10f,
            6f,
            9f,
            11f,
            7f,
            5f,
            3f,
            4f,
            7f,
            8f,
            15f,
            13f,
            12f,
            10f,
            5f,
            17f,
            16f,
            13f,
            12f,
            8f,
            13f,
            10f,
            6f,
            9f,
            11f,
            7f
        )

        for (i in heights.indices) {
            barEntrys.add(BarEntry(i.toFloat(), heights[i]))
        }
        seekBar.setEntries(barEntrys)

        seekBar.onRangeChanged = { leftPinValue, rightPinValue ->
            val leftVal = (leftPinValue?.toInt()?.div(2))?.times(100)
            val rightVal = (rightPinValue?.toInt()?.div(2))?.times(100)
            binding.tvMinimumVal.setText("$" + leftVal.toString())
            min = leftVal.toString()
            binding.tvMaximumValue.setText("$" + rightVal.toString())
            max = rightVal.toString()
        }
    }


    /*private fun callingPriceRangeGraphSelection() {
    val seekBar = binding.seekBar

    // Get the actual min/max from your API response or set defaults
   // val minPrice = min.toFloatOrNull() ?: 10f
   // val maxPrice = max.toFloatOrNull() ?: 140f
    val minPrice = binding.tvMinimumVal.text.toString().replace("$","").toFloat()
    val maxPrice = binding.tvMaximumValue.text.toString().replace("$","").toFloat()
    Log.d("checktvMinimumVal",binding.tvMinimumVal.text.toString())
    Log.d("checktvMinimumVal",binding.tvMaximumValue.text.toString())
    // Create entries that match your actual price range
    val barEntries = ArrayList<BarEntry>()
    val step = (maxPrice - minPrice) / 20 // Adjust 40 based on how many bars you want

    for (i in 0..20) {
        val price = minPrice + (i * step)
        // You might want to adjust the height based on your data distribution
        barEntries.add(BarEntry(i.toFloat(), (10f + i % 5))) // Example height pattern
    }

    seekBar.setEntries(barEntries)

    seekBar.onRangeChanged = { leftPinValue, rightPinValue ->
        // Calculate actual prices based on the pin positions
        val leftVal = minPrice + (leftPinValue?.toFloat() ?: 0f) * step
        val rightVal = minPrice + (rightPinValue?.toFloat() ?: 40f) * step

        binding.tvMinimumVal.setText("$${leftVal.toInt()}")
        min = leftVal.toInt().toString()
        binding.tvMaximumValue.setText("$${rightVal.toInt()}")
        max = rightVal.toInt().toString()
    }
}*/

    @SuppressLint("SetTextI18n")
    private fun clickListenerCalls() {

        binding.apply {
            tvHomeSetup.setOnClickListener(this@FiltersActivity)
            tvRoom.setOnClickListener(this@FiltersActivity)
            tvEntireHome.setOnClickListener(this@FiltersActivity)
            llDate.setOnClickListener(this@FiltersActivity)
            llTime.setOnClickListener {
               /* DateManager(this@FiltersActivity).showHourSelectionDialog(this@FiltersActivity) { selectedHour ->
                    tvHour.text = selectedHour
                }*/
                val items = getNewHourList()

                createDropdown (items,  binding.llTime)
            }
            byDefaultSelect()

            imageFilter.setOnClickListener {
                if (NetworkMonitorCheck._isConnected.value) {
                    val requestData = FilterRequest(
                        user_id = sessionManager.getUserId().toString(),
                        latitude = selectedLatitude.toString(),
                        longitude = selectedLongitude.toString(),
                        place_type = selectedOption?.takeUnless { it.isNullOrEmpty() }.toString(),
                        minimum_price = binding.tvMinimumVal.text.toString().replace("$", ""),//min,
                        maximum_price = binding.tvMaximumValue.text.toString()
                            .replace("$", ""),//max,
                        location = binding.autocompleteLocation.text.toString(),
                        date = if (!binding.tvDateSelect.text.toString()
                                .equals("")
                        ) ErrorDialog.convertDateFormatMMMMddyyyytoyyyyMMdd(binding.tvDateSelect.text.toString()) else "",
                        time = binding.tvHour.text.toString().replace(Regex("\\s*hours?")/*" hours"*/, ""),
                        people_count = availOption,
                        property_size = propertySize,
                        bedroom = bedroomCount,
                        bathroom = bathroomCount,
                        parkingcount = parkingCount,
                        instant_booking = instantBookingCount.toString(),
                        self_check_in = selfCheckIn.toString(),
                        allows_pets = petCheckIn.toString(),
                        activities = selectedActivityName,
                        amenities = selectedAmenities,
                        languages = selectedLanguages
                    )
                    sessionManager.setFilterRequest(Gson().toJson(requestData))
                    val intent = Intent()
                    intent.putExtra("type", "filter")
                    intent.putExtra("requestData", Gson().toJson(requestData))
                    setResult(Activity.RESULT_OK, intent)
                    finish() // Close the activity


                } else {
                    showErrorDialog(
                        this@FiltersActivity,
                        getString(R.string.no_internet_dialog_msg)
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
                val requestData = FilterRequest(
                    user_id = sessionManager.getUserId().toString(),
                    latitude = "",
                    longitude = "",
                    place_type = "",
                    minimum_price = "",
                    maximum_price = "",
                    location = "",
                    date = "",
                    time = "",
                    people_count = "",
                    property_size = "",
                    bedroom = "",
                    bathroom = "",
                    parkingcount = "",
                    instant_booking = "",
                    self_check_in = "",
                    allows_pets = "",
                    activities = mutableListOf(),
                    amenities = mutableListOf(),
                    languages = mutableListOf()
                )
                sessionManager.setFilterRequest(Gson().toJson(requestData))
                val intent = Intent()
                intent.putExtra("type", "clearAllBtn")
                setResult(Activity.RESULT_OK, intent)
                finish() // Close the activity

            }

            instantBookingSwitch.setOnClickListener {
                instantBookingCount = if (instantBookingSwitch.isChecked) "1" else "0"
            }

            selfCheckinToggle.setOnClickListener {
                selfCheckIn = if (selfCheckinToggle.isChecked) "1" else "0"
            }

            petToggle.setOnClickListener {
                petCheckIn = if (petToggle.isChecked) "1" else "0"
            }
        }
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
            languageAdapter.updateAdapter(languageList)

           // showingLessText()
            isExpanded = !isExpanded
            languageAdapter.toggleExpand()
            // Update button text
            binding.underlinedTextView.text = if (isExpanded) "Show Less" else "Show More"
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
            languageAdapter.updateAdapter(languageList.subList(0, 4))
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
            isExpanded = !isExpanded
            amenitiesAdapter.toggleExpand()

            // Update button text
            binding.underlinedTextView1.text = if (isExpanded) "Show Less" else "Show More"

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
            amenitiesAdapter.updateAdapter(amenitiesList.subList(0, 6))
            showingMoreAmText()
        }

    }


    private fun byDefaultSelect() {
        binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_inner_manage_place)
    }

    private fun showDropdown(anchorView: View) {

        val dropdownView = LayoutInflater.from(this).inflate(R.layout.dropdown_item_time, null)

        // Create the PopupWindow
        popupWindow = PopupWindow(
            dropdownView,
            250,
            400,
            true
        )

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
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }

        }
        dropdownView.findViewById<TextView>(R.id.item_4).setOnClickListener {
            selectedItemTextView.text = "4 Hour"
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }

        }
        dropdownView.findViewById<TextView>(R.id.item_5).setOnClickListener {
            selectedItemTextView.text = "5 Hour"
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }

        }
        dropdownView.findViewById<TextView>(R.id.item_6).setOnClickListener {
            selectedItemTextView.text = "6 Hour"
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }

        }

        dropdownView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val dropdownHeight = dropdownView.measuredHeight

        // Get the location of the anchor view on the screen
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        // Calculate the Y position to show the PopupWindow below the anchor view
        val yPosition = location[1] + anchorView.height // Bottom of the anchor view

        // Show the PopupWindow at the calculated position
        popupWindow.showAtLocation(anchorView.rootView, Gravity.NO_GRAVITY, location[0], yPosition)

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
                selectedOption = "any"
                Log.d("TESTING_VOOPON", "Here in the home setup")
                binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_inner_manage_place)
                binding.tvRoom.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tvEntireHome.setBackgroundResource(R.drawable.bg_outer_manage_place)
            }

            R.id.tv_room -> {
                selectedOption = "private_room"
                binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tvRoom.setBackgroundResource(R.drawable.bg_inner_manage_place)
                binding.tvEntireHome.setBackgroundResource(R.drawable.bg_outer_manage_place)
            }

            R.id.tv_entire_home -> {
                selectedOption = "entire_home"
                binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tvRoom.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tvEntireHome.setBackgroundResource(R.drawable.bg_inner_manage_place)
            }
        }
    }

    private fun settingDataToActivityModel() {
        activityList = mutableListOf<ActivityModel>()
        amenitiesList = PrepareData.getOnlyAmenitiesList()
        languageList = PrepareData.getLanguagePairs()


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


        val model6 = ActivityModel()
        model6.name = "Film Shoot"
        model6.image = R.drawable.ic_film_shoot
        activityList.add(model6)

        val model7 = ActivityModel()
        model7.name = "Performance"
        model7.image = R.drawable.ic_performance
        activityList.add(model7)

        val model8 = ActivityModel()
        model8.name = "Workshop"
        model8.image = R.drawable.ic_workshop
        activityList.add(model8)

        val model9 = ActivityModel()
        model9.name = "Corporate Event"
        model9.image = R.drawable.ic_corporate_event
        activityList.add(model9)

        val model10 = ActivityModel()
        model10.name = "Wedding"
        model10.image = R.drawable.ic_weding
        activityList.add(model10)

        val model11 = ActivityModel()
        model11.name = "Dinner"
        model11.image = R.drawable.ic_dinner
        activityList.add(model11)

        val model12 = ActivityModel()
        model12.name = "Retreat"
        model12.image = R.drawable.ic_retreat
        activityList.add(model12)


        val model13 = ActivityModel()
        model13.name = "Pop-up"
        model13.image = R.drawable.ic_popup_people
        activityList.add(model13)

        val model14 = ActivityModel()
        model14.name = "Networking"
        model14.image = R.drawable.ic_networking
        activityList.add(model14)

        val model15 = ActivityModel()
        model15.name = "Fitness Class"
        model15.image = R.drawable.ic_fitness_class
        activityList.add(model15)

        val model16 = ActivityModel()
        model16.name = "Audio Recording"
        model16.image = R.drawable.ic_audio_recording
        activityList.add(model16)

    }

    private fun getNationalLanguages(): MutableList<Pair<String, Boolean>> {
        return PrepareData.getLanguagePairs()
    }

    private fun showPopupWindowForPets(anchorView: View) {

        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_layout_pets, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.showAsDropDown(anchorView, 10, 0)
    }

    override fun onItemClick(list: MutableList<Pair<String, Boolean>>) {
        val selectedItems = list.filter { it.second }
        Log.d("FilterActivity", "Selected Amenities: $selectedItems")
    }

    private fun getPropertyPriceRange() {
        if (NetworkMonitorCheck._isConnected.value) {

            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.getPropertyPriceRange(

                ).collect {
                    when (it) {
                        is NetworkResult.Success -> {


                            if (it.data?.has("minimum_price") == true && !it.data.get("minimum_price")!!.isJsonNull) {
//                                binding.tvMinimumVal.setText("$"+it.data.get("minimum_price")!!.asString.toDouble().toInt().toString())
//                               min = it.data.get("minimum_price")!!.asString.toDouble().toInt().toString()
//                                Log.d("checkValue",it.data.get("minimum_price")!!.asString)
                                min = it.data.get("minimum_price")!!.asString.toDouble().toInt().toString()
                                binding.tvMinimumVal.setText("$$min")

                            }
                            if (it.data?.has("maximum_price") == true && !it.data.get("maximum_price")!!.isJsonNull) {
//                                binding.tvMaximumValue.setText("$"+it.data.get("maximum_price")!!.asString.toDouble().toInt().toString())
//                               max = it.data.get("maximum_price")!!.asString.toDouble().toInt().toString()
//                                Log.d("checkValue",it.data.get("maximum_price")!!.asString)
                                max = it.data.get("maximum_price")!!.asString.toDouble().toInt().toString()
                                binding.tvMaximumValue.setText("$$max")
                            }
                            setMinMax((10/10),(50/10))


                        }

                        is NetworkResult.Error -> {
                            showErrorDialog(this@FiltersActivity, it.message!!)
                        }

                        else -> {
                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                        }
                    }
                }
            }
        } else {
            showErrorDialog(
                this,
                resources.getString(R.string.no_internet_dialog_msg)
            )
        }
    }

    fun setMinMax(min: Int, max: Int) {
        if (min >= max) {
            Log.e("RangeBarWithChart", "Min value cannot be greater than or equal to max value.")
            return
        }

        val entries = ArrayList<BarEntry>()
        for (i in min..max) {
            entries.add(BarEntry(i.toFloat(), i.toFloat())) // or some other Y value
        }
        binding.seekBar.setEntries(entries)
    }

    private fun createDropdown(
        items: MutableList<ItemDropDown>,
        llHours: LinearLayout
    ) {
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.popup_menu_layout, null)

        // Setup your RecyclerView adapter here
        val popupWindow = PopupWindow(
            view,
            llHours.width, // Match width of the anchor view
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 10f

        // Show popup below llHours
        popupWindow.showAsDropDown(llHours, 0, 0)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = DropDownTextAdapter(items, object : OnClickListener {
            override fun itemClick(selectedIndex: Int) {
                 //   minimumHourIndex = selectedIndex
                    //minimumHourValue = selectedIndex + 1;
                    //Vipin
              //      minimumHourValue = selectedIndex + 2
            }
        }) { selectedText ->
            // Update TextView with the selected text
            binding.tvHour.text = selectedText
            popupWindow.dismiss()
        }
        recyclerView.adapter = adapter
        popupWindow.showAsDropDown(binding.tvHour)
    }

    fun getNewHourList(): MutableList<ItemDropDown> {
        return (1..23).map { ItemDropDown("$it hour${if (it >= 2) "s" else ""}")/* ItemDropDown("$it hour")*/ }.toMutableList()
    }


}