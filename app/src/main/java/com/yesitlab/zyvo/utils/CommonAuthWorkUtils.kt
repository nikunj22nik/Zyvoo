package com.yesitlab.zyvo.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.navigation.NavController
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.activity.AuthActivity
import com.yesitlab.zyvo.activity.GuesMain
import com.yesitlab.zyvo.databinding.DialogAddWishlistBinding
import com.yesitlab.zyvo.session.SessionManager
import com.yesitlab.zyvo.viewmodel.WishlistViewModel
import `in`.aabhasjindal.otptextview.OtpTextView

class CommonAuthWorkUtils(var context: Context, navController: NavController) {
     var navController = navController
    private lateinit var otpDigits: Array<EditText>
    fun dialogLogin(context: Context?){
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_login)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            var imageCross =  findViewById<ImageView>(R.id.imageCross)
            var imageEmailSocial =  findViewById<ImageView>(R.id.imageEmailSocial)
            var etMobileNumber =  findViewById<EditText>(R.id.etMobileNumber)
            var textContinueButton =  findViewById<TextView>(R.id.textContinueButton)
            var checkBox =  findViewById<CheckBox>(R.id.checkBox)
            var textKeepLogged =  findViewById<TextView>(R.id.textKeepLogged)
            var textForget =  findViewById<TextView>(R.id.textForget)
            var textDontHaveAnAccount =  findViewById<TextView>(R.id.textDontHaveAnAccount)
            var textRegister =  findViewById<TextView>(R.id.textRegister)

            textRegister.setOnClickListener{
                dialogRegister(context)
                dismiss()
            }

            textForget.setOnClickListener{
                dialogForgotPassword(context)
                dismiss()
            }
            imageEmailSocial.setOnClickListener{
                dialogLoginEmail(context)
                dismiss()
            }


            textContinueButton.setOnClickListener{
                var text = "Login Successful"

                dialogOtp(context,text)
                dismiss()
            }
            imageCross.setOnClickListener{
                dismiss()
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }}


    fun dialogRegister(context: Context?){
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_registration)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            var imageCross =  findViewById<ImageView>(R.id.imageCross)
            var imageEmailSocial =  findViewById<ImageView>(R.id.imageEmailSocial)
            var etMobileNumber =  findViewById<EditText>(R.id.etMobileNumber)
            var textContinueButton =  findViewById<TextView>(R.id.textContinueButton)
            var checkBox =  findViewById<CheckBox>(R.id.checkBox)
            var textKeepLogged =  findViewById<TextView>(R.id.textKeepLogged)

            var textLoginButton =  findViewById<TextView>(R.id.textLoginButton)

            textLoginButton.setOnClickListener{
                dialogLogin(context)
                dismiss()
            }

            textContinueButton.setOnClickListener{
                var text = "register Successful"
                dialogOtp(context,text)
                dismiss()
            }

            imageEmailSocial.setOnClickListener{
                dialogRegisterEmail(context)
                dismiss()
            }

            imageCross.setOnClickListener{
                dismiss()
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            show()
        }}


    fun dialogLoginEmail(context: Context?){
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_login_email)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            var imageCross =  findViewById<ImageView>(R.id.imageCross)

            var textLoginButton =  findViewById<TextView>(R.id.textLoginButton)
            var checkBox =  findViewById<CheckBox>(R.id.checkBox)
            var textKeepLogged =  findViewById<TextView>(R.id.textKeepLogged)
            var textForget =  findViewById<TextView>(R.id.textForget)
            var textDontHaveAnAccount =  findViewById<TextView>(R.id.textDontHaveAnAccount)
            var textRegister =  findViewById<TextView>(R.id.textRegister)

            textRegister.setOnClickListener{
                dialogRegisterEmail(context)
                dismiss()
            }

            textForget.setOnClickListener{
                dialogForgotPassword(context)
                dismiss()
            }
            textLoginButton.setOnClickListener{
                dismiss()
            }
            imageCross.setOnClickListener{
                dismiss()
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }}

    fun dialogRegisterEmail(context: Context?){
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_register_email)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            var imageCross =  findViewById<ImageView>(R.id.imageCross)

            var textCreateAccountButton =  findViewById<TextView>(R.id.textCreateAccountButton)
            var checkBox =  findViewById<CheckBox>(R.id.checkBox)
            var textKeepLogged =  findViewById<TextView>(R.id.textKeepLogged)


            var textLoginHere =  findViewById<TextView>(R.id.textLoginHere)

            textLoginHere.setOnClickListener{
                dialogLoginEmail(context)
                dismiss()
            }
            textCreateAccountButton.setOnClickListener{
                var text = "register Successful"

                dialogOtp(context,text)

                dismiss()
            }

            imageCross.setOnClickListener{
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }}



    fun dialogForgotPassword(context: Context?){
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_forgot_password)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            var imageCross =  findViewById<ImageView>(R.id.imageCross)
            var etMobileNumber =  findViewById<EditText>(R.id.etMobileNumber)
            var etEmail =  findViewById<EditText>(R.id.etEmail)
            var textSubmitButton =  findViewById<TextView>(R.id.textSubmitButton)
            textSubmitButton.setOnClickListener{
                var text = "Your password has been changed\n successfully."

                dialogOtp(context,text)

                dismiss()
            }
            imageCross.setOnClickListener{
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }}

    fun dialogNumberVerification(context: Context?){
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_number_verification)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            var imageCross =  findViewById<ImageView>(R.id.imageCross)
            var textSubmitButton =  findViewById<TextView>(R.id.textSubmitButton)
            textSubmitButton.setOnClickListener{

                var text = "Your Phone/ Email has been Verified\n  successfully."

                dialogOtp(context,text)

                dismiss()
            }
            imageCross.setOnClickListener{
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }}


    fun dialogChangeName(context: Context?){
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_change_names)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }


            var textSaveChangesButton =  findViewById<TextView>(R.id.textSaveChangesButton)
            textSaveChangesButton.setOnClickListener{

                dismiss()
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }}


    fun dialogEmailVerification(context: Context?){
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_email_verification)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            var imageCross =  findViewById<ImageView>(R.id.imageCross)

            var textSubmitButton =  findViewById<TextView>(R.id.textSubmitButton)
            textSubmitButton.setOnClickListener{
                var text = "Your Phone/ Email has been Verified\n  successfully."

                dialogOtp(context,text)

                dismiss()
            }
            imageCross.setOnClickListener{
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }}


    @SuppressLint("SuspiciousIndentation")
    fun dialogOtp(context: Context, text: String){
        val dialog =  Dialog(context, R.style.BottomSheetDialog)
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_otp_verification)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }

            val imageCross =  findViewById<ImageView>(R.id.imageCross)

            val textResend =  findViewById<TextView>(R.id.textResend)

            val textSubmitButton =  findViewById<TextView>(R.id.textSubmitButton)


            otpDigits = arrayOf<EditText>(
                findViewById(R.id.otp_digit1),
                findViewById(R.id.otp_digit2),
                findViewById(R.id.otp_digit3),
                findViewById(R.id.otp_digit4)
            )

            for (i in 0 until otpDigits.size) {
                val index = i
                otpDigits.get(i).setOnClickListener { v ->
                    otpDigits.get(index).requestFocus()
                }

                otpDigits.get(i).addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        if (s.length == 1 && index < otpDigits.size - 1) {
                            otpDigits.get(index + 1).requestFocus()
                        } else if (s.length == 0 && index > 0) {
                            otpDigits.get(index - 1).requestFocus()
                        }
                    }

                    override fun afterTextChanged(s: Editable) {}
                })

            }

            textSubmitButton.setOnClickListener{
                if (text == "Your password has been changed\n" + " successfully."){
                    dialogNewPassword(context,text)
                }
                else if(text.equals("Login Successful")){
                    var session =SessionManager(context)
                    session.setUserId(1)
                    var intent = Intent(context,GuesMain::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                    context.startActivity(intent)
                }
                else{
                    dialogSuccess(context,text)
                }
                dismiss()
            }
            textResend.setOnClickListener{
                dismiss()
            }
            imageCross.setOnClickListener{
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }}

    fun dialogNewPassword(context: Context?,text: String){
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_new_password)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            var imageCross =  findViewById<ImageView>(R.id.imageCross)
            var etPassword =  findViewById<EditText>(R.id.etPassword)
            var etConfirmPassword =  findViewById<EditText>(R.id.etConfirmPassword)

            var textSubmitButton =  findViewById<TextView>(R.id.textSubmitButton)
            textSubmitButton.setOnClickListener{
                dialogSuccess(context,text)
                dismiss()
            }

            imageCross.setOnClickListener{
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }}


    fun dialogSuccess(context: Context?, text: String) {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_success)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }

            findViewById<ImageView>(R.id.imageCross).setOnClickListener {
                dismiss()
            }

            findViewById<TextView>(R.id.text).text = text
            findViewById<TextView>(R.id.textOkayButton).setOnClickListener {
                if (text == "register Successful") {

                        Log.d("Navigation", "Navigating to turnNotificationsFragment")
                    navController.navigate(R.id.turnNotificationsFragment)

                }
                dismiss()
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

    @SuppressLint("MissingInflatedId")
    fun dialogLogOut(context: Context?, text: String) {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_logout)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            findViewById<ImageView>(R.id.imageCross).setOnClickListener {
                dismiss()
            }


            findViewById<RelativeLayout>(R.id.rlYes).setOnClickListener {
                var sessionManager = SessionManager(context)
                sessionManager.setUserId(-1)
                var intent  = Intent(context,AuthActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                context.startActivity(intent)
                dismiss()
            }

            findViewById<RelativeLayout>(R.id.rlCancel).setOnClickListener {
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

}