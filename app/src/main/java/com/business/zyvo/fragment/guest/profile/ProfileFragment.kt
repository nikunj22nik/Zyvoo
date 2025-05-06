package com.business.zyvo.fragment.guest.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.business.zyvo.AppConstant
import com.business.zyvo.BuildConfig
import com.business.zyvo.DateManager.DateManager
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.LoadingUtils.Companion.showSuccessDialog
import com.business.zyvo.MyApp
import com.business.zyvo.NetworkResult
import com.business.zyvo.OnClickListener1
import com.business.zyvo.OnLocalListener
import com.business.zyvo.R
import com.business.zyvo.activity.AuthActivity
import com.business.zyvo.activity.GuesMain
import com.business.zyvo.activity.HostMainActivity
import com.business.zyvo.activity.guest.checkout.model.MailingAddress
import com.business.zyvo.activity.guest.checkout.model.UserCards
import com.business.zyvo.adapter.AdapterAddPaymentCard
import com.business.zyvo.adapter.AddHobbiesAdapter
import com.business.zyvo.adapter.AddLanguageSpeakAdapter
import com.business.zyvo.adapter.AddLocationAdapter
import com.business.zyvo.adapter.AddPetsAdapter
import com.business.zyvo.adapter.AddWorkAdapter
import com.business.zyvo.adapter.SetPreferred
import com.business.zyvo.adapter.selectLanguage.LocaleAdapter
import com.business.zyvo.databinding.FragmentProfileBinding
import com.business.zyvo.fragment.both.completeProfile.HasName
import com.business.zyvo.fragment.guest.profile.model.UserProfile
import com.business.zyvo.fragment.guest.profile.viewModel.ProfileViewModel
import com.business.zyvo.model.AddHobbiesModel
import com.business.zyvo.model.AddLanguageModel
import com.business.zyvo.model.AddLocationModel
import com.business.zyvo.model.AddPetsModel
import com.business.zyvo.model.AddWorkModel
import com.business.zyvo.onItemClickData
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.CommonAuthWorkUtils
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.ErrorDialog.getLocationDetails
import com.business.zyvo.utils.ErrorDialog.isValidEmail
import com.business.zyvo.utils.ErrorDialog.showToast
import com.business.zyvo.utils.MediaUtils
import com.business.zyvo.utils.NetworkMonitorCheck
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hbb20.CountryCodePicker
import com.stripe.android.ApiResultCallback
import com.stripe.android.Stripe
import com.stripe.android.model.Address
import com.stripe.android.model.CardParams
import com.stripe.android.model.Token
import com.withpersona.sdk2.inquiry.Environment
import com.withpersona.sdk2.inquiry.Fields
import com.withpersona.sdk2.inquiry.Inquiry
import com.withpersona.sdk2.inquiry.InquiryResponse
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.Objects

@AndroidEntryPoint
class ProfileFragment : Fragment(), OnClickListener1, onItemClickData, OnClickListener,
    SetPreferred {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var commonAuthWorkUtils: CommonAuthWorkUtils
    private lateinit var addLocationAdapter: AddLocationAdapter
    private lateinit var addWorkAdapter: AddWorkAdapter
    private lateinit var addLanguageSpeakAdapter: AddLanguageSpeakAdapter
    private lateinit var addHobbiesAdapter: AddHobbiesAdapter
    private lateinit var addPetsAdapter: AddPetsAdapter
    private lateinit var dateManager: DateManager
    private lateinit var userId: String
    private lateinit var addPaymentCardAdapter: AdapterAddPaymentCard
    private val profileViewModel: ProfileViewModel by lazy {
        ViewModelProvider(this)[ProfileViewModel::class.java]
    }
    private var petsList: MutableList<AddPetsModel> = mutableListOf()
    private var hobbiesList: MutableList<AddHobbiesModel> = mutableListOf()
    private var locationList: MutableList<AddLocationModel> = mutableListOf()
    private var workList: MutableList<AddWorkModel> = mutableListOf()
    private var languageList: MutableList<AddLanguageModel> = mutableListOf()
    var userCardsList: MutableList<UserCards> = mutableListOf()
    private lateinit var apiKey: String
    private lateinit var localeAdapter: LocaleAdapter
    private var locales: List<Locale> = listOf()
    private var etSearch: TextView? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var imageStatus = ""
    var customerId = ""
    private var isDropdownOpen = false
    var selectuserCard: UserCards? = null
    lateinit var navController: NavController
    private lateinit var otpDigits: Array<EditText>
    private var countDownTimer: CountDownTimer? = null
    var resendEnabled = false
    var session: SessionManager? = null
    var imageBytes: ByteArray = byteArrayOf()
    var userProfile: UserProfile? = null
    var isPaymentDataLoaded = false
    private lateinit var getInquiryResult: ActivityResultLauncher<Inquiry>
    var firstName: String = ""
    var lastName: String = ""

    // For handling the result of the Autocomplete Activity
    private val startAutocomplete =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { intent ->
                    val place = Autocomplete.getPlaceFromIntent(intent)
                    val placeName = place.name ?: AppConstant.unknownLocation

                    Log.d("@@@@@", "Location: $placeName")

                    val newLocation = AddLocationModel(placeName)
                    addLivePlace(place_name = placeName)

                    // Update the list and notify adapter in one step
                    locationList.add(locationList.size - 1, newLocation)
                    // addLocationAdapter.notifyItemInserted(0)
                    addLocationAdapter.updateLocations(locationList)

                    Log.i(ErrorDialog.TAG, "Place: $placeName, ${place.id}")
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                Log.i(ErrorDialog.TAG, "User canceled autocomplete")
            }
        }


    // For handling the result of the Autocomplete Activity
    private val startStreertAutocomplete =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                result.data?.let { intent ->
                    val place = Autocomplete.getPlaceFromIntent(intent)
                    val latLng = place.latLng
                    getLocationDetails(requireContext(), latLng) { locationDetails ->
                        // Use city, state, zipCode here
                        locationDetails?.let {
                            Log.d(
                                ErrorDialog.TAG,
                                "City: ${it.city}, State: ${it.state}, Zip: ${it.zipCode}"
                            )
                            if (!it.city.isNullOrEmpty() &&
                                !it.state.isNullOrEmpty() &&
                                !it.zipCode.isNullOrEmpty()
                            ) {
                                binding.streetEditText.setText(place.name ?: "")
                                binding.cityET.setText(it.city)
                                binding.stateEt.setText(it.state)
                                binding.zipEt.setText(it.zipCode)
                                binding.streetEditText.isEnabled = false
                                binding.imageEditStreetAddress.visibility = View.VISIBLE
                                binding.imageStreetCheckedButton.visibility = GONE
                                updateAddStreetAddress(place.name ?: "")
                                updateStateAddress(AppConstant.profileType)
                                updateZipCode(it.zipCode, AppConstant.profileType)
                                updateCityAddress(it.city, AppConstant.profileType)
                            }
                        }
                    }
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                Log.i(ErrorDialog.TAG, "User canceled autocomplete")
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navController = findNavController()
        commonAuthWorkUtils = CommonAuthWorkUtils(requireContext(), navController)
        apiKey = getString(R.string.api_key)

        getInquiryResult = registerForActivityResult(Inquiry.Contract()) { result ->
            when (result) {
                is InquiryResponse.Complete -> {
                    // User identity verification completed successfully
                    verifyIdentityApi()
                }

                is InquiryResponse.Cancel -> {
                    // User abandoned the verification process
                    binding.textConfirmNow2.visibility = View.VISIBLE
                    binding.textVerified2.visibility = GONE
                    Toast.makeText(requireContext(), "Request Cancelled", Toast.LENGTH_LONG).show()
                }

                is InquiryResponse.Error -> {
                    // Error occurred during identity verification
                    binding.textConfirmNow2.visibility = View.VISIBLE
                    binding.textVerified2.visibility = GONE
                    Toast.makeText(requireContext(), "Error Occurred, Try Again", Toast.LENGTH_LONG)
                        .show()
                    Log.d("personaError", result.errorCode.toString())
                    Log.d("personaError", result.cause.toString())
                    Log.d("personaError", result.debugMessage.toString())
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dateManager = DateManager(requireContext())


        // Inflate the layout for this fragment
        _binding =
            FragmentProfileBinding.inflate(LayoutInflater.from(requireContext()), container, false)

        // val newLocation = AddLocationModel(AppConstant.unknownLocation)

        //  val newLocation = AddLocationModel(AppConstant.unknownLocation)


        binding.switchHost.setOnClickListener {
            val app = activity?.application as MyApp
            val session = SessionManager(requireContext())
            session.setCurrentPanel(AppConstant.Host)
            session.setChatToken("")
            app.clearInstance()
            val intent = Intent(requireContext(), HostMainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        // locationList.add(newLocation)
//        val newWork = AddWorkModel(AppConstant.unknownLocation)
//        workList.add(newWork)
//        val newLanguage = AddLanguageModel(AppConstant.unknownLocation)
//        languageList.add(newLanguage)
//        val newHobbies = AddHobbiesModel(AppConstant.unknownLocation)
//
//        hobbiesList.add(newHobbies)
//        val newPets = AddPetsModel(AppConstant.unknownLocation)
//
//        petsList.add(newPets)

//        locationList.add(newLocation)
//       val newWork = AddWorkModel(AppConstant.unknownLocation)
//        workList.add(newWork)
//        val newLanguage = AddLanguageModel(AppConstant.unknownLocation)
//        languageList.add(newLanguage)
//        val newHobbies = AddHobbiesModel(AppConstant.unknownLocation)
//
//        hobbiesList.add(newHobbies)
//        val newPets = AddPetsModel(AppConstant.unknownLocation)
//
//        petsList.add(newPets)


        addPaymentCardAdapter = AdapterAddPaymentCard(requireContext(), mutableListOf(), this)
        binding.recyclerViewPaymentCardList.adapter = addPaymentCardAdapter

        session = SessionManager(requireActivity())
        Log.d("CheckUserId", session?.getUserId().toString())
        userId = session?.getUserId().toString()


        // Observe the isLoading state
        lifecycleScope.launch {
            profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    LoadingUtils.showDialog(requireContext(), false)
                } else {
                    LoadingUtils.hideDialog()
                }
            }
        }
        initView()
        getUserProfile()

        return binding.root
    }

    private fun initView() {
        session?.getLoginType()?.let { Log.d("checkLoginType", it) }
        if (session?.getLoginType().equals("mobileNumber")) {
            Log.d("checkLoginType","1")
            binding.textPasswordTitle.visibility = View.GONE
            binding.rlPasswordTitle.visibility = View.GONE
            binding.viewPassword.visibility = View.GONE
        }else{
            Log.d("checkLoginType","2")
            binding.textPasswordTitle.visibility = View.VISIBLE
            binding.rlPasswordTitle.visibility = View.VISIBLE
            binding.viewPassword.visibility = View.VISIBLE
        }
        binding.apply {
            imageEditAbout.setOnClickListener {
                etAboutMe.isEnabled = true
                imageEditAbout.visibility = GONE
                imageAboutCheckedButton.visibility = View.VISIBLE
            }

            imageAboutCheckedButton.setOnClickListener {
                if (etAboutMe.text.isEmpty()) {
                    showErrorDialog(requireContext(), AppConstant.aboutMe)
                } else {
                    updateAddAboutMe(etAboutMe.text.toString())
                }
            }

            streetEditText.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    startStreetLocationPicker()
                }
            }
            streetEditText.setOnClickListener {
                startStreetLocationPicker()
            }

            imageEditStreetAddress.setOnClickListener {
                streetEditText.isEnabled = true
                imageEditStreetAddress.visibility = GONE
                imageStreetCheckedButton.visibility = View.VISIBLE
            }
            imageStreetCheckedButton.setOnClickListener {
                if (streetEditText.text.isEmpty()) {
                    showErrorDialog(requireContext(), "Street Cannot be Empty")
                } else {
                    updateAddStreetAddress(streetEditText.text.toString())
                }
                streetEditText.isEnabled = false
                imageEditStreetAddress.visibility = View.VISIBLE
                imageStreetCheckedButton.visibility = GONE
            }

            imageEditCityAddress.setOnClickListener {
                cityET.isEnabled = true
                imageEditCityAddress.visibility = GONE
                CityCheckedButton.visibility = View.VISIBLE
            }
            CityCheckedButton.setOnClickListener {
                if (cityET.text.isEmpty()) {
                    showErrorDialog(requireContext(), "City Cannot be Empty")
                } else {
                    updateCityAddress(cityET.text.toString(), "")
                }
                cityET.isEnabled = false
                imageEditCityAddress.visibility = View.VISIBLE
                CityCheckedButton.visibility = GONE
            }

            imageEditStateAddress.setOnClickListener {
                stateEt.isEnabled = true
                imageEditStateAddress.visibility = GONE
                stateCheckedButton.visibility = View.VISIBLE
            }
            stateCheckedButton.setOnClickListener {
                if (stateEt.text.isEmpty()) {
                    showErrorDialog(requireContext(), "State Cannot be Empty")
                } else {
                    updateStateAddress("")
                }
                stateEt.isEnabled = false
                imageEditStateAddress.visibility = View.VISIBLE
                stateCheckedButton.visibility = GONE
            }

            imageEditZipAddress.setOnClickListener {
                zipEt.isEnabled = true
                imageEditZipAddress.visibility = GONE
                zipCodeCheckedButton.visibility = View.VISIBLE
            }
            zipCodeCheckedButton.setOnClickListener {
                if (zipEt.text.isEmpty()) {
                    showErrorDialog(requireContext(), "Zip Cannot be Empty")
                } else {
                    updateZipCode(zipEt.text.toString(), "")
                }
                zipEt.isEnabled = false
                imageEditZipAddress.visibility = View.VISIBLE
                zipCodeCheckedButton.visibility = GONE

            }

            textConfirmNow2.setOnClickListener {
                if (NetworkMonitorCheck._isConnected.value) {
                    launchVerifyIdentity()
                } else {
                    showErrorDialog(
                        requireContext(),
                        resources.getString(R.string.no_internet_dialog_msg)
                    )
                }
            }


        }

    }

    // Function to start the location picker using Autocomplete
    private fun startStreetLocationPicker() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .build(requireContext())
        startStreertAutocomplete.launch(intent)
    }

    private fun launchVerifyIdentity() {
        val TEMPLATE_ID = BuildConfig.templateID

        val inquiry = Inquiry.fromTemplate(TEMPLATE_ID)
            .environment(Environment.SANDBOX) // Use Environment.PRODUCTION for live verification
            .referenceId(session?.getUserId().toString()) // Link the inquiry to a specific user
            .fields(
                Fields.Builder()
                    .build()
            )
            .locale(Locale.getDefault().language) // Set the locale for the verification process
            .build()

        getInquiryResult.launch(inquiry)

    }

    private fun getUserProfile() {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                profileViewModel.getUserProfile(session?.getUserId().toString()).collect { it ->
                    when (it) {
                        is NetworkResult.Success -> {
                            var name = ""
                            it.data?.let { resp ->
                                Log.d("TESTING_PROFILE", "HERE IN A USER PROFILE ,$resp")
                                userProfile = Gson().fromJson(resp, UserProfile::class.java)
                                userProfile.let {

                                    it?.first_name?.let {
                                        name += it + " "
                                        firstName = it
                                    }
                                    it?.last_name?.let {
                                        name += it
                                        lastName = it
                                    }

                                    it?.name = name
                                    binding.user = it

                                    if (it?.profile_image != null) {
                                        Glide.with(requireContext())
                                            .asBitmap() // Convert the image into Bitmap
                                            .load(BuildConfig.MEDIA_URL + it.profile_image) // User profile image URL
                                            .into(object : SimpleTarget<Bitmap>() {
                                                override fun onResourceReady(
                                                    resource: Bitmap,
                                                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                                                ) {
                                                    // The 'resource' is the Bitmap
                                                    // Now you can use the Bitmap (e.g., set it to an ImageView, or process it)
                                                    binding.imageProfilePicture.setImageBitmap(
                                                        resource
                                                    )
                                                    imageBytes =
                                                        MediaUtils.bitmapToByteArray(resource)
                                                    Log.d(ErrorDialog.TAG, imageBytes.toString())
                                                }
                                            })
                                    }
                                    if (it?.email_verified != null && it.email_verified == 1) {
                                        binding.textConfirmNow.visibility = GONE
                                        binding.textVerified.visibility =
                                            View.VISIBLE
                                    }
                                    if (it?.phone_verified != null && it.phone_verified == 1) {
                                        binding.textConfirmNow1.visibility = GONE
                                        binding.textVerified1.visibility =
                                            View.VISIBLE
                                    }
                                    if (it?.identity_verified != null && it.identity_verified == 1) {
                                        binding.textConfirmNow2.visibility = GONE
                                        binding.textVerified2.visibility =
                                            View.VISIBLE
                                    }
                                    if (it?.where_live != null && it.where_live.isNotEmpty()) {
                                        locationList = getObjectsFromNames(it.where_live) { name ->
                                            AddLocationModel(name)  // Using the constructor of MyObject to create instances
                                        }
                                        Log.d("ProfileCheck", "getObjectsFromNames")
                                        val newLanguage =
                                            AddLocationModel(AppConstant.unknownLocation)
                                        locationList.add(newLanguage)


                                        locationList.forEach {
                                            Log.d("ProifleDataLoc", it.toString())
                                        }
                                        Log.d("ProifleDataLoc", locationList.size.toString())
                                        addLocationAdapter.updateLocations(locationList)
                                    }
                                    if (it?.my_work != null && it.my_work.isNotEmpty()) {
                                        workList = getObjectsFromNames(it.my_work) { name ->
                                            AddWorkModel(name)  // Using the constructor of MyObject to create instances
                                        }
                                        val newLanguage = AddWorkModel(AppConstant.unknownLocation)
                                        workList.add(newLanguage)
                                        addWorkAdapter.updateWork(workList)
                                    }
                                    if (it?.languages != null && it.languages.isNotEmpty()) {
                                        languageList = getObjectsFromNames(it.languages) { name ->
                                            AddLanguageModel(name)  // Using the constructor of MyObject to create instances
                                        }
                                        val newLanguage =
                                            AddLanguageModel(AppConstant.unknownLocation)
                                        languageList.add(newLanguage)
                                        addLanguageSpeakAdapter.updateLanguage(languageList)
                                    }
                                    if (it?.hobbies != null && it.hobbies.isNotEmpty()) {
                                        hobbiesList = getObjectsFromNames(it.hobbies) { name ->
                                            AddHobbiesModel(name)  // Using the constructor of MyObject to create instances
                                        }
                                        val newLanguage =
                                            AddHobbiesModel(AppConstant.unknownLocation)
                                        hobbiesList.add(newLanguage)
                                        addHobbiesAdapter.updateHobbies(hobbiesList)
                                    }
                                    if (it?.pets != null && it.pets.isNotEmpty()) {
                                        petsList = getObjectsFromNames(it.pets) { name ->
                                            AddPetsModel(name)  // Using the constructor of MyObject to create instances
                                        }
                                        val newLanguage = AddPetsModel(AppConstant.unknownLocation)
                                        petsList.add(newLanguage)
                                        addPetsAdapter.updatePets(petsList)
                                    }
                                }
                            }
                        }

                        is NetworkResult.Error -> {
                            showErrorDialog(requireContext(), it.message!!)
                        }

                        else -> {
                            Log.v(ErrorDialog.TAG, "error::" + it.message)
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
        paymentOpenCloseDropDown()
        binding.textGiveFeedback.setOnClickListener(this)
        binding.rlPasswordTitle.setOnClickListener(this)
        binding.textTermServices.setOnClickListener(this)
        binding.textPrivacyPolicy.setOnClickListener(this)
        binding.textFaq.setOnClickListener(this)
        binding.textLogout.setOnClickListener(this)
        binding.textNotifications.setOnClickListener(this)
        binding.textVisitHelpCenter.setOnClickListener(this)
        binding.textAddNewPaymentCard.setOnClickListener(this)
        binding.imageInfoIcon.setOnClickListener(this)
        binding.clHead.setOnClickListener(this)
        binding.imageEditPicture.setOnClickListener(this)
        binding.imageEditName.setOnClickListener(this)
        binding.textConfirmNow.setOnClickListener(this)
        binding.textConfirmNow1.setOnClickListener(this)
        binding.imageEditEmail.setOnClickListener(this)
        binding.imageEditPhoneNumber.setOnClickListener(this)


        adapterInitialize()

        // Initialize Places API if not already initialized
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity(), apiKey)
        }

        // Set listeners


//        binding.filterIcon.setOnClickListener {
//            startActivity(Intent(requireActivity(), FiltersActivity::class.java))
//        }
//
//        binding.rlFind.setOnClickListener {
//
//            startActivity(Intent(requireActivity(), WhereTimeActivity::class.java))
//        }


    }

    // Function to initialize the adapter for adding locations
    private fun adapterInitialize() {
        addLocationAdapter = AddLocationAdapter(requireContext(), locationList, this, this)
        binding.recyclerViewLocation.adapter = addLocationAdapter

        // Update the adapter with the initial location list (if any)
        addLocationAdapter.updateLocations(locationList)



        addWorkAdapter = AddWorkAdapter(requireContext(), workList, this, this)
        binding.recyclerViewWork.adapter = addWorkAdapter
        Log.d("checkWorkList", workList.toString())
        addWorkAdapter.updateWork(workList)

        addLanguageSpeakAdapter =
            AddLanguageSpeakAdapter(requireContext(), languageList, this, this)
        binding.recyclerViewlanguages.adapter = addLanguageSpeakAdapter

        addLanguageSpeakAdapter.updateLanguage(languageList)


        addHobbiesAdapter = AddHobbiesAdapter(requireContext(), hobbiesList, this, this)
        binding.recyclerViewHobbies.adapter = addHobbiesAdapter

        addHobbiesAdapter.updateHobbies(hobbiesList)


        addPetsAdapter = AddPetsAdapter(requireContext(), petsList, this, this)
        binding.recyclerViewPets.adapter = addPetsAdapter

        addPetsAdapter.updatePets(petsList)


    }

    // Function to start the location picker using Autocomplete
    private fun startLocationPicker() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .build(requireContext())
        startAutocomplete.launch(intent)
    }

    // Display a dialog to select a language
    @SuppressLint("ObsoleteSdkInt")
    private fun dialogSelectLanguage() {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_select_language)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }

            val imageCross = findViewById<ImageView>(R.id.imageCross)
            val recyclerViewLanguages = findViewById<RecyclerView>(R.id.recyclerViewLanguages)

            // Fetch all available locales
            locales = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Locale.getAvailableLocales().toList()
            } else {
                Locale.getAvailableLocales().toList() // Same function for older Android versions
            }

            // Filter locales where the region is not empty
            locales = locales.filter { locale ->
                val regionCode = locale.country
                regionCode.isNotEmpty() // Ensures that locales with no country/region are filtered out
            }

            // Set the adapter for RecyclerView
            localeAdapter = LocaleAdapter(locales, object : OnLocalListener {
                override fun onItemClick(local: String) {
                    val newLanguage = AddLanguageModel(local)
                    addLanguageApi(newLanguage.name)
                    // Add the new language to the list
                    Log.d("laguageListSize", languageList.size.toString())
                    languageList.add(languageList.size - 1, newLanguage)
                    addLanguageSpeakAdapter.updateLanguage(languageList)
                    //addLanguageSpeakAdapter.notifyItemInserted(0)

                    // Delay dismissing the dialog slightly to prevent UI issues
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog.dismiss()
                    }, 200) // 200ms delay ensures smooth UI transition
                }
            })

            recyclerViewLanguages?.adapter = localeAdapter

            imageCross?.setOnClickListener { dismiss() }

            window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
            show()
        }
    }


    private fun bottomSheetUploadImage() {

        bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.BottomSheetDialog1)
        bottomSheetDialog!!.setContentView(R.layout.bottom_sheet_upload_image)
        bottomSheetDialog!!.show()


        val textCamera = bottomSheetDialog?.findViewById<TextView>(R.id.textCamera)
        val textGallery = bottomSheetDialog?.findViewById<TextView>(R.id.textGallery)


        textCamera?.setOnClickListener {
            bottomSheetDialog!!.dismiss()
            profileImageCameraChooser()
        }
        textGallery?.setOnClickListener {
            bottomSheetDialog!!.dismiss()
            profileImageGalleryChooser()
        }

    }

    private fun paymentOpenCloseDropDown() {
        // Set initial drawable
        binding.textPaymentMethod.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.ic_dropdown_close,
            0
        )

        binding.textPaymentMethod.setOnClickListener {
            // Toggle the state
            isDropdownOpen = !isDropdownOpen
            // Change the drawable based on the state
            val drawableRes = if (isDropdownOpen) {
                R.drawable.ic_dropdown_open
            } else {
                R.drawable.ic_dropdown_close
            }
            if (isDropdownOpen) {
                binding.recyclerViewPaymentCardList.visibility = View.VISIBLE
                binding.textAddNewPaymentCard.visibility = View.VISIBLE
                // API Call only if not already loaded
                if (!isPaymentDataLoaded) {
                    getUserCards()
                    isPaymentDataLoaded = true
                }
            } else {
                binding.recyclerViewPaymentCardList.visibility = GONE
                binding.textAddNewPaymentCard.visibility = GONE
            }
            binding.textPaymentMethod.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)
        }
    }

    override fun itemClick(obj: Int, text: String, enteredText: String) {
        when (text) {
            "location" -> {
                if (obj == locationList.size - 1) {
                    startLocationPicker()
                }
            }

            "work" -> {
                if (obj == workList.size - 1) {
                    if (enteredText.isNotEmpty()) {
                        addMyWork(enteredText)
                        AddWorkModel(enteredText)
                    }
                }
            }

            "language" -> {
                if (obj == languageList.size - 1) {
                    dialogSelectLanguage()
                }
            }

            "Hobbies" -> {
                if (obj == hobbiesList.size - 1) {
                    if (enteredText.isNotEmpty()) {
                        Log.d("@@@@@", "enteredText $enteredText")
                        addHobbiesApi(enteredText)
                        AddHobbiesModel(enteredText)
                    }
                }
            }

            "Pets" -> {
                if (obj == petsList.size - 1) {
                    if (enteredText.isNotEmpty()) {
                        addPetApi(enteredText)
                        AddPetsModel(enteredText)
                    }

                }
            }
        }
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.textPrivacyPolicy -> {
                val bundle = Bundle()
                bundle.putInt("privacy", 0)
                findNavController().navigate(R.id.privacyPolicyFragment, bundle)
            }

            R.id.textFaq -> {
                findNavController().navigate(R.id.frequentlyAskedQuestionsFragment)
            }

            R.id.rlPasswordTitle -> {
                var text = "Your password has been changed successfully"
                dialogNewPassword(requireContext(), text)
            }

            R.id.textTermServices -> {
                findNavController().navigate(R.id.termsServicesFragment)
            }

            R.id.textLogout -> {
                dialogLogOut(requireContext(), "LogOut")
            }

            R.id.textGiveFeedback -> {
                findNavController().navigate(R.id.feedbackFragment)
            }

            R.id.textNotifications -> {
                findNavController().navigate(R.id.notificationFragment)
            }

            R.id.textVisitHelpCenter -> {
                var bundle = Bundle()
                bundle.putString(AppConstant.type, AppConstant.Guest)
                findNavController().navigate(R.id.helpCenterFragment_host, bundle)
            }

            R.id.imageInfoIcon -> {
                binding.cvInfo.visibility = View.VISIBLE
            }

            R.id.clHead -> {
                binding.cvInfo.visibility = GONE
            }

            R.id.imageEditPicture -> {
                bottomSheetUploadImage()
            }

            R.id.imageEditName -> {
                dialogChangeName(requireContext())
            }

            R.id.textConfirmNow -> {
                dialogEmailVerification(requireContext())
            }

            R.id.textConfirmNow1 -> {
                dialogNumberVerification(requireContext())
            }

            R.id.imageEditEmail -> {
                dialogEmailVerification(requireContext())
            }

            R.id.imageEditPhoneNumber -> {
                dialogNumberVerification(requireContext())
            }


            R.id.textSaveButton -> {
                val intent = Intent(requireContext(), GuesMain::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                requireActivity().finish()
            }

            R.id.skip_now -> {
                val intent = Intent(requireContext(), GuesMain::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                requireActivity().finish()
            }

            R.id.textAddNewPaymentCard -> {
                dialogAddCard()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getUserCards() {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                profileViewModel.getUserCards(session?.getUserId().toString()).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                customerId = resp.get("stripe_customer_id").asString
                                val listType = object : TypeToken<List<UserCards>>() {}.type
                                userCardsList =
                                    Gson().fromJson(resp.getAsJsonArray("cards"), listType)
                                if (userCardsList.isNotEmpty()) {
                                    addPaymentCardAdapter.updateItem(userCardsList)
                                    for (card in userCardsList) {
                                        if (card.is_preferred) {
                                            selectuserCard = card
                                            break
                                        }
                                    }
                                }
                            }
                        }

                        is NetworkResult.Error -> {
                            showErrorDialog(requireContext(), it.message!!)
                        }

                        else -> {
                            Log.v(ErrorDialog.TAG, "error::" + it.message)
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

    private fun updateAddStreetAddress(streetAddress: String) {
        lifecycleScope.launch {
            profileViewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            lifecycleScope.launch {
                                profileViewModel.addStreetAddressApi(
                                    session?.getUserId().toString(),
                                    streetAddress
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Street Address added successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        is NetworkResult.Error -> {
                                            showErrorDialog(requireContext(), it.message!!)
                                        }

                                        else -> {
                                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun updateCityAddress(cityName: String, type: String) {
        lifecycleScope.launch {
            profileViewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            lifecycleScope.launch {
                                profileViewModel.addCityApi(
                                    session?.getUserId().toString(),
                                    cityName
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                if (!type.equals(AppConstant.profileType)) {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "City added successfully",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }

                                        is NetworkResult.Error -> {
                                            showErrorDialog(requireContext(), it.message!!)
                                        }

                                        else -> {
                                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun updateStateAddress(type: String) {
        lifecycleScope.launch {
            profileViewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            lifecycleScope.launch {
                                profileViewModel.addStateApi(
                                    session?.getUserId().toString(),
                                    binding.stateEt.text.toString()
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                if (!type.equals(AppConstant.profileType)) {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "State added successfully",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }

                                        is NetworkResult.Error -> {
                                            showErrorDialog(requireContext(), it.message!!)
                                        }

                                        else -> {
                                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun updateZipCode(zipCode: String, type: String) {
        lifecycleScope.launch {
            profileViewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            lifecycleScope.launch {
                                profileViewModel.addZipCodeApi(
                                    session?.getUserId().toString(),
                                    zipCode
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                if (!type.equals(AppConstant.profileType)) {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "Zipcode added successfully.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }

                                        is NetworkResult.Error -> {
                                            showErrorDialog(requireContext(), it.message!!)
                                        }

                                        else -> {
                                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }


    private fun addMyWork(work: String) {
        lifecycleScope.launch {
            profileViewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            lifecycleScope.launch {
                                profileViewModel.addMyWorkApi(
                                    session?.getUserId().toString(),
                                    work
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Toast.makeText(
                                                    requireContext(),
                                                    resp.first,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        is NetworkResult.Error -> {
                                            showErrorDialog(requireContext(), it.message!!)
                                        }

                                        else -> {
                                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    private val pickImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->

                    // Load image into BottomSheetDialog's ImageView if available
                    binding.imageProfilePicture?.let {
                        Glide.with(this)
                            .load(uri)
                            .error(R.drawable.ic_profile_login)
                            .placeholder(R.drawable.ic_profile_login)
                            .into(it)
                        Glide.with(this)
                            .asBitmap()
                            .load(uri)
                            .error(R.drawable.ic_profile_login)
                            .placeholder(R.drawable.ic_profile_login)
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                                ) {
                                    binding.imageProfilePicture.setImageBitmap(resource)

                                    imageBytes = MediaUtils.bitmapToByteArray(resource)
                                    Log.d(ErrorDialog.TAG, imageBytes.toString())
                                    uploadProfile(imageBytes)
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                    // Handle placeholder if needed
                                }
                            })
                    }
                    imageStatus = "1"
                }
            }
        }

    private fun uploadProfile(bytes: ByteArray) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                lifecycleScope.launch {
                    profileViewModel.uploadProfileImage(
                        session?.getUserId().toString(),
                        bytes
                    ).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                it.data?.let { resp ->
                                    showSuccessDialog(requireContext(), resp.first)
                                }
                            }

                            is NetworkResult.Error -> {
                                showErrorDialog(requireContext(), it.message!!)
                            }

                            else -> {
                                Log.v(ErrorDialog.TAG, "error::" + it.message)
                            }
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

    private fun updateName(
        first_name: String,
        last_name: String,
        dialog: Dialog, textSaveChangesButton: TextView
    ) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                lifecycleScope.launch {
                    profileViewModel.addUpdateName(
                        session?.getUserId().toString(),
                        first_name,
                        last_name
                    ).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                it.data?.let { resp ->
                                    showSuccessDialog(requireContext(), resp.first)
                                    userProfile?.name = first_name + " " + last_name
                                    binding.user = userProfile
                                }
                                toggleLoginButtonEnabled(true, textSaveChangesButton)
                                dialog.dismiss()
                            }

                            is NetworkResult.Error -> {
                                showErrorDialog(requireContext(), it.message!!)
                                toggleLoginButtonEnabled(true, textSaveChangesButton)
                            }

                            else -> {
                                Log.v(ErrorDialog.TAG, "error::" + it.message)
                                toggleLoginButtonEnabled(true, textSaveChangesButton)
                            }
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

    @SuppressLint("SetTextI18n")
    private fun updateAddAboutMe(about_me: String) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                lifecycleScope.launch {
                    profileViewModel.addAboutMe(
                        session?.getUserId().toString(),
                        about_me
                    ).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                it.data?.let { resp ->
                                    binding.etAboutMe.isEnabled = false
                                    binding.imageEditAbout.visibility = View.VISIBLE
                                    binding.imageAboutCheckedButton.visibility = GONE
                                    showSuccessDialog(requireContext(), resp.first)
                                    userProfile?.about_me = about_me
                                    binding.user = userProfile
                                }
                            }

                            is NetworkResult.Error -> {
                                showErrorDialog(requireContext(), it.message!!)
                            }

                            else -> {
                                Log.v(ErrorDialog.TAG, "error::" + it.message)
                            }
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

    @SuppressLint("SetTextI18n")
    private fun addLivePlace(place_name: String) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                profileViewModel.addLivePlace(
                    session?.getUserId().toString(),
                    place_name
                ).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            result.data?.let { resp ->
                                showSuccessDialog(requireContext(), resp.first)

                                // Prevent duplicates before adding
                                if (!locationList.any { it.name == place_name }) {
                                    val newLocation = AddLocationModel(place_name)

                                    // Update the list and adapter safely
                                    locationList.add(0, newLocation)
                                    addLocationAdapter.updateLocations(locationList)
                                }
                            }
                        }

                        is NetworkResult.Error -> {
                            showErrorDialog(requireContext(), result.message!!)
                        }

                        else -> {
                            Log.v(ErrorDialog.TAG, "error::" + result.message)
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


    private fun deleteLivePlace(index: Int) {
        lifecycleScope.launch {
            profileViewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            lifecycleScope.launch {
                                profileViewModel.deleteLivePlaceApi(
                                    session?.getUserId().toString(),
                                    index
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Log.d("checkResponse", resp.toString())
                                                Toast.makeText(
                                                    requireContext(),
                                                    resp.first,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        is NetworkResult.Error -> {
                                            showErrorDialog(requireContext(), it.message!!)
                                        }

                                        else -> {
                                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun deleteMyWork(index: Int) {
        lifecycleScope.launch {
            profileViewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            lifecycleScope.launch {
                                profileViewModel.deleteMyWorkApi(
                                    session?.getUserId().toString(),
                                    index
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Toast.makeText(
                                                    requireContext(),
                                                    resp.first,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        is NetworkResult.Error -> {
                                            showErrorDialog(requireContext(), it.message!!)
                                        }

                                        else -> {
                                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun addLanguageApi(languageName: String) {
        lifecycleScope.launch {
            profileViewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            lifecycleScope.launch {
                                profileViewModel.addLanguageApi(
                                    session?.getUserId().toString(),
                                    languageName
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Toast.makeText(
                                                    requireContext(),
                                                    resp.first,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        is NetworkResult.Error -> {
                                            showErrorDialog(requireContext(), it.message!!)
                                        }

                                        else -> {
                                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun deleteLanguageApi(index: Int) {
        lifecycleScope.launch {
            profileViewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            lifecycleScope.launch {
                                profileViewModel.deleteLanguageApi(
                                    session?.getUserId().toString(),
                                    index
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Toast.makeText(
                                                    requireContext(),
                                                    resp.first,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        is NetworkResult.Error -> {
                                            showErrorDialog(requireContext(), it.message!!)
                                        }

                                        else -> {
                                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun addHobbiesApi(enteredText: String) {
        lifecycleScope.launch {
            profileViewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            lifecycleScope.launch {
                                profileViewModel.addHobbiesApi(
                                    session?.getUserId().toString(),
                                    enteredText
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Toast.makeText(
                                                    requireContext(),
                                                    resp.first,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        is NetworkResult.Error -> {
                                            showErrorDialog(requireContext(), it.message!!)
                                        }

                                        else -> {
                                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun deleteHobbiesApi(index: Int) {
        lifecycleScope.launch {
            profileViewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            lifecycleScope.launch {
                                profileViewModel.deleteHobbiesApi(
                                    session?.getUserId().toString(),
                                    index
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Toast.makeText(
                                                    requireContext(),
                                                    resp.first,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        is NetworkResult.Error -> {
                                            showErrorDialog(requireContext(), it.message!!)
                                        }

                                        else -> {
                                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun addPetApi(enteredText: String) {
        lifecycleScope.launch {
            profileViewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            lifecycleScope.launch {
                                profileViewModel.addPetApi(
                                    session?.getUserId().toString(),
                                    enteredText
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {

                                            it.data?.let { resp ->

                                                Toast.makeText(
                                                    requireContext(),
                                                    resp.first,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        is NetworkResult.Error -> {
                                            showErrorDialog(requireContext(), it.message!!)
                                        }

                                        else -> {
                                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun deletePetsApi(index: Int) {
        lifecycleScope.launch {
            profileViewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            lifecycleScope.launch {
                                profileViewModel.deletePetsApi(
                                    session?.getUserId().toString(),
                                    index
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Toast.makeText(
                                                    requireContext(),
                                                    resp.first,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        is NetworkResult.Error -> {
                                            showErrorDialog(requireContext(), it.message!!)
                                        }

                                        else -> {
                                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun verifyIdentityApi() {
        lifecycleScope.launch {
            profileViewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            lifecycleScope.launch {
                                profileViewModel.getVerifyIdentityApi(
                                    session?.getUserId().toString(),
                                    identity_verify = 1.toString()
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                binding.textConfirmNow2.visibility = GONE
                                                binding.textVerified2.visibility = View.VISIBLE
                                                session?.setUserVerified(true)
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Verified Successfully!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        is NetworkResult.Error -> {
                                            showErrorDialog(requireContext(), it.message!!)
                                        }

                                        else -> {
                                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }


    private fun profileImageGalleryChooser() {
        ImagePicker.with(this)
            .galleryOnly()
            .crop(4f, 4f) // Crop image (Optional)
            .compress(1024 * 5) // Compress the image to less than 5 MB
            .maxResultSize(250, 250) // Set max resolution
            .createIntent { intent ->
                pickImageLauncher.launch(intent)
            }
    }

    private fun profileImageCameraChooser() {
        ImagePicker.with(this)
            .cameraOnly()
            .crop(4f, 4f) // Crop image (Optional)
            .compress(1024 * 5) // Compress the image to less than 5 MB
            .maxResultSize(250, 250) // Set max resolution
            .createIntent { intent ->
                pickImageLauncher.launch(intent)
            }
    }


    private fun dialogAddCard() {
        var street_address = ""
        var city = ""
        var state = ""
        var zip_code = ""
        var dateManager = DateManager(requireContext())
        val dialog = Dialog(requireContext(), R.style.BottomSheetDialog)
        dialog.apply {
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
            val submitButton: TextView = findViewById(R.id.textSubmitButton)
            val etStreet: EditText = findViewById(R.id.etStreet)
            val etCity: EditText = findViewById(R.id.etCity)
            val etState: EditText = findViewById(R.id.etState)
            val etZipCode: EditText = findViewById(R.id.etZipCode)
            val etCardCvv: EditText = findViewById(R.id.etCardCvv)
            val checkBox: MaterialCheckBox = findViewById(R.id.checkBox)
            checkBox.setOnClickListener {
                if (checkBox.isChecked) {
                    etStreet.setText(street_address)
                    etCity.setText(city)
                    etState.setText(state)
                    etZipCode.setText(zip_code)
                } else {
                    etStreet.text.clear()
                    etCity.text.clear()
                    etState.text.clear()
                    etZipCode.text.clear()
                }
            }
            textMonth.setOnClickListener {
                dateManager.showMonthSelectorDialog { selectedMonth ->
                    textMonth.text = selectedMonth
                }
                textYear.setOnClickListener {
                    dateManager.showYearPickerDialog { selectedYear ->
                        textYear.text = selectedYear.toString()
                    }
                }
            }
            //vipin
            etCardNumber.addTextChangedListener(object : TextWatcher {
                private var isFormatting: Boolean = false
                private var previousText: String = ""

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    previousText = s.toString()
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (isFormatting) return

                    isFormatting = true

                    val digitsOnly = s.toString().replace(" ", "")
                    val formatted = StringBuilder()

                    for (i in digitsOnly.indices) {
                        formatted.append(digitsOnly[i])
                        if ((i + 1) % 4 == 0 && i != digitsOnly.length - 1) {
                            formatted.append(" ")
                        }
                    }

                    if (formatted.toString() != s.toString()) {
                        etCardNumber.setText(formatted.toString())
                        etCardNumber.setSelection(formatted.length)
                    }

                    isFormatting = false
                }
            })
            //end


            submitButton.setOnClickListener {
                if (etCardHolderName.text.isEmpty()) {
                    showToast(requireContext(), AppConstant.cardName)
                } else if (textMonth.text.isEmpty()) {
                    showToast(requireContext(), AppConstant.cardMonth)
                } else if (textYear.text.isEmpty()) {
                    showToast(requireContext(), AppConstant.cardYear)
                } else if (etCardCvv.text.isEmpty()) {
                    showToast(requireContext(), AppConstant.cardCVV)
                } else {
                    LoadingUtils.showDialog(requireContext(), false)
                    val stripe = Stripe(requireContext(), BuildConfig.STRIPE_KEY)
                    var month: Int? = null
                    var year: Int? = null
                    //  val cardNumber: String = Objects.requireNonNull(etCardNumber.text.toString().trim()).toString()
                    //vipin
                    val cardNumber: String =
                        Objects.requireNonNull(etCardNumber.text.toString().replace(" ", "").trim())
                            .toString()
                    Log.d("checkCardNumber",cardNumber)

                    val cvvNumber: String =
                        Objects.requireNonNull(etCardCvv.text.toString().trim()).toString()
                    val name: String = etCardHolderName.text.toString().trim()
                    month = dateManager.getMonthNumber(textMonth.text.toString())
                    year = textYear.text.toString().toInt()
                    // Billing Address fields
                    val street = etStreet.text.toString().trim()
                    val city = etCity.text.toString().trim()
                    val state = etState.text.toString().trim()
                    val zip = etZipCode.text.toString().trim()
                    // Create Address object
                    val billingAddress = Address.Builder()
                        .setLine1(street)
                        .setCity(city)
                        .setState(state)
                        .setPostalCode(zip)
                        .build()
                    val card = CardParams(
                        cardNumber,
                        month!!,
                        Integer.valueOf(year!!),
                        cvvNumber,
                        name,
                        address = billingAddress
                    )
                    stripe?.createCardToken(card, null, null,
                        object : ApiResultCallback<Token> {
                            override fun onError(e: Exception) {
                                Log.d("******  Token Error :-", "${e.message}")
                                showErrorDialog(requireContext(), e.message.toString())
                                LoadingUtils.hideDialog()
                            }

                            override fun onSuccess(result: Token) {
                                val id = result.id
                                Log.d("******  Token payment :-", "data $id")
                                LoadingUtils.hideDialog()
                                saveCardStripe(dialog, id, checkBox.isChecked)

                            }
                        })
                }
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
            sameAsMailingAddress { mailingAddress ->
                // Do something with the address here
                if (mailingAddress != null) {
                    Log.d(ErrorDialog.TAG, mailingAddress.toString())
                    mailingAddress?.let {
                        it.street_address?.let {
                            street_address = it
                        }
                        it.city?.let {
                            city = it
                        }
                        it.state?.let {
                            state = it
                        }
                        it.zip_code?.let {
                            zip_code = it
                        }
                    }
                }
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun sameAsMailingAddress(onAddressReceived: (MailingAddress?) -> Unit) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                profileViewModel.sameAsMailingAddress(session?.getUserId().toString())
                    .collect { it ->
                        when (it) {
                            is NetworkResult.Success -> {
                                it.data?.let { resp ->
                                    val mailingAddress: MailingAddress = Gson().fromJson(
                                        resp,
                                        MailingAddress::class.java
                                    )
                                    onAddressReceived(mailingAddress)
                                }
                            }

                            is NetworkResult.Error -> {
                                showErrorDialog(requireContext(), it.message!!)
                                onAddressReceived(null)
                            }

                            else -> {
                                Log.v(ErrorDialog.TAG, "error::" + it.message)
                                onAddressReceived(null)
                            }
                        }
                    }
            }
        } else {
            onAddressReceived(null)
            showErrorDialog(
                requireContext(),
                resources.getString(R.string.no_internet_dialog_msg)
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun saveCardStripe(dialog: Dialog, tokenId: String, saveasMail: Boolean) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                profileViewModel.saveCardStripe(
                    session?.getUserId().toString(),
                    tokenId
                ).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                dialog.dismiss()
                                getUserCards()
                            }
                        }

                        is NetworkResult.Error -> {
                            showErrorDialog(requireContext(), it.message!!)
                        }

                        else -> {
                            Log.v(ErrorDialog.TAG, "error::" + it.message)
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

    override fun onResume() {
        super.onResume()
        (activity as? GuesMain)?.profileColor()
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
            var imageCross = findViewById<ImageView>(R.id.imageCross)
            var imageEmailSocial = findViewById<ImageView>(R.id.imageEmailSocial)
            var etMobileNumber = findViewById<EditText>(R.id.etMobileNumber)
            var textContinueButton = findViewById<TextView>(R.id.textContinueButton)
            var checkBox = findViewById<CheckBox>(R.id.checkBox)
            var textKeepLogged = findViewById<TextView>(R.id.textKeepLogged)
            var textForget = findViewById<TextView>(R.id.textForget)
            var textDontHaveAnAccount = findViewById<TextView>(R.id.textDontHaveAnAccount)
            var textRegister = findViewById<TextView>(R.id.textRegister)

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
                var text = "Login Successful"

                var textHeaderOfOtpVerfication =
                    "Please type the verification code send \n to +1 999 999 9999"

                dialogOtpLoginRegister(context, text, textHeaderOfOtpVerfication)
                dismiss()
            }
            imageCross.setOnClickListener {
                dismiss()
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

    private fun dialogOtpLoginRegister(
        context: Context,
        text: String,
        textHeaderOfOtpVerfication: String
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
                        s: CharSequence,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence,
                        start: Int,
                        before: Int,
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

            textSubmitButton.setOnClickListener {

                if (text == "Your password has been changed\n" + " successfully.") {
                    dialogNewPassword(context, text)
                } else if (text.equals("Login Successful")) {
                    var session = SessionManager(context)
                    session.setUserId(1)
                    var intent = Intent(context, GuesMain::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                    context.startActivity(intent)
                } else {
                    if (text == "Your password has been changed successfully") {
                        dialogNewPassword(context, text)
                    } else {
                        dialogSuccess(context, text)
                    }


                }
                dismiss()
            }


            textResend.setOnClickListener {
                if (resendEnabled) {
                    rlResendLine.visibility = View.VISIBLE
                    incorrectOtp.visibility = GONE
                    countDownTimer?.cancel()
                    startCountDownTimer(context, textTimeResend, rlResendLine, textResend)
                    textResend.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.grey
                        )
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

    override fun set(position: Int) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                profileViewModel.setPreferredCard(
                    session?.getUserId().toString(),
                    userCardsList[position].card_id
                ).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                getUserCards()
                                showToast(requireContext(), resp.first)
                            }
                        }

                        is NetworkResult.Error -> {
                            showSuccessDialog(requireContext(), it.message!!)
                        }

                        else -> {
                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                        }
                    }
                }
            }
        } else {
            showErrorDialog(requireContext(), resources.getString(R.string.no_internet_dialog_msg))
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
            var imageCross = findViewById<ImageView>(R.id.imageCross)
            var imageEmailSocial = findViewById<ImageView>(R.id.imageEmailSocial)
            var etMobileNumber = findViewById<EditText>(R.id.etMobileNumber)
            var textContinueButton = findViewById<TextView>(R.id.textContinueButton)
            var checkBox = findViewById<CheckBox>(R.id.checkBox)
            var textKeepLogged = findViewById<TextView>(R.id.textKeepLogged)

            var textLoginButton = findViewById<TextView>(R.id.textLoginButton)

            textLoginButton.setOnClickListener {
                dialogLogin(context)
                dismiss()
            }

            textContinueButton.setOnClickListener {
                var text = "Your account is registered \nsuccessfully"
                var textHeaderOfOtpVerfication =
                    "Please type the verification code send \n to +1 999 999 9999"
                dialogOtpLoginRegister(context, text, textHeaderOfOtpVerfication)
                dismiss()
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


    @SuppressLint("MissingInflatedId")
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
            var imageCross = findViewById<ImageView>(R.id.imageCross)

            var textLoginButton = findViewById<TextView>(R.id.textLoginButton)
            var checkBox = findViewById<CheckBox>(R.id.checkBox)
            var textKeepLogged = findViewById<TextView>(R.id.textKeepLogged)
            var textForget = findViewById<TextView>(R.id.textForget)
            val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
            var imgHidePass = findViewById<ImageView>(R.id.imgHidePass)
            var imgShowPass = findViewById<ImageView>(R.id.imgShowPass)
            var textDontHaveAnAccount = findViewById<TextView>(R.id.textDontHaveAnAccount)
            var textRegister = findViewById<TextView>(R.id.textRegister)

            eyeHideShow(imgHidePass, imgShowPass, etConfirmPassword)

            textRegister.setOnClickListener {
                dialogRegisterEmail(context)
                dismiss()
            }

            textForget.setOnClickListener {
                dialogForgotPassword(context)
                dismiss()
            }
            textLoginButton.setOnClickListener {
                val intent = Intent(context, GuesMain::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                context.startActivity(intent)
                dismiss()
            }
            imageCross.setOnClickListener {
                dismiss()
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }


    fun eyeHideShow(imgHidePass: ImageView?, imgShowPass: ImageView?, etPassword: EditText?) {
        imgHidePass?.setOnClickListener {
            imgShowPass?.visibility = View.VISIBLE
            imgHidePass.visibility = GONE
            etPassword?.apply {
                transformationMethod = HideReturnsTransformationMethod.getInstance()
                setSelection(text?.length ?: 0)
                invalidate() // Ensure UI refresh
            }
        }

        imgShowPass?.setOnClickListener {
            imgShowPass.visibility = GONE
            imgHidePass?.visibility = View.VISIBLE
            etPassword?.apply {
                transformationMethod = PasswordTransformationMethod.getInstance()
                setSelection(text?.length ?: 0)
                invalidate() // Ensure UI refresh
            }
        }
    }


    fun dialogRegisterEmail(context: Context?) {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_register_email)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            var imageCross = findViewById<ImageView>(R.id.imageCross)

            var textCreateAccountButton = findViewById<TextView>(R.id.textCreateAccountButton)
            var checkBox = findViewById<CheckBox>(R.id.checkBox)
            var textKeepLogged = findViewById<TextView>(R.id.textKeepLogged)


            var textLoginHere = findViewById<TextView>(R.id.textLoginHere)

            textLoginHere.setOnClickListener {
                dialogLoginEmail(context)
                dismiss()
            }
            textCreateAccountButton.setOnClickListener {
                var text = "Your account is registered \nsuccessfully"

                var textHeaderOfOtpVerfication =
                    "Please type the verification code send \nto abc@gmail.com"
                dialogOtpLoginRegister(context, text, textHeaderOfOtpVerfication)

                dismiss()
            }

            imageCross.setOnClickListener {
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
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
            var imageCross = findViewById<ImageView>(R.id.imageCross)
            var etEmail = findViewById<EditText>(R.id.etEmail)
            var textSubmitButton = findViewById<TextView>(R.id.textSubmitButton)
            textSubmitButton.setOnClickListener {
                var text = "Your password has been changed\n successfully."

                var textHeaderOfOtpVerfication =
                    "Please type the verification code send \nto abc@gmail.com"
                dialogOtpLoginRegister(context, text, textHeaderOfOtpVerfication)


                dismiss()
            }
            imageCross.setOnClickListener {
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

    private fun dialogNumberVerification(context: Context?) {
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
            val etMobileNumber = findViewById<EditText>(R.id.etMobileNumber)
            val countyCodePicker = findViewById<CountryCodePicker>(R.id.countyCodePicker)
          //  etMobileNumber.setText(binding.etPhoneNUMBER.text.toString())
            textSubmitButton.setOnClickListener {
                toggleLoginButtonEnabled(false, textSubmitButton)
                lifecycleScope.launch {
                    profileViewModel.networkMonitor.isConnected
                        .distinctUntilChanged() // Ignore duplicate consecutive values
                        .collect { isConn ->
                            if (!isConn) {
                                showErrorDialog(
                                    requireContext(),
                                    resources.getString(R.string.no_internet_dialog_msg)
                                )
                                toggleLoginButtonEnabled(true, textSubmitButton)
                            } else {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (etMobileNumber.text!!.isEmpty()) {
                                        etMobileNumber.error = "Mobile required"
                                        showErrorDialog(requireContext(), AppConstant.mobile)
                                        toggleLoginButtonEnabled(true, textSubmitButton)
                                    } else {
                                        val phoneNumber = etMobileNumber.text.toString()
                                        Log.d(ErrorDialog.TAG, phoneNumber)
                                        val countryCode =
                                            countyCodePicker.selectedCountryCodeWithPlus
                                        Log.d(ErrorDialog.TAG, countryCode)
                                        verifyPhoneNumber(
                                            countryCode,
                                            phoneNumber,
                                            dialog,
                                            textSubmitButton
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


    private fun dialogChangeName(context: Context?) {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(true)
            setContentView(R.layout.dialog_change_names)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            val imageProfilePicture = findViewById<CircleImageView>(R.id.imageProfilePicture)
            if (imageBytes.isNotEmpty()) {
                MediaUtils.setImageFromByteArray(imageBytes, imageProfilePicture)
            }


            val textSaveChangesButton = findViewById<TextView>(R.id.textSaveChangesButton)
            val editTextFirstName = findViewById<EditText>(R.id.editTextFirstName)
            val editTextLastName = findViewById<EditText>(R.id.editTextLastName)

            editTextFirstName.setText(firstName)
            editTextLastName.setText(lastName)

            textSaveChangesButton.setOnClickListener {
                if (editTextFirstName.text.isEmpty()) {
                    showErrorDialog(requireContext(), AppConstant.firstName)
                } else if (editTextLastName.text.isEmpty()) {
                    showErrorDialog(requireContext(), AppConstant.lastName)
                } else {
                    toggleLoginButtonEnabled(false, textSaveChangesButton)
                    updateName(
                        editTextFirstName.text.toString(),
                        editTextLastName.text.toString(),
                        dialog, textSaveChangesButton
                    )
                }
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }


    private fun dialogEmailVerification(context: Context?) {
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

            val etEmail = findViewById<EditText>(R.id.etEmail)
           // etEmail.setText(binding.etEmail.text.toString())
            val textSubmitButton = findViewById<TextView>(R.id.textSubmitButton)
            textSubmitButton.setOnClickListener {
                toggleLoginButtonEnabled(false, textSubmitButton)
                lifecycleScope.launch {
                    profileViewModel.networkMonitor.isConnected
                        .distinctUntilChanged() // Ignore duplicate consecutive values
                        .collect { isConn ->
                            if (!isConn) {
                                showErrorDialog(
                                    requireContext(),
                                    resources.getString(R.string.no_internet_dialog_msg)
                                )
                                toggleLoginButtonEnabled(true, textSubmitButton)
                            } else {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (etEmail.text!!.isEmpty()) {
                                        etEmail.error = "Email Address required"
                                        showErrorDialog(requireContext(), AppConstant.email)
                                        toggleLoginButtonEnabled(true, textSubmitButton)
                                    } else if (!isValidEmail(etEmail.text.toString())) {
                                        etEmail.error = "Invalid Email Address"
                                        showErrorDialog(requireContext(), AppConstant.invalideemail)
                                        toggleLoginButtonEnabled(true, textSubmitButton)
                                    } else {
                                        emailVerification(
                                            userId,
                                            etEmail.text.toString(),
                                            dialog,
                                            textSubmitButton
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

    @SuppressLint("SuspiciousIndentation", "CutPasteId")
    fun dialogOtp(
        context: Context,
        code: String,
        number: String,
        textHeaderOfOtpVerfication: String,
        type: String
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


            otpDigits = arrayOf(
                findViewById(R.id.otp_digit1),
                findViewById(R.id.otp_digit2),
                findViewById(R.id.otp_digit3),
                findViewById(R.id.otp_digit4)
            )

            for (i in otpDigits.indices) {
                otpDigits[i].setOnClickListener {
                    otpDigits[i].requestFocus()
                }

                otpDigits[i].addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        if (s.length == 1 && i < otpDigits.size - 1) {
                            otpDigits[i + 1].requestFocus()
                        } else if (s.isEmpty() && i > 0) {
                            otpDigits[i - 1].requestFocus()
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

            textSubmitButton.setOnClickListener {
                toggleLoginButtonEnabled(false, textSubmitButton)
                lifecycleScope.launch {
                    profileViewModel.networkMonitor.isConnected
                        .distinctUntilChanged() // Ignore duplicate consecutive values
                        .collect { isConn ->
                            if (!isConn) {
                                showErrorDialog(
                                    requireContext(),
                                    resources.getString(R.string.no_internet_dialog_msg)
                                )
                                toggleLoginButtonEnabled(true, textSubmitButton)
                            } else {
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
                                        if ("mobile".equals(type)) {
                                            otpVerifyPhoneVerification(
                                                userId,
                                                otp,
                                                dialog,
                                                number,
                                                textSubmitButton
                                            )
                                        }
                                        if ("email".equals(type)) {
                                            otpVerifyEmailVerification(
                                                userId,
                                                otp,
                                                dialog,
                                                number,
                                                textSubmitButton
                                            )
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
                lifecycleScope.launch {
                    profileViewModel.networkMonitor.isConnected
                        .distinctUntilChanged() // Ignore duplicate consecutive values
                        .collect { isConn ->
                            if (!isConn) {
                                showErrorDialog(
                                    requireContext(),
                                    resources.getString(R.string.no_internet_dialog_msg)
                                )
                            } else {
                                if ("email".equals(type)) {
                                    if (resendEnabled) {
                                        resendEmailVerification(
                                            userId, number, textResend,
                                            rlResendLine, incorrectOtp, textTimeResend
                                        )
                                    }
                                }
                                if ("mobile".equals(type)) {
                                    resendPhoneVerification(
                                        userId, code, number, textResend,
                                        rlResendLine, incorrectOtp, textTimeResend
                                    )
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
                textTimeResend.text = "${f.format(min)}:${f.format(sec)} sec"
            }

            override fun onFinish() {
                textTimeResend.text = "00:00"
                rlResendLine.visibility = GONE
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


    private fun dialogNewPassword(context: Context?, text: String) {
        context?.let { ctx ->
            Dialog(ctx, R.style.BottomSheetDialog).apply {
                setCancelable(false)
                setContentView(R.layout.dialog_new_password)

                window?.apply {
                    attributes = attributes?.apply {
                        width = WindowManager.LayoutParams.MATCH_PARENT
                        height = WindowManager.LayoutParams.MATCH_PARENT
                    }
                    setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }

                findViewById<ImageView>(R.id.imageCross)?.setOnClickListener { dismiss() }

                val etPassword = findViewById<EditText>(R.id.etPassword) ?: return
                val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword) ?: return

                findViewById<TextView>(R.id.textSubmitButton)?.setOnClickListener {
                    val password = etPassword.text.toString().trim()
                    val confirmPassword = etConfirmPassword.text.toString().trim()

                    when {
                        password.isEmpty() -> showErrorDialog(ctx, "Enter Password")
                        confirmPassword.isEmpty() -> showErrorDialog(ctx, "Enter Confirm Password")
                        password != confirmPassword -> showErrorDialog(ctx, "Password not match")
                        else -> {
                            updatePasswordApi(password, confirmPassword)
                            dismiss() // Dismiss the dialog after successful API call
                        }
                    }
                }

                show()
            }
        }
    }


    private fun dialogSuccess(context: Context?, text: String) {
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

                } else if (text == "Your password has been changed\n" + " successfully.") {
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
                lifecycleScope.launch {
                    profileViewModel.networkMonitor.isConnected
                        .distinctUntilChanged()
                        .collect { isConn ->
                            if (!isConn) {
                                LoadingUtils.showErrorDialog(
                                    requireContext(),
                                    resources.getString(R.string.no_internet_dialog_msg)
                                )
                            } else {
                                logout()
                            }

                        }
                }
//                val sessionManager = SessionManager(context)
//                sessionManager.setUserId(-1)
//                val intent = Intent(context, AuthActivity::class.java)
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                context.startActivity(intent)
                dismiss()
            }

            findViewById<RelativeLayout>(R.id.rlCancel).setOnClickListener {
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

    private fun updatePasswordApi(password: String, confirmPassword: String) {
        lifecycleScope.launch {
            profileViewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            lifecycleScope.launch {
                                profileViewModel.updatePasswordApi(
                                    session?.getUserId().toString(),
                                    password, confirmPassword
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                dialogSuccess(
                                                    context,
                                                    "Your password has been changed\n" + " successfully."
                                                )
                                            }
                                        }

                                        is NetworkResult.Error -> {
                                            showErrorDialog(requireContext(), it.message!!)
                                        }

                                        else -> {
                                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun emailVerification(
        userId: String,
        email: String,
        dialog: Dialog,
        textLoginButton: TextView
    ) {
        lifecycleScope.launch {
            profileViewModel.updateEmail(
                Integer.parseInt(userId),
                email
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            dialog.dismiss()
                            val textHeaderOfOtpVerfication =
                                "Please type the verification code send \nto $email"
                            dialogOtp(
                                requireActivity(),
                                "",
                                email,
                                textHeaderOfOtpVerfication,
                                "email"
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
                        Log.v(ErrorDialog.TAG, "error::" + it.message)
                    }
                }
            }
        }
    }

    private fun verifyPhoneNumber(
        countryCode: String,
        phoneNumber: String,
        dialog: Dialog,
        textSubmitButton: TextView
    ) {
        lifecycleScope.launch {
            session?.getUserId()?.let {
                profileViewModel.updatePhoneNumber(
                    it, phoneNumber,
                    countryCode
                ).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                dialog.dismiss()
                                val textHeaderOfOtpVerfication =
                                    "Please type the verification code send \nto $phoneNumber"
                                dialogOtp(
                                    requireActivity(),
                                    countryCode,
                                    phoneNumber,
                                    textHeaderOfOtpVerfication,
                                    "mobile"
                                )
                            }
                            dialog.dismiss()
                            toggleLoginButtonEnabled(true, textSubmitButton)
                        }

                        is NetworkResult.Error -> {
                            showErrorDialog(requireContext(), it.message!!)
                            toggleLoginButtonEnabled(true, textSubmitButton)
                        }

                        else -> {
                            toggleLoginButtonEnabled(true, textSubmitButton)
                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                        }
                    }
                }
            }
        }
    }

    private fun otpVerifyPhoneVerification(
        userId: String,
        otp: String,
        dialog: Dialog,
        number: String,
        text: TextView
    ) {
        lifecycleScope.launch {
            profileViewModel.otpVerifyUpdatePhoneNumber(
                Integer.parseInt(userId),
                otp,
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            binding.textConfirmNow1.visibility = GONE
                            binding.textVerified1.visibility = View.VISIBLE
                            dialog.dismiss()
                            showSuccessDialog(requireContext(), resp)
                            userProfile?.let {
                                it.phone_number = number
                                binding.user = it
                            }
                        }

                        toggleLoginButtonEnabled(true, text)
                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(requireContext(), it.message!!)
                        toggleLoginButtonEnabled(true, text)
                    }

                    else -> {
                        toggleLoginButtonEnabled(true, text)
                        Log.v(ErrorDialog.TAG, "error::" + it.message)
                    }
                }
            }
        }

    }

    private fun otpVerifyEmailVerification(
        userId: String,
        otp: String,
        dialog: Dialog,
        number: String,
        text: TextView
    ) {
        lifecycleScope.launch {
            profileViewModel.otpVerifyUpdateEmail(
                Integer.parseInt(userId),
                otp,
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            binding.textConfirmNow.visibility = GONE
                            binding.textVerified.visibility = View.VISIBLE
                            showSuccessDialog(requireContext(), resp)
                            userProfile?.let {
                                it.email = number
                                binding.user = it
                            }
                            dialog.dismiss()
                        }

                        toggleLoginButtonEnabled(true, text)
                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(requireContext(), it.message!!)
                        toggleLoginButtonEnabled(true, text)
                    }

                    else -> {
                        toggleLoginButtonEnabled(true, text)
                        Log.v(ErrorDialog.TAG, "error::" + it.message)
                    }
                }
            }
        }

    }

    private fun resendPhoneVerification(
        userId: String,
        code: String,
        number: String,
        textResend: TextView,
        rlResendLine: RelativeLayout,
        incorrectOtp: TextView,
        textTimeResend: TextView
    ) {
        lifecycleScope.launch {
            profileViewModel.phoneVerification(
                userId,
                code,
                number
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            rlResendLine.visibility = View.VISIBLE
                            incorrectOtp.visibility = GONE
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
                        toggleLoginButtonEnabled(true, textTimeResend)
                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(requireContext(), it.message!!)
                        toggleLoginButtonEnabled(true, textTimeResend)
                    }

                    else -> {
                        toggleLoginButtonEnabled(true, textTimeResend)
                        Log.v(ErrorDialog.TAG, "error::" + it.message)
                    }
                }
            }
        }
    }

    private fun resendEmailVerification(
        userId: String,
        email: String,
        textResend: TextView,
        rlResendLine: RelativeLayout,
        incorrectOtp: TextView,
        textTimeResend: TextView
    ) {
        lifecycleScope.launch {
            profileViewModel.emailVerification(
                userId,
                email
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            rlResendLine.visibility = View.VISIBLE
                            incorrectOtp.visibility = GONE
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
                        toggleLoginButtonEnabled(true, textResend)
                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(requireContext(), it.message!!)
                        toggleLoginButtonEnabled(true, textResend)
                    }

                    else -> {
                        toggleLoginButtonEnabled(true, textResend)
                        Log.v(ErrorDialog.TAG, "error::" + it.message)
                    }
                }
            }
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


    // Function to convert List<String> to MutableList<T>
    fun <T : HasName> getObjectsFromNames(
        names: List<String>,
        constructor: (String) -> T
    ): MutableList<T> {
        return names.mapNotNull {
            if (it != AppConstant.unknownLocation) {
                constructor(it) // Create an object of type T using the constructor
            } else {
                null
            }
        }.toMutableList()
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

    override fun itemClick(obj: Int, text: String) {
        when (text) {
            "location" -> {
                if (obj < locationList.size) {
                    deleteLivePlace(obj)
                    locationList.removeAt(obj)
                    addLocationAdapter.updateLocations(locationList)
                }
            }

            "work" -> {
                if (obj < workList.size - 1) {
                    deleteMyWork(obj)
                    workList.removeAt(obj)
                    addWorkAdapter.updateWork(workList)
                }
            }

            "language" -> {
                if (obj < languageList.size - 1) {
                    deleteLanguageApi(obj)
                    languageList.removeAt(obj)
                    addLanguageSpeakAdapter.updateLanguage(languageList)
                }
            }

            "Hobbies" -> {
                if (obj < hobbiesList.size - 1) {
                    deleteHobbiesApi(obj)
                    hobbiesList.removeAt(obj)
                    addHobbiesAdapter.updateHobbies(hobbiesList)
                }

            }

            "Pets" -> {
                if (obj < petsList.size - 1) {
                    deletePetsApi(obj)
                    petsList.removeAt(obj)
                    addPetsAdapter.updatePets(petsList)
                }
            }
        }
    }


    private fun logout() {
        lifecycleScope.launch {
            profileViewModel.logout(session?.getUserId().toString()).collect {
                when (it) {

                    is NetworkResult.Success -> {
                        showSuccessDialog(requireContext(), it.data!!)

                        val sessionManager = SessionManager(requireContext())
                        sessionManager.setUserId(-1)
                        val intent = Intent(requireContext(), AuthActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        requireActivity().startActivity(intent)
                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(requireContext(), it.message!!)

                    }

                    else -> {

                    }

                }
            }


        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}