package com.yesitlab.zyvo.fragment.guest

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
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
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yesitlab.zyvo.DateManager.DateManager
import com.yesitlab.zyvo.OnClickListener1
import com.yesitlab.zyvo.OnLocalListener
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.activity.GuesMain
import com.yesitlab.zyvo.activity.guest.FiltersActivity
import com.yesitlab.zyvo.activity.guest.WhereTimeActivity
import com.yesitlab.zyvo.adapter.AdapterAddPaymentCard
import com.yesitlab.zyvo.adapter.AddHobbiesAdapter
import com.yesitlab.zyvo.adapter.AddLanguageSpeakAdapter
import com.yesitlab.zyvo.adapter.AddLocationAdapter
import com.yesitlab.zyvo.adapter.AddPetsAdapter
import com.yesitlab.zyvo.adapter.AddWorkAdapter
import com.yesitlab.zyvo.adapter.selectLanguage.LocaleAdapter
import com.yesitlab.zyvo.databinding.FragmentProfileBinding
import com.yesitlab.zyvo.model.AddHobbiesModel
import com.yesitlab.zyvo.model.AddLanguageModel
import com.yesitlab.zyvo.model.AddLocationModel
import com.yesitlab.zyvo.model.AddPaymentCardModel
import com.yesitlab.zyvo.model.AddPetsModel
import com.yesitlab.zyvo.model.AddWorkModel
import com.yesitlab.zyvo.utils.CommonAuthWorkUtils
import com.yesitlab.zyvo.viewmodel.ImagePopViewModel
import com.yesitlab.zyvo.viewmodel.PaymentViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.w3c.dom.Text
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


        addPaymentCardAdapter = AdapterAddPaymentCard(requireContext(), mutableListOf())
        binding.recyclerViewPaymentCardList.adapter = addPaymentCardAdapter
        paymentCardViewHolder.paymentCardList.observe(viewLifecycleOwner, Observer { payment ->
            addPaymentCardAdapter.updateItem(payment)
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        paymentOpenCloseDropDown()
        binding.textGiveFeedback.setOnClickListener(this)
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


        adapterInitialize()

        // Initialize Places API if not already initialized
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity(), apiKey)
        }

        // Set listeners


        binding.filterIcon.setOnClickListener {
            startActivity(Intent(requireActivity(),FiltersActivity::class.java))
        }

        binding.rlFind.setOnClickListener {

            startActivity(Intent(requireActivity(),WhereTimeActivity::class.java))
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

            "Pets" -> {
                if (obj == petsList.size - 1) {


                } else {
                    petsList.removeAt(obj)
                    addPetsAdapter.updatePets(petsList)

                }
            }

        }

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
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


            R.id.textNotifications -> {
                findNavController().navigate(R.id.notificationFragment)
            }

            R.id.textVisitHelpCenter -> {
                findNavController().navigate(R.id.helpCenterFragment)
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
                commonAuthWorkUtils.dialogEmailVerification(requireContext())
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
                dialogAddCard()
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
                    }

                    imageStatus = "1"
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

}