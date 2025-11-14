package com.business.zyvo.activity.guest.checkout

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.BuildConfig
import com.business.zyvo.DateManager.DateManager
import com.business.zyvo.ErrorMessage
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.LoadingUtils.Companion.showSuccessDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.activity.ChatActivity
import com.business.zyvo.activity.guest.checkout.model.MailingAddress
import com.business.zyvo.activity.guest.checkout.model.ReqAddOn
import com.business.zyvo.activity.guest.checkout.model.UserCards
import com.business.zyvo.activity.guest.checkout.viewmodel.CheckOutPayViewModel
import com.business.zyvo.activity.guest.extratime.ExtraTimeActivity
import com.business.zyvo.activity.guest.propertydetails.model.AddOn
import com.business.zyvo.activity.guest.propertydetails.model.PropertyData
import com.business.zyvo.adapter.AdapterAddPaymentCard
import com.business.zyvo.adapter.guest.AdapterProAddOn
import com.business.zyvo.databinding.ActivityCheckOutPayBinding
import com.business.zyvo.locationManager.LocationManager
import com.business.zyvo.onClickSelectCard
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.ErrorDialog.calculatePercentage
import com.business.zyvo.utils.ErrorDialog.formatConvertCount
import com.business.zyvo.utils.ErrorDialog.formatDateyyyyMMddToMMMMddyyyy
import com.business.zyvo.utils.ErrorDialog.showToast
import com.business.zyvo.utils.ErrorDialog.truncateToTwoDecimalPlaces
import com.business.zyvo.utils.NetworkMonitorCheck
import com.business.zyvo.utils.PrepareData
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
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
import kotlinx.coroutines.withContext
import java.util.Arrays
import java.util.Calendar
import java.util.Objects

@AndroidEntryPoint
class CheckOutPayActivity : AppCompatActivity(), onClickSelectCard {

    lateinit var binding: ActivityCheckOutPayBinding
    lateinit var adapterAddon: AdapterProAddOn
    private lateinit var addPaymentCardAdapter: AdapterAddPaymentCard
    var propertyData: PropertyData? = null
    var hour: String? = null
    var price: String? = null
    var stTime: String? = null
    var edTime: String? = null
    var propertyMile: String? = null
    var date: String? = null
    var addOnList: MutableList<AddOn> = mutableListOf()
    var session: SessionManager? = null
    var userCardsList: MutableList<UserCards> = mutableListOf()
    var selectuserCard: UserCards? = null
    var customerId = ""
    var etAddress: EditText? = null
    var etCity1: EditText? = null
    var zipcode: EditText? = null
    var etState1: EditText? = null
    private val checkOutPayViewModel: CheckOutPayViewModel by lazy {
        ViewModelProvider(this)[CheckOutPayViewModel::class.java]
    }
    var latitude: String = "0.00"
    var longitude: String = "0.00"

    @RequiresApi(Build.VERSION_CODES.O)
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

        intent.extras?.let {
            propertyData =
                Gson().fromJson(it.getString(AppConstant.PROPERTY_DATA), PropertyData::class.java)
            hour = it.getString(AppConstant.HOUR)
            price = it.getString(AppConstant.PRICE_TEXT)
            stTime = it.getString(AppConstant.ST_TIME)
            edTime = it.getString(AppConstant.ED_TIME)
            propertyMile = it.getString(AppConstant.PROPERTY_MILE)
            date = it.getString(AppConstant.DATE)

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
        callingMessageClickListner()


        binding.readMore.setOnClickListener {
            if (binding.readMore.text.toString().equals(AppConstant.READ_MORE, true)) {
                binding.readMore.text = AppConstant.READ_LESS
                binding.tvReadMoreLess.maxLines = Integer.MAX_VALUE
            } else {
                binding.readMore.text = AppConstant.READ_MORE
                binding.tvReadMoreLess.maxLines = 3
            }
        }


    }

    private fun callingMessageClickListner() {
        binding.rlMsgHost.setOnClickListener {
            if (binding.llMsgHost.visibility == View.VISIBLE) {
                binding.llMsgHost.visibility = View.GONE
            } else {
                binding.llMsgHost.visibility = View.VISIBLE
            }
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
            if (!messageSend.equals(AppConstant.OTHER)) {
                propertyData?.let {
                    val propertyid = it.property_id
                    val hostId = it.host_id
                    val userId = SessionManager(this).getUserId()
                    var channelName = ""
                    if (userId != null && hostId != null) {
                        if (userId < hostId) {
                            channelName =
                                AppConstant.ZYVOOPROJ + userId + "_" + hostId + "_" + propertyid
                        } else {
                            channelName =
                                AppConstant.ZYVOOPROJ + hostId + "_" + userId + "_" + propertyid
                        }
                    }

                    callingJoinChannel(propertyid, hostId, userId!!, channelName, messageSend)
                }
            } else {
                if (userInput.trim().isNotEmpty()) {
                    propertyData?.let {
                        val propertyid = it.property_id
                        val hostId = it.host_id
                        val userId = SessionManager(this).getUserId()
                        val channelName = if (userId!! < hostId) {
                            AppConstant.ZYVOOPROJ + userId + "_" + hostId + "_" + propertyid
                        } else {
                            AppConstant.ZYVOOPROJ + hostId + "_" + userId + "_" + propertyid
                        }

                        callingJoinChannel(
                            propertyid,
                            hostId,
                            userId,
                            channelName,
                            userInput.trim()
                        )
                    }
                } else {
                    binding.etShareMessage.error = AppConstant.PLEASE_ENTER_SOMETHING
                }
            }
        }
    }


    private fun callingJoinChannel(
        property_id: Int,
        hostId: Int,
        userId: Int,
        channel: String,
        messageSend: String
    ) {
        lifecycleScope.launch {
            LoadingUtils.showDialog(this@CheckOutPayActivity, false)
            var channel: String = ""
            if (userId < hostId) {
                channel = AppConstant.ZYVOOPROJ + userId + "_" + hostId + "_" + property_id
            } else {
                channel = AppConstant.ZYVOOPROJ + hostId + "_" + userId + "_" + property_id
            }
            checkOutPayViewModel.joinChatChannel(
                userId,
                hostId,
                channel,
                AppConstant.GUEST_CHANNEL_TYPE
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        LoadingUtils.hideDialog()
                        val loggedInId = SessionManager(this@CheckOutPayActivity).getUserId()
                        if (it.data?.receiver_id?.toInt() == loggedInId) {
                            val userImage: String = it.data?.receiver_avatar.toString()
                            val friendImage: String = it.data?.sender_avatar.toString()
                            var friendName: String = ""
                            if (it.data?.sender_name != null) {
                                friendName = it.data.sender_name
                            }
                            var userName = ""
                            userName = it.data?.receiver_name.toString()
                            val intent = Intent(this@CheckOutPayActivity, ChatActivity::class.java)
                            intent.putExtra(AppConstant.USER_IMG, userImage).toString()
                            SessionManager(this@CheckOutPayActivity).getUserId()?.let { it1 ->
                                intent.putExtra(
                                    AppConstant.USER_ID,
                                    it1.toString()
                                )
                            }
                            intent.putExtra(AppConstant.CHANNEL_NAME, channel)
                            intent.putExtra(AppConstant.FRIEND_ID, hostId)
                            intent.putExtra(AppConstant.FRIEND_IMG, friendImage).toString()
                            intent.putExtra(AppConstant.FRIEND_NAME, friendName).toString()
                            intent.putExtra(AppConstant.USER_NAME, userName)
                            intent.putExtra(AppConstant.SENDER_ID, hostId)
                            intent.putExtra(AppConstant.MESSAGE, messageSend)
                            startActivity(intent)
                        } else if (it.data?.sender_id?.toInt() == loggedInId) {
                            val userImage: String = it.data?.sender_avatar.toString()
                            val friendImage: String = it.data?.receiver_avatar.toString()
                            var friendName: String = ""
                            if (it.data?.receiver_name != null) {
                                friendName = it.data.receiver_name
                            }
                            var userName = ""
                            userName = it.data?.sender_name.toString()
                            val intent = Intent(this@CheckOutPayActivity, ChatActivity::class.java)
                            intent.putExtra(AppConstant.USER_IMG, userImage).toString()
                            SessionManager(this@CheckOutPayActivity).getUserId()?.let { it1 ->
                                intent.putExtra(
                                    AppConstant.USER_ID,
                                    it1.toString()
                                )
                            }

                            intent.putExtra(AppConstant.CHANNEL_NAME, channel)
                            intent.putExtra(AppConstant.FRIEND_ID, hostId)
                            intent.putExtra(AppConstant.FRIEND_IMG, friendImage).toString()
                            intent.putExtra(AppConstant.FRIEND_NAME, friendName).toString()
                            intent.putExtra(AppConstant.USER_NAME, userName)
                            intent.putExtra(AppConstant.SENDER_ID, hostId)
                            intent.putExtra(AppConstant.MESSAGE, messageSend)
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


    @SuppressLint("SetTextI18n")
    private fun setPropertyData() {
        try {
            propertyData?.let {
                Log.d(
                    "TESTING_DATE_TIME",
                    "DATE " + date + " Hours" + hour + " StartTime" + stTime + " EndTime" + edTime
                )
                propertyData?.host_profile_image?.let {
                    Glide.with(this@CheckOutPayActivity).load(BuildConfig.MEDIA_URL + it)
                        .into(binding.profileImageHost)
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
                propertyData?.images?.let {
                    if (it.isNotEmpty()) {
                        Glide.with(this@CheckOutPayActivity).load(BuildConfig.MEDIA_URL + it.get(0))
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
                    val dummyData = formatDateyyyyMMddToMMMMddyyyy(it)

                    binding.tvDate.text = dummyData
                }
                stTime?.let { resp ->
                    edTime?.let {
                        binding.tvTiming.text = "From $resp to $it"
                        val fetchTimeDetails = PrepareData.extractTimeDetails(stTime!!, edTime!!)

                        binding.endHour.setText(fetchTimeDetails.startHour)
                        binding.endMinute.setText(fetchTimeDetails.startMinute)
                        binding.endAmPm.setText(fetchTimeDetails.startAmPm)

                        binding.startHour.setText(fetchTimeDetails.endHour)
                        binding.startMinute.setText(fetchTimeDetails.endMinute)
                        binding.startAmPm.setText(fetchTimeDetails.endAmPm)

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
                        if (addOnList.size <= 4) {
                            binding.tvShowMore.visibility = View.GONE

                        } else {
                            binding.tvShowMore.visibility = View.VISIBLE
                        }
                        Log.d("CheckAddOn", addOnList.toString())
                    } else {
                        binding.llAddOns.visibility = View.GONE
                    }
                }
                calculatePrice()
            }
        } catch (e: Exception) {
            Log.d(ErrorDialog.TAG, e.message.toString())
        }
    }

    @SuppressLint("SetTextI18n")
    fun messageHostListener() {
        binding.textSaveChangesButtonTime.setOnClickListener {
            stTime = binding.endHour.text.toString() + ":" +
                    binding.endMinute.text.toString() + " " + binding.endAmPm.text.toString()
            edTime = binding.startHour.text.toString() + ":" +
                    binding.startMinute.text.toString() + " " + binding.startAmPm.text.toString()
            binding.tvTiming.text = "From " + stTime + "to " + edTime
            binding.relTime.visibility = View.GONE
        }

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }


    private fun callingSelectionOfTime() {
        val hoursArray = Array(24) { i -> String.format("%02d", i + 1) } // Ensures "01, 02, 03..."
        val hoursList: List<String> = hoursArray.toList().subList(0, 12)
        val minutesArray = Array(60) { i -> String.format("%02d", i) } // Ensures "00, 01, 02..."
        val minutesList: List<String> = minutesArray.toList()
        val amPmList = listOf<String>("AM", "PM")
        binding.endHour.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.endHour.arrowAnimate = false
        binding.endHour.setItems(hoursList)
        binding.endHour.setIsFocusable(true)
        val recyclerView = binding.endHour.getSpinnerRecyclerView()
        val spacing = 16 // Spacing in pixels
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
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
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.top = spacing
            }
        })


        binding.endAmPm.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.endAmPm.arrowAnimate = false
        binding.endAmPm.setItems(amPmList)
        binding.endAmPm.setIsFocusable(true)
        val recyclerView45 = binding.endAmPm.getSpinnerRecyclerView()
        recyclerView45.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.top = spacing
            }
        })


        binding.startAmPm.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.startAmPm.arrowAnimate = false
        binding.startAmPm.setItems(amPmList)
        binding.startAmPm.setIsFocusable(true)
        val recyclerView46 = binding.endAmPm.getSpinnerRecyclerView()
        recyclerView46.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
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
            finish()
        }

        binding.rlHours.setOnClickListener {
            finish()
        }

    }

    fun callingSelectionOfDate() {
        val months = listOf(
            AppConstant.JANUARY,
            AppConstant.FEBRUARY,
            AppConstant.MARCH,
            AppConstant.APRIL,
            AppConstant.MAY,
            AppConstant.JUNE,
            AppConstant.JULY,
            AppConstant.AUGUST,
            AppConstant.SEPTEMBER,
            AppConstant.OCTOBER,
            AppConstant.NOVEMBER,
            AppConstant.DECEMBER
        )
        val am_pm_list = listOf("AM", "PM")
        val years = (2024..2050).toList()
        val yearsStringList = years.map { it.toString() }
        val days = resources.getStringArray(R.array.day).toList()
        binding.spinnerDate.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.spinnerDate.spinnerPopupHeight = 400
        binding.spinnerDate.arrowAnimate = false
        binding.spinnerDate.setItems(days)
        binding.spinnerDate.setIsFocusable(true)

        val recyclerView = binding.spinnerDate.getSpinnerRecyclerView()
        // Add item decoration for spacing
        val spacing = 16 // Spacing in pixels
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
            ) {
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
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.top = spacing
            }
        })

        binding.spinneryear.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.spinneryear.arrowAnimate = false
        binding.spinneryear.spinnerPopupHeight = 400
        binding.spinneryear.setItems(yearsStringList.subList(0, 16))
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
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.top = spacing
            }
        })


        val recyclerView5 = binding.endAmPm.getSpinnerRecyclerView()
        recyclerView5.addItemDecoration(object : RecyclerView.ItemDecoration() {

            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.top = spacing
            }

        })

        val recyclerView1 = binding.spinneryear.getSpinnerRecyclerView()
        recyclerView1.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.top = spacing
            }

        })

        binding.dateView.setOnClickListener {
            finish()
        }

        binding.textSaveChangesButton.setOnClickListener {
            val dateFormated =
                "" + binding.spinneryear.text.toString() + "-" + PrepareData.monthNameToNumber(
                    binding.spinnermonth.text.toString()
                ) + "-" + binding.spinnerDate.text.toString()

            Log.d("Formatted_Date", dateFormated)
            date = dateFormated
            val dummy = formatDateyyyyMMddToMMMMddyyyy(dateFormated)
            binding.tvDate.text = dummy
            binding.relCalendarLayouts.visibility = View.GONE
        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun clickListeneres() {
        binding.rlParking.setOnClickListener {
            if (binding.tvParkingRule.visibility == View.VISIBLE) {
                binding.tvParkingRule.visibility = View.GONE
            } else {
                binding.tvParkingRule.visibility = View.VISIBLE
            }
        }
        binding.rlHostRules.setOnClickListener {
            if (binding.tvHostRule.visibility == View.VISIBLE) {
                binding.tvHostRule.visibility = View.GONE
            } else {
                binding.tvHostRule.visibility = View.VISIBLE
            }
        }
        binding.rlConfirmPay.setOnClickListener {
            if (selectuserCard == null) {
                showToast(this, AppConstant.selectCard)
            } else {
                val addons = ArrayList<ReqAddOn>()
                propertyData?.let {
                    for (addon in addOnList) {
                        if (addon.checked) {
                            val add = ReqAddOn(addon.name, addon.price.toDouble())
                            addons.add(add)
                        }
                    }
                    stTime?.let { resp ->
                        edTime?.let {
                            date?.let { it1 ->
                                bookProperty(
                                    propertyData?.property_id.toString(),
                                    it1,
                                    date + " " + ErrorDialog.convertToTimeFormat(
                                        stTime!!
                                    ),
                                    date + " " + ErrorDialog.convertToTimeFormat(
                                        edTime!!
                                    ),
                                    binding.tvPrice.text.toString().replace("$", ""),
                                    binding.tvTotalPrice.text.toString().replace("$", ""),
                                    customerId,
                                    selectuserCard?.card_id!!, createAddonFields(addons),
                                    binding.tvZyvoServiceFee.text.toString().replace("$", ""),
                                    binding.tvTaxesPrice.text.toString().replace("$", ""),
                                    binding.tvDiscount.text.toString().replace("$", "")
                                )
                            }
                        }
                    }
                }
            }
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

    }


    fun createAddonFields(addons: List<ReqAddOn>): Map<String, String> {
        val fields = mutableMapOf<String, String>()
        addons.forEachIndexed { index, addon ->
            fields["addons[$index][name]"] = addon.name
            fields["addons[$index][price]"] = addon.price.toString()
        }
        return fields
    }

    private fun initialization() {
        adapterAddon = AdapterProAddOn(
            this@CheckOutPayActivity, addOnList,
            object : AdapterProAddOn.onItemClickListener {
                override fun onItemClick(list: MutableList<AddOn>, position: Int) {
                    addOnList = list
                    calculatePrice()
                }
            })
        binding.recyclerAddOn.layoutManager =
            LinearLayoutManager(this@CheckOutPayActivity, LinearLayoutManager.VERTICAL, false)
        binding.recyclerAddOn.adapter = adapterAddon

        showingMoreText()
        addPaymentCardAdapter = AdapterAddPaymentCard(this, userCardsList, this);

        binding.recyclerViewPaymentCardList.layoutManager =
            LinearLayoutManager(this@CheckOutPayActivity, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewPaymentCardList.adapter = addPaymentCardAdapter

        // Set years
        val years = ArrayList<String?>()
        val thisYear = Calendar.getInstance()[Calendar.YEAR]
        for (i in 1950..thisYear) {
            years.add(i.toString())
        }

        binding.dateView.setOnClickListener {
            binding.relCalendarLayouts.visibility = View.VISIBLE
        }

    }

    private fun showingMoreText() {
        val text = binding.tvShowMore.text
        val spannableString = SpannableString(text).apply {
            setSpan(UnderlineSpan(), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.tvShowMore.text = spannableString
        binding.tvShowMore.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        binding.tvShowMore.paint.isAntiAlias = true
        if (addOnList.size <= 4) {
            binding.tvShowMore.visibility = View.GONE

        } else {
            binding.tvShowMore.visibility = View.VISIBLE
        }
        binding.tvShowMore.setOnClickListener {
            adapterAddon.toggleList()
            if (binding.tvShowMore.text.equals(AppConstant.SHOW_LESS)) {
                binding.tvShowMore.text = AppConstant.SHOW_MORE
            } else {
                binding.tvShowMore.text = AppConstant.SHOW_LESS
            }
        }
    }

    private fun dialogAddCard() {
        var street_address = ""
        var city = ""
        var state = ""
        var zip_code = ""
        val dateManager = DateManager(this)
        val dialog = Dialog(this, R.style.BottomSheetDialog)
        dialog.apply {
            setCancelable(true)
            setContentView(R.layout.dialog_add_card_details)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
            val textMonth: TextView = findViewById(R.id.textMonth)
            val textYear: TextView = findViewById(R.id.textYear)
            val etCardNumber: EditText = findViewById(R.id.etCardNumber)
            val etCardHolderName: EditText = findViewById(R.id.etCardHolderName)
            val submitButton: TextView = findViewById(R.id.textSubmitButton)
            val etStreet: EditText = findViewById(R.id.etStreet)
            val etCity: EditText = findViewById(R.id.etCity)
            val etState: EditText = findViewById(R.id.etState)
            val etZipCode: EditText = findViewById(R.id.etZipCode)
            val etCardCvv: EditText = findViewById(R.id.etCardCvv)
            val checkBox: MaterialCheckBox = findViewById(R.id.checkBox)
            val imgcross: ImageView = findViewById(R.id.img_cross)

            etAddress = etStreet
            etCity1 = etCity
            zipcode = etZipCode
            etState1 = etState
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

            locationSelection(etStreet)

            textMonth.setOnClickListener {
                dateManager.showMonthSelectorDialog { selectedMonth ->
                    textMonth.text = selectedMonth
                }
            }
            textYear.setOnClickListener {
                dateManager.showYearPickerDialog { selectedYear ->
                    textYear.text = selectedYear.toString()
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
                    showToast(this@CheckOutPayActivity, AppConstant.cardName)
                } else if (etCardHolderName.text.toString().length > 30) {
                    showToast(this@CheckOutPayActivity, ErrorMessage.ENTER_CARD_HOLDER_NAME)
                } else if (etCardNumber.text.trim().isEmpty()) {
                    showToast(this@CheckOutPayActivity, AppConstant.cardNubmer)
                } else if (!ErrorDialog.isValidCardNumber(etCardNumber.text.toString())) {
                    showToast(this@CheckOutPayActivity, AppConstant.cardValidNubmer)
                } else if (textMonth.text.isEmpty()) {
                    showToast(this@CheckOutPayActivity, AppConstant.cardMonth)
                } else if (textYear.text.isEmpty()) {
                    showToast(this@CheckOutPayActivity, AppConstant.cardYear)
                } else if (etCardCvv.text.trim().isEmpty()) {
                    showToast(this@CheckOutPayActivity, AppConstant.cardCVV)
                } else {
                    LoadingUtils.showDialog(this@CheckOutPayActivity, false)
                    val stripe = Stripe(this@CheckOutPayActivity, BuildConfig.STRIPE_KEY)
                    var month: Int? = null
                    var year: Int? = null
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
                                showErrorDialog(this@CheckOutPayActivity, e.message.toString())
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
            show()
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

        }
    }

    fun locationSelection(etStreet: EditText) {
        etStreet.setOnClickListener {

            val apiKey = getString(R.string.api_key_location)
            if (!Places.isInitialized()) {
                Places.initialize(this, apiKey)
            }

            val fields: List<Place.Field> = Arrays.asList<Place.Field>(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )

            val intent: Intent =
                Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this)
            startActivityForResult(intent, 103)
        }

        etStreet.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val apiKey = getString(R.string.api_key_location)
                if (!Places.isInitialized()) {
                    Places.initialize(this, apiKey)
                }
                val fields: List<Place.Field> = Arrays.asList<Place.Field>(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG
                )

                val intent: Intent =
                    Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(this)
                startActivityForResult(intent, 103)
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun sameAsMailingAddress(onAddressReceived: (MailingAddress?) -> Unit) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                checkOutPayViewModel.sameAsMailingAddress(session?.getUserId().toString()).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                val mailingAddress: MailingAddress =
                                    Gson().fromJson(resp, MailingAddress::class.java)
                                onAddressReceived(mailingAddress)
                            }
                        }

                        is NetworkResult.Error -> {
                            showErrorDialog(this@CheckOutPayActivity, it.message!!)
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


    @SuppressLint("SetTextI18n")
    private fun saveCardStripe(dialog: Dialog, tokenId: String, saveasMail: Boolean) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                checkOutPayViewModel.saveCardStripe(
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
                            showErrorDialog(this@CheckOutPayActivity, it.message!!)
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
                checkOutPayViewModel.getUserCards(session?.getUserId().toString()).collect {
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
                            showErrorDialog(this@CheckOutPayActivity, it.message!!)
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

            val taxAmountStr = propertyData?.tax_amount
            val taxAmountDouble = taxAmountStr?.toDoubleOrNull()
            if (taxAmountStr.isNullOrEmpty() || taxAmountDouble == null || taxAmountDouble <= 0.0) {
                binding.rltax.visibility = View.GONE
            } else {
                binding.rltax.visibility = View.VISIBLE
                propertyData?.tax_amount?.toDoubleOrNull()?.let { resp ->
                    hourlyTotal?.let { amount ->
                        hour?.let { hr ->
                            val taxAmount = (amount + (hr.toInt() * resp))
                            binding.tvTaxesPrice.text =
                                "$${truncateToTwoDecimalPlaces(taxAmount.toString())}"
                            totalPrice += taxAmount
                        }
                    }
                }
            }
            addOnList.let {
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

    fun calculateTotalPrice(addOnList: List<AddOn>): Double {
        return addOnList.filter { it.checked }
            .sumOf { it.price.toDoubleOrNull() ?: 0.0 }
    }


    private fun bookProperty(
        property_id: String, booking_date: String, booking_start: String,
        booking_end: String, booking_amount: String, total_amount: String,
        customer_id: String, card_id: String, addons: Map<String, String>,
        service_fee: String, tax: String, discount_amount: String
    ) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                checkOutPayViewModel.bookProperty(
                    session?.getUserId().toString(),
                    property_id,
                    booking_date,
                    booking_start,
                    booking_end,
                    booking_amount,
                    total_amount,
                    customer_id,
                    card_id,
                    addons,
                    service_fee,
                    tax,
                    discount_amount
                ).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                var intent = Intent(
                                    this@CheckOutPayActivity, ExtraTimeActivity::class.java
                                )
                                intent.putExtra(
                                    AppConstant.PRICE_TEXT,
                                    binding.tvPrice.text.toString().replace("$", "")
                                )
                                intent.putExtra(AppConstant.ST_TIME, stTime)
                                intent.putExtra(AppConstant.ED_TIME, edTime)
                                intent.putExtra(
                                    AppConstant.PROPERTY_DATA,
                                    Gson().toJson(propertyData)
                                )
                                intent.putExtra(AppConstant.PROPERTY_MILE, propertyMile)
                                intent.putExtra(AppConstant.DATE, date)
                                intent.putExtra(
                                    AppConstant.BOOKING_ID_TEXT,
                                    resp.getAsJsonObject("booking").get("id").asInt.toString()
                                )
                                intent.putExtra(
                                    AppConstant.HOUR,
                                    binding.tvHours.text.toString().replace(" Hours", "")
                                )
                                startActivity(intent)
                                finish()
                                showToast(
                                    this@CheckOutPayActivity,
                                    ErrorMessage.BOOKING_CREATED_SUCCESSFULLY
                                )
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
        } else {
            showErrorDialog(
                this,
                resources.getString(R.string.no_internet_dialog_msg)
            )
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data)
            var address: String = place.address
            val latLng = place.latLng

            latitude = latLng.latitude.toString()
            longitude = latLng.longitude.toString()
            fetchAddressDetails(address, latitude.toDouble(), longitude.toDouble())
            etCity1?.isEnabled = true
            if (latitude == null) {
                latitude = "0.0001"
            }

            if (longitude == null) {
                longitude = "0.0001"
            }

            var add = address
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            etCity1?.isEnabled = true
            // TODO: Handle the error.
            val status = Autocomplete.getStatusFromIntent(data)
            Toast.makeText(this, "Error: " + status.statusMessage, Toast.LENGTH_LONG)
                .show()
        }

    }

    private fun fetchAddressDetails(address: String, latitude: Double, longitude: Double) {
        lifecycleScope.launch {
            try {
                val addressDetails = withContext(Dispatchers.IO) {
                    LocationManager(this@CheckOutPayActivity).getAddressFromCoordinates(
                        latitude,
                        longitude
                    )
                }
                etAddress?.setText(address)
                etAddress?.text?.length?.let { etAddress?.setSelection(it) }
                etCity1?.setText(addressDetails.city)
                zipcode?.setText(addressDetails.postalCode)

                etState1?.setText(addressDetails.state)


            } catch (e: Exception) {
                Log.e("Geocoder", "Error fetching address: ${e.message}")
                Toast.makeText(
                    this@CheckOutPayActivity,
                    ErrorMessage.UNABLE_FETCH_ADDRESS_DETAILS,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    override fun itemClickCard(pos: Int, type: String) {
        val cardIdSelect = userCardsList[pos].card_id
        when (type) {
            AppConstant.DELETE_CARD -> {
                deleteCardMethods(cardIdSelect, pos)
            }

            AppConstant.PRIMARY_CARD -> {
                setPrimary(pos)
            }
        }
    }

    fun setPrimary(position: Int) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                checkOutPayViewModel.setPreferredCard(
                    session?.getUserId().toString(),
                    userCardsList[position].card_id
                ).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                userCardsList.forEach { card ->
                                    card.is_preferred = false
                                }
                                userCardsList[position].is_preferred = true
                                selectuserCard = userCardsList[position]
                                addPaymentCardAdapter.updateItem(userCardsList)
                                showToast(this@CheckOutPayActivity, resp.first)
                            }
                        }

                        is NetworkResult.Error -> {
                            showSuccessDialog(this@CheckOutPayActivity, it.message!!)
                        }

                        else -> {
                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                        }
                    }
                }
            }
        } else {
            showErrorDialog(
                this@CheckOutPayActivity,
                resources.getString(R.string.no_internet_dialog_msg)
            )
        }
    }

    private fun deleteCardMethods(id: String, pos: Int) {
        lifecycleScope.launch {
            if (!NetworkMonitorCheck._isConnected.value) {
                LoadingUtils.showErrorDialog(
                    this@CheckOutPayActivity,
                    resources.getString(R.string.no_internet_dialog_msg)
                )
            } else {
                deleteCard(id, pos)
            }

        }
    }


    private fun deleteCard(id: String, position: Int) {
        Log.d("idType", id)
        lifecycleScope.launch {
            checkOutPayViewModel.deleteCard(session?.getUserId().toString(), id).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        userCardsList.removeAt(position)
                        addPaymentCardAdapter.updateItem(userCardsList)
                        it.data?.let { it1 -> showSuccessDialog(this@CheckOutPayActivity, it1) }
                        if (userCardsList.isNotEmpty()) {
                            binding.recyclerViewPaymentCardList.visibility = View.VISIBLE
                        } else {
                            binding.recyclerViewPaymentCardList.visibility = View.GONE
                        }
                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(this@CheckOutPayActivity, it.message!!)
                    }

                    else -> {
                    }
                }
            }
        }
    }

}