package com.yesitlab.zyvo.fragment.host

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
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
import com.yesitlab.zyvo.AppConstant
import com.yesitlab.zyvo.DateManager.DateManager
import com.yesitlab.zyvo.OnClickListener
import com.yesitlab.zyvo.OnClickListener1
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.adapter.guest.ActivitiesAdapter
import com.yesitlab.zyvo.adapter.guest.AmenitiesAdapter
import com.yesitlab.zyvo.adapter.host.AddOnAdapter
import com.yesitlab.zyvo.adapter.host.AddOnItemAdapter
import com.yesitlab.zyvo.adapter.host.GallaryAdapter
import com.yesitlab.zyvo.adapter.host.RadioTextAdapter
import com.yesitlab.zyvo.databinding.FragmentManagePlaceBinding
import com.yesitlab.zyvo.model.ActivityModel
import com.yesitlab.zyvo.model.host.AddOnModel
import com.yesitlab.zyvo.model.host.ItemRadio
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ManagePlaceFragment : Fragment() , OnMapReadyCallback , OnClickListener1 {

    lateinit var binding :FragmentManagePlaceBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityList : MutableList<ActivityModel>
    private lateinit var amenitiesList :MutableList<String>
    private lateinit var adapterActivity : ActivitiesAdapter
    private lateinit var adapterActivity2 : ActivitiesAdapter
    private lateinit var amenitiesAdapter : AmenitiesAdapter
    private lateinit var mapView: MapView
    private lateinit var imageList: MutableList<Uri>
    private var PICK_IMAGES_REQUEST =210
    private lateinit var galleryAdapter :GallaryAdapter
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentManagePlaceBinding.inflate(inflater,container,false)
        settingDataToActivityModel()
        ActivityCompat.requestPermissions(requireActivity(), permissions(), REQUEST_CODE_STORAGE_PERMISSION)

        initialization()
        setUpRecyclerView()
        mapInitialization(savedInstanceState)
        imagePermissionInitialization()

        val newWork = AddOnModel("Unknown Location", "0")
        addOnList.add(newWork)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
navController = Navigation.findNavController(view)
        settingBackgroundAllWeek()
        settingBackgroundAllMonth()
        onClickDialogOpenner()

        binding.imageBackButton.setOnClickListener {

            if (binding.llHomeSetup.isVisible == true){


                navController.navigateUp()

            }else if(binding.llGalleryLocation.isVisible == true){
                binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_inner_select_white)
                binding.tvGallery.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tvAvailability.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.llHomeSetup.visibility = View.VISIBLE
                binding.llGalleryLocation.visibility = View.GONE
                binding.llAvailability.visibility = View.GONE


            }else if(binding.llAvailability.isVisible == true){

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
            if (binding.llHomeSetup.isVisible == true){
                binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tvGallery.setBackgroundResource(R.drawable.bg_inner_select_white)
                binding.tvAvailability.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.llHomeSetup.visibility = View.GONE
                binding.llGalleryLocation.visibility = View.VISIBLE
                binding.llAvailability.visibility = View.GONE



            }else if(binding.llGalleryLocation.isVisible == true){
                binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tvGallery.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tvAvailability.setBackgroundResource(R.drawable.bg_inner_select_white)
                binding.llHomeSetup.visibility = View.GONE
                binding.llGalleryLocation.visibility = View.GONE
                binding.llAvailability.visibility = View.VISIBLE

                binding.textSaveAndContinueButton.text = "Publish Now"
            }else if(binding.llAvailability.isVisible == true){
                findNavController().navigate(R.id.host_fragment_properties)
            }



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

    private fun imagePermissionInitialization(){

        galleryAdapter.setOnItemClickListener(object :GallaryAdapter.onItemClickListener{
            override fun onItemClick(position: Int, type: String) {

                if(type.equals(AppConstant.DELETE)){
                    imageList.removeAt(position)
                    galleryAdapter.updateAdapter(imageList)
                }else{
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
                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    imageList.add(0,imageUri)
                }
                galleryAdapter.updateAdapter(imageList)
                Toast.makeText(requireContext(), "$count images selected", Toast.LENGTH_SHORT).show()
            } else if (data?.data != null) {
                // Single image selected
                val imageUri = data.data
                imageUris.add(imageUri!!)
                Toast.makeText(requireContext(), "1 image selected", Toast.LENGTH_SHORT).show()
            }

            // You can now handle the selected image URIs in the imageUris list
        }
    }

    private fun initialization(){

        addOnAdapter = AddOnAdapter(requireContext(), addOnList, this)
        binding.recyclerAddOn.adapter = addOnAdapter


        imageList = mutableListOf<Uri>()
        val dummyUri = Uri.parse("http://www.example.com")
        imageList.add(dummyUri)
        adapterActivity = ActivitiesAdapter(requireContext(),activityList.subList(0,3))
        adapterActivity2 = ActivitiesAdapter(requireContext(),activityList.subList(3,activityList.size))
        amenitiesAdapter = AmenitiesAdapter(requireContext(), mutableListOf())
        var hoursList = mutableListOf<String>("24 Hrs","3 Days","7 Days","15 Days","30 Days")
        binding.endHour.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.endHour.arrowAnimate = false
        binding.endHour.setItems(hoursList)
        binding.endHour.setIsFocusable(true)
        val recyclerView = binding.endHour.getSpinnerRecyclerView()
        val spacing = 16 // Spacing in pixels

        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
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

    private fun settingClickListenertoSpaceManagePlace(){
        binding.tvHome.setOnClickListener {
           binding.tvHome.setBackgroundResource(R.drawable.bg_inner_select_white)
           binding.tvPrivateRoom.setBackgroundResource(R.drawable.bg_outer_manage_place)

        }
        binding.tvPrivateRoom.setOnClickListener {
            binding.tvPrivateRoom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tvHome.setBackgroundResource(R.drawable.bg_outer_manage_place)
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

        binding.allowPets.setOnClickListener{
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
        val popupWindow = PopupWindow(popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT
            ,
            ViewGroup.LayoutParams.WRAP_CONTENT)

        // Show the popup window at the bottom right of the TextView

        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.showAsDropDown(anchorView, anchorView.width, 0)
    }

    private fun settingBackgroundTaskToBedroom(){
        binding.tvAnyBedrooms.setOnClickListener {
            binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv1Bathrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
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
        }

    }

    private fun settingBackgroundTaskToBathroom(){

        binding.tvAnyBedrooms.setOnClickListener {
            binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv1Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
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
        }
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
        }

        binding.tv250.setOnClickListener {
            binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv250.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv350.setOnClickListener {
            binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv350.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }


        binding.tv450.setOnClickListener {
            binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv450.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }
        binding.tv550.setOnClickListener {
            binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv550.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv650.setOnClickListener {
            binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv650.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }
        binding.tv750.setOnClickListener {
            binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv750.setBackgroundResource(R.drawable.bg_inner_select_white)
        }
    }

    private fun byDefaultSelectAvailability()
    {
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
        }


        binding.tv1.setOnClickListener {
            binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv2.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }
        binding.tv2.setOnClickListener {
            binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv3.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv3.setOnClickListener {
            binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv5.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv5.setOnClickListener {
            binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv7.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv7.setOnClickListener {
            binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7.setBackgroundResource(R.drawable.bg_inner_select_white)
        }
    }

    fun settingDataToActivityModel(){
        activityList = mutableListOf<ActivityModel>()
        amenitiesList = mutableListOf()

        amenitiesList.add("Wifi")
        amenitiesList.add("Kitchen")
        amenitiesList.add("Washer")
        amenitiesList.add("Dryer")
        amenitiesList.add("Air conditioning")
        amenitiesList.add("Heating")
        amenitiesList.add("Wifi")
        amenitiesList.add("Kitchen")
        amenitiesList.add("Washer")
        amenitiesList.add("Dryer")
        amenitiesList.add("Air conditioning")
        amenitiesList.add("Heating")



        var model1 = ActivityModel()
        model1.name = "Stays"
        model1.image = R.drawable.ic_stays
        activityList.add(model1)

        var model2 = ActivityModel()
        model2.name = "Event Space"
        model2.image = R.drawable.ic_event_space
        activityList.add(model2)

        var model3 = ActivityModel()
        model3.name = "Photo shoot"
        model3.image = R.drawable.ic_photo_shoot
        activityList.add(model3)

        var model4 = ActivityModel()
        model4.name = "Meeting"
        model4.image = R.drawable.ic_meeting
        activityList.add(model4)



        var model5 = ActivityModel()
        model5.name = "Party"
        model5.image = R.drawable.ic_party
        activityList.add(model5)


        var model6 = ActivityModel()
        model6.name = "Film Shoot"
        model6.image = R.drawable.ic_film_shoot
        activityList.add(model6)

        var model7 = ActivityModel()
        model7.name = "Performance"
        model7.image = R.drawable.ic_performance
        activityList.add(model7)

        var model8 = ActivityModel()
        model8.name = "Workshop"
        model8.image = R.drawable.ic_workshop
        activityList.add(model8)

        var model9 = ActivityModel()
        model9.name = "Corporate Event"
        model9.image = R.drawable.ic_corporate_event
        activityList.add(model9)

        var model10 = ActivityModel()
        model10.name = "Wedding"
        model10.image = R.drawable.ic_weding
        activityList.add(model10)

        var model11 = ActivityModel()
        model11.name = "Dinner"
        model11.image = R.drawable.ic_dinner
        activityList.add(model11)

        var model12 = ActivityModel()
        model12.name = "Retreat"
        model12.image = R.drawable.ic_retreat
        activityList.add(model12)


        var model13 = ActivityModel()
        model13.name = "Pop-up"
        model13.image = R.drawable.ic_popup_people
        activityList.add(model13)

        var model14 = ActivityModel()
        model14.name = "Networking"
        model14.image = R.drawable.ic_networking
        activityList.add(model14)

        var model15 = ActivityModel()
        model15.name = "Fitness Class"
        model15.image = R.drawable.ic_fitness_class
        activityList.add(model15)

        var model16 = ActivityModel()
        model16.name = "Audio Recording"
        model16.image = R.drawable.ic_audio_recording
        activityList.add(model16)

    }

    fun setUpRecyclerView() {
        galleryAdapter = GallaryAdapter(imageList)
        binding.recyclerGallery.layoutManager = GridLayoutManager(requireContext(),3)
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
        amenitiesAdapter.updateAdapter(amenitiesList)
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
      when(text){
          "add On"->{
              showAddOnDialog()

          }


          "add On Cross" -> {
              if (obj == addOnList.size - 1) {
                //  dialogSelectLanguage()
              } else {
                  addOnList.removeAt(obj)
                  addOnAdapter.updateAddOn(addOnList)

              }}

    }}
    fun getItemList(): List<String> {
        return listOf(
            "Computer Screen",
            "Studio Lights",
            "Projectors",
            "Speakers",
            "Microphones",
            "Sounds Systems",
            "DJ Equipment",
            "Tables",
            "Chairs",
            "Stage Platforms",
            "Art Supplies (Paint, brushes)",
            "Allow Alcohol",
            "Onsite Food Prep (Event)",
            "Extra Person above Max Capacity",
            "Photographer (Per Hour)",
            "Videographer (Per Hour)"
        )
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

//            viewModel.list.observe(viewLifecycleOwner) {
//                dialogAdapter.updateItem(it)
//            }
            // Handle Submit Button
            btnSubmit.setOnClickListener {
                val itemName = etItemName.text.toString()
                val itemPrice = etItemPrice.text.toString()

                if (itemName.isNotEmpty() && itemPrice.isNotEmpty()) {
//                    tvItemName.text = itemName
//                    tvItemPrice.text = itemPrice
                    val newAddOn = AddOnModel(itemName, itemPrice)
                    addOnList.add(0, newAddOn)

                    // Notify adapter about changes
                    addOnAdapter.updateAddOn(addOnList) // Custom method in adapter
                    addOnAdapter.notifyItemInserted(0)
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Please enter valid details", Toast.LENGTH_SHORT).show()
                }
            }


            // findViewById<TextView>(R.id.text).text = text


            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // Ensure dialog dismisses when touched outside
       //     window?.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND)

//            val rootView = findViewById<View>(android.R.id.content)
//            rootView.setOnTouchListener { _, _ ->
//                // Dismiss the dialog when touched outside
//                dialog.dismiss()
//                true
//            }

            show()
        }




    }


     fun settingBackgroundAllMonth() {
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
        }


        binding.tvJan.setOnClickListener {

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

    fun settingBackgroundAllWeek(){
        binding.tvAllDays.setOnClickListener {
            binding.tvAllDays.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tvOnlyWorkingDays.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvOnlyWeekends.setBackgroundResource(R.drawable.bg_outer_manage_place)


        }


        binding.tvOnlyWorkingDays.setOnClickListener {

            binding.tvAllDays.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvOnlyWorkingDays.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tvOnlyWeekends.setBackgroundResource(R.drawable.bg_outer_manage_place)

        }
        binding.tvOnlyWeekends.setOnClickListener {
            binding.tvAllDays.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvOnlyWorkingDays.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvOnlyWeekends.setBackgroundResource(R.drawable.bg_inner_select_white)

        }


    }



    private fun getItemListForRadioHoursText(): MutableList<ItemRadio> {
        var items =   mutableListOf(
            ItemRadio("1 hour minimum", false),
            ItemRadio("2 hour minimum", false),
            ItemRadio("3 hour minimum", false),
            ItemRadio("4 hour minimum", false),
            ItemRadio("5 hour minimum", false),
            ItemRadio("6 hour minimum", false),
            ItemRadio("7 hour minimum", false),
            ItemRadio("8 hour minimum", false),
            ItemRadio("9 hour minimum", false),
            ItemRadio("10 hour minimum", false),
            ItemRadio("11 hour minimum", false),
            ItemRadio("12 hour minimum", false),
            ItemRadio("13 hour minimum", false),
            ItemRadio("14 hour minimum", false),
            ItemRadio("15 hour minimum", false),
            ItemRadio("16 hour minimum", false),
            ItemRadio("17 hour minimum", false),
            ItemRadio("18 hour minimum", false),
            ItemRadio("19 hour minimum", false),
            ItemRadio("20 hour minimum", false),
            ItemRadio("21 hour minimum", false),
            ItemRadio("22 hour minimum", false),
            ItemRadio("23 hour minimum", false),
            ItemRadio("24 hour minimum", false)
        )

        // Restore the previously selected item's state
        if (previouslySelectedIndex != -1 && previouslySelectedIndex < items.size) {
            items[previouslySelectedIndex].isSelected = true
        }
        return items
    }


    private fun getItemListForRadioPerHoursRuppesText(): MutableList<ItemRadio> {
        var items =   mutableListOf(
            ItemRadio("$10 per hour", false),
            ItemRadio("20 per hour", false),
            ItemRadio("30 per hour", false),
            ItemRadio("40 per hour", false),
            ItemRadio("50 per hour", false),
            ItemRadio("60 per hour", false),
            ItemRadio("70 per hour", false),
            ItemRadio("80 per hour", false),
            ItemRadio("90 per hour", false),
            ItemRadio("100 per hour", false),
            ItemRadio("110 per hour", false),
            ItemRadio("120 per hour", false),
            ItemRadio("130 per hour", false),
            ItemRadio("14 hour minimum", false),
            ItemRadio("15 hour minimum", false),
            ItemRadio("16 hour minimum", false),
            ItemRadio("17 hour minimum", false),
            ItemRadio("18 hour minimum", false),
            ItemRadio("19 hour minimum", false),
            ItemRadio("20 hour minimum", false),
            ItemRadio("21 hour minimum", false),
            ItemRadio("22 hour minimum", false),
            ItemRadio("23 hour minimum", false),
            ItemRadio("24 hour minimum", false)
        )

        // Restore the previously selected item's state
        if (previouslySelectedIndex != -1 && previouslySelectedIndex < items.size) {
            items[previouslySelectedIndex].isSelected = true
        }
        return items
    }
    private fun getItemListForRadioPerHoursBulkText(): MutableList<ItemRadio> {
        var items =   mutableListOf(
            ItemRadio("1 hours", false),
            ItemRadio("2 hours", false),
            ItemRadio("3 hours", false),
            ItemRadio("4 hours", false),
            ItemRadio("5 hours", false),
            ItemRadio("6 hours", false),
            ItemRadio("7 hours", false),
            ItemRadio("8 hours", false),
            ItemRadio("9 hours", false),
            ItemRadio("10 hours", false),
            ItemRadio("11 hours", false),
            ItemRadio("12 hours", false),
            ItemRadio("13 hours", false),
            ItemRadio("14 hours", false),
            ItemRadio("15 hours", false),
            ItemRadio("16 hours", false),
            ItemRadio("17 hours", false),
            ItemRadio("18 hours", false),
            ItemRadio("19 hours", false),
            ItemRadio("20 hours", false),
            ItemRadio("21 hours", false),
            ItemRadio("22 hours", false),
            ItemRadio("23 hours", false),
            ItemRadio("24 hours", false)

        )

        // Restore the previously selected item's state
        if (previouslySelectedIndex != -1 && previouslySelectedIndex < items.size) {
            items[previouslySelectedIndex].isSelected = true
        }
        return items
    }
 private fun getItemListForRadioPerHoursDiscountText(): MutableList<ItemRadio> {
        var items =   mutableListOf(
            ItemRadio("5% Discount", false),
            ItemRadio("10% Discount", false),
            ItemRadio("15% Discount", false),
            ItemRadio("20% Discount", false),
            ItemRadio("25% Discount", false),
            ItemRadio("30% Discount", false),
            ItemRadio("35% Discount", false),
            ItemRadio("40% Discount", false),
            ItemRadio("45% Discount", false),
            ItemRadio("50% Discount", false),

            ItemRadio("75% Discount", false)

        )

        // Restore the previously selected item's state
        if (previouslySelectedIndex != -1 && previouslySelectedIndex < items.size) {
            items[previouslySelectedIndex].isSelected = true
        }
        return items
    }







    fun onClickDialogOpenner(){
        binding.llHours.setOnClickListener {
                    val items = getItemListForRadioHoursText()

            showSelectedDialog(requireContext(), items,binding.tvHoursSelect)
        }

        binding.llHoursRupees.setOnClickListener {
            val items = getItemListForRadioPerHoursRuppesText()

            showSelectedDialog(requireContext(), items,binding.tvHoursRupeesSelect)

        }
        binding.llHoursBulk.setOnClickListener {
            val items = getItemListForRadioPerHoursBulkText()

            showSelectedDialog(requireContext(), items,binding.tvHoursBulkSelect)

        }


        binding.llDiscount.setOnClickListener {
            val items = getItemListForRadioPerHoursDiscountText()

            showSelectedDialog(requireContext(), items,binding.tvDiscountSelect)

        }
        binding.llAvailabilityFromHours.setOnClickListener {
            DateManager(requireContext()).showTimePickerDialog(requireContext()) { selectedHour ->
                binding.tvHours.setText(selectedHour.toString())
            }


        }
        binding.llAvailabilityEndHours.setOnClickListener {
            DateManager(requireContext()).showTimePickerDialog(requireContext()) { selectedHour ->
                binding.tvHours1.setText(selectedHour.toString())
            }


        }


    }






    fun showSelectedDialog(context: Context, items: MutableList<ItemRadio>, text: TextView) {
        val dialog = Dialog(context, R.style.BottomSheetDialog)
        dialog.apply {
            setCancelable(false)

            setContentView(R.layout.dialog_for_select_radio_text)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }

            val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)


            recyclerView.layoutManager = LinearLayoutManager(context)
            val adapter = RadioTextAdapter(items, object : OnClickListener {
                override fun itemClick(selectedIndex: Int) {


                    previouslySelectedIndex = selectedIndex
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