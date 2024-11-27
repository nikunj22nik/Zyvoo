package com.yesitlab.zyvo.fragment.host

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.yesitlab.zyvo.AppConstant
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.adapter.guest.ActivitiesAdapter
import com.yesitlab.zyvo.adapter.guest.AmenitiesAdapter
import com.yesitlab.zyvo.adapter.host.GallaryAdapter
import com.yesitlab.zyvo.databinding.FragmentManagePlaceBinding
import com.yesitlab.zyvo.model.ActivityModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ManagePlaceFragment : Fragment() , OnMapReadyCallback {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentManagePlaceBinding.inflate(inflater,container,false)
        settingDataToActivityModel()
        initialization()
        setUpRecyclerView()
        mapInitialization(savedInstanceState)
        imagePermissionInitialization()
        return binding.root
    }

    private fun imagePermissionInitialization(){

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    openGallery()
                } else {
                    Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }


        galleryAdapter.setOnItemClickListener(object :GallaryAdapter.onItemClickListener{
            override fun onItemClick(position: Int, type: String) {

                if(type.equals(AppConstant.DELETE)){
                    imageList.removeAt(position)
                    galleryAdapter.updateAdapter(imageList)
                }else{
                    if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        openGallery()
                    } else {
                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }

            }
        })

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
        }

        binding.tvGallery.setOnClickListener {
            binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvGallery.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tvAvailability.setBackgroundResource(R.drawable.bg_outer_manage_place)

            binding.llGalleryLocation.visibility = View.VISIBLE
            binding.llHomeSetup.visibility = View.GONE
        }

        binding.tvAvailability.setOnClickListener {
            binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvGallery.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvAvailability.setBackgroundResource(R.drawable.bg_inner_select_white)
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
        mapView.onPause()  // Important to call in onPause
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()  // Important to call in onDestroy

    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()  // Important to call in onLowMemory
    }

}