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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.business.zyvo.AppConstant
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
import com.business.zyvo.utils.ErrorDialog.TAG
import com.business.zyvo.utils.ErrorDialog.customDialog
import com.business.zyvo.viewmodel.ImagePopViewModel
import com.business.zyvo.viewmodel.LoggedScreenViewModel
import com.google.gson.Gson
import com.hbb20.CountryCodePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoggedScreenFragment : Fragment(), OnClickListener, View.OnClickListener, OnClickListener1 {
    lateinit var navController: NavController
    private lateinit var otpDigits: Array<EditText>
    private var countDownTimer: CountDownTimer? = null
    var resendEnabled = false
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
    ): View {

        navController = findNavController()
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
        loggedScreenViewModel.imageList.observe(viewLifecycleOwner) { images ->
            adapter.updateData(images)
        }
        // Observe the isLoading state
        lifecycleScope.launch {
            loggedScreenViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    LoadingUtils.showDialog(requireContext(), false)
                } else {
                    LoadingUtils.hideDialog()
                }
            }
        }
        adapterWork()
        backPressTask()
        liveDataObserver()
    }

    private fun liveDataObserver() {


    }

    private fun adapterWork() {
        adapter.setOnItemClickListener(object : LoggedScreenAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                Log.d("TESTING_ZYVOO", "I AM HERE IN DEVELOPMENT")
                dialogLogin(requireContext())
            }
        })
    }

    private fun backPressTask() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
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


    fun dialogLogin(context: Context?) {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_login)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            val imageCross = findViewById<ImageView>(R.id.imageCross)
            val imageEmailSocial = findViewById<ImageView>(R.id.imageEmailSocial)
            val etMobileNumber = findViewById<EditText>(R.id.etMobileNumber)
            val textContinueButton = findViewById<TextView>(R.id.textContinueButton)
            val checkBox = findViewById<CheckBox>(R.id.checkBox)
            val textKeepLogged = findViewById<TextView>(R.id.textKeepLogged)
            val textForget = findViewById<TextView>(R.id.textForget)
            val textDontHaveAnAccount = findViewById<TextView>(R.id.textDontHaveAnAccount)
            val textRegister = findViewById<TextView>(R.id.textRegister)
            val countyCodePicker = findViewById<CountryCodePicker>(R.id.countyCodePicker)
            textRegister.setOnClickListener {
                dialogRegister(context)
                dismiss()
            }

            textForget.setOnClickListener {
                dialogForgotPassword(context)
                dismiss()
            }
            imageEmailSocial.setOnClickListener {
                dialogLoginEmail(context)
                dismiss()
            }
            textContinueButton.setOnClickListener {
                toggleLoginButtonEnabled(false, textContinueButton)
                lifecycleScope.launch {
                    loggedScreenViewModel.networkMonitor.isConnected
                        .distinctUntilChanged() // Ignore duplicate consecutive values
                        .collect { isConn ->
                            if (!isConn) {
                                customDialog(
                                    resources.getString(R.string.no_internet_dialog_msg),
                                    requireContext()
                                )
                                toggleLoginButtonEnabled(true, textContinueButton)
                            } else {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (etMobileNumber.text!!.isEmpty()) {
                                        etMobileNumber.error = "Mobile required"
                                        customDialog(AppConstant.mobile, requireContext())
                                        toggleLoginButtonEnabled(true, textContinueButton)
                                    } else {
                                        val phoneNumber = etMobileNumber.text.toString()
                                        Log.d(TAG, phoneNumber)
                                        val countryCode =
                                            countyCodePicker.selectedCountryCodeWithPlus
                                        Log.d(TAG, countryCode)
                                        submitLogin(
                                            countryCode,
                                            phoneNumber,
                                            dialog,
                                            textContinueButton,
                                            checkBox
                                        )
                                    }
                                }
                            }
                        }
                }
            }
            imageCross.setOnClickListener {
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

    private fun submitLogin(code: String, number: String, dialog: Dialog, text: TextView,
                            checkBox:CheckBox) {
        lifecycleScope.launch {
            loggedScreenViewModel.loginPhoneNumber(
                code,
                number
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            val text = "Login Successful"
                            val textHeaderOfOtpVerfication =
                                "Please type the verification code send \n to $code$number"
                            dialog.dismiss()
                            val userId = resp.second
                            dialogOtp(requireActivity(), text, textHeaderOfOtpVerfication,
                                code, number,userId,checkBox,"loginPhone")
                        }
                        toggleLoginButtonEnabled(true, text)
                    }

                    is NetworkResult.Error -> {
                        customDialog(
                            it.message,
                            requireContext()
                        )
                        toggleLoginButtonEnabled(true, text)
                    }

                    else -> {
                        toggleLoginButtonEnabled(true, text)
                        Log.v("RegisterViewModel", "error::" + it.message)
                    }
                }
            }
        }

    }



    private fun dialogRegister(context: Context?) {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_registration)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            val imageCross = findViewById<ImageView>(R.id.imageCross)
            val imageEmailSocial = findViewById<ImageView>(R.id.imageEmailSocial)
            val etMobileNumber = findViewById<EditText>(R.id.etMobileNumber)
            val textContinueButton = findViewById<TextView>(R.id.textContinueButton)
            val checkBox = findViewById<CheckBox>(R.id.checkBox)
            val textKeepLogged = findViewById<TextView>(R.id.textKeepLogged)

            val textLoginButton = findViewById<TextView>(R.id.textLoginButton)

            val countyCodePicker = findViewById<CountryCodePicker>(R.id.countyCodePicker)

            textLoginButton.setOnClickListener {
                dialogLogin(context)
                dismiss()
            }
            textContinueButton.setOnClickListener {
                toggleLoginButtonEnabled(false, textContinueButton)
                lifecycleScope.launch {
                    loggedScreenViewModel.networkMonitor.isConnected
                        .distinctUntilChanged() // Ignore duplicate consecutive values
                        .collect { isConn ->
                            if (!isConn) {
                                customDialog(
                                    resources.getString(R.string.no_internet_dialog_msg),
                                    requireContext()
                                )
                                toggleLoginButtonEnabled(true, textContinueButton)
                            } else {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (etMobileNumber.text!!.isEmpty()) {
                                        etMobileNumber.error = "Mobile required"
                                        customDialog(AppConstant.mobile, requireContext())
                                        toggleLoginButtonEnabled(true, textContinueButton)
                                    } else {
                                        val phoneNumber = etMobileNumber.text.toString()
                                        Log.d(TAG, phoneNumber)
                                        val countryCode =
                                            countyCodePicker.selectedCountryCodeWithPlus
                                        Log.d(TAG, countryCode)
                                        callingRegisterPhone(countryCode,phoneNumber,
                                            dialog,textContinueButton,checkBox)
                                    }
                                }
                            }
                        }
                }
            }

            imageEmailSocial.setOnClickListener {
                dialogRegisterEmail(context)
                dismiss()
            }

            imageCross.setOnClickListener {
                dismiss()
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            show()
        }
    }
    private fun callingRegisterPhone(code: String, number: String, dialog: Dialog,
                                     text: TextView,checkBox: CheckBox) {
        LoadingUtils.showDialog(requireContext(), false)
        Log.d(TAG, "Inside of fragment")
     /*   loggedScreenViewModel.signupPhoneNumber(number, code)
        loggedScreenViewModel.phoneSignUpLiveData.observe(viewLifecycleOwner) { data ->
            LoadingUtils.hideDialog()
            when (data) {
                is NetworkResult.Success -> {
                    val otp = data.data?.first
                    val temp = data.data?.second
                    Log.d(TAG, otp.toString())
                    val text = "Your account is registered \nsuccessfully"
                    val textHeaderOfOtpVerfication =
                        "Please type the verification code send \n to " + code + " " + number
                    dialogOtp(requireContext(), text, textHeaderOfOtpVerfication, code,
                        number,
                        temp!!,checkBox)
                    dialog.dismiss()
                }

                is NetworkResult.Error -> {
                    ErrorDialog.showErrorDialog(requireContext(), data.message.toString())
                }

                else -> {

                }
            }
        }*/

        lifecycleScope.launch {
            loggedScreenViewModel.signupPhoneNumber(
                code, number
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            val otp = resp.first
                            val temp = resp.second
                            Log.d(TAG, otp.toString())
                            val text = "Your account is registered \nsuccessfully"
                            val textHeaderOfOtpVerfication =
                                "Please type the verification code send \n to " + code + " " + number
                            dialogOtp(requireContext(), text, textHeaderOfOtpVerfication, code,
                                number,
                                temp!!,checkBox,"RegisterPhone")
                        }
                        dialog.dismiss()
                        toggleLoginButtonEnabled(true, text)
                    }

                    is NetworkResult.Error -> {
                        customDialog(it.message, requireContext())
                        toggleLoginButtonEnabled(true, text)
                    }

                    else -> {
                        toggleLoginButtonEnabled(true, text)
                        Log.v("RegisterViewModel", "error::" + it.message)
                    }
                }
            }
        }
    }
    fun dialogLoginEmail(context: Context?) {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_login_email)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            val imageCross = findViewById<ImageView>(R.id.imageCross)

            val textLoginButton = findViewById<TextView>(R.id.textLoginButton)
            val checkBox = findViewById<CheckBox>(R.id.checkBox)
            val textKeepLogged = findViewById<TextView>(R.id.textKeepLogged)
            val textForget = findViewById<TextView>(R.id.textForget)
            val imgHidePass = findViewById<ImageView>(R.id.imgHidePass)
            val imgShowPass = findViewById<ImageView>(R.id.imgShowPass)
            val textDontHaveAnAccount = findViewById<TextView>(R.id.textDontHaveAnAccount)
            val textRegister = findViewById<TextView>(R.id.textRegister)

            val etLoginEmail = findViewById<EditText>(R.id.etLoginEmail)
            val etLoginPassword = findViewById<EditText>(R.id.etLoginPassword)

            eyeHideShow(imgHidePass, imgShowPass, etLoginPassword)

            textRegister.setOnClickListener {
                dialogRegisterEmail(context)
                dismiss()
            }

            textForget.setOnClickListener {
                dialogForgotPassword(context)
                dismiss()
            }
            textLoginButton.setOnClickListener {
                toggleLoginButtonEnabled(false, textLoginButton)
                lifecycleScope.launch {
                    loggedScreenViewModel.networkMonitor.isConnected
                        .distinctUntilChanged() // Ignore duplicate consecutive values
                        .collect { isConn ->
                            if (!isConn) {
                                customDialog(
                                    resources.getString(R.string.no_internet_dialog_msg),
                                    requireContext()
                                )
                                toggleLoginButtonEnabled(true, textLoginButton)
                            } else {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (etLoginEmail.text!!.isEmpty()) {
                                        etLoginEmail.error = "Email Address required"
                                        customDialog(AppConstant.email, requireContext())
                                        toggleLoginButtonEnabled(true, textLoginButton)
                                    }else if (etLoginPassword.text!!.isEmpty()) {
                                        etLoginPassword.error = "Password required"
                                        customDialog(AppConstant.password, requireContext())
                                        toggleLoginButtonEnabled(true, textLoginButton)
                                    }
                                    else {
                                        loginEmail(etLoginEmail.text.toString(),
                                            etLoginPassword.text.toString(),
                                            dialog,textLoginButton,
                                            checkBox)
                                    }
                                }
                            }
                        }
                }
            }
            imageCross.setOnClickListener {
                dismiss()
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

    private fun loginEmail(
        email: String,
        password: String,
        dialog: Dialog,
        textLoginButton: TextView,
        checkBox: CheckBox
    ) {
        lifecycleScope.launch {
            loggedScreenViewModel.loginEmail(
                email,
                password
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            val session: SessionManager = SessionManager(requireActivity())
                            if (resp.has("is_profile_complete") &&
                                resp.get("is_profile_complete").asBoolean) {
                                if (resp.has("user_id")) {
                                    if (checkBox!=null && checkBox.isChecked){
                                        session.setUserSession(true)
                                    }
                                    session.setUserId(resp.get("user_id").asInt)
                                    session.setAuthToken(resp.get("token").asString)
                                    val intent = Intent(requireActivity(), GuesMain::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    startActivity(intent)
                                }
                            }else{
                                if (checkBox!=null && checkBox.isChecked){
                                    session.setUserSession(true)
                                }
                                val bundle = Bundle()
                                bundle.putString("data",Gson().toJson(resp))
                                findNavController().navigate(R.id.completeProfileFragment,bundle)
                            }
                        }
                        dialog.dismiss()
                        toggleLoginButtonEnabled(true, textLoginButton)
                    }

                    is NetworkResult.Error -> {
                        customDialog(it.message, requireContext())
                        toggleLoginButtonEnabled(true, textLoginButton)
                    }

                    else -> {
                        toggleLoginButtonEnabled(true, textLoginButton)
                        Log.v("RegisterViewModel", "error::" + it.message)
                    }
                }
            }
        }
    }


    fun eyeHideShow(imgHidePass: ImageView, imgShowPass: ImageView, etPassword: EditText) {
        imgHidePass.setOnClickListener {
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

    private fun dialogRegisterEmail(context: Context?) {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_register_email)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            val imageCross = findViewById<ImageView>(R.id.imageCross)

            val textCreateAccountButton = findViewById<TextView>(R.id.textCreateAccountButton)
            val checkBox = findViewById<CheckBox>(R.id.checkBox)
            val textKeepLogged = findViewById<TextView>(R.id.textKeepLogged)
            val textLoginHere = findViewById<TextView>(R.id.textLoginHere)

            val etRegisterEmail = findViewById<EditText>(R.id.etRegisterEmail)
            val etRegisterPassword = findViewById<EditText>(R.id.etRegisterPassword)

            textLoginHere.setOnClickListener {
                dialogLoginEmail(context)
                dismiss()
            }
            textCreateAccountButton.setOnClickListener {
                toggleLoginButtonEnabled(false, textCreateAccountButton)
                lifecycleScope.launch {
                    loggedScreenViewModel.networkMonitor.isConnected
                        .distinctUntilChanged() // Ignore duplicate consecutive values
                        .collect { isConn ->
                            if (!isConn) {
                                customDialog(
                                    resources.getString(R.string.no_internet_dialog_msg),
                                    requireContext()
                                )
                                toggleLoginButtonEnabled(true, textCreateAccountButton)
                            } else {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (etRegisterEmail.text!!.isEmpty()) {
                                        etRegisterEmail.error = "Email Address required"
                                        customDialog(AppConstant.email, requireContext())
                                        toggleLoginButtonEnabled(true, textCreateAccountButton)
                                    }else if (etRegisterPassword.text!!.isEmpty()) {
                                        etRegisterPassword.error = "Password required"
                                        customDialog(AppConstant.password, requireContext())
                                        toggleLoginButtonEnabled(true, textCreateAccountButton)
                                    }
                                    else {
                                        signupEmail(etRegisterEmail.text.toString(),
                                            etRegisterPassword.text.toString(),
                                            dialog,textCreateAccountButton,
                                            checkBox)
                                    }
                                }
                            }
                        }
                }
            }

            imageCross.setOnClickListener {
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }
    private fun signupEmail(
        email: String,
        password: String,
        dialog: Dialog,
        textLoginButton: TextView,
        checkBox: CheckBox
    ) {
        lifecycleScope.launch {
            loggedScreenViewModel.signupEmail(
                email,
                password
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            val otp = resp.first
                            val temp = resp.second
                            Log.d(TAG, otp.toString())
                            val text = "Your account is registered \nsuccessfully"
                            val textHeaderOfOtpVerfication =
                   "Please type the verification code send \nto "+email
                        dialogOtp(requireActivity(), text, textHeaderOfOtpVerfication, email, password,
                        temp,checkBox,
                            "RegisterEmail")
                        }
                        dialog.dismiss()
                        toggleLoginButtonEnabled(true, textLoginButton)
                    }

                    is NetworkResult.Error -> {
                        customDialog(it.message, requireContext())
                        toggleLoginButtonEnabled(true, textLoginButton)
                    }

                    else -> {
                        toggleLoginButtonEnabled(true, textLoginButton)
                        Log.v("RegisterViewModel", "error::" + it.message)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    fun dialogForgotPassword(context: Context?) {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_forgot_password)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            val imageCross = findViewById<ImageView>(R.id.imageCross)
            val etEmail = findViewById<EditText>(R.id.etEmail)
            val textSubmitButton = findViewById<TextView>(R.id.textSubmitButton)
            textSubmitButton.setOnClickListener {
                toggleLoginButtonEnabled(false, textSubmitButton)
                lifecycleScope.launch {
                    loggedScreenViewModel.networkMonitor.isConnected
                        .distinctUntilChanged() // Ignore duplicate consecutive values
                        .collect { isConn ->
                            if (!isConn) {
                                customDialog(
                                    resources.getString(R.string.no_internet_dialog_msg),
                                    requireContext()
                                )
                                toggleLoginButtonEnabled(true, textSubmitButton)
                            } else {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (etEmail.text!!.isEmpty()) {
                                        etEmail.error = "Email Address required"
                                        customDialog(AppConstant.email, requireContext())
                                        toggleLoginButtonEnabled(true, textSubmitButton)
                                    } else {
                                        forgotPassword(etEmail.text.toString(),
                                            dialog,textSubmitButton)
                                    }
                                }
                            }
                        }
                }

            }
            imageCross.setOnClickListener {
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

    fun dialogNumberVerification(context: Context?) {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_number_verification)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            val imageCross = findViewById<ImageView>(R.id.imageCross)
            val textSubmitButton = findViewById<TextView>(R.id.textSubmitButton)
            textSubmitButton.setOnClickListener {

                val text = "Your Phone has been Verified\n  successfully."

                val textHeaderOfOtpVerfication =
                    "Please type the verification code send \nto +1 999 999 9999"
                dialogOtp(context, text, textHeaderOfOtpVerfication, "", "",
                    "",null,"")


                dismiss()
            }
            imageCross.setOnClickListener {
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }


    fun dialogChangeName(context: Context?) {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_change_names)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }


            val textSaveChangesButton = findViewById<TextView>(R.id.textSaveChangesButton)
            textSaveChangesButton.setOnClickListener {

                dismiss()
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }


    fun dialogEmailVerification(context: Context?, text: String) {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_email_verification)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            val imageCross = findViewById<ImageView>(R.id.imageCross)

            val textSubmitButton = findViewById<TextView>(R.id.textSubmitButton)
            textSubmitButton.setOnClickListener {
                val text2 = "Your Email has been Verified\n  successfully."

                val texter = if (text != null.toString()) text else text2

                val textHeaderOfOtpVerfication =
                    "Please type the verification code send \nto abc@gmail.com"
                dialogOtp(context, texter, textHeaderOfOtpVerfication, "", "",
                    "",null,"")

                dismiss()
            }
            imageCross.setOnClickListener {
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }


    @SuppressLint("SuspiciousIndentation", "CutPasteId")
    fun dialogOtp(
        context: Context, text: String, textHeaderOfOtpVerfication: String,
        code: String, number: String, userId:String,
        checkBox: CheckBox?,
        otpType:String
    ) {
        val dialog = Dialog(context, R.style.BottomSheetDialog)
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_otp_verification)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            val imageCross = findViewById<ImageView>(R.id.imageCross)
            val textResend = findViewById<TextView>(R.id.textResend)
            val textEnterYourEmail = findViewById<TextView>(R.id.textEnterYourEmail)
            val textSubmitButton = findViewById<TextView>(R.id.textSubmitButton)
            val rlResendLine = findViewById<RelativeLayout>(R.id.rlResendLine)
            val textTimeResend = findViewById<TextView>(R.id.textTimeResend)
            val incorrectOtp = findViewById<TextView>(R.id.incorrectOtp)
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
                        s: CharSequence, start: Int, count: Int, after: Int) {
                    }

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int,
                        count: Int
                    ) {
                        if (s.length == 1 && index < otpDigits.size - 1) {
                            otpDigits.get(index + 1).requestFocus()
                        } else if (s.length == 0 && index > 0) {
                            otpDigits.get(index - 1).requestFocus()
                        }
                    }
                    override fun afterTextChanged(s: Editable) {}
                })
            }
            startCountDownTimer(context, textTimeResend, rlResendLine, textResend)
            countDownTimer!!.cancel()
            textTimeResend.text = "${"00"}:${"00"} sec"
            if (textTimeResend.text == "${"00"}:${"00"} sec") {
                resendEnabled = true
                textResend.setTextColor(
                    ContextCompat.getColor(context, R.color.scroll_bar_color))
            } else {
                textResend.setTextColor(ContextCompat.getColor(context, R.color.grey))
            }
            textSubmitButton.setOnClickListener {
                toggleLoginButtonEnabled(false, textSubmitButton)
                if (text == "Your password has been changed\n successfully.") {
                    lifecycleScope.launch {
                        loggedScreenViewModel.networkMonitor.isConnected
                            .distinctUntilChanged() // Ignore duplicate consecutive values
                            .collect { isConn ->
                                if (!isConn) {
                                    customDialog(
                                        resources.getString(R.string.no_internet_dialog_msg),
                                        requireContext())
                                    toggleLoginButtonEnabled(true, textSubmitButton)
                                } else {
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        if (findViewById<EditText>(R.id.otp_digit1).text.toString().isEmpty()&&
                                            findViewById<EditText>(R.id.otp_digit2).text.toString().isEmpty()&&
                                            findViewById<EditText>(R.id.otp_digit3).text.toString().isEmpty()&&
                                            findViewById<EditText>(R.id.otp_digit4).text.toString().isEmpty()) {
                                            customDialog(AppConstant.otp, requireContext())
                                            toggleLoginButtonEnabled(true, textSubmitButton)
                                        } else {
                                            val otp = findViewById<EditText>(R.id.otp_digit1).text.toString()+
                                                    findViewById<EditText>(R.id.otp_digit2).text.toString()+
                                                    findViewById<EditText>(R.id.otp_digit3).text.toString()+
                                                    findViewById<EditText>(R.id.otp_digit4).text.toString()
                                            otpVerifyForgotPassword(userId,
                                                otp,
                                                dialog,
                                                textSubmitButton,
                                                text)
                                        }
                                    }
                                }
                            }
                    }
                } else if (text.equals("Login Successful")) {
                    lifecycleScope.launch {
                        loggedScreenViewModel.networkMonitor.isConnected
                            .distinctUntilChanged() // Ignore duplicate consecutive values
                            .collect { isConn ->
                                if (!isConn) {
                                    customDialog(
                                        resources.getString(R.string.no_internet_dialog_msg),
                                        requireContext())
                                    toggleLoginButtonEnabled(true, textSubmitButton)
                                } else {
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        if (findViewById<EditText>(R.id.otp_digit1).text.toString().isEmpty()&&
                                            findViewById<EditText>(R.id.otp_digit2).text.toString().isEmpty()&&
                                            findViewById<EditText>(R.id.otp_digit3).text.toString().isEmpty()&&
                                            findViewById<EditText>(R.id.otp_digit4).text.toString().isEmpty()) {
                                            customDialog(AppConstant.otp, requireContext())
                                            toggleLoginButtonEnabled(true, textSubmitButton)
                                        } else {
                                            val otp = findViewById<EditText>(R.id.otp_digit1).text.toString()+
                                             findViewById<EditText>(R.id.otp_digit2).text.toString()+
                                            findViewById<EditText>(R.id.otp_digit3).text.toString()+
                                            findViewById<EditText>(R.id.otp_digit4).text.toString()
                                            otpVerifyLoginPhone(
                                                userId,
                                                otp,
                                                dialog,
                                                textSubmitButton,
                                                checkBox)
                                        }
                                    }
                                }
                            }
                    }
                } else if (text.equals("Your account is registered \nsuccessfully")){
                        lifecycleScope.launch {
                            loggedScreenViewModel.networkMonitor.isConnected
                                .distinctUntilChanged() // Ignore duplicate consecutive values
                                .collect { isConn ->
                                    if (!isConn) {
                                        customDialog(
                                            resources.getString(R.string.no_internet_dialog_msg),
                                            requireContext()
                                        )
                                        toggleLoginButtonEnabled(true, textSubmitButton)
                                    } else {
                                        lifecycleScope.launch(Dispatchers.Main) {
                                            if (findViewById<EditText>(R.id.otp_digit1).text.toString().isEmpty()&&
                                                findViewById<EditText>(R.id.otp_digit2).text.toString().isEmpty()&&
                                                findViewById<EditText>(R.id.otp_digit3).text.toString().isEmpty()&&
                                                findViewById<EditText>(R.id.otp_digit4).text.toString().isEmpty()) {
                                                customDialog(AppConstant.otp, requireContext())
                                                toggleLoginButtonEnabled(true, textSubmitButton)
                                            } else {
                                                val otp = findViewById<EditText>(R.id.otp_digit1).text.toString()+
                                                        findViewById<EditText>(R.id.otp_digit2).text.toString()+
                                                        findViewById<EditText>(R.id.otp_digit3).text.toString()+
                                                        findViewById<EditText>(R.id.otp_digit4).text.toString()
                                                if (otpType.equals("RegisterPhone")) {
                                                    otpVerifySignupPhone(
                                                        userId, otp, dialog, textSubmitButton,
                                                        checkBox,
                                                        text
                                                    )
                                                }
                                                if (otpType.equals("RegisterEmail")) {
                                                    otpVerifySignupEmail( userId, otp, dialog, textSubmitButton,
                                                        checkBox,
                                                        text

                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                        }

                }
            }
            textResend.setOnClickListener {
                findViewById<EditText>(R.id.otp_digit1).text.clear()
                findViewById<EditText>(R.id.otp_digit2).text.clear()
                findViewById<EditText>(R.id.otp_digit3).text.clear()
                findViewById<EditText>(R.id.otp_digit4).text.clear()
                if (text.equals("Login Successful")) {
                    lifecycleScope.launch {
                        loggedScreenViewModel.networkMonitor.isConnected
                            .distinctUntilChanged() // Ignore duplicate consecutive values
                            .collect { isConn ->
                                if (!isConn) {
                                    customDialog(
                                        resources.getString(R.string.no_internet_dialog_msg),
                                        requireContext()
                                    )
                                } else {
                                    if (resendEnabled) {
                                        resendLoginMobile(
                                            code, number, rlResendLine,
                                            incorrectOtp, textTimeResend, textResend
                                        )
                                    }
                                }
                            }
                    }
                }else if (text.equals("Your account is registered \nsuccessfully")){
                    lifecycleScope.launch {
                        loggedScreenViewModel.networkMonitor.isConnected
                            .distinctUntilChanged() // Ignore duplicate consecutive values
                            .collect { isConn ->
                                if (!isConn) {
                                    customDialog(
                                        resources.getString(R.string.no_internet_dialog_msg),
                                        requireContext()
                                    )
                                } else {
                                    if (otpType.equals("RegisterPhone")){
                                        if (resendEnabled) {
                                            resendRegisterMobile( code, number, rlResendLine,
                                                incorrectOtp, textTimeResend, textResend)
                                        }
                                    }
                                    if (otpType.equals("RegisterEmail")) {
                                        if (resendEnabled) {
                                            resendSignupEmail(
                                                code, number,
                                                rlResendLine,
                                                incorrectOtp, textTimeResend, textResend
                                            )
                                        }
                                    }

                                }
                            }
                    }
                }else if (text == "Your password has been changed\n successfully.") {
                    if (resendEnabled) {
                        resendForgotPassword(
                            number,
                            rlResendLine,
                            incorrectOtp, textTimeResend, textResend
                        )
                    }
                }
            }
            imageCross.setOnClickListener {
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }


    private fun resendSignupEmail(
        email: String,
        password: String,
        rlResendLine: RelativeLayout,
        incorrectOtp: TextView,
        textTimeResend: TextView,
        textResend: TextView
    ) {
        lifecycleScope.launch {
            loggedScreenViewModel.signupEmail(
                email,
                password
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            rlResendLine.visibility = View.VISIBLE
                            incorrectOtp.visibility = View.GONE
                            countDownTimer?.cancel()
                            startCountDownTimer(
                                requireContext(),
                                textTimeResend,
                                rlResendLine,
                                textResend
                            )
                            textResend.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.grey
                                )
                            )
                        }

                    }

                    is NetworkResult.Error -> {
                        customDialog(it.message, requireContext())

                    }

                    else -> {

                        Log.v("RegisterViewModel", "error::" + it.message)
                    }
                }
            }
        }
    }

    private fun resendForgotPassword(
        email: String,
        rlResendLine: RelativeLayout,
        incorrectOtp: TextView,
        textTimeResend: TextView,
        textResend: TextView
    ) {
        lifecycleScope.launch {
            loggedScreenViewModel.forgotPassword(
                email
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            rlResendLine.visibility = View.VISIBLE
                            incorrectOtp.visibility = View.GONE
                            countDownTimer?.cancel()
                            startCountDownTimer(
                                requireContext(),
                                textTimeResend,
                                rlResendLine,
                                textResend
                            )
                            textResend.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.grey
                                )
                            )
                        }

                    }

                    is NetworkResult.Error -> {
                        customDialog(it.message, requireContext())

                    }

                    else -> {

                        Log.v("RegisterViewModel", "error::" + it.message)
                    }
                }
            }
        }
    }

    private fun otpVerifySignupEmail(tempId:String,otp: String, dialog: Dialog, text: TextView,
                                     checkBox: CheckBox?,dialogtext: String) {
        lifecycleScope.launch {
            loggedScreenViewModel.otpVerifySignupEmail(
                tempId,
                otp,
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            val session = SessionManager(requireActivity())
                            if (checkBox!=null && checkBox.isChecked){
                                session.setUserSession(true)
                            }
                            dialogSuccess(context, dialogtext,Gson().toJson(resp))
                        }
                        dialog.dismiss()
                        toggleLoginButtonEnabled(true, text)
                    }

                    is NetworkResult.Error -> {
                        customDialog(
                            it.message,
                            requireContext()
                        )
                        toggleLoginButtonEnabled(true, text)
                    }

                    else -> {
                        toggleLoginButtonEnabled(true, text)
                        Log.v("RegisterViewModel", "error::" + it.message)
                    }
                }
            }
        }

    }


    private fun otpVerifyForgotPassword(userId:String,otp: String, dialog: Dialog, text: TextView,
                                     dialogtext: String) {
        lifecycleScope.launch {
            loggedScreenViewModel.otpVerifyForgotPassword(
                userId,
                otp,
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let {
                            dialogNewPassword(context, dialogtext,userId)
                        }
                        dialog.dismiss()
                        toggleLoginButtonEnabled(true, text)
                    }

                    is NetworkResult.Error -> {
                        customDialog(
                            it.message,
                            requireContext()
                        )
                        toggleLoginButtonEnabled(true, text)
                    }

                    else -> {
                        toggleLoginButtonEnabled(true, text)
                        Log.v("RegisterViewModel", "error::" + it.message)
                    }
                }
            }
        }

    }



    private fun otpVerifyLoginPhone(userId:String,otp: String, dialog: Dialog, text: TextView,
                                    checkBox: CheckBox?) {
        lifecycleScope.launch {
            loggedScreenViewModel.otpVerifyLoginPhone(
                userId,
                otp,
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            val session: SessionManager = SessionManager(requireActivity())
                            if (resp.has("is_profile_complete") &&
                                resp.get("is_profile_complete").asBoolean) {
                                if (resp.has("user_id")) {
                                    if (checkBox!=null && checkBox.isChecked){
                                        session.setUserSession(true)
                                    }
                                    session.setUserId(resp.get("user_id").asInt)
                                    session.setAuthToken(resp.get("token").asString)
                                    val intent = Intent(requireActivity(), GuesMain::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    startActivity(intent)
                                }
                            }else{
                                if (checkBox!=null && checkBox.isChecked){
                                    session.setUserSession(true)
                                }
                                val bundle = Bundle()
                                bundle.putString("data",Gson().toJson(resp))
                                findNavController().navigate(R.id.completeProfileFragment,bundle)
                            }
                        }
                        dialog.dismiss()
                        toggleLoginButtonEnabled(true, text)
                    }

                    is NetworkResult.Error -> {
                        customDialog(
                            it.message,
                            requireContext()
                        )
                        toggleLoginButtonEnabled(true, text)
                    }

                    else -> {
                        toggleLoginButtonEnabled(true, text)
                        Log.v("RegisterViewModel", "error::" + it.message)
                    }
                }
            }
        }

    }

    private fun forgotPassword(email:String,dialog: Dialog, text: TextView) {
        lifecycleScope.launch {
            loggedScreenViewModel.forgotPassword(
                email
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            val userId = resp.second
                            val text = "Your password has been changed\n successfully."
                            val textHeaderOfOtpVerfication =
                                "Please type the verification code send \nto "+email
                            dialogOtp(requireContext(),
                                text,
                                textHeaderOfOtpVerfication, "", email,
                                userId,null,"ForgotPassword")
                        }
                        dialog.dismiss()
                        toggleLoginButtonEnabled(true, text)
                    }

                    is NetworkResult.Error -> {
                        customDialog(
                            it.message,
                            requireContext()
                        )
                        toggleLoginButtonEnabled(true, text)
                    }

                    else -> {
                        toggleLoginButtonEnabled(true, text)
                        Log.v("RegisterViewModel", "error::" + it.message)
                    }
                }
            }
        }

    }

    private fun otpVerifySignupPhone(tempId:String,otp: String, dialog: Dialog, text: TextView,
                                    checkBox: CheckBox?,dialogtext: String) {
        lifecycleScope.launch {
            loggedScreenViewModel.otpVerifySignupPhone(
                tempId,
                otp,
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            val session = SessionManager(requireActivity())
                            if (checkBox!=null && checkBox.isChecked){
                                session.setUserSession(true)
                            }
                            dialogSuccess(context, dialogtext,Gson().toJson(resp))
                        }
                        dialog.dismiss()
                        toggleLoginButtonEnabled(true, text)
                    }

                    is NetworkResult.Error -> {
                        customDialog(
                            it.message,
                            requireContext()
                        )
                        toggleLoginButtonEnabled(true, text)
                    }

                    else -> {
                        toggleLoginButtonEnabled(true, text)
                        Log.v("RegisterViewModel", "error::" + it.message)
                    }
                }
            }
        }

    }

    private fun resendLoginMobile(
        code: String,
        number: String,
        rlResendLine: RelativeLayout,
        incorrectOtp: TextView,
        textTimeResend: TextView,
        textResend: TextView
    ) {
        lifecycleScope.launch {
            loggedScreenViewModel.loginPhoneNumber(
                code,
                number
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let {
                            rlResendLine.visibility = View.VISIBLE
                            incorrectOtp.visibility = View.GONE
                            countDownTimer?.cancel()
                            startCountDownTimer(
                                requireContext(),
                                textTimeResend,
                                rlResendLine,
                                textResend
                            )
                            textResend.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.grey
                                )
                            )
                        }
                    }

                    is NetworkResult.Error -> {
                        customDialog(
                            it.message,
                            requireContext()
                        )
                    }

                    else -> {
                        Log.v("RegisterViewModel", "error::" + it.message)
                    }
                }
            }
        }

    }

    private fun resendRegisterMobile(
        code: String,
        number: String,
        rlResendLine: RelativeLayout,
        incorrectOtp: TextView,
        textTimeResend: TextView,
        textResend: TextView
    ) {
        Log.d(TAG, "Inside of fragment")
        lifecycleScope.launch {
            loggedScreenViewModel.signupPhoneNumber(
                code, number
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            rlResendLine.visibility = View.VISIBLE
                            incorrectOtp.visibility = View.GONE
                            countDownTimer?.cancel()
                            startCountDownTimer(
                                requireContext(),
                                textTimeResend,
                                rlResendLine,
                                textResend
                            )
                            textResend.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.grey
                                )
                            )
                        }
                    }

                    is NetworkResult.Error -> {
                        customDialog(it.message, requireContext())
                    }

                    else -> {
                        Log.v("RegisterViewModel", "error::" + it.message)
                    }
                }
            }
        }

    }


    private fun startCountDownTimer(
        context: Context,
        textTimeResend: TextView,
        rlResendLine: RelativeLayout,
        textResend: TextView
    ) {
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


    private fun dialogNewPassword(context: Context?, text: String,
                                  userId: String) {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_new_password)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            val imageCross = findViewById<ImageView>(R.id.imageCross)
            val etPassword = findViewById<EditText>(R.id.etPassword)
            val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
            val textSubmitButton = findViewById<TextView>(R.id.textSubmitButton)
            textSubmitButton.setOnClickListener {
                toggleLoginButtonEnabled(false, textSubmitButton)
                lifecycleScope.launch {
                    loggedScreenViewModel.networkMonitor.isConnected
                        .distinctUntilChanged() // Ignore duplicate consecutive values
                        .collect { isConn ->
                            if (!isConn) {
                                customDialog(
                                    resources.getString(R.string.no_internet_dialog_msg),
                                    requireContext()
                                )
                                toggleLoginButtonEnabled(true, textSubmitButton)
                            } else {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (etPassword.text!!.isEmpty()) {
                                        etPassword.error = "Password required"
                                        customDialog(AppConstant.password, requireContext())
                                        toggleLoginButtonEnabled(true, textSubmitButton)
                                    }else  if (etConfirmPassword.text!!.isEmpty()) {
                                        etConfirmPassword.error = "Confirm Password required"
                                        customDialog(AppConstant.conPassword, requireContext())
                                        toggleLoginButtonEnabled(true, textSubmitButton)
                                    }
                                    else {
                                        resetPassword(userId,
                                            etPassword.text.toString(),
                                            etConfirmPassword.text.toString(),
                                            dialog,textSubmitButton,text)
                                    }
                                }
                            }
                        }
                }
            }

            imageCross.setOnClickListener {
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }
    private fun resetPassword(userId:String,password: String, passwordConfirmation:String
                              ,dialog: Dialog, text: TextView,
                                        dialogtext: String) {
        lifecycleScope.launch {
            loggedScreenViewModel.resetPassword(
                userId,
                password,
                passwordConfirmation,
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let {
                            dialogSuccess(context, dialogtext,"")
                            dialog.dismiss()
                        }
                        dialog.dismiss()
                        toggleLoginButtonEnabled(true, text)
                    }

                    is NetworkResult.Error -> {
                        customDialog(
                            it.message,
                            requireContext()
                        )
                        toggleLoginButtonEnabled(true, text)
                    }

                    else -> {
                        toggleLoginButtonEnabled(true, text)
                        Log.v("RegisterViewModel", "error::" + it.message)
                    }
                }
            }
        }

    }

    private fun dialogSuccess(context: Context?, text: String, data:String) {
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
                    val bundle = Bundle()
                    bundle.putString("data",data)
                    navController?.navigate(R.id.turnNotificationsFragment,bundle)

                } else if (text == "Your password has been changed\n successfully.") {
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
                val sessionManager = SessionManager(context)
                sessionManager.setUserId(-1)
                val intent = Intent(context, AuthActivity::class.java)
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


    private fun showPopupWindow(anchorView: View, position: Int) {
        // Inflate the custom layout for the popup menu
        val popupView =
            LayoutInflater.from(context).inflate(R.layout.popup_filter_all_conversations, null)

        // Create PopupWindow with the custom layout
        val popupWindow = PopupWindow(
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
        popupWindow.showAsDropDown(
            anchorView,
            xOffset,
            yOffset,
            Gravity.END
        )  // Adjust the Y offset dynamically
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


    private fun toggleLoginButtonEnabled(isEnabled: Boolean, text: TextView) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                text.isEnabled = isEnabled
            } catch (e: Exception) {
                Log.e(ErrorDialog.TAG, "exception toggleLoginButtonEnabled ${e.message}")
            }
        }
    }

}

