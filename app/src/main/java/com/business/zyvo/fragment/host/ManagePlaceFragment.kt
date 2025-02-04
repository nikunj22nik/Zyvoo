package com.business.zyvo.fragment.host

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.business.zyvo.AppConstant
import com.business.zyvo.DateManager.DateManager
import com.business.zyvo.LoadingUtils
import com.business.zyvo.NetworkResult
import com.business.zyvo.OnClickListener
import com.business.zyvo.OnClickListener1
import com.business.zyvo.R
import com.business.zyvo.adapter.guest.ActivitiesAdapter
import com.business.zyvo.adapter.guest.AmenitiesAdapter
import com.business.zyvo.adapter.host.AddOnAdapter
import com.business.zyvo.adapter.host.AddOnItemAdapter
import com.business.zyvo.adapter.host.GallaryAdapter
import com.business.zyvo.adapter.host.RadioTextAdapter
import com.business.zyvo.databinding.FragmentManagePlaceBinding
import com.business.zyvo.locationManager.LocationManager
import com.business.zyvo.model.ActivityModel
import com.business.zyvo.model.ViewpagerModel
import com.business.zyvo.model.host.AddOnModel
import com.business.zyvo.model.host.ItemRadio
import com.business.zyvo.model.host.PropertyDetailsSave
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog.TAG
import com.business.zyvo.utils.ErrorDialog.customDialog
import com.business.zyvo.utils.PrepareData
import com.business.zyvo.viewmodel.LoggedScreenViewModel
import com.business.zyvo.viewmodel.host.CreatePropertyViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener
import dagger.BindsInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Arrays


@AndroidEntryPoint
class ManagePlaceFragment : Fragment(), OnMapReadyCallback, OnClickListener1 {

        // variables for availability

        var minimumHourValue = 1;
        var hourlyPrice = 10;
        var bulkDiscountHour = 1;
        var bulkDiscountPrice =10;
        var availableMonth :String ="00"
        var fromHour :String ="00:00"
        var toHour :String ="00:00"
        var days ="all"
        var cleaningCharges :String= ""
        var addonlist :MutableList<String> = mutableListOf()
        var addonPrice :MutableList<String> = mutableListOf()

        // variables for homeSetup

        var spaceType : String = "entire_home"
        var propertySize :Int =0
        var peopleCount :Int =0
        var badroomCount=0;
        var bathroomCount =0;
        var activityListResult = mutableSetOf<String>()
        var amenitiesListResult = mutableListOf<String>()
        var instantBookingCheck = 0;
        var selfCheckIn =0;
        var allowsPets =0;
        var cancellationDays :String = "00"

      // variables for Gallery and location
        var galleryList = mutableListOf<String>()
        var titleResult : String =""
        var descriptionResult : String =""
        var parkingRule :String =""
        var hostRule :String =""
        var street :String =""
        var city :String =""
        var zipcode :String =""
        var country :String =""
        var state : String =""
        var latitude :String ="0.00"
        var longitude :String ="0.00"


    private val viewModel: CreatePropertyViewModel by lazy {
        ViewModelProvider(this)[CreatePropertyViewModel::class.java]
    }

    lateinit var binding: FragmentManagePlaceBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityList: MutableList<ActivityModel>
    private lateinit var amenitiesList: MutableList<Pair<String,Boolean>>
    private lateinit var adapterActivity: ActivitiesAdapter
    private lateinit var adapterActivity2: ActivitiesAdapter
    private lateinit var amenitiesAdapter: AmenitiesAdapter
    private lateinit var mapView: MapView
    private lateinit var imageList: MutableList<Uri>

    private var PICK_IMAGES_REQUEST = 210
    private  var minimumHourIndex = 0
    private var priceIndex =0
    private var discountHourIndex =0;
    private var discountPriceIndex =0;
    private lateinit var galleryAdapter: GallaryAdapter
    private var mMap: GoogleMap? = null
    private val REQUEST_CODE_STORAGE_PERMISSION = 1
    val STORAGE_PERMISSION_CODE = 100
    private var previouslySelectedIndex: Int = -1
    private lateinit var addOnAdapter: AddOnAdapter
    private var addOnList: MutableList<AddOnModel> = mutableListOf()
    var storage_permissions = arrayOf<String>(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE
    )
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    var storage_permissions_33 = arrayOf<String>(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_VIDEO
    )
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
    : View? {
        binding = FragmentManagePlaceBinding.inflate(inflater, container, false)
        settingDataToActivityModel()
        ActivityCompat.requestPermissions(requireActivity(), permissions(), REQUEST_CODE_STORAGE_PERMISSION)
        initialization()
        setUpRecyclerView()
        locationSelection()
        mapInitialization(savedInstanceState)
        imagePermissionInitialization()
        val newWork = AddOnModel("Unknown Location", "0")
        arguments?.let {
            if(it.containsKey(AppConstant.CREATE_EVENT)){
                viewModel.pageAfterPageWork = true
            }
        }
        addOnList.add(newWork)
        swictchChangeListener()
        galleryTextField()
        return binding.root
    }



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        settingBackgroundAllWeek()
        settingBackgroundAllMonth()
        onClickDialogOpenner()

        var session : SessionManager = SessionManager(requireContext())

        Log.d("TESTING","Auth Token is "+session.getAuthToken().toString())
        Log.d("TESTING","User Id Is "+session.getUserId().toString())

        binding.imageBackButton.setOnClickListener {
            if (binding.llHomeSetup.isVisible == true) {
                navController.navigateUp()
            } else if (binding.llGalleryLocation.isVisible == true) {
                binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_inner_select_white)
                binding.tvGallery.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tvAvailability.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.llHomeSetup.visibility = View.VISIBLE
                binding.llGalleryLocation.visibility = View.GONE
                binding.llAvailability.visibility = View.GONE
            } else if (binding.llAvailability.isVisible == true) {

                binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tvGallery.setBackgroundResource(R.drawable.bg_inner_select_white)
                binding.tvAvailability.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.llHomeSetup.visibility = View.GONE
                binding.llGalleryLocation.visibility = View.VISIBLE
                binding.llAvailability.visibility = View.GONE

                binding.textSaveAndContinueButton.text = "Save & Continue"
            }
        }


        binding.textSaveAndContinueButton.setOnClickListener {
            if (binding.llHomeSetup.isVisible == true) {
                Log.d("TESTING","ActivityList size "+activityListResult.size)
                Log.d("TESTING","AmenitiesList Size"+amenitiesList.size)
                if(activityListResult.size==0){
                    LoadingUtils.showErrorDialog(requireContext(),"Please Select Activity")
                    return@setOnClickListener
                }
                if(amenitiesListResult.size ==0){
                    LoadingUtils.showErrorDialog(requireContext(),"Please Select Aminities")
                    return@setOnClickListener
                }
                if(cancellationDays.equals("00")){
                    LoadingUtils.showErrorDialog(requireContext(),"Please Select Cancellation Time")
                    return@setOnClickListener
                }
                binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tvGallery.setBackgroundResource(R.drawable.bg_inner_select_white)
                binding.tvAvailability.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.llHomeSetup.visibility = View.GONE
                binding.llGalleryLocation.visibility = View.VISIBLE
                binding.llAvailability.visibility = View.GONE
            }
            else if (binding.llGalleryLocation.isVisible == true) {
                if(!checkingGalleryValidation()){
                    return@setOnClickListener
                }
                binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tvGallery.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tvAvailability.setBackgroundResource(R.drawable.bg_inner_select_white)
                binding.llHomeSetup.visibility = View.GONE
                binding.llGalleryLocation.visibility = View.GONE
                binding.llAvailability.visibility = View.VISIBLE
                binding.textSaveAndContinueButton.text = "Publish Now"
            }
            else if (binding.llAvailability.isVisible == true) {
                callingPublishNowApi()
                //findNavController().navigate(R.id.host_fragment_properties)
            }
        }
    }

     @RequiresApi(Build.VERSION_CODES.O)
     fun callingPublishNowApi(){
         var requestBody = PropertyDetailsSave()
         var resultActivityList = mutableListOf<String>()

         activityListResult.forEach {
             resultActivityList.add(it)
         }

         if(!validation()){
             Log.d("TESTING","Inside of Validation")
             return
         }

         Log.d("TESTING","sIZE IS "+resultActivityList.size)

         val session : SessionManager = SessionManager(requireContext())
         requestBody.user_id = session.getUserId()!!

         Log.d("TESTING", "User Id is "+session.getUserId())

         requestBody.title = titleResult
         requestBody.space_type = spaceType
         requestBody.property_size = propertySize
         requestBody.max_guest_count = peopleCount
         requestBody.bedroom_count = badroomCount
         requestBody.bathroom_count = bathroomCount
         requestBody.is_instant_book = if(instantBookingCheck==1)true else false
         requestBody.has_self_checkin = if(selfCheckIn == 1) true else false
         requestBody.allows_pets = if(allowsPets ==1) true else false
         requestBody.cancellation_duration = cancellationDays.toInt()
         requestBody.description = descriptionResult
         requestBody.parking_rules = parkingRule
         requestBody.host_rules = hostRule
         requestBody.street_address = street
         requestBody.city = city
         requestBody.zip_code = zipcode
         requestBody.country = country
         requestBody.state = state
         requestBody.latitude = latitude.toFloat()
         requestBody.longitude = longitude.toFloat()
         requestBody.min_booking_hours = minimumHourValue
         requestBody.hourly_rate = hourlyPrice
         requestBody.bulk_discount_hour = bulkDiscountHour
         requestBody.bulk_discount_rate = bulkDiscountPrice
         requestBody.cleaning_fee=if(cleaningCharges.toString().length ==0)0.0f else cleaningCharges.toFloat();   ///need to correct
         requestBody.available_month = availableMonth
         requestBody.available_day = days
         requestBody.available_from = fromHour
         requestBody.available_to = toHour
         requestBody.images = galleryList
         requestBody.country = country
         requestBody.activities = resultActivityList
         requestBody.amenities = amenitiesListResult
         requestBody.add_ons = addOnList

         lifecycleScope.launch {
             LoadingUtils.showDialog(requireContext(),false)
             viewModel.addProperty(requestBody).collect{
                 when(it){
                     is NetworkResult.Success ->{
                         LoadingUtils.hideDialog()
                         Toast.makeText(requireContext(),"Succesfully Uploaded",Toast.LENGTH_LONG).show()
                     }
                     is NetworkResult.Error->{
                         LoadingUtils.hideDialog()
                         Toast.makeText(requireContext(),it.message.toString(),Toast.LENGTH_LONG).show()
                     }
                     else ->{
                         LoadingUtils.hideDialog()
                     }
                 }
             }
         }
     }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun validation() : Boolean{
        if(activityListResult.size==0){
            LoadingUtils.showErrorDialog(requireContext(),"Please Select Activity")
            return false
        }
        if(amenitiesListResult.size ==0){
            LoadingUtils.showErrorDialog(requireContext(),"Please Select Aminities")
            return false
        }
        if(cancellationDays.equals("00")){
            LoadingUtils.showErrorDialog(requireContext(),"Please Select Cancellation Time")
            return false
        }
        if(!checkingGalleryValidation()){
            return false
        }
        if(!checkingAvailabilityData()){
            return false
        }
        return true
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkingAvailabilityData() : Boolean{

          if(addOnList.size ==0){
              LoadingUtils.showErrorDialog(requireContext(),"Please Select Add-on")
              return false
          }
          if(fromHour.equals("00::00") && toHour.equals("00:00")){
            LoadingUtils.showErrorDialog(requireContext(),"Please Select Availability Hours")
            return false
          }
          var value =DateManager(requireContext()).isFromTimeLessThanToTime(fromHour,toHour)
           if(!value){
            LoadingUtils.showErrorDialog(requireContext(),"The 'from' time ($fromHour) is NOT earlier than the 'to' time ($toHour).")
            return false
          }

         return true
    }


    private fun galleryTextField(){
        binding.textType.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(editable: Editable?) {
                if (editable != null && editable.isNotEmpty()) {
                    cleaningCharges = editable.toString()
                }
            }
        })


        binding.etCity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(editable: Editable?) {
                if (editable != null && editable.isNotEmpty()) {
                    city = editable.toString()
                }
            }
        })

        binding.zipcode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(editable: Editable?) {
                if (editable != null && editable.isNotEmpty()) {
                    zipcode = editable.toString()
                }
            }
        })



        binding.state.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(editable: Editable?) {
                if (editable != null && editable.isNotEmpty()) {
                    state = editable.toString()
                }
            }
        })

        binding.etTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(editable: Editable?) {
                if (editable != null && editable.isNotEmpty()) {
                   titleResult = editable.toString()
                }
            }
        })

        binding.etDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(editable: Editable?) {
                if (editable != null && editable.isNotEmpty()) {
                    descriptionResult = editable.toString()
                }
            }
        })

        binding.etParkingRule.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(editable: Editable?) {
                if (editable != null && editable.isNotEmpty()) {
                    parkingRule = editable.toString()
                }
            }
        })

        binding.etHostRule.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(editable: Editable?) {
                if (editable != null && editable.isNotEmpty()) {
                    hostRule = editable.toString()
                }
            }
        })

        binding.etAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(editable: Editable?) {
                if (editable != null && editable.isNotEmpty()) {
                    street = editable.toString()
                }
            }
        })




    }

    private fun checkingGalleryValidation():Boolean{

        if(galleryList.size ==0){
            LoadingUtils.showErrorDialog(requireContext(),"Please Upload Images of location")
            return false
        }
        if(titleResult.isEmpty()){
            LoadingUtils.showErrorDialog(requireContext(),"Please Enter Title of Space")
            return false
        }
        if(descriptionResult.isEmpty()){
            LoadingUtils.showErrorDialog(requireContext(),"Please Enter Description of Space")
            return false
        }

       if(street.isEmpty()){
           LoadingUtils.showErrorDialog(requireContext(),"Please Enter Street")
           return false
       }

        if(city.isEmpty()){
            LoadingUtils.showErrorDialog(requireContext(),"Please Enter City Name")
            return false
        }

        if(zipcode.isEmpty()){
            LoadingUtils.showErrorDialog(requireContext(),"Please Enter Zip Code")
            return false
        }

        if(country.isEmpty()){
            LoadingUtils.showErrorDialog(requireContext(),"Please Enter Country")
            return false
        }
        if(state.isEmpty()){
            LoadingUtils.showErrorDialog(requireContext(),"Please Enter Country")
            return false
        }
        return true
    }

    fun locationSelection() {
       binding.etCity.setOnClickListener {
           binding.etCity.isEnabled = false
           val apiKey = getString(R.string.api_key_location)
           if (!Places.isInitialized()) {
               Places.initialize(context, apiKey)
           }

               val fields: List<Place.Field> = Arrays.asList<Place.Field>(
                   Place.Field.ID,
                   Place.Field.NAME,
                   Place.Field.ADDRESS,
                   Place.Field.LAT_LNG
               )

               val intent: Intent =
                   Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                       .build(activity)
               startActivityForResult(intent, 103)
           }


    }

    fun permissions(): Array<String> {
        val p: Array<String>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            p = storage_permissions_33
        } else {
            p = storage_permissions
        }
        return p
    }

    private fun imagePermissionInitialization() {

        galleryAdapter.setOnItemClickListener(object : GallaryAdapter.onItemClickListener {
            override fun onItemClick(position: Int, type: String) {

                if (type.equals(AppConstant.DELETE)) {
                    imageList.removeAt(position)
                    galleryList.removeAt(position)
                    galleryAdapter.updateAdapter(imageList)
                } else {
//                    if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                        openGallery()
//                    } else {
//                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
//                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
                        context?.let { openGallery() }
                    } else {
                        if (hasPermissions(requireContext(), *permissions())) {
                            //Do our task
                            context?.let { openGallery() }
                        } else {
                            requestPermission();
                        }
                    }
                }

            }
        })

    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", activity?.packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it, arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), STORAGE_PERMISSION_CODE
                )
            }
        }
    }

    private val storageActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    context?.let { it1 -> openGallery() }
                } else {
                    Toast.makeText(
                        context,
                        "Manage External Storage permission is denied",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {

            }
        }


    private fun hasPermissions(context: Context, vararg permissions: String): Boolean =
        permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    private fun openGallery() {
        // Intent to pick multiple images
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGES_REQUEST)
    }

    private fun mapInitialization(savedInstanceState: Bundle?) {
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGES_REQUEST) {
            val imageUris: ArrayList<Uri> = ArrayList()

            // Handle multiple image selection
            if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                Log.d("TESTING_ZYVOO","Counting is "+count)

                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                      var bitmapString = PrepareData.uriToBase64(imageUri,requireContext().contentResolver)

                    imageList.add(0, imageUri)
                    if (bitmapString != null) {
                        galleryList.add(0,bitmapString)
                    }
                }
                galleryAdapter.updateAdapter(imageList)
                Toast.makeText(requireContext(), "$count images selected", Toast.LENGTH_SHORT)
                    .show()
            } else if (data?.data != null) {
                // Single image selected
                val imageUri = data.data
                imageUris.add(imageUri!!)
                Toast.makeText(requireContext(), "1 image selected", Toast.LENGTH_SHORT).show()
            }

            // You can now handle the selected image URIs in the imageUris list
        }

        else if (requestCode == 103) {

            if (resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data)
                //  Toast.makeText(this, "ID: " + place.getId() + "address:" + place.getAddress() + "Name:" + place.getName() + " latlong: " + place.getLatLng(), Toast.LENGTH_LONG).show();
                val addressComponents = place.addressComponents?.asList()
                var address: String = place.address
                // do query with address

                val latLng = place.latLng

                latitude = latLng.latitude.toString()
                longitude = latLng.longitude.toString()

                fetchAddressDetails(latitude.toDouble(),longitude.toDouble())
                binding.etCity.isEnabled = true
                if (latitude == null) {
                    latitude = "0.0001"
                }

                if (longitude == null) {
                    longitude = "0.0001"
                }


                var add = address
//                setmarkeronMAp(latitude,longitude);
                //  setmarkeronMAp(latitude,longitude,0);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                binding.etCity.isEnabled = true
                // TODO: Handle the error.
                val status = Autocomplete.getStatusFromIntent(data)
                Toast.makeText(activity, "Error: " + status.statusMessage, Toast.LENGTH_LONG)
                    .show()
//                Log.i(TAG, status.getStatusMessage());
            }
        }
    }

    private fun fetchAddressDetails(latitude: Double, longitude: Double) {
        // Launching a coroutine to run the geocoding task in the background
        lifecycleScope.launch {
            try {
                val addressDetails = withContext(Dispatchers.IO) {
                    LocationManager(requireContext()).getAddressFromCoordinates(latitude, longitude)
                }
                binding.etCity.setText(addressDetails.city)
                binding.zipcode.setText(addressDetails.postalCode)
                binding.country.setText(addressDetails.country)
                binding.state.setText(addressDetails.state)


            } catch (e: Exception) {
                Log.e("Geocoder", "Error fetching address: ${e.message}")
                Toast.makeText(requireContext(), "Unable to fetch address details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun swictchChangeListener(){
        binding.listBookSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
              instantBookingCheck =1
            } else {
                instantBookingCheck =0
            }
        }

        binding.selfCheckIn.setOnCheckedChangeListener { compoundButton, b ->
            if(b) selfCheckIn=1 else selfCheckIn =0
        }

        binding.allowPetsSwitch.setOnCheckedChangeListener { compoundButton, b ->
            if(b) allowsPets =1 else allowsPets =0
        }

    }

    private fun initialization() {

        addOnAdapter = AddOnAdapter(requireContext(), addOnList, this)
        binding.recyclerAddOn.adapter = addOnAdapter


        imageList = mutableListOf<Uri>()
        val dummyUri = Uri.parse("http://www.example.com")
        imageList.add(dummyUri)
        adapterActivity = ActivitiesAdapter(requireContext(), activityList.subList(0, 3))

        adapterActivity.setOnItemClickListener{ adapterActivity,Int->
            run {
                savingActivityBackground(adapterActivity)
            }
        }

        adapterActivity2 = ActivitiesAdapter(requireContext(), activityList.subList(3, activityList.size))

        adapterActivity2.setOnItemClickListener { adapterActivity, Int ->
            run {
                savingActivityBackground(adapterActivity)
            }
        }

        amenitiesAdapter = AmenitiesAdapter(requireContext(), mutableListOf())
        var hoursList = mutableListOf<String>("24 Hrs", "3 Days", "7 Days", "15 Days", "30 Days")
        binding.endHour.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.endHour.arrowAnimate = false
        binding.endHour.setItems(hoursList)
        binding.endHour.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newText ->
            if(newIndex ==0){
                cancellationDays="24"
            }
            else if(newIndex ==1){
                cancellationDays="72"

            }
            else if(newIndex ==2){
                cancellationDays ="168"
            }
            else if(newIndex ==3){
                cancellationDays ="360"
            }
            else if(newIndex ==4){
                cancellationDays="720"
            }

        }



        binding.endHour.setIsFocusable(true)
        val recyclerView = binding.endHour.getSpinnerRecyclerView()
        val spacing = 16 // Spacing in pixels

        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.top = spacing
            }
        })
        settingBackgroundTaskToBedroom()
        settingBackgroundTaskToBathroom()
        settingBackgroundTaskToProperty()
        byDefaultSelectAvailability()
        settingBackgroundTaskToPeople()
        settingClickListenertoSpaceManagePlace()
    }

    private fun savingActivityBackground(adapterActivity:MutableList<ActivityModel>){
        CoroutineScope(Dispatchers.IO).launch {
            adapterActivity.forEach {
                if(it.checked){
                    Log.d("TESTING","cHECKING nAME IS "+ it.name)
                    activityListResult.add(it.name)
                }
            }
        }
    }

    private fun settingClickListenertoSpaceManagePlace() {
        binding.tvHome.setOnClickListener {
            binding.tvHome.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tvPrivateRoom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            spaceType = "entire_home"

        }
        binding.tvPrivateRoom.setOnClickListener {
            binding.tvPrivateRoom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tvHome.setBackgroundResource(R.drawable.bg_outer_manage_place)
            spaceType = "private_room"
        }

        binding.tvHomeSetup.setOnClickListener {
            binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tvGallery.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvAvailability.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.llGalleryLocation.visibility = View.GONE
            binding.llHomeSetup.visibility = View.VISIBLE
            binding.llAvailability.visibility = View.GONE
            binding.textSaveAndContinueButton.text = "Save & Continue"
        }

        binding.tvGallery.setOnClickListener {
            binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvGallery.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tvAvailability.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.llGalleryLocation.visibility = View.VISIBLE
            binding.llAvailability.visibility = View.GONE
            binding.llHomeSetup.visibility = View.GONE
            binding.textSaveAndContinueButton.text = "Save & Continue"
        }

        binding.tvAvailability.setOnClickListener {
            binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvGallery.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvAvailability.setBackgroundResource(R.drawable.bg_inner_select_white)

            binding.llGalleryLocation.visibility = View.GONE
            binding.llAvailability.visibility = View.VISIBLE
            binding.llHomeSetup.visibility = View.GONE

            binding.textSaveAndContinueButton.text = "Publish Now"
        }

        binding.allowPets.setOnClickListener {
            showPopupWindowForPets(binding.allowPets)
        }

        binding.allowCancel.setOnClickListener {
            showPopupWindowForPets(binding.allowCancel)
        }
    }

    private fun showPopupWindowForPets(anchorView: View) {
        // Inflate the popup layout
        val inflater = LayoutInflater.from(requireContext())
        val popupView = inflater.inflate(R.layout.popup_layout_pets, null)

        // Create the PopupWindow
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Show the popup window at the bottom right of the TextView

        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.showAsDropDown(anchorView, anchorView.width, 0)
    }

    private fun settingBackgroundTaskToBedroom() {
        binding.tvAnyBedrooms.setOnClickListener {
            binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv1Bathrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            bathroomCount =0
        }

        binding.tv1Bathrooms.setOnClickListener {
            binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bathrooms.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv2Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            bathroomCount =1
        }

        binding.tv2Bathroom.setOnClickListener {
            binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bathrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bathroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv3Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            bathroomCount =2
        }
        binding.tv3Bathroom.setOnClickListener {
            binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bathrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bathroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv4Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            bathroomCount =3
        }

        binding.tv4Bathroom.setOnClickListener {
            binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bathrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bathroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv5Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            bathroomCount = 4
        }

        binding.tv5Bathroom.setOnClickListener {
            binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bathrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bathroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv7Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            bathroomCount = 5
        }
        binding.tv7Bathroom.setOnClickListener {
            binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bathrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bathroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv8Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            bathroomCount =7
        }

        binding.tv8Bathroom.setOnClickListener {
            binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bathrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bathroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            bathroomCount = 8
        }
        binding.etBathroom.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                val newText = charSequence.toString()
            }
            override fun afterTextChanged(editable: Editable?) {
                val finalText = editable.toString()
                bathroomCount = finalText.toInt()
            }
        })

        binding.etCity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                val newText = charSequence.toString()
            }
            override fun afterTextChanged(editable: Editable?) {
                val finalText = editable.toString()
                city = finalText.toString()
            }
        })

        binding.country.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                val newText = charSequence.toString()
            }
            override fun afterTextChanged(editable: Editable?) {
                val finalText = editable.toString()
                country = finalText.toString()
            }
        })

        binding.state.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                val newText = charSequence.toString()
            }
            override fun afterTextChanged(editable: Editable?) {
                val finalText = editable.toString()
                state = finalText.toString()
            }
        })
    }

    private fun settingBackgroundTaskToBathroom() {

        binding.tvAnyBedrooms.setOnClickListener {
            binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv1Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            badroomCount =0
        }

        binding.tv1Bedroom.setOnClickListener {
            binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bedroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv2Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            badroomCount =1
        }

        binding.tv2Bedroom.setOnClickListener {
            binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bedroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv3Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            badroomCount =2
        }

        binding.tv3Bedroom.setOnClickListener {
            binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bedroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv4Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            badroomCount =3
        }

        binding.tv4Bedroom.setOnClickListener {
            binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bedroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv5Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            badroomCount =4
        }

        binding.tv5Bedroom.setOnClickListener {
            binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bedroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv7Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            badroomCount =5
        }


        binding.tv7Bedroom.setOnClickListener {
            binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bedroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv8Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            badroomCount =7
        }

        binding.tv8Bedroom.setOnClickListener {
            binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bedroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            badroomCount =  8
        }

        binding.etBedRoomCount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                val newText = charSequence.toString()
            }
            override fun afterTextChanged(editable: Editable?) {
                val finalText = editable.toString()
                badroomCount = finalText.toInt()
            }
        })



    }

    private fun settingBackgroundTaskToProperty() {

        binding.tvAny.setOnClickListener {
            binding.tvAny.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
            propertySize =0
        }

        binding.tv250.setOnClickListener {
            binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv250.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
            propertySize =250
        }

        binding.tv350.setOnClickListener {
            binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv350.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
            propertySize = 350
        }


        binding.tv450.setOnClickListener {
            binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv450.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
            propertySize = 450
        }
        binding.tv550.setOnClickListener {
            binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv550.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
            propertySize = 550
        }

        binding.tv650.setOnClickListener {
            binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv650.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
            propertySize = 650
        }
        binding.tv750.setOnClickListener {
            binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv750.setBackgroundResource(R.drawable.bg_inner_select_white)
            propertySize = 750
        }

        binding.etPropertySize.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                val newText = charSequence.toString()
            }
            override fun afterTextChanged(editable: Editable?) {
                val finalText = editable.toString()
                propertySize = finalText.toInt()
            }
        })
    }

    private fun byDefaultSelectAvailability() {
        binding.tvAny.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_inner_select_white)

    }

    private fun settingBackgroundTaskToPeople() {
        //No of people

        binding.tvAnyPeople.setOnClickListener {
            binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv1.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7.setBackgroundResource(R.drawable.bg_outer_manage_place)
            peopleCount =0
        }


        binding.tv1.setOnClickListener {
            binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv2.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7.setBackgroundResource(R.drawable.bg_outer_manage_place)
            peopleCount =1
        }
        binding.tv2.setOnClickListener {
            binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv3.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7.setBackgroundResource(R.drawable.bg_outer_manage_place)
            peopleCount = 2
        }

        binding.tv3.setOnClickListener {
            binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv5.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7.setBackgroundResource(R.drawable.bg_outer_manage_place)
            peopleCount =3
        }

        binding.tv5.setOnClickListener {
            binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv7.setBackgroundResource(R.drawable.bg_outer_manage_place)
            peopleCount =5
        }

        binding.tv7.setOnClickListener {
            binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7.setBackgroundResource(R.drawable.bg_inner_select_white)
            peopleCount =7
        }

        binding.peopleCount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                val newText = charSequence.toString()
            }
            override fun afterTextChanged(editable: Editable?) {
                val finalText = editable.toString()
                peopleCount = finalText.toInt()
            }
        })
    }

    fun settingDataToActivityModel() {
        var data = PrepareData.getAmenitiesList()
        activityList = data.first
        amenitiesList = PrepareData.getOnlyAmenitiesList()
    }

    fun setUpRecyclerView() {
        galleryAdapter = GallaryAdapter(imageList)


        binding.recyclerGallery.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerGallery.adapter = galleryAdapter

        val gridLayoutManager = GridLayoutManager(requireContext(), 3) // Set 4 columns
        val gridLayoutManager2 = GridLayoutManager(requireContext(), 3)
        val gridLayoutManager3 = GridLayoutManager(requireContext(), 2)
        binding.recyclerActivity2.layoutManager = gridLayoutManager2
        binding.recyclerActivity.layoutManager = gridLayoutManager
        binding.recyclerActivity2.isNestedScrollingEnabled = false
        binding.recyclerActivity.adapter = adapterActivity
        binding.recyclerActivity.isNestedScrollingEnabled = false
        binding.recyclerActivity2.adapter = adapterActivity2
        binding.recyclerActivity2.visibility = View.GONE
        binding.tvOtherActivity.setOnClickListener {
            if (binding.recyclerActivity2.visibility == View.VISIBLE) {
                binding.recyclerActivity2.visibility = View.GONE
            } else {
                binding.recyclerActivity2.visibility = View.VISIBLE
                binding.recyclerActivity2.scrollToPosition(activityList.size - 3)
            }
        }

        //Amenities
        binding.recyclerAmenties.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerAmenties.adapter = amenitiesAdapter
        binding.recyclerAmenties.isNestedScrollingEnabled = false

        amenitiesAdapter.updateAdapter(amenitiesList)

        amenitiesAdapter.setOnItemClickListener(object:AmenitiesAdapter.onItemClickListener{
            override fun onItemClick(list: MutableList<Pair<String, Boolean>>) {
               amenitiesList = list
               changingAmenitiesList(amenitiesList)
            }
        })


    }

    private fun changingAmenitiesList(amenitiesList: MutableList<Pair<String, Boolean>>) {

        CoroutineScope(Dispatchers.IO).launch {
          amenitiesListResult.clear()

            amenitiesList.forEach {
                if(it.second)amenitiesListResult.add(it.first)
            }

        }
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        val newYork = LatLng(40.7128, -74.0060)
        mMap?.addMarker(MarkerOptions().position(newYork).title("Marker in New York"))
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(newYork, 10f))
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()  // Important to call in onResume
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()  // Important to call in onDestroy

    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()  // Important to call in onLowMemory
    }

    override fun itemClick(obj: Int, text: String) {
        when (text) {
            "add On" -> {
                showAddOnDialog()
            }
            "add On Cross" -> {
                if (obj == addOnList.size - 1) {
                    //  dialogSelectLanguage()
                } else {
                    addOnList.removeAt(obj)
                    addonlist.removeAt(obj)
                    addonPrice.removeAt(obj)
                    addOnAdapter.updateAddOn(addOnList)

                }
            }

        }
    }

    fun getItemList(): List<String> {
        return PrepareData.getEventEquipment()
    }

    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    fun showAddOnDialog() {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)

            setContentView(R.layout.dialog_add_new_add_on_host)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }


            val recyclerView: RecyclerView = findViewById(R.id.rcy)

            val etItemName: EditText = findViewById(R.id.etAdd)

            val etItemPrice: EditText = findViewById(R.id.etRupees)

            val btnSubmit: TextView = findViewById(R.id.textAddButton)

            val itemList = getItemList()
            var selectedItem: String? = null

            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = AddOnItemAdapter(itemList) { item ->
                selectedItem = item
                etItemName.setText(item)
            }
            btnSubmit.setOnClickListener {
                val itemName = etItemName.text.toString()
                val itemPrice = etItemPrice.text.toString()

                if (itemName.isNotEmpty() && itemPrice.isNotEmpty()) {
                    val newAddOn = AddOnModel(itemName, itemPrice)
                    addOnList.add(0, newAddOn)
                    addonlist.add(itemName)
                    addonPrice.add(itemPrice)
                    addOnAdapter.updateAddOn(addOnList)
                    addOnAdapter.notifyItemInserted(0)
                    dialog.dismiss()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please enter valid details",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

    private fun settingBackgroundAllMonth() {
        //No of people

        binding.tvAll.setOnClickListener {
            binding.tvAll.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tvJan.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvFeb.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvMar.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvApr.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvMay.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJun.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJul.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvAug.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvSep.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvOct.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvNov.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvDec.setBackgroundResource(R.drawable.bg_outer_manage_place)
            availableMonth="00"
        }


        binding.tvJan.setOnClickListener {
            availableMonth="01"

            binding.tvAll.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJan.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tvFeb.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvMar.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvApr.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvMay.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJun.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.tvJul.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvAug.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvSep.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvOct.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvNov.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvDec.setBackgroundResource(R.drawable.bg_outer_manage_place)

        }
        binding.tvFeb.setOnClickListener {
            availableMonth="02"

            binding.tvAll.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJan.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvFeb.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tvMar.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvApr.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvMay.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJun.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.tvJul.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvAug.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvSep.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvOct.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvNov.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvDec.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }


        binding.tvMar.setOnClickListener {
            availableMonth="03"

            binding.tvAll.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJan.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvFeb.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.tvMar.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tvApr.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvMay.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJun.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.tvJul.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvAug.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvSep.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvOct.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvNov.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvDec.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tvApr.setOnClickListener {
            availableMonth="04"

            binding.tvAll.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJan.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvFeb.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.tvMar.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvApr.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tvMay.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJun.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.tvJul.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvAug.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvSep.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvOct.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvNov.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvDec.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tvMay.setOnClickListener {
            availableMonth="05"

            binding.tvAll.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJan.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvFeb.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.tvMar.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvApr.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvMay.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tvJun.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.tvJul.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvAug.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvSep.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvOct.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvNov.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvDec.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }
        binding.tvJun.setOnClickListener {
            availableMonth="06"

            binding.tvAll.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJan.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvFeb.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.tvMar.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvApr.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvMay.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJun.setBackgroundResource(R.drawable.bg_inner_select_white)

            binding.tvJul.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvAug.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvSep.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvOct.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvNov.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvDec.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }
        binding.tvJul.setOnClickListener {
            availableMonth="07"

            binding.tvAll.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJan.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvFeb.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.tvMar.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvApr.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvMay.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJun.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.tvJul.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tvAug.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvSep.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvOct.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvNov.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvDec.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }
        binding.tvAug.setOnClickListener {
            availableMonth="08"

            binding.tvAll.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJan.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvFeb.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.tvMar.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvApr.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvMay.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJun.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.tvJul.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvAug.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tvSep.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvOct.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvNov.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvDec.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }
        binding.tvSep.setOnClickListener {
            availableMonth="09"

            binding.tvAll.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJan.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvFeb.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.tvMar.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvApr.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvMay.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJun.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.tvJul.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvAug.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvSep.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tvOct.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvNov.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvDec.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }
        binding.tvOct.setOnClickListener {
            availableMonth="10"

            binding.tvAll.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJan.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvFeb.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.tvMar.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvApr.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvMay.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJun.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.tvJul.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvAug.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvSep.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvOct.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tvNov.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvDec.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }
        binding.tvNov.setOnClickListener {
            availableMonth="11"

            binding.tvAll.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJan.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvFeb.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.tvMar.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvApr.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvMay.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJun.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.tvJul.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvAug.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvSep.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvOct.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvNov.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tvDec.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }
        binding.tvDec.setOnClickListener {
            availableMonth="12"

            binding.tvAll.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJan.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvFeb.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.tvMar.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvApr.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvMay.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvJun.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.tvJul.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvAug.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvSep.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvOct.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvNov.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvDec.setBackgroundResource(R.drawable.bg_inner_select_white)
        }


    }

    fun settingBackgroundAllWeek() {

        binding.tvAllDays.setOnClickListener {
            binding.tvAllDays.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tvOnlyWorkingDays.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvOnlyWeekends.setBackgroundResource(R.drawable.bg_outer_manage_place)
            days = "all"
        }


        binding.tvOnlyWorkingDays.setOnClickListener {
            binding.tvAllDays.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvOnlyWorkingDays.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tvOnlyWeekends.setBackgroundResource(R.drawable.bg_outer_manage_place)
            days = "working_days"
        }
        binding.tvOnlyWeekends.setOnClickListener {
            binding.tvAllDays.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvOnlyWorkingDays.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvOnlyWeekends.setBackgroundResource(R.drawable.bg_inner_select_white)
            days = "weekends"
        }


    }

    private fun getItemListForRadioHoursText(): MutableList<ItemRadio> {
        val items = PrepareData.getHourMinimumList()

        // Restore the previously selected item's state
        if (minimumHourIndex != -1 && minimumHourIndex < items.size) {
            items[minimumHourIndex].isSelected = true
        }
        return items
    }

    private fun getItemListForRadioPerHoursRuppesText(): MutableList<ItemRadio> {
        val items = PrepareData.getPriceAndHourList()
        // Restore the previously selected item's state
        if (priceIndex != -1 && priceIndex < items.size) {
            items[priceIndex].isSelected = true
        }
        return items
    }

    private fun getItemListForRadioPerHoursBulkText(): MutableList<ItemRadio> {
        val items = PrepareData.getPriceAndHourList1()

        // Restore the previously selected item's state
        if (discountHourIndex != -1 && discountHourIndex < items.size) {
            items[discountHourIndex].isSelected = true
        }
        return items
    }

    private fun getItemListForRadioPerHoursDiscountText(): MutableList<ItemRadio> {
        val items = PrepareData.getDiscountList()

        // Restore the previously selected item's state
        if (discountPriceIndex != -1 && discountPriceIndex < items.size) {
            items[discountPriceIndex].isSelected = true
        }
        return items
    }

    fun onClickDialogOpenner() {
        binding.llHours.setOnClickListener {
            val items = getItemListForRadioHoursText()
            showSelectedDialog(requireContext(), items, binding.tvHoursSelect,AppConstant.MINIMUM_HOUR)
        }

        binding.llHoursRupees.setOnClickListener {
            val items = getItemListForRadioPerHoursRuppesText()

            showSelectedDialog(requireContext(), items, binding.tvHoursRupeesSelect,AppConstant.PRICE)

        }
        binding.llHoursBulk.setOnClickListener {
            val items = getItemListForRadioPerHoursBulkText()
            showSelectedDialog(requireContext(), items, binding.tvHoursBulkSelect,AppConstant.BULK_HOUR)
        }


        binding.llDiscount.setOnClickListener {
            val items = getItemListForRadioPerHoursDiscountText()
            showSelectedDialog(requireContext(), items, binding.tvDiscountSelect,AppConstant.DISCOUNT)
        }

        binding.llAvailabilityFromHours.setOnClickListener {
            DateManager(requireContext()).showTimePickerDialog(requireContext()) { selectedHour ->
                binding.tvHours.setText(selectedHour.toString())
                 fromHour = DateManager(requireContext()).convertTo24HourFormat(selectedHour)
                Log.d("TESTING_ZYVOO", "From "+fromHour)
            }
        }

        binding.llAvailabilityEndHours.setOnClickListener {
            DateManager(requireContext()).showTimePickerDialog(requireContext()) { selectedHour ->
                binding.tvHours1.setText(selectedHour.toString())
                toHour = DateManager(requireContext()).convertTo24HourFormat(selectedHour)
                Log.d("TESTING_ZYVOO","To "+toHour)
            }

        }

    }

    fun showSelectedDialog(context: Context, items: MutableList<ItemRadio>, text: TextView,type:String) {
        val dialog = Dialog(context, R.style.BottomSheetDialog)
        dialog.apply {
            setCancelable(true)
            setContentView(R.layout.dialog_for_select_radio_text)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }

            val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

            recyclerView.layoutManager = LinearLayoutManager(context)
            val adapter = RadioTextAdapter(items, object : OnClickListener {
                override fun itemClick(selectedIndex: Int) {
                     if(type.equals(AppConstant.MINIMUM_HOUR)){
                         minimumHourIndex = selectedIndex
                         minimumHourValue = selectedIndex+1;
                       }
                    else if(type.equals(AppConstant.PRICE)){
                        priceIndex = selectedIndex
                         hourlyPrice = (selectedIndex+1)*10
                     }
                    else if(type.equals(AppConstant.DISCOUNT)){
                        discountPriceIndex = selectedIndex
                         bulkDiscountPrice = (selectedIndex+1)*10
                     }
                    else if(type.equals(AppConstant.BULK_HOUR)){
                        discountHourIndex = selectedIndex
                         bulkDiscountHour = (selectedIndex+1)
                     }
                }
            }) { selectedText ->
                // Update TextView with the selected text
                text.text = selectedText
                dismiss()
            }

            recyclerView.adapter = adapter

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

  }