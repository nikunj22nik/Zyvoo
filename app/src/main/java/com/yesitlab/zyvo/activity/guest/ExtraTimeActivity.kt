package com.yesitlab.zyvo.activity.guest

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
import com.yesitlab.zyvo.DateManager.DateManager
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.activity.GuesMain
import com.yesitlab.zyvo.databinding.ActivityExtraTimeBinding
import com.yesitlab.zyvo.fragment.guest.SelectHourFragmentDialog

class ExtraTimeActivity : AppCompatActivity(),SelectHourFragmentDialog.DialogListener {

    lateinit var binding :ActivityExtraTimeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityExtraTimeBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.reportIssue.setOnClickListener {
           dialogReportIssue()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        clickListeners()

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

        binding.rlMsgHost.setOnClickListener {
            if(binding.llMsgHost.visibility == View.VISIBLE){
                binding.llMsgHost.visibility = View.GONE
            }
            else{
                binding.llMsgHost.visibility = View.VISIBLE
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
            var dialog1 = SelectHourFragmentDialog()
            dialog1.setDialogListener(this)
            dialog1.show(supportFragmentManager, "MYDIALOF")
        }

        binding.myBooking.setOnClickListener {
            val intent = Intent(this, GuesMain::class.java)
            intent.putExtra("key_name","12345")
            startActivity(intent)
            finish()
        }

    }

    private fun dialogReportIssue() {
        var dateManager = DateManager(this)
        val dialog =  Dialog(this, R.style.BottomSheetDialog)
        dialog?.apply {
            setCancelable(true)
            setContentView(R.layout.report_violation)
//            window?.attributes = WindowManager.LayoutParams().apply {
//                copyFrom(window?.attributes)
//                width = WindowManager.LayoutParams.MATCH_PARENT
//                height = WindowManager.LayoutParams.MATCH_PARENT
//            }

            val crossButton: ImageView = findViewById(R.id.img_cross)
            val submit :RelativeLayout = findViewById(R.id.rl_submit_report)
            val txtSubmit : TextView = findViewById(R.id.txt_submit)

            submit.setOnClickListener {
                if (txtSubmit.text.toString().trim().equals("Submitted") == false) {
                    txtSubmit.setText("Submitted")
                }else{
                    dialog.dismiss()
                     openDialogSuccess()
                }
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

                var okBtn :ImageView = findViewById<ImageView>(R.id.img_cross)
                var cross :RelativeLayout = findViewById<RelativeLayout>(R.id.rl_okay)
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

            var okBtn :ImageView = findViewById<ImageView>(R.id.img_crss_1)
            var cross :RelativeLayout = findViewById<RelativeLayout>(R.id.yes_btn)
            var cancelBtn :RelativeLayout = findViewById(R.id.cancel_btn)

            cancelBtn.setOnClickListener {
                dialog.dismiss()
            }

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
            val submit :RelativeLayout = findViewById(R.id.yes_btn)
            val txtSubmit : RelativeLayout = findViewById(R.id.rl_cancel_btn)

            submit.setOnClickListener {
               dialog.dismiss()
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