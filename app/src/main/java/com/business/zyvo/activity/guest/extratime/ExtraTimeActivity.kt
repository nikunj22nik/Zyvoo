package com.business.zyvo.activity.guest.extratime

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.BuildConfig
import com.business.zyvo.ErrorMessage
import com.skydoves.powerspinner.PowerSpinnerView
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.activity.ChatActivity
import com.business.zyvo.activity.GuesMain
import com.business.zyvo.activity.guest.extratimecharges.ExtraTimeChargesActivity
import com.business.zyvo.activity.guest.extratime.model.ReportReason
import com.business.zyvo.activity.guest.extratime.viewmodel.ExtraTimeViewModel
import com.business.zyvo.activity.guest.propertydetails.model.AddOn
import com.business.zyvo.activity.guest.propertydetails.model.PropertyData
import com.business.zyvo.databinding.ActivityExtraTimeBinding
import com.business.zyvo.fragment.guest.SelectHourFragmentDialog
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.ErrorDialog.TAG
import com.business.zyvo.utils.ErrorDialog.calculatePercentage
import com.business.zyvo.utils.ErrorDialog.convertHoursToHrMin
import com.business.zyvo.utils.ErrorDialog.formatConvertCount
import com.business.zyvo.utils.ErrorDialog.formatDateyyyyMMddToMMMMddyyyy
import com.business.zyvo.utils.ErrorDialog.showToast
import com.business.zyvo.utils.ErrorDialog.truncateToTwoDecimalPlaces
import com.business.zyvo.utils.NetworkMonitorCheck
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ExtraTimeActivity : AppCompatActivity(),SelectHourFragmentDialog.DialogListener {
    lateinit var binding :ActivityExtraTimeBinding
    var propertyData: PropertyData?=null
    var hour:String?=null
    var price:String?=null
    var stTime:String?=null
    var edTime:String?=null
    var propertyMile:String? = null
    var date:String?=null
    var addOnList: MutableList<AddOn> = mutableListOf()
    var bookingId:String?=null
    var reportReasonsMap: HashMap<String, Int> = HashMap()
    var session: SessionManager?=null
    var propertyId :String ="-1"
    var hostId :String ="-1"

    private val extraTimeViewModel: ExtraTimeViewModel by lazy {
        ViewModelProvider(this)[ExtraTimeViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityExtraTimeBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        session = SessionManager(this)
        intent.extras?.let {
            propertyData = Gson().fromJson(it.getString(AppConstant.PROPERTY_DATA), PropertyData::class.java)
            hour = it.getString(AppConstant.HOUR)
            price = it.getString(AppConstant.PRICE_TEXT)
            stTime = it.getString(AppConstant.ST_TIME)
            edTime = it.getString(AppConstant.ED_TIME)
            propertyMile = it.getString(AppConstant.PROPERTY_MILE)
            date = it.getString(AppConstant.DATE)
            bookingId = it.getString(AppConstant.BOOKING_ID_TEXT)
            propertyId = propertyData?.property_id.toString()
            hostId = propertyData?.host_id.toString()
        }

        lifecycleScope.launch {
            extraTimeViewModel.isLoading.observe(this@ExtraTimeActivity) { isLoading ->
                if (isLoading) {
                    LoadingUtils.showDialog(this@ExtraTimeActivity, false)
                } else {
                    LoadingUtils.hideDialog()
                }
            }
        }

        binding.reportIssue.setOnClickListener {
           dialogReportIssue()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        clickListeners()
        setPropertyData()

        binding.rlMsgHost.setOnClickListener {
            //  callingJoinChannelApi()
            callingMessageClickListner()
        }

        binding.readMore.setOnClickListener {
            if ( binding.readMore.text.toString().equals(AppConstant.READ_MORE,true)){
                binding.readMore.text = AppConstant.READ_LESS
                binding.tvReadMoreLess.maxLines = Integer.MAX_VALUE
            }else{
                binding.readMore.text = AppConstant.READ_MORE
                binding.tvReadMoreLess.maxLines = 3
            }
        }

    }

    private fun callingMessageClickListner(){
        if (binding.llMsgHost.visibility == View.VISIBLE) {
                binding.llMsgHost.visibility = View.GONE
        }
        else {
                binding.llMsgHost.visibility = View.VISIBLE
        }

        var messageSend = AppConstant.HAVE_DOUBT
        binding.doubt.setOnClickListener {
            binding.etShareMessage.setText("")
            binding.tvShareMessage.visibility = View.GONE
            messageSend = AppConstant.HAVE_DOUBT
            binding.tvAvailableDay.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
            binding.doubt.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box)
            binding.tvOtherReason.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
        }

        binding.tvAvailableDay.setOnClickListener {
            binding.etShareMessage.setText("")
            binding.tvShareMessage.visibility = View.GONE
            messageSend = AppConstant.AVAILABLE_DAYS
            binding.tvAvailableDay.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box)
            binding.doubt.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
            binding.tvOtherReason.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
        }
        binding.tvOtherReason.setOnClickListener {
            binding.tvShareMessage.visibility = View.VISIBLE
            messageSend = AppConstant.OTHER
            binding.doubt.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
            binding.tvAvailableDay.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
            binding.tvOtherReason.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box)
        }

        var writeMessage =""
        binding.etShareMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                writeMessage+=charSequence.toString()
                binding.tvAvailableDay.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
                binding.doubt.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
            }

            override fun afterTextChanged(editable: Editable?) {
            }
        })

        binding.rlSubmitMessage.setOnClickListener {
            val userInput = binding.etShareMessage.text.toString()
            if(userInput.length>0){
                messageSend = userInput
            }
            if (!messageSend.equals(AppConstant.OTHER)  ){
                propertyData?.let  { pro->
                    bookingId?.let {
                        val propertyid = it
                        val hostId = pro.host_id

                        Log.d(
                            TAG,
                            "PropertyId :- " + propertyid.toString() + " Hostid" + hostId
                        )

                        callingJoinChannelApi(messageSend)
                    }
                }
            }else{
                if (userInput.trim().isNotEmpty()){
                    propertyData?.let { pro->
                        bookingId?.let {
                            val propertyid = it
                            val hostId = pro.host_id

                            Log.d(
                                "TESTING_IDS",
                                "PropertyId :- " + propertyid.toString() + " Hostid" + hostId
                            )
                            callingJoinChannelApi(messageSend)
                        }
                    }
                }else{
                    binding.etShareMessage.error = ErrorMessage.PLEASE_ENTER_SOMETHING
                }
            }
        }
    }

    private fun callingJoinChannelApi(messageSend: String) {
        if(hostId.equals("-1") == false && propertyId.equals("-1") ==false){
            lifecycleScope.launch {
                val session= SessionManager(this@ExtraTimeActivity)
                val userId = session.getUserId()
                if(userId != null) {
                    LoadingUtils.showDialog(this@ExtraTimeActivity,true)
                    var channelName :String =""
                    if (userId < Integer.parseInt(hostId)) {
                        channelName = AppConstant.ZYVOOPROJ + userId + "_" + hostId +"_"+bookingId
                    }
                    else {
                        channelName = AppConstant.ZYVOOPROJ + hostId + "_" + userId +"_"+bookingId
                    }

                    extraTimeViewModel.joinChatChannel(userId,Integer.parseInt(hostId),channelName,
                        AppConstant.GUEST_CHANNEL_TYPE).collect{
                        when(it){
                            is NetworkResult.Success ->{
                                LoadingUtils.hideDialog()
                                val loggedInId = SessionManager(this@ExtraTimeActivity).getUserId()
                                if(it.data?.receiver_id?.toInt() == loggedInId){
                                    val userImage :String =  it.data?.receiver_avatar.toString()
                                    val friendImage :String = it.data?.sender_avatar.toString()
                                    var friendName :String = ""
                                    if(it.data?.sender_name != null){
                                        friendName = it.data.sender_name
                                    }
                                    var userName = ""
                                    userName = it.data?.receiver_name.toString()
                                    val intent = Intent(this@ExtraTimeActivity, ChatActivity::class.java)
                                    intent.putExtra(AppConstant.USER_IMG,userImage).toString()
                                    SessionManager(this@ExtraTimeActivity).getUserId()?.let { it1 -> intent.putExtra(AppConstant.USER_ID, it1.toString()) }
                                    Log.d("TESTING","REVIEW HOST"+channelName)
                                    intent.putExtra(AppConstant.CHANNEL_NAME,channelName)
                                    intent.putExtra(AppConstant.FRIEND_ID,hostId)
                                    intent.putExtra(AppConstant.FRIEND_IMG,friendImage).toString()
                                    intent.putExtra(AppConstant.FRIEND_NAME,friendName).toString()
                                    intent.putExtra(AppConstant.USER_NAME,userName)
                                    intent.putExtra(AppConstant.SENDER_ID, hostId)
                                    intent.putExtra(AppConstant.MESSAGE,messageSend)
                                    startActivity(intent)
                                }
                                else if(it.data?.sender_id?.toInt() == loggedInId){
                                    val userImage :String =  it.data?.sender_avatar.toString()
                                    Log.d("TESTING_PROFILE_HOST",userImage)
                                    val friendImage :String = it.data?.receiver_avatar.toString()
                                    Log.d("TESTING_PROFILE_HOST",friendImage)
                                    var friendName :String = ""
                                    if(it.data?.receiver_name != null){
                                        friendName = it.data.receiver_name
                                    }
                                    var userName = ""
                                    userName = it.data?.sender_name.toString()
                                    val intent = Intent(this@ExtraTimeActivity, ChatActivity::class.java)
                                    intent.putExtra(AppConstant.USER_IMG,userImage).toString()
                                    SessionManager(this@ExtraTimeActivity).getUserId()?.let { it1 -> intent.putExtra(AppConstant.USER_ID, it1.toString()) }
                                    Log.d("TESTING","REVIEW HOST"+channelName)
                                    intent.putExtra(AppConstant.CHANNEL_NAME,channelName)
                                    intent.putExtra(AppConstant.FRIEND_ID,hostId)
                                    intent.putExtra(AppConstant.FRIEND_IMG,friendImage).toString()
                                    intent.putExtra(AppConstant.FRIEND_NAME,friendName).toString()
                                    intent.putExtra(AppConstant.USER_NAME,userName)
                                    intent.putExtra(AppConstant.SENDER_ID, hostId)
                                    intent.putExtra(AppConstant.MESSAGE,messageSend)
                                    startActivity(intent)
                                }
                            }
                            is NetworkResult.Error ->{
                                LoadingUtils.hideDialog()

                            }
                            else ->{
                                LoadingUtils.hideDialog()
                            }
                        }
                    }
                }
            }
        }else{
            Toast.makeText(this@ExtraTimeActivity, ErrorMessage.ERROR_LOADING_CHAT,Toast.LENGTH_LONG).show()
        }
    }


    private fun clickListeners(){

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.rlCancelBooking.setOnClickListener {
            cancelScreen()
        }

        binding.rlParking.setOnClickListener {
            if(binding.tvParkingRule.visibility == View.VISIBLE){
                binding.tvParkingRule.visibility= View.GONE
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


        binding.dateView2.setOnClickListener {
            val dialog1 = SelectHourFragmentDialog()
            dialog1.setDialogListener(this)
            dialog1.show(supportFragmentManager, "MYDIALOF")
        }

        binding.myBooking.setOnClickListener {
            val intent = Intent(this, GuesMain::class.java)
            intent.putExtra(AppConstant.KEY_NAME,"12345")
            startActivity(intent)
           finish()
        }

    }


    private fun reportViolation(userId : String,
                                booking_id : String,
                                property_id : String,
                                report_reasons_id : String,
                                additional_details:String,
                                dialog:Dialog) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                extraTimeViewModel.reportViolation(userId,
                    booking_id,
                    property_id,
                    report_reasons_id,
                    additional_details).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                dialog.dismiss()
                                //openDialogNotification()
                                openDialogSuccess()
                            }
                        }
                        is NetworkResult.Error -> {
                            showErrorDialog(this@ExtraTimeActivity, it.message!!)
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

    private fun listReportReasons(powerSpinner: PowerSpinnerView) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                extraTimeViewModel.listReportReasons().collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                val items: MutableList<String> = ArrayList()

                                val listType = object : TypeToken<List<ReportReason>>() {}.type
                                val reportReason:MutableList<ReportReason> = Gson().fromJson(resp, listType)
                                // Populate the HashMap
                                reportReason.forEach { reason ->
                                    reportReasonsMap[reason.reason] = reason.id
                                    items.add(reason.reason)
                                }
                                powerSpinner.setItems(items)
                            }
                        }
                        is NetworkResult.Error -> {
                            showErrorDialog(this@ExtraTimeActivity, it.message!!)
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
    private fun setPropertyData() {
        try {
            propertyData?.let {
                propertyData?.host_profile_image?.let {
                    Glide.with(this@ExtraTimeActivity).load(BuildConfig.MEDIA_URL + it)
                        .into(binding.profileImage1)
                }
                propertyData?.hosted_by?.let {
                    binding.tvHostName.text = it
                }
                propertyData?.is_star_host?.let {
                    if (it == "true") {
                        binding.ivStar.visibility = View.VISIBLE
                    } else {
                        binding.ivStar.visibility = View.GONE
                    }
                }
                propertyData?.min_booking_hours?.let {
                 //   binding.tvResponseTime.text = "Respond within "+ convertHoursToHrMin(it.toDouble())
                }
                propertyData?.images?.let {
                    if (it.isNotEmpty()) {
                        Glide.with(this@ExtraTimeActivity).load(BuildConfig.MEDIA_URL + it.get(0))
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
                    val dummyData = formatDateyyyyMMddToMMMMddyyyy(it)

                    binding.tvDate.text = dummyData
                 //   binding.tvDate.text = date
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
                    }
                }
                calculatePrice()
            }
        } catch (e: Exception) {
            Log.d(ErrorDialog.TAG, e.message.toString())
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
                if (it.equals("23")){
                    binding.dateView2.visibility = View.GONE
                }else{
                    binding.dateView2.visibility = View.VISIBLE
                }
            }
            propertyData?.hourly_rate?.toDoubleOrNull()?.let { resp ->
                hour?.let {
                    hourlyTotal = (resp * it.toDouble())
                    binding.tvPrice.text = "$${truncateToTwoDecimalPlaces(hourlyTotal.toString())}"
                    totalPrice += hourlyTotal
                }
            }
            propertyData?.cleaning_fee?.toDoubleOrNull()?.let {
                binding.tvCleaningFee.text = "$${truncateToTwoDecimalPlaces(it.toString())}"
                totalPrice += it
            }
            propertyData?.service_fee?.toDoubleOrNull()?.let {resp ->
                hourlyTotal?.let {
                    val taxAmount = calculatePercentage(it,resp)
                    binding.tvZyvoServiceFee.text = "$${truncateToTwoDecimalPlaces(taxAmount.toString())}"
                    totalPrice += taxAmount
                }
            }
            propertyData?.tax?.toDoubleOrNull()?.let {resp ->
                hourlyTotal?.let {
                    val taxAmount = calculatePercentage(it,resp)
                    binding.tvTaxesPrice.text = "$${truncateToTwoDecimalPlaces(taxAmount.toString())}"
                    totalPrice += taxAmount

                }
            }
            addOnList?.let {
                if (it.isNotEmpty()){
                    binding.rladdOn.visibility = View.VISIBLE
                    val total = calculateTotalPrice(addOnList)
                    if (total==0.0){
                        binding.rladdOn.visibility = View.GONE
                    }
                    binding.tvAddOnPrice.text = "$${truncateToTwoDecimalPlaces(total.toString())}"
                    totalPrice += total
                }else{
                    binding.rladdOn.visibility = View.GONE
                }
            }
            // Apply Discount if Hours Exceed Discount Hour
            var discountAmount = 0.0
            propertyData?.bulk_discount_hour?.let { h ->
                hour?.let { cHr->
                    propertyData?.bulk_discount_rate?.let {
                        if (cHr.toInt() > h) {
                            discountAmount = (hourlyTotal * it.toDouble()) / 100
                            totalPrice -= discountAmount
                        }
                    }
                }
            }
            // Display Discount if Applied
            if (discountAmount > 0) {
                binding.tvDiscount.text = "-$${truncateToTwoDecimalPlaces(discountAmount.toString())}"
                binding.llDiscountLabel.visibility = View.VISIBLE
            } else {
                binding.llDiscountLabel.visibility = View.GONE
            }
            // Final Total Price Display
            binding.tvTotalPrice.text = "$${truncateToTwoDecimalPlaces(totalPrice.toString())}"
        }catch (e:Exception){
            Log.d(ErrorDialog.TAG,"calculatePrice ${e.message}")
        }
    }

    private fun calculateTotalPrice(addOnList: List<AddOn>): Double {
        return addOnList.filter { it.checked }
            .sumOf { it.price.toDoubleOrNull() ?: 0.0 }
    }

    private fun dialogReportIssue() {
        val dialog =  Dialog(this, R.style.BottomSheetDialog)
        dialog?.apply {
            setCancelable(true)
            setContentView(R.layout.report_violation)
            val crossButton: ImageView = findViewById(R.id.img_cross)
            val submit :RelativeLayout = findViewById(R.id.rl_submit_report)
            val txtSubmit : TextView = findViewById(R.id.txt_submit)
            val et_addiotnal_detail : EditText = findViewById(R.id.et_addiotnal_detail)
            val powerSpinner :PowerSpinnerView = findViewById(R.id.spinnerView1)
            submit.setOnClickListener {
                    if(et_addiotnal_detail.text.isEmpty()){
                    showToast(this@ExtraTimeActivity,AppConstant.additional)
                }
                else if(powerSpinner.text.toString().isEmpty()){
                    showToast(this@ExtraTimeActivity,AppConstant.spinner)
                }
                else{
                    reportViolation(session?.getUserId().toString(),
                        bookingId!!,
                        propertyData?.property_id.toString(),
                        reportReasonsMap.get(powerSpinner.text.toString()).toString(),
                        et_addiotnal_detail.text.toString(),dialog)
                }
            }
            // Handle item click
            powerSpinner.setOnSpinnerItemSelectedListener<String> { _, _, position, item ->

            }
            crossButton.setOnClickListener {
                dialog.dismiss()
            }

            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),  // Width 90% of screen
                ViewGroup.LayoutParams.WRAP_CONTENT                   // Height wrap content
            )
            window?.setBackgroundDrawableResource(android.R.color.transparent) // Optional


            show()
            listReportReasons(powerSpinner)
        }

    }

    private fun openDialogSuccess(){
        val dialog=Dialog(this, R.style.BottomSheetDialog)
        dialog?.apply {
                setCancelable(true)
                setContentView(R.layout.dialog_success_report_submit)
                window?.attributes = WindowManager.LayoutParams().apply {
                    copyFrom(window?.attributes)
                    width = WindowManager.LayoutParams.MATCH_PARENT
                    height = WindowManager.LayoutParams.MATCH_PARENT
                }

                val okBtn :ImageView = findViewById<ImageView>(R.id.img_cross)
                val cross :RelativeLayout = findViewById<RelativeLayout>(R.id.rl_okay)
                okBtn.setOnClickListener {
                    dialog.dismiss()
                }
                cross.setOnClickListener {
                    dialog.dismiss()
                }

                okBtn.setOnClickListener {
                    dialog.dismiss()
                }
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),  // Width 90% of screen
                ViewGroup.LayoutParams.WRAP_CONTENT                   // Height wrap content
            )
            window?.setBackgroundDrawableResource(android.R.color.transparent)
                show()
            }
    }

    private fun cancelScreen(){
        val dialog=Dialog(this, R.style.BottomSheetDialog)

        dialog?.apply {
            setCancelable(true)
            setContentView(R.layout.dialog_cancel)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }

            val okBtn :ImageView = findViewById<ImageView>(R.id.img_crss_1)
            val cross :RelativeLayout = findViewById<RelativeLayout>(R.id.yes_btn)
            val cancelBtn :RelativeLayout = findViewById(R.id.cancel_btn)

            cancelBtn.setOnClickListener {
                dialog.dismiss()
            }

            okBtn.setOnClickListener {
                dialog.dismiss()
            }
            cross.setOnClickListener {
                cancelBooking(dialog)
            }
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),  // Width 90% of screen
                ViewGroup.LayoutParams.WRAP_CONTENT                   // Height wrap content
            )
            window?.setBackgroundDrawableResource(android.R.color.transparent)


            show()
        }
    }

    private fun cancelBooking(dialog: Dialog) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                extraTimeViewModel.cancelBooking(
                    session?.getUserId().toString(),
                    /*"44"*/bookingId!!
                ).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                dialog.dismiss()
                                startActivity(Intent(this@ExtraTimeActivity, GuesMain::class.java))
                            }
                        }
                        is NetworkResult.Error -> {
                            showErrorDialog(this@ExtraTimeActivity, it.message!!)
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

    override fun onSubmitClicked(hour:String) {
        propertyData?.hourly_rate?.toDoubleOrNull()?.let { resp ->
            hour?.let {
                val hourlyTotal = (resp * it.toDouble())
                openNewDialog(hourlyTotal,hour)
            }
        }
    }

    fun openNewDialog(hourlTotal:Double,hour: String){
        val dialog =  Dialog(this, R.style.BottomSheetDialog)
        dialog?.apply {
            setCancelable(true)
            setContentView(R.layout.dialog_price_amount)
            val crossButton: ImageView = findViewById(R.id.imgCross)
            val submit :RelativeLayout = findViewById(R.id.yes_btn)
            val tvNewAmount:TextView = findViewById<TextView>(R.id.tvNewAmount)
            tvNewAmount.text = "Your new total amount is $${hourlTotal.toInt()}"
            val txtSubmit : RelativeLayout = findViewById(R.id.rl_cancel_btn)
            txtSubmit.setOnClickListener {
                dialog.dismiss()
            }
            submit.setOnClickListener {
                dialog.dismiss()
                val intent = Intent(this@ExtraTimeActivity
                    ,ExtraTimeChargesActivity::class.java)
                intent.putExtra(AppConstant.PRICE_TEXT,hourlTotal)
                intent.putExtra(AppConstant.ST_TIME,stTime)
                intent.putExtra(AppConstant.ED_TIME,edTime)
                intent.putExtra(AppConstant.PROPERTY_DATA,Gson().toJson(propertyData))
                intent.putExtra(AppConstant.PROPERTY_MILE,propertyMile)
                intent.putExtra(AppConstant.DATE,date)
                intent.putExtra(AppConstant.HOUR,hour)
                intent.putExtra(AppConstant.type,"Booking")
                intent.putExtra(AppConstant.BOOKING_ID_TEXT,bookingId)
                startActivity(intent)
            }

            crossButton.setOnClickListener {
                dialog.dismiss()
            }

            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),  // Width 90% of screen
                ViewGroup.LayoutParams.WRAP_CONTENT                   // Height wrap content
            )
            window?.setBackgroundDrawableResource(android.R.color.transparent) // Optional
            show()
        }
    }

}