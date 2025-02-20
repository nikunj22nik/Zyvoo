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
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
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
import com.business.zyvo.AppConstant
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
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
import com.business.zyvo.utils.MediaUtils
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.hbb20.CountryCodePicker
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.Locale
@AndroidEntryPoint
class CompleteProfileFragment : Fragment(),OnClickListener1, onItemClickData , OnClickListener{

    private lateinit var binding: FragmentCompleteProfileBinding
    private  lateinit var  commonAuthWorkUtils: CommonAuthWorkUtils
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
    private lateinit var apiKey: String
    private lateinit var localeAdapter: LocaleAdapter
    private var locales: List<Locale> = listOf()
    private var etSearch: TextView? = null
    private  var bottomSheetDialog : BottomSheetDialog? = null
    private var imageStatus = ""

    lateinit  var navController :NavController
    private lateinit var otpDigits: Array<EditText>
    private var countDownTimer: CountDownTimer? = null
    var resendEnabled = false
    var userId:String = ""
    var token:String = ""
    var type:String = ""
    var email:String = ""
    var session: SessionManager?=null
    var imageBytes: ByteArray = byteArrayOf()
    val completeProfileReq = CompleteProfileReq()



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
                    locationList.add(0, newLocation)

                 //   Toast.makeText(requireContext(),"count${locationList.size}",Toast.LENGTH_LONG).show()

//                    // Check if we need to hide the "Add New" button

                    //  addLocationAdapter.updateLocations(locationList)  // Notify adapter here
                    addLocationAdapter.notifyItemInserted(0)
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


    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCompleteProfileBinding.inflate(inflater, container, false)
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

        adapterInitialize()

        // Initialize Places API if not already initialized
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity(), apiKey)
        }

        // Set listeners
        setCheckVerified()


    }

    private fun setCheckVerified() {
        if ("mobile".equals(type)){
            binding.textConfirmNow1.visibility = View.GONE
            binding.textVerified1.visibility = View.VISIBLE
        }
        if ("email".equals(type)){
            binding.textConfirmNow.visibility = View.GONE
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
            localeAdapter = LocaleAdapter(locales, object : OnLocalListener{
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
           R.id.imageInfoIcon->{
               binding.cvInfo.visibility = View.VISIBLE
           }
           R.id.clHead->{
               binding.cvInfo.visibility = View.GONE
           }
           R.id.imageEditPicture->{
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
                                   if (validation()) {
                                       completeProfileReq.user_id = userId.toInt()
                                       completeProfileReq.about_me = binding.etAboutMe.text.toString()
                                       completeProfileReq.where_live = getNames(locationList)
                                       completeProfileReq.works = getNames(workList)
                                       completeProfileReq.languages = getNames(languageList)
                                       completeProfileReq.hobbies = getNames(hobbiesList)
                                       completeProfileReq.pets = getNames(petsList)
                                       completeProfileReq.bytes = imageBytes
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
               var intent = Intent(requireContext(),GuesMain::class.java)
               intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
               startActivity(intent)
               requireActivity().finish()
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
                                    } else {
                                        val phoneNumber = etMobileNumber.text.toString()
                                        Log.d(ErrorDialog.TAG, phoneNumber)
                                        val countryCode =
                                            countyCodePicker.selectedCountryCodeWithPlus
                                        Log.d(ErrorDialog.TAG, countryCode)
                                        phoneVerification(userId,countryCode, phoneNumber ,dialog,textSubmitButton)
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


    private fun dialogEmailVerification(context: Context?){
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
                                    } else {
                                        emailVerification(userId,etEmail.text.toString(),dialog,textSubmitButton)
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
                code,
                number
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
            } else {
                textResend.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.grey
                    )
                )
            }

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
                                            otpVerifyPhoneVerification(userId,otp,dialog,textSubmitButton)
                                        }
                                        if ("email".equals(type)){
                                            otpVerifyEmailVerification(userId,otp,dialog,textSubmitButton)
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
                            binding.textConfirmNow1.visibility = View.GONE
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

    private fun otpVerifyEmailVerification(userId: String, otp: String, dialog: Dialog, text: TextView) {
        lifecycleScope.launch {
            completeProfileViewModel.otpVerifyEmailVerification(
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
            completeProfileViewModel.phoneVerification(userId,
                code,
                number
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            rlResendLine.visibility = View.VISIBLE
                            incorrectOtp.visibility = View.GONE
                            countDownTimer?.cancel()
                            startCountDownTimer(requireContext(),textTimeResend,rlResendLine,textResend)
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
            completeProfileViewModel.emailVerification(userId,
                email
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            rlResendLine.visibility = View.VISIBLE
                            incorrectOtp.visibility = View.GONE
                            countDownTimer?.cancel()
                            startCountDownTimer(requireContext(),textTimeResend,rlResendLine,textResend)
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


    private fun startCountDownTimer(context: Context,textTimeResend : TextView,rlResendLine: RelativeLayout, textResend : TextView) {
        countDownTimer = object : CountDownTimer(120000, 1000) {
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
                languageList.removeAt(obj)
                addLanguageSpeakAdapter.updateLanguage(languageList)

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

}
