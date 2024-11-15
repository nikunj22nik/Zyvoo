package com.yesitlab.zyvo.fragment.guest

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.yesitlab.zyvo.OnClickListener
import com.yesitlab.zyvo.OnClickListener1
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.activity.guest.FiltersActivity
import com.yesitlab.zyvo.activity.guest.RestaurantDetailActivity
import com.yesitlab.zyvo.activity.guest.WhereTimeActivity
import com.yesitlab.zyvo.adapter.LoggedScreenAdapter
import com.yesitlab.zyvo.adapter.WishlistAdapter
import com.yesitlab.zyvo.databinding.DialogAddWishlistBinding
import com.yesitlab.zyvo.databinding.FragmentGuestDiscoverBinding
import com.yesitlab.zyvo.utils.CommonAuthWorkUtils
import com.yesitlab.zyvo.viewmodel.ImagePopViewModel
import com.yesitlab.zyvo.viewmodel.WishlistViewModel
import com.yesitlab.zyvo.viewmodel.guest.GuestDiscoverViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GuestDiscoverFragment : Fragment(),View.OnClickListener,OnClickListener, OnMapReadyCallback,
    OnClickListener1 {

    lateinit var binding :FragmentGuestDiscoverBinding ;

    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private val totalDuration = 20000L
    private lateinit var adapter: LoggedScreenAdapter

    private var commonAuthWorkUtils: CommonAuthWorkUtils? = null
    private  val viewModel : WishlistViewModel by viewModels()

    private lateinit var map: GoogleMap

    private var isMapVisible = false

    private val loggedScreenViewModel: GuestDiscoverViewModel by lazy {
        ViewModelProvider(this)[GuestDiscoverViewModel::class.java]
    }

    private val imagePopViewModel: ImagePopViewModel by lazy {
        ViewModelProvider(this)[ImagePopViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {

        binding = FragmentGuestDiscoverBinding.inflate(LayoutInflater.from(requireContext()))
        val navController = findNavController()
        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                // Handle the result
            }
        }
        commonAuthWorkUtils = CommonAuthWorkUtils(requireActivity(),navController)

        adapter = LoggedScreenAdapter(requireContext(), mutableListOf(),
            this, viewLifecycleOwner, imagePopViewModel,this)

        setRetainInstance(true);

        binding.recyclerViewBooking.adapter = adapter

        binding.filterIcon.setOnClickListener {
            var intent = Intent(requireContext(),FiltersActivity::class.java)
            startForResult.launch(intent)
        }

        binding.textWhere.setOnClickListener(this)
        binding.textTime.setOnClickListener(this)
        binding.textActivity.setOnClickListener(this)
        binding.rlShowMap.setOnClickListener(this)

        loggedScreenViewModel.imageList.observe(viewLifecycleOwner, Observer {
            images -> adapter.updateData(images)
        })

        adapterClickListnerTask()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment

            if (mapFragment == null) {
                Log.d("ZYVOO_TESTING","")
               childFragmentManager.beginTransaction().replace(R.id.map, SupportMapFragment.newInstance()).commit()
              //  childFragmentManager.beginTransaction().replace(R.id.map, mapFragment).commit()
            }else{
                childFragmentManager.beginTransaction().replace(R.id.map, mapFragment).commit()

            }


        }


        binding.customProgressBar.setProgressWidth(13f)
        binding.customProgressBar.setMax(100.0) // Set max progress as 100%

        // Start the countdown timer
        startCountdown()
    }
    private fun startCountdown() {
        // Countdown timer for 20 seconds with 1-second intervals
        object : CountDownTimer(totalDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt()

                // Update the TextView for seconds
                binding.llSecs.findViewById<TextView>(R.id.textSecs).text = secondsRemaining.toString()

                // Calculate progress percentage and update progress bar
                val progress = ((totalDuration - millisUntilFinished).toDouble() / totalDuration) * 100
                binding.customProgressBar.setProgress(progress)
            }

            override fun onFinish() {
                // Set the final state when countdown finishes
                binding.llSecs.findViewById<TextView>(R.id.textSecs).text = "0"
                binding.customProgressBar.setProgress(100.0)
            }
        }.start()
    }
    override fun onClick(p0: View?) {
        when(p0?.id){

            R.id.textWhere ->{
                var intent = Intent(requireContext(),WhereTimeActivity::class.java)
                startActivity(intent)
            }
            R.id.textTime ->{
                var intent = Intent(requireContext(),WhereTimeActivity::class.java)
                startActivity(intent)
            }
            R.id.textActivity ->{
                var intent = Intent(requireContext(),WhereTimeActivity::class.java)
                startActivity(intent)
            }
            R.id.rl_show_map ->{
                if(binding.recyclerViewBooking.visibility == View.VISIBLE){
                    binding.tvMapContent.setText("Show List")
                    binding.rlMapView.visibility = View.VISIBLE
                    binding.recyclerViewBooking.visibility = View.GONE
                }
                else{
                    binding.tvMapContent.setText("Show Map")
                    toggleMapVisibility()
                    binding.rlMapView.visibility = View.GONE
                    binding.recyclerViewBooking.visibility = View.VISIBLE
                }
            }
        }
    }


    private fun toggleMapVisibility() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map)
        if (mapFragment != null) {
            if (isMapVisible) {
                mapFragment.view?.visibility = View.GONE
                Log.d("TESTING_ZYVOO","Inside Map Gone")
            } else {
                mapFragment.view?.visibility = View.VISIBLE


            }
            isMapVisible = !isMapVisible
        }
    }


    fun adapterClickListnerTask(){
        Log.d("TESTING_ZYVOO","I AM HERE IN AdapterClickListener Task")

        adapter.setOnItemClickListener(object : LoggedScreenAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                Log.d("TESTING_ZYVOO","I AM HERE IN DEVELOPMENT")
                var intent = Intent(requireContext(),RestaurantDetailActivity::class.java)
                startActivity(intent)
            }

        })
    }

    override fun itemClick(obj: Int) {}

    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap

        // Example coordinates

        val location = LatLng(37.7749, -122.4194)
        val location1 = LatLng(37.7740, -122.4200)
        val location2 = LatLng(37.7730, -122.4190)
        val location3 = LatLng(37.7750, -122.4200)

        for(i in 1..4){
            when(i){
                1->{
                    map.addMarker(MarkerOptions().position(location))
                }
                2->{
                    map.addMarker(MarkerOptions().position(location1))
                }
                3->{
                    map.addMarker(MarkerOptions().position(location2))
                }
                4->{
                    map.addMarker(MarkerOptions().position(location3))
                }
            }
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15F));

        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                return null
            }
            override fun getInfoContents(marker: Marker): View {
                val infoView = layoutInflater.inflate(R.layout.custom_info_window, null)
                return infoView
            }
        })

    }




    override fun onDestroyView() {
        super.onDestroyView()
//        val nestedFragment = childFragmentManager.findFragmentById(R.id.map)
//
//        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
//        if (nestedFragment != null) {
//            transaction.remove(nestedFragment) // Remove the nested fragment
//            transaction.commit() // Commit the transaction to make changes
//        }
    }

    private fun showWishlistDialog() {
        //  private  val viewModel : WishlistViewModel by viewModels()

        val dialogBinding = DialogAddWishlistBinding.inflate(LayoutInflater.from(requireContext()))
        //DialogWishlistBinding

        // Set up the RecyclerView for the dialog
        val dialogAdapter = WishlistAdapter(requireContext(),true, mutableListOf())
        dialogBinding.rvWishList.adapter = dialogAdapter

        viewModel.list.observe(viewLifecycleOwner) {
            dialogAdapter.updateItem(it)
        }

        // Create and show the dialog
        val dialog = AlertDialog.Builder(context)
            .setView(dialogBinding.root)
            .create()

        dialog.show()

        // Optional: Set dialog size for consistency
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Close button logic
        dialogBinding.imageCross.setOnClickListener {
            dialog.dismiss()
        }
    }

    override fun itemClick(obj: Int, text: String) {
        when(text){
            "Add Wish"->{
                showWishlistDialog()
            }
        }
    }

}