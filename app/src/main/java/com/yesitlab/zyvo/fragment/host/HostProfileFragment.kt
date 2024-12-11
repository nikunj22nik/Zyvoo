package com.yesitlab.zyvo.fragment.host

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.skydoves.powerspinner.PowerSpinnerView
import com.yesitlab.zyvo.AppConstant
import com.yesitlab.zyvo.DateManager.DateManager
import com.yesitlab.zyvo.OnClickListener1
import com.yesitlab.zyvo.OnLocalListener
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.activity.GuesMain
import com.yesitlab.zyvo.activity.HostMainActivity
import com.yesitlab.zyvo.activity.PlaceOpenActivity
import com.yesitlab.zyvo.activity.guest.FiltersActivity
import com.yesitlab.zyvo.activity.guest.WhereTimeActivity
import com.yesitlab.zyvo.adapter.AdapterAddPaymentCard
import com.yesitlab.zyvo.adapter.AddHobbiesAdapter
import com.yesitlab.zyvo.adapter.AddLanguageSpeakAdapter
import com.yesitlab.zyvo.adapter.AddLocationAdapter
import com.yesitlab.zyvo.adapter.AddPetsAdapter
import com.yesitlab.zyvo.adapter.AddWorkAdapter
import com.yesitlab.zyvo.adapter.selectLanguage.LocaleAdapter
import com.yesitlab.zyvo.databinding.FragmentHostProfileBinding
import com.yesitlab.zyvo.databinding.FragmentProfileBinding
import com.yesitlab.zyvo.model.AddHobbiesModel
import com.yesitlab.zyvo.model.AddLanguageModel
import com.yesitlab.zyvo.model.AddLocationModel
import com.yesitlab.zyvo.model.AddPaymentCardModel
import com.yesitlab.zyvo.model.AddPetsModel
import com.yesitlab.zyvo.model.AddWorkModel
import com.yesitlab.zyvo.utils.CommonAuthWorkUtils
import com.yesitlab.zyvo.viewmodel.PaymentViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale


@AndroidEntryPoint
class HostProfileFragment : Fragment(), OnClickListener1, OnClickListener {
    lateinit var binding :FragmentHostProfileBinding
    private lateinit var commonAuthWorkUtils: CommonAuthWorkUtils
    private lateinit var addLocationAdapter: AddLocationAdapter
    private lateinit var addWorkAdapter: AddWorkAdapter
    private lateinit var addLanguageSpeakAdapter: AddLanguageSpeakAdapter
    private lateinit var addHobbiesAdapter: AddHobbiesAdapter
    private lateinit var dateManager: DateManager
    private lateinit var addPaymentCardAdapter: AdapterAddPaymentCard
    private val paymentCardViewHolder: PaymentViewModel by lazy {
        ViewModelProvider(this)[PaymentViewModel::class.java]
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


    // For handling the result of the Autocomplete Activity
    private val startAutocomplete =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
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
            }
            else if (result.resultCode == Activity.RESULT_CANCELED) {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHostProfileBinding.inflate(LayoutInflater.from(requireContext()), container, false)
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
        binding.textAddNewPaymentCard.setOnClickListener(this)
        binding.imageInfoIcon.setOnClickListener(this)
        binding.clHead.setOnClickListener(this)
        binding.imageEditPicture.setOnClickListener(this)
        binding.imageEditName.setOnClickListener(this)
        binding.textConfirmNow.setOnClickListener(this)
        binding.textConfirmNow1.setOnClickListener(this)
        binding.textPaymentWithdraw.setOnClickListener(this)
        binding.textLanguage.setOnClickListener(this)

        adapterInitialize()

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
            val intent = Intent(requireContext(), GuesMain::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            requireActivity().finish()
        }
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


//        addHobbiesAdapter = AddHobbiesAdapter(requireContext(), hobbiesList, this)
//        binding.recyclerViewHobbies.adapter = addHobbiesAdapter
//
//        addHobbiesAdapter.updateHobbies(hobbiesList)
//
//
//        addPetsAdapter = AddPetsAdapter(requireContext(), petsList, this)
//        binding.recyclerViewPets.adapter = addPetsAdapter

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

                binding.recyclerViewPaymentCardList.visibility = View.VISIBLE
                binding.textAddNewPaymentCard.visibility = View.VISIBLE
            } else if (!isDropdownOpen) {
                binding.recyclerViewPaymentCardList.visibility = View.GONE
                binding.textAddNewPaymentCard.visibility = View.GONE

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
                    locationList.removeAt(obj)
                    addLocationAdapter.updateLocations(locationList)

                }
            }

            "work" -> {
                if (obj == workList.size - 1) {
//                    var text: String = "Add Your Work Here"
//                    dialogAddItem(text)
                } else {
                    workList.removeAt(obj)
                    addWorkAdapter.updateWork(workList)

                }
            }

            "language" -> {
                if (obj == languageList.size - 1) {
                    dialogSelectLanguage()
                } else {
                    languageList.removeAt(obj)
                    addLanguageSpeakAdapter.updateLanguage(languageList)

                }

            }

            "Hobbies" -> {
                if (obj == hobbiesList.size - 1) {
                } else {
                    hobbiesList.removeAt(obj)
                    addHobbiesAdapter.updateHobbies(hobbiesList)

                }
            }

        }
    }


//cfupg7644r
    override fun onClick(p0: View?) {
        when (p0?.id) {



            R.id.textBooking->{
                findNavController().navigate(R.id.bookingScreenHostFragment)
            }
            R.id.textCreateList->{
               startActivity(Intent(requireActivity(),PlaceOpenActivity::class.java))
            }
            R.id.textPrivacyPolicy->{
                findNavController().navigate(R.id.privacyPolicyFragment)
            }
            R.id.textTermServices->{
                findNavController().navigate(R.id.termsServicesFragment)
            }
            R.id.textLogout->{
                commonAuthWorkUtils.dialogLogOut(requireContext(),"LogOut")
            }
            R.id.textGiveFeedback->{
                findNavController().navigate(R.id.feedbackFragment)
            }
            R.id.textLanguage ->{
                findNavController().navigate(R.id.language_fragment_host)
            }
            R.id.textNotifications -> {
                findNavController().navigate(R.id.notificationFragment)
            }
            R.id.textVisitHelpCenter -> {
                var bundle = Bundle()
                bundle.putString(AppConstant.type, AppConstant.Host)
                findNavController().navigate(R.id.helpCenterFragment_host,bundle)
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
                commonAuthWorkUtils.dialogChangeName(requireContext())
            }

            R.id.textConfirmNow -> {
                commonAuthWorkUtils.dialogNumberVerification(requireContext())
                binding.textConfirmNow.visibility = View.GONE
                binding.textVerified.visibility = View.VISIBLE
            }

            R.id.textConfirmNow1 -> {
                commonAuthWorkUtils.dialogEmailVerification(requireContext(), null.toString())
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
            R.id.textPaymentWithdraw ->{
                findNavController().navigate(R.id.hostPaymentFragment)
            }
        }
    }


    private fun dialogSelectPaymentMethod() {
        val dialog1 = Dialog(requireContext(), R.style.BottomSheetDialog)
        dialog1.setContentView(R.layout.dialog_select_payment_host)

        dialog1.setCancelable(false)
        dialog1.apply {
            val togglePaymentTypeSelectButton = findViewById<ToggleButton>(R.id.togglePaymentTypeSelectButton)
            val rlBankAccount = findViewById<RelativeLayout>(R.id.rlBankAccount)
            val llDebitCard = findViewById<LinearLayout>(R.id.llDebitCard)
            val spinnermonth = findViewById<PowerSpinnerView>(R.id.spinnermonth)
            val spinneryear = findViewById<PowerSpinnerView>(R.id.spinneryear)

            val btnAddPayment = findViewById<TextView>(R.id.btnAddPayment)

            callingSelectionOfDate(spinnermonth,spinneryear)

            togglePaymentTypeSelectButton.setOnCheckedChangeListener{v1, isChecked->
                if (!isChecked){
                    llDebitCard.visibility = View.GONE
                    rlBankAccount.visibility = View.VISIBLE

                }
                else {
                    rlBankAccount.visibility = View.GONE
                    llDebitCard.visibility = View.VISIBLE
                }

            }


            btnAddPayment.setOnClickListener {
                dismiss()
                binding.textAddNewPaymentCard.visibility =View.GONE
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }

    }

    fun callingSelectionOfDate(spinnermonth: PowerSpinnerView, spinneryear: PowerSpinnerView) {
        val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        // val am_pm_list = listOf("AM","PM")
        val years = (2024..2050).toList()
        val yearsStringList = years.map { it.toString() }
        Toast.makeText(requireContext(),"Year String List: "+yearsStringList.size, Toast.LENGTH_LONG).show()
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
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.top = spacing
            }
        })

        spinneryear.layoutDirection = View.LAYOUT_DIRECTION_LTR
        spinneryear.arrowAnimate = false
        spinneryear.spinnerPopupHeight = 400
        spinneryear.setItems(yearsStringList.subList(0,16))
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
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.top = spacing
            }

        })



    }


    private val pickImageLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result -> if (result.resultCode == Activity.RESULT_OK) {
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
            .createIntent {
                intent -> pickImageLauncher.launch(intent)
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
}

// 14-01-1967
// 10-12-1971
//50100614300827


