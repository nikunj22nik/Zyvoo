package com.yesitlab.zyvo.fragment.guest

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.yesitlab.zyvo.AppConstant
import com.yesitlab.zyvo.OnClickListener
import com.yesitlab.zyvo.OnClickListener1
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.activity.GuesMain
import com.yesitlab.zyvo.activity.guest.ExtraTimeChargesActivity
import com.yesitlab.zyvo.activity.guest.FiltersActivity
import com.yesitlab.zyvo.activity.guest.RestaurantDetailActivity
import com.yesitlab.zyvo.activity.guest.WhereTimeActivity
import com.yesitlab.zyvo.adapter.LoggedScreenAdapter
import com.yesitlab.zyvo.adapter.WishlistAdapter
import com.yesitlab.zyvo.databinding.DialogAddWishlistBinding
import com.yesitlab.zyvo.databinding.FragmentGuestDiscoverBinding
import com.yesitlab.zyvo.model.Location
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
    private lateinit var mapView: MapView

    private var commonAuthWorkUtils: CommonAuthWorkUtils? = null
    private  val viewModel : WishlistViewModel by viewModels()

    private lateinit var googleMap: GoogleMap

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
        mapView = binding.map
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
        binding.customProgressBar.setOnClickListener(this)

        loggedScreenViewModel.imageList.observe(viewLifecycleOwner, Observer {
            images -> adapter.updateData(images)
        })

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        adapterClickListnerTask()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val locations = listOf(
            Location(37.7749, -122.4194, "San Francisco"),
            Location(34.0522, -118.2437, "Los Angeles"),
            Location(40.7128, -74.0060, "New York")
        )




        binding.customProgressBar.setProgressWidth(15f)
       // binding.customProgressBar.setMax(100.0) // Set max progress as 100%
        binding.customProgressBar.setMax(100.0)
       // binding.customProgressBar.setProgressWithAnimation(75.0)


//        binding.customProgressBar.setThumb(ContextCompat.getDrawable(requireContext(), R.drawable.ic_progress_runnable_image)!!)
//        binding.customProgressBar.setOnProgressChangeListener { progress ->
//            Log.d("Progress", "Current progress: $progress")
//        }
        // Load the image and set it

//        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_progress_runnable_image)
//        if (bitmap == null) {
//            Log.e("SemiCircleProgressBar", "Bitmap not loaded. Check resource ID.")
//        }
//        binding.customProgressBar.setImageBitmap(bitmap)


        // Start the countdown timer
        startCountdown()
    }
    private fun startCountdown() {
        // Countdown timer for 20 seconds with 1-second intervals
        object : CountDownTimer(totalDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt()

                // Update the TextView for seconds
             //   binding.llSecs.findViewById<TextView>(R.id.textSecs).text = secondsRemaining.toString()

                // Calculate progress percentage and update progress bar
                val progress = ((totalDuration - millisUntilFinished).toDouble() / totalDuration) * 100
                binding.customProgressBar.setProgress(progress)
            }

            override fun onFinish() {
                // Set the final state when countdown finishes
             //   binding.llSecs.findViewById<TextView>(R.id.textSecs).text = "0"
                binding.customProgressBar.setProgress(100.0)
            }
        }.start()
    }
    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.textWhere ->{
                var intent = Intent(requireContext(),WhereTimeActivity::class.java)
                intent.putExtra(AppConstant.WHERE, AppConstant.WHERE)
                startActivity(intent)
            }
            R.id.textTime ->{
                var intent = Intent(requireContext(),WhereTimeActivity::class.java)
                intent.putExtra(AppConstant.TIME, AppConstant.TIME)
                startActivity(intent)
            }
            R.id.textActivity ->{
                var intent = Intent(requireContext(),WhereTimeActivity::class.java)
                intent.putExtra(AppConstant.ACTIVITY, AppConstant.ACTIVITY)
                startActivity(intent)
            }
            R.id.rl_show_map ->{
                if(binding.recyclerViewBooking.visibility == View.VISIBLE){
                    binding.tvMapContent.setText("Show List")
                    binding.rlMapView.visibility = View.VISIBLE
                    binding.recyclerViewBooking.visibility = View.GONE
                    binding.clTimeLeftProgressBar.visibility = View.GONE
                }
                else{
                    binding.tvMapContent.setText("Show Map")

                    binding.rlMapView.visibility = View.GONE
                    binding.recyclerViewBooking.visibility = View.VISIBLE
                    binding.clTimeLeftProgressBar.visibility = View.VISIBLE

                }
            }

            R.id.customProgressBar ->{
                var intent = Intent(requireContext(),ExtraTimeChargesActivity::class.java)
                intent.putExtra(AppConstant.TIME, AppConstant.TIME)
                startActivity(intent)
            }

        }
    }


//    private fun toggleMapVisibility() {
//        val mapFragment = childFragmentManager.findFragmentById(R.id.map)
//        if (mapFragment != null) {
//            if (isMapVisible) {
//                mapFragment.view?.visibility = View.GONE
//                Log.d("TESTING_ZYVOO","Inside Map Gone")
//            } else {
//                mapFragment.view?.visibility = View.VISIBLE
//            }
//            isMapVisible = !isMapVisible
//        }
//    }


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

    override fun onMapReady(mp: GoogleMap) {

        googleMap = mp
        // Example coordinates
        val locations = listOf(
            Location(37.7749, -122.4194, " $13/h"),
            Location(34.0522, -118.2437, " $15/h"),
            Location(40.7128, -74.0060, " $19/h"),
            Location(51.5074, -0.1278, " $23/h"),
            Location(48.8566, 2.3522, " $67/h")
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

    }

    fun createCustomMarker(context: Context, text: String): Bitmap {
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
//        val nestedFragment = childFragmentManager.findFragmentById(R.id.map)
//
//        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
//        if (nestedFragment != null) {
//            transaction.remove(nestedFragment) // Remove the nested fragment
//            transaction.commit() // Commit the transaction to make changes
//        }
    }
/*
    private fun showWishlistDialog() {
        //  private  val viewModel : WishlistViewModel by viewModels()

        val dialogBinding = DialogAddWishlistBinding.inflate(LayoutInflater.from(requireContext()))
        //DialogWishlistBinding

        // Set up the RecyclerView for the dialog


        // Create and show the dialog
        val dialog = AlertDialog.Builder(context)
            .setView(dialogBinding.root)
            .create()

        dialog.show()

        // Optional: Set dialog size for consistency
//        dialog.window?.setLayout(
//            (resources.displayMetrics.widthPixels * 0.9).toInt(),
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )

        // Close button logic
        dialogBinding.imageCross.setOnClickListener {
            dialog.dismiss()
        }
    }

 */



   private fun showAddWishlistDialog() {
        val dialogAdapter = WishlistAdapter(requireContext(),true, mutableListOf(), null)

        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_add_wishlist)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }

//            // Retrieve NavController using NavHostFragment
//            val navHostFragment = (context as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.fragmentAuthContainerView) as NavHostFragment
//            val navController = navHostFragment.navController

val rvWishList : RecyclerView =  findViewById<RecyclerView>(R.id.rvWishList)

            rvWishList.adapter = dialogAdapter

            viewModel.list.observe(viewLifecycleOwner) {
                dialogAdapter.updateItem(it)
            }

            findViewById<ImageView>(R.id.imageCross).setOnClickListener {
                dismiss()
            }
      findViewById<TextView>(R.id.textCreateWishList).setOnClickListener {
          createWishListDialog()
                dismiss()
            }

           // findViewById<TextView>(R.id.text).text = text



            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun createWishListDialog(){

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


            findViewById<ImageView>(R.id.imageCross).setOnClickListener {
                dismiss()
            }
            findViewById<TextView>(R.id.textCreate).setOnClickListener {
                dismiss()
            }
            findViewById<TextView>(R.id.textClear).setOnClickListener {
                dismiss()
            }
         val etDescription =    findViewById<EditText>(R.id.etDescription)

         val tvMaxCount =    findViewById<TextView>(R.id.textMaxCount)
            setupCharacterCountListener(etDescription, tvMaxCount, 50)
            // findViewById<TextView>(R.id.text).text = text



            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
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
                showAddWishlistDialog()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        (activity as? GuesMain)?.discoverResume()
    }

}