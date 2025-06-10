package com.business.zyvo.fragment.both.completeProfile

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
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.transition.Transition
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
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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
import com.business.zyvo.AppConstant
import com.business.zyvo.BuildConfig
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.LoadingUtils.Companion.showSuccessDialog
import com.business.zyvo.NetworkResult
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.business.zyvo.OnClickListener1
import com.business.zyvo.OnLocalListener
import com.business.zyvo.R
import com.business.zyvo.activity.GuesMain
import com.business.zyvo.adapter.AddHobbiesAdapter
import com.business.zyvo.adapter.AddLanguageSpeakAdapter
import com.business.zyvo.adapter.AddLocationAdapter
import com.business.zyvo.adapter.AddPetsAdapter
import com.business.zyvo.adapter.AddWorkAdapter
import com.business.zyvo.adapter.selectLanguage.LocaleAdapter
import com.business.zyvo.databinding.FragmentCompleteProfileBinding
import com.business.zyvo.fragment.both.completeProfile.model.CompleteProfileReq
import com.business.zyvo.fragment.both.completeProfile.viewmodel.CompleteProfileViewModel
import com.business.zyvo.fragment.guest.profile.model.UserProfile
import com.business.zyvo.model.AddHobbiesModel
import com.business.zyvo.model.AddLanguageModel
import com.business.zyvo.model.AddLocationModel
import com.business.zyvo.model.AddPetsModel
import com.business.zyvo.model.AddWorkModel
import com.business.zyvo.onItemClickData
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.CommonAuthWorkUtils
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.ErrorDialog.TAG
import com.business.zyvo.utils.ErrorDialog.customDialog
import com.business.zyvo.utils.ErrorDialog.isValidEmail
import com.business.zyvo.utils.MediaUtils
import com.business.zyvo.utils.MultipartUtils
import com.business.zyvo.utils.NetworkMonitorCheck
import com.business.zyvo.utils.PrepareData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.hbb20.CountryCodePicker
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
@AndroidEntryPoint
class CompleteProfileFragment : Fragment(),OnClickListener1, onItemClickData , OnClickListener{

    private var _binding: FragmentCompleteProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var  commonAuthWorkUtils: CommonAuthWorkUtils
    private lateinit var addLocationAdapter: AddLocationAdapter
    private lateinit var addWorkAdapter: AddWorkAdapter
    private lateinit var addLanguageSpeakAdapter: AddLanguageSpeakAdapter
    private lateinit var addHobbiesAdapter: AddHobbiesAdapter
    private lateinit var addPetsAdapter: AddPetsAdapter
    private var petsList: MutableList<AddPetsModel> = mutableListOf()
    private var hobbiesList: MutableList<AddHobbiesModel> = mutableListOf()
    private var locationList: MutableList<AddLocationModel> = mutableListOf()
    private var workList: MutableList<AddWorkModel> = mutableListOf()
    private var languageList: MutableList<AddLanguageModel> = mutableListOf()
    private lateinit var getInquiryResult: ActivityResultLauncher<Inquiry>
    private lateinit var apiKey: String
    private lateinit var localeAdapter: LocaleAdapter
    private var locales: List<Locale> = listOf()
    private var etSearch: TextView? = null
    private var bottomSheetDialog : BottomSheetDialog? = null
    private var imageStatus = ""
    lateinit var navController :NavController
    private lateinit var otpDigits: Array<EditText>
    private var countDownTimer: CountDownTimer? = null
    private var identityVerified = 0
    var resendEnabled = false
    var userId:String = ""
    private var token:String = ""
    var type:String = ""
    var email:String = ""
    var fullName:String = ""
    var session: SessionManager?=null
    var imageBytes: ByteArray = byteArrayOf()
    private val completeProfileReq = CompleteProfileReq()
    var userProfile: UserProfile? = null
    var firstName :String =""
    var lastName :String =""
    var updateFirstName :String =""
    var updateLastName :String =""


    private val completeProfileViewModel: CompleteProfileViewModel by lazy {
        ViewModelProvider(this)[CompleteProfileViewModel::class.java]
    }

    // For handling the result of the Autocomplete Activity
    private val startAutocomplete =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    val place = Autocomplete.getPlaceFromIntent(intent)
                    etSearch?.text = place.name
                    val newLocation = AddLocationModel(place.name ?: AppConstant.unknownLocation)

                    // Add the new location to the list and notify the adapter
                    locationList.add(locationList.size-1, newLocation)

                    //   Toast.makeText(requireContext(),"count${locationList.size}",Toast.LENGTH_LONG).show()
                    // Check if we need to hide the "Add New" button

                    //  addLocationAdapter.updateLocations(locationList)  // Notify adapter here
                    addLocationAdapter.updateLocations(locationList)
                    //  addLocationAdapter.notifyItemInserted(locationList.size - 1)

                    Log.i(ErrorDialog.TAG, "Place: ${place.name}, ${place.id}")
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
                Log.i(ErrorDialog.TAG, "User canceled autocomplete")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
        commonAuthWorkUtils = CommonAuthWorkUtils(requireContext(),navController)
        apiKey = getString(R.string.api_key)

        getInquiryResult = registerForActivityResult(Inquiry.Contract()) { result ->
            when (result) {
                is InquiryResponse.Complete -> {
                    // User identity verification completed successfully
                    binding.textConfirmNow2.visibility = GONE
                    binding.textVerified2.visibility = View.VISIBLE
                    identityVerified = 1
                    Toast.makeText(requireContext(), "Verified Successfully!", Toast.LENGTH_SHORT).show()
                }
                is InquiryResponse.Cancel -> {
                    // User abandoned the verification process
                    binding.textConfirmNow2.visibility = View.VISIBLE
                    binding.textVerified2.visibility = GONE
                    Toast.makeText(requireContext(),"Request Cancelled",Toast.LENGTH_LONG).show()
                }
                is InquiryResponse.Error -> {
                    // Error occurred during identity verification
                    binding.textConfirmNow2.visibility = View.VISIBLE
                    binding.textVerified2.visibility = GONE
                    Toast.makeText(requireContext(),"Error Occurred, Try Again",Toast.LENGTH_LONG).show()

                }
            }
        }

    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCompleteProfileBinding.inflate(inflater, container, false)
        val newLocation = AddLocationModel(AppConstant.unknownLocation)

        locationList.add(newLocation)

        val newWork = AddWorkModel(AppConstant.unknownLocation)

        workList.add(newWork)
        val newLanguage = AddLanguageModel(AppConstant.unknownLocation)

        languageList.add(newLanguage)

        val newHobbies = AddHobbiesModel(AppConstant.unknownLocation)

        hobbiesList.add(newHobbies)
        val newPets = AddPetsModel(AppConstant.unknownLocation)

        petsList.add(newPets)
        session = SessionManager(requireActivity())

        binding.imageEditAbout.setOnClickListener {
            binding.etAboutMe.isEnabled = true
        }

        arguments?.let {
            val data = Gson().fromJson(requireArguments().getString("data")!!,JsonObject::class.java)
            userId = data.get("user_id").asInt.toString()
            session!!.setUserId(userId.toInt())
            token = data.get("token").asString
            token.let {
                session?.setAuthToken(it)
            }
            type = requireArguments().getString("type")!!
            email = requireArguments().getString("email")!!
            Log.d(ErrorDialog.TAG,userId+"\n"+type+"\n"+email+"\n"+token)
        }

        binding.textConfirmNow2.setOnClickListener {
            if (NetworkMonitorCheck._isConnected.value) {
                launchVerifyIdentity()
            } else {
                showErrorDialog(requireContext(), resources.getString(R.string.no_internet_dialog_msg))
            }
        }

        // Observe the isLoading state
        lifecycleScope.launch {
            completeProfileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    LoadingUtils.showDialog(requireContext(), false)
                } else {
                    LoadingUtils.hideDialog()
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageInfoIcon.setOnClickListener(this)
        binding.clHead.setOnClickListener(this)
        binding.imageEditPicture.setOnClickListener(this)
        binding.imageEditName.setOnClickListener(this)
        binding.textConfirmNow.setOnClickListener(this)
        binding.textConfirmNow1.setOnClickListener(this)
        binding.textSaveButton.setOnClickListener(this)
        binding.skipNow.setOnClickListener(this)
        SessionManager(requireContext()).clearLanguage()
        adapterInitialize()
        if (session?.getName() != ""){
            binding.textName.text = session?.getName()
            fullName = session?.getName().toString()
        }

        // Initialize Places API if not already initialized
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity(), apiKey)
        }

        // Set listeners
        setCheckVerified()
        getUserProfile()
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    // Handle back press logic here
                    requireActivity().finishAffinity()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)
    }

    private fun launchVerifyIdentity(){
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

    private fun setCheckVerified() {
        if ("mobile".equals(type)){

            binding.textConfirmNow1.visibility = GONE
            binding.textVerified1.visibility = View.VISIBLE
        }
        if ("email".equals(type)){

            binding.textConfirmNow.visibility = GONE
            binding.textVerified.visibility = View.VISIBLE
        }
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
        addLanguageSpeakAdapter = AddLanguageSpeakAdapter(requireContext(), languageList, this,this)
        binding.recyclerViewlanguages.adapter = addLanguageSpeakAdapter
        addLanguageSpeakAdapter.updateLanguage(languageList)
        addHobbiesAdapter = AddHobbiesAdapter(requireContext(),hobbiesList,this,this)
        binding.recyclerViewHobbies.adapter = addHobbiesAdapter
        addHobbiesAdapter.updateHobbies(hobbiesList)
        addPetsAdapter = AddPetsAdapter(requireContext(),petsList,this,this)
        binding.recyclerViewPets.adapter = addPetsAdapter
        addPetsAdapter.updatePets(petsList)

    }

    // Function to start the location picker using Autocomplete
    private fun startLocationPicker() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(requireContext())
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
            localeAdapter = LocaleAdapter(locales, object : OnLocalListener{
                @SuppressLint("SuspiciousIndentation")
                override fun onItemClick(local: String/*,type: String*/) {
                    val newLanguage = AddLanguageModel(local)

                    // Add the new location to the list and notify the adapter
                    //  languageList.add(0, newLanguage)
                    //  addLocationAdapter.updateLocations(locationList)  // Notify adapter here
                    /*  if (type == "add") {*/
                    languageList.add(languageList.size - 1, newLanguage)
                    addLanguageSpeakAdapter.updateLanguage(languageList)
                    callingLanguageApi(local)
                    /* }else{
                         val index = languageList.indexOfFirst { it.name == local }
                         val removedLang = languageList[index].name
                         deleteLanguageApi(index)
                         languageList.removeAt(index)
                         SessionManager(requireContext()).removeLanguage(requireContext(), removedLang)
                     }*/
                    //  addLocationAdapter.notifyItemInserted(locationList.size - 1)
                    dismiss()
                }

            }, PrepareData.languagesWithRegions/*, languageList*/)
            recyclerViewLanguages?.adapter = localeAdapter

            imageCross?.setOnClickListener { dismiss() }

            window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
            show()
        }
    }
    private fun callingLanguageApi(languageName :String){
        lifecycleScope.launch {
            var userId = SessionManager(requireContext()).getUserId()
            LoadingUtils.showDialog(requireContext(),false)
            completeProfileViewModel.addLanguageApi(userId.toString(), languageName).collect {
                when(it){
                    is NetworkResult.Success ->{
                        LoadingUtils.hideDialog()
                    }
                    is NetworkResult.Error ->{
                        LoadingUtils.hideDialog()
                        LoadingUtils.showErrorDialog(requireContext(),it.message.toString())
                    }
                    else ->{

                    }
                }
            }

        }
    }



    fun validation(): Boolean{
        if (binding.textName.text.isEmpty()){
            showErrorDialog(requireContext(),AppConstant.name)
            return false
        }else if (getNames(locationList).isEmpty()){
            showErrorDialog(requireContext(),AppConstant.locationCheck)
            return false
        }else if (getNames(languageList).isEmpty()){
            showErrorDialog(requireContext(),AppConstant.languageCheck)
            return false
        }
        return true
    }

    private fun bottomSheetUploadImage(){
        Log.d("TESTING_COMPELETE_PROFILE","Inside of Bottom Sheet Upload Image")
        bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.BottomSheetDialog1)
        bottomSheetDialog?.setContentView(R.layout.bottom_sheet_upload_image)

        val textCamera = bottomSheetDialog?.findViewById<TextView>(R.id.textCamera)

        val textGallery = bottomSheetDialog?.findViewById<TextView>(R.id.textGallery)

        textCamera?.setOnClickListener {
            profileImageCameraChooser()
            bottomSheetDialog?.dismiss()
        }

        textGallery?.setOnClickListener {
            profileImageGalleryChooser()
            bottomSheetDialog?.dismiss()
        }


        bottomSheetDialog?.show()

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

            "language" ->{
                if (obj == languageList.size - 1) {
                    dialogSelectLanguage()
                }

            }

            "Hobbies"->{
                if (obj == hobbiesList.size - 1) {


                }
            }

            "Pets"->{
                if (obj == petsList.size - 1) {


                }
            }

        }

    }

    override fun onClick(p0: View?) {

        when(p0?.id){
            R.id.imageInfoIcon -> {
                binding.cvInfo.visibility = View.VISIBLE
            }


            R.id.clHead -> {
                binding.cvInfo.visibility = View.GONE


            }

            R.id.imageEditPicture -> {
                bottomSheetUploadImage()
            }

            R.id.imageEditName->{
                dialogChangeName(requireContext())
            }

            R.id.textConfirmNow->{

                dialogEmailVerification(requireContext())
            }

            R.id.textConfirmNow1->{
                dialogNumberVerification(requireContext())
            }

            R.id.textSaveButton -> {
                toggleLoginButtonEnabled(false, binding.textSaveButton)
                lifecycleScope.launch {
                    completeProfileViewModel.networkMonitor.isConnected
                        .distinctUntilChanged() // Ignore duplicate consecutive values
                        .collect { isConn ->
                            if (!isConn) {
                                showErrorDialog(
                                    requireContext(),
                                    resources.getString(R.string.no_internet_dialog_msg)
                                )
                                toggleLoginButtonEnabled(true, binding.textSaveButton)
                            } else {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    val fullName = binding.textName.text.toString().trim()
                                    val nameParts = fullName.split(" ")

                                    firstName = nameParts.getOrNull(0) ?: ""
                                    lastName = nameParts.getOrNull(1) ?: ""

                                    if (validation()) {
                                        completeProfileReq.user_id = session?.getUserId()!!
                                        completeProfileReq.first_name =  firstName
                                        completeProfileReq.last_name =  lastName
                                        completeProfileReq.about_me = binding.etAboutMe.text.toString()
                                        completeProfileReq.where_live = getNames(locationList)
                                        completeProfileReq.works = getNames(workList)
                                        completeProfileReq.languages = getNames(languageList)
                                        completeProfileReq.hobbies = getNames(hobbiesList)
                                        completeProfileReq.pets = getNames(petsList)
                                        completeProfileReq.bytes = imageBytes
                                        completeProfileReq.identityVerified = identityVerified
                                        completeProfile(completeProfileReq,binding.textSaveButton)
                                    }else{
                                        toggleLoginButtonEnabled(true, binding.textSaveButton)
                                    }
                                }
                            }
                        }
                }
            }

            R.id.skip_now ->{
                if (binding.textName.text.toString().isNotEmpty()) {
                    val intent = Intent(requireContext(), GuesMain::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    requireActivity().finish()
                }else{
                    showErrorDialog(requireContext(),"Kindly update at least your name.")
                }
            }
        }

    }

    private fun completeProfile(completeProfileReq: CompleteProfileReq, textSaveButton: TextView) {
        lifecycleScope.launch {
            completeProfileViewModel.completeProfile(completeProfileReq).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            if (userId!=null && !userId.equals("")) {

                                session?.setUserId(userId.toInt())
                                Log.d("CheckUserIdComplete",session?.getUserId().toString())
                            }
                            ErrorDialog.showToast(requireActivity(),resp.first)
                            val intent = Intent(requireContext(),GuesMain::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            requireActivity().finish()
                        }

                        toggleLoginButtonEnabled(true, textSaveButton)
                    }
                    is NetworkResult.Error -> {
                        showErrorDialog(requireContext(), it.message!!)
                        toggleLoginButtonEnabled(true, textSaveButton)
                    }

                    else -> {
                        toggleLoginButtonEnabled(true, textSaveButton)
                        Log.v(ErrorDialog.TAG, "error::" + it.message)
                    }
                }
            }
        }

    }

    private val pickImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                result.data?.data?.let { uri ->
                    if (uri!=null) {
                        // Load image into BottomSheetDialog's ImageView if available
                        /* binding.imageProfilePicture.let {
                             Glide.with(this)
                                 .load(uri)
                                 .error(R.drawable.ic_profile_login)
                                 .placeholder(R.drawable.ic_profile_login)
                                 .into(it)
                         }*/
                        Glide.with(this)
                            .asBitmap()
                            .load(uri)
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                                ) {
                                    binding.imageProfilePicture.setImageBitmap(resource)

                                    imageBytes = MediaUtils.bitmapToByteArray(resource)
                                    Log.d(ErrorDialog.TAG, imageBytes.toString())
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                    // Handle placeholder if needed
                                }
                            })
                        imageStatus = "1"
                    }
                }
            }
        }

    private fun profileImageGalleryChooser() {
        ImagePicker.with(this)
            .galleryOnly()
            .crop(4f,4f) // Crop image (Optional)
            .compress(1024 * 5) // Compress the image to less than 5 MB
            .maxResultSize(250, 250) // Set max resolution
            .createIntent { intent ->
                pickImageLauncher.launch(intent)
            }
    }

    private fun profileImageCameraChooser() {
        ImagePicker.with(this)
            .cameraOnly()
            .crop(4f,4f) // Crop image (Optional)
            .compress(1024 * 5) // Compress the image to less than 5 MB
            .maxResultSize(250, 250) // Set max resolution
            .createIntent { intent ->
                pickImageLauncher.launch(intent)
            }
    }


    private fun dialogNumberVerification(context: Context?){
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
            val etMobileNumber = findViewById<EditText>(R.id.etMobileNumber)
            val countyCodePicker = findViewById<CountryCodePicker>(R.id.countyCodePicker)
            textSubmitButton.setOnClickListener{
                toggleLoginButtonEnabled(false, textSubmitButton)
                lifecycleScope.launch {
                    completeProfileViewModel.networkMonitor.isConnected
                        .distinctUntilChanged() // Ignore duplicate consecutive values
                        .collect { isConn ->
                            if (!isConn) {
                                showErrorDialog(
                                    requireContext(),resources.getString(R.string.no_internet_dialog_msg)
                                )
                                toggleLoginButtonEnabled(true, textSubmitButton)
                            } else {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (etMobileNumber.text!!.isEmpty()) {
                                        etMobileNumber.error = "Mobile required"
                                        showErrorDialog(requireContext(),AppConstant.mobile)
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
                                        phoneVerification(session?.getUserId().toString(),countryCode, phoneNumber ,dialog,textSubmitButton)
                                    }
                                }
                            }
                        }
                }
            }
            imageCross.setOnClickListener{
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }}

    /*
        @SuppressLint("SetTextI18n")
        private fun dialogChangeName(context: Context?){
            val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
            dialog?.apply {
                setCancelable(true)
                setCanceledOnTouchOutside(true)
                setContentView(R.layout.dialog_change_names)
                window?.attributes = WindowManager.LayoutParams().apply {
                    copyFrom(window?.attributes)
                    width = WindowManager.LayoutParams.MATCH_PARENT
                    height = WindowManager.LayoutParams.MATCH_PARENT
                }


                val textSaveChangesButton =  findViewById<TextView>(R.id.textSaveChangesButton)
                val editTextFirstName =  findViewById<EditText>(R.id.editTextFirstName)
                val editTextLastName =  findViewById<EditText>(R.id.editTextLastName)
                val imageProfilePicture =  findViewById<CircleImageView>(R.id.imageProfilePicture)
                if (imageBytes.isNotEmpty()){
                    MediaUtils.setImageFromByteArray(imageBytes,imageProfilePicture)
                }
                textSaveChangesButton.setOnClickListener{
                    if (editTextFirstName.text.isEmpty()){
                        showErrorDialog(requireContext(),AppConstant.firstName)
                    }else if (editTextLastName.text.isEmpty()){
                        showErrorDialog(requireContext(),AppConstant.lastName)
                    }else{
                        dismiss()
                        completeProfileReq.first_name =editTextFirstName.text.toString()
                        completeProfileReq.last_name =editTextLastName.text.toString()
                        binding.textName.text = editTextFirstName.text.toString()+" "+editTextLastName.text.toString()
                    }

                }

                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                show()
            }}

     */

    // this function change the name of dialog
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
            if (imageBytes.isNotEmpty()) {
                MediaUtils.setImageFromByteArray(imageBytes, imageProfilePicture)
            }


            val textSaveChangesButton = findViewById<TextView>(R.id.textSaveChangesButton)
            val editTextFirstName = findViewById<EditText>(R.id.editTextFirstName)
            val editTextLastName = findViewById<EditText>(R.id.editTextLastName)
//        if (fullName != ""){
//            val nameParts = fullName.trim().split(" ", limit = 2)
//            editTextFirstName.setText(nameParts[0]) // First name
//            editTextLastName.setText(if (nameParts.size > 1) nameParts[1] else "")
//        }
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

            editTextFirstName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    updateFirstName = s.toString()
                    firstName = s.toString()
                }
            })

            editTextLastName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    updateLastName = s.toString()
                    lastName = s.toString()
                }
            })


            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }


    private fun dialogEmailVerification(context: Context?){
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(true)
            setContentView(R.layout.dialog_email_verification)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.WRAP_CONTENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
            var imageCross =  findViewById<ImageView>(R.id.imageCross)

            var etEmail =  findViewById<EditText>(R.id.etEmail)

            var textSubmitButton =  findViewById<TextView>(R.id.textSubmitButton)
            textSubmitButton.setOnClickListener{
                toggleLoginButtonEnabled(false, textSubmitButton)
                lifecycleScope.launch {
                    completeProfileViewModel.networkMonitor.isConnected
                        .distinctUntilChanged() // Ignore duplicate consecutive values
                        .collect { isConn ->
                            if (!isConn) {
                                showErrorDialog(
                                    requireContext(),resources.getString(R.string.no_internet_dialog_msg)
                                )
                                toggleLoginButtonEnabled(true, textSubmitButton)
                            } else {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (etEmail.text!!.isEmpty()) {
                                        etEmail.error = "Email Address required"
                                        showErrorDialog(requireContext(),AppConstant.email)
                                        toggleLoginButtonEnabled(true, textSubmitButton)
                                    }else if (!isValidEmail(etEmail.text.toString())){
                                        etEmail.error = "Invalid Email Address"
                                        showErrorDialog(requireContext(),AppConstant.invalideemail)
                                        toggleLoginButtonEnabled(true, textSubmitButton)
                                    } else {
                                        emailVerification(session?.getUserId().toString(),
                                            etEmail.text.toString(),dialog,textSubmitButton)
                                    }
                                }
                            }
                        }
                }
            }
            imageCross.setOnClickListener{
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }}
    private fun emailVerification(
        userId: String,
        email :String,
        dialog: Dialog,
        textLoginButton: TextView
    ) {
        lifecycleScope.launch {
            completeProfileViewModel.emailVerification(userId,
                email
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            dialog.dismiss()
                            val textHeaderOfOtpVerfication = "Please type the verification code send \nto $email"
                            dialogOtp(requireActivity(),"",email,textHeaderOfOtpVerfication,"email")
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


    private fun phoneVerification(
        userId: String,
        code :String,
        number :String,
        dialog: Dialog,
        textLoginButton: TextView
    ) {
        lifecycleScope.launch {
            completeProfileViewModel.phoneVerification(userId,
                code, number
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            dialog.dismiss()
                            var textHeaderOfOtpVerfication = "Please type the verification code send \nto $number"
                            dialogOtp(requireActivity(),code,number,textHeaderOfOtpVerfication,"mobile")
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
    fun dialogOtp(context: Context,code:String,number:String, textHeaderOfOtpVerfication: String,type:String){
        val dialog =  Dialog(context, R.style.BottomSheetDialog)
        dialog.apply {
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
                textResend.isClickable = true
                textResend.isEnabled = true
            }
            else {
                textResend.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.grey
                    )
                )
                textResend.isClickable = false
                textResend.isEnabled = false
            }

            textSubmitButton.setOnClickListener{
                toggleLoginButtonEnabled(false, textSubmitButton)
                lifecycleScope.launch {
                    completeProfileViewModel.networkMonitor.isConnected
                        .distinctUntilChanged()
                        .collect { isConn ->
                            if (!isConn) {
                                showErrorDialog(
                                    requireContext(),resources.getString(R.string.no_internet_dialog_msg)
                                )
                                toggleLoginButtonEnabled(true, textSubmitButton)
                            } else {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    if (findViewById<EditText>(R.id.otp_digit1).text.toString().isEmpty()&&
                                        findViewById<EditText>(R.id.otp_digit2).text.toString().isEmpty()&&
                                        findViewById<EditText>(R.id.otp_digit3).text.toString().isEmpty()&&
                                        findViewById<EditText>(R.id.otp_digit4).text.toString().isEmpty()) {
                                        showErrorDialog(requireContext(),AppConstant.otp)
                                        toggleLoginButtonEnabled(true, textSubmitButton)
                                    } else {
                                        val otp = findViewById<EditText>(R.id.otp_digit1).text.toString()+
                                                findViewById<EditText>(R.id.otp_digit2).text.toString()+
                                                findViewById<EditText>(R.id.otp_digit3).text.toString()+
                                                findViewById<EditText>(R.id.otp_digit4).text.toString()
                                        if ("mobile".equals(type)){
                                            otpVerifyPhoneVerification(session?.getUserId().toString(),otp,dialog,textSubmitButton)
                                        }
                                        if ("email".equals(type)){
                                            otpVerifyEmailVerification(session?.getUserId().toString(),otp,dialog,textSubmitButton)
                                        }
                                    }
                                }
                            }
                        }
                }
            }

            textResend.setOnClickListener{
                findViewById<EditText>(R.id.otp_digit1).text.clear()
                findViewById<EditText>(R.id.otp_digit2).text.clear()
                findViewById<EditText>(R.id.otp_digit3).text.clear()
                findViewById<EditText>(R.id.otp_digit4).text.clear()
                lifecycleScope.launch {
                    completeProfileViewModel.networkMonitor.isConnected
                        .distinctUntilChanged() // Ignore duplicate consecutive values
                        .collect { isConn ->
                            if (!isConn) {
                                showErrorDialog(
                                    requireContext(),resources.getString(R.string.no_internet_dialog_msg)
                                )
                            } else {
                                if ("email".equals(type)){
                                    if (resendEnabled) {
                                        resendEmailVerification(userId,number,textResend,
                                            rlResendLine,incorrectOtp,textTimeResend)
                                    }
                                }
                                if ("mobile".equals(type)){
                                    resendPhoneVerification(userId,code,number,textResend,
                                        rlResendLine,incorrectOtp,textTimeResend)
                                }

                            }
                        }
                }
            }
            imageCross.setOnClickListener{
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }


    private fun otpVerifyPhoneVerification(userId: String, otp: String, dialog: Dialog, text: TextView) {
        lifecycleScope.launch {
            completeProfileViewModel.otpVerifyPhoneVerification(
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

    private fun otpVerifyEmailVerification(userId: String, otp: String, dialog: Dialog, text: TextView) {
        lifecycleScope.launch {
            completeProfileViewModel.otpVerifyEmailVerification(
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
            completeProfileViewModel.phoneVerification(userId,
                code,
                number
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            rlResendLine.visibility = View.VISIBLE
                            incorrectOtp.visibility = GONE
                            countDownTimer?.cancel()
                            startCountDownTimer(requireContext(),textTimeResend,rlResendLine,textResend)
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
            completeProfileViewModel.emailVerification(userId,
                email
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            rlResendLine.visibility = View.VISIBLE
                            incorrectOtp.visibility = GONE
                            countDownTimer?.cancel()
                            startCountDownTimer(requireContext(),textTimeResend,rlResendLine,textResend)
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


    private fun startCountDownTimer(context: Context,textTimeResend : TextView,rlResendLine: RelativeLayout, textResend : TextView) {
        countDownTimer = object : CountDownTimer(60000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val f = android.icu.text.DecimalFormat("00")
                val min = (millisUntilFinished / 60000) % 60
                val sec = (millisUntilFinished / 1000) % 60
                textTimeResend.text = "${f.format(min)}:${f.format(sec)} sec"
            }

            @SuppressLint("SetTextI18n")
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

    private fun toggleLoginButtonEnabled(isEnabled: Boolean, text: TextView) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                text.isEnabled = isEnabled
            } catch (e: Exception) {
                Log.e(ErrorDialog.TAG, "exception toggleLoginButtonEnabled ${e.message}")
            }
        }
    }

    // Function to extract 'name' from a list of HasName objects
    fun <T : HasName> getNames(objectsList: MutableList<T>): MutableList<String> {
        return objectsList.map { it.name }
            .filter { it != AppConstant.unknownLocation }  // Remove "Unknown Location"
            .toMutableList()
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

            "language" ->{
                deleteLanguageApi(obj)
            }

            "Hobbies"->{
                hobbiesList.removeAt(obj)
                addHobbiesAdapter.updateHobbies(hobbiesList)
            }

            "Pets"->{
                petsList.removeAt(obj)
                addPetsAdapter.updatePets(petsList)
            }

        }
    }

    private fun deleteLanguageApi(index: Int) {
        lifecycleScope.launch {
            completeProfileViewModel.networkMonitor.isConnected
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
                                completeProfileViewModel.deleteLanguageApi(
                                    session?.getUserId().toString(),
                                    index
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            it.data?.let {
                                                    resp ->
//                                                Toast.makeText(
//                                                requireContext(),
//                                                    resp.toString(),
//                                                    Toast.LENGTH_SHORT
//                                                ).show()
                                                languageList.removeAt(index)
                                                addLanguageSpeakAdapter.updateLanguage(languageList)
                                                var savingList = mutableListOf<AddLanguageModel>()
                                                languageList.forEach {
                                                    var localeNew = AddLanguageModel(it.name,it.country)
                                                    savingList.add(localeNew)
                                                }

                                                SessionManager(requireContext()).clearLanguage()
                                                SessionManager(requireContext()).saveLanguages(requireContext(),savingList)

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




    private fun updateName(
        first_name: String,
        last_name: String,
        dialog: Dialog, textSaveChangesButton: TextView
    ) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                lifecycleScope.launch {
                    completeProfileViewModel.addUpdateName(
                        session?.getUserId().toString(),
                        first_name,
                        last_name
                    ).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                it.data?.let { resp ->
                                    showSuccessDialog(requireContext(), resp.first)
//                                    userProfile?.name = first_name + " " + last_name
//                                    binding.user = userProfile
                                    binding.textName.text = first_name + " " + last_name
                                    session?.setName(first_name + " " + last_name)
                                    Log.d("checkthisResponse", resp.toString())
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

    private fun getUserProfile() {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                LoadingUtils.showDialog(requireContext(), false)
                val session = SessionManager(requireContext())
                completeProfileViewModel.getUserProfile(session.getUserId().toString()).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            var name = ""
                            it.data?.let { resp ->
                                Log.d("TESTING_PROFILE", "HERE IN A USER PROFILE ," + resp.toString())
                                userProfile = Gson().fromJson(resp, UserProfile::class.java)
                                userProfile.let {
                                    it?.first_name?.let {
                                        name+=it+" "
                                        firstName = it
                                    }
                                    it?.last_name?.let {
                                        name+=it
                                        lastName = it
                                    }
                                    it?.about_me?.let {

                                        binding.etAboutMe.setText(it)
                                    }


                                    it?.name = name
                                    binding.user = it

                                    if (it?.profile_image != null) {
                                        Glide.with(requireContext()).asBitmap().load(BuildConfig.MEDIA_URL + it.profile_image) // User profile image URL
                                            .into(object : SimpleTarget<Bitmap>() {
                                                override fun onResourceReady(
                                                    resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
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
                                        var savingList = mutableListOf<AddLanguageModel>()
                                        languageList.forEach {
                                            var localeNew = AddLanguageModel(it.name,it.country)
                                            savingList.add(localeNew)
                                        }
                                        SessionManager(requireContext()).clearLanguage()
                                        SessionManager(requireContext()).saveLanguages(requireContext(),savingList)
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
            showErrorDialog(requireContext(), resources.getString(R.string.no_internet_dialog_msg))
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




