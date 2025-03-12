package com.business.zyvo.fragment.host.hostPayout

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import com.business.zyvo.databinding.FragmentHostPayoutBinding
import com.business.zyvo.fragment.host.hostPayout.viewmodel.HostPayoutViewModel
import com.business.zyvo.model.StateModel
import com.business.zyvo.model.host.CountryModel
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.CompressImage
import com.business.zyvo.utils.MultipartUtils
import com.github.dhaval2404.imagepicker.ImagePicker
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.jaiselrahman.filepicker.model.MediaFile
import com.stripe.android.ApiResultCallback
import com.stripe.android.Stripe
import com.stripe.android.model.CardParams
import com.stripe.android.model.Token
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@AndroidEntryPoint
class HostPayoutFragment : Fragment() {

    lateinit var binding: FragmentHostPayoutBinding
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
    private val PICK_FILE_REQUEST_CODE = 1001
    private var selectedFileUri: Uri? = null

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
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.CAMERA
        )
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
    private val REQUEST_CODE = 11
    private var bankuploadMultipart: MultipartBody.Part? = null
    private var countriesList: MutableList<CountryModel> = mutableListOf()
    private var countriesListStr: MutableList<String> = mutableListOf()
    private var countryCode: String = ""
    private var statetCode: String = ""
    private var cityCode: String = ""
    private var stateList: MutableList<StateModel> = mutableListOf()
    private var stateListStr: MutableList<String> = mutableListOf()
    private var cityListStr: MutableList<String> = mutableListOf()

    private var filefrontCardid: String = "No"
    private var filebackCardid: String = "No"
    private var filefrontCard: File? = null
    private var filebackCard: File? = null

    private var countriesListCard: MutableList<CountryModel> = mutableListOf()
    private var countriesListStrCard: MutableList<String> = mutableListOf()
    private var countryCodeCard: String = ""
    private var statetCodeCard: String = ""
    private var cityCodeCard: String = ""
    private var stateListCard: MutableList<StateModel> = mutableListOf()
    private var stateListStrCard: MutableList<String> = mutableListOf()
    private var cityListStrCard: MutableList<String> = mutableListOf()
    private var imgtypeCard: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHostPayoutBinding.inflate(
            LayoutInflater.from(requireContext()),
            container,
            false
        )
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

            callingGetCountryApi()

        }
        return binding.root
    }

    private fun callingGetCountryApi() {
        lifecycleScope.launch {
            LoadingUtils.showDialog(requireContext(), false)
            viewModel.getCountries().collect {
                when (it) {
                    is NetworkResult.Success -> {
                        countriesList = it.data!!

                        countriesListStr = viewModel.getCountriesList(countriesList)

                        binding.spinnerSelectCountry.setItems(countriesListStr)
                        binding.spinnerSelectCountryDebitCard.setItems(countriesListStr)
                        LoadingUtils.hideDialog()
                    }

                    is NetworkResult.Error -> {
                        LoadingUtils.hideDialog()
                    }

                    else -> {

                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        ActivityCompat.requestPermissions(
            requireActivity(),
            permissions(),
            REQUEST_CODE_STORAGE_PERMISSION
        )
        toggleBankAccountAndDebitCard()
        clickListener()
        spinners()
        spinnersDebitCard()
        setUpUi()
        setUpBankEvent()
        setUpCardEvent()

    }

    private fun setUpUi() {
        binding.imguploaddocument.setOnClickListener {
            //if (hasPermissions(requireContext(), *permissions())) {
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
                //fileIntentMulti()
                //  onUploadPdfClick()
                openFilePicker()
            }

            dialog.show()
//            } else {
//                Toast.makeText(
//                    requireContext(),
//                    "Please go to setting Enable Permission",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }

            //  onUploadPdfClick()
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



        binding.layFrontCard.setOnClickListener {
            imgtypeCard = "front"
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }
        binding.layBackCard.setOnClickListener {
            imgtypeCard = "back"
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }
        binding.etDOBDebitCard.setOnClickListener {
            DateManager(requireContext()).selectDateManager { date ->
                binding.etDOBDebitCard.text = date
            }
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

    private fun openFilePicker() {
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.type = "*/*"
//        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST_CODE)
//
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        // Allow only specific MIME types for documents
        intent.type = "application/*"
        intent.putExtra(
            Intent.EXTRA_MIME_TYPES,
            arrayOf(
                "application/pdf"
            )
        )
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST_CODE)


    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ImagePicker.REQUEST_CODE) {
            if (data?.data != null) {
                if (imgtype.equals("front", true)) {
                    val uri = data.data!!
                    val paramName = "event_image[]"
                    filefront = BaseApplication.getPath(requireContext(), uri)?.let { File(it) }
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
                    bankuploadfile =
                        BaseApplication.getPath(requireContext(), uri)?.let { File(it) }
//                    bankuploadMultipart  = MultipartUtils.uriToMultipartBodyPart(requireContext(),uri,"bank_proof_document")
//                    var fileName = ""
//                    fileName = getFileNameFromUri(requireContext(), uri).toString()
//                    Log.d("FileName", "Selected File Name: $fileName")
//                    if (fileName != "") {
//                        binding.textChooseBankProof.text = fileName
//                    }

                    binding.textChooseBankProof.text = bankuploadfile.toString()
                    filebankid = "Yes"
                }

                if (imgtype.equals("Gallery", true)) {
                    val uri = data.data!!
                    // bankuploadfile = BaseApplication.getPath(requireContext(), uri)?.let { File(it) }
                    //  bankuploadMultipart  = MultipartUtils.uriToMultipartBodyPart(requireContext(),uri,"bank_proof_document")
                    filebankid = "Yes"
                    bankuploadfile =
                        BaseApplication.getPath(requireContext(), uri)?.let { File(it) }

                    binding.textChooseBankProof.text = bankuploadfile.toString()

                }


                if (imgtypeCard.equals("front", true)) {
                    val uri = data.data!!
                    filefrontCard = BaseApplication.getPath(requireContext(), uri)?.let { File(it) }
                    filefrontCardid = "Yes"
                    binding.textChooseVerificationDocumentDebitCard.text = filefrontCard.toString()
                }
                if (imgtypeCard.equals("back", true)) {
                    val uri = data.data!!
                    filebackCard = BaseApplication.getPath(requireContext(), uri)?.let { File(it) }
                    filebackCardid = "Yes"
                    binding.textChooseVerificationDocumentBackDebitCard.text =
                        filebackCard.toString()
                }


            }
        }
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                Log.d("uriVipin", uri.toString())

                val mimeType = context?.contentResolver?.getType(uri)
                if (isValidMimeType(mimeType)) {
                    selectedFileUri = uri
//                    var fileName = ""
//                    fileName = getFileNameFromUri(requireContext(), uri).toString()
//                    Log.d("FileName", "Selected File Name: $fileName")
//                    if (fileName != "") {
//                        binding.textChooseBankProof.text = fileName
//                    }
//val addpart = MultipartUtils.uriToMultipartBodyPart(requireContext(),uri,"bank_proof_document")
//                    bankuploadMultipart  = addpart
//Log.d("bankuploadMultipart",bankuploadMultipart.toString())

                    filebankid = "Yes"
                    bankuploadfile = resolveFile(requireContext(), uri)
                    //   bankuploadfile = BaseApplication.getPath(requireContext(), uri)?.let { File(it) }

                    binding.textChooseBankProof.text = bankuploadfile.toString()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please select a valid PDF document.",
                        Toast.LENGTH_LONG
                    ).show()

                }
            }
        }
    }

    @SuppressLint("Range")
    private fun getFileName(context: Context, uri: Uri): String {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        return uri.lastPathSegment ?: "unknown_file"
    }

    private fun resolveFile(context: Context, uri: Uri): File? {
        return try {
            val path = BaseApplication.getPath(context, uri)
            if (path != null) {
                Log.d("AddResumeFragment", "Resolved Path: $path")
                File(path)
            } else {
                // Fallback: Copy to cache directory
                val inputStream = context.contentResolver.openInputStream(uri)
                val tempFile = File(context.cacheDir, getFileName(context, uri))
                inputStream.use { input ->
                    tempFile.outputStream().use { output ->
                        input?.copyTo(output)
                    }
                }
                tempFile
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        try {
            var fileName: String? = null
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = it.getString(nameIndex)
                    }
                }
            }
            return fileName
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    private fun isValidMimeType(mimeType: String?): Boolean {
        return mimeType == "application/pdf" ||
                mimeType == "application/msword" ||
                mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    }

    /*
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

     */


    private fun permissions(): Array<String> {
        val p: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            storage_permissions_33
        } else {
            storage_permissions
        }
        return p

    }


    private fun toggleBankAccountAndDebitCard() {
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


    private fun clickListener() {

//        binding.textAddBank.setOnClickListener {
//            //  binding.cvBankAccount2.visibility = View.GONE
//            // binding.cvDebitCard3.visibility = View.GONE
//            // binding.llBankAccount.visibility = View.GONE
//            //   binding.llSavedBankAccountDetails4.visibility = View.VISIBLE
//
//
//
//        }
//        binding.textAddCardDebitCard.setOnClickListener {
//            //   binding.cvBankAccount2.visibility = View.GONE
//            //  binding.cvDebitCard3.visibility = View.GONE
//            //    binding.llBankAccount.visibility = View.GONE
//            //    binding.llSavedBankAccountDetails4.visibility = View.VISIBLE
//
//        }


        binding.imageBackIcon.setOnClickListener {
            navController.navigateUp()
        }

    }

    fun spinners() {
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
            countryCode = countriesList.get(newIndex).iso2
            selectState(countryCode)
        }

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
            statetCode = stateList.get(newIndex).iso2
            callingCityApi()
        }

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
            listOf("Bank account statement", "Voided cheque", "Bank letterhead")
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

    private fun selectState(code: String) {
        lifecycleScope.launch {
            LoadingUtils.showDialog(requireContext(), false)
            viewModel.getState(code).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        stateList = it.data!!
                        stateListStr = viewModel.getStateList(stateList)
                        binding.spinnerSelectState.setItems(
                            stateListStr
                        )
                        binding.spinnerSelectStateDebitCard.setItems(
                            stateListStr
                        )

                        LoadingUtils.hideDialog()
                    }

                    is NetworkResult.Error -> {
                        LoadingUtils.hideDialog()

                    }

                    else -> {
                        LoadingUtils.hideDialog()

                    }

                }
            }
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

            val dobText = binding.etDOBDebitCard.text.toString() // e.g., "01-21-1998"
            val dobParts = dobText.split("-") // Splitting into [month, day, year]

            val dobList = listOf(
                MultipartBody.Part.createFormData("dob[]", dobParts[0].toInt().toString()), // Month
                MultipartBody.Part.createFormData("dob[]", dobParts[1].toInt().toString()), // Day
                MultipartBody.Part.createFormData("dob[]", dobParts[2].toInt().toString())  // Year
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
            val countryBody = countryCode.toRequestBody("multipart/form-data".toMediaTypeOrNull())

            val shortStateNameBody =
                statetCode.toRequestBody("multipart/form-data".toMediaTypeOrNull())

            val cityBody = cityCode.toRequestBody("multipart/form-data".toMediaTypeOrNull())

            val postalCodeBody = binding.etPostalCode.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())

            val bankDocumentTypeBody = when (binding.spinnerSelectOption.text.toString()) {
                "Bank account statement" -> "bank_statement".toRequestBody("multipart/form-data".toMediaTypeOrNull())
                "Voided cheque" -> "voided_check".toRequestBody("multipart/form-data".toMediaTypeOrNull())
                else -> "bank_letterhead".toRequestBody("multipart/form-data".toMediaTypeOrNull())

            }


            val bankNameBody = binding.etBankName.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val accountHolderNameBody = binding.etAccountHolderName.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val accountNumberBody = binding.etBankAccountNumber.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val accountNumberConfirmationBody = binding.etConfirmAccountNumber.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val routingPropertyBody = binding.etRoutingNumber.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())


            val filePartFront: MultipartBody.Part? = if (filefront != null) {
                val requestBody =
                    filefront?.asRequestBody(filefront!!.extension.toMediaTypeOrNull())
                MultipartBody.Part.createFormData(
                    "verification_document_front",
                    filefront?.name,
                    requestBody!!
                )
            } else {
                null
            }
            val filePartBack: MultipartBody.Part? = if (fileback != null) {
                val requestBody = fileback?.asRequestBody(fileback!!.extension.toMediaTypeOrNull())
                MultipartBody.Part.createFormData(
                    "verification_document_back",
                    fileback?.name,
                    requestBody!!
                )
            } else {
                null
            }

            val filePart: MultipartBody.Part? = if (bankuploadfile != null) {
                val requestBody =
                    bankuploadfile?.asRequestBody(bankuploadfile!!.extension.toMediaTypeOrNull())
                MultipartBody.Part.createFormData(
                    "bank_proof_document",
                    bankuploadfile?.name,
                    requestBody!!
                )
            } else {
                null
            }

//            val filePart: MultipartBody.Part = bankuploadfile.let {
//                val requestFile = bankuploadfile!!.asRequestBody("application/pdf".toMediaTypeOrNull())
//                MultipartBody.Part.createFormData("bank_proof_document", bankuploadfile!!.path, requestFile)
//            }

            viewModel.addPayOut(
                userIdPart, firstNameBody, lastNameBody, emailBody, phoneBody,
                dobList, idTypeBody, ssnBody, personalIdentificationNobody, addressBody,
                countryBody, shortStateNameBody, cityBody, postalCodeBody, bankNameBody,
                accountHolderNameBody, accountNumberBody, accountNumberConfirmationBody,
                routingPropertyBody, bankDocumentTypeBody, filePart, filePartFront,
                filePartBack
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        showSuccessDialog(requireContext(), it.data!!)
                        navController.navigateUp()
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


    fun spinnersDebitCard() {

        binding.spinnerSelectIDTypeDebitCard.setItems(
            listOf("Driver license", "passport")
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


//        binding.spinnerSelectCountryDebitCard.setItems(
//            listOf("USA", "UK","INDIA", "BRAZIL", "RUSSIA","CHINA")
//        )

        /*
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

                binding.spinnerSelectState.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->
                    statetCode = stateList.get(newIndex).iso2
                    callingCityApi()
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
                    cityCode = cityListStr.get(newIndex)

                }

         */
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
            countryCode = countriesList.get(newIndex).iso2
            selectState(countryCode)
        }

//        binding.spinnerSelectState.setItems(
//            listOf("UP", "MP","HARYANA", "PUNJAB", "ODISHA")
//        )


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
            statetCode = stateList.get(newIndex).iso2
            callingCityApi()
        }

        binding.spinnerSelectCityDebitCard.setOnFocusChangeListener { _, b ->
            closeSelectCityCard = if (b) {
                1
            } else {
                0
            }
        }

        when (closeSelectCityCard) {
            0 -> {
                binding.spinnerSelectCityDebitCard.dismiss()
            }

            1 -> {
                binding.spinnerSelectCityDebitCard.show()
            }
        }

        binding.spinnerSelectCityDebitCard.setIsFocusable(true)

        binding.spinnerSelectCityDebitCard.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->
            cityCode = cityListStr.get(newIndex)
        }

        binding.spinnerSelectOption.setItems(
            listOf("Bank account statement", "Voided cheque", "Bank letterhead")
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

    private fun callingCityApi() {
        lifecycleScope.launch {
            LoadingUtils.showDialog(requireContext(), false)
            Log.d("TESTING_CODE", "CountryCode is " + countryCode + " State Code is" + statetCode)
            viewModel.getCityName(countryCode, statetCode).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        LoadingUtils.hideDialog()
                        cityListStr = it.data!!
                        binding.spinnerSelectCity.setItems(cityListStr)
                        binding.spinnerSelectCityDebitCard.setItems(cityListStr)
                    }

                    is NetworkResult.Error -> {
                        LoadingUtils.hideDialog()
                        Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_SHORT)
                            .show()
                    }

                    else -> {
                        LoadingUtils.hideDialog()
                    }
                }
            }
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

    private fun setUpCardEvent() {
        binding.textAddCardDebitCard.setOnClickListener {
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

                            if (isValidationCard()) {
                                val cNumber = binding.etCardNumber.text.toString().trim()
                                val monthName = binding.etMonth.text.toString().trim()
                                val month = getMonthNumber(monthName)
                                val year = binding.etYear.text.toString().trim()
                                val cvv = binding.etCVVNumber.text.toString().trim()
                                val firstName = binding.etFirstNameDebitCard.text.toString().trim()
                                val lastName = binding.etLastNameDebitCard.text.toString().trim()
                                val fullName = firstName + lastName
                                val stripe = Stripe(
                                    requireContext(),
                                    "pk_test_51QnHZl2Nd862ZJtETiUKw9fMnacKnSy3u27rwJzDsDzGoKV7yFcHWW7Zy68KXflyGZqc5Cjm2ChdpWlaE72R0fp200DSuioFyd"
                                )
                                val card = CardParams(
                                    cNumber,
                                    Integer.valueOf(month),
                                    Integer.valueOf(year),
                                    cvv,
                                    fullName,
                                    currency = "usd"
                                )
                                // countryCode
                                val curency = getCurrencyByCountry(countryCode)
                                Log.d("CurrnecyName" , curency)
                                stripe.createCardToken(card, null, null, object :
                                    ApiResultCallback<Token> {
                                    override fun onError(e: Exception) {
                                        Log.d("PaymentActivity1", "data$e")

                                        Toast.makeText(
                                            requireContext(),
                                            e.message.toString(),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                    override fun onSuccess(result: Token) {
                                        val token = result.id
                                        Log.d("Token payment :-", "$token")

                                        addCardApi(token)
                                    }
                                })

                            }
                        }

                    }
            }
        }

    }

    fun getCurrencyByCountry(countryCode: String): String {
        return when (countryCode.uppercase()) {
            "US" -> "usd"  // United States
            "GB" -> "gbp"  // United Kingdom
            "CA" -> "cad"  // Canada
            "EU", "FR", "DE", "IT", "ES", "NL", "BE", "AT", "PT", "FI", "IE", "LU", "GR" -> "eur"  // Eurozone countries
            "AU" -> "aud"  // Australia
            "NZ" -> "nzd"  // New Zealand
            "IN" -> "inr"  // India
            "JP" -> "jpy"  // Japan
            "CN" -> "cny"  // China
            "HK" -> "hkd"  // Hong Kong
            "SG" -> "sgd"  // Singapore
            "KR" -> "krw"  // South Korea
            "BR" -> "brl"  // Brazil
            "MX" -> "mxn"  // Mexico
            "ZA" -> "zar"  // South Africa
            "CH" -> "chf"  // Switzerland
            "SE" -> "sek"  // Sweden
            "DK" -> "dkk"  // Denmark
            "NO" -> "nok"  // Norway
            "PL" -> "pln"  // Poland
            "CZ" -> "czk"  // Czech Republic
            "HU" -> "huf"  // Hungary
            "MY" -> "myr"  // Malaysia
            "TH" -> "thb"  // Thailand
            "ID" -> "idr"  // Indonesia
            "PH" -> "php"  // Philippines
            "AE" -> "aed"  // United Arab Emirates
            "SA" -> "sar"  // Saudi Arabia
            "IL" -> "ils"  // Israel
            "TR" -> "try"  // Turkey
            "RU" -> "rub"  // Russia
            "NG" -> "ngn"  // Nigeria
            "AR" -> "ars"  // Argentina
            "CO" -> "cop"  // Colombia
            "CL" -> "clp"  // Chile
            "PK" -> "pkr"  // Pakistan
            "BD" -> "bdt"  // Bangladesh
            "VN" -> "vnd"  // Vietnam
            "EG" -> "egp"  // Egypt
            "KE" -> "kes"  // Kenya
            "GH" -> "ghs"  // Ghana
            "UA" -> "uah"  // Ukraine
            else -> "usd"  // Default to USD
        }
    }

    private fun getMonthNumber(monthName: String): String {
        val months = mapOf(
            "January" to "01",
            "February" to "02",
            "March" to "03",
            "April" to "04",
            "May" to "05",
            "June" to "06",
            "July" to "07",
            "August" to "08",
            "September" to "09",
            "October" to "10",
            "November" to "11",
            "December" to "12"
        )

        return months[monthName] ?: "00"  // Default "00" if input is invalid
    }


    private fun addCardApi(token: String) {
        lifecycleScope.launch {
            val userIdPart = userId
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val tokenBody = token
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())

            val firstNameBody = binding.etFirstNameDebitCard.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val lastNameBody = binding.etLastNameDebitCard.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val emailBody = binding.etEmailDebitCard.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())

            val dobText = binding.etDOBDebitCard.text.toString() // e.g., "01-21-1998"
            val dobParts = dobText.split("-") // Splitting into [month, day, year]

            val dobList = listOf(
                MultipartBody.Part.createFormData("dob[]", dobParts[0].toInt().toString()), // Month
                MultipartBody.Part.createFormData("dob[]", dobParts[1].toInt().toString()), // Day
                MultipartBody.Part.createFormData("dob[]", dobParts[2].toInt().toString())  // Year
            )
            val ssnBody = binding.etSSNDebitCard.text.toString().trim()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())

            val phoneBody = binding.etPhoneNumberDebitCard.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())

            val addressBody = binding.etAddressDebitCard.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())

            val cityBody = cityCode.toRequestBody("multipart/form-data".toMediaTypeOrNull())

            val shortStateNameBody =
                statetCode.toRequestBody("multipart/form-data".toMediaTypeOrNull())


            val countryBody = countryCode.toRequestBody("multipart/form-data".toMediaTypeOrNull())

            val postalCodeBody = binding.etPostalCodeDebitCard.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())


            val idTypeBody = binding.spinnerSelectIDTypeDebitCard.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val personalIdentificationNobody =
                binding.etPersonalIdentificationNumberDebitCard.text.toString().trim()
                    .toRequestBody("multipart/form-data".toMediaTypeOrNull())


            val filePartFront: MultipartBody.Part? = if (filefrontCard != null) {
                val requestBody =
                    filefrontCard?.asRequestBody(filefrontCard!!.extension.toMediaTypeOrNull())
                MultipartBody.Part.createFormData(
                    "verification_document_front",
                    filefrontCard?.name,
                    requestBody!!
                )
            } else {
                null
            }
            val filePartBack: MultipartBody.Part? = if (filebackCard != null) {
                val requestBody =
                    filebackCard?.asRequestBody(filebackCard!!.extension.toMediaTypeOrNull())
                MultipartBody.Part.createFormData(
                    "verification_document_back",
                    filebackCard?.name,
                    requestBody!!
                )
            } else {
                null
            }


            viewModel.addPayOutCard(
                userIdPart,
                tokenBody,
                firstNameBody,
                lastNameBody,
                emailBody,
                dobList,
                ssnBody,
                phoneBody,
                addressBody,
                cityBody,
                shortStateNameBody,
                countryBody,
                postalCodeBody,
                idTypeBody,
                personalIdentificationNobody,
                filePartFront,
                filePartBack
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        showSuccessDialog(requireContext(), it.data!!)
                        navController.navigateUp()
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


    private fun isValidationCard(): Boolean {
        if (binding.etFirstNameDebitCard.text.trim().toString().trim().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.firstNameError)
            return false
        } else if (binding.etLastNameDebitCard.text?.trim().toString().trim().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.lastNameError)
            return false
        } else if (binding.etEmailDebitCard.text?.trim().toString().trim().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.emailError)
            return false
        } else if (!binding.etEmailDebitCard.text?.trim().toString().contains("@")) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.validEmail)
            return false
        } else if (binding.etPhoneNumberDebitCard.text?.trim().toString().trim().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.phoneError)
            return false
        } else if (binding.etPhoneNumberDebitCard.text?.trim().toString().length != 10) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.validPhoneNumber)
            return false
        } else if (binding.etDOBDebitCard.text?.toString().equals("MM/DD/YYYY")) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.dobError)
            return false
        } else if (binding.spinnerSelectIDTypeDebitCard.text?.toString().equals("Select ID type")) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.selectIdTypeError)
            return false
        } else if (binding.etPersonalIdentificationNumberDebitCard.text?.trim().toString()
                .isEmpty()
        ) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.pINError)
            return false
        } else if (binding.etSSNDebitCard.text?.trim().toString().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.SNNError)
            return false
        } else if (binding.etSSNDebitCard.text?.trim().toString().length != 4) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.SNNValidError)
            return false
        } else if (binding.etAddressDebitCard.text?.trim().toString().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.addressError)
            return false
        } else if (binding.spinnerSelectCountryDebitCard.text?.toString()
                .equals("Select Country")
        ) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.countryError)
            return false
        } else if (binding.spinnerSelectStateDebitCard.text?.toString().equals("Select State")) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.stateError)
            return false
        } else if (binding.spinnerSelectCityDebitCard.text?.toString().equals("Select City")) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.cityError)
            return false
        } else if (binding.etPostalCodeDebitCard.text?.trim().toString().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.postalCodeError)
            return false
        } else if (binding.etCardNumber.text?.trim().toString().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), "Card number cannot be empty")
            return false
        } else if (!isValidCardNumber(binding.etCardNumber.text?.trim().toString())) {
            LoadingUtils.showErrorDialog(
                requireContext(),
                "Invalid card number. Please enter a valid card number."
            )
            return false
        } else if (binding.etCVVNumber.text?.trim().toString().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), "CVV cannot be empty")
            return false
        } else if (!isValidCVV(
                binding.etCVVNumber.text?.trim().toString(),
                binding.etCardNumber.text?.trim().toString()
            )
        ) {
            LoadingUtils.showErrorDialog(requireContext(), "Invalid CVV. Please enter a valid CVV.")
            return false
        } else if (binding.etMonth.text?.trim().toString().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.postalCodeError)
            return false
        } else if (binding.etYear.text?.trim().toString().isEmpty()) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.postalCodeError)
            return false
        } else if (filefrontCardid.equals("No", true)) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.frontimageError)
            return false
        } else if (filebackCardid.equals("No", true)) {
            LoadingUtils.showErrorDialog(requireContext(), AppConstant.backimageError)
            return false
        }

        return true
    }

    private fun isValidCardNumber(cardNumber: String): Boolean {
        if (cardNumber.isEmpty()) return false

        // Define valid card lengths for USA & Europe
        val validLengths = listOf(13, 15, 16, 19)

        if (cardNumber.length !in validLengths) {
            return false
        }

        return isLuhnValid(cardNumber)
    }

    // Luhn Algorithm to validate credit/debit card number
    private fun isLuhnValid(cardNumber: String): Boolean {
        var sum = 0
        var alternate = false
        val digits = cardNumber.reversed().map { it.toString().toIntOrNull() ?: 0 }

        for (digit in digits) {
            var temp = digit
            if (alternate) {
                temp *= 2
                if (temp > 9) temp -= 9
            }
            sum += temp
            alternate = !alternate
        }

        return sum % 10 == 0
    }

    private fun isValidCVV(cvv: String, cardNumber: String): Boolean {
        if (cvv.isEmpty()) return false

        return when {
            cardNumber.startsWith("3") -> cvv.length == 4  // American Express (AMEX) uses 4-digit CVV
            else -> cvv.length == 3  // Visa, MasterCard, Discover, and other European cards use 3-digit CVV
        }
    }


}