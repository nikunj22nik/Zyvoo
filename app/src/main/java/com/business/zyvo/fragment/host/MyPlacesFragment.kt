package com.business.zyvo.fragment.host

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.business.zyvo.AppConstant
import com.business.zyvo.LoadingUtils
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.adapter.host.MyPlacesHostAdapter
import com.business.zyvo.databinding.FragmentMyPlacesBinding
import com.business.zyvo.locationManager.LocationHelper
import com.business.zyvo.model.HostMyPlacesModel
import com.business.zyvo.model.ViewpagerModel
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.CommonAuthWorkUtils
import com.business.zyvo.viewmodel.host.MyPlaceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MyPlacesFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentMyPlacesBinding? = null
    private  val binding get() =  _binding!!

    private lateinit var placesList: MutableList<HostMyPlacesModel>

    private lateinit var locationHelper: LocationHelper

    lateinit var adapter: MyPlacesHostAdapter

    private val myPlaceViewModel: MyPlaceViewModel by lazy {
        ViewModelProvider(this)[MyPlaceViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentMyPlacesBinding.inflate(LayoutInflater.from(requireContext()))
        adapter = MyPlacesHostAdapter(requireContext(), mutableListOf())
        binding.recyclerMyPlaces.adapter = adapter
        adapter.setOnItemClickListener(object : MyPlacesHostAdapter.onItemClickListener {
            override fun onItemClick(position: HostMyPlacesModel, type: String) {
                if (type.equals(AppConstant.DELETE)) {

                    deleteProperty(position.property_id)
                } else if (type.equals(AppConstant.EDIT)) {
                    val bundle = Bundle()
                    val id = position.property_id
                    Log.d("TESTING_ID", id.toString())
                    bundle.putInt(AppConstant.PROPERTY_ID, id)
                    findNavController().navigate(
                        R.id.host_fragment_property_to_host_manage_property_frag,
                        bundle
                    )
                } else if (type.equals(AppConstant.placeOpenActivity)) {
                    val property_images = position.property_images[0]
                    val latitude = position.latitude
                    val longitude = position.longitude
                    val property_rating = position.property_rating
                    val property_status = position.property_status
                    val property_review_count = position.property_review_count
                    val distance_miles = position.distance_miles
                    val title = position.title

                    val bundle = Bundle().apply {
                        putString(AppConstant.PROPERTY_ID, position.property_id.toString())
                        putString(AppConstant.property_images, property_images)
                        putString(AppConstant.latitude, latitude)
                        putString(AppConstant.longitude, longitude)
                        putString(AppConstant.property_rating, property_rating)
                        putString(AppConstant.property_status, property_status)
                        putString(AppConstant.property_review_count, property_review_count)
                        putString(AppConstant.distance_miles, distance_miles)
                        putString(AppConstant.title, title)
                    }
                    findNavController().navigate(R.id.placeOpenFragment, bundle)


                }
            }
        })


        binding.recyclerMyPlaces.isNestedScrollingEnabled = false

        binding.floatingIcon.setOnClickListener {
            findNavController().navigate(R.id.host_fragment_property_to_host_manage_property_frag)
        }

        binding.imgFilter.setOnClickListener(this)
        binding.rlAddNewPlace.setOnClickListener(this)
        binding.tvPlaces.setOnClickListener(this)
        binding.rlAddNewPlace1.setOnClickListener(this)
        binding.rlPrice.setOnClickListener(this)

        initialization()

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finishAffinity()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        return _binding!!.root
    }

    private fun deleteProperty(propertyId: Int) {
        lifecycleScope.launch {
            Log.d("TESTING_PROPERTY_ID", propertyId.toString())
            LoadingUtils.showDialog(requireContext(), false)
            myPlaceViewModel.deleteProperty(propertyId).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        LoadingUtils.hideDialog()
                        Log.d("TESTING_PROPERTY_ID", propertyId.toString())
                        var pp = placesList.filter {
                            it.property_id != propertyId
                        }
                        adapter.updateData(pp.toMutableList())
                         }
                         is NetworkResult.Error ->{
                             LoadingUtils.hideDialog()
                             LoadingUtils.showSuccessDialog(requireContext(),it.message.toString())
                         }
                         else ->{
                         }
                }
            }
        }
    }

    fun initialization() {
        binding.dataView.visibility = View.VISIBLE
        binding.noDataView.visibility = View.GONE

        var commonAuthWorkUtils = CommonAuthWorkUtils(requireActivity(), findNavController())
        if (commonAuthWorkUtils.isScreenLarge(requireContext())) {
            // Use GridLayoutManager for larger screens (e.g., tablets)
            val gridLayoutManager = GridLayoutManager(requireContext(), 2) // 2 columns for grid
            binding.recyclerMyPlaces.setLayoutManager(gridLayoutManager)
        } else {
            // Use LinearLayoutManager for smaller screens (e.g., phones)
            val linearLayoutManager = LinearLayoutManager(requireContext())
            binding.recyclerMyPlaces.setLayoutManager(linearLayoutManager)
        }

        locationTask()
    }


    private fun locationTask() {
        locationHelper = LocationHelper(requireContext())

        // Check if location permission is granted
        if (locationHelper.checkLocationPermission()) {
            // If permission granted, fetch location
            locationHelper.getLocationInBackground(lifecycle) { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    var sessionManager = SessionManager(requireContext())

                    sessionManager.setLatitude(latitude.toString())
                    sessionManager.setLongitude(longitude.toString())
                    myPlaceApi(latitude, longitude)
                } else {
                    myPlaceApi(null, null)
                }
            }
        } else {
           // permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            alertBoxLocation1()
            //  locationHelper.requestLocationPermission(requireActivity())
        }
    }
    private fun alertBoxLocation1() {
//        val dialogView = layoutInflater.inflate(R.layout.dialog_location_permission, null)
//
//        val builder = AlertDialog.Builder(requireContext())
//        builder.setView(dialogView)
//
//        val alertDialog = builder.create()
//        alertDialog.setCancelable(false)
//
//        val btnAllow = dialogView.findViewById<TextView>(R.id.btnLocation)
//        val btnCancel = dialogView.findViewById<TextView>(R.id.textNotnow)
//
//        btnAllow.setOnClickListener {
//            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//            val uri = Uri.fromParts("package", requireContext().packageName, null)
//            intent.data = uri
//            startActivityForResult(intent, 200)
//            alertDialog.dismiss()
//        }
//
//        btnCancel.setOnClickListener {
//            alertDialog.dismiss()
//        }
//
//        alertDialog.show()

        val dialog = Dialog(requireActivity(), R.style.BottomSheetDialog)
        dialog.setContentView(R.layout.dialog_location_permission)

        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val btnTurnOnLocation = dialog.findViewById<TextView>(R.id.btnLocation)
        val btnCancel = dialog.findViewById<TextView>(R.id.textNotnow)

        btnTurnOnLocation.setOnClickListener {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivityForResult(intent, 200)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        //dialog.setCancelable(false)
        dialog.show()
    }
    private fun myPlaceApi(latitude: Double?, longitude: Double?) {
        var userId = SessionManager(requireContext()).getUserId()
        LoadingUtils.showDialog(requireContext(), false)
        lifecycleScope.launch {
            if (userId != null) {
                myPlaceViewModel.getMyPlaces(userId, latitude, longitude).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { it1 ->
                                placesList = it.data.first
                                if (it1.first.size < 3) {
                                    binding.floatingIcon.visibility = View.GONE
                                } else {
                                    binding.floatingIcon.visibility = View.VISIBLE
                                }
                                binding.rlPrice.visibility = View.VISIBLE
                                binding.tvTxt.setText("$"+ it1.second)
                                adapter.updateData(it1.first)
                                LoadingUtils.hideDialog()
                            }
                        }

                        is NetworkResult.Error -> {
                            LoadingUtils.hideDialog()

                            LoadingUtils.showSuccessDialog(requireContext(),it.message.toString())

                        }

                        else -> {
                            LoadingUtils.hideDialog()

                        }
                    }
                }
            }

        }
    }


    private fun loadImages(): MutableList<ViewpagerModel> {
        val images = mutableListOf<ViewpagerModel>(
            ViewpagerModel(R.drawable.ic_image_for_viewpager),
            ViewpagerModel(R.drawable.ic_image_for_viewpager),
            ViewpagerModel(R.drawable.ic_image_for_viewpager),
            ViewpagerModel(R.drawable.ic_image_for_viewpager),
            ViewpagerModel(R.drawable.image_hotel),
            ViewpagerModel(R.drawable.image_hotel),
            ViewpagerModel(R.drawable.image_hotel)
        )
        return images
    }


    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted, fetch location
                locationHelper.getLocationInBackground(viewLifecycleOwner.lifecycle) { location ->
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude

                        var sessionManager = SessionManager(requireContext())

                        sessionManager.setLatitude(latitude.toString())
                        sessionManager.setLongitude(longitude.toString())

                        myPlaceApi(latitude, longitude)
                    } else {
                        myPlaceApi(null, null)
                    }
                }
            } else {
                // Permission denied, show a message
                Toast.makeText(
                    requireContext(),
                    "Location permission is required.",
                    Toast.LENGTH_SHORT
                ).show()
                myPlaceApi(null, null)
            }
        }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("TESTING", "Inside ZYVOO Permission")
        // Check if the permission is granted
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, proceed with fetching the location


            locationHelper.getLocationInBackground(lifecycle) { location ->
                if (location != null) {
                    Log.d("TESTING", "Inside Location not null")
                    val latitude = location.latitude
                    val longitude = location.longitude
                    // Call the API with the location

                    var sessionManager = SessionManager(requireContext())

                    sessionManager.setLatitude(latitude.toString())
                    sessionManager.setLongitude(longitude.toString())
                    myPlaceApi(latitude, longitude)
                } else {
                    Log.d("TESTING", "Inside Location  null")
                    // Call the API with null values if location is null
                    myPlaceApi(null, null)
                }
            }
        } else {
            // Permission denied, handle accordingly
            // You can show a message to the user explaining that the permission is required
            Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT)
                .show()
        }


    }

    private fun showPopupWindow(anchorView: View, position: Int) {
        // Inflate the custom layout for the popup menu
        val popupView = LayoutInflater.from(context).inflate(R.layout.popup_earning, null)

        // Create PopupWindow with the custom layout
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set click listeners for each menu item in the popup layout

        popupView.findViewById<TextView>(R.id.itemAllConversations).setOnClickListener {
            totalEarningApi()
            popupWindow.dismiss()
        }

        popupView.findViewById<TextView>(R.id.itemArchived).setOnClickListener {
            futureEarningApi()
            popupWindow.dismiss()
        }

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
        popupWindow.showAsDropDown(
            anchorView,
            xOffset,
            yOffset,
            Gravity.END
        )  // Adjust the Y offset dynamically
    }

    private fun futureEarningApi() {
        lifecycleScope.launch {
            var sessionManager: SessionManager = SessionManager(requireContext())
            var hostId = sessionManager.getUserId()
            binding.rlPrice.visibility = View.VISIBLE
            if (hostId != null) {
                LoadingUtils.showDialog(requireContext(), false)
                myPlaceViewModel.earning(hostId, "future").collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            var data = it.data
                            binding.tvTxt.setText(data)
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
    }

    private fun totalEarningApi() {
        lifecycleScope.launch {
            binding.rlPrice.visibility = View.VISIBLE
            val sessionManager: SessionManager = SessionManager(requireContext())
            val hostId = sessionManager.getUserId()
            if (hostId != null) {
                LoadingUtils.showDialog(requireContext(), false)
                myPlaceViewModel.earning(hostId, "total").collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            var data = it.data
                            binding.tvTxt.setText(data)
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
    }

    private fun showPopupWindowPrice(anchorView: View, position: Int) {
        val popupView = LayoutInflater.from(context).inflate(R.layout.popup_layout_pets, null)

        // Create PopupWindow with the custom layout
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set click listeners for each menu item in the popup layout

//        popupView.findViewById<TextView>(R.id.itemAllConversations).setOnClickListener {
//            popupWindow.dismiss()
//        }
//
//        popupView.findViewById<TextView>(R.id.itemArchived).setOnClickListener {
//            popupWindow.dismiss()
//        }

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
        popupWindow.showAsDropDown(anchorView, xOffset, yOffset, Gravity.END)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.img_filter -> {
                showPopupWindow(binding.imgFilter, 0)
            }

            R.id.rl_add_new_place -> {
                findNavController().navigate(R.id.host_fragment_property_to_host_manage_property_frag)
            }

            R.id.rl_add_new_place_1 -> {
                findNavController().navigate(R.id.host_fragment_property_to_host_manage_property_frag)
            }

            R.id.tv_places -> {
//                var intent = Intent(requireContext(), PlaceOpenActivity::class.java)
//                startActivity(intent)
            }

            R.id.rl_price -> {
                showPopupWindowPrice(binding.rlPrice, 0)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

       // _binding = null
    }

}