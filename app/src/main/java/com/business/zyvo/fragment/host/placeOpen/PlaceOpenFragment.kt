package com.business.zyvo.fragment.host.placeOpen

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.BuildConfig
import com.business.zyvo.DateManager.DateManager
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.LoadingUtils.Companion.showSuccessDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.ScheduleEvent
import com.business.zyvo.fragment.host.placeOpen.model.PropertyResponse
import com.business.zyvo.fragment.host.placeOpen.viewModel.PlaceOpenViewModel
import com.business.zyvo.adapter.host.AdapterOuterPlaceOrder
import com.business.zyvo.databinding.FragmentPlaceOpenBinding
import com.business.zyvo.fragment.host.placeOpen.model.PropertyStatusResponse
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random


@AndroidEntryPoint
class PlaceOpenFragment : Fragment() {

    private var _binding: FragmentPlaceOpenBinding? = null
    private val binding get() = _binding!!
    lateinit var adapterOuterPlaceOrder: AdapterOuterPlaceOrder
    val viewModel: PlaceOpenViewModel by lazy {
        ViewModelProvider(this)[PlaceOpenViewModel::class.java]
    }
    lateinit var list: MutableList<Pair<String, List<String>>>
    var propertyId = ""
    var propertyImages = ""
    var latitude = ""
    var longitude = ""
    var propertyRating = ""
    var property_status = ""
    var property_review_count = ""
    var distanceMiles = ""
    var title = ""
    var startDate: String? = null
    var endDate: String? = null

    private val daysOfWeek = listOf(
        "10 - Mon", "11 - Tue", "12 - Wed", "13 - Thu",
        "14 - Fri", "15 - Sat"
    )

    private val events = listOf(
        ScheduleEvent("Katelyn Francy", "Finished", "01:00 - 02:00", 1, 2, R.drawable.green_bg),
        ScheduleEvent("Person Name", "Waiting payment", "03:00 - 04:00", 2, 4, R.drawable.orange_bg)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            propertyId = it.getString(AppConstant.PROPERTY_ID).toString()
            propertyImages = it.getString(AppConstant.property_images).toString()
            latitude = it.getString(AppConstant.latitude).toString()
            longitude = it.getString(AppConstant.longitude).toString()
            propertyRating = it.getString(AppConstant.property_rating).toString()
            property_status = it.getString(AppConstant.property_status).toString()
            property_review_count = it.getString(AppConstant.property_review_count).toString()
            distanceMiles = it.getString(AppConstant.distance_miles).toString()
            title = it.getString(AppConstant.title).toString()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPlaceOpenBinding.inflate(LayoutInflater.from(requireContext()))
        initializeData()
        initialization()
        list = mutableListOf()

        updateStatus()



        lifecycleScope.launch {
            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    LoadingUtils.showDialog(requireContext(), false)
                } else {
                    LoadingUtils.hideDialog()
                }
            }
        }

//        binding.scheduleView.setDays(daysOfWeek)


        binding.imageBackButton.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.rlEditPlace.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt(AppConstant.PROPERTY_ID, propertyId.toInt())
            findNavController().navigate(
                R.id.host_manage_property_frag,
                bundle
            )
        }

        fillDataInCalendar()
        settingRecyclerViewData()

        lifecycleScope.launch {
            viewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        LoadingUtils.showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {

                        val user_id: String =
                            SessionManager(requireContext()).getUserId().toString()
                        val (start_date, end_date) = getCurrentWeekDates()

                        propertyBookingDetails(
                            propertyId,
                            user_id,
                            start_date,
                            end_date,
                            latitude,
                            longitude
                        )
                    }

                }
        }
        binding.rlPauseButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.networkMonitor.isConnected
                    .distinctUntilChanged()
                    .collect { isConn ->
                        if (!isConn) {
                            LoadingUtils.showErrorDialog(
                                requireContext(),
                                resources.getString(R.string.no_internet_dialog_msg)
                            )
                        } else {
                            val user_id: String =
                                SessionManager(requireContext()).getUserId().toString()
                            togglePropertyBooking(
                                propertyId,
                                user_id
                            )
                        }

                    }
            }
        }
        if (startDate == null && endDate == null) {
            val currentDate = Date()
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val dateFormat1 = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
            val currentDateStr = dateFormat.format(currentDate)
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.DAY_OF_MONTH, 7)
            val futureDateStr = dateFormat1.format(calendar.time)
            binding.tvDateRange.text = "$currentDateStr - $futureDateStr"
        }
        binding.llDateRangeSelect.setOnClickListener {
            DateManager(requireContext()).getRangeSelectedDateWithYear(
                fragmentManager = parentFragmentManager
            ) { selectedData ->
                selectedData?.let {

                    val (dateRange, year) = it
                    val (startDate1, endDate1) = dateRange
                    startDate = startDate1 //+ " " + year
                    endDate = endDate1 //+ " " + year
                    //binding.tvDateRange.text = "$startDate1 - $endDate1 $year"
                    binding.tvDateRange.text = "$startDate1 - $endDate1"
//                    Toast.makeText(
//                        this,
//                        "Range: $startDate to $endDate, Year: $year",
//                        Toast.LENGTH_SHORT
//                    ).show()

                    lifecycleScope.launch {
                        viewModel.networkMonitor.isConnected
                            .distinctUntilChanged()
                            .collect { isConn ->
                                if (!isConn) {
                                    LoadingUtils.showErrorDialog(
                                        requireContext(),
                                        resources.getString(R.string.no_internet_dialog_msg)
                                    )
                                } else {
                                    try {
                                        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

//                                    val startDate1 = LocalDate.parse(
//                                        "$startDate $year",
//                                        DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH)
//                                    )
//                                        .format(outputFormatter)

//                                    val formattedStartDate = "$startDate $year".replace(Regex("\\s+"), " ") // Ensure proper spacing
//                                    val formattedEndDate = "$endDate $year".replace(Regex("\\s+"), " ") // Ensure proper spacing


                                        Log.d("checkDates", startDate!!)
                                        Log.d("checkDates", endDate!!)
                                        val startDate1 = LocalDate.parse(
                                            startDate,
                                            DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH)
                                        ).format(outputFormatter)

                                        val endDate1 = LocalDate.parse(
                                            endDate,
                                            DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH)
                                        )
                                            .format(outputFormatter)

                                        val user_id: String =
                                            SessionManager(requireContext()).getUserId().toString()
                                        propertyBookingDetails(
                                            propertyId,
                                            user_id,
                                            "$startDate1",
                                            "$endDate1",
                                            latitude,
                                            longitude
                                        )
                                    }catch (e:Exception){
                                        Log.d(ErrorDialog.TAG,e.message?:"")
                                    }
                                }

                            }
                    }

                } ?: run {
                    Toast.makeText(requireContext(), "No date selected", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return binding.root
    }

    private fun initializeData() {
        if (propertyImages != "") {
            Glide
                .with(this)
                .load(BuildConfig.MEDIA_URL + propertyImages)
                .centerCrop()
                .placeholder(R.drawable.ic_img_not_found)
                .into(binding.imageProfile)

        }
        if (title != "") {
            binding.textTitle.text = "$title"
        }
        if (propertyRating != "") {
            binding.textRatingStar.text = "$propertyRating"
        }
        if (property_review_count != "") {
            binding.textK.text = "(" + property_review_count + ")"
        }


        if (distanceMiles != "") {
            binding.textMiles.text = "$distanceMiles miles away"
        }

    }

    fun updateStatus(){
        if (property_status == "active"){
            binding.textPauseButton.text = "Resume Bookings"
        }else{
            binding.textPauseButton.text = "Pause Bookings"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentWeekDates(): Pair<String, String> {
        val today = LocalDate.now()
        val monday = today.with(DayOfWeek.MONDAY) // Get Monday of current week
        val sunday = today.with(DayOfWeek.SUNDAY) // Get Sunday of current week

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return Pair(monday.format(formatter), sunday.format(formatter))
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDaysOfWeek(startDate: String, endDate: String): List<String> {
        val dateFormatter = DateTimeFormatter.ofPattern("dd - EEE") // New Format: "10 - Mon"
        val start = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val end = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val daysOfWeek = mutableListOf<String>()
        var currentDate = start

        while (!currentDate.isAfter(end)) {
            daysOfWeek.add(currentDate.format(dateFormatter)) // Format as "10 - Mon"
            currentDate = currentDate.plusDays(1) // Move to next day
        }

        return daysOfWeek
    }


    private fun getStatusColor(status: String): Int {
        return when (status) {
            "confirmed" -> R.drawable.green_bg
            "pending" -> R.drawable.orange_bg
            "cancelled" -> R.drawable.orange_bg
            else -> R.drawable.orange_bg
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun propertyBookingDetails(
        property_id: String,
        user_id: String,
        start_date: String,
        end_date: String,
        latitude: String,
        longitude: String
    ) {
        lifecycleScope.launch {

            viewModel.propertyBookingDetails(
                property_id,
                user_id,
                start_date,
                end_date,
                latitude,
                longitude
            ).collect {
                when (it) {

                    is NetworkResult.Success -> {
                        val model = Gson().fromJson(it.data, PropertyResponse::class.java)

                        val daysOfWeek = getDaysOfWeek(start_date, end_date)
                        // Set days in schedule view
                        binding.scheduleView.setDays(daysOfWeek)
                        // Final event list
                        val events = mutableListOf<ScheduleEvent>()

                        // Get all bookings
                        model.data.bookings?.forEach { booking ->
                            // Column index (0 for start_date)
                            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            val referenceStartDate = LocalDate.parse(start_date, dateFormatter)
                            val referenceEndDate =
                                LocalDate.parse(booking.booking_date, dateFormatter)

                            // val column = if (booking.booking_date == start_date) 0 else 1 // Adjust as needed

                            val column =
                                ChronoUnit.DAYS.between(referenceStartDate, referenceEndDate)
                                    .toInt()

                            // Extract start and end hour
                            val timeParts = booking.booking_start_end.split(" - ")
                            val startHour = timeParts[0].split(":")[0].toInt()
                            val endHour = timeParts[1].split(":")[0].toInt()

                            // Generate ScheduleEvent for each hour
                            for (hour in startHour until endHour) {
                                events.add(
                                    ScheduleEvent(
                                        title = booking.guest_name,
                                        subtitle = booking.booking_status,
                                        time = "$hour:00 - ${hour + 1}:00",
                                        column = column,
                                        row = hour,
                                        drawableRes = getStatusColor(booking.booking_status)
                                    )
                                )
                                binding.scheduleView.setEvents(events)
                            }
                        }

                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(requireContext(), it.message!!)

                    }

                    else -> {

                    }

                }
            }


        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun togglePropertyBooking(
        property_id: String,
        user_id: String
    ) {
        lifecycleScope.launch {

            viewModel.togglePropertyBooking(
                property_id,
                user_id
            ).collect {
                when (it) {

                    is NetworkResult.Success -> {
                        val model = Gson().fromJson(it.data, PropertyStatusResponse::class.java)
                        showSuccessDialog(requireContext(), model.message)

                        if (model.data.property_status != null){
                            property_status = model.data.property_status
                            updateStatus()
                        }
                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(requireContext(), it.message!!)

                    }

                    else -> {

                    }

                }
            }


        }
    }

    private fun settingRecyclerViewData() {
        adapterOuterPlaceOrder = AdapterOuterPlaceOrder(requireContext(), list)
        binding.recyclerPlaceOrder.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL, false
        )
        binding.recyclerPlaceOrder.adapter = adapterOuterPlaceOrder
        binding.recyclerPlaceOrder.isNestedScrollingEnabled = false

    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun fillDataInCalendar() {
        var hourList = DateManager(requireContext()).generateHourList()
        var pos = 0
        hourList.forEach {
            if (pos == 0) {
                var p: Pair<String, List<String>> = Pair<String, List<String>>(
                    it,
                    DateManager(requireContext()).getCurrentWeek()
                )
                list.add(p)
            } else {
                var p: Pair<String, List<String>> =
                    Pair<String, List<String>>(it, getNewListOfData())
                list.add(p)
            }
            pos++
        }

    }

    fun getNewListOfData(): MutableList<String> {
        var dummyList: MutableList<String> = mutableListOf()
        for (i in 1..7) {
            var rndNumber = generateRandomNumber()
            if (rndNumber == i) {
                dummyList.add("Add")
            } else {
                dummyList.add("")
            }
        }
        return dummyList
    }

    fun generateRandomNumber(): Int {
        return Random.nextInt(
            1,
            8
        ) // Generates a random number between 1 (inclusive) and 8 (exclusive)
    }

    fun initialization() {


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}