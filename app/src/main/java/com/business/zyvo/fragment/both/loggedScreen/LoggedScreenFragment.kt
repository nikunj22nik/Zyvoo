package com.business.zyvo.fragment.both.loggedScreen

import android.Manifest

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable

import android.location.LocationManager

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher

import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.business.zyvo.AppConstant
import com.business.zyvo.AppConstant.Companion.passwordMustConsist
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.OnClickListener
import com.business.zyvo.OnClickListener1
import com.business.zyvo.R
import com.business.zyvo.activity.AuthActivity
import com.business.zyvo.activity.GuesMain
import com.business.zyvo.activity.MainActivity
import com.business.zyvo.activity.guest.filter.FiltersActivity
import com.business.zyvo.activity.guest.WhereTimeActivity
import com.business.zyvo.activity.guest.propertydetails.RestaurantDetailActivity
import com.business.zyvo.activity.guest.sorryresult.SorryActivity
import com.business.zyvo.adapter.LoggedScreenAdapter
import com.business.zyvo.databinding.FragmentLoggedScreenBinding

import com.business.zyvo.model.Data
import com.business.zyvo.model.SocialLoginModel

import com.business.zyvo.fragment.guest.home.model.HomePropertyData
import com.business.zyvo.model.FilterRequest
import com.business.zyvo.model.SearchFilterRequest

import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.CommonAuthWorkUtils
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.ErrorDialog.TAG
import com.business.zyvo.utils.MultipartUtils
import com.business.zyvo.utils.NetworkMonitorCheck
import com.business.zyvo.utils.PermissionManager
import com.business.zyvo.viewmodel.LoggedScreenViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hbb20.CountryCodePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@AndroidEntryPoint
class LoggedScreenFragment : Fragment(), OnClickListener, View.OnClickListener, OnClickListener1 {
    private  val LOCATION_SETTINGS_REQUEST_CODE = 200
    private lateinit var startSearchForResult: ActivityResultLauncher<Intent>
    private lateinit var startForResult: ActivityResultLauncher<Intent>
    lateinit var navController: NavController
    private lateinit var otpDigits: Array<EditText>
    private var countDownTimer: CountDownTimer? = null
    var resendEnabled = false
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var _binding: FragmentLoggedScreenBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: LoggedScreenAdapter
    private var commonAuthWorkUtils: CommonAuthWorkUtils? = null
    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity

    private var personName: String? = ""
    private var personGivenName: String? = ""
    private var personFamilyName: String? = ""
    private var personEmail: String? = ""
    var username: String = ""
    private var personId: String = ""
    private var token: String = ""
    private var personPhoto: Uri? = null
    private var socialModel: SocialLoginModel? = null
    private lateinit var sessionManager: SessionManager
    private var showOneTapUI = true
    private var Token = ""
    private var latitude: String = ""
    private var longitude: String = ""
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var locationManager: LocationManager? = null
    var session: SessionManager? = null
    private var homePropertyData: MutableList<HomePropertyData> = mutableListOf()
    private var loginDialog: Dialog? = null
    private var logType = "login"
    private var islogTypeMobile = false
    private var islogTypeEmail = false

    private val loggedScreenViewModel: LoggedScreenViewModel by lazy {
        ViewModelProvider(this)[LoggedScreenViewModel::class.java]
    }

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val showDialog = result.data?.getBooleanExtra("SHOW_DIALOG", false) ?: false
            if (showDialog) {
                dialogLogin(requireContext()) // Open your dialog
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        Log.d("TESTING", "Inside On Create of LoggedScreen")
        navController = findNavController()
        commonAuthWorkUtils = CommonAuthWorkUtils(requireActivity(), navController)
        _binding = FragmentLoggedScreenBinding.inflate(inflater, container, false)

        session = SessionManager(requireActivity())
        // This is use for LocationServices declaration
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationManager =
            requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

        // This condition for check location run time permission

        if (!PermissionManager.hasLocationPermission(requireActivity())) {
            alertBoxLocation1()
        }else{
            getCurrentLocation()
        }

//        if (ContextCompat.checkSelfPermission(
//                requireActivity(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
//                requireActivity(),
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            getCurrentLocation()
//        } else {
//            requestPermissions(
//                arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ), 100
//            )
//        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        sessionManager = SessionManager(requireContext())
        getFCMToken()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        googleSignInClient.signOut()

        startSearchForResult = registerForActivityResult(
            ActivityResultContracts
                .StartActivityForResult()
        ) { result ->
            try {
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    // Handle the resultl
                    if (data != null) {
                        if (data.extras?.getString("type").equals("filter")) {
                            val value: SearchFilterRequest = Gson().fromJson(
                                data.extras?.getString("SearchrequestData"),
                                SearchFilterRequest::class.java
                            )
                            value.let {
                                it.user_id = ""
                                Log.d(TAG, Gson().toJson(value))
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    getHomeDataSearchFilter(it)
                                }
                            }
                        } else {
                            loadHomeApi()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, e.message!!)
            }
        }

        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                try {
                    if (result.resultCode == Activity.RESULT_OK) {
                        val data = result.data
                        // Handle the resultl
                        if (data != null) {
                            if (data.extras?.getString("type").equals("filter")) {
                                val value: FilterRequest = Gson().fromJson(
                                    data.extras?.getString("requestData"), FilterRequest::class.java
                                )
                                value.let {
                                    Log.d(TAG, Gson().toJson(value))
                                    filteredDataAPI(it)
                                }
                            } else {
                                loadHomeApi()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.message!!)
                }
            }
    }


    private fun filteredDataAPI(filterRequest: FilterRequest) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                loggedScreenViewModel.getFilterHomeDataApi(
                    "",
                    filterRequest.latitude, filterRequest.longitude,
                    filterRequest.place_type,
                    filterRequest.minimum_price,
                    filterRequest.maximum_price,
                    filterRequest.location, filterRequest.date,
                    filterRequest.time,
                    filterRequest.people_count,
                    filterRequest.property_size,
                    filterRequest.bedroom,
                    filterRequest.bathroom,
                    filterRequest.instant_booking,
                    filterRequest.self_check_in,
                    filterRequest.allows_pets,
                    filterRequest.activities,
                    filterRequest.amenities, filterRequest.languages
                ).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                val listType = object : TypeToken<List<HomePropertyData>>() {}.type
                                val properties: MutableList<HomePropertyData> =
                                    Gson().fromJson(resp, listType)
                                homePropertyData = properties
                                if (homePropertyData.isNotEmpty()) {
                                    adapter.updateData(homePropertyData)
                                }
                            }
                        }

                        is NetworkResult.Error -> {
                            requireActivity().startActivity(
                                Intent(
                                    requireActivity(),
                                    SorryActivity::class.java
                                )
                            )
                        }

                        else -> {
                            Log.v(TAG, "error::" + it.message)
                        }
                    }
                }
            }
        } else {
            showErrorDialog(
                requireContext(),
                resources.getString(R.string.no_internet_dialog_msg)
            )
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getHomeDataSearchFilter(filterRequest: SearchFilterRequest) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                loggedScreenViewModel.getHomeDataSearchFilter(
                    "",
                    filterRequest.latitude, filterRequest.longitude,
                    filterRequest.date,
                    filterRequest.hour,
                    ErrorDialog.convertDateToTimeFormat(filterRequest.start_time),
                    ErrorDialog.convertDateToTimeFormat(filterRequest.end_time),
                    filterRequest.activity,
                    filterRequest.property_price
                ).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                //  session?.setFilterRequest("")
                                //   session?.setSearchFilterRequest("")
                                val listType = object : TypeToken<List<HomePropertyData>>() {}.type
                                val properties: MutableList<HomePropertyData> =
                                    Gson().fromJson(resp, listType)
                                homePropertyData = properties
                                if (homePropertyData.isNotEmpty()) {
                                    adapter.updateData(homePropertyData)
                                }
                            }
                        }

                        is NetworkResult.Error -> {
                            requireActivity().startActivity(
                                Intent(
                                    requireActivity(),
                                    SorryActivity::class.java
                                )
                            )
                        }

                        else -> {
                            Log.v(TAG, "error::" + it.message)
                        }
                    }
                }
            }
        } else {
            showErrorDialog(
                requireContext(),
                resources.getString(R.string.no_internet_dialog_msg)
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textLogin.setOnClickListener(this)
        binding.textWhere.setOnClickListener(this)
        binding.textTime.setOnClickListener(this)
        binding.textActivity.setOnClickListener(this)
        binding.filterIcon.setOnClickListener(this)
        binding.textWishlists.setOnClickListener(this)
        binding.textDiscover.setOnClickListener(this)

        // Set up adapter with lifecycleOwner passed
        adapter = LoggedScreenAdapter(
            requireContext(),
            homePropertyData,
            this,
            this
        )
        binding.recyclerViewBooking.adapter = adapter

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
    }

    override fun onStart() {
        super.onStart()
        getFCMToken()

    }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val signInType =
                    data?.getStringExtra("SIGN_IN_TYPE") ?: "login" // Default to "login"
                firebaseAuthWithGoogle(account, signInType)
            } catch (e: ApiException) {
                Log.w("AUTH", "Google sign-in failed", e)
            }
        }


    private fun startGoogleSignIn(type: String) {
        val signInIntent = googleSignInClient.signInIntent.putExtra("SIGN_IN_TYPE", type)
        googleSignInLauncher.launch(signInIntent)


    }


    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount, signInType: String) {
        try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Log.d("AUTH", "signInWithCredential: success")
                        // Extract user detail
                        personName = account.displayName
                        personGivenName = account.givenName
                        personFamilyName = account.familyName
                        personEmail = account.email
                        personId = account.id ?: ""
                        personPhoto = account.photoUrl

                        val firstName = personGivenName ?: personName?.substringBefore(" ") ?: ""
                        val lastName = personFamilyName ?: personName?.substringAfter(" ") ?: ""

                        Log.d(
                            "GoogleUser", """
                        ID: $personId
                        Name: $personName
                        First Name: $firstName
                        Last Name: $lastName
                        Email: $personEmail
                        Photo: $personPhoto""".trimIndent()
                        )

                        // Call your API with extracted information
                        callSocialApi(firstName, lastName, signInType)
                    } else {
                        Log.w("AUTH", "signInWithCredential: failure", task.exception)
                        showErrorDialog(requireContext(), "Authentication failed")
                    }
                }
        } catch (e: ApiException) {
            Log.e("GoogleAuthError", "Google sign-in failed: ${e.message}", e)
            showErrorDialog(requireContext(), "Google sign-in failed: ${e.message}")
        } catch (e: Exception) {
            Log.e("Exception", "Unexpected error: ${e.message}", e)
        }
    }

    private fun callSocialApi(firstName: String?, lastName: String?, signInType: String) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                try {
                    loggedScreenViewModel.getSocialAPI(
                        firstName ?: "",
                        lastName ?: "",
                        personEmail ?: "",
                        personId,
                        token,
                        "Android"
                    ).collect { result ->
                        when (result) {
                            is NetworkResult.Success -> {
                                result.data?.let { resp ->
                                    try {
                                        val data: Data = Gson().fromJson(resp, Data::class.java)
                                        sessionManager.setUserId(data.user_id)
                                        sessionManager.setAuthToken(data.token)
                                            data.user_image?.let { Log.d("imageCheck", it) }
                                            data.user_image?.let { sessionManager.setUserImage(it) }
                                       

                                        val bundle = Bundle().apply {
                                            putString("data", Gson().toJson(data))
                                            putString("type", "google")
                                            putString("email", personEmail)
                                        }

                                        if (signInType == "login") {
                                            session?.setLoginType("emailAddress")
                                            val intent = Intent(
                                                requireActivity(),
                                                GuesMain::class.java
                                            ).apply {
                                                flags =
                                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                                                putExtras(bundle)
                                            }
                                            startActivity(intent)
                                        } else if (signInType == "register") {
                                            navController.navigate(
                                                R.id.completeProfileFragment,
                                                bundle
                                            )
                                        }
                                    } catch (e: Exception) {
                                        Log.e(
                                            "SocialLogin",
                                            "Error parsing response: ${e.localizedMessage}",
                                            e
                                        )
                                        showErrorDialog(requireContext(), "Parsing error occurred")
                                    }
                                }
                            }

                            is NetworkResult.Error -> {
                                showErrorDialog(requireContext(), result.message ?: "Unknown error")
                            }

                            else -> {
                                Log.v("ErrorDialog", "Unexpected result: ${result.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SocialApiError", "Exception occurred: ${e.localizedMessage}", e)
                    showErrorDialog(requireContext(), "Unexpected error occurred")
                }
            }
        } else {
            showErrorDialog(requireContext(), getString(R.string.no_internet_dialog_msg))
        }
    }


    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    token = task.result

                    Log.d("FCM", "Device Token: $token")
                } else {
                    Log.e("FCM", "Token retrieval failed", task.exception)
                }
            }
    }

    private fun adapterWork() {
        adapter.setOnItemClickListener(object : LoggedScreenAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                Log.d("TESTING_ZYVOO", "I AM HERE IN DEVELOPMENT")
               // dialogLogin(requireContext())

                Log.d(TAG,"I AM HERE IN DEVELOPMENT")
                Log.d("checkPropertyId", homePropertyData.get(position)?.property_id.toString())
                val intent = Intent(requireContext(), RestaurantDetailActivity::class.java)
                intent.putExtra("LoginType","NotLogging")
                intent.putExtra("propertyId", homePropertyData.get(position)?.property_id.toString())
                intent.putExtra("propertyMile", homePropertyData.get(position)?.distance_miles.toString())

                activityResultLauncher.launch(intent)
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
                islogTypeMobile = false
                dialogLogin(requireContext())

            }

            R.id.filter_icon -> {
                val intent = Intent(requireContext(), FiltersActivity::class.java)
                startForResult.launch(intent)
            }

            R.id.textWhere -> {
                val intent = Intent(requireContext(), WhereTimeActivity::class.java)
                intent.putExtra(AppConstant.WHERE, AppConstant.WHERE)
                startSearchForResult.launch(intent)
            }

            R.id.textTime -> {
                val intent = Intent(requireContext(), WhereTimeActivity::class.java)
                intent.putExtra(AppConstant.TIME, AppConstant.TIME)
                startSearchForResult.launch(intent)
            }

            R.id.textActivity -> {
                val intent = Intent(requireContext(), WhereTimeActivity::class.java)
                intent.putExtra(AppConstant.ACTIVITY, AppConstant.ACTIVITY)
                startSearchForResult.launch(intent)
            }

            R.id.textDiscover -> {
                latitude.takeIf { it.isNotEmpty() }?.let {
                    longitude.takeIf { it.isNotEmpty() }?.let {
                        loadHomeApi()
                    }
                }

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


    @SuppressLint("MissingInflatedId")
    fun dialogLogin(context: Context?) {
        if (loginDialog?.isShowing == true) {
            return // Dialog is already showing, don't open another one
        }
        loginDialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        loginDialog?.apply {
            setCancelable(true)
            setContentView(R.layout.dialog_login)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            val imageCross = findViewById<ImageView>(R.id.imageCross)
            val imageEmailSocial = findViewById<ImageView>(R.id.imageEmailSocial)
            val googleLoginBtn = findViewById<ImageView>(R.id.googleLogin)
            val etMobileNumber = findViewById<EditText>(R.id.etMobileNumber)
            val textContinueButton = findViewById<TextView>(R.id.textContinueButton)
            val checkBox = findViewById<CheckBox>(R.id.checkBox)
            val textTitle = findViewById<TextView>(R.id.textTitle)
            val textForget = findViewById<TextView>(R.id.textForget)
            val textEnterYourEmail = findViewById<TextView>(R.id.textEnterYourEmail)
            val textRegister = findViewById<TextView>(R.id.textRegister)
            val textDontHaveAnAccount = findViewById<TextView>(R.id.textDontHaveAnAccount)
            val countyCodePicker = findViewById<CountryCodePicker>(R.id.countyCodePicker)
            countyCodePicker.setFlagSize(resources.getDimensionPixelSize(R.dimen.default_twentySpacing)) // yaha flag size set hoga
            // Set IME options for password field
            etMobileNumber.imeOptions = EditorInfo.IME_ACTION_DONE
            etMobileNumber.setImeActionLabel("Login", EditorInfo.IME_ACTION_DONE)

            // Handle keyboard "Done" action
            etMobileNumber.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Perform the same action as login button click
                    textContinueButton.performClick()
                    true
                } else {
                    false
                }
            }
            textRegister.setOnClickListener {
                if (islogTypeMobile){
                    islogTypeMobile = false
                    textRegister.text = getString(R.string.register_now)
                    textTitle.text = getString(R.string.login)
                    textEnterYourEmail.text =
                        getString(R.string.enter_your_phone_to_login_your_naccount)
                    textDontHaveAnAccount.text = getString(R.string.don_t_have_an_account)
                }else {
                    textRegister.text = getString(R.string.login_here)
                    textTitle.text = getString(R.string.register_now)
                    textEnterYourEmail.text =
                        getString(R.string.enter_your_phone_to_register_your_naccount)
                    textDontHaveAnAccount.text = getString(R.string.already_have_an_account)
                    islogTypeMobile = true
                }
              //  dialogRegister(context)
             //   dismiss()
            }

            textForget.setOnClickListener {
                dialogForgotPassword(context)
                dismiss()
            }
            imageEmailSocial.setOnClickListener {
                islogTypeEmail = false
                dialogLoginEmail(context)
                dismiss()
            }
            textContinueButton.setOnClickListener {
                toggleLoginButtonEnabled(false, textContinueButton)
                if (NetworkMonitorCheck._isConnected.value) {
                    if (islogTypeMobile){
                        lifecycleScope.launch(Dispatchers.Main) {
                            if (etMobileNumber.text!!.isEmpty()) {
                                toggleLoginButtonEnabled(true, textContinueButton)
                                showErrorDialog(requireContext(), AppConstant.mobile)

                            } else if (!MultipartUtils.isPhoneNumberMatchingCountryCode(etMobileNumber.text.toString(),countyCodePicker.selectedCountryCodeWithPlus)) {
                                showErrorDialog(requireContext(), AppConstant.VALID_PHONE)
                                toggleLoginButtonEnabled(true, textContinueButton)
                            } else {
                                val phoneNumber = etMobileNumber.text.toString()
                                Log.d(TAG, phoneNumber)
                                val countryCode = countyCodePicker.selectedCountryCodeWithPlus
                                Log.d(TAG, countryCode)
                                callingRegisterPhone(
                                    countryCode,
                                    phoneNumber,
                                    loginDialog!!,
                                    textContinueButton,
                                    checkBox
                                )
                            }
                        }
                    }else{
                        lifecycleScope.launch(Dispatchers.Main) {
                            if (etMobileNumber.text!!.isEmpty()) {
                                showErrorDialog(requireContext(), AppConstant.mobile)
                                toggleLoginButtonEnabled(true, textContinueButton)
                            }
                            else if (!MultipartUtils.isPhoneNumberMatchingCountryCode(etMobileNumber.text.toString(),countyCodePicker.selectedCountryCodeWithPlus)){
                                showErrorDialog(requireContext(),AppConstant.validPhoneNumber)
                                toggleLoginButtonEnabled(true, textContinueButton)

                            }
                            else {
                                val phoneNumber = etMobileNumber.text.toString()
                                Log.d(TAG, phoneNumber)
                                val countryCode =
                                    countyCodePicker.selectedCountryCodeWithPlus
                                Log.d(TAG, countryCode)
                                submitLogin(
                                    countryCode, phoneNumber,
                                    loginDialog!!, textContinueButton, checkBox
                                )
                            }
                        }
                    }
                } else {
                    showErrorDialog(
                        requireContext(),
                        resources.getString(R.string.no_internet_dialog_msg)
                    )
                    toggleLoginButtonEnabled(true, textContinueButton)
                }

            }
            imageCross.setOnClickListener {
                dismiss()
            }
            googleLoginBtn.setOnClickListener {
                if (NetworkMonitorCheck._isConnected.value) {
                    dismiss()
                    if (islogTypeMobile){
                        startGoogleSignIn("register")
                    }else{
                        startGoogleSignIn("login")
                    }

                } else {
                    showErrorDialog(
                        requireContext(),
                        resources.getString(R.string.no_internet_dialog_msg)
                    )
                }
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

    private fun submitLogin(
        code: String, number: String, dialog: Dialog, text: TextView,
        checkBox: CheckBox
    ) {
        lifecycleScope.launch {
            loggedScreenViewModel.loginPhoneNumber(
                code, number, token
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            val text = "Login Successful"
                            val textHeaderOfOtpVerfication =
                                "Please type the verification code send \n to $code$number"
                            dialog.dismiss()
                            val userId = resp.second

//                            dialogOtp(
//                                requireActivity(), text, textHeaderOfOtpVerfication,
//                                code, number, userId, checkBox, "loginPhone"
//                            )

                            dialogOtp(
                                requireActivity(), text, textHeaderOfOtpVerfication,
                                code, number, userId, checkBox, "loginPhone"
                            )
                            Toast.makeText(requireContext(), resp.first, Toast.LENGTH_LONG).show()

                        }

                        toggleLoginButtonEnabled(true, text)
                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(
                            requireContext(),
                            it.message!!
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


    @SuppressLint("MissingInflatedId")
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
            val googleRegBtn = findViewById<ImageView>(R.id.googleRegLogin)
            val textKeepLogged = findViewById<TextView>(R.id.textKeepLogged)

            val textLoginButton = findViewById<TextView>(R.id.textLoginButton)

            val countyCodePicker = findViewById<CountryCodePicker>(R.id.countyCodePicker)


            etMobileNumber.imeOptions = EditorInfo.IME_ACTION_DONE
            etMobileNumber.setImeActionLabel("Login", EditorInfo.IME_ACTION_DONE)

            // Handle keyboard "Done" action
            etMobileNumber.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Perform the same action as login button click
                    textContinueButton.performClick()
                    true
                } else {
                    false
                }
            }

            textLoginButton.setOnClickListener {
                dialogLogin(context)
                dismiss()
            }
            textContinueButton.setOnClickListener {
                toggleLoginButtonEnabled(false, textContinueButton)
                if (NetworkMonitorCheck._isConnected.value) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        if (etMobileNumber.text!!.isEmpty()) {
                            toggleLoginButtonEnabled(true, textContinueButton)
                            showErrorDialog(requireContext(), AppConstant.mobile)

                        } else if (!MultipartUtils.isPhoneNumberMatchingCountryCode(etMobileNumber.text.toString(),countyCodePicker.selectedCountryCodeWithPlus)) {
                            showErrorDialog(requireContext(), AppConstant.VALID_PHONE)
                            toggleLoginButtonEnabled(true, textContinueButton)
                        } else {
                            val phoneNumber = etMobileNumber.text.toString()
                            Log.d(TAG, phoneNumber)
                            val countryCode = countyCodePicker.selectedCountryCodeWithPlus
                            Log.d(TAG, countryCode)
                            callingRegisterPhone(
                                countryCode,
                                phoneNumber,
                                dialog,
                                textContinueButton,
                                checkBox
                            )
                        }
                    }
                } else {
                    showErrorDialog(
                        requireContext(),
                        resources.getString(R.string.no_internet_dialog_msg)
                    )
                    toggleLoginButtonEnabled(true, textContinueButton)
                }

            }
            imageEmailSocial.setOnClickListener {
                dialogRegisterEmail(context)
                dismiss()
            }
            googleRegBtn.setOnClickListener {
                if (NetworkMonitorCheck._isConnected.value) {
                    startGoogleSignIn("register")
                    dismiss()
                } else {
                    showErrorDialog(
                        requireContext(),
                        resources.getString(R.string.no_internet_dialog_msg)
                    )
                }
            }

            imageCross.setOnClickListener {
                dismiss()
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            show()
        }
    }

    private fun callingRegisterPhone(
        code: String, number: String, dialog: Dialog,
        text: TextView, checkBox: CheckBox
    ) {
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
                            dialogOtp(
                                requireContext(), text, textHeaderOfOtpVerfication, code,
                                number,

                                temp, checkBox, "RegisterPhone"
                            )


                            Toast.makeText(requireContext(), resp.first, Toast.LENGTH_LONG).show()

                        }
                        dialog.dismiss()
                        toggleLoginButtonEnabled(true, text)
                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(requireContext(), it.message!!)
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
            val textTitle = findViewById<TextView>(R.id.textTitle)
            val textForget = findViewById<TextView>(R.id.textForget)

            val imgHidePass = findViewById<ImageView>(R.id.imgHidePass)
            val imgShowPass = findViewById<ImageView>(R.id.imgShowPass)

            val textDontHaveAnAccount = findViewById<TextView>(R.id.textDontHaveAnAccount)
            val textRegister = findViewById<TextView>(R.id.textRegister)

            val etLoginEmail = findViewById<EditText>(R.id.etLoginEmail)
            val etLoginPassword = findViewById<EditText>(R.id.etLoginPassword)

            val textEnterYourEmail = findViewById<TextView>(R.id.textEnterYourEmail)

            // Set IME options for password field
            etLoginPassword.imeOptions = EditorInfo.IME_ACTION_DONE
            etLoginPassword.setImeActionLabel("Login", EditorInfo.IME_ACTION_DONE)

            // Handle keyboard "Done" action
            etLoginPassword.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Perform the same action as login button click
                    textLoginButton.performClick()
                    true
                } else {
                    false
                }
            }
            eyeHideShow(imgHidePass, imgShowPass, etLoginPassword)

            textRegister.setOnClickListener {
                if (islogTypeEmail){
                    islogTypeEmail = false
                    textRegister.text = getString(R.string.register_now)
                    textTitle.text = getString(R.string.login)
                    textEnterYourEmail.text =
                        getString(R.string.enter_your_email_and_password_to_n_login_your_account)
                    textDontHaveAnAccount.text = getString(R.string.don_t_have_an_account)
                    textLoginButton.text = getString(R.string.login)
                    textForget.visibility = View.VISIBLE
                }else {
                    islogTypeEmail = true
                    textRegister.text = getString(R.string.login_here)
                    textTitle.text = getString(R.string.register_now)
                    textEnterYourEmail.text =
                        getString(R.string.enter_your_email_to_register_your_n_account)
                    textDontHaveAnAccount.text = getString(R.string.already_have_an_account)
                    textLoginButton.text = getString(R.string.create_account)
                    textForget.visibility = View.GONE
                }
             //   dialogRegisterEmail(context)
             //   dismiss()
            }

            textForget.setOnClickListener {
                dialogForgotPassword(context)
                dismiss()
            }
            textLoginButton.setOnClickListener {
                toggleLoginButtonEnabled(false, textLoginButton)
                if (NetworkMonitorCheck._isConnected.value) {
                    if (islogTypeEmail){
                        lifecycleScope.launch(Dispatchers.Main) {
                            if (etLoginEmail.text.isEmpty()) {
                                etLoginEmail.error = "Email Address required"
                                showErrorDialog(requireContext(), AppConstant.email)
                                toggleLoginButtonEnabled(true, textLoginButton)
                            } else if (!isValidEmail(etLoginEmail.text.toString())) {
                                etLoginEmail.error = "Invalid Email Address"
                                showErrorDialog(requireContext(), AppConstant.invalideemail)
                                toggleLoginButtonEnabled(true, textLoginButton)
                            } else if (etLoginPassword.text.isEmpty()) {
                                etLoginPassword.error = "Password required"
                                showErrorDialog(requireContext(), AppConstant.password)
                                toggleLoginButtonEnabled(true, textLoginButton)
                            } else if (!checkPasswordValidity(etLoginPassword.text.toString())) {
                                toggleLoginButtonEnabled(true, textLoginButton)
                                return@launch
                            }  else {
                                signupEmail(
                                    etLoginEmail.text.toString(),
                                    etLoginPassword.text.toString(),
                                    dialog, textLoginButton,
                                    checkBox
                                )
                            }
                        }
                    }else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            if (etLoginEmail.text!!.isEmpty()) {
                                etLoginEmail.error = "Email address required"
                                showErrorDialog(requireContext(), AppConstant.email)
                                toggleLoginButtonEnabled(true, textLoginButton)
                            } else if (!isValidEmail(etLoginEmail.text.toString())) {
                                etLoginEmail.error = "Invalid email address"
                                showErrorDialog(requireContext(), AppConstant.invalideemail)
                                toggleLoginButtonEnabled(true, textLoginButton)
                            } else if (etLoginPassword.text!!.isEmpty()) {
                                etLoginPassword.error = "Password required"
                                showErrorDialog(requireContext(), AppConstant.password)
                                toggleLoginButtonEnabled(true, textLoginButton)
                            } else if (!checkPasswordValidity(etLoginPassword.text.toString())) {
                                toggleLoginButtonEnabled(true, textLoginButton)
                                return@launch
                            } else {
                                loginEmail(
                                    etLoginEmail.text.toString(),
                                    etLoginPassword.text.toString(),
                                    dialog, textLoginButton,
                                    checkBox
                                )
                            }
                        }
                    }
                } else {
                    showErrorDialog(
                        requireContext(),
                        resources.getString(R.string.no_internet_dialog_msg)
                    )
                    toggleLoginButtonEnabled(true, textLoginButton)
                }
            }
            imageCross.setOnClickListener {
                dismiss()
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }


    private fun validationEmailPassword(email: String, password: String): Boolean {

        if (!SessionManager(requireContext()).isValidEmailOrPhone(email)) {
            showErrorDialog(requireContext(), AppConstant.VALID_EMAIL)
            return false
        } else if (!SessionManager(requireContext()).isValidPassword(password)) {
            showErrorDialog(requireContext(), AppConstant.VALID_PASSWORD)
            return false
        }

        return true
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
                password,
                token
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            val session: SessionManager = SessionManager(requireActivity())
                            if (resp.has("is_profile_complete") &&
                                resp.get("is_profile_complete").asBoolean
                            ) {
                                if (resp.has("user_id")) {
                                    if (checkBox != null && checkBox.isChecked) {
                                        session.setUserSession(true)
                                    }else{
                                        session.setUserSession(false)
                                    }
                                    session.setUserId(resp.get("user_id").asInt)
                                    session.setAuthToken(resp.get("token").asString)
                                    if (resp.has("full_name") && !resp.get("full_name").isJsonNull) {
                                        session.setName(resp.get("full_name").asString)
                                    }

                                    Log.d(
                                        "Testing",
                                        "Response Token is " + resp.get("token").asString
                                    )
                                    session.setLoginType("emailAddress")
                                    if (resp.get("user_image") != null){
                                        session.setUserImage(resp.get("user_image").asString)
                                    }
                                    session.setCurrentPanel(AppConstant.Guest)
                                    val intent = Intent(requireActivity(), GuesMain::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    startActivity(intent)
                                }
                            } else {
                                if (checkBox != null && checkBox.isChecked) {
                                    session.setUserSession(true)
                                }else{
                                    session.setUserSession(false)
                                }
                                Log.d("Testing", "Response Token is " + resp.get("token").asString)
                                session.setUserId(resp.get("user_id").asInt)
                                session.setAuthToken(resp.get("token").asString)
                                val bundle = Bundle()
                                if (resp.has("full_name") && !resp.get("full_name").isJsonNull) {
                                    session.setName(resp.get("full_name").asString)
                                    bundle.putString("full_name", resp.get("full_name").asString)
                                }

                                bundle.putString("data", Gson().toJson(resp))
                                bundle.putString("type", "email")
                                bundle.putString("email", email)
                                findNavController().navigate(R.id.completeProfileFragment, bundle)
                            }
                        }
                        dialog.dismiss()
                        toggleLoginButtonEnabled(true, textLoginButton)
                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(requireContext(), it.message!!)
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

            val imgHidePass = findViewById<ImageView>(R.id.imgHidePass)
            val imgShowPass = findViewById<ImageView>(R.id.imgShowPass)
            val imgCorrect = findViewById<ImageView>(R.id.imgCorrect)
            val imgWrong = findViewById<ImageView>(R.id.imgWrong)
            etRegisterPassword.imeOptions = EditorInfo.IME_ACTION_DONE
            etRegisterPassword.setImeActionLabel("Login", EditorInfo.IME_ACTION_DONE)

            // Handle keyboard "Done" action
            etRegisterPassword.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Perform the same action as login button click
                    textCreateAccountButton.performClick()
                    true
                } else {
                    false
                }
            }

            etRegisterPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val password = s.toString()
                    if (checkPasswordValidity1(password)) {
                        // Show correct icon and hide wrong icon
                        imgCorrect.visibility = View.VISIBLE
                        imgWrong.visibility = View.GONE
                    } else {
                        // Show wrong icon and hide correct icon
                        imgCorrect.visibility = View.GONE
                        imgWrong.visibility = View.VISIBLE
                    }
                }
            })

            eyeHideShow(imgHidePass, imgShowPass, etRegisterPassword)

            textLoginHere.setOnClickListener {
                dialogLoginEmail(context)
                dismiss()
            }
            textCreateAccountButton.setOnClickListener {
                toggleLoginButtonEnabled(false, textCreateAccountButton)
                if (NetworkMonitorCheck._isConnected.value) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        if (etRegisterEmail.text.isEmpty()) {
                            etRegisterEmail.error = "Email Address required"
                            showErrorDialog(requireContext(), AppConstant.email)
                            toggleLoginButtonEnabled(true, textCreateAccountButton)
                        } else if (!isValidEmail(etRegisterEmail.text.toString())) {
                            etRegisterEmail.error = "Invalid Email Address"
                            showErrorDialog(requireContext(), AppConstant.invalideemail)
                            toggleLoginButtonEnabled(true, textCreateAccountButton)
                        } else if (etRegisterPassword.text.isEmpty()) {
                            etRegisterPassword.error = "Password required"
                            showErrorDialog(requireContext(), AppConstant.password)
                            toggleLoginButtonEnabled(true, textCreateAccountButton)
                        } else if (!checkPasswordValidity(etRegisterPassword.text.toString())) {
                            toggleLoginButtonEnabled(true, textCreateAccountButton)
                            return@launch
                        }  else {
                            signupEmail(
                                etRegisterEmail.text.toString(),
                                etRegisterPassword.text.toString(),
                                dialog, textCreateAccountButton,
                                checkBox
                            )
                        }
                    }
                } else {
                    showErrorDialog(
                        requireContext(),
                        resources.getString(R.string.no_internet_dialog_msg)
                    )
                    toggleLoginButtonEnabled(true, textCreateAccountButton)
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
                                "Please type the verification code send \nto " + email
                            dialogOtp(
                                requireActivity(),
                                text,
                                textHeaderOfOtpVerfication,
                                email,
                                password,
                                temp,
                                checkBox,
                                "RegisterEmail"
                            )
                        }
                        dialog.dismiss()
                        toggleLoginButtonEnabled(true, textLoginButton)
                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(requireContext(), it.message!!)
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

            // Set IME options for password field
            etEmail.imeOptions = EditorInfo.IME_ACTION_DONE
            etEmail.setImeActionLabel("Login", EditorInfo.IME_ACTION_DONE)

            // Handle keyboard "Done" action
            etEmail.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Perform the same action as login button click
                    textSubmitButton.performClick()
                    true
                } else {
                    false
                }
            }
            textSubmitButton.setOnClickListener {
                toggleLoginButtonEnabled(false, textSubmitButton)
                if (NetworkMonitorCheck._isConnected.value) {
                    if (isValidEmail(etEmail.text!!.toString().trim())) {
                        if (etEmail.text!!.isEmpty()) {
                            showErrorDialog(requireContext(), AppConstant.email)
                            toggleLoginButtonEnabled(true, textSubmitButton)
                        } else if (!isValidEmail(etEmail.text.toString())) {
                            showErrorDialog(requireContext(), AppConstant.invalideemail)
                            toggleLoginButtonEnabled(true, textSubmitButton)
                        } else {
                            lifecycleScope.launch(Dispatchers.Main) {
                                forgotPassword(
                                    etEmail.text.toString(),
                                    dialog, textSubmitButton
                                )
                            }
                        }
                    } else {
                        showErrorDialog(requireContext(), "Please enter a valid email.")
                        toggleLoginButtonEnabled(true, textSubmitButton)
                    }


                } else {
                    showErrorDialog(
                        requireContext(),
                        resources.getString(R.string.no_internet_dialog_msg)
                    )
                    toggleLoginButtonEnabled(true, textSubmitButton)
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
                dialogOtp(
                    context, text, textHeaderOfOtpVerfication, "", "",
                    "", null, ""
                )


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
                dialogOtp(
                    context, texter, textHeaderOfOtpVerfication, "", "",
                    "", null, ""
                )

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
        code: String, number: String, userId: String, checkBox: CheckBox?, otpType: String
    ) {
        val dialog = Dialog(context, R.style.BottomSheetDialog)
        dialog.apply {
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

            otpDigits[3].imeOptions = EditorInfo.IME_ACTION_DONE
            otpDigits[3].setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val otp = otpDigits.joinToString("") { it.text.toString() }
                    if (otp.length == 4 && otp.all { it.isDigit() }) {
                        textSubmitButton.performClick()
                    } else {
                        Toast.makeText(context, "Please enter the complete OTP.", Toast.LENGTH_SHORT).show()
                    }
                    true
                } else {
                    false
                }
            }


            for (i in 0 until otpDigits.size) {
                val index = i
                otpDigits.get(i).setOnClickListener { v ->
                    otpDigits.get(index).requestFocus()
                }

                otpDigits.get(i).addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence, start: Int, count: Int, after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence, start: Int, before: Int,
                        count: Int
                    ) {
                        val currentEditText = otpDigits.get(index)
                        if (s.length == 1) {
                            // Set background color when a value is entered
                            currentEditText.setBackgroundDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.otp_fill_box
                                )
                            )

                            // Move to next EditText
                            if (index < otpDigits.size - 1) {
                                otpDigits.get(index + 1).requestFocus()
                            }

                        } else if (s.isEmpty()) {
                            // Set default background color when value is removed
                            currentEditText.setBackgroundDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.otp_box
                                )
                            )

                            // Move to previous EditText
                            if (index > 0) {
                                otpDigits.get(index - 1).requestFocus()
                            }
                        }

                        /* if (s.length == 1 && index < otpDigits.size - 1) {
                             otpDigits.get(index + 1).requestFocus()

                         } else if (s.length == 0 && index > 0) {
                             otpDigits.get(index - 1).requestFocus()
                         }*/
                    }

                    override fun afterTextChanged(s: Editable) {}
                })
            }


            startCountDownTimer(context, textTimeResend, rlResendLine, textResend)

           // countDownTimer!!.cancel()
            countDownTimer!!.start()

            textTimeResend.text = "${"00"}:${"60"} sec"

            if (textTimeResend.text.toString() == "${"00"}:${"00"} sec") {
                resendEnabled = true
                textResend.setTextColor(
                    ContextCompat.getColor(context, R.color.scroll_bar_color)
                )
            } else {
                textResend.setTextColor(ContextCompat.getColor(context, R.color.grey))
            }

            textSubmitButton.setOnClickListener {
                //   toggleLoginButtonEnabled(false, textSubmitButton)
              /*  if (textTimeResend.text.toString() == "${"00"}:${"00"} sec") {
                    resendEnabled = true
                    textResend.setTextColor(
                        ContextCompat.getColor(context, R.color.scroll_bar_color))*/

                    if (text == "Your password has been changed\n successfully.") {
                        if (NetworkMonitorCheck._isConnected.value) {

                            lifecycleScope.launch(Dispatchers.Main) {
                                if (findViewById<EditText>(R.id.otp_digit1).text.toString()
                                        .isEmpty() &&
                                    findViewById<EditText>(R.id.otp_digit2).text.toString()
                                        .isEmpty() &&
                                    findViewById<EditText>(R.id.otp_digit3).text.toString()
                                        .isEmpty() &&
                                    findViewById<EditText>(R.id.otp_digit4).text.toString()
                                        .isEmpty()
                                ) {
                                    showErrorDialog(requireContext(), AppConstant.otp)
                                    toggleLoginButtonEnabled(true, textSubmitButton)
                                } else {
                                    val otp =
                                        findViewById<EditText>(R.id.otp_digit1).text.toString() +
                                                findViewById<EditText>(R.id.otp_digit2).text.toString() +
                                                findViewById<EditText>(R.id.otp_digit3).text.toString() +
                                                findViewById<EditText>(R.id.otp_digit4).text.toString()
                                    if (otp.length == 4) {
                                        otpVerifyForgotPassword(
                                            userId,
                                            otp,
                                            dialog,
                                            textSubmitButton,
                                            text
                                        )
                                    } else {
                                        showErrorDialog(
                                            requireContext(),
                                            "Please enter the complete OTP."
                                        )
                                    }

                                }
                            }
                        } else {
                            showErrorDialog(
                                requireContext(),
                                resources.getString(R.string.no_internet_dialog_msg)
                            )
                            toggleLoginButtonEnabled(true, textSubmitButton)
                        }
                    }
                    else if (text.equals("Login Successful")) {
                        if (NetworkMonitorCheck._isConnected.value) {
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (findViewById<EditText>(R.id.otp_digit1).text.toString()
                                        .isEmpty() &&
                                    findViewById<EditText>(R.id.otp_digit2).text.toString()
                                        .isEmpty() &&
                                    findViewById<EditText>(R.id.otp_digit3).text.toString()
                                        .isEmpty() && findViewById<EditText>(R.id.otp_digit4).text.toString()
                                        .isEmpty()
                                ) {
                                    showErrorDialog(requireContext(), AppConstant.otp)
                                    toggleLoginButtonEnabled(true, textSubmitButton)
                                } else {
                                    val otp =
                                        findViewById<EditText>(R.id.otp_digit1).text.toString() +
                                                findViewById<EditText>(R.id.otp_digit2).text.toString() +
                                                findViewById<EditText>(R.id.otp_digit3).text.toString() +
                                                findViewById<EditText>(R.id.otp_digit4).text.toString()
                                    if (otp.length == 4) {
                                        otpVerifyLoginPhone(
                                            userId,
                                            otp,
                                            dialog,
                                            textSubmitButton,
                                            checkBox,
                                            number
                                        )
                                    } else {
                                        showErrorDialog(
                                            requireContext(),
                                            "Please enter the complete OTP."
                                        )
                                    }
                                }
                            }
                        } else {
                            showErrorDialog(
                                requireContext(),
                                resources.getString(R.string.no_internet_dialog_msg)
                            )
                            toggleLoginButtonEnabled(true, textSubmitButton)
                        }


                    } else if (text.equals("Your account is registered \nsuccessfully")) {
                        if (NetworkMonitorCheck._isConnected.value) {
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (findViewById<EditText>(R.id.otp_digit1).text.toString()
                                        .isEmpty() &&
                                    findViewById<EditText>(R.id.otp_digit2).text.toString()
                                        .isEmpty() &&
                                    findViewById<EditText>(R.id.otp_digit3).text.toString()
                                        .isEmpty() &&
                                    findViewById<EditText>(R.id.otp_digit4).text.toString()
                                        .isEmpty()
                                ) {
                                    showErrorDialog(requireContext(), AppConstant.otp)
                                    toggleLoginButtonEnabled(true, textSubmitButton)
                                } else {
                                    val otp =
                                        findViewById<EditText>(R.id.otp_digit1).text.toString() +
                                                findViewById<EditText>(R.id.otp_digit2).text.toString() +
                                                findViewById<EditText>(R.id.otp_digit3).text.toString() +
                                                findViewById<EditText>(R.id.otp_digit4).text.toString()
                                    if (otpType.equals("RegisterPhone")) {
                                        otpVerifySignupPhone(
                                            userId,
                                            otp,
                                            dialog,
                                            textSubmitButton,
                                            checkBox,
                                            text,
                                            number,
                                            "mobile"
                                        )
                                    }
                                    if (otpType.equals("RegisterEmail")) {
                                        if (otp.length == 4) {
                                            otpVerifySignupEmail(
                                                userId, otp, dialog, textSubmitButton,
                                                checkBox,
                                                text,
                                                number,
                                                "email"
                                            )
                                        } else {
                                            showErrorDialog(
                                                requireContext(),
                                                "Please enter the complete OTP."
                                            )
                                        }

                                    }
                                }
                            }
                        } else {
                            showErrorDialog(
                                requireContext(),
                                resources.getString(R.string.no_internet_dialog_msg)
                            )
                            toggleLoginButtonEnabled(true, textSubmitButton)
                        }
                    }

                }
         //   }

            textResend.setOnClickListener {
                findViewById<EditText>(R.id.otp_digit1).text.clear()
                findViewById<EditText>(R.id.otp_digit2).text.clear()
                findViewById<EditText>(R.id.otp_digit3).text.clear()
                findViewById<EditText>(R.id.otp_digit4).text.clear()
                if (text.equals("Login Successful")) {
                    if (NetworkMonitorCheck._isConnected.value) {
                        if (resendEnabled) {
                            resendLoginMobile(
                                code, number, rlResendLine,
                                incorrectOtp, textTimeResend, textResend
                            )
                        }
                    } else {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    }


                } else if (text.equals("Your account is registered \nsuccessfully")) {
                    if (NetworkMonitorCheck._isConnected.value) {
                        if (otpType.equals("RegisterPhone")) {
                            if (resendEnabled) {
                                resendRegisterMobile(
                                    code, number, rlResendLine,
                                    incorrectOtp, textTimeResend, textResend
                                )
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
                    } else {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    }

                } else if (text == "Your password has been changed\n successfully.") {
                    if (NetworkMonitorCheck._isConnected.value) {
                        if (resendEnabled) {
                            resendForgotPassword(
                                number,
                                rlResendLine,
                                incorrectOtp, textTimeResend, textResend
                            )
                        }
                    } else {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
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
                        showErrorDialog(requireContext(), it.message!!)

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
                        showErrorDialog(requireContext(), it.message!!)

                    }

                    else -> {

                        Log.v("RegisterViewModel", "error::" + it.message)
                    }
                }
            }
        }
    }

    private fun otpVerifySignupEmail(
        tempId: String, otp: String, dialog: Dialog, text: TextView,
        checkBox: CheckBox?, dialogtext: String,
        number: String, type: String
    ) {
        lifecycleScope.launch {
            loggedScreenViewModel.otpVerifySignupEmail(
                tempId,
                otp,
                token
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            val session = SessionManager(requireActivity())
                            if (checkBox != null && checkBox.isChecked) {
                                session.setUserSession(true)
                            }else{
                                session.setUserSession(false)
                            }
                            session.setLoginType("emailAddress")
                            dialogSuccess(context, dialogtext, Gson().toJson(resp), number, type)
                        }
                        dialog.dismiss()
                        toggleLoginButtonEnabled(true, text)
                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(
                            requireContext(), it.message!!
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


    private fun otpVerifyForgotPassword(
        userId: String, otp: String, dialog: Dialog, text: TextView,
        dialogtext: String
    ) {
        lifecycleScope.launch {
            loggedScreenViewModel.otpVerifyForgotPassword(
                userId,
                otp,
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let {
                            dialogNewPassword(context, dialogtext, userId)
                        }
                        dialog.dismiss()
                        toggleLoginButtonEnabled(true, text)
                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(
                            requireContext(), it.message!!
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


    private fun otpVerifyLoginPhone(
        userId: String, otp: String, dialog: Dialog, text: TextView,
        checkBox: CheckBox?, number: String
    ) {
        lifecycleScope.launch {
            loggedScreenViewModel.otpVerifyLoginPhone(
                userId,
                otp,
                token
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            val session: SessionManager = SessionManager(requireActivity())
                            if (resp.has("is_profile_complete") &&
                                resp.get("is_profile_complete").asBoolean
                            ) {
                                if (resp.has("user_id")) {
                                    if (checkBox != null && checkBox.isChecked) {
                                        session.setUserSession(true)
                                    }else{
                                        session.setUserSession(false)
                                    }
                                    session.setUserId(resp.get("user_id").asInt)
                                    session.setAuthToken(resp.get("token").asString)
                                    session.setLoginType("mobileNumber")

                                    if (resp.get("user_image") != null){
                                        Log.d("imageCheck", resp.get("user_image").asString)
                                        session.setUserImage(resp.get("user_image").asString)
                                    }
                                    Log.d("checkLoginType","i get this mobileNumber"+session.getLoginType())
                                    session.setCurrentPanel(AppConstant.Guest)
                                    val intent = Intent(requireActivity(), GuesMain::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    startActivity(intent)
                                }
                            } else {
                                if (checkBox != null && checkBox.isChecked) {
                                    session.setUserSession(true)
                                }else{
                                    session.setUserSession(false)
                                }
                                session.setUserId(resp.get("user_id").asInt)
                                session.setAuthToken(resp.get("token").asString)
                                if (resp.has("full_name") && !resp.get("full_name").isJsonNull) {
                                    session.setName(resp.get("full_name").asString)
                                }
                                session.setLoginType("mobileNumber")
                                Log.d("checkLoginType","i get this mobileNumber"+session.getLoginType())
                                if (resp.get("user_image") != null){
                                    Log.d("imageCheck", resp.get("user_image").asString)
                                    session.setUserImage(resp.get("user_image").asString)
                                }
                                val bundle = Bundle()
                                bundle.putString("data", Gson().toJson(resp))
                                bundle.putString("type", "mobile")
                                bundle.putString("email", number)
                                findNavController().navigate(R.id.completeProfileFragment, bundle)
                            }
                        }
                        dialog.dismiss()
                        toggleLoginButtonEnabled(true, text)
                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(
                            requireContext(), it.message!!
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

    private fun forgotPassword(email: String, dialog: Dialog, text: TextView) {
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
                                "Please type the verification code send \nto " + email
                            dialogOtp(
                                requireContext(),
                                text,
                                textHeaderOfOtpVerfication, "", email,
                                userId, null, "ForgotPassword"
                            )
                        }
                        dialog.dismiss()
                        toggleLoginButtonEnabled(true, text)
                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(
                            requireContext(), it.message!!
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

    private fun otpVerifySignupPhone(
        tempId: String, otp: String, dialog: Dialog, text: TextView,
        checkBox: CheckBox?, dialogtext: String, number: String,
        type: String
    ) {
        lifecycleScope.launch {
            loggedScreenViewModel.otpVerifySignupPhone(
                tempId,
                otp,
                token
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            val session = SessionManager(requireActivity())
                            if (checkBox != null && checkBox.isChecked) {
                                session.setUserSession(true)
                            }else{
                                session.setUserSession(false)
                            }
                            session.setLoginType("mobileNumber")
                            dialogSuccess(
                                context, dialogtext, Gson().toJson(resp),
                                number, type
                            )
                        }
                        dialog.dismiss()
                        toggleLoginButtonEnabled(true, text)
                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(
                            requireContext(), it.message!!
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
                number,
                fcmToken = token
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
                        showErrorDialog(
                            requireContext(), it.message!!
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
        code: String, number: String, rlResendLine: RelativeLayout, incorrectOtp: TextView,
        textTimeResend: TextView, textResend: TextView
    ) {
        Log.d(TAG, "Inside of fragment")
        lifecycleScope.launch {
            loggedScreenViewModel.signupPhoneNumber(code, number).collect {
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
                        showErrorDialog(requireContext(), it.message!!)
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
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val f = android.icu.text.DecimalFormat("00")
                val min = (millisUntilFinished / 60000) % 60
                val sec = (millisUntilFinished / 1000) % 60
                textResend.isEnabled = false
                textTimeResend.text = "${f.format(min)}:${f.format(sec)} sec"
            }

            override fun onFinish() {
                textTimeResend.text = "00:00"
                rlResendLine.visibility = View.GONE
                textResend.isEnabled = true
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


    private fun dialogNewPassword(
        context: Context?,
        text: String,
        userId: String
    ) {
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

            val imgCorrectSignPassword = findViewById<ImageView>(R.id.imgCorrectSign)
            val imgWrongSignPassword = findViewById<ImageView>(R.id.imgWrongSign)
            val imgCorrectSignConfirm = findViewById<ImageView>(R.id.imgCorrectSign1)
            val imgWrongSignConfirm = findViewById<ImageView>(R.id.imgWrongSign1)

            val textSubmitButton = findViewById<TextView>(R.id.textSubmitButton)

            fun isValidPassword(password: String): Boolean {
                val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,}$")
                return password.matches(regex)
            }

            fun validateInputs() {
                val password = etPassword.text.toString()
                val confirmPassword = etConfirmPassword.text.toString()

                // Password rule check
                if (password.isEmpty()) {
                    imgCorrectSignPassword.visibility = View.GONE
                    imgWrongSignPassword.visibility = View.GONE
                } else if (isValidPassword(password)) {
                    imgCorrectSignPassword.visibility = View.VISIBLE
                    imgWrongSignPassword.visibility = View.GONE
                } else {
                    imgCorrectSignPassword.visibility = View.GONE
                    imgWrongSignPassword.visibility = View.VISIBLE
                }

                // Confirm password match check
                if (confirmPassword.isEmpty()) {
                    imgCorrectSignConfirm.visibility = View.GONE
                    imgWrongSignConfirm.visibility = View.GONE
                } else if (password == confirmPassword) {
                    imgCorrectSignConfirm.visibility = View.VISIBLE
                    imgWrongSignConfirm.visibility = View.GONE
                } else {
                    imgCorrectSignConfirm.visibility = View.GONE
                    imgWrongSignConfirm.visibility = View.VISIBLE
                }
            }

            etPassword.addTextChangedListener { validateInputs() }
            etConfirmPassword.addTextChangedListener { validateInputs() }

            etConfirmPassword.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    textSubmitButton.performClick()
                    true
                } else false
            }

            textSubmitButton.setOnClickListener {
                toggleLoginButtonEnabled(false, textSubmitButton)

                val password = etPassword.text.toString()
                val confirmPassword = etConfirmPassword.text.toString()

                if (!NetworkMonitorCheck._isConnected.value) {
                    ErrorDialog.showErrorDialog(requireContext(),"No internet connection")
                    toggleLoginButtonEnabled(true, textSubmitButton)
                    return@setOnClickListener
                }

                lifecycleScope.launch(Dispatchers.Main) {
                    var isValid = true

                    if (password.isEmpty()) {
                        etPassword.error = "Password is required"
                        etPassword.requestFocus()
                        isValid = false
                    } else if (!isValidPassword(password)) {
                        etPassword.error =
                            "Password must be 8+ characters with uppercase, lowercase, number, and symbol"
                        etPassword.requestFocus()
                        isValid = false
                    }

                    if (confirmPassword.isEmpty()) {
                        etConfirmPassword.error = "Confirm password is required"
                        etConfirmPassword.requestFocus()
                        isValid = false
                    } else if (password != confirmPassword) {
                        etConfirmPassword.error = "Passwords do not match"
                        etConfirmPassword.requestFocus()
                        isValid = false
                    }

                    if (isValid) {
                        resetPassword(userId, password, confirmPassword, dialog, textSubmitButton, text)
                    } else {
                        toggleLoginButtonEnabled(true, textSubmitButton)
                    }
                }
            }

            imageCross.setOnClickListener {
                dialog.dismiss()
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }



    private fun checkPasswordValidity(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#\$%^&*()-+=])[a-zA-Z0-9!@#\$%^&*()-+=]{8,}$"
        val pattern = Pattern.compile(passwordPattern)
        val isPasswordValid = pattern.matcher(password).matches()

        if (!isPasswordValid) {
            //showErrorDialog(ErrorMessages.passwordMustConsist)
            showErrorDialog(
                requireContext(), passwordMustConsist
            )
           // binding.etPassword.requestFocus()
        }

        return isPasswordValid
    }

    private fun checkPasswordValidity1(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#\$%^&*()-+=])[a-zA-Z0-9!@#\$%^&*()-+=]{8,}$"
        val pattern = Pattern.compile(passwordPattern)
        val isPasswordValid = pattern.matcher(password).matches()

        if (!isPasswordValid) {
            //showErrorDialog(ErrorMessages.passwordMustConsist)
//            showErrorDialog(
//                requireContext(), passwordMustConsist
//            )
            return false
            // binding.etPassword.requestFocus()
        }

        return isPasswordValid
    }


    private fun resetPassword(
        userId: String,
        password: String,
        passwordConfirmation: String,
        dialog: Dialog,
        text: TextView,
        dialogtext: String
    ) {
        lifecycleScope.launch {
            loggedScreenViewModel.resetPassword(
                userId,
                password,
                passwordConfirmation,
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let {
                            dialogSuccess(context, dialogtext, "", "", "")
                            dialog.dismiss()
                        }
                        dialog.dismiss()
                        toggleLoginButtonEnabled(true, text)
                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(
                            requireContext(), it.message!!
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

    private fun dialogSuccess(
        context: Context?, text: String, data: String, number: String,
        type: String
    ) {
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
                    bundle.putString("data", data)
                    bundle.putString("type", type)
                    bundle.putString("email", number)
                    navController.navigate(R.id.turnNotificationsFragment, bundle)

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
                //text.isEnabled = true
            } catch (e: Exception) {
                Log.e(TAG, "exception toggleLoginButtonEnabled ${e.message}")
            }
        }
    }

    private fun getCurrentLocation() {
        // Initialize Location manager
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Check condition
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        ) {
            // When location service is enabled
            // Get last location
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mFusedLocationClient.lastLocation.addOnCompleteListener { task ->
                // Initialize location
                val location = task.result
                // Check condition
                if (location != null) {
                    latitude = location.latitude.toString()
                    longitude = location.longitude.toString()
                    loadHomeApi()
                } else {
                    val locationRequest =
                        LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(10000)
                            .setFastestInterval(1000)
                            .setNumUpdates(1)

                    val locationCallback: LocationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            // Initialize
                            val location1 = locationResult.lastLocation
                            if (location1 != null) {
                                latitude = location1.latitude.toString()
                                longitude = location1.longitude.toString()
                                loadHomeApi()
                            }
                        }
                    }
                    // Request location updates
                    mFusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.myLooper()!!
                    )

                }
            }
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 100
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check condition
        if (requestCode == 100) {
            if (requestCode == 100 && grantResults.isNotEmpty() && (grantResults[0] + grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
               // displayLocationSettingsRequest(requireActivity())
                showCustomLocationDialog()
            } else {
                alertBoxLocation1()
            }
        }

        if (requestCode == 1000) {
            if (requestCode == 1000 && grantResults.isNotEmpty() && (grantResults[0] + grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
             //   displayLocationSettingsRequest11(requireActivity())
                showCustomLocationDialog()
            } else {
              //  displayLocationSettingsRequest11(requireActivity())
                showCustomLocationDialog()
            }
        }


    }
    private fun showCustomLocationDialog() {
        val dialog = Dialog(requireActivity(), R.style.BottomSheetDialog)
        dialog.setContentView(R.layout.dialog_location_permission)

        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val btnTurnOnLocation = dialog.findViewById<TextView>(R.id.btnLocation)
        val btnCancel = dialog.findViewById<TextView>(R.id.textNotnow)

        btnTurnOnLocation.setOnClickListener {
            // Location settings open करें
            openLocationSettings()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
            // Optional: Cancel पर कुछ action
            Toast.makeText(requireActivity(), "Location is required for better experience", Toast.LENGTH_SHORT).show()
        }

        dialog.setCancelable(false)
        dialog.show()
    }

    private fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivityForResult(intent, LOCATION_SETTINGS_REQUEST_CODE)
    }
    private fun displayLocationSettingsRequest(context: Context) {
        val googleApiClient = GoogleApiClient.Builder(context)
            .addApi(LocationServices.API).build()
        googleApiClient.connect()
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 1000
        locationRequest.numUpdates = 1
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result: PendingResult<LocationSettingsResult> =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { result ->
            val status: Status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> {
                    Log.i(TAG, "All location settings are satisfied.")
                    getCurrentLocation()
                }

                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    Log.i(
                        TAG,
                        "Location settings are not satisfied. Show the user a dialog to upgrade location settings "
                    )
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.resolution?.let {
                            startIntentSenderForResult(
                                it.intentSender,
                                100,
                                null,
                                0,
                                0,
                                0,
                                null
                            )
                        }

                    } catch (e: SendIntentException) {
                        Log.i(TAG, "PendingIntent unable to execute request.")
                    }
                }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.i(
                    TAG,
                    "Location settings are inadequate, and cannot be fixed here. Dialog not created."
                )

            }
        }
    }

    private fun displayLocationSettingsRequest11(context: Context) {
        val googleApiClient = GoogleApiClient.Builder(context)
            .addApi(LocationServices.API).build()
        googleApiClient.connect()
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 1000
        locationRequest.numUpdates = 1
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result: PendingResult<LocationSettingsResult> =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { result ->
            val status: Status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> {
                    Log.i(TAG, "All location settings are satisfied.")
                    getCurrentLocation()
                }

                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    Log.i(
                        TAG,
                        "Location settings are not satisfied. Show the user a dialog to upgrade location settings "
                    )
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.resolution?.let {
                            startIntentSenderForResult(
                                it.intentSender,
                                1000,
                                null,
                                0,
                                0,
                                0,
                                null
                            )
                        }

                    } catch (e: SendIntentException) {
                        Log.i(TAG, "PendingIntent unable to execute request.")
                    }
                }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.i(
                    TAG,
                    "Location settings are inadequate, and cannot be fixed here. Dialog not created."
                )

            }
        }
    }

    private fun alertBoxLocation() {
        val builder = AlertDialog.Builder(requireContext())
        //set title for alert dialog
        builder.setTitle("Alert")
        //set message for alert dialog
        builder.setMessage(R.string.dialogMessage)
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("Yes") { _, _ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivityForResult(intent, 200)
        }
        //performing cancel action
        builder.setNeutralButton("Cancel") { _, _ ->

        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()

    }

    private fun alertBoxLocation1() {
//        val dialogView = layoutInflater.inflate(R.layout.dialog_location_permission, null)
//
//        val builder = AlertDialog.Builder(requireContext())
//        builder.setView(dialogView)
//
//        val alertDialog = builder.create()
//        alertDialog.setCancelable(false)
//
//        val btnAllow = dialogView.findViewById<TextView>(R.id.btnLocation)
//        val btnCancel = dialogView.findViewById<TextView>(R.id.textNotnow)
//
//        btnAllow.setOnClickListener {
//            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//            val uri = Uri.fromParts("package", requireContext().packageName, null)
//            intent.data = uri
//            startActivityForResult(intent, 200)
//            alertDialog.dismiss()
//        }
//
//        btnCancel.setOnClickListener {
//            alertDialog.dismiss()
//        }
//
//        alertDialog.show()
        val dialog = Dialog(requireActivity(), R.style.BottomSheetDialog)
        dialog.setContentView(R.layout.dialog_location_permission)

        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val btnTurnOnLocation = dialog.findViewById<TextView>(R.id.btnLocation)
        val btnCancel = dialog.findViewById<TextView>(R.id.textNotnow)

        btnTurnOnLocation.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivityForResult(intent, 200)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

     //  dialog.setCancelable(false)
        dialog.show()
    }
    private fun loadHomeApi() {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                loggedScreenViewModel.getHomeData(
                    "",
                    latitude, longitude
                ).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                val listType = object : TypeToken<List<HomePropertyData>>() {}.type
                                val properties: MutableList<HomePropertyData> =
                                    Gson().fromJson(resp, listType)
                                homePropertyData = properties
                                if (homePropertyData.isNotEmpty()) {
                                    adapter.updateData(homePropertyData)
                                }
                            }
                        }

                        is NetworkResult.Error -> {

                            showErrorDialog(requireContext(), "We didn’t find any properties close to your location.")

                        }

                        else -> {
                            Log.v(TAG, "error::" + it.message)
                        }
                    }
                }
            }
        } else {
            showErrorDialog(
                requireContext(),
                resources.getString(R.string.no_internet_dialog_msg)
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (Activity.RESULT_OK == resultCode) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Please turn on location", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        if (requestCode == 200) {
            getCurrentLocation()
        }
    }

    fun isValidEmailOrPhone(input: String): Boolean {
        val pattern = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$|^\+?[0-9]{10,15}$"""
        val regex = Pattern.compile(pattern)
        val matcher = regex.matcher(input)
        return matcher.matches()
    }

    fun isValidPassword(password: String): Boolean {
        // val pattern = """^(?=.*[a-zA-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$"""
        val pattern = """^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$"""
        val regex = Pattern.compile(pattern)
        val matcher = regex.matcher(password)
        return matcher.matches()
    }

    fun isPhoneNumber(input: String): Boolean {
        val phonePattern = """^\+?[0-9]{10,15}$"""
        val regex = Pattern.compile(phonePattern)
        return regex.matcher(input).matches()
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        val pattern = Pattern.compile(emailPattern)
        return pattern.matcher(email).matches()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null

        countDownTimer?.cancel()
        locationManager = null
        session?.setFilterRequest("")
        session?.setSearchFilterRequest("")

    }

}




