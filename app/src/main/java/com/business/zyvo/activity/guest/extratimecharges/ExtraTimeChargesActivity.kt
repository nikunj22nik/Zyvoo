package com.business.zyvo.activity.guest.extratimecharges

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
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
import com.business.zyvo.activity.guest.checkout.model.MailingAddress
import com.business.zyvo.activity.guest.checkout.model.UserCards
import com.business.zyvo.activity.guest.extratime.ExtraTimeActivity
import com.business.zyvo.activity.guest.extratimecharges.viewmodel.ExtraTimeChargeViewModel
import com.business.zyvo.activity.guest.propertydetails.model.AddOn
import com.business.zyvo.activity.guest.propertydetails.model.PropertyData
import com.business.zyvo.adapter.AdapterAddPaymentCard
import com.business.zyvo.adapter.SetPreferred
import com.business.zyvo.databinding.ActivityExtraTimeChargesBinding
import com.business.zyvo.fragment.guest.SelectHourFragmentDialog
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.ErrorDialog.calculatePercentage
import com.business.zyvo.utils.ErrorDialog.convertHoursToHrMin
import com.business.zyvo.utils.ErrorDialog.formatConvertCount
import com.business.zyvo.utils.ErrorDialog.showToast
import com.business.zyvo.utils.NetworkMonitorCheck
import com.business.zyvo.viewmodel.PaymentViewModel
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.stripe.android.ApiResultCallback
import com.stripe.android.Stripe
import com.stripe.android.model.CardParams
import com.stripe.android.model.Token
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Objects

@AndroidEntryPoint
class ExtraTimeChargesActivity : AppCompatActivity(), SelectHourFragmentDialog.DialogListener,
    SetPreferred {
    lateinit var binding :ActivityExtraTimeChargesBinding
    private lateinit var addPaymentCardAdapter: AdapterAddPaymentCard

    var propertyData: PropertyData?=null
    var hour:String?=null
    var price:String?=null
    var stTime:String?=null
    var edTime:String?=null
    var propertyMile:String? = null
    var date:String?=null
    var type:String?= null
    var bookingId:String?=null
    var addOnList: MutableList<AddOn> = mutableListOf()
    var userCardsList: MutableList<UserCards> = mutableListOf()
    var selectuserCard:UserCards?=null
    var session: SessionManager?=null
    var customerId = ""

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
            if (type!=null && type.equals("Booking")){
                propertyData = Gson().fromJson(it.getString("propertyData"), PropertyData::class.java)
                hour = it.getString("hour")
                price = it.getString("price")
                stTime = it.getString("stTime")
                edTime = it.getString("edTime")
                propertyMile = it.getString("propertyMile")
                date = it.getString("date")
                bookingId = it.getString("bookingId")
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
         //   startActivity(Intent(this, ExtraTimeActivity::class.java))
        }


        binding.rlParking.setOnClickListener {
            if(binding.tvParkingRule.visibility == View.VISIBLE){
                binding.tvParkingRule.visibility= View.GONE
            }else{
                binding.tvParkingRule.visibility = View.VISIBLE
            }
        }

        addPaymentCardAdapter = AdapterAddPaymentCard(this, userCardsList,this);

        binding.recyclerViewPaymentCardList.layoutManager = LinearLayoutManager(this@ExtraTimeChargesActivity,
            LinearLayoutManager.VERTICAL,false)
        binding.recyclerViewPaymentCardList.adapter = addPaymentCardAdapter
        binding.rlHostRules.setOnClickListener {
            if(binding.tvHostRule.visibility == View.VISIBLE){
                binding.tvHostRule.visibility = View.GONE
            }
            else{
                binding.tvHostRule.visibility = View.VISIBLE
            }
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

        binding.dateView2.setOnClickListener {
            var dialog1 = SelectHourFragmentDialog()
            dialog1.setDialogListener(this)
            dialog1.show(supportFragmentManager, "MYDIALOF")
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



        binding.tvReadMoreLess.setCollapsedText("Read More")
        binding.tvReadMoreLess.setCollapsedTextColor(R.color.green_color_bar)

        binding.tvReadMoreLess.setCollapsedText("show more")
        binding.tvReadMoreLess.setCollapsedTextColor(R.color.green_color_bar)

        binding.myBooking.setOnClickListener {
           startActivity(Intent(this, ExtraTimeActivity::class.java))
        }

        setPropertyData()
        getUserCards()
    }

    @SuppressLint("SetTextI18n")
    private fun setPropertyData() {
        try {
            propertyData?.let {
                propertyData?.host_profile_image?.let {
                    Glide.with(this@ExtraTimeChargesActivity).load(AppConstant.BASE_URL + it)
                        .into(binding.profileImage1)
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
                    binding.tvResponseTime.text = "Respond within "+ convertHoursToHrMin(it.toDouble())
                }
                propertyData?.images?.let {
                    if (it.isNotEmpty()) {
                        Glide.with(this@ExtraTimeChargesActivity).load(AppConstant.BASE_URL + it.get(0))
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


    private fun dialogAddCard() {
        var dateManager = DateManager(this)
        val dialog =  Dialog(this, R.style.BottomSheetDialog)
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
                if (etCardHolderName.text.isEmpty()){
                    showToast(this@ExtraTimeChargesActivity,AppConstant.cardName)
                }else if (textMonth.text.isEmpty()){
                    showToast(this@ExtraTimeChargesActivity,AppConstant.cardMonth)
                }else if (textYear.text.isEmpty()){
                    showToast(this@ExtraTimeChargesActivity,AppConstant.cardYear)
                }else if (etCardCvv.text.isEmpty()){
                    showToast(this@ExtraTimeChargesActivity,AppConstant.cardCVV)
                }else
                {
                    LoadingUtils.showDialog(this@ExtraTimeChargesActivity, false)
                    val stripe = Stripe(this@ExtraTimeChargesActivity, BuildConfig.STRIPE_KEY)
                    var month: Int? = null
                    var year: Int? = null
                    val cardNumber: String =
                        Objects.requireNonNull(etCardNumber.text.toString().trim()).toString()
                    val cvvNumber: String =
                        Objects.requireNonNull(etCardCvv.text.toString().trim()).toString()
                    val name: String = etCardHolderName.text.toString().trim()
                    month = dateManager.getMonthNumber(textMonth.text.toString())
                    year = textYear.text.toString().toInt()
                    val card = CardParams(
                        cardNumber,
                        month!!,
                        Integer.valueOf(year!!),
                        cvvNumber,
                        name)
                    stripe?.createCardToken(card, null, null,
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
                                saveCardStripe(dialog,id,checkBox.isChecked)

                            }
                        })
                }
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            sameAsMailingAddress(etStreet,etCity,etState,etZipCode)
            show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun sameAsMailingAddress(etStreet: EditText,
                                     etCity: EditText,
                                     etState: EditText,
                                     etZipCode: EditText
    ) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                extraTimeChargeViewModel.sameAsMailingAddress(session?.getUserId().toString()).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                val mailingAddress: MailingAddress = Gson().fromJson(resp,
                                    MailingAddress::class.java)
                                mailingAddress?.let {
                                    it.street_address?.let {
                                        etStreet.setText(it)
                                    }
                                    it.city?.let {
                                        etCity.setText(it)
                                    }
                                    it.state?.let {
                                        etState.setText(it)
                                    }
                                    it.zip_code?.let {
                                        etZipCode.setText(it)
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
        }else{
            showErrorDialog(this,
                resources.getString(R.string.no_internet_dialog_msg))
        }
    }


    @SuppressLint("SetTextI18n")
    private fun saveCardStripe(dialog: Dialog,
                               tokenId:String,
                               saveasMail:Boolean) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                extraTimeChargeViewModel.saveCardStripe(session?.getUserId().toString(),
                    tokenId).collect {
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
        }else{
            showErrorDialog(this,
                resources.getString(R.string.no_internet_dialog_msg))
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
                                userCardsList = Gson().fromJson(resp.getAsJsonArray("cards"), listType)
                                if (userCardsList.isNotEmpty()){
                                    addPaymentCardAdapter.updateItem(userCardsList)
                                    for (card in userCardsList){
                                        if (card.is_preferred){
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
            val submit : RelativeLayout = findViewById(R.id.yes_btn)
            val txtSubmit : RelativeLayout = findViewById(R.id.rl_cancel_btn)
            val tvNewAmount:TextView = findViewById(R.id.tvNewAmount)
            tvNewAmount.text = "Your new total amount is $$hourlTotal"
            submit.setOnClickListener {
                this@ExtraTimeChargesActivity.hour = hour
                calculatePrice()
                dialog.dismiss()
            }
            crossButton.setOnClickListener {
                dialog.dismiss()
            }
            txtSubmit.setOnClickListener {
                dialog.dismiss()
            }
            window?.setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            window?.setBackgroundDrawableResource(android.R.color.transparent) // Optional
            show()
        }

    }

    override fun set(position: Int) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                extraTimeChargeViewModel.setPreferredCard(session?.getUserId().toString(),
                    userCardsList?.get(position)?.card_id!!).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                getUserCards()
                                showToast(this@ExtraTimeChargesActivity,resp.first)
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
        }else{
            showErrorDialog(this,
                resources.getString(R.string.no_internet_dialog_msg))
        }
    }

}