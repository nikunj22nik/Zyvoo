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
import com.business.zyvo.model.host.AddOnModel
import com.business.zyvo.model.host.GetPropertyDetail
import com.business.zyvo.model.host.ItemRadio
import com.business.zyvo.model.host.PropertyDetailsSave
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.PrepareData
import com.business.zyvo.viewmodel.host.CreatePropertyViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        var deleteImage :MutableList<Int> = mutableListOf()

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
        var galleryList = mutableListOf<Pair<String,Boolean>>()
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


    var galleryListId = mutableListOf<Int>()
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
    private var propertyId :Int =-1

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

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):View? {
        binding = FragmentManagePlaceBinding.inflate(inflater, container, false)
        settingDataToActivityModel()
        ActivityCompat.requestPermissions(requireActivity(), permissions(), REQUEST_CODE_STORAGE_PERMISSION)
        initialization()
        setUpRecyclerView()
        locationSelection()
        mapInitialization(savedInstanceState)
        imagePermissionInitialization()
        val newWork = AddOnModel("Unknown Location", "0")

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
        arguments?.let {
            if(it.containsKey(AppConstant.PROPERTY_ID)){
                propertyId = it.getInt(AppConstant.PROPERTY_ID)
                callingPropertyDetailApi(propertyId)
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

         val newGalleryList = mutableListOf<String>()

         galleryList.forEach {
             if(it.second){
                 newGalleryList.add(it.first)
             }
         }
         if(newGalleryList.size ==0){
             LoadingUtils.showErrorDialog(requireContext(),"Please Upload Images")
             return
         }

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
         requestBody.images = newGalleryList
         requestBody.country = country
         requestBody.activities = resultActivityList
         requestBody.amenities = amenitiesListResult
         requestBody.add_ons = addOnList

         lifecycleScope.launch {

             LoadingUtils.showDialog(requireContext(),false)
             if(propertyId ==-1) {
                 viewModel.addProperty(requestBody).collect {
                     when (it) {
                         is NetworkResult.Success -> {
                             LoadingUtils.hideDialog()

                             LoadingUtils.showSuccessDialog(requireContext(),"Property Updated Succesfully")
                         }

                         is NetworkResult.Error -> {
                             LoadingUtils.hideDialog()
                             Toast.makeText(
                                 requireContext(),
                                 it.message.toString(),
                                 Toast.LENGTH_LONG
                             ).show()
                         }

                         else -> {
                             LoadingUtils.hideDialog()
                         }
                     }
                 }
             }
             else{
                 requestBody.property_id = propertyId
                 requestBody.delete_images = deleteImage
                 LoadingUtils.showDialog(requireContext(),true)
                 viewModel.updateProperty(requestBody).collect{
                     when(it){
                         is NetworkResult.Success ->{
                             LoadingUtils.hideDialog()
                             LoadingUtils.showSuccessDialog(requireContext(),it.data.toString())
                         }
                         is NetworkResult.Error ->{
                             LoadingUtils.hideDialog()
                              LoadingUtils.showErrorDialog(requireContext(),it.message.toString())
                         }
                         is NetworkResult.Loading ->{

                         }
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
            LoadingUtils.showErrorDialog(requireContext(),"Please Select Amenities")
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


    private fun callingPropertyDetailApi(propertyId :Int){
       LoadingUtils.showDialog(requireContext(),false)

        lifecycleScope.launch {
            viewModel.propertyDetail(propertyId).collect{
                when(it){
                    is NetworkResult.Success ->{
                        LoadingUtils.hideDialog()
                        detailsDataSetToUi(it.data)
                    }
                    is NetworkResult.Error ->{
                        LoadingUtils.hideDialog()
                    }
                    else ->{

                    }
                }
            }
        }
    }


    private fun detailsDataSetToUi(data: GetPropertyDetail?) {
        data?.let {
            //first Screen Work
            if(it.space_type.equals("entire_home")){
                homeSelectTask()
            }
            else{
                privateRoomSelectTask()
            }
            checkChangeWork(it)
            propertySetDataToUi(it.property_size)
            numberOfPeopleSetDataToUi(it.max_guest_count)
            bedRoomSetDataToUi(it.bedroom_count)
            BathRoomSetDataToUi(it.bathroom_count)
            activitiesSetDataToUi(it.activities)
            amenitiesAdapter(it.amenities)

            //Second Screen Work
            galleryLocationScreenTask(it)

           // third Screen Work
            availabilityScreenTask(it)
        }
    }

    private fun availabilityScreenTask(data: GetPropertyDetail?){
              data?.let {
                  if(it.available_month.equals("00")){
                      availableMonth ="00"
                      anyMonth()
                  }
                  else if(it.available_month.equals("01")){
                      availableMonth ="01"
                      janSelect()
                  }else if(it.available_month.equals("02")){
                      availableMonth ="02"
                      febSelect()
                  }else if(it.available_month.equals("03")){
                      availableMonth ="03"
                      marchSelect()
                  }else if(it.available_month.equals("04")){
                      availableMonth ="04"
                      aprilSelect()
                  }else if(it.available_month.equals("05")){
                      availableMonth ="05"
                      maySelect()
                  }else if(it.available_month.equals("06")){
                      availableMonth ="06"
                      juneSelect()
                  }
                  else if(it.available_month.equals("07")){
                      availableMonth ="07"
                      julySelect()
                  }else if(it.available_month.equals("08")){
                      availableMonth ="08"
                      augustSelect()
                  }else if(it.available_month.equals("09")){
                      availableMonth ="09"
                      septemberSelect()
                  }else if(it.available_month.equals("10")){
                      availableMonth ="10"
                      octoberSelect()
                  }else if(it.available_month.equals("11")){
                      availableMonth ="11"
                      novemberSelect()
                  }else if(it.available_month.equals("12")){
                       decSelect()
                      availableMonth ="12"
                  }

                  binding.etType.setText(it.cleaning_fee)
                  cleaningCharges = it.cleaning_fee

                  if(it.available_day.equals("working_days")){
                      onlyWorkingDay()
                      days = "working_days"
                  }
                  else if(it.available_month.equals("all")){
                      anyWeekSelect()
                      days ="all"
                  }else{
                      onlyWeekend()
                      days = "weekends"
                  }
                  it.available_to =DateManager(requireContext()).getHoursAndMinutes(it.available_to)
                  it.available_from = DateManager(requireContext()).getHoursAndMinutes(it.available_from)
                  var fromHour = DateManager(requireContext()).convert24HourToAMPM(it.available_from)
                  var toHour = DateManager(requireContext()).convert24HourToAMPM(it.available_to)
                  binding.tvHours.setText(fromHour)
                  binding.tvHours1.setText(toHour)
                  Log.d("TESTING_ZYvoo", "Available From "+ it.available_from +" Available To "+ it.available_to)
                  this.fromHour = it.available_from
                  this.toHour = it.available_to

                  val minHour = it.min_booking_hours.toDouble()
                  minimumHourValue = minHour.toInt()
                  binding.tvHoursSelect.setText(minimumHourValue.toString()+" hour minimum")

                  var hPrice = it.hourly_rate.toDouble()
                  hourlyPrice = hPrice.toInt()
                  binding.tvHoursRupeesSelect.setText("$"+hourlyPrice.toString())


                  var discountHour = it.bulk_discount_hour.toDouble()
                  bulkDiscountHour= discountHour.toInt()
                  binding.tvHoursBulkSelect.setText(bulkDiscountHour.toString()+" hour minimum")



                  val disCountPrice = it.bulk_discount_rate.toDouble()
                  val disPrice = disCountPrice.toInt()
                  bulkDiscountPrice = disPrice
                  binding.tvDiscountSelect.setText(bulkDiscountPrice.toString()+"%  Discount")



                  addOnList = it.add_ons.toMutableList()
                  addOnAdapter.updateAddOn(addOnList)
              }
    }

    private fun galleryLocationScreenTask(data: GetPropertyDetail?){
        binding.etTitle.setText(data?.title)
        titleResult = data?.title.toString()

        if(data?.latitude != null){
            latitude = data.latitude
        }

        if(data?.longitude != null){
            longitude = data.longitude
        }

        binding.etDescription.setText(data?.property_description)

        descriptionResult = data?.property_description.toString()

        data?.parking_rules?.let {
            binding.etParkingRule.setText(it.toString())
            parkingRule = it.toString()
        }

        data?.host_rules?.let {
            binding.etHostRule.setText(it.toString())
            hostRule = it.toString()
        }

        data?.street_address?.let {
            binding.etAddress.setText(it.toString())
            street = it.toString()
        }

        data?.city?.let {
            binding.etCity.setText(it.toString())
            city = it.toString()
        }

        data?.zip_code?.let {
            binding.zipcode.setText(it.toString())
            zipcode = it.toString()
        }

        data?.country?.let {
            binding.country.setText(it.toString())
            country = it
        }

        data?.state?.let {
            binding.state.setText(it.toString())
            state = it.toString()
        }

        if(!latitude.equals("00") && !longitude.equals("00")) {
            Log.d("TESTING_LATITUDE",latitude.toString() +" "+longitude.toString())

        }

        val resultList = mutableListOf<Uri>()

        galleryListId.clear()

        data?.property_images?.forEach {
            val str = AppConstant.BASE_URL + it.image_url
            val uri = Uri.parse(str)
            galleryListId.add(it.id)
            galleryList.add(Pair<String,Boolean>(str,false))
            Log.d("TESTING_URL", uri.toString())
            resultList.add(uri)
        }

        imageList = resultList

        galleryAdapter.updateAdapter(resultList)
    }

    private fun checkChangeWork(data: GetPropertyDetail?){
        data?.let {
            if(it.is_instant_book == 1){
                binding.listBookSwitch.isChecked = true
                instantBookingCheck =1
            }else{
                binding.listBookSwitch.isChecked = false
                instantBookingCheck =0
            }

            if(it.has_self_checkin ==1){
                binding.selfCheckIn.isChecked = true
                selfCheckIn =1
            }else{
                binding.selfCheckIn.isChecked = false
                selfCheckIn =0
            }

            if(it.allows_pets ==1){
                binding.allowPetsSwitch.isChecked = true
                allowsPets =1
            }else{
                binding.allowPetsSwitch.isChecked = false
                allowsPets =0
            }

            cancellationDays = it.cancellation_duration.toString()

            if(cancellationDays.equals("24")){
                binding.endHour.selectItemByIndex(0)
            }
            else if(cancellationDays.equals("72")){
                binding.endHour.selectItemByIndex(1)
            }
            else if(cancellationDays.equals("168")){
                binding.endHour.selectItemByIndex(2)
            }
            else if(cancellationDays.equals("360")){
                binding.endHour.selectItemByIndex(3)
            }
            else if(cancellationDays.equals("720")){
                binding.endHour.selectItemByIndex(4)
            }
        }

    }

    private fun amenitiesAdapter(list :List<String>){
        val dataTmp = PrepareData.getOnlyAmenitiesList()
        var count =0

        amenitiesListResult.clear()

        dataTmp.forEach {
          if(list.contains(it.first)){
              Log.d("TESTING_RESULT","Inside Truth")
              val pair = Pair(it.first, true)
              dataTmp.set(count,pair)
              amenitiesListResult.add(it.first)
          }
            count++
        }
        amenitiesAdapter.updateAdapter(dataTmp)
    }


    private fun activitiesSetDataToUi(list :List<String>){
        val dataTmp = PrepareData.getAmenitiesList()
        val dataFirst = dataTmp.first
        var count =0
        activityListResult.clear()
        dataFirst.forEach {
           if(list.contains(it.name.trim())){
               var newFormed = it
               newFormed.checked = true
               dataFirst.set(count,newFormed)
               activityListResult.add(it.name)
           }
            count++
        }
        adapterActivity.updateAdapter(dataFirst.subList(0,3))
        adapterActivity2.updateAdapter(dataFirst.subList(3,dataFirst.size))

    }

    private fun BathRoomSetDataToUi(count:Int){
        if(count ==0){
            bathRoomAnySelect()
        }
        else if(viewModel.numberSelectMap.containsKey(count)){
            if(count ==1){
                bathRoomFirstSelect()
            }
            else if(count ==2){
                bathRoomSecondSelect()
            }
            else if(count ==3){
                bathRoomThirdSelect()
            }
            else if(count ==4){
                bathRoomFourthSelect()
            }
            else if(count ==5){
                bathRoomFifthSelect()
            }
            else if(count ==7){
                bathroom7Select()
            }
            else if(count ==8){
                bathRoom8Select()
            }
        }

        else{
            binding.etBathroom.setText(count.toString())
            bathroomCount = count
            clearBathRommBackground()
        }
    }

    private fun bedRoomSetDataToUi(count: Int){
        if(count==0){
            bedRoomAnySelect()
        }
        else if(viewModel.numberSelectMap.containsKey(count)){
            if(count==1){
                bedRoomFirstSelect()
            }
            else if(count ==2){
                bedRoomSecondSelect()
            }
            else if(count ==3){
                bedRoomThirdSelect()
            }
            else if(count ==4){
                bedRoomFourthSelect()
            }else if(count ==5){
                bedRoomFifthSelect()
            }
            else if(count ==7){
                bedroom7Select()
            }else if(count ==8) {
                bedRoom8Select()
            }
        }
        else{
           binding.etBedRoomCount.setText(count.toString())
            badroomCount = count
            badRoomClearBackground()
        }
    }

    private fun numberOfPeopleSetDataToUi(count:Int){
        if(count==6){
            peopleCount =6
            binding.peopleCount.setText(peopleCount.toString())
        }
        else if(viewModel.numberSelectMap.containsKey(count)){
           if(count==1){
              onePeopleCount()
           }
            else if(count ==2){
                twoPeopleCount()
           }
            else if(count==3){
                peopleCount3()
           }
            else if(count ==5){
                peopleCount5()
           }
            else if(count == 7){
                peopleCount7()
           }
        }
        else if( count == 0 ){
               anyPeopleCount()
        }
        else{
            peopleCount =count
            binding.peopleCount.setText(peopleCount.toString())
            clearPeopleCountBackground()
        }

    }


    private fun propertySetDataToUi(propertySize: Int){
        if(viewModel.propertyMap.containsKey(propertySize)){
                if(propertySize == 250){
                    propertyRoomFirstSelect()
                } else if(propertySize == 350){
                    propertySecondSelect()
                }
                else if(propertySize == 450){
                    propertyThirdSelect()
                }
                 else if(propertySize == 550){
                     propertyFourthSelect()
                }else if(propertySize == 650){
                    propertyFifthSelect()
                }
            else if(propertySize == 750){
                property7Select()
                }

        }
        else if(propertySize == 0){
            propertyRoomAnySelect()
        }
        else{
           binding.etPropertySize.setText(propertySize.toString())
            this.propertySize = propertySize
            clearPropertyBackground()
        }

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
       //   var value =DateManager(requireContext()).isFromTimeLessThanToTime(fromHour,toHour)
         //  if(!value){
           // LoadingUtils.showErrorDialog(requireContext(),"The 'from' time ($fromHour) is NOT earlier than the 'to' time ($toHour).")
           // return false


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
            override fun afterTextChanged(editable: Editable?)   {
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
                    //callImageDeleteApi(galleryListId.get(position),position)

                    imageList.removeAt(position)
                    galleryList.removeAt(position)
                    deleteImage.add(galleryListId.get(position))
                    galleryListId.removeAt(position)
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

//    fun callImageDeleteApi(id:Int,position :Int){
//        lifecycleScope.launch {
//            LoadingUtils.showDialog(requireContext(),false)
//            viewModel.propertyImageDelete(id).collect{
//                when(it){
//                    is NetworkResult.Success ->{
//                        LoadingUtils.hideDialog()
//
//                    }
//                    is NetworkResult.Error ->{
//                        LoadingUtils.hideDialog()
//                        LoadingUtils.showErrorDialog(requireContext(),it.message.toString())
//                    }
//                    else ->{
//                    }
//                }
//            }
//
//        }
//    }

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
                        galleryList.add(0,Pair<String,Boolean>(bitmapString,true))
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
                val location = LatLng(latitude.toDouble(), longitude.toDouble())
                // Move the camera to the specified location
                mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10f))
                // Add a marker at that location
                mMap?.addMarker(MarkerOptions().position(location))
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

    private fun homeSelectTask(){
        binding.tvHome.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tvPrivateRoom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        spaceType = "entire_home"
    }

    private fun privateRoomSelectTask(){
        binding.tvPrivateRoom.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tvHome.setBackgroundResource(R.drawable.bg_outer_manage_place)
        spaceType = "private_room"
    }

    private fun settingClickListenertoSpaceManagePlace() {
        binding.tvHome.setOnClickListener {
          homeSelectTask()
        }
        binding.tvPrivateRoom.setOnClickListener {
         privateRoomSelectTask()
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


    private fun clearBathRommBackground(){
        binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv1Bathrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv2Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv3Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv4Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv5Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv7Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv8Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
    }


    private fun bathRoomAnySelect(){
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

    private fun bathRoomFirstSelect(){
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

    private fun bathRoomSecondSelect(){
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

    private fun bathRoomThirdSelect(){
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

    private fun bathRoomFourthSelect(){
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

    private fun bathRoomFifthSelect(){
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

    private fun bathroom7Select(){
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

    private fun bathRoom8Select(){
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


    private fun settingBackgroundTaskToBedroom() {
        binding.tvAnyBedrooms.setOnClickListener {
            bathRoomAnySelect()
        }

        binding.tv1Bathrooms.setOnClickListener {
            bathRoomFirstSelect()
        }

        binding.tv2Bathroom.setOnClickListener {
            bathRoomSecondSelect()
        }
        binding.tv3Bathroom.setOnClickListener {
            bathRoomThirdSelect()
        }

        binding.tv4Bathroom.setOnClickListener {
            bathRoomFourthSelect()
        }

        binding.tv5Bathroom.setOnClickListener {
            bathRoomFifthSelect()
        }
        binding.tv7Bathroom.setOnClickListener {
            bathroom7Select()
        }

        binding.tv8Bathroom.setOnClickListener {
            bathRoom8Select()
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
                clearBathRommBackground()
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



    private fun bedRoomAnySelect(){
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


    private fun bedRoomFirstSelect(){
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

    private fun bedRoomSecondSelect(){
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

    private fun bedRoomThirdSelect(){
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

    private fun bedRoomFourthSelect(){
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

    private fun bedRoomFifthSelect(){
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

    private fun bedroom7Select(){
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

    private fun bedRoom8Select(){
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



    private fun settingBackgroundTaskToBathroom() {

        binding.tvAnyBedrooms.setOnClickListener {
            bedRoomAnySelect()
        }

        binding.tv1Bedroom.setOnClickListener {
            bedRoomFirstSelect()
        }

        binding.tv2Bedroom.setOnClickListener {
            bedRoomSecondSelect()
        }

        binding.tv3Bedroom.setOnClickListener {
            bedRoomThirdSelect()
        }

        binding.tv4Bedroom.setOnClickListener {
            bedRoomFourthSelect()
        }

        binding.tv5Bedroom.setOnClickListener {
            bedRoomFifthSelect()
        }


        binding.tv7Bedroom.setOnClickListener {
          bedroom7Select()
        }

        binding.tv8Bedroom.setOnClickListener {
            bedRoom8Select()
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
                badRoomClearBackground()
            }
        })
    }

    private fun badRoomClearBackground(){
        binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv1Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv2Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv3Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv4Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv5Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv7Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv8Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
    }


    private fun propertyRoomAnySelect(){
        binding.tvAny.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
        propertySize =0
    }


    private fun propertyRoomFirstSelect(){
        binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv250.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
        propertySize =250

    }

    private fun propertySecondSelect(){
        binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv350.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
        propertySize = 350
    }

    private fun propertyThirdSelect(){
        binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv450.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
        propertySize = 450
    }

    private fun clearPropertyBackground(){
        binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)

    }


    private fun propertyFourthSelect(){
        binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv550.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
        propertySize = 550
    }

    private fun propertyFifthSelect(){
        binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv650.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
        propertySize = 650
    }

    private fun property7Select(){
        binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv750.setBackgroundResource(R.drawable.bg_inner_select_white)
        propertySize = 750
    }


    private fun settingBackgroundTaskToProperty() {
        binding.tvAny.setOnClickListener {
            propertyRoomAnySelect()
        }
        binding.tv250.setOnClickListener {
            propertyRoomFirstSelect()
        }
        binding.tv350.setOnClickListener {
            propertySecondSelect()
        }
        binding.tv450.setOnClickListener {
            propertyThirdSelect()
        }
        binding.tv550.setOnClickListener {
            propertyFourthSelect()
        }
        binding.tv650.setOnClickListener {
            propertyFifthSelect()
        }
        binding.tv750.setOnClickListener {
            property7Select()
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
                clearPropertyBackground()
            }
        })
    }

    private fun byDefaultSelectAvailability() {
        binding.tvAny.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_inner_select_white)
    }

    private fun anyPeopleCount(){
        binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tv1.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv2.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv3.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv5.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv7.setBackgroundResource(R.drawable.bg_outer_manage_place)
        peopleCount =0
    }

    private fun onePeopleCount(){
        binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv1.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tv2.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv3.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv5.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv7.setBackgroundResource(R.drawable.bg_outer_manage_place)
        peopleCount =1
    }

    private fun twoPeopleCount(){
        binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv1.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv2.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tv3.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv5.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv7.setBackgroundResource(R.drawable.bg_outer_manage_place)
        peopleCount = 2
    }

    private fun peopleCount3(){
        binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv1.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv2.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv3.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tv5.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv7.setBackgroundResource(R.drawable.bg_outer_manage_place)
        peopleCount =3
    }

    private fun peopleCount5(){
        binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv1.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv2.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv3.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv5.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tv7.setBackgroundResource(R.drawable.bg_outer_manage_place)
        peopleCount =5
    }

    private fun peopleCount7(){
        binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv1.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv2.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv3.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv5.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv7.setBackgroundResource(R.drawable.bg_inner_select_white)
        peopleCount =7
    }

    private fun settingBackgroundTaskToPeople() {
        //No of people
        binding.tvAnyPeople.setOnClickListener {
          anyPeopleCount()
        }


        binding.tv1.setOnClickListener {
            onePeopleCount()
        }
        binding.tv2.setOnClickListener {
            twoPeopleCount()
        }

        binding.tv3.setOnClickListener {
         peopleCount3()
        }

        binding.tv5.setOnClickListener {
            peopleCount5()
        }

        binding.tv7.setOnClickListener {
          peopleCount7()
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
                clearPeopleCountBackground()
            }
        })
    }

    fun clearPeopleCountBackground(){
        binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv1.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv2.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv3.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv5.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tv7.setBackgroundResource(R.drawable.bg_outer_manage_place)
    }

    fun settingDataToActivityModel() {
        var data = PrepareData.getAmenitiesList()
        activityList = data.first
        amenitiesList = PrepareData.getOnlyAmenitiesList()
    }

    fun setUpRecyclerView() {
        galleryAdapter = GallaryAdapter(imageList,requireContext())
        galleryAdapter.updateAdapter(imageList)


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
//        val newYork = LatLng(40.7128, -74.0060)
//        mMap?.addMarker(MarkerOptions().position(newYork).title("Marker in New York"))
//        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(newYork, 10f))
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
        val dialog = context?.let {
            Dialog(it, R.style.BottomSheetDialog)
        }
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
                    Toast.makeText(requireContext(), "Please enter valid details", Toast.LENGTH_SHORT).show()
                }
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }


    private fun anyMonth(){
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

    private fun janSelect(){
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

    private fun febSelect(){
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


    private fun marchSelect(){
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

    private fun aprilSelect(){
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

    private fun maySelect(){
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

    private fun juneSelect(){
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

    private fun julySelect(){
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

    private fun augustSelect(){
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

    private fun septemberSelect(){
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

    private fun octoberSelect(){
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

    private fun novemberSelect(){
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

    private fun settingBackgroundAllMonth() {
        //No of people
        binding.tvAll.setOnClickListener {
            anyMonth()
        }


        binding.tvJan.setOnClickListener {
            janSelect()
        }
        binding.tvFeb.setOnClickListener {
            febSelect()
        }


        binding.tvMar.setOnClickListener {
           marchSelect()
        }

        binding.tvApr.setOnClickListener {
           aprilSelect()
        }

        binding.tvMay.setOnClickListener {
            maySelect()
        }
        binding.tvJun.setOnClickListener {
            juneSelect()
        }
        binding.tvJul.setOnClickListener {
           julySelect()
        }
        binding.tvAug.setOnClickListener {
            augustSelect()
        }
        binding.tvSep.setOnClickListener {
          septemberSelect()
        }
        binding.tvOct.setOnClickListener {
            octoberSelect()
        }
        binding.tvNov.setOnClickListener {
          novemberSelect()
        }
        binding.tvDec.setOnClickListener {
         decSelect()
        }


    }

    private fun decSelect(){
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

    private fun anyWeekSelect(){
        binding.tvAllDays.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tvOnlyWorkingDays.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tvOnlyWeekends.setBackgroundResource(R.drawable.bg_outer_manage_place)
        days = "all"
    }

    fun onlyWorkingDay(){
        binding.tvAllDays.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tvOnlyWorkingDays.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tvOnlyWeekends.setBackgroundResource(R.drawable.bg_outer_manage_place)
        days = "working_days"
    }
     fun onlyWeekend(){
        binding.tvAllDays.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tvOnlyWorkingDays.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tvOnlyWeekends.setBackgroundResource(R.drawable.bg_inner_select_white)
        days = "weekends"
    }

    fun settingBackgroundAllWeek() {

        binding.tvAllDays.setOnClickListener {
            anyWeekSelect()
        }
        binding.tvOnlyWorkingDays.setOnClickListener {
            onlyWorkingDay()
        }
        binding.tvOnlyWeekends.setOnClickListener {
            onlyWeekend()
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
        val items = PrepareData.getPriceAndHourList()

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