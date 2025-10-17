package com.business.zyvo.activity.guest.extratimecharges

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.BuildConfig
import com.business.zyvo.DateManager.DateManager
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.activity.ChatActivity
import com.business.zyvo.activity.GuesMain
import com.business.zyvo.activity.guest.checkout.CheckOutPayActivity
import com.business.zyvo.activity.guest.checkout.model.MailingAddress
import com.business.zyvo.activity.guest.checkout.model.UserCards
import com.business.zyvo.activity.guest.extratimecharges.viewmodel.ExtraTimeChargeViewModel
import com.business.zyvo.activity.guest.propertydetails.model.AddOn
import com.business.zyvo.activity.guest.propertydetails.model.PropertyData
import com.business.zyvo.adapter.AdapterAddPaymentCard
import com.business.zyvo.adapter.SetPreferred
import com.business.zyvo.databinding.ActivityExtraTimeChargesBinding
import com.business.zyvo.fragment.guest.SelectHourFragmentDialog
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.ErrorDialog.addHours
import com.business.zyvo.utils.ErrorDialog.calculatePercentage
import com.business.zyvo.utils.ErrorDialog.convertHoursToHrMin
import com.business.zyvo.utils.ErrorDialog.formatConvertCount
import com.business.zyvo.utils.ErrorDialog.formatDateyyyyMMddToMMMMddyyyy
import com.business.zyvo.utils.ErrorDialog.showToast
import com.business.zyvo.utils.ErrorDialog.truncateToTwoDecimalPlaces
import com.business.zyvo.utils.NetworkMonitorCheck
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.stripe.android.ApiResultCallback
import com.stripe.android.Stripe
import com.stripe.android.model.Address
import com.stripe.android.model.CardParams
import com.stripe.android.model.Token
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Objects

@AndroidEntryPoint
class ExtraTimeChargesActivity : AppCompatActivity(), SelectHourFragmentDialog.DialogListener,
    SetPreferred {
    lateinit var binding: ActivityExtraTimeChargesBinding
    private lateinit var addPaymentCardAdapter: AdapterAddPaymentCard

    var propertyData: PropertyData? = null
    var hour: String? = null
    var price: String? = null
    var stTime: String? = null
    var edTime: String? = null
    var propertyMile: String? = null
    var date: String? = null
    var type: String? = null
    var bookingId: String? = null
    var addOnList: MutableList<AddOn> = mutableListOf()
    var userCardsList: MutableList<UserCards> = mutableListOf()
    var selectuserCard: UserCards? = null
    var session: SessionManager? = null
    var customerId = ""
    var propertyId: String = "-1"
    var hostId: String = "-1"

    private val extraTimeChargeViewModel: ExtraTimeChargeViewModel by lazy {
        ViewModelProvider(this)[ExtraTimeChargeViewModel::class.java]
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityExtraTimeChargesBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        session = SessionManager(this)
        intent.extras?.let {
            type = it.getString("type")
            if (type != null && type.equals("Booking")) {
                propertyData =
                    Gson().fromJson(it.getString("propertyData"), PropertyData::class.java)
                hour = it.getString("hour")
                price = it.getString("price")
                stTime = it.getString("stTime")
                edTime = it.getString("edTime")
                propertyMile = it.getString("propertyMile")
                date = it.getString("date")
                bookingId = it.getString("bookingId")
                propertyId = propertyData?.property_id.toString()
                hostId = propertyData?.host_id.toString()
                Log.d(ErrorDialog.TAG, date.toString())
            }
        }
        // Observe the isLoading state
        lifecycleScope.launch {
            extraTimeChargeViewModel.isLoading.observe(this@ExtraTimeChargesActivity) { isLoading ->
                if (isLoading) {
                    LoadingUtils.showDialog(this@ExtraTimeChargesActivity, false)
                } else {
                    LoadingUtils.hideDialog()
                }
            }
        }
        binding.myBooking.setOnClickListener {
            if (selectuserCard == null) {
                showToast(this, AppConstant.selectCard)
            } else {
                getBookingExtensionTimeAmount(
                    bookingId!!,
                    hour!!,
                    binding.tvZyvoServiceFee.text.toString().replace("$", ""),
                    binding.tvTaxesPrice.text.toString().replace("$", ""),
                    binding.tvCleaningFee.text.toString().replace("$", ""),
                    binding.tvTotalPrice.text.toString().replace("$", ""),
                    binding.tvPrice.text.toString().replace("$", ""),
                    binding.tvDiscount.text.toString().replace("$", "")
                )
            }
        }


        binding.rlParking.setOnClickListener {
            if (binding.tvParkingRule.visibility == View.VISIBLE) {
                binding.tvParkingRule.visibility = View.GONE
            } else {
                binding.tvParkingRule.visibility = View.VISIBLE
            }
        }

        addPaymentCardAdapter = AdapterAddPaymentCard(this, userCardsList, this);

        binding.recyclerViewPaymentCardList.layoutManager = LinearLayoutManager(
            this@ExtraTimeChargesActivity,
            LinearLayoutManager.VERTICAL, false
        )
        binding.recyclerViewPaymentCardList.adapter = addPaymentCardAdapter
        binding.rlHostRules.setOnClickListener {
            if (binding.tvHostRule.visibility == View.VISIBLE) {
                binding.tvHostRule.visibility = View.GONE
            } else {
                binding.tvHostRule.visibility = View.VISIBLE
            }
        }


        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.dateView2.setOnClickListener {
            val dialog1 = SelectHourFragmentDialog()
            dialog1.setDialogListener(this)
            dialog1.show(supportFragmentManager, "MYDIALOF")
        }

        binding.rlCreditDebitCard.setOnClickListener {
            if (binding.rlCreditDebitRecycler.visibility == View.GONE) {
                binding.rlCreditDebitRecycler.visibility = View.VISIBLE
                binding.imageDropDown.setImageResource(R.drawable.ic_dropdown_close)
            } else {
                binding.rlCreditDebitRecycler.visibility = View.GONE
                binding.imageDropDown.setImageResource(R.drawable.ic_dropdown_open)
            }
        }
        binding.rlAddCard.setOnClickListener {
            dialogAddCard()
        }



        binding.tvReadMoreLess.setCollapsedText("Read More")
        binding.tvReadMoreLess.setCollapsedTextColor(R.color.green_color_bar)

        binding.tvReadMoreLess.setCollapsedText("show more")
        binding.tvReadMoreLess.setCollapsedTextColor(R.color.green_color_bar)


        setPropertyData()
        getUserCards()
        binding.rlMsgHost.setOnClickListener {
            // callingJoinChannelApi()
            callingMessageClickListner()
        }
    }

    private fun callingMessageClickListner() {
        if (binding.llMsgHost.visibility == View.VISIBLE) {
            binding.llMsgHost.visibility = View.GONE
        } else {
            binding.llMsgHost.visibility = View.VISIBLE
        }


        var messageSend = "I have a doubt"
        binding.doubt.setOnClickListener {
            binding.etShareMessage.setText("")
            binding.tvShareMessage.visibility = View.GONE
            messageSend = "I have a doubt"
            binding.tvAvailableDay.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
            binding.doubt.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box)
            binding.tvOtherReason.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)


        }

        binding.tvAvailableDay.setOnClickListener {
            binding.etShareMessage.setText("")
            binding.tvShareMessage.visibility = View.GONE
            messageSend = "Available days"
            binding.tvAvailableDay.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box)
            binding.doubt.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
            binding.tvOtherReason.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)


        }
        binding.tvOtherReason.setOnClickListener {

            binding.tvShareMessage.visibility = View.VISIBLE
            messageSend = "other"
            binding.doubt.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
            binding.tvAvailableDay.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
            binding.tvOtherReason.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box)

        }

        var writeMessage = ""

        binding.etShareMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                writeMessage += charSequence.toString()
                binding.tvAvailableDay.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
                binding.doubt.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
            }

            override fun afterTextChanged(editable: Editable?) {

            }
        })
        binding.rlSubmitMessage.setOnClickListener {
            val userInput = binding.etShareMessage.text.toString()
            if (userInput.length > 0) {
                messageSend = userInput
            }
            if (!messageSend.equals("other")) {
                propertyData?.let { pro ->
                    bookingId?.let {
                        val propertyid = it
                        val hostId = pro.host_id
                        val userId = SessionManager(this).getUserId()
                        var channelName = if (userId!! < hostId) {
                            "ZYVOOPROJ_" + userId + "_" + hostId + "_" + propertyid
                        } else {
                            "ZYVOOPROJ_" + hostId + "_" + userId + "_" + propertyid
                        }

                        Log.d(
                            "TESTING_IDS",
                            "PropertyId :- " + propertyid.toString() + " Hostid" + hostId
                        )

                        callingJoinChannelApi(messageSend)
                    }

                }
            } else {
                if (userInput.trim().isNotEmpty()) {
                    propertyData?.let { pro ->
                        bookingId?.let {
                            val propertyid = it
                            val hostId = pro.host_id
                            val userId = SessionManager(this).getUserId()
                            var channelName = if (userId!! < hostId) {
                                "ZYVOOPROJ_" + userId + "_" + hostId + "_" + propertyid
                            } else {
                                "ZYVOOPROJ_" + hostId + "_" + userId + "_" + propertyid
                            }

                            Log.d(
                                "TESTING_IDS",
                                "PropertyId :- " + propertyid.toString() + " Hostid" + hostId
                            )

                            callingJoinChannelApi(messageSend)
                        }
                    }
                } else {
                    binding.etShareMessage.error = "Please Enter something"
                }


            }

        }
    }


    private fun callingJoinChannelApi(messageSend: String) {
        if (hostId.equals("-1") == false && propertyId.equals("-1") == false) {
            lifecycleScope.launch {
                val session = SessionManager(this@ExtraTimeChargesActivity)
                val userId = session.getUserId()
                if (userId != null) {
                    LoadingUtils.showDialog(this@ExtraTimeChargesActivity, true)
                    var channelName: String = ""
                    if (userId < Integer.parseInt(hostId)) {
                        channelName = "ZYVOOPROJ_" + userId + "_" + hostId + "_" + bookingId
                    } else {
                        channelName = "ZYVOOPROJ_" + hostId + "_" + userId + "_" + bookingId
                    }
                    extraTimeChargeViewModel.joinChatChannel(
                        userId,
                        Integer.parseInt(hostId),
                        channelName,
                        "guest"
                    ).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                LoadingUtils.hideDialog()
                                val loggedInId =
                                    SessionManager(this@ExtraTimeChargesActivity).getUserId()

                                if (it.data?.receiver_id?.toInt() == loggedInId) {

                                    val userImage: String = it.data?.receiver_avatar.toString()
                                    Log.d("TESTING_PROFILE_HOST", userImage)
                                    val friendImage: String = it.data?.sender_avatar.toString()
                                    Log.d("TESTING_PROFILE_HOST", friendImage)
                                    var friendName: String = ""
                                    if (it.data?.sender_name != null) {
                                        friendName = it.data.sender_name
                                    }
                                    var userName = ""
                                    userName = it.data?.receiver_name.toString()
                                    val intent = Intent(
                                        this@ExtraTimeChargesActivity,
                                        ChatActivity::class.java
                                    )
                                    intent.putExtra("user_img", userImage).toString()
                                    SessionManager(this@ExtraTimeChargesActivity).getUserId()
                                        ?.let { it1 ->
                                            intent.putExtra(
                                                AppConstant.USER_ID,
                                                it1.toString()
                                            )
                                        }
                                    Log.d("TESTING", "REVIEW HOST" + channelName)
                                    intent.putExtra(AppConstant.CHANNEL_NAME, channelName)
                                    intent.putExtra(AppConstant.FRIEND_ID, hostId)
                                    intent.putExtra("friend_img", friendImage).toString()
                                    intent.putExtra("friend_name", friendName).toString()
                                    intent.putExtra("user_name", userName)
                                    intent.putExtra("sender_id", hostId)
                                    intent.putExtra("message", messageSend)
                                    startActivity(intent)
                                } else if (it.data?.sender_id?.toInt() == loggedInId) {
                                    val userImage: String = it.data?.sender_avatar.toString()
                                    Log.d("TESTING_PROFILE_HOST", userImage)
                                    val friendImage: String = it.data?.receiver_avatar.toString()
                                    Log.d("TESTING_PROFILE_HOST", friendImage)
                                    var friendName: String = ""
                                    if (it.data?.receiver_name != null) {
                                        friendName = it.data.receiver_name
                                    }
                                    var userName = ""
                                    userName = it.data?.sender_name.toString()
                                    val intent = Intent(
                                        this@ExtraTimeChargesActivity,
                                        ChatActivity::class.java
                                    )
                                    intent.putExtra("user_img", userImage).toString()
                                    SessionManager(this@ExtraTimeChargesActivity).getUserId()
                                        ?.let { it1 ->
                                            intent.putExtra(
                                                AppConstant.USER_ID,
                                                it1.toString()
                                            )
                                        }
                                    Log.d("TESTING", "REVIEW HOST" + channelName)
                                    intent.putExtra(AppConstant.CHANNEL_NAME, channelName)
                                    intent.putExtra(AppConstant.FRIEND_ID, hostId)
                                    intent.putExtra("friend_img", friendImage).toString()
                                    intent.putExtra("friend_name", friendName).toString()
                                    intent.putExtra("user_name", userName)
                                    intent.putExtra("sender_id", hostId)
                                    intent.putExtra("message", messageSend)
                                    startActivity(intent)
                                }
                            }

                            is NetworkResult.Error -> {
                                LoadingUtils.hideDialog()

                            }

                            else -> {
                                LoadingUtils.hideDialog()
                            }
                        }
                    }
                }
            }


        } else {
            Toast.makeText(
                this@ExtraTimeChargesActivity,
                "Error in Loading Chat",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun setPropertyData() {
        try {
            propertyData?.let {
                propertyData?.host_profile_image?.let {
                    Glide.with(this@ExtraTimeChargesActivity).load(BuildConfig.MEDIA_URL + it)
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
                    // binding.tvResponseTime.text = "Respond within " + convertHoursToHrMin(it.toDouble())
                }
                propertyData?.images?.let {
                    if (it.isNotEmpty()) {
                        Glide.with(this@ExtraTimeChargesActivity)
                            .load(BuildConfig.MEDIA_URL + it.get(0))
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
                    binding.tvTotalReview.text = "(" + formatConvertCount(it) + ")"
                }
                propertyMile?.let {
                    binding.tvMiles.text = "$it miles away"
                }
                date?.let {
                    Log.d(ErrorDialog.TAG, it)
                    val dummyData = formatDateyyyyMMddToMMMMddyyyy(it)
                    binding.tvDate.text = dummyData
                    //   binding.tvDate.text = date
                }

                hour?.let { resp ->
                    edTime?.let {
                        binding.tvTiming.text = "From $it to ${addHours(it, resp.toInt())}"
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


    private fun calculatePrice() {
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
                    binding.tvPrice.text = "$${truncateToTwoDecimalPlaces(hourlyTotal.toString())}"
                    totalPrice += hourlyTotal
                }
            }
            propertyData?.cleaning_fee?.toDoubleOrNull()?.let {
                binding.tvCleaningFee.text = "$${truncateToTwoDecimalPlaces(it.toString())}"
                totalPrice += it
            }
            propertyData?.service_fee?.toDoubleOrNull()?.let { resp ->
                hourlyTotal?.let {
                    val taxAmount = calculatePercentage(it, resp)
                    binding.tvZyvoServiceFee.text =
                        "$${truncateToTwoDecimalPlaces(taxAmount.toString())}"
                    totalPrice += taxAmount
                }
            }
            propertyData?.tax?.toDoubleOrNull()?.let { resp ->
                hourlyTotal?.let {
                    val taxAmount = calculatePercentage(it, resp)
                    binding.tvTaxesPrice.text =
                        "$${truncateToTwoDecimalPlaces(taxAmount.toString())}"
                    totalPrice += taxAmount

                }
            }
            addOnList?.let {
                if (it.isNotEmpty()) {
                    binding.rladdOn.visibility = View.VISIBLE
                    val total = calculateTotalPrice(addOnList)
                    if (total == 0.0) {
                        binding.rladdOn.visibility = View.GONE
                    }
                    binding.tvAddOnPrice.text = "$${truncateToTwoDecimalPlaces(total.toString())}"
                    totalPrice += total
                } else {
                    binding.rladdOn.visibility = View.GONE
                }
            }
            // Apply Discount if Hours Exceed Discount Hour
            var discountAmount = 0.0
            propertyData?.bulk_discount_hour?.let { h ->
                hour?.let { cHr ->
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
                binding.tvDiscount.text =
                    "-$${truncateToTwoDecimalPlaces(discountAmount.toString())}"
                binding.llDiscountLabel.visibility = View.VISIBLE
            } else {
                binding.llDiscountLabel.visibility = View.GONE
            }
            // Final Total Price Display
            binding.tvTotalPrice.text = "$${truncateToTwoDecimalPlaces(totalPrice.toString())}"
        } catch (e: Exception) {
            Log.d(ErrorDialog.TAG, "calculatePrice ${e.message}")
        }
    }

    private fun calculateTotalPrice(addOnList: List<AddOn>): Double {
        return addOnList.filter { it.checked }
            .sumOf { it.price.toDoubleOrNull() ?: 0.0 }
    }

    private fun getBookingExtensionTimeAmount(
        booking_id: String,
        extension_time: String,
        service_fee: String,
        tax: String,
        cleaning_fee: String,
        extension_total_amount: String,
        extension_booking_amount: String,
        discount_amount: String
    ) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                extraTimeChargeViewModel.getBookingExtensionTimeAmount(
                    session?.getUserId().toString(),
                    booking_id,
                    extension_time,
                    service_fee,
                    tax,
                    cleaning_fee,
                    extension_total_amount,
                    extension_booking_amount,
                    discount_amount
                ).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                val intent =
                                    Intent(this@ExtraTimeChargesActivity, GuesMain::class.java)
                                intent.putExtra("key_name", "12345")
                                startActivity(intent)
                                finish()
                                showToast(
                                    this@ExtraTimeChargesActivity,
                                    "Booking Extend successfully."
                                )
                            }
                        }

                        is NetworkResult.Error -> {
                            showErrorDialog(this@ExtraTimeChargesActivity, it.message!!)
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


    private fun dialogAddCard() {
        var street_address = ""
        var city = ""
        var state = ""
        var zip_code = ""
        val dateManager = DateManager(this)
        val dialog = Dialog(this, R.style.BottomSheetDialog)
        dialog?.apply {
            setCancelable(true)
            setContentView(R.layout.dialog_add_card_details)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            val textMonth: TextView = findViewById(R.id.textMonth)
            val textYear: TextView = findViewById(R.id.textYear)
            val etCardNumber: EditText = findViewById(R.id.etCardNumber)
            val etCardHolderName: EditText = findViewById(R.id.etCardHolderName)
            val etStreet: EditText = findViewById(R.id.etStreet)
            val etCity: EditText = findViewById(R.id.etCity)
            val etState: EditText = findViewById(R.id.etState)
            val etZipCode: EditText = findViewById(R.id.etZipCode)
            val etCardCvv: EditText = findViewById(R.id.etCardCvv)
            val checkBox: MaterialCheckBox = findViewById(R.id.checkBox)
            val imgcross: ImageView = findViewById(R.id.img_cross)
            checkBox.setOnClickListener {
                if (checkBox.isChecked) {
                    etStreet.setText(street_address)
                    etCity.setText(city)
                    etState.setText(state)
                    etZipCode.setText(zip_code)
                } else {
                    etStreet.text.clear()
                    etCity.text.clear()
                    etState.text.clear()
                    etZipCode.text.clear()
                }
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
            //vipin
            etCardNumber.addTextChangedListener(object : TextWatcher {
                private var isFormatting: Boolean = false
                private var previousText: String = ""

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    previousText = s.toString()
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (isFormatting) return

                    isFormatting = true

                    val digitsOnly = s.toString().replace(" ", "")
                    val formatted = StringBuilder()

                    for (i in digitsOnly.indices) {
                        formatted.append(digitsOnly[i])
                        if ((i + 1) % 4 == 0 && i != digitsOnly.length - 1) {
                            formatted.append(" ")
                        }
                    }

                    if (formatted.toString() != s.toString()) {
                        etCardNumber.setText(formatted.toString())
                        etCardNumber.setSelection(formatted.length)
                    }

                    isFormatting = false
                }
            })
            //end
            imgcross.setOnClickListener {
                dismiss()
            }
            submitButton.setOnClickListener {
                if (etCardHolderName.text.trim().isEmpty()) {
                    showToast(this@ExtraTimeChargesActivity, AppConstant.cardName)
                }else if(etCardHolderName.text.toString().length >30){
                    showToast(this@ExtraTimeChargesActivity, "Please Enter Card Holder Name less than 30 character")
                }
                else if (etCardNumber.text.trim().isEmpty()) {
                    showToast(this@ExtraTimeChargesActivity, AppConstant.cardNubmer)
                } else if (!ErrorDialog.isValidCardNumber(etCardNumber.text.toString())) {
                    showToast(this@ExtraTimeChargesActivity, AppConstant.cardValidNubmer)
                } else if (textMonth.text.isEmpty()) {
                    showToast(this@ExtraTimeChargesActivity, AppConstant.cardMonth)
                } else if (textYear.text.isEmpty()) {
                    showToast(this@ExtraTimeChargesActivity, AppConstant.cardYear)
                } else if (etCardCvv.text.trim().isEmpty()) {
                    showToast(this@ExtraTimeChargesActivity, AppConstant.cardCVV)
                } else {
                    LoadingUtils.showDialog(this@ExtraTimeChargesActivity, false)
                    val stripe = Stripe(this@ExtraTimeChargesActivity, BuildConfig.STRIPE_KEY)
                    var month: Int? = null
                    var year: Int? = null

                    //vipin
                    val cardNumber: String =
                        Objects.requireNonNull(etCardNumber.text.toString().replace(" ", "").trim())
                            .toString()
                    Log.d("checkCardNumber", cardNumber)


                    val cvvNumber: String =
                        Objects.requireNonNull(etCardCvv.text.toString().trim()).toString()
                    val name: String = etCardHolderName.text.toString().trim()
                    month = dateManager.getMonthNumber(textMonth.text.toString())
                    year = textYear.text.toString().toInt()
                    // Billing Address fields
                    val street = etStreet.text.toString().trim()
                    val city = etCity.text.toString().trim()
                    val state = etState.text.toString().trim()
                    val zip = etZipCode.text.toString().trim()
                    // Create Address object
                    val billingAddress = Address.Builder()
                        .setLine1(street)
                        .setCity(city)
                        .setState(state)
                        .setPostalCode(zip)
                        .build()
                    val card = CardParams(
                        cardNumber,
                        month!!,
                        Integer.valueOf(year!!),
                        cvvNumber,
                        name,
                        address = billingAddress
                    )
                    stripe?.createCardToken(
                        card, null, null,
                        object : ApiResultCallback<Token> {
                            override fun onError(e: Exception) {
                                Log.d("******  Token Error :-", "${e.message}")
                                showErrorDialog(this@ExtraTimeChargesActivity, e.message.toString())
                                LoadingUtils.hideDialog()
                            }

                            override fun onSuccess(result: Token) {
                                val id = result.id
                                Log.d("******  Token payment :-", "data $id")
                                LoadingUtils.hideDialog()
                                saveCardStripe(dialog, id, checkBox.isChecked)

                            }
                        })
                }
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            sameAsMailingAddress { mailingAddress ->
                // Do something with the address here
                if (mailingAddress != null) {
                    Log.d(ErrorDialog.TAG, mailingAddress.toString())
                    mailingAddress?.let {
                        it.street_address?.let {
                            street_address = it
                        }
                        it.city?.let {
                            city = it
                        }
                        it.state?.let {
                            state = it
                        }
                        it.zip_code?.let {
                            zip_code = it
                        }
                    }
                }
            }
            show()
        }
    }


    private fun sameAsMailingAddress(
        onAddressReceived: (MailingAddress?) -> Unit
    ) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                extraTimeChargeViewModel.sameAsMailingAddress(session?.getUserId().toString())
                    .collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                it.data?.let { resp ->
                                    val mailingAddress: MailingAddress = Gson().fromJson(
                                        resp,
                                        MailingAddress::class.java
                                    )
                                    onAddressReceived(mailingAddress)
                                }
                            }

                            is NetworkResult.Error -> {
                                showErrorDialog(this@ExtraTimeChargesActivity, it.message!!)
                                onAddressReceived(null)
                            }

                            else -> {
                                Log.v(ErrorDialog.TAG, "error::" + it.message)
                                onAddressReceived(null)
                            }
                        }
                    }
            }
        } else {
            onAddressReceived(null)
            showErrorDialog(
                this,
                resources.getString(R.string.no_internet_dialog_msg)
            )
        }
    }


    private fun saveCardStripe(
        dialog: Dialog,
        tokenId: String,
        saveasMail: Boolean
    ) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                extraTimeChargeViewModel.saveCardStripe(
                    session?.getUserId().toString(),
                    tokenId
                ).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                dialog.dismiss()
                                getUserCards()
                            }
                        }

                        is NetworkResult.Error -> {
                            showErrorDialog(this@ExtraTimeChargesActivity, it.message!!)
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

    @SuppressLint("SetTextI18n")
    private fun getUserCards() {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                extraTimeChargeViewModel.getUserCards(session?.getUserId().toString()).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                customerId = resp.get("stripe_customer_id").asString
                                val listType = object : TypeToken<List<UserCards>>() {}.type
                                userCardsList =
                                    Gson().fromJson(resp.getAsJsonArray("cards"), listType)
                                if (userCardsList.isNotEmpty()) {
                                    addPaymentCardAdapter.updateItem(userCardsList)
                                    for (card in userCardsList) {
                                        if (card.is_preferred) {
                                            selectuserCard = card
                                            break
                                        }
                                    }
                                }
                            }
                        }

                        is NetworkResult.Error -> {
                            showErrorDialog(this@ExtraTimeChargesActivity, it.message!!)
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

    override fun onSubmitClicked(hour: String) {
        propertyData?.hourly_rate?.toDoubleOrNull()?.let { resp ->
            hour?.let {
                val hourlyTotal = (resp * it.toDouble())
                openNewDialog(hourlyTotal, hour)
            }
        }
    }

    fun openNewDialog(hourlTotal: Double, hour: String) {
        val dialog = Dialog(this, R.style.BottomSheetDialog)
        dialog?.apply {
            setCancelable(true)

            setContentView(R.layout.dialog_price_amount)
            val crossButton: ImageView = findViewById(R.id.imgCross)
            val submit: RelativeLayout = findViewById(R.id.yes_btn)
            val txtSubmit: RelativeLayout = findViewById(R.id.rl_cancel_btn)
            val tvNewAmount: TextView = findViewById(R.id.tvNewAmount)
            tvNewAmount.text = "Your new total amount is $${hourlTotal.toInt()}"
            submit.setOnClickListener {
                this@ExtraTimeChargesActivity.hour = hour
                hour?.let { resp ->
                    edTime?.let {
                        binding.tvTiming.text = "From $it to ${addHours(it, resp.toInt())}"
                    }
                }
                calculatePrice()
                dialog.dismiss()
            }
            crossButton.setOnClickListener {
                dialog.dismiss()
            }
            txtSubmit.setOnClickListener {
                dialog.dismiss()
            }
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window?.setBackgroundDrawableResource(android.R.color.transparent) // Optional
            show()
        }

    }

    override fun set(position: Int) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                extraTimeChargeViewModel.setPreferredCard(
                    session?.getUserId().toString(),
                    userCardsList?.get(position)?.card_id!!
                ).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                getUserCards()
                                showToast(this@ExtraTimeChargesActivity, resp.first)
                            }
                        }

                        is NetworkResult.Error -> {
                            showErrorDialog(this@ExtraTimeChargesActivity, it.message!!)
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

}