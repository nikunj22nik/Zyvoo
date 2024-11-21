package com.yesitlab.zyvo.activity.guest

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
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
import com.yesitlab.zyvo.databinding.ActivityExtraTimeBinding

class ExtraTimeActivity : AppCompatActivity() {

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
    }

    private fun dialogReportIssue() {
        var dateManager = DateManager(this)
        val dialog =  Dialog(this, R.style.BottomSheetDialog)
        dialog?.apply {
            setCancelable(true)
            setContentView(R.layout.report_violation)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }

            val crossButton: ImageView = findViewById(R.id.img_cross)
            val submit :RelativeLayout = findViewById(R.id.rl_submit_report)
            val txtSubmit : TextView = findViewById(R.id.txt_submit)

            submit.setOnClickListener {
                if (txtSubmit.text.toString().trim().equals("Submitted") == false) {
                    txtSubmit.setText("Submitted")
                }else{
                     openDialogSuccess()
                }
            }

            crossButton.setOnClickListener {
                dialog.dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }

    }

    private fun openDialogSuccess(){

        val dialog=Dialog(this, R.style.BottomSheetDialog)

              dialog?.apply {
                setCancelable(true)
                setContentView(R.layout.dialog_success)
                window?.attributes = WindowManager.LayoutParams().apply {
                    copyFrom(window?.attributes)
                    width = WindowManager.LayoutParams.MATCH_PARENT
                    height = WindowManager.LayoutParams.MATCH_PARENT
                }

                var okBtn :ImageView = findViewById<ImageView>(R.id.imageCross)
                var cross :TextView = findViewById<TextView>(R.id.textOkayButton)
                okBtn.setOnClickListener {
                    dialog.dismiss()
                }
                cross.setOnClickListener {
                    dialog.dismiss()
                }

                okBtn.setOnClickListener {
                    dialog.dismiss()
                }
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
            okBtn.setOnClickListener {
                dialog.dismiss()
            }
            cross.setOnClickListener {
                dialog.dismiss()
            }

            okBtn.setOnClickListener {
                dialog.dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

<<<<<<< Updated upstream
=======
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

            txtSubmit.setOnClickListener {
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
>>>>>>> Stashed changes

}