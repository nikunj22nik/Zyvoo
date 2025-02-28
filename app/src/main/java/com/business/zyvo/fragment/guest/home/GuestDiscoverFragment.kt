package com.business.zyvo.fragment.guest.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.business.zyvo.AppConstant
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.OnClickListener
import com.business.zyvo.OnClickListener1
import com.business.zyvo.R
import com.business.zyvo.activity.GuesMain
import com.business.zyvo.activity.guest.ExtraTimeChargesActivity
import com.business.zyvo.activity.guest.FiltersActivity
import com.business.zyvo.activity.guest.RestaurantDetailActivity
import com.business.zyvo.activity.guest.WhereTimeActivity
import com.business.zyvo.adapter.WishlistAdapter
import com.business.zyvo.adapter.guest.HomeScreenAdapter
import com.business.zyvo.databinding.FragmentGuestDiscoverBinding
import com.business.zyvo.fragment.guest.home.model.HomePropertyData
import com.business.zyvo.fragment.guest.home.model.WishlistItem
import com.business.zyvo.fragment.guest.home.viewModel.GuestDiscoverViewModel
import com.business.zyvo.model.FilterRequest
import com.business.zyvo.model.Location
import com.business.zyvo.utils.CommonAuthWorkUtils
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.ErrorDialog.showToast
import com.business.zyvo.utils.NetworkMonitorCheck
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GuestDiscoverFragment : Fragment(),View.OnClickListener,OnClickListener, OnMapReadyCallback,
    OnClickListener1 {

    lateinit var binding :FragmentGuestDiscoverBinding
    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private val totalDuration = 20000L
    private lateinit var adapter: HomeScreenAdapter
    private lateinit var mapView: MapView
    private var commonAuthWorkUtils: CommonAuthWorkUtils? = null
    private lateinit var googleMap: GoogleMap
    private var latitude: String = ""
    private var longitude: String = ""
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var locationManager: LocationManager? = null
    var session: SessionManager?=null
    private var homePropertyData: MutableList<HomePropertyData> = mutableListOf()
    private var wishlistItem: MutableList<WishlistItem> = mutableListOf()


    private val guestDiscoverViewModel: GuestDiscoverViewModel by lazy {
        ViewModelProvider(this)[GuestDiscoverViewModel::class.java]
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View {

        binding = FragmentGuestDiscoverBinding.inflate(LayoutInflater.from(requireContext()))
        val navController = findNavController()

        // Observe the isLoading state
        lifecycleScope.launch {
            guestDiscoverViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    LoadingUtils.showDialog(requireContext(), false)
                } else {
                    LoadingUtils.hideDialog()
                }
            }
        }

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                // Handle the result
            }
        }
        mapView = binding.map
        commonAuthWorkUtils = CommonAuthWorkUtils(requireActivity(),navController)
        session = SessionManager(requireActivity())

        adapter = HomeScreenAdapter(requireContext(), homePropertyData,
            this)

        setRetainInstance(true);

        binding.recyclerViewBooking.adapter = adapter

        binding.filterIcon.setOnClickListener {
            val intent = Intent(requireContext(),FiltersActivity::class.java)
            startForResult.launch(intent)
        }

        binding.textWhere.setOnClickListener(this)
        binding.textTime.setOnClickListener(this)
        binding.textActivity.setOnClickListener(this)
        binding.rlShowMap.setOnClickListener(this)
        binding.customProgressBar.setOnClickListener(this)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        adapterClickListnerTask()

        // This is use for LocationServices declaration
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationManager = requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager


        // This condition for check location run time permission
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 100)
        }

        val filterData = arguments?.getParcelable<FilterRequest>("filter_data")
        filterData?.let {
            Log.d("FilterData", "User ID: ${it.user_id}, Location: ${it.location}, Price: ${it.minimum_price} - ${it.maximum_price}")

            filteredDataAPI(it)
        }

        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.clSearch.visibility = View.VISIBLE
        val locations = listOf(
            Location(37.7749, -122.4194, "San Francisco"),
            Location(34.0522, -118.2437, "Los Angeles"),
            Location(40.7128, -74.0060, "New York")
        )

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finishAffinity()
                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)


        binding.customProgressBar.setProgressWidth(15f)
       // binding.customProgressBar.setMax(100.0) // Set max progress as 100%
        binding.customProgressBar.setMax(100.0)

        // Start the countdown timer
        startCountdown()
    }
    private fun startCountdown() {
        // Countdown timer for 20 seconds with 1-second intervals
        object : CountDownTimer(totalDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                val progress = ((totalDuration - millisUntilFinished).toDouble() / totalDuration) * 100
                binding.customProgressBar.setProgress(progress)
            }

            override fun onFinish() {
                binding.customProgressBar.setProgress(100.0)
            }
        }.start()
    }
    @SuppressLint("SetTextI18n")
    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.textWhere ->{
                val intent = Intent(requireContext(),WhereTimeActivity::class.java)
                intent.putExtra(AppConstant.WHERE, AppConstant.WHERE)
                startActivity(intent)
            }
            R.id.textTime ->{
                val intent = Intent(requireContext(),WhereTimeActivity::class.java)
                intent.putExtra(AppConstant.TIME, AppConstant.TIME)
                startActivity(intent)
            }
            R.id.textActivity ->{
                val intent = Intent(requireContext(),WhereTimeActivity::class.java)
                intent.putExtra(AppConstant.ACTIVITY, AppConstant.ACTIVITY)
                startActivity(intent)
            }
            R.id.rl_show_map ->{
                if(binding.recyclerViewBooking.visibility == View.VISIBLE){
                    binding.tvMapContent.setText("Show List")
                    binding.rlMapView.visibility = View.VISIBLE
                    binding.recyclerViewBooking.visibility = View.GONE
                    binding.clTimeLeftProgressBar.visibility = View.GONE
                    binding.clSearch.visibility = View.GONE
                    if (homePropertyData.isNotEmpty()) {
                        for (location in homePropertyData) {
                          location.latitude?.let {
                              location.longitude?.let {
                                  val customMarkerBitmap =
                                      createCustomMarker(requireContext(), "${location.hourly_rate}/h")
                                  val markerOptions = MarkerOptions()
                                      .position(LatLng(location.latitude.toDouble(), location.longitude.toDouble()))
                                      .icon(BitmapDescriptorFactory.fromBitmap(customMarkerBitmap))
                                      .title("${location.hourly_rate}/h")
                                  googleMap?.addMarker(markerOptions)
                                  // Move and zoom the camera to the first location
                                      googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                          LatLng(location.latitude.toDouble(), location.longitude.toDouble()), 10f))
                              }
                          }
                        }
                        // Apply custom style to the map
                        val success: Boolean = googleMap!!.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
                        )
                        if (!success) {
                            Log.e("MapsActivity", "Style parsing failed.")
                        }
                    }
                    try {
                        val params = binding.rlShowMap.layoutParams as ConstraintLayout.LayoutParams
                        params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, 300) // 50px bottom margin
                        binding.rlShowMap.layoutParams = params
                        binding.rlShowMap.requestLayout() // Ensure UI updates
                    }catch (e:Exception){
                        e.message
                    }
                }
                else{
                    binding.tvMapContent.setText("Show Map")
                    binding.rlMapView.visibility = View.GONE
                    binding.recyclerViewBooking.visibility = View.VISIBLE
                    binding.clTimeLeftProgressBar.visibility = View.VISIBLE
                    try {
                        val params = binding.rlShowMap.layoutParams as ConstraintLayout.LayoutParams
                        params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, 50) // 50px bottom margin
                        binding.rlShowMap.layoutParams = params
                        binding.rlShowMap.requestLayout() // Ensure UI updates

                    }catch (e:Exception){
                        e.message
                    }

                }
            }

            R.id.customProgressBar ->{
                val intent = Intent(requireContext(),ExtraTimeChargesActivity::class.java)
                intent.putExtra(AppConstant.TIME, AppConstant.TIME)
                startActivity(intent)
            }

        }
    }



    private fun adapterClickListnerTask(){
        Log.d(ErrorDialog.TAG,"I AM HERE IN AdapterClickListener Task")

        adapter.setOnItemClickListener(object : HomeScreenAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                Log.d(ErrorDialog.TAG,"I AM HERE IN DEVELOPMENT")
                val intent = Intent(requireContext(),RestaurantDetailActivity::class.java)
                startActivity(intent)
            }

        })
    }

    override fun itemClick(obj: Int) {}

    override fun onMapReady(mp: GoogleMap) {
        try {
            googleMap = mp
            // Add a marker in New York and move the camera

          /*  // Example coordinates
            val locations = listOf(
                Location(37.7749, -122.4194, "$13 / h"),
                Location(34.0522, -118.2437, "$15 / h"),
                Location(40.7128, -74.0060, "$19 / h"),
                Location(51.5074, -0.1278, "$23 / h"),
                Location(48.8566, 2.3522, "$67 / h")
            )

            for (location in locations) {
                val customMarkerBitmap = createCustomMarker(requireContext(), location.title)

                val markerOptions = MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .icon(BitmapDescriptorFactory.fromBitmap(customMarkerBitmap))
                    .title(location.title)

                googleMap.addMarker(markerOptions)
            }

            // Move and zoom the camera to the first location
            if (locations.isNotEmpty()) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(locations[0].latitude, locations[0].longitude), 10f))
            }

            // Apply custom style to the map
            val success: Boolean = googleMap!!.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            )
            if (!success) {
                Log.e("MapsActivity", "Style parsing failed.")
            }*/
        } catch (e: Resources.NotFoundException) {
            Log.e("MapsActivity", "Can't find style. Error: ", e)
        }






    }

    private fun createCustomMarker(context: Context, text: String): Bitmap {
        val markerView = LayoutInflater.from(context).inflate(R.layout.custom_marker, null)
        val label = markerView.findViewById<TextView>(R.id.label)
        label.text = text

        markerView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)
        val bitmap = Bitmap.createBitmap(
            markerView.measuredWidth,
            markerView.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        markerView.draw(canvas)
        return bitmap
    }



    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }




   private fun showAddWishlistDialog(property_id: String,pos: Int) {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_add_wishlist)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            val dialogAdapter = WishlistAdapter(requireContext(),true, wishlistItem,object: OnClickListener{
                override fun itemClick(obj: Int) {
                    saveItemInWishlist(property_id, pos,wishlistItem?.get(pos)?.wishlist_id.toString(),
                        dialog)
                }

            })
        val rvWishList : RecyclerView =  findViewById(R.id.rvWishList)
            rvWishList.adapter = dialogAdapter
            findViewById<ImageView>(R.id.imageCross).setOnClickListener {
                dismiss()
            }
      findViewById<TextView>(R.id.textCreateWishList).setOnClickListener {
          createWishListDialog(property_id,pos)
                dismiss()
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
            getWisList(dialogAdapter)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun createWishListDialog(property_id: String,pos: Int){
        val dialog = context?.let {
            Dialog(it, R.style.BottomSheetDialog)
        }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_create_wishlist)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            val etDescription =    findViewById<EditText>(R.id.etDescription)

            val tvMaxCount =    findViewById<TextView>(R.id.textMaxCount)
            setupCharacterCountListener(etDescription, tvMaxCount, 50)

            val etName =    findViewById<EditText>(R.id.etName)
            findViewById<ImageView>(R.id.imageCross).setOnClickListener {
                dismiss()
            }
            findViewById<TextView>(R.id.textCreate).setOnClickListener {
                if (etName.text.isEmpty()){
                    etName.error = AppConstant.name
                    etName.requestFocus()
                    showToast(requireContext(),AppConstant.name)
                }else if (etDescription.text.isEmpty()){
                    etDescription.error = AppConstant.description
                    etDescription.requestFocus()
                    showToast(requireContext(),AppConstant.description)
                }else{
                    createWishlist(etName.text.toString(),etDescription.text.toString(),
                        property_id,dialog,pos)
                }
            }
            findViewById<TextView>(R.id.textClear).setOnClickListener {
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }

    }

    private fun getWisList(dialogAdapter: WishlistAdapter) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                guestDiscoverViewModel.getWisList(session?.getUserId().toString()).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                val listType = object : TypeToken<List<WishlistItem>>() {}.type
                                val wish: MutableList<WishlistItem> = Gson().fromJson(resp, listType)
                                wishlistItem = wish
                                if (wishlistItem.isNotEmpty()) {
                                    dialogAdapter.updateItem(wishlistItem)
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
            showErrorDialog(requireContext(),
                resources.getString(R.string.no_internet_dialog_msg))
        }
    }

    private fun createWishlist(name: String,
                               description: String,
                               property_id: String,
                               dialog: Dialog,
                               pos: Int) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                guestDiscoverViewModel.createWishlist(session?.getUserId().toString(),
                    name,description,property_id).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                showToast(requireContext(),resp.first)
                                val homeData = homePropertyData.get(pos)
                                homeData.is_in_wishlist = 1
                                homePropertyData.set(pos,homeData)
                                adapter.updateData(homePropertyData)
                                dialog.dismiss()
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
            showErrorDialog(requireContext(),
                resources.getString(R.string.no_internet_dialog_msg))
        }
    }

    private fun removeItemFromWishlist(property_id: String,pos: Int) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                guestDiscoverViewModel.removeItemFromWishlist(session?.getUserId().toString(),
                    property_id).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                showToast(requireContext(),resp.first)
                                val homeData = homePropertyData.get(pos)
                                homeData.is_in_wishlist = 0
                                homePropertyData.set(pos,homeData)
                                adapter.updateData(homePropertyData)

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
            showErrorDialog(requireContext(),
                resources.getString(R.string.no_internet_dialog_msg))
        }
    }

    private fun saveItemInWishlist(property_id: String,pos: Int,
                                   wishlist_id: String,dialog: Dialog) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                guestDiscoverViewModel.saveItemInWishlist(session?.getUserId().toString(),
                    property_id,
                    wishlist_id).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                showToast(requireContext(),resp.first)
                                val homeData = homePropertyData.get(pos)
                                homeData.is_in_wishlist = 1
                                homePropertyData.set(pos,homeData)
                                adapter.updateData(homePropertyData)
                                dialog.dismiss()

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
            showErrorDialog(requireContext(),
                resources.getString(R.string.no_internet_dialog_msg))
        }
    }

    private fun setupCharacterCountListener(editText: EditText, textView: TextView, maxLength: Int) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val remainingChars = maxLength - (s?.length ?: 0)
                textView.text = "max $remainingChars characters"
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun itemClick(obj: Int, text: String) {
        when(text){
            "Add Wish"->{
                showAddWishlistDialog(homePropertyData?.get(obj)?.property_id.toString(),obj)
            }
            "Remove Wish"->{
                removeItemFromWishlist(homePropertyData?.get(obj)?.property_id.toString(),obj)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        (activity as? GuesMain)?.discoverResume()
    }




    private fun getCurrentLocation() {
        // Initialize Location manager
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Check condition
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        ) {
            // When location service is enabled
            // Get last location
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mFusedLocationClient.lastLocation.addOnCompleteListener { task ->
                // Initialize location
                val location = task.result
                // Check condition
                if (location != null) {
                    latitude = location.latitude.toString()
                    longitude = location.longitude.toString()
                    loadHomeApi()
                } else {
                    val locationRequest =
                        LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(10000)
                            .setFastestInterval(1000)
                            .setNumUpdates(1)

                    val locationCallback: LocationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            // Initialize
                            val location1 = locationResult.lastLocation
                            if (location1 != null) {
                                latitude = location1.latitude.toString()
                                longitude = location1.longitude.toString()
                                loadHomeApi()
                            }
                        }
                    }
//                    // Request location updates
                    mFusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.myLooper()!!
                    )

                }
            }
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 100
            )
        }
    }

    private fun loadHomeApi() {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                guestDiscoverViewModel.getHomeData(session?.getUserId().toString(),
                    latitude,longitude).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                val listType = object : TypeToken<List<HomePropertyData>>() {}.type
                                val properties: MutableList<HomePropertyData> = Gson().fromJson(resp, listType)
                                homePropertyData = properties
                                if (homePropertyData.isNotEmpty()) {
                                    adapter.updateData(homePropertyData)
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
            showErrorDialog(requireContext(),
                resources.getString(R.string.no_internet_dialog_msg))
        }
    }

    private fun filteredDataAPI(filterRequest: FilterRequest) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                guestDiscoverViewModel.getFilterHomeDataApi(filterRequest.user_id,filterRequest.latitude,filterRequest.longitude,
                    filterRequest.place_type,filterRequest.minimum_price,filterRequest.maximum_price,filterRequest.location,filterRequest.date,
                    filterRequest.time,filterRequest.people_count,filterRequest.property_size,filterRequest.bedroom,filterRequest.bathroom,filterRequest.instant_booking,
                    filterRequest.self_check_in,filterRequest.allows_pets,filterRequest.activities,filterRequest.amenities,filterRequest.languages).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                val listType = object : TypeToken<List<HomePropertyData>>() {}.type
                                val properties: MutableList<HomePropertyData> = Gson().fromJson(resp, listType)
                                homePropertyData = properties
                                if (homePropertyData.isNotEmpty()) {
                                    adapter.updateData(homePropertyData)
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
            showErrorDialog(requireContext(),
                resources.getString(R.string.no_internet_dialog_msg))
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (Activity.RESULT_OK == resultCode) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Please turn on location", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        if (requestCode == 200) {
            getCurrentLocation()
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check condition
        if (requestCode == 100) {
            if (requestCode == 100 && grantResults.isNotEmpty() && (grantResults[0] + grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                displayLocationSettingsRequest(requireActivity())
            } else {
                alertBoxLocation()
            }
        }

        if (requestCode == 1000) {
            if (requestCode == 1000 && grantResults.isNotEmpty() && (grantResults[0] + grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                displayLocationSettingsRequest11(requireActivity())
            } else {
                displayLocationSettingsRequest11(requireActivity())
            }
        }


    }

    private fun displayLocationSettingsRequest(context: Context) {
        val googleApiClient = GoogleApiClient.Builder(context)
            .addApi(LocationServices.API).build()
        googleApiClient.connect()
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 1000
        locationRequest.numUpdates = 1
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result: PendingResult<LocationSettingsResult> =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { result ->
            val status: Status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> {
                    Log.i(ErrorDialog.TAG, "All location settings are satisfied.")
                    getCurrentLocation()
                }
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    Log.i(
                        ErrorDialog.TAG,
                        "Location settings are not satisfied. Show the user a dialog to upgrade location settings "
                    )
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.resolution?.let {
                            startIntentSenderForResult(
                                it.intentSender,
                                100,
                                null,
                                0,
                                0,
                                0,
                                null
                            )
                        }

                    } catch (e: SendIntentException) {
                        Log.i(ErrorDialog.TAG, "PendingIntent unable to execute request.")
                    }
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.i(
                    ErrorDialog.TAG,
                    "Location settings are inadequate, and cannot be fixed here. Dialog not created."
                )

            }
        }
    }

    private fun displayLocationSettingsRequest11(context: Context) {
        val googleApiClient = GoogleApiClient.Builder(context)
            .addApi(LocationServices.API).build()
        googleApiClient.connect()
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 1000
        locationRequest.numUpdates = 1
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result: PendingResult<LocationSettingsResult> =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { result ->
            val status: Status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> {
                    Log.i(ErrorDialog.TAG, "All location settings are satisfied.")
                    getCurrentLocation()
                }
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    Log.i(
                        ErrorDialog.TAG,
                        "Location settings are not satisfied. Show the user a dialog to upgrade location settings "
                    )
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.resolution?.let {
                            startIntentSenderForResult(
                                it.intentSender,
                                1000,
                                null,
                                0,
                                0,
                                0,
                                null
                            )
                        }

                    } catch (e: SendIntentException) {
                        Log.i(ErrorDialog.TAG, "PendingIntent unable to execute request.")
                    }
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.i(
                    ErrorDialog.TAG,
                    "Location settings are inadequate, and cannot be fixed here. Dialog not created."
                )

            }
        }
    }

    private fun alertBoxLocation() {
        val builder = AlertDialog.Builder(requireContext())
        //set title for alert dialog
        builder.setTitle("Alert")
        //set message for alert dialog
        builder.setMessage(R.string.dialogMessage)
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("Yes") { _, _ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivityForResult(intent, 200)
        }
        //performing cancel action
        builder.setNeutralButton("Cancel") { _, _ ->

        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()



        
    }
}