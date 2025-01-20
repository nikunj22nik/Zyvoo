package com.business.zyvo.activity.guest

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.business.zyvo.DateManager.DateManager
import com.business.zyvo.R
import com.business.zyvo.adapter.AdapterAddPaymentCard
import com.business.zyvo.databinding.ActivityExtraTimeChargesBinding
import com.business.zyvo.fragment.guest.SelectHourFragmentDialog
import com.business.zyvo.viewmodel.PaymentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExtraTimeChargesActivity : AppCompatActivity(), SelectHourFragmentDialog.DialogListener {
    lateinit var binding :ActivityExtraTimeChargesBinding
    private lateinit var addPaymentCardAdapter: AdapterAddPaymentCard

    private val paymentCardViewHolder: PaymentViewModel by lazy {
        ViewModelProvider(this)[PaymentViewModel::class.java]
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
        binding.myBooking.setOnClickListener {
            startActivity(Intent(this,ExtraTimeActivity::class.java))
        }


        binding.rlParking.setOnClickListener {
            if(binding.tvParkingRule.visibility == View.VISIBLE){
                binding.tvParkingRule.visibility= View.GONE
            }else{
                binding.tvParkingRule.visibility = View.VISIBLE
            }
        }
        addPaymentCardAdapter = AdapterAddPaymentCard(this, mutableListOf())
        paymentCardViewHolder.paymentCardList.observe(this, Observer { payment ->
            addPaymentCardAdapter.updateItem(payment)
        })
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
        binding.tvReadMoreLess.setCollapsedTextColor(com.business.zyvo.R.color.green_color_bar)

        binding.tvReadMoreLess.setCollapsedText("show more")
        binding.tvReadMoreLess.setCollapsedTextColor(com.business.zyvo.R.color.green_color_bar)

        binding.myBooking.setOnClickListener {
           startActivity(Intent(this,ExtraTimeActivity::class.java))
        }


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


    override fun onSubmitClicked() {
       openNewDialog()
    }

    fun openNewDialog(){
        val dialog =  Dialog(this, R.style.BottomSheetDialog)
        dialog?.apply {
            setCancelable(true)

            setContentView(R.layout.dialog_price_amount)

//            window?.attributes = WindowManager.LayoutParams().apply {
//                copyFrom(window?.attributes)
//                width = WindowManager.LayoutParams.MATCH_PARENT
//                height = WindowManager.LayoutParams.MATCH_PARENT
//            }

            val crossButton: ImageView = findViewById(R.id.imgCross)
            val submit : RelativeLayout = findViewById(R.id.yes_btn)
            val txtSubmit : RelativeLayout = findViewById(R.id.rl_cancel_btn)

            submit.setOnClickListener {
                dialog.dismiss()
            }

            crossButton.setOnClickListener {
                dialog.dismiss()
            }

            window?.setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            window?.setBackgroundDrawableResource(android.R.color.transparent) // Optional

            show()

        }

    }

}