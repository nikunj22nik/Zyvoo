package com.business.zyvo.fragment.host.hostPayout

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.business.zyvo.AppConstant
import com.business.zyvo.BaseApplication
import com.business.zyvo.DateManager.DateManager
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.LoadingUtils.Companion.showSuccessDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.ScheduleEvent
import com.business.zyvo.databinding.FragmentHostPayoutBinding
import com.business.zyvo.fragment.host.hostPayout.viewmodel.HostPayoutViewModel
import com.business.zyvo.fragment.host.placeOpen.model.PropertyResponse
import com.business.zyvo.fragment.host.placeOpen.viewModel.PlaceOpenViewModel
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.CompressImage
import com.business.zyvo.utils.MultipartUtils
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.jaiselrahman.filepicker.model.MediaFile
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@AndroidEntryPoint
class HostPayoutFragment : Fragment() {

    lateinit var binding : FragmentHostPayoutBinding
    val viewModel: HostPayoutViewModel by lazy {
        ViewModelProvider(this)[HostPayoutViewModel::class.java]
    }
    var session: SessionManager? = null
    private lateinit var userId: String
    private var closeSelectIDType = 0
    private var closeSelectCountry = 0
    private var closeSelectState = 0
    private var closeSelectCity = 0
    private var closeSelectOption = 0


    private var closeSelectIDTypeCard = 0
    private var closeSelectCountryCard = 0
    private var closeSelectStateCard = 0
    private var closeSelectCityCard = 0
    private var closeSelectOptionCard = 0
    private var imgtype: String = ""
    private var storage_permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private var storage_permissions_33 =
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA)
    private lateinit var dateManager: DateManager

    lateinit var navController: NavController
    private val mediaFiles: ArrayList<MediaFile?> = ArrayList()
    private val REQUEST_Folder = 2
    private val REQUEST_CODE_STORAGE_PERMISSION = 1
    private var filefrontid: String = "No"
    private var filebackid: String = "No"
    private var filebankid: String = "No"
    private var filefront: File? = null
    private var fileback: File? = null
    private var bankuploadfile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHostPayoutBinding.inflate(LayoutInflater.from(requireContext()),container,false)
        dateManager = DateManager(requireContext())
        session = SessionManager(requireActivity())
        userId = session?.getUserId().toString()
        lifecycleScope.launch {
            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
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
        navController = Navigation.findNavController(view)
        ActivityCompat.requestPermissions(requireActivity(), permissions(), REQUEST_CODE_STORAGE_PERMISSION)
        toggleBankAccountAndDebitCard()
        clickListener()
        spinners()
        spinnersDebitCard()
        setUpUi()
        setUpBankEvent()

    }

    private fun setUpUi() {
        binding.imguploaddocument.setOnClickListener {

            if (hasPermissions(requireContext(), *permissions())) {
                val dialog = Dialog(requireContext(), R.style.BottomSheetDialog)
                dialog.setContentView(R.layout.alert_box_gallery_pdf)
                val layoutParams = WindowManager.LayoutParams()
                layoutParams.copyFrom(dialog.window!!.attributes)
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                dialog.window!!.attributes = layoutParams
                val laygallery: LinearLayout = dialog.findViewById(R.id.lay_gallery)
                val laycamera: LinearLayout = dialog.findViewById(R.id.lay_camera)
                val view1: View = dialog.findViewById(R.id.view1)
                val laypdf: LinearLayout = dialog.findViewById(R.id.lay_pdf)
                view1.visibility = View.VISIBLE
                laycamera.visibility = View.VISIBLE
                laycamera.setOnClickListener {
                    dialog.dismiss()
                    imgtype = "camera"
                    ImagePicker.with(this)
                        .cameraOnly()
                        .crop()
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .start()
                }

                laygallery.setOnClickListener {
                    dialog.dismiss()
                    imgtype = "Gallery"
                    ImagePicker.with(this)
                        .galleryOnly()
                        .crop()
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .start()
                }

                laypdf.setOnClickListener {
                    imgtype = "pdffile"
                    dialog.dismiss()
                    fileIntentMulti()
                }

                dialog.show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please go to setting Enable Permission",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        binding.layFront.setOnClickListener {
            imgtype = "front"
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }

        binding.layBack.setOnClickListener {
            imgtype = "back"
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }
        binding.etDOB.setOnClickListener {
            DateManager(requireContext()).selectDateManager { date ->
                binding.etDOB.text = date
            }
        }
    }
    private fun fileIntentMulti() {
        val intent = Intent(requireContext(), FilePickerActivity::class.java)
        intent.putExtra(
            FilePickerActivity.CONFIGS, Configurations.Builder()
                .setCheckPermission(true)
                .setSelectedMediaFiles(mediaFiles)
                .setShowFiles(true)
                .setShowImages(false)
                .setShowAudios(false)
                .setShowVideos(false)
                .setIgnoreNoMedia(false)
                .enableVideoCapture(false)
                .enableImageCapture(false)
                .setIgnoreHiddenFile(false)
                .setMaxSelection(1)
                .build()
        )
        startActivityForResult(intent, REQUEST_Folder)
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ImagePicker.REQUEST_CODE) {
            if (data?.data != null) {
                if (imgtype.equals("front", true)) {
                    val uri = data.data!!

                    val paramName = "event_image[]"

                    filefront =  BaseApplication.getPath(requireContext(), uri)?.let { File(it) }
                    filefrontid = "Yes"
                    binding.textChooseVerificationDocument.text = filefront.toString()

                }
                if (imgtype.equals("back", true)) {
                    val uri = data.data!!
                    val paramName = "event_image[]"
                    fileback = BaseApplication.getPath(requireContext(), uri)?.let { File(it) }
                    filebackid = "Yes"
                    binding.textChooseVerificationDocumentBack.text = fileback.toString()

                }

                if (imgtype.equals("camera", true)) {
                    val uri = data.data!!
                    bankuploadfile = BaseApplication.getPath(requireContext(), uri)?.let { File(it) }
                    binding.textChooseBankProof.text = bankuploadfile.toString()
                    filebankid = "Yes"

                }
                if (imgtype.equals("Gallery", true)) {
                    val uri = data.data!!
                    bankuploadfile = BaseApplication.getPath(requireContext(), uri)?.let { File(it) }
                    filebankid = "Yes"
                    binding.textChooseBankProof.text = bankuploadfile.toString()

                }
            }
        }
        if (requestCode == REQUEST_Folder) {
            data?.let { onSelectFromFolderResult(it) }
        }
    }

    private fun onSelectFromFolderResult(data: Intent?) {
        if (data != null) {
            try {
                val files =
                    data.getParcelableArrayListExtra<MediaFile>(FilePickerActivity.MEDIA_FILES)
                Log.v("pdf", files!![0].uri.toString())
                Log.v("pdf", files[0].name.toString())
                bankuploadfile = CompressImage.from(requireContext(), files[0].uri)
                filebankid = "Yes"
                binding.textChooseBankProof.text = bankuploadfile.toString()

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Log.d("pdf not found", "no data :-" + e.message.toString())
            }
        }
    }
    private fun permissions(): Array<String> {
        val p: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            storage_permissions_33
        } else {
            storage_permissions
        }
        return p
    }
    private fun hasPermissions(context: Context, vararg permissions: String): Boolean =
        permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    private fun toggleBankAccountAndDebitCard(){


        binding.textBankAccountToggle.setOnClickListener {
            binding.textDebitCardToggle.setBackgroundColor(Color.TRANSPARENT)
            binding.textBankAccountToggle.setBackgroundResource(R.drawable.selected_green_toogle_bg)
            binding.textBankAccountToggle.setTextColor(Color.parseColor("#FFFFFF"))
            binding.textDebitCardToggle.setTextColor(Color.parseColor("#06C169"))
            binding.cvBankAccount2.visibility = View.VISIBLE
            binding.cvDebitCard3.visibility = View.GONE


        }

        binding.textDebitCardToggle.setOnClickListener {
            binding.textBankAccountToggle.setBackgroundColor(Color.TRANSPARENT)
            binding.textDebitCardToggle.setBackgroundResource(R.drawable.selected_green_toogle_bg)
            binding.textBankAccountToggle.setTextColor(Color.parseColor("#06C169"))
            binding.textDebitCardToggle.setTextColor(Color.parseColor("#FFFFFF"))
            binding.cvBankAccount2.visibility = View.GONE
            binding.cvDebitCard3.visibility = View.VISIBLE
        }


    }


    private fun clickListener(){

        binding.textAddBank.setOnClickListener {
            //  binding.cvBankAccount2.visibility = View.GONE
            // binding.cvDebitCard3.visibility = View.GONE
            // binding.llBankAccount.visibility = View.GONE
            //   binding.llSavedBankAccountDetails4.visibility = View.VISIBLE


            navController.navigateUp()
        }
        binding.textAddCardDebitCard.setOnClickListener {
            //   binding.cvBankAccount2.visibility = View.GONE
            //  binding.cvDebitCard3.visibility = View.GONE
            //    binding.llBankAccount.visibility = View.GONE
            //    binding.llSavedBankAccountDetails4.visibility = View.VISIBLE
            navController.navigateUp()
        }


        binding.imageBackIcon.setOnClickListener {
            navController.navigateUp()
        }

        binding.etMonth.setOnClickListener {

            dateManager.showMonthSelectorDialog { selectedMonth ->
                binding.etMonth.text = selectedMonth


            }


        }

        binding.etYear.setOnClickListener {
            dateManager.showYearPickerDialog { selectedYear ->
                binding.etYear.text = selectedYear.toString()
            }
        }


    }

    fun spinners(){


        binding.spinnerSelectIDType.setItems(
            listOf("Driver license", "passport")
        )


        binding.spinnerSelectIDType.setOnFocusChangeListener { _, b ->
            closeSelectIDType = if (b) {
                1
            } else {
                0
            }
        }

        when (closeSelectIDType) {
            0 -> {
                binding.spinnerSelectIDType.dismiss()
            }
            1 -> {
                binding.spinnerSelectIDType.show()
            }
        }

        binding.spinnerSelectIDType.setIsFocusable(true)

        binding.spinnerSelectIDType.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->

        }


        binding.spinnerSelectCountry.setItems(
            listOf("USA", "UK","INDIA", "BRAZIL", "RUSSIA","CHINA")
        )


        binding.spinnerSelectCountry.setOnFocusChangeListener { _, b ->
            closeSelectCountry = if (b) {
                1
            } else {
                0
            }
        }

        when (closeSelectCountry) {
            0 -> {
                binding.spinnerSelectCountry.dismiss()
            }
            1 -> {
                binding.spinnerSelectCountry.show()
            }
        }

        binding.spinnerSelectCountry.setIsFocusable(true)

        binding.spinnerSelectCountry.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->

        }

        binding.spinnerSelectState.setItems(
            listOf("UP", "MP","HARYANA", "PUNJAB", "ODISHA")
        )


        binding.spinnerSelectState.setOnFocusChangeListener { _, b ->
            closeSelectState = if (b) {
                1
            } else {
                0
            }
        }

        when (closeSelectState) {
            0 -> {
                binding.spinnerSelectState.dismiss()
            }
            1 -> {
                binding.spinnerSelectState.show()
            }
        }

        binding.spinnerSelectState.setIsFocusable(true)

        binding.spinnerSelectState.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->

        }
        binding.spinnerSelectCity.setItems(
            listOf("NEW DELHI", "MUMBAI","KANPUR", "NOIDA")
        )


        binding.spinnerSelectCity.setOnFocusChangeListener { _, b ->
            closeSelectCity = if (b) {
                1
            } else {
                0
            }
        }

        when (closeSelectCity) {
            0 -> {
                binding.spinnerSelectCity.dismiss()
            }
            1 -> {
                binding.spinnerSelectCity.show()
            }
        }

        binding.spinnerSelectCity.setIsFocusable(true)

        binding.spinnerSelectCity.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->

        }
        binding.spinnerSelectOption.setItems(
            listOf("Bank account statement", "Voided cheque","Bank letterhead")
        )


        binding.spinnerSelectOption.setOnFocusChangeListener { _, b ->
            closeSelectOption = if (b) {
                1
            } else {
                0
            }
        }

        when (closeSelectOption) {
            0 -> {
                binding.spinnerSelectOption.dismiss()
            }
            1 -> {
                binding.spinnerSelectOption.show()
            }
        }

        binding.spinnerSelectOption.setIsFocusable(true)

        binding.spinnerSelectOption.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->

        }


    }

    private fun setUpBankEvent() {
        binding.textAddBank.setOnClickListener {
            lifecycleScope.launch {
                viewModel.networkMonitor.isConnected
                    .distinctUntilChanged()
                    .collect { isConn ->
                        if (!isConn) {
                            LoadingUtils.showErrorDialog(
                                requireContext(),
                                resources.getString(R.string.no_internet_dialog_msg)
                            )
                        } else {

                            if (isValidation()) {
                                addBankApi()
                            }
                        }

                    }
            }
        }

    }

    private fun addBankApi() {
        lifecycleScope.launch {
            val userIdPart = userId
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val firstNameBody = binding.etFirstName.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val lastNameBody = binding.etLastName.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val emailBody = binding.etEmail.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val phoneBody = binding.etPhoneNumber.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())

//            val dobBody = binding.etDOB.text.toString()
//                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val dobText = binding.etDOB.text.toString() // e.g., "01-21-1998"
            val dobParts = dobText.split("-") // Splitting into [month, day, year]

            val dobList = listOf(
                MultipartBody.Part.createFormData("dob[]", dobParts[0]), // Month
                MultipartBody.Part.createFormData("dob[]", dobParts[1]), // Day
                MultipartBody.Part.createFormData("dob[]", dobParts[2])  // Year
            )
            val idTypeBody = binding.spinnerSelectIDType.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val personalIdentificationNobody =
                binding.etPersonalIdentificationNumber.text.toString().trim()
                    .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val ssnBody = binding.etSSN.text.toString().trim()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val addressBody = binding.etAddress.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val countryBody = binding.spinnerSelectCountry.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val shortStateNameBody = binding.spinnerSelectState.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val cityBody = binding.spinnerSelectCity.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val postalCodeBody = binding.etPostalCode.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val bankDocumentTypeBody = when (binding.spinnerSelectOption.getText().toString()) {
                "Bank account statement" -> "statement".toRequestBody("multipart/form-data".toMediaTypeOrNull())
                "Voided cheque" -> "cheque".toRequestBody("multipart/form-data".toMediaTypeOrNull())
                else -> "letterhead".toRequestBody("multipart/form-data".toMediaTypeOrNull())
            }
            val deviceTypeBody = "Android".toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val tokenTypeBody =
                "bank_account".toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val saveCardBody = "".toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val amountBody = "".toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val paymentTypeBody = "".toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val bankNameBody = binding.etBankName.text.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val accountHolderNameBody  = binding.etAccountHolderName.text.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val accountNumberBody  = binding.etBankAccountNumber.text.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val accountNumberConfirmationBody  = binding.etConfirmAccountNumber.text.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val routingPropertyBody  = binding.etRoutingNumber.text.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())


            val filePartFront: MultipartBody.Part? = if (filefront != null) {
                val requestBody =
                    filefront?.asRequestBody(filefront!!.extension.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("verification_document_front", filefront?.name, requestBody!!)
            } else {
                null
            }
            val filePartBack: MultipartBody.Part? = if (fileback != null) {
                val requestBody = fileback?.asRequestBody(fileback!!.extension.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("verification_document_back", fileback?.name, requestBody!!)
            } else {
                null
            }

            val filePart: MultipartBody.Part? = if (bankuploadfile != null) {
                val requestBody =
                    bankuploadfile?.asRequestBody(bankuploadfile!!.extension.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("bank_proof_document", bankuploadfile?.name, requestBody!!)
            } else {
                null
            }

            viewModel.addPayOut(
                userIdPart,
                firstNameBody,
                lastNameBody,
                emailBody,
                phoneBody,
                dobList,
                idTypeBody,
                ssnBody,
                personalIdentificationNobody,
                addressBody,
                countryBody,
                shortStateNameBody,
                cityBody,
                postalCodeBody,
                bankNameBody,
                accountHolderNameBody,
                accountNumberBody,
                accountNumberConfirmationBody,
                routingPropertyBody,
                filePart,
                filePartFront,
                filePartBack).collect{
                when (it) {

                    is NetworkResult.Success -> {
                        showSuccessDialog(requireContext(), it.data!!)
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


    fun spinnersDebitCard(){


        binding.spinnerSelectIDTypeDebitCard.setItems(
            listOf("Driver license", "Passport")
        )


        binding.spinnerSelectIDTypeDebitCard.setOnFocusChangeListener { _, b ->
            closeSelectIDTypeCard = if (b) {
                1
            } else {
                0
            }
        }

        when (closeSelectIDTypeCard) {
            0 -> {
                binding.spinnerSelectIDTypeDebitCard.dismiss()
            }
            1 -> {
                binding.spinnerSelectIDTypeDebitCard.show()
            }
        }

        binding.spinnerSelectIDTypeDebitCard.setIsFocusable(true)

        binding.spinnerSelectIDTypeDebitCard.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->

        }


        binding.spinnerSelectCountryDebitCard.setItems(
            listOf("USA", "UK","INDIA", "BRAZIL", "RUSSIA","CHINA")
        )


        binding.spinnerSelectCountryDebitCard.setOnFocusChangeListener { _, b ->
            closeSelectCountryCard = if (b) {
                1
            } else {
                0
            }
        }

        when (closeSelectCountryCard) {
            0 -> {
                binding.spinnerSelectCountryDebitCard.dismiss()
            }
            1 -> {
                binding.spinnerSelectCountryDebitCard.show()
            }
        }

        binding.spinnerSelectCountryDebitCard.setIsFocusable(true)

        binding.spinnerSelectCountryDebitCard.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->

        }

        binding.spinnerSelectStateDebitCard.setItems(
            listOf("UP", "MP","HARYANA", "PUNJAB", "ODISHA")
        )


        binding.spinnerSelectStateDebitCard.setOnFocusChangeListener { _, b ->
            closeSelectStateCard = if (b) {
                1
            } else {
                0
            }
        }

        when (closeSelectStateCard) {
            0 -> {
                binding.spinnerSelectStateDebitCard.dismiss()
            }
            1 -> {
                binding.spinnerSelectStateDebitCard.show()
            }
        }

        binding.spinnerSelectStateDebitCard.setIsFocusable(true)

        binding.spinnerSelectStateDebitCard.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->

        }
        binding.spinnerSelectCityDebitCard.setItems(
            listOf("NEW DELHI", "MUMBAI","KANPUR", "NOIDA")
        )


        binding.spinnerSelectCity.setOnFocusChangeListener { _, b ->
            closeSelectCityCard = if (b) {
                1
            } else {
                0
            }
        }

        when (closeSelectCityCard) {
            0 -> {
                binding.spinnerSelectCity.dismiss()
            }
            1 -> {
                binding.spinnerSelectCity.show()
            }
        }

        binding.spinnerSelectCity.setIsFocusable(true)

        binding.spinnerSelectCity.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->

        }
        binding.spinnerSelectOption.setItems(
            listOf("Bank account statement", "Voided cheque","Bank letterhead")
        )


        binding.spinnerSelectOption.setOnFocusChangeListener { _, b ->
            closeSelectOptionCard = if (b) {
                1
            } else {
                0
            }
        }

        when (closeSelectOptionCard) {
            0 -> {
                binding.spinnerSelectOption.dismiss()
            }
            1 -> {
                binding.spinnerSelectOption.show()
            }
        }

        binding.spinnerSelectOption.setIsFocusable(true)

        binding.spinnerSelectOption.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->

        }


    }

    private fun isValidation(): Boolean {
        if (binding.etFirstName.text.trim().toString().trim().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.firstNameError)
            return false
        } else if (binding.etLastName.text?.trim().toString().trim().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.lastNameError)
            return false
        } else if (binding.etEmail.text?.trim().toString().trim().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.emailError)
            return false
        } else if (!binding.etEmail.text?.trim().toString().contains("@")) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.validEmail)
            return false
        } else if (binding.etPhoneNumber.text?.trim().toString().trim().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.phoneError)
            return false
        } else if (binding.etPhoneNumber.text?.trim().toString().length != 10) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.validPhoneNumber)
            return false
        } else if (binding.etDOB.text?.toString().equals("MM/DD/YYYY")) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.dobError)
            return false
        } else if (binding.spinnerSelectIDType.text?.toString().equals("Select ID type")) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.selectIdTypeError)
            return false
        } else if (binding.etPersonalIdentificationNumber.text?.trim().toString().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.pINError)
            return false
        } else if (binding.etSSN.text?.trim().toString().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.SNNError)
            return false
        } else if (binding.etSSN.text?.trim().toString().length != 4) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.SNNValidError)
            return false
        } else if (binding.etAddress.text?.trim().toString().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.addressError)
            return false
        } else if (binding.spinnerSelectCountry.text?.toString().equals("Select Country")) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.countryError)
            return false
        } else if (binding.spinnerSelectState.text?.toString().equals("Select State")) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.stateError)
            return false
        } else if (binding.spinnerSelectCity.text?.toString().equals("Select City")) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.cityError)
            return false
        } else if (binding.etPostalCode.text?.trim().toString().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.postalCodeError)
            return false
        } else if (binding.etBankName.text?.trim().toString().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.bankNameError)
            return false
        } else if (binding.etAccountHolderName.text?.trim().toString().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.cardholderError)
            return false
        } else if (binding.etBankAccountNumber.text?.trim().toString().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.accountNumberError)
            return false
        } else if (binding.etConfirmAccountNumber.text?.trim().toString().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.cAccountNumberError)
            return false
        } else if (binding.etRoutingNumber.text.toString().trim().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.routingNumberError)
            return false
        } else if (filebankid.equals("No", true)) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.proofofbanError)
            return false
        } else if (filefrontid.equals("No", true)) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.frontimageError)
            return false
        } else if (filebackid.equals("No", true)) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.backimageError)
            return false
        }

        return true
    }

}