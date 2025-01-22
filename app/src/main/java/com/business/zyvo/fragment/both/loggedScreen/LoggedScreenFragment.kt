package com.business.zyvo.fragment.both.loggedScreen

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.business.zyvo.LoadingUtils
import com.business.zyvo.NetworkResult
import com.business.zyvo.OnClickListener
import com.business.zyvo.OnClickListener1
import com.business.zyvo.R
import com.business.zyvo.activity.AuthActivity
import com.business.zyvo.activity.GuesMain
import com.business.zyvo.activity.guest.FiltersActivity
import com.business.zyvo.activity.guest.WhereTimeActivity
import com.business.zyvo.adapter.LoggedScreenAdapter
import com.business.zyvo.databinding.FragmentLoggedScreenBinding
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.CommonAuthWorkUtils
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.viewmodel.ImagePopViewModel
import com.business.zyvo.viewmodel.LoggedScreenViewModel
import com.hbb20.CountryCodePicker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoggedScreenFragment : Fragment(), OnClickListener, View.OnClickListener, OnClickListener1 {


    lateinit  var navController :NavController
    private lateinit var otpDigits: Array<EditText>
    private var countDownTimer: CountDownTimer? = null
    var resendEnabled = false
    var otpValue: String = ""
    private lateinit var binding: FragmentLoggedScreenBinding

    private lateinit var adapter: LoggedScreenAdapter

    private var commonAuthWorkUtils: CommonAuthWorkUtils? = null

    private val loggedScreenViewModel: LoggedScreenViewModel by lazy {
        ViewModelProvider(this)[LoggedScreenViewModel::class.java]
    }

    private val imagePopViewModel: ImagePopViewModel by lazy {
        ViewModelProvider(this)[ImagePopViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        val navController = findNavController()
        commonAuthWorkUtils = CommonAuthWorkUtils(requireActivity(), navController)
        binding = FragmentLoggedScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textLogin.setOnClickListener(this)
        binding.rlFind.setOnClickListener(this)
        binding.filterIcon.setOnClickListener(this)
        binding.textWishlists.setOnClickListener(this)
        binding.textDiscover.setOnClickListener(this)
        // Set up adapter with lifecycleOwner passed
        adapter = LoggedScreenAdapter(
            requireContext(),
            mutableListOf(),
            this,
            viewLifecycleOwner,
            imagePopViewModel, this
        )
        binding.recyclerViewBooking.adapter = adapter

        // Observe ViewModel data and update adapter
        loggedScreenViewModel.imageList.observe(viewLifecycleOwner, Observer { images ->
            adapter.updateData(images)
        })
         adapterWork()
         backPressTask()
         liveDataObserver()
    }

    private fun liveDataObserver(){


    }

    private fun adapterWork(){
        adapter.setOnItemClickListener(object : LoggedScreenAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                Log.d("TESTING_ZYVOO", "I AM HERE IN DEVELOPMENT")
                dialogLogin(requireContext())
            }
        })
    }

    private fun backPressTask(){
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }


    private fun callingRegisterPhone(code: String, number: String, dialog: Dialog){
        LoadingUtils.showDialog(requireContext(),false)
        Log.d("TESTING_ZYVOO","Inside of fragment")
        loggedScreenViewModel.signupPhoneNumber(number,code)
        loggedScreenViewModel.phoneSignUpLiveData.observe(viewLifecycleOwner, Observer { data ->
            LoadingUtils.hideDialog()
            when(data){
                is NetworkResult.Success ->{
                    var otp = data.data?.first
                    Log.d("TESTING_ZYVOO",otp.toString())
                    var text = "Your account is registered \nsuccessfully"
                    var textHeaderOfOtpVerfication =
                        "Please type the verification code send \n to "+code+ " "+number
                    dialogOtp(requireContext(), text, textHeaderOfOtpVerfication)
                    dialog.dismiss()
                }
                is NetworkResult.Error ->{
                    ErrorDialog.showErrorDialog(requireContext(),data.message.toString())
                }
                else ->{

                }
            }
        })
    }

    override fun itemClick(obj: Int) {
        findNavController().navigate(R.id.viewImageFragment)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.textLogin -> {

              dialogLogin(requireContext())

            }

            R.id.filter_icon -> {
                startActivity(Intent(requireActivity(), FiltersActivity::class.java))
            }

            R.id.rlFind -> {
                startActivity(Intent(requireActivity(), WhereTimeActivity::class.java))
            }

            R.id.textDiscover -> {
                dialogLogin(requireContext())
                }


            R.id.textWishlists -> {
  dialogLogin(requireContext())
            }
        }
    }

    override fun itemClick(obj: Int, text: String) {
        when (text) {
            "Add Wish" -> {
                dialogLogin(requireContext())
            }
        }

    }



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

                var textHeaderOfOtpVerfication = "Please type the verification code send \n to +1 999 999 9999"

                dialogOtp(context,text, textHeaderOfOtpVerfication)
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
                var text = "Your account is registered \nsuccessfully"
                var textHeaderOfOtpVerfication = "Please type the verification code send \n to +1 999 999 9999"
                dialogOtp(context,text,textHeaderOfOtpVerfication)
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
            var etConfirmPassword =  findViewById<EditText>(R.id.etConfirmPassword)
            var imgHidePass =  findViewById<ImageView>(R.id.imgHidePass)
            var imgShowPass =  findViewById<ImageView>(R.id.imgShowPass)
            var textDontHaveAnAccount =  findViewById<TextView>(R.id.textDontHaveAnAccount)
            var textRegister =  findViewById<TextView>(R.id.textRegister)

            eyeHideShow(imgHidePass,imgShowPass,etConfirmPassword)

            textRegister.setOnClickListener{
                dialogRegisterEmail(context)
                dismiss()
            }

            textForget.setOnClickListener{
                dialogForgotPassword(context)
                dismiss()
            }
            textLoginButton.setOnClickListener{
                var intent = Intent(context,GuesMain::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                context.startActivity(intent)
                dismiss()
            }
            imageCross.setOnClickListener{
                dismiss()
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }}


    fun eyeHideShow(imgHidePass:ImageView,imgShowPass:ImageView,etPassword:EditText){
        imgHidePass.setOnClickListener{
            imgShowPass.visibility = View.VISIBLE
            imgHidePass.visibility = View.GONE
            etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            etPassword.setSelection(etPassword.text.length)
        }
        imgShowPass.setOnClickListener {
            imgShowPass.visibility = View.GONE
            imgHidePass.visibility = View.VISIBLE
            etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            etPassword.setSelection(etPassword.text.length)
        }
    }

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
                var text = "Your account is registered \nsuccessfully"

                var textHeaderOfOtpVerfication = "Please type the verification code send \nto abc@gmail.com"
                dialogOtp(context,text,textHeaderOfOtpVerfication)

                dismiss()
            }

            imageCross.setOnClickListener{
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }}



    @SuppressLint("MissingInflatedId")
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
            var etEmail =  findViewById<EditText>(R.id.etEmail)
            var textSubmitButton =  findViewById<TextView>(R.id.textSubmitButton)
            textSubmitButton.setOnClickListener{
                var text = "Your password has been changed\n successfully."

                var textHeaderOfOtpVerfication = "Please type the verification code send \nto abc@gmail.com"
                dialogOtp(context,text,textHeaderOfOtpVerfication)


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

                var text = "Your Phone has been Verified\n  successfully."

                var textHeaderOfOtpVerfication = "Please type the verification code send \nto +1 999 999 9999"
                dialogOtp(context,text,textHeaderOfOtpVerfication)


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


    fun dialogEmailVerification(context: Context?,text: String){
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
                var text2 = "Your Email has been Verified\n  successfully."

                val texter = if (text != null.toString()) text else text2

                var textHeaderOfOtpVerfication = "Please type the verification code send \nto abc@gmail.com"
                dialogOtp(context,texter,textHeaderOfOtpVerfication)

                dismiss()
            }
            imageCross.setOnClickListener{
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }}


    @SuppressLint("SuspiciousIndentation")
    fun dialogOtp(context: Context, text: String, textHeaderOfOtpVerfication: String){
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
            val textEnterYourEmail =  findViewById<TextView>(R.id.textEnterYourEmail)

            val textSubmitButton =  findViewById<TextView>(R.id.textSubmitButton)
            val rlResendLine =  findViewById<RelativeLayout>(R.id.rlResendLine)

            val textTimeResend =  findViewById<TextView>(R.id.textTimeResend)
            val incorrectOtp =  findViewById<TextView>(R.id.incorrectOtp)



            textEnterYourEmail.text = textHeaderOfOtpVerfication


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


            startCountDownTimer(context,textTimeResend,rlResendLine,textResend)
            countDownTimer!!.cancel()




            textTimeResend.text = "${"00"}:${"00"} sec"
            if (textTimeResend.text == "${"00"}:${"00"} sec") {
                resendEnabled = true
                textResend.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.scroll_bar_color
                    )
                )
            } else {
                textResend.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.grey
                    )
                )
            }







            textSubmitButton.setOnClickListener{

                /*
                  if (otpValue.equals("1234")) {

                alertBox()

            } else {
                binding.rlResendLine.visibility = View.GONE
                binding.incorrectOtp.visibility = View.VISIBLE
                binding.textResend.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.orange
                    )
                )
            }
                 */


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
                    if (text == "Your password has been changed successfully" ){
                        dialogNewPassword(context,text)
                    }else{
                        dialogSuccess(context,text)
                    }


                }
                dismiss()
            }


            textResend.setOnClickListener{
                if (resendEnabled) {
                    rlResendLine.visibility = View.VISIBLE
                    incorrectOtp.visibility = View.GONE
                    countDownTimer?.cancel()
                    startCountDownTimer(context,textTimeResend,rlResendLine,textResend)
                    textResend.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.grey
                        )
                    )
                }
            }
            imageCross.setOnClickListener{
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }}






    private fun startCountDownTimer(context: Context,textTimeResend : TextView,rlResendLine: RelativeLayout, textResend : TextView) {
        countDownTimer = object : CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val f = android.icu.text.DecimalFormat("00")
                val min = (millisUntilFinished / 60000) % 60
                val sec = (millisUntilFinished / 1000) % 60
                textTimeResend.text = "${f.format(min)}:${f.format(sec)} sec"
            }

            override fun onFinish() {
                textTimeResend.text = "00:00"
                rlResendLine.visibility = View.GONE
                if (textTimeResend.text == "00:00") {
                    resendEnabled = true
                    textResend.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.scroll_bar_color
                        )
                    )
                } else {
                    textResend.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.grey
                        )
                    )
                }
            }
        }
        countDownTimer?.start()
    }



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
                if (text == "Your account is registered \nsuccessfully") {

                    Log.d("Navigation", "Navigating to turnNotificationsFragment")
                    navController?.navigate(R.id.turnNotificationsFragment)

                }
                else if (text == "Your password has been changed\n" + " successfully."){
                    dialogLoginEmail(context)
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


    private fun showPopupWindow(anchorView: View, position: Int)   {
        // Inflate the custom layout for the popup menu
        val popupView = LayoutInflater.from(context).inflate(R.layout.popup_filter_all_conversations, null)

        // Create PopupWindow with the custom layout
        val popupWindow =   PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set click listeners for each menu item in the popup layout
        popupView.findViewById<TextView>(R.id.itemAllConversations).setOnClickListener {

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemArchived).setOnClickListener {

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemUnread).setOnClickListener {

            popupWindow.dismiss()
        }


        // Get the location of the anchor view (three-dot icon)
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        // Get the height of the PopupView after inflating it
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = popupView.measuredHeight
        val popupWeight = popupView.measuredWidth
        val screenWidht = context?.resources?.displayMetrics?.widthPixels
        val anchorX = location[1]
        val spaceEnd = screenWidht?.minus((anchorX + anchorView.width))

        val xOffset = if (popupWeight > spaceEnd!!) {
            // If there is not enough space below, show it above
            -(popupWeight + 20) // Adjust this value to add a gap between the popup and the anchor view
        } else {
            // Otherwise, show it below
            // 20 // This adds a small gap between the popup and the anchor view
            -(popupWeight + 20)
        }
        // Calculate the Y offset to make the popup appear above the three-dot icon
        val screenHeight = context?.resources?.displayMetrics?.heightPixels
        val anchorY = location[1]

        // Calculate the available space above the anchorView
        val spaceAbove = anchorY
        val spaceBelow = screenHeight?.minus((anchorY + anchorView.height))

        // Determine the Y offset
        val yOffset = if (popupHeight > spaceBelow!!) {
            // If there is not enough space below, show it above
            -(popupHeight + 20) // Adjust this value to add a gap between the popup and the anchor view
        } else {
            // Otherwise, show it below
            20 // This adds a small gap between the popup and the anchor view
        }

        // Show the popup window anchored to the view (three-dot icon)
        popupWindow.elevation = 8.0f  // Optional: Add elevation for shadow effect
        popupWindow.showAsDropDown(anchorView, xOffset, yOffset, Gravity.END)  // Adjust the Y offset dynamically
    }

    fun isScreenLarge(context: Context): Boolean {
        // Get the screen width
        val display =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val width = display.width

        // Convert pixels to dp
        val density = context.resources.displayMetrics.density
        val widthInDp = (width / density).toInt()

        // Check if the screen width is greater than 600dp (typical for tablets)
        return widthInDp > 600
    }




}

