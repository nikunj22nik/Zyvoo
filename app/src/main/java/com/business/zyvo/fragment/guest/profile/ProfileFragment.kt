package com.business.zyvo.fragment.guest.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
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
import androidx.lifecycle.Observer
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
import com.business.zyvo.activity.HostMainActivity
import com.business.zyvo.adapter.AdapterAddPaymentCard
import com.business.zyvo.adapter.AddHobbiesAdapter
import com.business.zyvo.adapter.AddLanguageSpeakAdapter
import com.business.zyvo.adapter.AddLocationAdapter
import com.business.zyvo.adapter.AddPetsAdapter
import com.business.zyvo.adapter.AddWorkAdapter
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
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.CommonAuthWorkUtils
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.MediaUtils

import com.business.zyvo.utils.NetworkMonitorCheck
import com.business.zyvo.viewmodel.PaymentViewModel

import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class ProfileFragment : Fragment(), OnClickListener1, OnClickListener {
    lateinit var binding: FragmentProfileBinding
    private lateinit var commonAuthWorkUtils: CommonAuthWorkUtils
    private lateinit var addLocationAdapter: AddLocationAdapter
    private lateinit var addWorkAdapter: AddWorkAdapter
    private lateinit var addLanguageSpeakAdapter: AddLanguageSpeakAdapter
    private lateinit var addHobbiesAdapter: AddHobbiesAdapter
    private lateinit var addPetsAdapter: AddPetsAdapter
    private lateinit var dateManager: DateManager

    private lateinit var addPaymentCardAdapter: AdapterAddPaymentCard
    private val profileViewModel: ProfileViewModel by lazy {
        ViewModelProvider(this)[ProfileViewModel::class.java]
    }
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
    lateinit  var navController :NavController
    private lateinit var otpDigits: Array<EditText>
    private var countDownTimer: CountDownTimer? = null
    var resendEnabled = false
    var session: SessionManager?=null
    var imageBytes: ByteArray = byteArrayOf()
    var userProfile:UserProfile?=null

    // For handling the result of the Autocomplete Activity
    private val startAutocomplete =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                if (intent != null) {

                    place = Autocomplete.getPlaceFromIntent(intent).name ?: "Unknown Location"
                    etSearch?.text = place
                    val newLocation = AddLocationModel(place)
                    addLivePlace(newLocation.name)
                    locationList.add(0, newLocation)
                    addLocationAdapter.notifyItemInserted(0)
                    Log.i(TAG, "Place: ${place}, $place")

                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "User canceled autocomplete")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        val navController = findNavController()
        commonAuthWorkUtils = CommonAuthWorkUtils(requireContext(), navController)
        apiKey = getString(R.string.api_key)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dateManager = DateManager(requireContext())


        // Inflate the layout for this fragment
        binding =
            FragmentProfileBinding.inflate(LayoutInflater.from(requireContext()), container, false)
        val newLocation = AddLocationModel(AppConstant.unknownLocation)


        binding.switchHost.setOnClickListener{

            val session = SessionManager(requireContext())
            session.setCurrentPanel(AppConstant.Host)
            var intent = Intent(requireContext(),HostMainActivity::class.java)

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)

            startActivity(intent)
        }
        locationList.add(newLocation)
        val newWork = AddWorkModel(AppConstant.unknownLocation)
        workList.add(newWork)
        val newLanguage = AddLanguageModel(AppConstant.unknownLocation)
        languageList.add(newLanguage)
        val newHobbies = AddHobbiesModel(AppConstant.unknownLocation)

        hobbiesList.add(newHobbies)
        val newPets = AddPetsModel(AppConstant.unknownLocation)

        petsList.add(newPets)


        addPaymentCardAdapter = AdapterAddPaymentCard(requireContext(), mutableListOf())
        binding.recyclerViewPaymentCardList.adapter = addPaymentCardAdapter
        profileViewModel.paymentCardList.observe(viewLifecycleOwner, Observer { payment ->
            addPaymentCardAdapter.updateItem(payment)
        })

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
        initView()
        getUserProfile()

        return binding.root
    }

    private fun initView() {
        binding.apply {
            imageEditAbout.setOnClickListener {
                etAboutMe.isEnabled = true
                imageEditAbout.visibility = GONE
                imageAboutCheckedButton.visibility = View.VISIBLE
            }

            imageAboutCheckedButton.setOnClickListener {
                if (etAboutMe.text.isEmpty()){
                    showErrorDialog(requireContext(),AppConstant.aboutMe)
                }else{
                    updateAddAboutMe(etAboutMe.text.toString())
                }
            }

            imageEditStreetAddress.setOnClickListener {
                streetEditText.isEnabled = true
                imageEditStreetAddress.visibility = GONE
                imageStreetCheckedButton.visibility = View.VISIBLE
            }
            imageStreetCheckedButton.setOnClickListener {
                if (streetEditText.text.isEmpty()){
                    showErrorDialog(requireContext(),"Street Cannot be Empty")
                }else{
                    updateAddStreetAddress(streetEditText.text.toString())
                }
                binding.streetEditText.isEnabled = false
                binding.imageEditStreetAddress.visibility = View.VISIBLE
                binding.imageStreetCheckedButton.visibility = GONE
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
                    updateCityAddress(cityET.text.toString())
                }
                binding.cityET.isEnabled = false
                binding.imageEditCityAddress.visibility = View.VISIBLE
                binding.CityCheckedButton.visibility = GONE
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
                    updateStateAddress(stateEt.text.toString())
                }
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
                    updateZipCode(zipEt.text.toString())
                }
            }

        }

    }

    private fun getUserProfile() {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                profileViewModel.getUserProfile(session?.getUserId().toString()).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                var name = ""
                                it.data?.let { resp ->
                                    Log.d("TESTING_PROFILE","HERE IN A USER PROFILE ,"+resp.toString())
                                    userProfile = Gson().fromJson(resp, UserProfile::class.java)
                                    userProfile.let {
                                        if (it?.first_name != null && it.last_name != null) {
                                            name =
                                                it.first_name + " " + it.last_name
                                        }
                                        it?.name = name
                                        binding.user = it
                                        if (it?.profile_image != null) {

                                            Glide.with(requireContext())
                                                .asBitmap() // Convert the image into Bitmap
                                                .load(BuildConfig.MEDIA_URL + it.profile_image) // User profile image URL
                                                .into(object : SimpleTarget<Bitmap>() {
                                                    override fun onResourceReady(resource: Bitmap,
                                                                                 transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                                                        // The 'resource' is the Bitmap
                                                        // Now you can use the Bitmap (e.g., set it to an ImageView, or process it)
                                                        binding.imageProfilePicture.setImageBitmap(resource)
                                                        imageBytes = MediaUtils.bitmapToByteArray(resource)
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
                                        if (it?.where_live!=null && it.where_live.isNotEmpty()){
                                            locationList = getObjectsFromNames(it.where_live) { name ->
                                                AddLocationModel(name)  // Using the constructor of MyObject to create instances
                                            }
                                            val newLanguage = AddLocationModel(AppConstant.unknownLocation)
                                            locationList.add(newLanguage)
                                            addLocationAdapter.updateLocations(locationList)
                                        }
                                        if (it?.my_work!=null && it.my_work.isNotEmpty()){
                                            workList = getObjectsFromNames(it.my_work) { name ->
                                                AddWorkModel(name)  // Using the constructor of MyObject to create instances
                                            }
                                            val newLanguage = AddWorkModel(AppConstant.unknownLocation)
                                            workList.add(newLanguage)
                                            addWorkAdapter.updateWork(workList)
                                        }
                                        if (it?.languages!=null && it.languages.isNotEmpty()){
                                            languageList = getObjectsFromNames(it.languages) { name ->
                                                AddLanguageModel(name)  // Using the constructor of MyObject to create instances
                                            }
                                            val newLanguage = AddLanguageModel(AppConstant.unknownLocation)
                                            languageList.add(newLanguage)
                                            addLanguageSpeakAdapter.updateLanguage(languageList)
                                        }
                                        if (it?.hobbies!=null && it.hobbies.isNotEmpty()){
                                            hobbiesList = getObjectsFromNames(it.hobbies) { name ->
                                                AddHobbiesModel(name)  // Using the constructor of MyObject to create instances
                                            }
                                            val newLanguage = AddHobbiesModel(AppConstant.unknownLocation)
                                            hobbiesList.add(newLanguage)
                                            addHobbiesAdapter.updateHobbies(hobbiesList)
                                        }
                                        if (it?.pets!=null && it.pets.isNotEmpty()){
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
        }else{
            LoadingUtils.showErrorDialog(requireContext(),
                resources.getString(R.string.no_internet_dialog_msg)
            )}
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        paymentOpenCloseDropDown()
        binding.textGiveFeedback.setOnClickListener(this)
        binding.rlPasswordTitle.setOnClickListener(this)
        binding.textTermServices.setOnClickListener(this)
        binding.textPrivacyPolicy.setOnClickListener(this)
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
        addLocationAdapter = AddLocationAdapter(requireContext(), locationList, this)
        binding.recyclerViewLocation.adapter = addLocationAdapter

        // Update the adapter with the initial location list (if any)
        addLocationAdapter.updateLocations(locationList)



        addWorkAdapter = AddWorkAdapter(requireContext(), workList, this)
        binding.recyclerViewWork.adapter = addWorkAdapter

        addWorkAdapter.updateWork(workList)

        addLanguageSpeakAdapter = AddLanguageSpeakAdapter(requireContext(), languageList, this)
        binding.recyclerViewlanguages.adapter = addLanguageSpeakAdapter

        addLanguageSpeakAdapter.updateLanguage(languageList)


        addHobbiesAdapter = AddHobbiesAdapter(requireContext(), hobbiesList, this)
        binding.recyclerViewHobbies.adapter = addHobbiesAdapter

        addHobbiesAdapter.updateHobbies(hobbiesList)


        addPetsAdapter = AddPetsAdapter(requireContext(), petsList, this)
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
                    val languageName = Locale(local).getDisplayLanguage(Locale.ENGLISH)
                    val newLanguage = AddLanguageModel(local)
                    Log.d("language", "onItemClick: $languageName")
                    addLanguageApi(languageName)

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
                R.drawable.ic_dropdown_open // Replace with your open icon resource

            } else {
                R.drawable.ic_dropdown_close // Replace with your close icon resource
            }

            if (isDropdownOpen) {
                binding.recyclerViewPaymentCardList.visibility = View.VISIBLE
                binding.textAddNewPaymentCard.visibility = View.VISIBLE
            } else if (!isDropdownOpen) {
                binding.recyclerViewPaymentCardList.visibility = GONE
                binding.textAddNewPaymentCard.visibility = GONE

            }
            binding.textPaymentMethod.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)
        }
    }

    override fun itemClick(obj: Int, text: String) {
        when (text) {
            "location" -> {
                if (obj == locationList.size - 1) {
                    startLocationPicker()
                } else {
                    deleteLivePlace(obj)
                    locationList.removeAt(obj)
                    addLocationAdapter.updateLocations(locationList)

                }
            }

            "work" -> {
                if (obj == workList.size - 1) {
//                   addMyWork()
                } else {
                    deleteMyWork(obj)
                    workList.removeAt(obj)
                    addWorkAdapter.updateWork(workList)

                }
            }

            "language" -> {
                if (obj == languageList.size - 1) {
                    dialogSelectLanguage()
                } else {
                    deleteLanguageApi(obj)
                    languageList.removeAt(obj)
                    addLanguageSpeakAdapter.updateLanguage(languageList)

                }

            }

            "Hobbies" -> {
                if (obj == hobbiesList.size - 1) {
//                    addHobbiesApi()
                } else {
                    deleteHobbiesApi(obj)
                    hobbiesList.removeAt(obj)
                    addHobbiesAdapter.updateHobbies(hobbiesList)

                }
            }

            "Pets" -> {
                if (obj == petsList.size - 1) {
//                    addPetApi()
                } else {
                    deletePetsApi(obj)
                    petsList.removeAt(obj)
                    addPetsAdapter.updatePets(petsList)
                }
            }

        }

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.textPrivacyPolicy -> {
                var bundle = Bundle()
                bundle.putInt("privacy",0)
                findNavController().navigate(R.id.privacyPolicyFragment,bundle)
            }

            R.id.rlPasswordTitle -> {
                var text = "Your password has been changed successfully"
               dialogNewPassword(requireContext(),text)
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
                findNavController().navigate(R.id.helpCenterFragment_host,bundle)
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
               dialogEmailVerification(requireContext(), null.toString())
                binding.textConfirmNow.visibility = GONE
                binding.textVerified.visibility = View.VISIBLE
            }

            R.id.textConfirmNow1 -> {

                dialogNumberVerification(requireContext())
                binding.textConfirmNow1.visibility = GONE
                binding.textVerified1.visibility = View.VISIBLE
            }
            R.id.imageEditEmail -> {
              dialogEmailVerification(requireContext(), null.toString())

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
                                profileViewModel.addStreetAddressApi(session?.getUserId().toString(),
                                    streetAddress
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Toast.makeText(requireContext(),"Street Address added successfully",Toast.LENGTH_SHORT).show()
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
                                profileViewModel.addCityApi(session?.getUserId().toString(),
                                    cityName
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Toast.makeText(requireContext(),"City added successfully",Toast.LENGTH_SHORT).show()
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

    private fun updateStateAddress(stateName: String) {
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
                                profileViewModel.addStateApi(session?.getUserId().toString(),
                                    stateName
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Toast.makeText(requireContext(),"State added successfully",Toast.LENGTH_SHORT).show()
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

    private fun updateZipCode(zipCode : String){
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
                                profileViewModel.addZipCodeApi(session?.getUserId().toString(),
                                    zipCode
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Toast.makeText(requireContext(),"State added successfully",Toast.LENGTH_SHORT).show()
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
                                profileViewModel.addMyWorkApi(session?.getUserId().toString(),
                                    work
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Toast.makeText(requireContext(),"item added successfully",Toast.LENGTH_SHORT).show()
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
                       /* Glide.with(this)
                            .load(uri)
                            .error(R.drawable.ic_profile_login)
                            .placeholder(R.drawable.ic_profile_login)
                            .into(it)*/
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
                    profileViewModel.uploadProfileImage(session?.getUserId().toString(),
                        bytes).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                it.data?.let { resp ->
                                    showErrorDialog(requireContext(),resp.first)
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
        }else{
            showErrorDialog(requireContext(),
                resources.getString(R.string.no_internet_dialog_msg)
            )
        }
    }

    private fun updateName(first_name: String,
                           last_name: String,
                           dialog:Dialog, textSaveChangesButton:TextView) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                lifecycleScope.launch {
                    profileViewModel.addUpdateName(session?.getUserId().toString(),
                        first_name,
                        last_name).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                it.data?.let { resp ->
                                    showErrorDialog(requireContext(),resp.first)
                                    userProfile?.name = first_name+" "+last_name
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
        }else{
            showErrorDialog(requireContext(),
                resources.getString(R.string.no_internet_dialog_msg)
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateAddAboutMe(about_me: String) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                lifecycleScope.launch {
                    profileViewModel.addAboutMe(session?.getUserId().toString(),
                        about_me).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                it.data?.let { resp ->
                                    binding.etAboutMe.isEnabled = false
                                    binding.imageEditAbout.visibility = View.VISIBLE
                                    binding.imageAboutCheckedButton.visibility = GONE
                                    showErrorDialog(requireContext(),resp.first)
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
        }else{
            showErrorDialog(requireContext(),
                resources.getString(R.string.no_internet_dialog_msg)
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addLivePlace(place_name: String) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                lifecycleScope.launch {
                    profileViewModel.addLivePlace(session?.getUserId().toString(),
                        place_name).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                it.data?.let { resp ->
                                    showErrorDialog(requireContext(),resp.first)
                                    val newLocation = AddLocationModel(place_name ?: "Unknown Location")

                                    // Add the new location to the list and notify the adapter
                                    locationList.add(0, newLocation)

                                    //  addLocationAdapter.updateLocations(locationList)  // Notify adapter here
                                    addLocationAdapter.notifyItemInserted(0)
                                    //  addLocationAdapter.notifyItemInserted(locationList.size - 1)
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
        }else{
            showErrorDialog(requireContext(),
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
                                profileViewModel.deleteLivePlaceApi(session?.getUserId().toString(),
                                    index
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Toast.makeText(requireContext(),"item removed",Toast.LENGTH_SHORT).show()
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
                                profileViewModel.deleteMyWorkApi(session?.getUserId().toString(),
                                    index
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Toast.makeText(requireContext(),"item removed",Toast.LENGTH_SHORT).show()
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
                                profileViewModel.addLanguageApi(session?.getUserId().toString(),
                                    languageName
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Toast.makeText(requireContext(),"Language added!",Toast.LENGTH_SHORT).show()
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
                                profileViewModel.deleteLanguageApi(session?.getUserId().toString(),
                                    index
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Toast.makeText(requireContext(),"Language added!",Toast.LENGTH_SHORT).show()
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

    private fun addHobbiesApi(hobbies:String) {
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
                                profileViewModel.addHobbiesApi(session?.getUserId().toString(),
                                    hobbies
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Toast.makeText(requireContext(),"Hobbie added!",Toast.LENGTH_SHORT).show()
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
                                profileViewModel.deleteHobbiesApi(session?.getUserId().toString(),
                                    index
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Toast.makeText(requireContext(),"Hobbie removed!",Toast.LENGTH_SHORT).show()
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

    private fun addPetApi(petName : String){
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
                                profileViewModel.addPetApi(session?.getUserId().toString(),
                                    petName
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Toast.makeText(requireContext(),"Hobbie added!",Toast.LENGTH_SHORT).show()
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
                                profileViewModel.deletePetsApi(session?.getUserId().toString(),
                                    index
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let { resp ->
                                                Toast.makeText(requireContext(),"Pet removed!",Toast.LENGTH_SHORT).show()
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
            .crop() // Crop image (Optional)
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
            var imageCross =  findViewById<ImageView>(R.id.imageCross)

            var textLoginButton =  findViewById<TextView>(R.id.textLoginButton)
            var checkBox =  findViewById<CheckBox>(R.id.checkBox)
            var textKeepLogged =  findViewById<TextView>(R.id.textKeepLogged)
            var textForget =  findViewById<TextView>(R.id.textForget)
            val etConfirmPassword =  findViewById<EditText>(R.id.etConfirmPassword)
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
            imgHidePass.visibility = GONE
            etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            etPassword.setSelection(etPassword.text.length)
        }
        imgShowPass.setOnClickListener {
            imgShowPass.visibility = GONE
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


    private fun dialogChangeName(context: Context?){
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(true)
            setContentView(R.layout.dialog_change_names)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            val imageProfilePicture =  findViewById<CircleImageView>(R.id.imageProfilePicture)
            if (imageBytes.isNotEmpty()){
                MediaUtils.setImageFromByteArray(imageBytes,imageProfilePicture)
            }


            val textSaveChangesButton =  findViewById<TextView>(R.id.textSaveChangesButton)
            val editTextFirstName =  findViewById<EditText>(R.id.editTextFirstName)
            val editTextLastName =  findViewById<EditText>(R.id.editTextLastName)
            textSaveChangesButton.setOnClickListener{
                if (editTextFirstName.text.isEmpty()){
                    showErrorDialog(requireContext(),AppConstant.firstName)
                }else if (editTextLastName.text.isEmpty()){
                    showErrorDialog(requireContext(),AppConstant.lastName)
                }else {
                    toggleLoginButtonEnabled(false, textSaveChangesButton)
                    updateName(editTextFirstName.text.toString(),
                        editTextLastName.text.toString(),
                        dialog,textSaveChangesButton)
                }
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
                    incorrectOtp.visibility = GONE
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
                var intent  = Intent(context, AuthActivity::class.java)
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


    // Function to convert List<String> to MutableList<T>
    fun <T : HasName> getObjectsFromNames(names: List<String>, constructor: (String) -> T): MutableList<T> {
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

}