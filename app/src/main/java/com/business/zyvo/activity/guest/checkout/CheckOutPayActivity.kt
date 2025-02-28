package com.business.zyvo.activity.guest.checkout

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.DateManager.DateManager
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.activity.guest.ExtraTimeActivity
import com.business.zyvo.activity.guest.checkout.viewmodel.CheckOutPayViewModel
import com.business.zyvo.activity.guest.propertydetails.model.AddOn
import com.business.zyvo.activity.guest.propertydetails.model.Pagination
import com.business.zyvo.activity.guest.propertydetails.model.PropertyData
import com.business.zyvo.activity.guest.propertydetails.model.Review
import com.business.zyvo.adapter.AdapterAddPaymentCard
import com.business.zyvo.adapter.guest.AdapterProAddOn
import com.business.zyvo.databinding.ActivityCheckOutPayBinding
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.ErrorDialog.calculatePercentage
import com.business.zyvo.utils.ErrorDialog.convertHoursToHrMin
import com.business.zyvo.utils.ErrorDialog.formatConvertCount
import com.business.zyvo.utils.ErrorDialog.formatDateyyyyMMddToMMMMddyyyy
import com.business.zyvo.utils.NetworkMonitorCheck
import com.business.zyvo.viewmodel.PaymentViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class CheckOutPayActivity : AppCompatActivity() {

    lateinit var binding : ActivityCheckOutPayBinding
    lateinit var adapterAddon : AdapterProAddOn

    private lateinit var addPaymentCardAdapter: AdapterAddPaymentCard

    var propertyData: PropertyData?=null
    var hour:String?=null
    var price:String?=null
    var stTime:String?=null
    var edTime:String?=null
    var propertyMile:String? = null
    var date:String?=null
    var addOnList: MutableList<AddOn> = mutableListOf()
    var session: SessionManager?=null

    private val checkOutPayViewModel: CheckOutPayViewModel by lazy {
        ViewModelProvider(this)[CheckOutPayViewModel::class.java]
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCheckOutPayBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        session = SessionManager(this)
        binding.tvReadMoreLess.setCollapsedText("Read More")
        binding.tvReadMoreLess.setCollapsedTextColor(R.color.green_color_bar)

        intent.extras?.let {
            propertyData = Gson().fromJson(it.getString("propertyData"),PropertyData::class.java)
            hour = it.getString("hour")
            price = it.getString("price")
            stTime = it.getString("stTime")
            edTime = it.getString("edTime")
            propertyMile = it.getString("propertyMile")
            date = it.getString("date")
        }
        // Observe the isLoading state
        lifecycleScope.launch {
            checkOutPayViewModel.isLoading.observe(this@CheckOutPayActivity) { isLoading ->
                if (isLoading) {
                    LoadingUtils.showDialog(this@CheckOutPayActivity, false)
                } else {
                    LoadingUtils.hideDialog()
                }
            }
        }
        initialization()
        clickListeneres()
        callingSelectionOfDate()
        callingSelectionOfTime()
        messageHostListener()
        setPropertyData()
        getUserCards()


    }

    @SuppressLint("SetTextI18n")
    private fun setPropertyData() {
        try {
            propertyData?.let {
                propertyData?.host_profile_image?.let {
                    Glide.with(this@CheckOutPayActivity).load(AppConstant.BASE_URL + it.get(0))
                        .into(binding.profileImageHost)
                }
                propertyData?.hosted_by?.let {
                    binding.tvHostName.text = it
                }
                propertyData?.is_instant_book?.let {
                    if (it == 1) {
                        binding.ivInsta.visibility = View.VISIBLE
                    } else {
                        binding.ivInsta.visibility = View.GONE
                    }
                }
                propertyData?.min_booking_hours?.let {
                    binding.tvResponseTime.text = "Respond within "+convertHoursToHrMin(it.toDouble())
                }
                propertyData?.images?.let {
                    if (it.isNotEmpty()) {
                        Glide.with(this@CheckOutPayActivity).load(AppConstant.BASE_URL + it.get(0))
                            .into(binding.ivProImage)
                    }
                }
                propertyData?.property_title?.let {
                    binding.tvProName.text = it
                }
                propertyData?.reviews_total_rating?.let {
                    binding.tvRating.text = it
                }
                propertyData?.reviews_total_count?.let {
                    binding.tvTotalReview.text = "("+ formatConvertCount(it) +")"
                }
                propertyMile?.let {
                    binding.tvMiles.text = "$it miles away"
                }
                date?.let {
                    date = formatDateyyyyMMddToMMMMddyyyy(it)
                    binding.tvDate.text = date
                }
                stTime?.let  { resp ->
                    edTime?.let {
                        binding.tvTiming.text = "From $resp to $it"
                    }
                }
                propertyData?.parking_rules?.let {
                    binding.proParkingRule.text = it
                }
                propertyData?.host_rules?.let {
                    binding.proHostRule.text = it
                }
                propertyData?.add_ons?.let {
                    if (it.isNotEmpty()) {
                        addOnList = it.toMutableList()
                        adapterAddon.updateAdapter(addOnList)
                    }
                }
                calculatePrice()
            }
        } catch (e: Exception) {
            Log.d(ErrorDialog.TAG, e.message.toString())
        }
    }
    @SuppressLint("SetTextI18n")
    fun messageHostListener(){
        val dateManager = DateManager(this)
        binding.rlHours.setOnClickListener {
            dateManager.showHourSelectionDialog(this) { selectedHour ->
                binding.tvHours.text = selectedHour
                hour = selectedHour.replace(" hours","")
                calculatePrice()
            }
        }
        binding.textSaveChangesButtonTime.setOnClickListener {
            stTime = binding.endHour.text.toString()+":"+
                    binding.endMinute.text.toString()+" "+binding.endAmPm.text.toString()
            edTime =  binding.startHour.text.toString()+":"+
                    binding.startMinute.text.toString()+" "+binding.startAmPm.text.toString()
            binding.tvTiming.text = "From "+stTime+"to "+edTime
            binding.relTime.visibility = View.GONE
        }
        binding.rlMsgHost.setOnClickListener {
            if(binding.llMsgHost.visibility == View.VISIBLE){
                binding.llMsgHost.visibility = View.GONE
            }
            else{
                binding.llMsgHost.visibility = View.VISIBLE
            }
        }
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
        binding.doubt.setOnClickListener {
            binding.doubt.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box)
            binding.tvAvailableDay.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
            binding.tvOtherReason.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
        }
        binding.tvAvailableDay.setOnClickListener {
            binding.doubt.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
            binding.tvAvailableDay.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box)
            binding.tvOtherReason.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
        }
        binding.tvOtherReason.setOnClickListener {
            binding.doubt.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
            binding.tvAvailableDay.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
            binding.tvOtherReason.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box)
        }



    }

    fun callingSelectionOfTime(){
        val hoursArray = Array(24) { i -> (i + 1).toString() }
        val hoursList: List<String> = hoursArray.toList()
        val minutesArray = Array(60) { i -> (i + 1).toString() }
        val minutesList :List<String> = minutesArray.toList()
        val amPmList = listOf<String>("AM","PM")

        binding.endHour.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.endHour.arrowAnimate = false
        binding.endHour.setItems(hoursList)
        binding.endHour.setIsFocusable(true)
        val recyclerView = binding.endHour.getSpinnerRecyclerView()
        val spacing = 16 // Spacing in pixels
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.top = spacing
            }
        })


        binding.endMinute.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.endMinute.arrowAnimate = false
        binding.endMinute.setItems(minutesList)
        binding.endMinute.setIsFocusable(true)
        val recyclerView1 = binding.endMinute.getSpinnerRecyclerView()
        // Spacing in pixels
        recyclerView1.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.top = spacing
            }
        })


        binding.endAmPm.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.endAmPm.arrowAnimate = false
        binding.endAmPm.setItems(amPmList)
        binding.endAmPm.setIsFocusable(true)
        val recyclerView45 = binding.endAmPm.getSpinnerRecyclerView()
        recyclerView45.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.top = spacing
            }
        })


        binding.startAmPm.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.startAmPm.arrowAnimate = false
        binding.startAmPm.setItems(amPmList)
        binding.startAmPm.setIsFocusable(true)
        val recyclerView46 = binding.endAmPm.getSpinnerRecyclerView()
        recyclerView46.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.top = spacing
            }
        })


        binding.startHour.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.startHour.arrowAnimate = false
        binding.startHour.setItems(hoursList)
        binding.startHour.setIsFocusable(true)

        val recyclerView2 = binding.startHour.getSpinnerRecyclerView()
        recyclerView2.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.top = spacing
            }
        })

        binding.startMinute.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.startMinute.arrowAnimate = false
        binding.startMinute.setItems(hoursList)
        binding.startMinute.setIsFocusable(true)
        val recyclerView3 = binding.startMinute.getSpinnerRecyclerView()
        recyclerView3.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.top = spacing
            }
        })

        binding.dateView1.setOnClickListener {
            if(binding.relTime.visibility == View.VISIBLE){
                binding.relTime.visibility = View.GONE
            }else{
                binding.relTime.visibility = View.VISIBLE
            }
        }

    }

    fun callingSelectionOfDate(){
        val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        val am_pm_list = listOf("AM","PM")
        val years = (2024..2050).toList()
        val yearsStringList = years.map { it.toString() }
        Toast.makeText(this,"Year String List: "+yearsStringList.size,Toast.LENGTH_LONG).show()
        val days = resources.getStringArray(R.array.day).toList()


        binding.spinnerLanguage.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.spinnerLanguage.spinnerPopupHeight = 400
        binding.spinnerLanguage.arrowAnimate = false
        binding.spinnerLanguage.setItems(days)
        binding.spinnerLanguage.setIsFocusable(true)
        val recyclerView = binding.spinnerLanguage.getSpinnerRecyclerView()

        // Add item decoration for spacing
        val spacing = 16 // Spacing in pixels
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.top = spacing
            }
        })

        binding.spinnermonth.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.spinnermonth.arrowAnimate = false
        binding.spinnermonth.spinnerPopupHeight = 400
        binding.spinnermonth.setItems(months)
        binding.spinnermonth.setIsFocusable(true)

        val recyclerView3 = binding.spinnermonth.getSpinnerRecyclerView()

        recyclerView3.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.top = spacing
            }
        })

        binding.spinneryear.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.spinneryear.arrowAnimate = false
        binding.spinneryear.spinnerPopupHeight = 400
        binding.spinneryear.setItems(yearsStringList.subList(0,16))
        binding.spinneryear.setIsFocusable(true)


        binding.endAmPm.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.endAmPm.arrowAnimate = false
        binding.endAmPm.spinnerPopupHeight = 200
        binding.endAmPm.setItems(am_pm_list)
        binding.endAmPm.setIsFocusable(true)
        binding.startAmPm.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.startAmPm.arrowAnimate = false
        binding.startAmPm.spinnerPopupHeight = 200
        binding.startAmPm.setItems(am_pm_list)
        binding.startAmPm.setIsFocusable(true)

        val recyclerView6 = binding.startAmPm.getSpinnerRecyclerView()

        recyclerView6.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.top = spacing
            }
        })


        val recyclerView5 = binding.endAmPm.getSpinnerRecyclerView()
        recyclerView5.addItemDecoration(object : RecyclerView.ItemDecoration() {

            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.top = spacing
            }

        })

        val recyclerView1 = binding.spinneryear.getSpinnerRecyclerView()
        recyclerView1.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.top = spacing
            }

        })

        binding.dateView.setOnClickListener {
            if(binding.relCalendarLayouts.visibility == View.VISIBLE){
                binding.relCalendarLayouts.visibility = View.GONE
            }
            else{
                binding.relCalendarLayouts.visibility = View.VISIBLE
            }
        }

        binding.textSaveChangesButton.setOnClickListener {
            date = binding.spinnermonth.text.toString()+" "+binding.spinnerLanguage.text.toString()+","+binding.spinneryear.text.toString()
            binding.tvDate.text = date
            binding.relCalendarLayouts.visibility = View.GONE
        }

    }


    fun clickListeneres(){

        binding.rlParking.setOnClickListener {
            if(binding.tvParkingRule.visibility == View.VISIBLE){
                binding.tvParkingRule.visibility=View.GONE
            }else{
                binding.tvParkingRule.visibility = View.VISIBLE
            }
        }
        binding.rlHostRules.setOnClickListener {
            if(binding.tvHostRule.visibility == View.VISIBLE){
                binding.tvHostRule.visibility = View.GONE
            }
            else{
                binding.tvHostRule.visibility = View.VISIBLE
            }
        }
        binding.rlConfirmPay.setOnClickListener {
            var intent = Intent(this@CheckOutPayActivity,ExtraTimeActivity::class.java)
            startActivity(intent)
        }
        binding.rlCreditDebitCard.setOnClickListener {
            if(binding.rlCreditDebitRecycler.visibility == View.GONE) {
                binding.rlCreditDebitRecycler.visibility = View.VISIBLE
                binding.imageDropDown.setImageResource(R.drawable.ic_dropdown_close)
            }else{
                binding.rlCreditDebitRecycler.visibility = View.GONE
                binding.imageDropDown.setImageResource(R.drawable.ic_dropdown_open)
            }
        }
        binding.rlAddCard.setOnClickListener {
            dialogAddCard()
        }

    }

    private fun initialization(){
        adapterAddon = AdapterProAddOn(this@CheckOutPayActivity,addOnList,
            object : AdapterProAddOn.onItemClickListener {
            override fun onItemClick(list: MutableList<AddOn>, position: Int) {
                addOnList = list
                calculatePrice()
            }
        })
        binding.recyclerAddOn.layoutManager = LinearLayoutManager(this@CheckOutPayActivity, LinearLayoutManager.VERTICAL ,false)
        binding.recyclerAddOn.adapter = adapterAddon

        showingMoreText()
        addPaymentCardAdapter = AdapterAddPaymentCard(this, mutableListOf())

        binding.recyclerViewPaymentCardList.layoutManager = LinearLayoutManager(this@CheckOutPayActivity,LinearLayoutManager.VERTICAL,false)
        binding.recyclerViewPaymentCardList.adapter = addPaymentCardAdapter

        val dayarray = resources.getStringArray(R.array.day)
        val adapterday: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, dayarray)

        // Set years
        val years = ArrayList<String?>()
        val thisYear = Calendar.getInstance()[Calendar.YEAR]
        for (i in 1950..thisYear) {
            years.add(i.toString())
        }

//        val adapteryear: ArrayAdapter<String?> = ArrayAdapter<String?>(this, android.R.layout.simple_spinner_item, years)
//        binding.spinnerYear.setAdapter<ArrayAdapter<*>>(adapteryear)

        checkOutPayViewModel.paymentCardList.observe(this, Observer { payment ->
            addPaymentCardAdapter.updateItem(payment)
        })

        binding.dateView.setOnClickListener {
            binding.relCalendarLayouts.visibility=View.VISIBLE
        }

    }

    private fun showingMoreText() {
        val text = "Show More"
        val spannableString = SpannableString(text).apply {
            setSpan(UnderlineSpan(), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.tvShowMore.text = spannableString
        binding.tvShowMore.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        binding.tvShowMore.paint.isAntiAlias = true
        binding.tvShowMore.setOnClickListener {
            adapterAddon.toggleList()
            binding.tvShowMore.text = if (adapterAddon.itemCount == addOnList.size) "Show Less" else "Show More"
        }
    }

    private fun dialogAddCard() {
        var dateManager = DateManager(this)
        val dialog =  Dialog(this, R.style.BottomSheetDialog)
        dialog.apply {
            setCancelable(true)
            setContentView(R.layout.dialog_add_card_details)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            val month: TextView = findViewById(R.id.textMonth)
            val year: TextView = findViewById(R.id.textYear)
            val submitButton: TextView = findViewById(R.id.textSubmitButton)
            month.setOnClickListener {
                dateManager.showMonthSelectorDialog { selectedMonth ->
                    month.text = selectedMonth
                }
                year.setOnClickListener {
                    dateManager.showYearPickerDialog { selectedYear ->
                        year.text = selectedYear.toString()
                    }
                }
            }
            submitButton.setOnClickListener {
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getUserCards() {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                checkOutPayViewModel.getUserCards(session?.getUserId().toString()).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->

                            }
                        }
                        is NetworkResult.Error -> {
                            showErrorDialog(this@CheckOutPayActivity, it.message!!)
                        }

                        else -> {
                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                        }
                    }
                }
            }
        }else{
            showErrorDialog(this,
                resources.getString(R.string.no_internet_dialog_msg))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun calculatePrice(){
        try {
            var totalPrice = 0.0
            var hourlyTotal = 0.0
            hour?.let {
                binding.tvHours.text = "$it Hours"
                binding.tvTotalHours.text = "$it Hours"
            }
            propertyData?.hourly_rate?.toDoubleOrNull()?.let { resp ->
                hour?.let {
                    hourlyTotal = (resp * it.toDouble())
                    binding.tvPrice.text = "$$hourlyTotal"
                    totalPrice += hourlyTotal
                }
            }
            propertyData?.cleaning_fee?.toDoubleOrNull()?.let {
                binding.tvCleaningFee.text = "$$it"
                totalPrice += it
            }
            propertyData?.service_fee?.toDoubleOrNull()?.let {
                binding.tvZyvoServiceFee.text = "$$it"
                totalPrice += it
            }
            propertyData?.tax?.toDoubleOrNull()?.let {resp ->
                hourlyTotal?.let {
                    val taxAmount = calculatePercentage(it,resp)
                    binding.tvTaxesPrice.text = "$$taxAmount"
                    totalPrice += taxAmount

                }
            }
            addOnList?.let {
                if (it.isNotEmpty()){
                    val total = calculateTotalPrice(addOnList)
                    binding.tvAddOnPrice.text = "$$total"
                    totalPrice += total
                }
            }
            // Apply Discount if Hours Exceed Discount Hour
            var discountAmount = 0.0
            propertyData?.bulk_discount_hour?.let { h ->
                hour?.let { cHr->
                    propertyData?.bulk_discount_rate?.let {
                        if (cHr.toInt() > h) {
                            discountAmount = (totalPrice * it.toDouble()) / 100
                            totalPrice -= discountAmount
                        }
                    }
                }
            }
            // Display Discount if Applied
            if (discountAmount > 0) {
                binding.tvDiscount.text = "-$$discountAmount"
                binding.llDiscountLabel.visibility = View.VISIBLE
            } else {
                binding.llDiscountLabel.visibility = View.GONE
            }
            // Final Total Price Display
            binding.tvTotalPrice.text = "$$totalPrice"
        }catch (e:Exception){
            Log.d(ErrorDialog.TAG,"calculatePrice ${e.message}")
        }
    }

    fun calculateTotalPrice(addOnList: List<AddOn>): Double {
        return addOnList.filter { it.checked }
            .sumOf { it.price.toDoubleOrNull() ?: 0.0 }
    }



}