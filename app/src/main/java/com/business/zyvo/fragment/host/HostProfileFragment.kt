package com.business.zyvo.fragment.host

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
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
import com.bumptech.glide.request.target.SimpleTarget
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
import com.business.zyvo.NetworkResult
import com.business.zyvo.OnClickListener1
import com.business.zyvo.OnLocalListener
import com.business.zyvo.R
import com.business.zyvo.activity.AuthActivity
import com.business.zyvo.activity.GuesMain
import com.business.zyvo.activity.PlaceOpenActivity
import com.business.zyvo.adapter.AddHobbiesAdapter
import com.business.zyvo.adapter.AddLanguageSpeakAdapter
import com.business.zyvo.adapter.AddLocationAdapter
import com.business.zyvo.adapter.AddPetsAdapter
import com.business.zyvo.adapter.AddWorkAdapter
import com.business.zyvo.adapter.host.BankNameAdapter
import com.business.zyvo.adapter.host.CardNumberAdapter
import com.business.zyvo.adapter.selectLanguage.LocaleAdapter
import com.business.zyvo.databinding.FragmentHostProfileBinding
import com.business.zyvo.fragment.both.completeProfile.HasName
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
import com.business.zyvo.utils.MediaUtils
import com.business.zyvo.utils.NetworkMonitorCheck
import com.business.zyvo.viewmodel.PaymentViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale


@AndroidEntryPoint

class HostProfileFragment : Fragment(),OnClickListener1, onItemClickData, OnClickListener {
    lateinit var binding :FragmentHostProfileBinding

    private lateinit var commonAuthWorkUtils: CommonAuthWorkUtils
    private lateinit var addLocationAdapter: AddLocationAdapter
    private lateinit var addWorkAdapter: AddWorkAdapter
    private lateinit var addLanguageSpeakAdapter: AddLanguageSpeakAdapter
    private lateinit var addHobbiesAdapter: AddHobbiesAdapter
    private lateinit var dateManager: DateManager
    private lateinit var bankNameAdapter: BankNameAdapter
    private lateinit var cardNumberAdapter: CardNumberAdapter
    private lateinit var addPetsAdapter: AddPetsAdapter
    var userProfile: UserProfile? = null
    var imageBytes: ByteArray = byteArrayOf()

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
    private var isDropdownOpen = false
    lateinit var navController: NavController
    private lateinit var otpDigits: Array<EditText>
    private var countDownTimer: CountDownTimer? = null

    var resendEnabled = false
    var otpValue: String = ""


    private val list1 = mutableListOf<CountryLanguage>()
    private val list2 = mutableListOf<CountryLanguage>()


    // For handling the result of the Autocomplete Activity
    private val startAutocomplete =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    val place = Autocomplete.getPlaceFromIntent(intent)
                    etSearch?.text = place.name
                    val newLocation = AddLocationModel(place.name ?: "Unknown Location")

                    // Add the new location to the list and notify the adapter
                    locationList.add(0, newLocation)

                    //   Toast.makeText(requireContext(),"count${locationList.size}",Toast.LENGTH_LONG).show()

//                    // Check if we need to hide the "Add New" button

                    //  addLocationAdapter.updateLocations(locationList)  // Notify adapter here
                    addLocationAdapter.notifyItemInserted(0)
                    //  addLocationAdapter.notifyItemInserted(locationList.size - 1)

                    Log.i(TAG, "Place: ${place.name}, ${place.id}")
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
                Log.i(TAG, "User canceled autocomplete")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navController = findNavController()
        commonAuthWorkUtils = CommonAuthWorkUtils(requireContext(), navController)
        apiKey = getString(R.string.api_key)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHostProfileBinding.inflate(
            LayoutInflater.from(requireContext()),
            container,
            false
        )
        dateManager = DateManager(requireContext())
        // Inflate the layout for this fragment
        val newLocation = AddLocationModel("Unknown Location")
        locationList.add(newLocation)
        val newWork = AddWorkModel("Unknown Location")
        workList.add(newWork)
        val newLanguage = AddLanguageModel("Unknown Location")
        languageList.add(newLanguage)
        val newHobbies = AddHobbiesModel("Unknown Location")
        hobbiesList.add(newHobbies)
        val newPets = AddPetsModel("Unknown Location")
        petsList.add(newPets)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textGiveFeedback.setOnClickListener(this)
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
        // Initialize Places API if not already initialized
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity(), apiKey)
        }

        // Set listeners

//        binding.filterIcon.setOnClickListener {
//        startActivity(Intent(requireActivity(), FiltersActivity::class.java))
//        }
//
//        binding.rlFind.setOnClickListener {
//            startActivity(Intent(requireActivity(), WhereTimeActivity::class.java))
//        }


        binding.switchHost.setOnClickListener {
            val session = SessionManager(requireContext())
            session.setCurrentPanel(AppConstant.Guest)
            val intent = Intent(requireContext(), GuesMain::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            requireActivity().finish()
        }
        binding.textAddNew.setOnClickListener {
            findNavController().navigate(R.id.payoutFragment)
        }

        binding.textAddNewPaymentMethod.setOnClickListener {
            findNavController().navigate(R.id.payoutFragment)
        }
        getUserProfile()

    }


    // Function to initialize the adapter for adding locations
    private fun adapterInitialize() {
        addLocationAdapter = AddLocationAdapter(requireContext(), locationList, this,this)

        binding.recyclerViewLocation.adapter = addLocationAdapter

        // Update the adapter with the initial location list (if any)

        addLocationAdapter.updateLocations(locationList)

        addWorkAdapter = AddWorkAdapter(requireContext(), workList, this,this)

        binding.recyclerViewWork.adapter = addWorkAdapter

        addWorkAdapter.updateWork(workList)

        addLanguageSpeakAdapter = AddLanguageSpeakAdapter(requireContext(), languageList, this)
        binding.recyclerViewlanguages.adapter = addLanguageSpeakAdapter

        addLanguageSpeakAdapter.updateLanguage(languageList)


//        addHobbiesAdapter = AddHobbiesAdapter(requireContext(), hobbiesList, this)
//        binding.recyclerViewHobbies.adapter = addHobbiesAdapter
//
//        addHobbiesAdapter.updateHobbies(hobbiesList)
//
//
//        addPetsAdapter = AddPetsAdapter(requireContext(), petsList, this)
//        binding.recyclerViewPets.adapter = addPetsAdapter

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

        addPetsAdapter = AddPetsAdapter(requireContext(), petsList, this,this)

    }


    private fun getUserProfile() {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                LoadingUtils.showDialog(requireContext(),false)
                var session = SessionManager(requireContext())
                profileViewModel.getUserProfile(session?.getUserId().toString()).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            var name = ""
                            LoadingUtils.hideDialog()
                            it.data?.let { resp ->
                                Log.d(
                                    "TESTING_PROFILE", "HERE IN A USER PROFILE ," + resp.toString()
                                )
                                userProfile = Gson().fromJson(resp, UserProfile::class.java)
                                userProfile.let {
                                    if (it?.first_name != null && it.last_name != null) {
                                        name =
                                            it.first_name + " " + it.last_name
                                    }
                                    it?.name = name
                                    binding.user = it

                                    it?.email?.let {
                                        binding.etEmail.setText(it)
                                        binding.etEmail.isEnabled = false
                                    }
                                    it?.phone_number?.let {
                                       binding.etPhoneNumeber.setText(it)
                                        binding.etPhoneNumeber.isEnabled = false
                                    }
                                    it?.street?.let {
                                        binding.etStreet.setText(it)
                                    }

                                    it?.state?.let {
                                        binding.etState.setText(it)
                                    }

                                    it?.zip_code?.let {
                                        binding.etZipcode.setText(it)
                                    }

                                    it?.city?.let{
                                        binding.etcity.setText(it)
                                         }

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
                                        val newLanguage =
                                            AddLocationModel(AppConstant.unknownLocation)
                                        locationList.add(newLanguage)
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
                                        val newLanguage = AddLanguageModel(AppConstant.unknownLocation)
                                        languageList.add(newLanguage)
                                        addLanguageSpeakAdapter.updateLanguage(languageList)
                                    }
//                                    if (it?.hobbies != null && it.hobbies.isNotEmpty()) {
//                                        hobbiesList = getObjectsFromNames(it.hobbies) { name ->
//                                            AddHobbiesModel(name)  // Using the constructor of MyObject to create instances
//                                        }
//                                        val newLanguage =
//                                            AddHobbiesModel(AppConstant.unknownLocation)
//                                        hobbiesList.add(newLanguage)
//                                        addHobbiesAdapter.updateHobbies(hobbiesList)
//                                    }
//                                    if (it?.pets != null && it.pets.isNotEmpty()) {
//                                        petsList = getObjectsFromNames(it.pets) { name ->
//                                            AddPetsModel(name)  // Using the constructor of MyObject to create instances
//                                        }
//                                        val newLanguage = AddPetsModel(AppConstant.unknownLocation)
//                                        petsList.add(newLanguage)
//                                        addPetsAdapter.updatePets(petsList)
//                                    }
                                }
                            }
                        }

                        is NetworkResult.Error -> {
                            LoadingUtils.hideDialog()
                            showErrorDialog(requireContext(), it.message!!)
                        }

                        else -> {
                            LoadingUtils.hideDialog()
                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                        }

                    }
                }
            }
        } else {
            LoadingUtils.hideDialog()
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


    // Function to start the location picker using Autocomplete
    private fun startLocationPicker() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .build(requireContext())
        startAutocomplete.launch(intent)
    }

    // Display a dialog to select a language
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

                    // Add the new location to the list and notify the adapter
                    languageList.add(0, newLanguage)
                    //  addLocationAdapter.updateLocations(locationList)  // Notify adapter here
                    addLanguageSpeakAdapter.notifyItemInserted(0)
                    //  addLocationAdapter.notifyItemInserted(locationList.size - 1)
                    dismiss()
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
            profileImageCameraChooser()
        }
        textGallery?.setOnClickListener {
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
                R.drawable.ic_dropdown_open // Replace with your open icon resource

            } else {
                R.drawable.ic_dropdown_close // Replace with your close icon resource
            }

            if (isDropdownOpen) {

                binding.rlBankNameAndCardName.visibility = View.VISIBLE

            } else if (!isDropdownOpen) {
                binding.rlBankNameAndCardName.visibility = View.GONE


            }
            binding.textPaymentMethod.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)
        }
    }

    override fun itemClick(obj: Int, text: String, enteredText: String)  {
        when (text) {
            "location" -> {
                if (obj == locationList.size - 1) {
                    startLocationPicker()
                }
            }

            "work" -> {
                if (obj == workList.size - 1) {
//                    var text: String = "Add Your Work Here"
//                    dialogAddItem(text)
                }
            }

            "language" -> {
                if (obj == languageList.size - 1) {
                    dialogSelectLanguage()
                }
            }

            "Hobbies" -> {
                if (obj == hobbiesList.size - 1) {
                }
            }

        }
    }




    override fun onClick(p0: View?) {
        when (p0?.id) {


            R.id.textBooking -> {
                findNavController().navigate(R.id.bookingScreenHostFragment)
            }

            R.id.textCreateList -> {
                startActivity(Intent(requireActivity(), PlaceOpenActivity::class.java))
            }

            R.id.textPrivacyPolicy -> {
                val bundle = Bundle()
                bundle.putInt("privacy", 1)
                findNavController().navigate(R.id.privacyPolicyFragment, bundle)
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
                findNavController().navigate(R.id.language_fragment_host)
            }

            R.id.textNotifications -> {
                var bundle = Bundle()
                bundle.putString(AppConstant.Host,"HOST")
                findNavController().navigate(R.id.notificationFragment,bundle)
            }

            R.id.imageEditEmail -> {
                dialogEmailVerification(requireContext(), null.toString())

            }

            R.id.imageEditPhoneNumber -> {

                dialogNumberVerification(requireContext())

            }

            R.id.textVisitHelpCenter -> {
                var bundle = Bundle()
                bundle.putString(AppConstant.type, AppConstant.Host)
                findNavController().navigate(R.id.helpCenterFragment_host, bundle)
            }

            R.id.imageInfoIcon -> {
                binding.cvInfo.visibility = View.VISIBLE
            }

            R.id.clHead -> {
                binding.cvInfo.visibility = View.GONE
            }

            R.id.imageEditPicture -> {
                bottomSheetUploadImage()
            }

            R.id.imageEditName -> {
                dialogChangeName(requireContext())
            }

            R.id.textConfirmNow -> {
                dialogNumberVerification(requireContext())
                binding.textConfirmNow.visibility = View.GONE
                binding.textVerified.visibility = View.VISIBLE
            }

            R.id.textConfirmNow1 -> {
                dialogEmailVerification(requireContext(), null.toString())
                binding.textConfirmNow1.visibility = View.GONE
                binding.textVerified1.visibility = View.VISIBLE
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
                    llDebitCard.visibility = View.GONE
                    rlBankAccount.visibility = View.VISIBLE

                } else {
                    rlBankAccount.visibility = View.GONE
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
                        Glide.with(this)
                            .load(uri)
                            .error(R.drawable.ic_profile_login)
                            .placeholder(R.drawable.ic_profile_login)
                            .into(it)
                    }

                    imageStatus = "1"
                }
            }
        }

    private fun profileImageGalleryChooser() {
        ImagePicker.with(this)
            .galleryOnly().crop() // Crop image (Optional)
            .compress(1024 * 5) // Compress the image to less than 5 MB
            .maxResultSize(250, 250) // Set max resolution
            .createIntent { intent ->
                pickImageLauncher.launch(intent)
            }
    }

    private fun profileImageCameraChooser() {
        ImagePicker.with(this)
            .cameraOnly()
            .crop() // Crop image (Optional)
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

                dialogOtp(context, text, textHeaderOfOtpVerfication)
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
                dialogOtp(context, text, textHeaderOfOtpVerfication)
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
            var imageCross = findViewById<ImageView>(R.id.imageCross)

            var textLoginButton = findViewById<TextView>(R.id.textLoginButton)
            var checkBox = findViewById<CheckBox>(R.id.checkBox)
            var textKeepLogged = findViewById<TextView>(R.id.textKeepLogged)
            var textForget = findViewById<TextView>(R.id.textForget)
            val etConfirmPassword =  findViewById<EditText>(R.id.etConfirmPassword)

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
                var intent = Intent(context, GuesMain::class.java)
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
                dialogOtp(context, text, textHeaderOfOtpVerfication)

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
                dialogOtp(context, text, textHeaderOfOtpVerfication)


                dismiss()
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
            var imageCross = findViewById<ImageView>(R.id.imageCross)
            var textSubmitButton = findViewById<TextView>(R.id.textSubmitButton)
            textSubmitButton.setOnClickListener {

                var text = "Your Phone has been Verified\n  successfully."

                var textHeaderOfOtpVerfication =
                    "Please type the verification code send \nto +1 999 999 9999"
                dialogOtp(context, text, textHeaderOfOtpVerfication)


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


            var textSaveChangesButton = findViewById<TextView>(R.id.textSaveChangesButton)
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
            var imageCross = findViewById<ImageView>(R.id.imageCross)

            var textSubmitButton = findViewById<TextView>(R.id.textSubmitButton)
            textSubmitButton.setOnClickListener {
                var text2 = "Your Email has been Verified\n  successfully."

                val texter = if (text != null.toString()) text else text2

                var textHeaderOfOtpVerfication =
                    "Please type the verification code send \nto abc@gmail.com"
                dialogOtp(context, texter, textHeaderOfOtpVerfication)

                dismiss()
            }
            imageCross.setOnClickListener {
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }


    @SuppressLint("SuspiciousIndentation")
    fun dialogOtp(context: Context, text: String, textHeaderOfOtpVerfication: String) {
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
                    incorrectOtp.visibility = View.GONE
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


    fun dialogNewPassword(context: Context?, text: String) {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_new_password)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            var imageCross = findViewById<ImageView>(R.id.imageCross)
            var etPassword = findViewById<EditText>(R.id.etPassword)
            var etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)

            var textSubmitButton = findViewById<TextView>(R.id.textSubmitButton)
            textSubmitButton.setOnClickListener {
                dialogSuccess(context, text)
                dismiss()
            }

            imageCross.setOnClickListener {
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
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
                var sessionManager = SessionManager(context)
                sessionManager.setUserId(-1)
                var intent = Intent(context, AuthActivity::class.java)
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

    override fun itemClick(obj: Int, text: String) {
        when (text) {
            "location" -> {
                locationList.removeAt(obj)
                addLocationAdapter.updateLocations(locationList)
            }


            "work" -> {
                workList.removeAt(obj)
                addWorkAdapter.updateWork(workList)
            }

            "language" -> {
                languageList.removeAt(obj)
                addLanguageSpeakAdapter.updateLanguage(languageList)

            }

            "Hobbies" -> {
                hobbiesList.removeAt(obj)
                addHobbiesAdapter.updateHobbies(hobbiesList)

            }

        }
    }



}


