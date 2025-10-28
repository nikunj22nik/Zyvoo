package com.business.zyvo.fragment.host

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.skydoves.powerspinner.PowerSpinnerView
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
import com.business.zyvo.activity.guest.checkout.model.MailingAddress
import com.business.zyvo.activity.guest.checkout.model.UserCards
import com.business.zyvo.adapter.AdapterAddPaymentCard
import com.business.zyvo.adapter.AddHobbiesAdapter
import com.business.zyvo.adapter.AddLanguageSpeakAdapter
import com.business.zyvo.adapter.AddLocationAdapter
import com.business.zyvo.adapter.AddPetsAdapter
import com.business.zyvo.adapter.AddWorkAdapter
import com.business.zyvo.adapter.host.BankNameAdapter
import com.business.zyvo.adapter.host.BankNameAdapterPayout
import com.business.zyvo.adapter.host.CardNumberAdapter
import com.business.zyvo.adapter.host.CardNumberAdapterPayout
import com.business.zyvo.adapter.selectLanguage.LocaleAdapter
import com.business.zyvo.databinding.FragmentHostProfileBinding
import com.business.zyvo.fragment.both.completeProfile.HasName
import com.business.zyvo.fragment.guest.profile.model.BankAccountPayout
import com.business.zyvo.fragment.guest.profile.model.CardPayout
import com.business.zyvo.fragment.guest.profile.model.GetPayoutResponse
import com.business.zyvo.fragment.guest.profile.model.UserProfile
import com.business.zyvo.fragment.guest.profile.viewModel.ProfileViewModel
import com.business.zyvo.model.AddHobbiesModel
import com.business.zyvo.model.AddLanguageModel
import com.business.zyvo.model.AddLocationModel
import com.business.zyvo.model.AddPaymentCardModel
import com.business.zyvo.model.AddPetsModel
import com.business.zyvo.model.AddWorkModel
import com.business.zyvo.model.CountryLanguage
import com.business.zyvo.onItemClickData
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.CommonAuthWorkUtils
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.ErrorDialog.getLocationDetails
import com.business.zyvo.utils.ErrorDialog.isValidEmail
import com.business.zyvo.utils.ErrorDialog.showToast
import com.business.zyvo.utils.MediaUtils
import com.business.zyvo.utils.NetworkMonitorCheck
import com.business.zyvo.utils.PrepareData
import com.business.zyvo.viewmodel.PaymentViewModel
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.gson.Gson
import com.google.gson.JsonObject
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
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.Objects
import androidx.core.graphics.drawable.toDrawable
import androidx.core.widget.addTextChangedListener
import com.business.zyvo.AppConstant.Companion.passwordMustConsist
import com.business.zyvo.activity.HostMainActivity
import com.business.zyvo.locationManager.LocationManager
import com.business.zyvo.onClickSelectCard
import com.business.zyvo.utils.MultipartUtils
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.widget.AutocompleteActivity
import java.util.Arrays


 @AndroidEntryPoint
 class HostProfileFragment : Fragment(), OnClickListener1, onItemClickData, OnClickListener,onClickSelectCard{

    private var _binding: FragmentHostProfileBinding? = null
    private val binding get() = _binding!!
    var userCardsList: MutableList<UserCards> = mutableListOf()
    private lateinit var commonAuthWorkUtils: CommonAuthWorkUtils
    private lateinit var addLocationAdapter: AddLocationAdapter
    private lateinit var addWorkAdapter: AddWorkAdapter
    private lateinit var addLanguageSpeakAdapter: AddLanguageSpeakAdapter
    private lateinit var addHobbiesAdapter: AddHobbiesAdapter
    var selectuserCard: UserCards? = null
    private lateinit var dateManager: DateManager
    private lateinit var bankNameAdapter: BankNameAdapter
    private lateinit var cardNumberAdapter: CardNumberAdapter
    private lateinit var bankNameAdapterPayout: BankNameAdapterPayout
    private lateinit var cardNumberAdapterPayout: CardNumberAdapterPayout
    private lateinit var addPetsAdapter: AddPetsAdapter
    private lateinit var addPaymentCardAdapter: AdapterAddPaymentCard
    var firstTime: Boolean = true
    var userProfileImage: String? = null
    var imageBytes: ByteArray = byteArrayOf()
    var session: SessionManager? = null
    var languageDialogOpen = false

    var customerId = ""

    // private lateinit var addPaymentCardAdapter: AdapterAddPaymentCard
    private val paymentCardViewHolder: PaymentViewModel by lazy {
        ViewModelProvider(this)[PaymentViewModel::class.java]
    }

    private val profileViewModel: ProfileViewModel by lazy {
        ViewModelProvider(this)[ProfileViewModel::class.java]
    }

    private var paymentList: MutableList<AddPaymentCardModel> = mutableListOf()
    private var petsList: MutableList<AddPetsModel> = mutableListOf()
    private var hobbiesList: MutableList<AddHobbiesModel> = mutableListOf()
    private var locationList: MutableList<AddLocationModel> = mutableListOf()
    private var workList: MutableList<AddWorkModel> = mutableListOf()
    private var languageList: MutableList<AddLanguageModel> = mutableListOf()
    private lateinit var apiKey: String
    private lateinit var localeAdapter: LocaleAdapter
    private var locales: List<Locale> = listOf()
    private var etSearch: TextView? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var imageStatus = ""
    private var place = ""
    private var isDropdownOpen = false
    private var isDropdownOpenpayout = false
    lateinit var navController: NavController
    private lateinit var otpDigits: Array<EditText>
    private var countDownTimer: CountDownTimer? = null
    var resendEnabled = false
    var otpValue: String = ""
    var editAboutButton = true
    var firstName: String = ""
    var lastName: String = ""
    var userProfile: UserProfile? = null

    private val list1 = mutableListOf<CountryLanguage>()
    private val list2 = mutableListOf<CountryLanguage>()

    private val bankListPayout = mutableListOf<BankAccountPayout>()
    private val cardListPayout = mutableListOf<CardPayout>()

    private lateinit var getInquiryResult: ActivityResultLauncher<Inquiry>

    private lateinit var userId: String
    var etAddress : EditText? = null
    var etCity1 : EditText? = null
    var zipcode : EditText? = null
    var etState1 : EditText? = null
    var latitude: String = "0.00"
    var longitude: String = "0.00"
    private var cardDialog: Dialog? = null

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


                    var flag =false
                    locationList.forEach {
                        if(it.name == placeName){
                            flag = true
                        }
                    }
                    if(!flag) {

                        // Update the list and notify adapter in one step
                        locationList.add(locationList.size - 1, newLocation)
                        // addLocationAdapter.notifyItemInserted(0)
                        addLocationAdapter.updateLocations(locationList)
                    }
                    Log.i(ErrorDialog.TAG, "Place: $placeName, ${place.id}")
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                Log.i(ErrorDialog.TAG, "User canceled autocomplete")
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TESTING_ZYVOO", "I AM INSIDE THE ONCREATE")
        val navController = findNavController()
        commonAuthWorkUtils = CommonAuthWorkUtils(requireContext(), navController)
        apiKey = getString(R.string.api_key)
        Log.d("CheckUserId", session?.getUserId().toString())
        userId = session?.getUserId().toString()
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

                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("TESTING_ZYVOO", "I AM INSIDE THE ONCREATEVIEW")
        _binding = FragmentHostProfileBinding.inflate(
            LayoutInflater.from(requireContext()),
            container,
            false
        )

        session = SessionManager(requireActivity())

        binding.textAddNew.setOnClickListener {
            dialogAddCardGuest()
        }
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

        addPaymentCardAdapter = AdapterAddPaymentCard(requireContext(), mutableListOf(), this)
        binding.recyclerViewPaymentCardList1.adapter = addPaymentCardAdapter
        dateManager = DateManager(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("TESTING_ZYVOO", "I AM INSIDE THE ONVIEWCREATED")

        binding.textGiveFeedback.setOnClickListener(this)
        binding.rlPasswordTitle.setOnClickListener(this)
        binding.textBooking.setOnClickListener(this)
        binding.textCreateList.setOnClickListener(this)
        //   binding.textTermServices.setOnClickListener(this)
        binding.textPrivacyPolicy.setOnClickListener(this)
        binding.textLogout.setOnClickListener(this)
        binding.textNotifications.setOnClickListener(this)
        binding.textVisitHelpCenter.setOnClickListener(this)
        binding.imageInfoIcon.setOnClickListener(this)
        binding.clHead.setOnClickListener(this)
        binding.imageEditPicture.setOnClickListener(this)
        binding.imageEditName.setOnClickListener(this)
        binding.textConfirmNow.setOnClickListener(this)
        binding.textConfirmNow1.setOnClickListener(this)
        binding.textPaymentWithdraw.setOnClickListener(this)
        binding.textLanguage.setOnClickListener(this)
        binding.imageEditEmail.setOnClickListener(this)
        binding.imageEditPhoneNumber.setOnClickListener(this)
        binding.textTermServices.setOnClickListener(this)
        adapterInitialize()
        paymentOpenCloseDropDown()
        payoutOpenCloseDropDown()
        //getPayoutMethods()
        // Initialize Places API if not already initialized
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity(), apiKey)
        }
        binding.switchHost.setOnClickListener {
            val app = activity?.application as MyApp
            val session = SessionManager(requireContext())
            session.setCurrentPanel(AppConstant.Guest)
            session.setChatToken("")
            app.clearInstance()
            val intent = Intent(requireContext(), GuesMain::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.putExtra("OPEN_PROFILE_FRAGMENT", true)
            startActivity(intent)
            requireActivity().finish()
        }
//        binding.textAddNew.setOnClickListener {
//          //  findNavController().navigate(R.id.payoutFragment)
//        }

        binding.textAddNewPaymentMethod.setOnClickListener {
            findNavController().navigate(R.id.hostPayoutFragment)
        }
        // Observe the isLoading state
//        lifecycleScope.launch {
//            profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
//                if (isLoading) {
//                    LoadingUtils.showDialog(requireContext(), false)
//                } else {
//                    LoadingUtils.hideDialog()
//                }
//            }
//        }
        getUserProfile()

        binding.rlEdit.setOnClickListener {
            if (editAboutButton) {
                binding.tvEdit.text = "Save"
                binding.imageEditAboutIcon.visibility = GONE
                binding.imageSaveAboutIcon.visibility = View.VISIBLE
                binding.etAboutMeText.isEnabled = true
                editAboutButton = false
            } else {
                if (binding.etAboutMeText.text.isEmpty()) {
                    showErrorDialog(requireContext(), AppConstant.aboutMe)
                } else {
                    updateAddAboutMe(binding.etAboutMeText.text.toString())
                }
            }
        }


        binding.imageEditStreetAddress.setOnClickListener {
            binding.streetEditText.isEnabled = true
            binding.imageEditStreetAddress.visibility = GONE
            binding.imageStreetCheckedButton.visibility = View.VISIBLE
        }
        binding.imageStreetCheckedButton.setOnClickListener {
            if (binding.streetEditText.text.isEmpty()) {
                showErrorDialog(requireContext(), "Street Cannot be Empty")
            } else {
                updateAddStreetAddress(binding.streetEditText.text.toString())
            }
            binding.streetEditText.isEnabled = false
         //   binding.imageEditStreetAddress.visibility = View.VISIBLE
         //   binding.imageStreetCheckedButton.visibility = GONE
        }

        binding.imageEditCityAddress.setOnClickListener {
            binding.cityET.isEnabled = true
            binding.imageEditCityAddress.visibility = GONE
            binding.CityCheckedButton.visibility = View.VISIBLE
        }
        binding.CityCheckedButton.setOnClickListener {
            if (binding.cityET.text.isEmpty()) {
                showErrorDialog(requireContext(), "City Cannot be Empty")
            } else {
                updateCityAddress(binding.cityET.text.toString(), "")
            }
            binding.cityET.isEnabled = false
          //  binding.imageEditCityAddress.visibility = View.VISIBLE
         //   binding.CityCheckedButton.visibility = GONE
        }

        binding.imageEditStateAddress.setOnClickListener {
            binding.stateEt.isEnabled = true
            binding.imageEditStateAddress.visibility = GONE
            binding.stateCheckedButton.visibility = View.VISIBLE
        }
        binding.stateCheckedButton.setOnClickListener {
            if (binding.stateEt.text.isEmpty()) {
                showErrorDialog(requireContext(), "State Cannot be Empty")
            } else {
                updateStateAddress("")
            }
            binding.stateEt.isEnabled = false
          //  binding.imageEditStateAddress.visibility = View.VISIBLE
        //    binding.stateCheckedButton.visibility = GONE
        }

        binding.imageEditZipAddress.setOnClickListener {
            binding.zipEt.isEnabled = true
            binding.imageEditZipAddress.visibility = GONE
            binding.zipCodeCheckedButton.visibility = View.VISIBLE
        }
        binding.zipCodeCheckedButton.setOnClickListener {
            if (binding.zipEt.text.isEmpty()) {
                showErrorDialog(requireContext(), "Zip Cannot be Empty")
            } else {
                updateZipCode(binding.zipEt.text.toString(), "")
            }
            binding.zipEt.isEnabled = false
         //   binding.imageEditZipAddress.visibility = View.VISIBLE
        //    binding.zipCodeCheckedButton.visibility = GONE

        }

        binding.textConfirmNow2.setOnClickListener {
            if (NetworkMonitorCheck._isConnected.value) {
                launchVerifyIdentity()
            } else {
                showErrorDialog(
                    requireContext(),
                    resources.getString(R.string.no_internet_dialog_msg)
                )
            }
        }

        binding.streetEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                startStreetLocationPicker()
            }
        }
        binding.streetEditText.setOnClickListener {
            startStreetLocationPicker()
        }


    }

    // For handling the result of the Autocomplete Activity
    private val startStreertAutocomplete =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                result.data?.let { intent ->
                    val place = Autocomplete.getPlaceFromIntent(intent)
                    val latLng = place.latLng
                    binding.imageEditStreetAddress.visibility = View.VISIBLE
                    binding.imageStreetCheckedButton.visibility = GONE
                    binding.streetEditText.clearFocus()
                    getLocationDetails(requireContext(), latLng) { locationDetails ->
                        // Use city, state, zipCode here
                        locationDetails?.let {
                            Log.d(
                                ErrorDialog.TAG,
                                "City: ${it.city}, State: ${it.state}, Zip: ${it.zipCode}"
                            )
                            if (!it.city.isNullOrEmpty() && !it.state.isNullOrEmpty() && !it.zipCode.isNullOrEmpty()
                            ) {
                                var street = ""
                                if (it.streetAddress.isNullOrEmpty()) {
                                    binding.streetEditText.setText(place.name ?: "")
                                    street = place.name ?: ""
                                } else {
                                    binding.streetEditText.setText(it.streetAddress ?: "")
                                    street = it.streetAddress ?: ""
                                }
                                binding.cityET.setText(it.city)
                                binding.stateEt.setText(it.state)
                                binding.zipEt.setText(it.zipCode)
                                binding.streetEditText.isEnabled = false
                                updateAddStreetAddress(street ?: "")
                                updateStateAddress(AppConstant.profileType)
                                updateZipCode(it.zipCode, AppConstant.profileType)
                                updateCityAddress(it.city, AppConstant.profileType)
                            }else{
                                binding.imageEditStreetAddress.visibility = GONE
                                binding.imageStreetCheckedButton.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                Log.i(ErrorDialog.TAG, "User canceled autocomplete")
                binding.imageEditStreetAddress.visibility = GONE
                binding.imageStreetCheckedButton.visibility = View.VISIBLE
            }
        }

    // Function to start the location picker using Autocomplete
    private fun startStreetLocationPicker() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .build(requireContext())
        binding.streetEditText.clearFocus()
        startStreertAutocomplete.launch(intent)
    }



    // Function to initialize the adapter for adding locations
    private fun adapterInitialize() {
        addLocationAdapter = AddLocationAdapter(requireContext(), locationList, this, this)
        binding.recyclerViewLocation.adapter = addLocationAdapter
        addLocationAdapter.updateLocations(locationList)
        addWorkAdapter = AddWorkAdapter(requireContext(), workList, this, this)
        binding.recyclerViewWork.adapter = addWorkAdapter
        addWorkAdapter.updateWork(workList)
        addLanguageSpeakAdapter = AddLanguageSpeakAdapter(requireContext(), languageList, this, this)
        binding.recyclerViewlanguages.adapter = addLanguageSpeakAdapter
        addLanguageSpeakAdapter.updateLanguage(languageList)
        bankNameAdapter = BankNameAdapter(requireContext(), list = list1)
        cardNumberAdapter = CardNumberAdapter(requireContext(), list = list2)
        binding.recyclerViewPaymentCardList.adapter = bankNameAdapter
        binding.recyclerViewCardNumberList.adapter = cardNumberAdapter
        val initialList1 = List(6) { index ->
            if (index % 2 == 0) {
                CountryLanguage("Bank Name", "Bill gilbert, Checking .....4898(USD)")
            } else {
                CountryLanguage("Bank Name", "Bill gilbert, Checking .....4898(USD)")
            }
        }
        val initialList2 = List(4) { index ->
            if (index % 2 == 0) {
                CountryLanguage("Card Number", " ****  **** ****78")
            } else {
                CountryLanguage("Card Number", " ****  **** ****78")
            }
        }
        bankNameAdapter.addItems(initialList1)
        cardNumberAdapter.addItems(initialList2)




        bankNameAdapterPayout = BankNameAdapterPayout(requireContext(), list = bankListPayout, this)
        cardNumberAdapterPayout =
            CardNumberAdapterPayout(requireContext(), list = cardListPayout, this)
        binding.recyclerViewPaymentCardListPayOut.adapter = bankNameAdapterPayout
        binding.recyclerViewCardNumberListPayOut.adapter = cardNumberAdapterPayout

//        if (bankListPayout.isNotEmpty()) {
//            bankNameAdapterPayout.addItems(bankListPayout)
//            binding.recyclerViewPaymentCardListPayOut.visibility = View.VISIBLE
//            binding.textBankNoDataFound.visibility = View.GONE
//        }else{
//            binding.recyclerViewPaymentCardListPayOut.visibility = View.GONE
//            binding.textBankNoDataFound.visibility = View.VISIBLE
//        }
//
//        if (cardListPayout.isNotEmpty()){
//            cardNumberAdapterPayout.addItems(cardListPayout)
//            binding.recyclerViewCardNumberListPayOut.visibility = View.VISIBLE
//            binding.textCardNoDataFound.visibility = View.GONE
//        }else{
//            binding.recyclerViewCardNumberListPayOut.visibility = View.GONE
//            binding.textCardNoDataFound.visibility = View.VISIBLE
//        }


        addPetsAdapter = AddPetsAdapter(requireContext(), petsList, this, this)
    }

    // this is used to get the userProfile and this func will provide the profile of the users there in the city
    private fun getUserProfile() {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                LoadingUtils.showDialog(requireContext(), false)
                val session = SessionManager(requireContext())
                profileViewModel.getUserProfile(session?.getUserId().toString()).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            binding.llScrlView.visibility = View.VISIBLE

                            var name = ""

                            it.data?.let { resp ->
                                Log.d(
                                    "TESTING_PROFILE",
                                    "HERE IN A USER PROFILE ," + resp.toString()
                                )
//                                userProfile = Gson().fromJson(resp, UserProfile::class.java)
//                                userProfile.let {
//                                   it?.first_name?.let {
//                                       name+=it+" "
//                                       firstName = it
//                                   }
//                                    it?.last_name?.let {
//                                        name+=it
//                                        lastName = it
//                                    }
//
//
//                                    it?.name = name
//                                    binding.user = it
//                                    it?.email?.let {
//                                        binding.etEmail.setText(it)
//                                        binding.etEmail.isEnabled = false
//                                    }
//                                    it?.phone_number?.let {
//                                        binding.etPhoneNumeber.setText(it)
//                                        binding.etPhoneNumeber.isEnabled = false
//                                    }
//                                    it?.street?.let {
//                                        binding.streetEditText.setText(it)
//                                    }
//
//                                    it?.state?.let {
//                                        binding.stateEt.setText(it)
//                                    }
//
//                                    it?.zip_code?.let {
//                                        binding.zipEt.setText(it)
//                                    }
//
//                                    it?.city?.let {
//                                        binding.cityET.setText(it)
//                                    }
//
//                                    if (it?.profile_image != null) {
//                                        Glide.with(requireContext())
//                                            .asBitmap() // Convert the image into Bitmap
//                                            .load(BuildConfig.MEDIA_URL + it.profile_image) // User profile image URL
//                                            .into(object : SimpleTarget<Bitmap>() {
//                                                override fun onResourceReady(
//                                                    resource: Bitmap,
//                                                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
//                                                ) {
//                                                    // The 'resource' is the Bitmap
//                                                    // Now you can use the Bitmap (e.g., set it to an ImageView, or process it)
//                                                    binding.imageProfilePicture.setImageBitmap(
//                                                        resource
//                                                    )
//                                                    imageBytes =
//                                                        MediaUtils.bitmapToByteArray(resource)
//                                                    Log.d(ErrorDialog.TAG, imageBytes.toString())
//                                                }
//                                            })
//                                    }
//                                    if (it?.email_verified != null && it.email_verified == 1) {
//                                        binding.textConfirmNow.visibility = GONE
//                                        binding.textVerified.visibility =
//                                            View.VISIBLE
//                                    }
//                                    if (it?.phone_verified != null && it.phone_verified == 1) {
//                                        binding.textConfirmNow1.visibility = GONE
//                                        binding.textVerified1.visibility =
//                                            View.VISIBLE
//                                    }
//                                    if (it?.identity_verified != null && it.identity_verified == 1) {
//                                        binding.textConfirmNow2.visibility = GONE
//                                        binding.textVerified2.visibility =
//                                            View.VISIBLE
//                                    }
//                                    if (it?.where_live != null && it.where_live.isNotEmpty()) {
//                                        locationList = getObjectsFromNames(it.where_live) { name ->
//                                            AddLocationModel(name)  // Using the constructor of MyObject to create instances
//                                        }
//
//                                        val newLanguage = AddLocationModel(AppConstant.unknownLocation)
//                                        locationList.add(newLanguage)
//                                        addLocationAdapter.updateLocations(locationList)
//                                    }
//                                    if (it?.my_work != null && it.my_work.isNotEmpty()) {
//                                        workList = getObjectsFromNames(it.my_work) { name ->
//                                            AddWorkModel(name)  // Using the constructor of MyObject to create instances
//                                        }
//                                        val newLanguage = AddWorkModel(AppConstant.unknownLocation)
//                                        workList.add(newLanguage)
//                                        addWorkAdapter.updateWork(workList)
//                                    }
//                                    if (it?.languages != null && it.languages.isNotEmpty()) {
//                                        languageList = getObjectsFromNames(it.languages) { name ->
//                                            AddLanguageModel(name)  // Using the constructor of MyObject to create instances
//                                        }
//                                        val newLanguage =
//                                            AddLanguageModel(AppConstant.unknownLocation)
//                                        languageList.add(newLanguage)
//                                        addLanguageSpeakAdapter.updateLanguage(languageList)
//                                    }
//
//                                }

                                settingDataToUi(resp)
                            }
                        }

                        is NetworkResult.Error -> {
                            LoadingUtils.hideDialog()
                            binding.llScrlView.visibility = GONE
                            showErrorDialog(requireContext(), it.message!!)
                        }

                        else -> {

                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                        }
                    }
                }
            }
        } else {
            LoadingUtils.showErrorDialog(
                requireContext(),
                resources.getString(R.string.no_internet_dialog_msg)
            )
        }
    }

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

    @SuppressLint("SuspiciousIndentation")
    private fun settingDataToUi(resp: JsonObject) {
        lifecycleScope.launch(Dispatchers.IO) {
             userProfile = Gson().fromJson(resp, UserProfile::class.java)
            userProfile?.let {
                val nameBuilder = StringBuilder()
                firstName = it.first_name?.also { nameBuilder.append("$it ") } ?: ""
                lastName = it.last_name?.also { nameBuilder.append(it) } ?: ""
                it.name = nameBuilder.toString()

                // Transform lists off main thread
                val transformedLocationList = it.where_live?.let {
                    getObjectsFromNames(it) {
                            name -> AddLocationModel(name)
                    }
                }?.apply {
                    add(AddLocationModel(AppConstant.unknownLocation))
                } ?: emptyList()

//                val transformedWorkList = it.my_work?.let {
//                    getObjectsFromNames(it) { name -> AddWorkModel(name) }
//                }?.apply {
//                    add(AddWorkModel(AppConstant.unknownLocation))
//                } ?: emptyList()


          //  val transformedWorkList = userProfile.my_work?.let {
//                workList = getObjectsFromNames(userProfile.my_work) { name ->
//                    AddWorkModel(name)  // Using the constructor of MyObject to create instances
//                }
//                val newLanguage = AddWorkModel(AppConstant.unknownLocation)
//                workList.add(newLanguage)

              //  getObjectsFromNames(it) { name -> AddWorkModel(name) }
//            }?.apply {
//                add(AddWorkModel(AppConstant.unknownLocation))
//            } ?: emptyList()

                                                if (it.my_work != null && it.my_work.isNotEmpty()) {
                                        workList = getObjectsFromNames(it.my_work) { name ->
                                            AddWorkModel(name)  // Using the constructor of MyObject to create instances
                                        }
                                        val newLanguage = AddWorkModel(AppConstant.unknownLocation)
                                        workList.add(newLanguage)
                                    }
                                    if (it.languages != null && it.languages.isNotEmpty()) {
                                        languageList = getObjectsFromNames(it.languages) { name ->
                                            AddLanguageModel(name)  // Using the constructor of MyObject to create instances
                                        }
                                        val newLanguage =
                                            AddLanguageModel(AppConstant.unknownLocation)
                                        languageList.add(newLanguage)

                                    }

//            val transformedLanguageList = userProfile.languages?.let {
//                getObjectsFromNames(it) { name -> AddLanguageModel(name) }
//            }?.apply {
//                add(AddLanguageModel(AppConstant.unknownLocation))
//            } ?: emptyList()

//                val transformedLanguageList = it.languages?.let {
//                    getObjectsFromNames(it) { name -> AddLanguageModel(name) }
//                }?.apply {
//                    add(AddLanguageModel(AppConstant.unknownLocation))
//                } ?: emptyList()

                // Now switch to UI thread
                withContext(Dispatchers.Main) {
                    binding.user = it
                    binding.etEmail.setText(it.email ?: "")
                    binding.etEmail.isEnabled = false
                    binding.etPhoneNumeber.setText(it.phone_number ?: "")
                    binding.etPhoneNumeber.isEnabled = false
                    binding.streetEditText.setText(it.street ?: "")
                    binding.stateEt.setText(it.state ?: "")
                    binding.zipEt.setText(it.zip_code ?: "")
                    binding.cityET.setText(it.city ?: "")


                    if (it.email_verified == 1) {
                        binding.textConfirmNow.visibility = GONE
                        binding.textVerified.visibility = View.VISIBLE
                    }
                    if (it.phone_verified == 1) {
                        binding.textConfirmNow1.visibility = GONE
                        binding.textVerified1.visibility = View.VISIBLE
                    }
                    if (it.identity_verified == 1) {
                        binding.textConfirmNow2.visibility = GONE
                        binding.textVerified2.visibility = View.VISIBLE
                    }

                    // Load profile image
                    it.profile_image?.let {
                        userProfileImage = BuildConfig.MEDIA_URL + it
                        Glide.with(requireContext())
                            .asBitmap()
                            .load(BuildConfig.MEDIA_URL + it)
                            .into(binding.imageProfilePicture)
                        session?.setUserImage(it)
                        Glide.with(requireContext()).load(BuildConfig.MEDIA_URL + it).into( (activity as HostMainActivity).binding.imageProfile)
                        (activity as HostMainActivity).showImage()
                    }

                    LoadingUtils.hideDialog()
                    // Update adapters
                    addLocationAdapter.updateLocations(transformedLocationList.toMutableList())
          
                    addWorkAdapter.updateWork(workList)
                    //  addLanguageSpeakAdapter.updateLanguage(transformedLanguageList.toMutableList())
                    addLanguageSpeakAdapter.updateLanguage(languageList)
            }

            }
        }

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
        Log.d("TESTING_ZYVOO_LANGUAGE", "SELECT LANGUAGE HERE")

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

                override fun onItemClick(local: String/*,type: String*/) {
                    val newLanguage = AddLanguageModel(local)

                    // Add the new language to the list
                    Log.d("laguageListSize", languageList.size.toString())
                    Log.d("laguageListSize", languageList.toString())
                    //  if (type == "add") {
                    if (languageList.size < 3) {

                        addLanguageApi(newLanguage.name)
                        if (!languageList.contains(newLanguage)) {
                            languageList.add(languageList.size - 1, newLanguage)
                        }
//                        else {
//                            showErrorDialog(requireContext(),"Can't add one Same Language more than one.")
//                        }
                    }else{
                        showErrorDialog(requireContext(),"You can only have \n two languages.")
                    }
                    /*  }else{
                          val index = languageList.indexOfFirst { it.name == local }
                          val removedLang = languageList[index].name
                          deleteLanguageApi(index)
                          languageList.removeAt(index)
                          SessionManager(requireContext()).removeLanguage(requireContext(), removedLang)
                      }*/


                    Log.d("laguageListSize", languageList.toString())
                    addLanguageSpeakAdapter.updateLanguage(languageList)
                    //addLanguageSpeakAdapter.notifyItemInserted(0)

                    // Delay dismissing the dialog slightly to prevent UI issues

                    Handler(Looper.getMainLooper()).postDelayed({ dialog.dismiss() }, 200)

                    // 200ms delay ensures smooth UI transition
                }


            }, PrepareData.languagesWithRegions/*, languageList*/)

            recyclerViewLanguages?.adapter = localeAdapter

            imageCross?.setOnClickListener {
                languageDialogOpen = false
                dismiss()
            }

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
            profileImageCameraChooser()
            bottomSheetDialog!!.dismiss()
        }
        textGallery?.setOnClickListener {
            profileImageGalleryChooser()
            bottomSheetDialog!!.dismiss()
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
                R.drawable.ic_dropdown_open // Replace with your open icon resource

            } else {
                R.drawable.ic_dropdown_close // Replace with your close icon resource
            }

            if (isDropdownOpen) {
                if (firstTime) {
                    getUserCards()
                }

                binding.recyclerViewPaymentCardList1.visibility = View.VISIBLE

            } else if (!isDropdownOpen) {
                binding.recyclerViewPaymentCardList1.visibility = GONE


            }
            binding.textPaymentMethod.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)
        }
    }


    private fun payoutOpenCloseDropDown() {
        // Set initial drawable
        binding.textPayOutMethod.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.ic_dropdown_close,
            0
        )
        binding.textPayOutMethod.setOnClickListener {
            // Toggle the state
            isDropdownOpenpayout = !isDropdownOpenpayout

            val drawableRes = if (isDropdownOpenpayout) {
                R.drawable.ic_dropdown_open
            } else {
                R.drawable.ic_dropdown_close
            }
            if (isDropdownOpenpayout) {
                getPayoutApi()
                binding.rlBankNameAndCardNamePayOut.visibility = View.VISIBLE

            } else if (!isDropdownOpenpayout) {
                binding.rlBankNameAndCardNamePayOut.visibility = GONE
            }
            binding.textPayOutMethod.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)
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
//                        addHobbiesApi(enteredText)
//                        AddHobbiesModel(enteredText)
                    }
                }
            }

            "Pets" -> {
                if (obj == petsList.size - 1) {
                    if (enteredText.isNotEmpty()) {
//                        addPetApi(enteredText)
//                        AddPetsModel(enteredText)
                    }

                }
            }

            "setPrimary" -> {
                setPrimaryPayoutMethods(enteredText,text,obj)
            }
            "setPrimaryCard" -> {
                setPrimaryPayoutMethods(enteredText,text,obj)
            }
            "delete" -> {
                deletePayoutMethods(enteredText,text,obj)
            }
            "deleteCard" -> {
                deletePayoutMethods(enteredText,text,obj)
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {


            R.id.textBooking -> {
                findNavController().navigate(R.id.bookingScreenHostFragment)
            }

            R.id.textCreateList -> {
                findNavController().navigate(R.id.host_manage_property_frag)
                //  startActivity(Intent(requireActivity(), PlaceOpenActivity::class.java))

            }

            R.id.textPrivacyPolicy -> {
                val bundle = Bundle()
                bundle.putInt("privacy", 1)
                findNavController().navigate(R.id.privacyPolicyFragment, bundle)
            }

            R.id.rlPasswordTitle -> {
                val text = "Your password has been changed successfully"
                dialogNewPassword(requireContext(), text)
            }

            R.id.textTermServices -> {
                val bundle = Bundle()
                bundle.putInt("privacy", 1)
                findNavController().navigate(R.id.termsServicesFragment, bundle)
            }

            R.id.textLogout -> {
                dialogLogOut(requireContext(), "LogOut")
            }

            R.id.textGiveFeedback -> {
                findNavController().navigate(R.id.feedbackFragment)
            }

            R.id.textLanguage -> {
                //  findNavController().navigate(R.id.language_fragment_host)
             //   if (!languageDialogOpen) {
                   // languageDialogOpen = true
                    dialogSelectLanguage()
               // }
            }

            R.id.textNotifications -> {
                var bundle = Bundle()
                bundle.putString(AppConstant.Host, "HOST")
                findNavController().navigate(R.id.notificationFragment, bundle)
            }

            R.id.imageEditEmail -> {

                dialogEmailVerification(requireContext())
            }

            R.id.imageEditPhoneNumber -> {
                dialogNumberVerification(requireContext())
            }

            R.id.textVisitHelpCenter -> {
                val bundle = Bundle()
                bundle.putString(AppConstant.type, AppConstant.Host)
                findNavController().navigate(R.id.helpCenterFragment_host, bundle)
            }

            R.id.imageInfoIcon -> {
                showPopupHostInfoWindow(binding.imageInfoIcon)
               /// binding.cvInfo.visibility = View.VISIBLE
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
                dialogEmailVerificationProfile(requireContext())
//                binding.textConfirmNow.visibility = View.GONE
//                binding.textVerified.visibility = View.VISIBLE
            }

            R.id.textConfirmNow1 -> {
                dialogNumberVerification(requireContext())
//                binding.textConfirmNow1.visibility = View.GONE
//                binding.textVerified1.visibility = View.VISIBLE
            }

            R.id.textSaveButton -> {
                var intent = Intent(requireContext(), GuesMain::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                requireActivity().finish()
            }

            R.id.skip_now -> {
                var intent = Intent(requireContext(), GuesMain::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                requireActivity().finish()
            }

            R.id.textAddNewPaymentCard -> {
                dialogSelectPaymentMethod()
            }

            R.id.textPaymentWithdraw -> {
                findNavController().navigate(R.id.hostPaymentFragment)
            }
        }
    }


    private fun dialogSelectPaymentMethod() {
        val dialog1 = Dialog(requireContext(), R.style.BottomSheetDialog)
        dialog1.setContentView(R.layout.dialog_select_payment_host)

        dialog1.setCancelable(false)
        dialog1.apply {
            val togglePaymentTypeSelectButton =
                findViewById<ToggleButton>(R.id.togglePaymentTypeSelectButton)
            val rlBankAccount = findViewById<RelativeLayout>(R.id.rlBankAccount)
            val llDebitCard = findViewById<LinearLayout>(R.id.llDebitCard)
            val spinnermonth = findViewById<PowerSpinnerView>(R.id.spinnermonth)
            val spinneryear = findViewById<PowerSpinnerView>(R.id.spinneryear)

            val btnAddPayment = findViewById<TextView>(R.id.btnAddPayment)

            callingSelectionOfDate(spinnermonth, spinneryear)

            togglePaymentTypeSelectButton.setOnCheckedChangeListener { v1, isChecked ->
                if (!isChecked) {
                    llDebitCard.visibility = GONE
                    rlBankAccount.visibility = View.VISIBLE

                } else {
                    rlBankAccount.visibility = GONE
                    llDebitCard.visibility = View.VISIBLE
                }

            }


            btnAddPayment.setOnClickListener {
                dismiss()

            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }

    }

    fun callingSelectionOfDate(spinnermonth: PowerSpinnerView, spinneryear: PowerSpinnerView) {
        val months = listOf(
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"
        )
        // val am_pm_list = listOf("AM","PM")
        val years = (2024..2050).toList()
        val yearsStringList = years.map { it.toString() }
        Toast.makeText(
            requireContext(),
            "Year String List: " + yearsStringList.size,
            Toast.LENGTH_LONG
        ).show()
        val days = resources.getStringArray(R.array.day).toList()


        // Add item decoration for spacing
        val spacing = 16 // Spacing in pixels


        spinnermonth.layoutDirection = View.LAYOUT_DIRECTION_LTR
        spinnermonth.arrowAnimate = false
        spinnermonth.spinnerPopupHeight = 400
        spinnermonth.setItems(months)
        spinnermonth.setIsFocusable(true)

        val recyclerView3 = spinnermonth.getSpinnerRecyclerView()

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

        spinneryear.layoutDirection = View.LAYOUT_DIRECTION_LTR
        spinneryear.arrowAnimate = false
        spinneryear.spinnerPopupHeight = 400
        spinneryear.setItems(yearsStringList.subList(0, 16))
        spinneryear.setIsFocusable(true)
//        binding.spinneryear.post {
//            binding.spinneryear.spinnerPopupWidth = binding.spinneryear.width
//        }


//        binding.endAmPm.post {
//            binding.endAmPm.spinnerPopupWidth = binding.endAmPm.width
//        }


//        binding.startAmPm.post {
//            binding.startAmPm.spinnerPopupWidth = binding.startAmPm.width
//        }


        val recyclerView1 = spinneryear.getSpinnerRecyclerView()
        recyclerView1.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.top = spacing
            }

        })


    }


    private val pickImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->

                    // Load image into BottomSheetDialog's ImageView if available
                    binding.imageProfilePicture?.let {
                        /*     Glide.with(this)
                                 .load(uri)
                                 .error(R.drawable.ic_profile_login)
                                 .placeholder(R.drawable.ic_profile_login)
                                 .into(it)

                         */
                        //vipin
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

    private fun profileImageGalleryChooser() {
        ImagePicker.with(this)
            .galleryOnly().crop(4f, 4f) // Crop image (Optional)
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
        val dialog = requireActivity()?.let { Dialog(it, R.style.BottomSheetDialog) }
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

    override fun onResume() {
        super.onResume()
        (activity as? HostMainActivity)?.profileColor()

    }

    override fun onPause() {
        super.onPause()
        firstTime = true
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


    fun dialogRegister(context: Context?) {
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
//            countDownTimer!!.cancel()
//
//            textTimeResend.text = "${"00"}:${"00"} sec"
            countDownTimer!!.start()

            textTimeResend.text = "${"00"}:${"60"} sec"
            if (textTimeResend.text == "${"00"}:${"00"} sec") {
                resendEnabled = true
                textResend.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.scroll_bar_color
                    )
                )
                textResend.isClickable = true
                textResend.isEnabled = true
            } else {
                textResend.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.grey
                    )
                )
                textResend.isClickable = false
                textResend.isEnabled = false
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
                    textResend.isClickable = false
                    textResend.isEnabled = false
                }
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
            var imageCross = findViewById<ImageView>(R.id.imageCross)
            var textSubmitButton = findViewById<TextView>(R.id.textSubmitButton)
            val etMobileNumber = findViewById<EditText>(R.id.etMobileNumber)
            val countyCodePicker = findViewById<CountryCodePicker>(R.id.countyCodePicker)
            etMobileNumber.setText(binding.etPhoneNumeber.text.toString())
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
//                                        etMobileNumber.error = "Mobile required"
                                        showErrorDialog(requireContext(), AppConstant.mobile)
                                        toggleLoginButtonEnabled(true, textSubmitButton)
                                    }else if(!MultipartUtils.isPhoneNumberMatchingCountryCode(etMobileNumber.text.toString(),  countyCodePicker.selectedCountryCodeWithPlus)){
                                        showErrorDialog(requireContext(), AppConstant.validPhoneNumber)
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
            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            show()
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
                    it, phoneNumber, countryCode
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


    private fun toggleLoginButtonEnabled(isEnabled: Boolean, text: TextView) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                text.isEnabled = isEnabled
            } catch (e: Exception) {
                Log.e(ErrorDialog.TAG, "exception toggleLoginButtonEnabled ${e.message}")
            }
        }
    }

    fun isValidName(minLength: Int = 2, maxLength: Int = 15, str: String): Boolean {
        val trimmed = str.trim()
        return trimmed.length in minLength..maxLength && trimmed.matches("^[A-Za-z\\s'-]+$".toRegex())
    }

    private fun dialogChangeName(context: Context?) {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(true)
            setContentView(R.layout.dialog_change_names)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.WRAP_CONTENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
            val imageProfilePicture = findViewById<CircleImageView>(R.id.imageProfilePicture)
            Glide.with(requireContext()).load(userProfileImage).into(imageProfilePicture)

//            userProfile?.profile_image?.let { imagePath ->
//                Glide.with(context)
//                    .asBitmap()
//                    .load(BuildConfig.MEDIA_URL + imagePath)
//                    .into(imageProfilePicture)
//            }




            val textSaveChangesButton = findViewById<TextView>(R.id.textSaveChangesButton)
            val editTextFirstName = findViewById<EditText>(R.id.editTextFirstName)
            val editTextLastName = findViewById<EditText>(R.id.editTextLastName)
            editTextFirstName.setText(firstName)
            editTextLastName.setText(lastName)
            textSaveChangesButton.setOnClickListener {
                if (editTextFirstName.text.isEmpty()) {
                    showErrorDialog(requireContext(), AppConstant.firstName)
                } else if (!isValidName(str = editTextFirstName.text.toString())) {
                    showErrorDialog(
                        requireContext(),
                        "First name should be between 3 and 30 characters long."
                    )
                } else if (editTextLastName.text.isEmpty()) {
                    showErrorDialog(requireContext(), AppConstant.lastName)
                } else if (!isValidName(str = editTextLastName.text.toString())) {
                    showErrorDialog(
                        requireContext(),
                        "Last name should be between 3 and 30 characters long."
                    )
                } else {
                    toggleLoginButtonEnabled(false, textSaveChangesButton)
                    updateName(
                        editTextFirstName.text.toString(),
                        editTextLastName.text.toString(),
                        dialog, textSaveChangesButton
                    )
                }
            }

            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            show()
        }
    }


    private fun dialogEmailVerificationProfile(context: Context?) {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(true)
            setContentView(R.layout.dialog_email_verification)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.WRAP_CONTENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
            var imageCross = findViewById<ImageView>(R.id.imageCross)

            var etEmail = findViewById<EditText>(R.id.etEmail)

            var textSubmitButton = findViewById<TextView>(R.id.textSubmitButton)
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
                                        emailVerificationProfile(
                                            session?.getUserId().toString(),
                                            etEmail.text.toString(), dialog, textSubmitButton
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

    private fun emailVerificationProfile(
        userId: String,
        email: String,
        dialog: Dialog,
        textLoginButton: TextView
    ) {
        lifecycleScope.launch {
            profileViewModel.emailVerificationProfile(
                userId,
                email
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            dialog.dismiss()
                            val textHeaderOfOtpVerfication =
                                "Please type the verification code send \nto $email"
                            dialogOtpVerification(
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

    @SuppressLint("SuspiciousIndentation", "CutPasteId", "SetTextI18n")
    fun dialogOtpVerification(
        context: Context, code: String, number: String,
        textHeaderOfOtpVerfication: String, type: String
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
//            countDownTimer!!.cancel()
//
//            textTimeResend.text = "${"00"}:${"00"} sec"
            countDownTimer!!.start()

            textTimeResend.text = "${"00"}:${"60"} sec"

            if (textTimeResend.text == "${"00"}:${"00"} sec") {
                resendEnabled = true
                textResend.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.scroll_bar_color
                    )
                )
                textResend.isClickable = true
                textResend.isEnabled = true
            } else {
                textResend.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.grey
                    )
                )
                textResend.isClickable = false
                textResend.isEnabled = false
            }

            textSubmitButton.setOnClickListener {
                toggleLoginButtonEnabled(false, textSubmitButton)
                lifecycleScope.launch {
                    profileViewModel.networkMonitor.isConnected
                        .distinctUntilChanged()
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
                                            otpVerifyPhoneVerificationProfile(
                                                session?.getUserId().toString(),
                                                otp,
                                                dialog,
                                                textSubmitButton
                                            )
                                        }
                                        if ("email".equals(type)) {
                                            otpVerifyEmailVerificationProfile(
                                                session?.getUserId().toString(),
                                                otp,
                                                dialog,
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
                                        resendEmailVerificationProfile(
                                            userId, number, textResend,
                                            rlResendLine, incorrectOtp, textTimeResend
                                        )
                                    }
                                }
                                if ("mobile".equals(type)) {
                                    resendPhoneVerificationProfile(
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

    private fun otpVerifyPhoneVerificationProfile(
        userId: String, otp: String,
        dialog: Dialog, text: TextView
    ) {
        lifecycleScope.launch {
            profileViewModel.otpVerifyPhoneVerificationProfile(
                userId,
                otp,
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            binding.textConfirmNow1.visibility = GONE
                            binding.textVerified1.visibility = View.VISIBLE
                            dialog.dismiss()
                        }

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
                        Log.v(ErrorDialog.TAG, "error::" + it.message)
                    }
                }
            }
        }

    }

    private fun otpVerifyEmailVerificationProfile(
        userId: String, otp: String,
        dialog: Dialog, text: TextView
    ) {
        lifecycleScope.launch {
            profileViewModel.otpVerifyEmailVerificationProfile(
                userId,
                otp,
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->

                            binding.textConfirmNow.visibility = GONE
                            binding.textVerified.visibility = View.VISIBLE
                            dialog.dismiss()
                        }

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
                        Log.v(ErrorDialog.TAG, "error::" + it.message)
                    }
                }
            }
        }

    }

    private fun resendPhoneVerificationProfile(
        userId: String,
        code: String,
        number: String,
        textResend: TextView,
        rlResendLine: RelativeLayout,
        incorrectOtp: TextView,
        textTimeResend: TextView
    ) {
        lifecycleScope.launch {
            profileViewModel.phoneVerificationProfile(
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
                            textResend.isClickable = false
                            textResend.isEnabled = false
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

    private fun resendEmailVerificationProfile(
        userId: String,
        email: String,
        textResend: TextView,
        rlResendLine: RelativeLayout,
        incorrectOtp: TextView,
        textTimeResend: TextView
    ) {
        lifecycleScope.launch {
            profileViewModel.emailVerificationProfile(
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
                            textResend.isClickable = false
                            textResend.isEnabled = false
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
            var imageCross = findViewById<ImageView>(R.id.imageCross)

            var etEmail = findViewById<EditText>(R.id.etEmail)
            etEmail.setText(binding.etEmail.text.toString())
            var textSubmitButton = findViewById<TextView>(R.id.textSubmitButton)
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
//                                        etEmail.error = "Email Address required"
                                        showErrorDialog(requireContext(), AppConstant.email)
                                        toggleLoginButtonEnabled(true, textSubmitButton)
                                    } else if (!isValidEmail(etEmail.text.toString())) {
//                                        etEmail.error = "Invalid Email Address"
                                        showErrorDialog(requireContext(), AppConstant.invalideemail)
                                        toggleLoginButtonEnabled(true, textSubmitButton)
                                    } else {
                                        emailVerification(
                                            SessionManager(requireContext()).getUserId().toString(),
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

//    private fun dialogEmailVerification(context: Context?) {
//        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
//        dialog?.apply {
//            setCancelable(false)
//            setContentView(R.layout.dialog_email_verification)
//            window?.attributes = WindowManager.LayoutParams().apply {
//                copyFrom(window?.attributes)
//                width = WindowManager.LayoutParams.MATCH_PARENT
//                height = WindowManager.LayoutParams.MATCH_PARENT
//            }
//            val imageCross = findViewById<ImageView>(R.id.imageCross)
//
//            val etEmail = findViewById<EditText>(R.id.etEmail)
//            etEmail.setText(binding.etEmail.text.toString())
//
//            val textSubmitButton = findViewById<TextView>(R.id.textSubmitButton)
//            textSubmitButton.setOnClickListener {
//                toggleLoginButtonEnabled(false, textSubmitButton)
//                lifecycleScope.launch {
//                    profileViewModel.networkMonitor.isConnected
//                        .distinctUntilChanged() // Ignore duplicate consecutive values
//                        .collect { isConn ->
//                            if (!isConn) {
//                                showErrorDialog(
//                                    requireContext(),
//                                    resources.getString(R.string.no_internet_dialog_msg)
//                                )
//                                toggleLoginButtonEnabled(true, textSubmitButton)
//                            } else {
//                                lifecycleScope.launch(Dispatchers.Main) {
//                                    if (etEmail.text!!.isEmpty()) {
//                                        etEmail.error = "Email Address required"
//                                        showErrorDialog(requireContext(), AppConstant.email)
//                                        toggleLoginButtonEnabled(true, textSubmitButton)
//                                    } else {
//                                        emailVerification(
//                                            session?.getUserId().toString(),
//                                            etEmail.text.toString(),
//                                            dialog,
//                                            textSubmitButton
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                }
//            }
//            imageCross.setOnClickListener {
//                dismiss()
//            }
//            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            show()
//        }
//    }

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
//            countDownTimer!!.cancel()
//
//            textTimeResend.text = "${"00"}:${"00"} sec"
            countDownTimer!!.start()

            textTimeResend.text = "${"00"}:${"60"} sec"
            if (textTimeResend.text == "${"00"}:${"00"} sec") {
                resendEnabled = true
                textResend.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.scroll_bar_color
                    )
                )
                textResend.isClickable = true
                textResend.isEnabled = true
            } else {
                textResend.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.grey
                    )
                )
                textResend.isClickable = false
                textResend.isEnabled = false
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
                                                session?.getUserId().toString(),
                                                otp,
                                                dialog,
                                                textSubmitButton
                                            )
                                        }
                                        if ("email".equals(type)) {
                                            otpVerifyEmailVerification(
                                                session?.getUserId().toString(),
                                                otp,
                                                dialog,
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
                                            session?.getUserId().toString(), number, textResend,
                                            rlResendLine, incorrectOtp, textTimeResend
                                        )
                                    }
                                }
                                if ("mobile".equals(type)) {
                                    resendPhoneVerification(
                                        session?.getUserId().toString(), code, number, textResend,
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

    private fun otpVerifyPhoneVerification(
        userId: String, otp: String, dialog: Dialog, text: TextView
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
                            getUserProfile()
                            dialog.dismiss()
                            showSuccessDialog(requireContext(), resp)
                        }

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
                            getUserProfile()
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
                            textResend.isClickable = false
                            textResend.isEnabled = false
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

            profileViewModel.emailVerification(userId, email).collect {
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
                                ContextCompat.getColor(requireContext(), R.color.grey)
                            )
                            textResend.isClickable = false
                            textResend.isEnabled = false
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
                    textResend.isClickable = true
                    textResend.isEnabled = true
                } else {
                    textResend.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.grey
                        )
                    )
                    textResend.isClickable = false
                    textResend.isEnabled = false
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
                val imgCorrectSignPassword = findViewById<ImageView>(R.id.imgCorrectSign)
                val imgWrongSignPassword = findViewById<ImageView>(R.id.imgWrongSign)
                val imgCorrectSignConfirm = findViewById<ImageView>(R.id.imgCorrectSign1)
                val imgWrongSignConfirm = findViewById<ImageView>(R.id.imgWrongSign1)
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


                findViewById<TextView>(R.id.textSubmitButton)?.setOnClickListener {
                    val password = etPassword.text.toString().trim()
                    val confirmPassword = etConfirmPassword.text.toString().trim()
                    if (!password.equals(confirmPassword)) {
                        LoadingUtils.showErrorDialog(
                            requireContext(),
                            "Password and confirm password should be same"
                        )
                        return@setOnClickListener
                    }

//                    when {
//                        password.isEmpty() -> showErrorDialog(ctx, "Enter Password")
//                        confirmPassword.isEmpty() -> showErrorDialog(ctx, "Enter Confirm Password")
//                        password != confirmPassword -> showErrorDialog(ctx, "Password not match")
//                        else -> {
//                            dismiss()
//                            callingOtpFetchApi(password, confirmPassword)
//
//                            // Dismiss the dialog after successful API call
//                        }
//                    }

                    if (password.isEmpty()) {
                        showErrorDialog(ctx, "Enter Password")
                    } else if (!isValidPassword(password)){
                        showErrorDialog(
                            requireContext(), passwordMustConsist
                        )
                    }
                    else if (confirmPassword.isEmpty()) {
                        showErrorDialog(ctx, "Enter Confirm Password")
                    } else if (password != confirmPassword) {
                        showErrorDialog(ctx, "Password not match")
                    } else {

                        dismiss() // Dismiss the dialog after successful API call
                        callingOtpFetchApi(password, confirmPassword)
                    }
                }

                show()
            }
        }
    }


    private fun callingOtpFetchApi(password: String, confirmPassword: String) {
        lifecycleScope.launch {
            LoadingUtils.showDialog(requireContext(), true)
            SessionManager(requireContext()).getUserId()
                ?.let {
                    profileViewModel.otpResetPassword(it).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                LoadingUtils.hideDialog()
                                dialogOtp(
                                    requireContext(),
                                    it.data?.first.toString(),
                                    it.data?.second.toString(),
                                    password
                                )
                            }

                            is NetworkResult.Error -> {
                                LoadingUtils.hideDialog()
                                LoadingUtils.showErrorDialog(
                                    requireContext(),
                                    it.message.toString()
                                )
                            }

                            else -> {

                            }

                        }
                    }
                }
        }
    }


    fun dialogOtp(context: Context, otp: String, otpType: String, password: String) {

        val dialog = Dialog(context, R.style.BottomSheetDialog)

        dialog.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_otp_verification)
            var str = "Please type the verification code send \nto" + " " + otpType

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
            textEnterYourEmail.text = str
            rlResendLine.visibility = GONE

            imageCross.setOnClickListener {
                dialog.dismiss()
            }

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
                        s: CharSequence, start: Int, count: Int, after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence, start: Int, before: Int,
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

            startCountDownTimer(requireContext(), textTimeResend, rlResendLine, textResend)

//            countDownTimer!!.cancel()
//
//            textTimeResend.text = "${"00"}:${"00"} sec"
            countDownTimer!!.start()

            textTimeResend.text = "${"00"}:${"60"} sec"

            if (textTimeResend.text == "${"00"}:${"00"} sec") {
                resendEnabled = true
                textResend.setTextColor(
                    ContextCompat.getColor(context, R.color.scroll_bar_color)
                )
                textResend.isClickable = true
                textResend.isEnabled = true
            } else {
                textResend.setTextColor(ContextCompat.getColor(context, R.color.grey))
                textResend.isClickable = false
                textResend.isEnabled = false
            }

            textSubmitButton.setOnClickListener {
                var enteredOtp = ""
                var count = 0;
                otpDigits.forEach {
                    enteredOtp += it.text.toString().trim()
                }
                if (enteredOtp == otp) {
                    updatePasswordApi(password, password, it)
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Otp not match", Toast.LENGTH_LONG).show()
                }
            }
        }
        dialog.show()
    }


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
//                if (text == "Your account is registered \nsuccessfully") {
//
//                    Log.d("Navigation", "Navigating to turnNotificationsFragment")
//                    navController?.navigate(R.id.turnNotificationsFragment)
//
//                } else if (text == "Your password has been changed\n" + " successfully.") {
//                    dialogLoginEmail(context)
//                }
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
//                var sessionManager = SessionManager(context)
//                sessionManager.setUserId(-1)
//                var intent = Intent(context, AuthActivity::class.java)
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

    private fun showPopupHostInfoWindow(anchorView: View) {
        val popupView = LayoutInflater.from(context).inflate(R.layout.pop_up_info, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            elevation = 8f
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        // Optional: Slight X offset to align with anchor
        val xOffset = -popupView.measuredWidth / 2 + anchorView.width / 2
        val yOffset = 8  // slight space between icon and popup

        popupWindow.showAsDropDown(anchorView, xOffset, yOffset)
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
                Log.d("WorkDelete", "i'm here 1")
                if (obj < workList.size - 1) {
                    Log.d("WorkDelete", "i'm here 2")
                    deleteMyWork(obj)
                    workList.removeAt(obj)
                    addWorkAdapter.updateWork(workList)
                }
            }

            "language" -> {
                if (obj < languageList.size - 1) {
                    val removedLang = languageList[obj].name
                    deleteLanguageApi(obj)
                }
            }

            "Hobbies" -> {
                if (obj < hobbiesList.size - 1) {
//                    deleteHobbiesApi(obj)
//                    hobbiesList.removeAt(obj)
//                    addHobbiesAdapter.updateHobbies(hobbiesList)
                }

            }

            "Pets" -> {
                if (obj < petsList.size - 1) {
//                    deletePetsApi(obj)
//                    petsList.removeAt(obj)
//                    addPetsAdapter.updatePets(petsList)
                }
            }


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


                                    binding.tvEdit.text = "Edit"
                                    binding.imageEditAboutIcon.visibility = View.VISIBLE
                                    binding.imageSaveAboutIcon.visibility = GONE
                                    binding.etAboutMeText.isEnabled = false
                                    editAboutButton = true

                                    showSuccessDialog(requireContext(), resp.first)
                                    userProfile?.about_me = about_me
                                    Log.d("checkAboutMe","about_me"+about_me+"1")
                                    //binding.user = userProfile
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
                                    session?.setUserImage(resp.third)
                                    Glide.with(requireContext()).load(BuildConfig.MEDIA_URL + resp.third).into( (activity as HostMainActivity).binding.imageProfile)
                                    (activity as HostMainActivity).showImage()
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
                                    firstName = first_name
                                    lastName = last_name
                                    userProfile?.name = first_name + " " + last_name
                                    session?.setName(first_name + " " + last_name)
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
        Log.d("WorkDelete", "i'm here 3")
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
                                                SessionManager(requireContext()).removeLanguage(
                                                    requireActivity(),
                                                    languageList.get(index).name
                                                )
                                                languageList.removeAt(index)
                                                addLanguageSpeakAdapter.updateLanguage(languageList)
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


    @SuppressLint("SetTextI18n")
    private fun addLivePlace(place_name: String) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                lifecycleScope.launch {
                    profileViewModel.addLivePlace(
                        session?.getUserId().toString(),
                        place_name
                    ).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                it.data?.let { resp ->

                                    showSuccessDialog(requireContext(), resp.first)
                                    val newLocation =
                                        AddLocationModel(place_name ?: "Unknown Location")

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

    private fun updatePasswordApi(password: String, confirmPassword: String, view: View) {
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

    private fun logout() {
        lifecycleScope.launch {
            profileViewModel.logout(session?.getUserId().toString()).collect {
                when (it) {

                    is NetworkResult.Success -> {
                        showSuccessDialog(requireContext(), it.data!!)
                        val sessionManager = SessionManager(requireContext())
                        sessionManager.logOut()
                        sessionManager.setUserId(-1)
                        sessionManager.setUserImage("")
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

    private fun getPayoutMethods() {
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
                        getPayoutApi()
                    }

                }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun getPayoutApi() {
        lifecycleScope.launch {
            profileViewModel.getPayoutMethods(session?.getUserId().toString()).collect {
                when (it) {

                    is NetworkResult.Success -> {
                        val model = Gson().fromJson(it.data, GetPayoutResponse::class.java)

                        Log.d("API_RESPONSE_Payout", it.data.toString())


                        model.data?.let { payoutData ->
                            payoutData.bankAccounts?.let { bankList ->
                                if (bankList.isNotEmpty()) {
                                    bankNameAdapterPayout.addItems(bankList)
                                    bankNameAdapterPayout.notifyDataSetChanged()
                                    binding.recyclerViewPaymentCardListPayOut.visibility =
                                        View.VISIBLE
                                    binding.textBankNoDataFound.visibility = GONE
                                    if (bankNameAdapterPayout != null) {
                                        bankNameAdapterPayout.notifyDataSetChanged()
                                    }
                                } else {
                                    binding.recyclerViewPaymentCardListPayOut.visibility = GONE
                                    binding.textBankNoDataFound.visibility = View.VISIBLE
                                }
                            }

                            payoutData.cards?.let { cardList ->
                                if (cardList.isNotEmpty()) {
                                    cardNumberAdapterPayout.addItems(cardList)
                                    cardNumberAdapterPayout.notifyDataSetChanged()
                                    binding.recyclerViewCardNumberListPayOut.visibility =
                                        View.VISIBLE
                                    binding.textCardNoDataFound.visibility = GONE

                                    Log.d("cardList", "cardListImhere")

                                    if (cardNumberAdapterPayout != null) {
                                        bankNameAdapterPayout.notifyDataSetChanged()
                                    }

                                } else {
                                    binding.recyclerViewCardNumberListPayOut.visibility = GONE
                                    binding.textCardNoDataFound.visibility = View.VISIBLE
                                }
                            }
                        } ?: run {
                            // Handle case where `data` is null
                            // showErrorDialog(requireContext(), "Payout data is unavailable")
                        }


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


    private fun setPrimaryPayoutMethods(id: String, type: String, position: Int) {
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
                        setPrimaryPayoutApi(id,type,position)
                    }

                }
        }
    }


    private fun setPrimaryPayoutApi(id: String, type: String, position: Int) {
        Log.d("idType", id)
        lifecycleScope.launch {
            profileViewModel.setPrimaryPayoutMethod(session?.getUserId().toString(), id).collect {
                when (it) {

                    is NetworkResult.Success -> {
                        it.data?.let { it1 -> showSuccessDialog(requireContext(), it1) }

                        when(type){
                            "setPrimary"->{
                                bankListPayout.forEach { card ->
                                    card.defaultForCurrency = false
                                }
                                bankListPayout[position].defaultForCurrency = true

                                bankNameAdapterPayout.updateItem(bankListPayout)
                                bankNameAdapterPayout.notifyDataSetChanged()
                                cardListPayout.forEach { card ->
                                    card.defaultForCurrency = false
                                }
                                cardNumberAdapterPayout.updateItem(cardListPayout)
                                cardNumberAdapterPayout.notifyDataSetChanged()
                            }
                            "setPrimaryCard"->{
                                cardListPayout.forEach { card ->
                                    card.defaultForCurrency = false
                                }
                                cardListPayout[position].defaultForCurrency = true

                                bankListPayout.forEach { card ->
                                    card.defaultForCurrency = false
                                }
//                                bankListPayout[position].defaultForCurrency = true

                                bankNameAdapterPayout.updateItem(bankListPayout)
                                bankNameAdapterPayout.notifyDataSetChanged()

                                cardNumberAdapterPayout.updateItem(cardListPayout)
                                cardNumberAdapterPayout.notifyDataSetChanged()
                            }
                        }


                 // getPayoutApi()

                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(requireContext(), it.message!!)

                    }

                    is NetworkResult.Loading -> {}

                }
            }


        }
    }

    private fun deletePayoutMethods(id: String, type: String, position: Int) {
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
                        deletePayoutApi(id,type,position)
                    }

                }
        }
    }


    private fun deletePayoutApi(id: String, type: String,position: Int) {
        Log.d("idType", id)
        lifecycleScope.launch {
            profileViewModel.deletePayoutMethod(session?.getUserId().toString(), id).collect {
                when (it) {

                    is NetworkResult.Success -> {
                        it.data?.let { it1 -> showSuccessDialog(requireContext(), it1) }

                        when (type) {
                            "deleteCard" -> {
                                // Card list se remove karein
                                if (position < cardListPayout.size) {
                                    cardListPayout.removeAt(position)
                                    cardNumberAdapterPayout.updateItem(cardListPayout)

                                    // Visibility update karein
                                    if (cardListPayout.isEmpty()) {
                                        binding.recyclerViewCardNumberListPayOut.visibility = View.GONE
                                        binding.textCardNoDataFound.visibility = View.VISIBLE
                                    }
                                }
                            }
                            "delete" -> {
                                // Bank list se remove karein
                                if (position < bankListPayout.size) {
                                    bankListPayout.removeAt(position)
                                    bankNameAdapterPayout.updateItem(bankListPayout)
                                    // Visibility update karein
                                    if (bankListPayout.isEmpty()) {
                                        binding.recyclerViewPaymentCardListPayOut.visibility = View.GONE
                                        binding.textBankNoDataFound.visibility = View.VISIBLE
                                    }
                                }
                            }
                        }
                     //  getPayoutApi()

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


    private fun dialogAddCardGuest() {
        if (cardDialog?.isShowing == true) {
            return // Dialog is already showing, don't open another one
        }
        var street_address = ""
        var city = ""
        var state = ""
        var zip_code = ""
        var dateManager = DateManager(requireContext())
        cardDialog = Dialog(requireContext(), R.style.BottomSheetDialog)
        cardDialog?.apply {

            setContentView(R.layout.dialog_add_card_details)
            setCancelable(false)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.WRAP_CONTENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
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
            val cross: ImageView = findViewById(R.id.img_cross)
            etAddress = etStreet
            etCity1 = etCity
            zipcode = etZipCode
            etState1 = etState
            cross.setOnClickListener {
                dismiss()
            }
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
            locationSelection(etStreet)
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
                }else if(etCardHolderName.text.toString().length >30){
                    showToast(requireContext(), "Please Enter Card Holder Name less than 30 character")
                }
                else if (etCardNumber.text.trim().isEmpty()) {
                    showToast(requireContext(), AppConstant.cardNubmer)
                }else if (!ErrorDialog.isValidCardNumber(etCardNumber.text.toString())) {
                    showToast(requireContext(), AppConstant.cardValidNubmer)
                }
                else if (textMonth.text.isEmpty()) {
                    showToast(requireContext(), AppConstant.cardMonth)
                } else if (textYear.text.isEmpty()) {
                    showToast(requireContext(), AppConstant.cardYear)
                } else if (etCardCvv.text.trim().isEmpty()) {
                    showToast(requireContext(), AppConstant.cardCVV)
                } else {
                    LoadingUtils.showDialog(requireContext(), false)
                    val stripe = Stripe(requireContext(), BuildConfig.STRIPE_KEY)
                    var month: Int? = null
                    var year: Int? = null
                    // val cardNumber: String =
                    //   Objects.requireNonNull(etCardNumber.text.toString().trim()).toString()
                    //vipin
                    val cardNumber: String =
                        Objects.requireNonNull(etCardNumber.text.toString().replace(" ", "").trim())
                            .toString()
                    Log.d("checkCardNumber", cardNumber)


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

                                saveCardStripe(cardDialog!!, id, checkBox.isChecked)
                                LoadingUtils.hideDialog()
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
            showErrorDialog(
                requireContext(),
                resources.getString(R.string.no_internet_dialog_msg)
            )
            onAddressReceived(null)
        }
    }

    private fun getUserCards() {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                profileViewModel.getUserCards(session?.getUserId().toString()).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                customerId = resp.get("stripe_customer_id").asString
                                firstTime = false
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
            showErrorDialog(requireContext(), resources.getString(R.string.no_internet_dialog_msg))
        }
    }

     fun setPrimary(position: Int) {
         if (NetworkMonitorCheck._isConnected.value) {
             lifecycleScope.launch(Dispatchers.Main) {
                 profileViewModel.setPreferredCard(
                     session?.getUserId().toString(),
                     userCardsList[position].card_id
                 ).collect {
                     when (it) {
                         is NetworkResult.Success -> {
                             it.data?.let { resp ->
                                 userCardsList.forEach { card ->
                                     card.is_preferred = false
                                 }
                                 userCardsList[position].is_preferred = true
                                 selectuserCard = userCardsList[position]
                                 addPaymentCardAdapter.updateItem(userCardsList)
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
    fun locationSelection(etStreet : EditText) {
        etStreet.setOnClickListener {

            val apiKey = getString(R.string.api_key_location)
            if (!Places.isInitialized()) {
                Places.initialize(requireContext(), apiKey)
            }

            val fields: List<Place.Field> = Arrays.asList<Place.Field>(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )

            val intent: Intent =
                Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(requireContext())
            startActivityForResult(intent, 103)
        }

        etStreet.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val apiKey = getString(R.string.api_key_location)
                if (!Places.isInitialized()) {
                    Places.initialize(requireContext(), apiKey)
                }
                val fields: List<Place.Field> = Arrays.asList<Place.Field>(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG
                )

                val intent: Intent =
                    Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(requireContext())
                startActivityForResult(intent, 103)
            }
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (resultCode == Activity.RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data)
            //  Toast.makeText(this, "ID: " + place.getId() + "address:" + place.getAddress() + "Name:" + place.getName() + " latlong: " + place.getLatLng(), Toast.LENGTH_LONG).show();
            val addressComponents = place.addressComponents?.asList()
            var address: String = place.address
            // do query with address

            val latLng = place.latLng

            latitude = latLng.latitude.toString()
            longitude = latLng.longitude.toString()
            val location = LatLng(latitude.toDouble(), longitude.toDouble())
            // Move the camera to the specified location
            fetchAddressDetails(address,latitude.toDouble(), longitude.toDouble())
            etCity1?.isEnabled = true
            if (latitude == null) {
                latitude = "0.0001"
            }

            if (longitude == null) {
                longitude = "0.0001"
            }


            var add = address
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            etCity1?.isEnabled = true
            // TODO: Handle the error.
            val status = Autocomplete.getStatusFromIntent(data)
            Toast.makeText(requireContext(), "Error: " + status.statusMessage, Toast.LENGTH_LONG)
                .show()
        }

    }

    private fun fetchAddressDetails(address:String,latitude: Double, longitude: Double) {
        // Launching a coroutine to run the geocoding task in the background
        lifecycleScope.launch {
            try {
                val addressDetails = withContext(Dispatchers.IO) {
                    LocationManager(requireContext()).getAddressFromCoordinates(latitude, longitude)
                }
                etAddress?.setText(address)
                etAddress?.text?.length?.let { etAddress?.setSelection(it) }
                etCity1?.setText(addressDetails.city)
                zipcode?.setText(addressDetails.postalCode)

                etState1?.setText(addressDetails.state)



            } catch (e: Exception) {
                Log.e("Geocoder", "Error fetching address: ${e.message}")
                Toast.makeText(
                    requireContext(),
                    "Unable to fetch address details",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

     override fun itemClickCard(pos: Int, type: String) {
         val cardIdSelect=userCardsList[pos].card_id
         when (type){
             "delete" ->{
                 deleteCardMethods(cardIdSelect,pos)
             }
             "primary" ->{
                 setPrimary(pos)
             }
         }
     }

     private fun deleteCardMethods(id: String,pos:Int) {
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
                         deleteCard(id,pos)
                     }

                 }
         }
     }

     private fun deleteCard(id: String,position: Int) {
         Log.d("idType", id)
         lifecycleScope.launch {
             profileViewModel.deleteCard(session?.getUserId().toString(), id).collect {
                 when (it) {
                     is NetworkResult.Success -> {
                         userCardsList.removeAt(position)
                         addPaymentCardAdapter.updateItem(userCardsList)
                         it.data?.let { it1 -> showSuccessDialog(requireContext(), it1) }
                         if (userCardsList.isNotEmpty()){
                             binding.recyclerViewPaymentCardList.visibility = View.VISIBLE
                         }else{
                             binding.recyclerViewPaymentCardList.visibility = View.GONE
                         }
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
 }


