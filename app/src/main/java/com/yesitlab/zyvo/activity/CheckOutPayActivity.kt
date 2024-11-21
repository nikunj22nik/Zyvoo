package com.yesitlab.zyvo.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yesitlab.zyvo.DateManager.DateManager
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.activity.guest.ExtraTimeActivity
import com.yesitlab.zyvo.adapter.AdapterAddOn
import com.yesitlab.zyvo.adapter.AdapterAddPaymentCard
import com.yesitlab.zyvo.databinding.ActivityCheckOutPayBinding
import com.yesitlab.zyvo.viewmodel.PaymentViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class CheckOutPayActivity : AppCompatActivity() {

    lateinit var binding : ActivityCheckOutPayBinding
    lateinit var adapterAddon : AdapterAddOn

    private lateinit var addPaymentCardAdapter: AdapterAddPaymentCard

    private val paymentCardViewHolder: PaymentViewModel by lazy {
        ViewModelProvider(this)[PaymentViewModel::class.java]
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

        initialization()
        clickListeneres()
        callingSelectionOfDate()
        callingSelectionOfTime()
        messageHostListener()


    }


    fun messageHostListener(){

        var dateManager = DateManager(this)

        binding.rlHours.setOnClickListener {
            dateManager.showHourSelectionDialog(this) { selectedHour ->
                    binding.tvHours.setText(selectedHour.toString())
                }
        }

        binding.textSaveChangesButtonTime.setOnClickListener {
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

        val days = resources.getStringArray(R.array.day).toList()


        binding.spinnerLanguage.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.spinnerLanguage.arrowAnimate = false
        binding.spinnerLanguage.setItems(days)
        binding.spinnerLanguage.setIsFocusable(true)
        val recyclerView = binding.spinnerLanguage.getSpinnerRecyclerView()

       // Add item decoration for spacing
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

        binding.spinnermonth.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.spinnermonth.arrowAnimate = false
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
        binding.spinneryear.setItems(yearsStringList.subList(0,16))
        binding.spinneryear.setIsFocusable(true)

        binding.spinneryear.post {
            binding.spinneryear.spinnerPopupWidth = binding.spinneryear.width
        }

        binding.endAmPm.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.endAmPm.arrowAnimate = false
        binding.endAmPm.setItems(am_pm_list)
        binding.endAmPm.setIsFocusable(true)

        binding.endAmPm.post {
            binding.endAmPm.spinnerPopupWidth = binding.endAmPm.width
        }


        binding.startAmPm.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.startAmPm.arrowAnimate = false
        binding.startAmPm.setItems(am_pm_list)
        binding.startAmPm.setIsFocusable(true)

        binding.startAmPm.post {
            binding.startAmPm.spinnerPopupWidth = binding.startAmPm.width
        }

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
            binding.tvDate.setText(binding.spinnermonth.text.toString()+" "+binding.spinnerLanguage.text.toString()+","+binding.spinneryear.text.toString())
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
            }else{
                binding.rlCreditDebitRecycler.visibility = View.GONE
            }
        }
        binding.rlAddCard.setOnClickListener {
            dialogAddCard()
        }

    }

    fun initialization(){

        adapterAddon = AdapterAddOn(this@CheckOutPayActivity,getAddOnList().subList(0,4))
        binding.recyclerAddOn.layoutManager = LinearLayoutManager(this@CheckOutPayActivity, LinearLayoutManager.VERTICAL ,false)
        binding.recyclerAddOn.adapter = adapterAddon
        addPaymentCardAdapter = AdapterAddPaymentCard(this, mutableListOf())

        binding.recyclerViewPaymentCardList.layoutManager = LinearLayoutManager(this@CheckOutPayActivity,LinearLayoutManager.VERTICAL,false)
        binding.recyclerViewPaymentCardList.adapter = addPaymentCardAdapter

        val dayarray = resources.getStringArray(R.array.day)
        val adapterday: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, dayarray)

        // Assign the adapter to this ListActivity
//        binding.spinnerDay.setAdapter<ArrayAdapter<*>>(adapterday)
//
//        val montharray = resources.getStringArray(R.array.month)
//
//        val adaptermonth: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, montharray)
//
//        // Assign the adapter to this ListActivity
//        binding.spinnerMonth.setAdapter<ArrayAdapter<*>>(adaptermonth)

        // Set years
        val years = ArrayList<String?>()
        val thisYear = Calendar.getInstance()[Calendar.YEAR]
        for (i in 1950..thisYear) {
            years.add(i.toString())
        }

//        val adapteryear: ArrayAdapter<String?> = ArrayAdapter<String?>(this, android.R.layout.simple_spinner_item, years)
//        binding.spinnerYear.setAdapter<ArrayAdapter<*>>(adapteryear)

        paymentCardViewHolder.paymentCardList.observe(this, Observer { payment ->
            addPaymentCardAdapter.updateItem(payment)
        })

        binding.dateView.setOnClickListener {
            binding.relCalendarLayouts.visibility=View.VISIBLE
        }

    }

    private fun getAddOnList(): MutableList<String> {
        var list = mutableListOf<String>()
        list.add("Computer Screen")
        list.add("Bed Sheets")
        list.add("Phone charger")
        list.add("Ring Light")
        list.add("Left Light")
        list.add("Water Bottle")
        return list
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

}