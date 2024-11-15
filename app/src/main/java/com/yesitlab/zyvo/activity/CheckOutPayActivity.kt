package com.yesitlab.zyvo.activity

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.yesitlab.zyvo.DateManager.DateManager
import com.yesitlab.zyvo.R
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

        binding.rlAddCard.setOnClickListener {
            dialogAddCard()
        }

    }

    fun initialization(){

        adapterAddon = AdapterAddOn(this@CheckOutPayActivity,getAddOnList().subList(0,4))
        binding.recyclerAddOn.layoutManager = LinearLayoutManager(this@CheckOutPayActivity, LinearLayoutManager.VERTICAL ,false)
        binding.recyclerAddOn.adapter = adapterAddon

        addPaymentCardAdapter = AdapterAddPaymentCard(this, mutableListOf())
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