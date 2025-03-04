package com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.business.zyvo.R
import com.business.zyvo.adapter.AdapterAddOn
import com.business.zyvo.adapter.BookingIncludeAdapter
import com.business.zyvo.adapter.guest.AdapterReview
import com.business.zyvo.databinding.FragmentReviewBookingBinding
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.Review
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.NetworkMonitorCheck
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ReviewBookingFragment : Fragment() , OnMapReadyCallback {
    private var _binding : FragmentReviewBookingBinding? = null
    private  val binding get() = _binding!!
    private var bookingId = 0
    private lateinit var adapterAddon: AdapterAddOn
    private lateinit var adapterReview: AdapterReview
    private lateinit var adapterInclude: BookingIncludeAdapter
    private lateinit var mapView: MapView
    private var mMap: GoogleMap? = null
    lateinit var  navController: NavController
    private lateinit var sessionManager: SessionManager
    private var reviewList : MutableList<Review> = mutableListOf()
    private val bookingViewModel: BookingViewModel by viewModels()
    var latitude = 0.0
    var longitude = 0.0
    var propertyId = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bookingId = it.getInt("BOOKING_ID")
        }
        sessionManager = SessionManager(requireContext())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_review_booking,container,false)

        binding.textReportIssueButton.visibility = View.GONE
        binding.textUserName.visibility = View.VISIBLE
        binding.rlNeedHelp.visibility = View.VISIBLE
        binding.imageStar1.visibility = View.GONE
        binding.textRatingStar1.visibility = View.GONE

        getBookingDetailsListAPI()

        return binding.root
    }


    override fun onStart() {
        super.onStart()
        binding.textUserName.visibility = View.VISIBLE
        binding.rlNeedHelp.visibility = View.VISIBLE
        binding.imageStar1.visibility = View.GONE
        binding.textRatingStar1.visibility = View.GONE
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        initialization()

        navController = Navigation.findNavController(view)
        binding.imageBackIcon.setOnClickListener {
            navController.navigateUp()
        }


        binding.textReviewBookingButton.setOnClickListener {
            dialogReview()
        }

        binding.textMessageTheHostButton.setOnClickListener {
            findNavController().navigate(R.id.chatFragment)
        }

        binding.imageShare.setOnClickListener {
            shareApp()
        }
        binding.textReviewClick.setOnClickListener {
            showPopupWindow(it,0)
        }
        showingMoreText()


        binding.rlParking.setOnClickListener {
            if(binding.tvParkingRule.visibility == View.VISIBLE){
                binding.tvParkingRule.visibility= View.GONE
            }else{
                binding.tvParkingRule.visibility = View.VISIBLE
            }
        }

        binding.rlHostRules.setOnClickListener {
            if (binding.tvHostRule.visibility == View.VISIBLE) {
                binding.tvHostRule.visibility = View.GONE
            } else {
                binding.tvHostRule.visibility = View.VISIBLE
            }
        }
    }

    private fun getBookingDetailsListAPI() {
        if (!NetworkMonitorCheck._isConnected.value) {
            showErrorDialog(requireContext(), resources.getString(R.string.no_internet_dialog_msg))
            return
        }

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                bookingViewModel.getBookingDetailsList(sessionManager.getUserId().toString(), bookingId).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            val list = it.data
                            Log.d("DATACHECK","list : $list")

                            if (list!=null) { // Check if list is not null or empty
                                val data = list.data

                                binding.textUserName.text = data.host_name ?: "N/A"
                                binding.tvNamePlace.text = data.property_name ?: "N/A"
                                binding.tvStatus.text = data.status
                                binding.textMiles.text = (data.distance_miles ?: "N/A").toString()
                                binding.textRatingStar.text = data.total_rating ?: "0"
                                binding.time.text = data.charges?.booking_hours.toString()
                                binding.money.text = data.charges?.hourly_rate ?: "0.00"
                                binding.tvCleaningFee.text = data.charges?.cleaning_fee ?: "0.00"
                                binding.tvServiceFee.text = data.charges?.zyvo_service_fee ?: "0.00"
                                binding.tvTaxes.text = data.charges?.taxes ?: "0.00"
                                binding.tvAddOn.text = data.charges?.add_on?.toString() ?: "N/A"
                                binding.tvTotalPrice.text = data.charges?.total ?: "0.00"
                                binding.tvBookingDate.text = data.booking_detail?.date ?: "N/A"
                                binding.bookingFromTo.text = data.booking_detail?.start_end_time ?: "N/A"
                                binding.tvBookingTotalTime.text = data.booking_detail?.time ?: "N/A"
                                binding.tvParkingContent.text = (data.parking_rules ?: "N/A").toString()
                                binding.tvHostContent.text = (data.host_rules ?: "N/A").toString()
                                binding.tvLocationName.text = data.location ?: "N/A"
                                propertyId = data.property_id ?: 0

                                //image loading from glide
                                Glide.with(requireContext())
                                    .load(AppConstant.BASE_URL + data.host_profile_image)
                                    .error(R.drawable.ic_circular_img_user)
                                    .into(binding.imageProfilePicture)

                                Glide.with(requireContext())
                                    .load(AppConstant.BASE_URL+ data.first_property_image)
                                    .error(R.drawable.image_hotel)
                                    .into(binding.imgProfileHotel)

                                Glide.with(requireContext())
                                    .load(AppConstant.BASE_URL+ data.property_images)
                                    .error(R.drawable.ic_dummy_img_1)
                                    .into(binding.shapeableImageView11)

                                // Safe parsing of latitude & longitude
                                latitude = data.latitude?.toDoubleOrNull() ?: 0.0
                                longitude = data.longitude.toDoubleOrNull() ?: 0.0

                                // Update reviews dynamically
                                adapterReview.updateAdapter(data.reviews ?: mutableListOf())
                                data.activities?.let { it1 -> adapterInclude.updateItems(it1) }
                                data.amenities?.let { it1 -> adapterAddon.updateAdapter(it1) }
                                reviewList = data.reviews!!
                            } else {
                                Log.e("API_ERROR", "Empty or null data received")
                                showErrorDialog(requireContext(), "No booking details found.")
                            }
                        }
                        is NetworkResult.Error -> {
                            Log.e("API_ERROR", "Server Error: ${it.message}")
                            showErrorDialog(requireContext(), it.message ?: "Unknown error")
                        }
                        else -> {
                            Log.v("API_ERROR", "Unexpected error: ${it.message ?: "Unknown error"}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("API_EXCEPTION", "Unexpected API error", e)
                showErrorDialog(requireContext(), "Something went wrong. Please try again.")
            }
        }
    }

    private fun reviewPublishAPI(responseRate: Float, communication: Float, onTime: Float, etMessage: String) {
        if (!NetworkMonitorCheck._isConnected.value) {
            showErrorDialog(requireContext(), getString(R.string.no_internet_dialog_msg))
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                bookingViewModel.getReviewPublishAPI(
                    sessionManager.getUserId()!!,
                    bookingId, propertyId,responseRate.toInt(),
                    communication.toInt(), onTime.toInt(),
                    etMessage
                ).collect { result ->
                    withContext(Dispatchers.Main) {
                        when (result) {
                            is NetworkResult.Success -> {
                                Log.d("TAG", "reviewPublishAPI: ${result.data} ")
                                Toast.makeText(requireContext(), "Thanks for your review", Toast.LENGTH_SHORT).show()
                            }
                            is NetworkResult.Error -> {
                                Log.e("API_ERROR", "Server Error: ${result.message}")
                                showErrorDialog(requireContext(), result.message ?: "Unknown error")
                            }
                            else -> {
                                Log.v("API_ERROR", "Unexpected error: ${result.message ?: "Unknown error"}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("API_EXCEPTION", "Unexpected API error", e)
                    showErrorDialog(requireContext(), "Something went wrong. Please try again.")
                }
            }
        }
    }



    private fun showingMoreText() {
        val text = "Show More"
        val spannableString = SpannableString(text).apply {
            setSpan(UnderlineSpan(), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.tvShowMore.text = spannableString
        binding.tvShowMore.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        binding.tvShowMore.paint.isAntiAlias = true
        binding.tvShowMore.setOnClickListener {
            showingLessText()
        }

    }

    private fun showingLessText() {
        val text = "Show Less"
        val spannableString = SpannableString(text).apply {
            setSpan(UnderlineSpan(), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.tvShowMore.text = spannableString
        binding.tvShowMore.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        binding.tvShowMore.paint.isAntiAlias = true
        binding.tvShowMore.setOnClickListener {

//            adapterAddon.updateAdapter(getAddOnList().subList(0, 4))
            showingMoreText()
        }
    }

    private fun showPopupWindow(anchorView: View, position: Int) {
        // Inflate the custom layout for the popup menu
        val popupView =
            LayoutInflater.from(requireContext()).inflate(R.layout.popup_positive_review, null)

        // Create PopupWindow with the custom layout
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set click listeners for each menu item in the popup layout
        popupView.findViewById<TextView>(R.id.itemHighestReview).setOnClickListener {

            binding.textReviewClick.text ="Sort by: Highest Review"
            sortReviewsBy("Highest")
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemLowestReview).setOnClickListener {
            binding.textReviewClick.text ="Sort by: Lowest Review"
            sortReviewsBy("Lowest")
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemRecentReview).setOnClickListener {
            binding.textReviewClick.text ="Sort by: Recent Review"
            sortReviewsBy("Recent")
            popupWindow.dismiss()
        }

        // Get the location of the anchor view (three-dot icon)
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        // Get the height of the PopupView after inflating it
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = popupView.measuredHeight
        val popupWeight = popupView.measuredWidth
        val screenWidht = resources?.displayMetrics?.widthPixels
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
        val screenHeight = resources?.displayMetrics?.heightPixels
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
        popupWindow.showAsDropDown(anchorView, xOffset, yOffset, Gravity.END)  // Adjust the Y offset dynamically
    }

    private fun shareApp() {
        val appPackageName = requireActivity().packageName
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "Buy this best app at: https://play.google.com/store/apps/details?id=$appPackageName"
            )
            type = "text/plain"
        }
        startActivity(sendIntent)
    }

    private fun sortReviewsBy(option: String) {
        when (option) {
            "Highest" -> reviewList.sortByDescending { it.rating }
            "Lowest" -> reviewList.sortBy { it.rating }
            "Recent" -> reviewList.sortByDescending { it.date }
        }
        adapterReview.updateAdapter(reviewList)
    }


    @SuppressLint("MissingInflatedId")
    fun dialogReview(){
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_review)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            var responseRate =  findViewById<RatingBar>(R.id.ratingbar)
            var communication =  findViewById<RatingBar>(R.id.ratingbar2)
            var onTime =  findViewById<RatingBar>(R.id.ratingbar3)
            var textPublishReview =  findViewById<TextView>(R.id.textPublishReview)
            var etMessage =  findViewById<TextView>(R.id.etMessage)

            textPublishReview.setOnClickListener{
                reviewPublishAPI(responseRate.rating,communication.rating,onTime.rating,etMessage.text.toString())
                dismiss()
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }}

    private fun initialization() {

        adapterAddon = AdapterAddOn(requireContext(), getAddOnList().subList(0, 4))

        adapterReview = AdapterReview(requireContext(), mutableListOf())

        binding.recyclerAddOn.adapter = adapterAddon
        binding.recyclerAddOn.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerAddOn.isNestedScrollingEnabled = false

        adapterInclude = BookingIncludeAdapter(mutableListOf())
        binding.recyclerIncludeBooking.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerIncludeBooking.adapter = adapterInclude

        binding.recyclerReviews.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerReviews.isNestedScrollingEnabled = false
        binding.recyclerReviews.adapter = adapterReview

        val textView = binding.tvShowMore
        textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        binding.tvLocationName.paintFlags =
            binding.tvLocationName.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        binding.tvShowMore.setOnClickListener {
            binding.tvShowMore.visibility = View.GONE
            adapterAddon.updateAdapter(getAddOnList())
        }

    }
    private fun getAddOnList(): MutableList<String> {

        var list = mutableListOf<String>()

        list.add("Computer Screen")

        list.add("Bed Sheets")

        list.add("Phone charger")

        list.add("Ring Light")

        list.add("Left Light")

        list.add("Water Bottle")

        return list

    }
    override fun onResume() {
        super.onResume()
        mapView.onResume()  // Important to call in onResume
        binding.textUserName.visibility = View.VISIBLE
        binding.rlNeedHelp.visibility = View.VISIBLE
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()  // Important to call in onPause
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()  // Important to call in onDestroy
        _binding = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()  // Important to call in onLowMemory
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap?.setOnMapLoadedCallback {
            val place = LatLng(latitude, longitude)
            mMap?.addMarker(MarkerOptions().position(place).title("Marker in place"))
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 10f))
        }
    }


}