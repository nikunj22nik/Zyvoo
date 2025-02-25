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
import com.bumptech.glide.request.target.CustomTarget
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
import com.hbb20.CountryCodePicker
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.Locale


@AndroidEntryPoint
class HostProfileFragment : Fragment(), OnClickListener1, onItemClickData, OnClickListener {
    lateinit var binding: FragmentHostProfileBinding

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
    var session: SessionManager? = null

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
    lateinit var navController: NavController
    private lateinit var otpDigits: Array<EditText>
    private var countDownTimer: CountDownTimer? = null

    var resendEnabled = false
    var otpValue: String = ""
    var editAboutButton = true


    private val list1 = mutableListOf<CountryLanguage>()
    private val list2 = mutableListOf<CountryLanguage>()


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
                locationList.add(0, newLocation)
                addLocationAdapter.notifyItemInserted(0)

                Log.i(ErrorDialog.TAG, "Place: $placeName, ${place.id}")
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
        session = SessionManager(requireActivity())
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
        // Initialize Places API if not already initialized
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity(), apiKey)
        }
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

        binding.rlEdit.setOnClickListener {
            if (editAboutButton) {
                binding.tvEdit.text = "Save"
                binding.imageEditAboutIcon.visibility = View.GONE
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
            binding.imageEditStreetAddress.visibility = View.VISIBLE
            binding.imageStreetCheckedButton.visibility = GONE
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
                updateCityAddress(binding.cityET.text.toString())
            }
            binding.cityET.isEnabled = false
            binding.imageEditCityAddress.visibility = View.VISIBLE
            binding.CityCheckedButton.visibility = GONE
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
                updateStateAddress()
            }
            binding.stateEt.isEnabled = false
            binding.imageEditStateAddress.visibility = View.VISIBLE
            binding.stateCheckedButton.visibility = GONE
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
                updateZipCode(binding.zipEt.text.toString())
            }
            binding.zipEt.isEnabled = false
            binding.imageEditZipAddress.visibility = View.VISIBLE
            binding.zipCodeCheckedButton.visibility = GONE

        }

    }


    // Function to initialize the adapter for adding locations
    private fun adapterInitialize() {
        addLocationAdapter = AddLocationAdapter(requireContext(), locationList, this, this)
        binding.recyclerViewLocation.adapter = addLocationAdapter
        addLocationAdapter.updateLocations(locationList)
        addWorkAdapter = AddWorkAdapter(requireContext(), workList, this, this)
        binding.recyclerViewWork.adapter = addWorkAdapter
        addWorkAdapter.updateWork(workList)
        addLanguageSpeakAdapter = AddLanguageSpeakAdapter(requireContext(), languageList, this,this)
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
        addPetsAdapter = AddPetsAdapter(requireContext(), petsList, this, this)
    }


    private fun getUserProfile() {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                LoadingUtils.showDialog(requireContext(), false)
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
                                        binding.streetEditText.setText(it)
                                    }

                                    it?.state?.let {
                                        binding.stateEt.setText(it)
                                    }

                                    it?.zip_code?.let {
                                        binding.zipEt.setText(it)
                                    }

                                    it?.city?.let {
                                        binding.cityET.setText(it)
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
                                        val newLanguage =
                                            AddLanguageModel(AppConstant.unknownLocation)
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

    @SuppressLint("ObsoleteSdkInt")
    private fun dialogSelectLanguage() {
        context?.let { ctx ->
            val dialog = Dialog(ctx, R.style.BottomSheetDialog).apply {
                setCancelable(false)
                setContentView(R.layout.dialog_select_language)

                window?.apply {
                    setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }

                val imageCross = findViewById<ImageView>(R.id.imageCross)
                val recyclerViewLanguages = findViewById<RecyclerView>(R.id.recyclerViewLanguages)

                // Get unique and valid locales
                val locales = Locale.getAvailableLocales()
                    .filter { it.country.isNotEmpty() }
                    .distinctBy { it.language }  // Ensure unique languages

                // Set RecyclerView Adapter
                val localeAdapter = LocaleAdapter(locales, object : OnLocalListener {
                    override fun onItemClick(local: String) {
                        val languageName = Locale(local).getDisplayLanguage(Locale.ENGLISH)
                        Log.d("language", "Selected Language: $languageName") // Debugging log

                        addLanguageApi(languageName) // API call

                        val newLanguage = AddLanguageModel(local)
                        languageList.add(0, newLanguage)
                        addLanguageSpeakAdapter.notifyItemInserted(0)

                        dismiss()
                    }
                })
                recyclerViewLanguages?.adapter = localeAdapter

                imageCross?.setOnClickListener { dismiss() }

                show()
            }
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
        binding.textPaymentMethod.setCompoundDrawablesWithIntrinsicBounds(0,
            0, R.drawable.ic_dropdown_close, 0)

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
        }
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {


            R.id.textBooking -> {
                findNavController().navigate(R.id.bookingScreenHostFragment)
            }

            R.id.textCreateList -> {
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
                findNavController().navigate(R.id.language_fragment_host)
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
                dialogEmailVerification(requireContext())


                binding.textConfirmNow.visibility = View.GONE
                binding.textVerified.visibility = View.VISIBLE
            }

            R.id.textConfirmNow1 -> {
                dialogNumberVerification(requireContext())
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

    private fun verifyPhoneNumber(
        countryCode: String,
        phoneNumber: String,
        dialog: Dialog,
        textSubmitButton: TextView
    ) {
        lifecycleScope.launch {
            profileViewModel.phoneVerification(
                session?.getUserId().toString(),
                countryCode,
                phoneNumber
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


    private fun toggleLoginButtonEnabled(isEnabled: Boolean, text: TextView) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                text.isEnabled = isEnabled
            } catch (e: Exception) {
                Log.e(ErrorDialog.TAG, "exception toggleLoginButtonEnabled ${e.message}")
            }
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
                                    } else {
                                        emailVerification(
                                            session?.getUserId().toString(),
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

    private fun emailVerification(
        userId: String,
        email: String,
        dialog: Dialog,
        textLoginButton: TextView
    ) {
        lifecycleScope.launch {
            profileViewModel.emailVerification(
                userId,
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
        userId: String,
        otp: String,
        dialog: Dialog,
        text: TextView
    ) {
        lifecycleScope.launch {
            profileViewModel.otpVerifyPhoneVerification(
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

    private fun otpVerifyEmailVerification(
        userId: String,
        otp: String,
        dialog: Dialog,
        text: TextView
    ) {
        lifecycleScope.launch {
            profileViewModel.otpVerifyEmailVerification(
                userId,
                otp,
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            binding.textConfirmNow.visibility = View.GONE
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
                lifecycleScope.launch {
                    profileViewModel.networkMonitor.isConnected
                        .distinctUntilChanged()
                        .collect{isConn ->
                            if (!isConn){
                                LoadingUtils.showErrorDialog(requireContext(),resources.getString(R.string.no_internet_dialog_msg))
                            }else{
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
                                    binding.imageSaveAboutIcon.visibility = View.GONE
                                    binding.etAboutMeText.isEnabled = false
                                    editAboutButton = true

                                    showErrorDialog(requireContext(), resp.first)
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
                                    showErrorDialog(requireContext(), resp.first)
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
                                    showErrorDialog(requireContext(), resp.first)
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
                                                    "item removed",
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
                                                    "item removed",
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
                                                    resp.toString(),
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
                                    showErrorDialog(requireContext(), resp.first)
                                    val newLocation =
                                        AddLocationModel(place_name ?: "Unknown Location")

                                    // Prevent duplicates before adding
                                    if (!locationList.any { it.name == place_name }) {
                                        val newLocation = AddLocationModel(place_name)

                                        // Update the list and adapter safely
                                        locationList.add(0, newLocation)
                                        addLocationAdapter.updateLocations(locationList)
                                    } }
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
                                                    "item added successfully",
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
                                                    "Language added!",
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

    private fun updateCityAddress(cityName: String) {
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
                                                Toast.makeText(
                                                    requireContext(),
                                                    "City added successfully",
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

    private fun updateStateAddress() {
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
                                                Toast.makeText(
                                                    requireContext(),
                                                    "State added successfully",
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

    private fun updateZipCode(zipCode: String) {
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
                                                Toast.makeText(
                                                    requireContext(),
                                                    "State added successfully",
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

    private fun logout() {
        lifecycleScope.launch {
            profileViewModel.logout(session?.getUserId().toString()).collect{
                when(it){

                    is NetworkResult.Success -> {
                        showErrorDialog(requireContext(),it.data!!)

                        val sessionManager = SessionManager(requireContext())
                        sessionManager.setUserId(-1)
                        val intent = Intent(requireContext(), AuthActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        requireActivity().startActivity(intent)
                    }
                    is NetworkResult.Error -> {
                        showErrorDialog(requireContext(),it.message!!)

                    }
                    else ->{

                    }

                }
            }


        }
    }

}


