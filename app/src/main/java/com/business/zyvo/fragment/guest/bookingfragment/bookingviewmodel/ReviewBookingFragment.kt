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
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.business.zyvo.R
import com.business.zyvo.activity.ChatActivity
import com.business.zyvo.activity.guest.propertydetails.model.Pagination
import com.business.zyvo.activity.guest.propertydetails.model.Review
import com.business.zyvo.adapter.AdapterAddOn
import com.business.zyvo.adapter.BookingIncludeAdapter
import com.business.zyvo.adapter.guest.AdapterProReview
import com.business.zyvo.adapter.guest.PropertyIncludedAdapter
import com.business.zyvo.databinding.FragmentReviewGustBookingBinding
import com.business.zyvo.fragment.both.viewImage.ViewImageDialogFragment
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.ErrorDialog.formatConvertCount
import com.business.zyvo.utils.ErrorDialog.truncateToTwoDecimalPlaces
import com.business.zyvo.utils.NetworkMonitorCheck
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ReviewBookingFragment : Fragment() , OnMapReadyCallback {

    private var _binding : FragmentReviewGustBookingBinding? = null
    private  val binding get() = _binding!!
    private var bookingId = 0
    private lateinit var adapterAddon: AdapterAddOn
    private lateinit var adapterReview: AdapterProReview
    private lateinit var adapterInclude: BookingIncludeAdapter
    private lateinit var mapView: MapView
    private var mMap: GoogleMap? = null
    lateinit var  navController: NavController
    private lateinit var sessionManager: SessionManager
    private val bookingViewModel: BookingViewModel by viewModels()
    var latitude = 0.0
    var longitude = 0.0
    var propertyId = 0
    var reviewList: MutableList<Review> = mutableListOf()
    var pagination:Pagination?=null
    var filter = "highest_review"
    private var hostId :String ="-1"



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
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_review_gust_booking,container,false)

        binding.textReportIssueButton.visibility = View.GONE
        binding.textUserName.visibility = View.VISIBLE
        binding.rlNeedHelp.visibility = View.VISIBLE
        binding.imageStar1.visibility = View.GONE
        binding.textRatingStar1.visibility = View.GONE
        binding.viewProfile.visibility = View.GONE

        binding.textMessageTheHostButton.setOnClickListener {
            callingJoinChannelApi()
        }

        // Observe the isLoading state
        lifecycleScope.launch {
            bookingViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    LoadingUtils.showDialog(requireContext(), false)
                } else {
                    LoadingUtils.hideDialog()
                }
            }
        }

        binding.rlNeedHelp.setOnClickListener {
            val bundle = Bundle().apply {
                putString("type", "Guest")
            }
            findNavController().navigate(R.id.helpCenterFragment_host,bundle)
        }

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

        binding.showMoreReview.setOnClickListener {
            pagination?.let {
                if (it.current_page!=it.total_pages && it.current_page<it.total_pages) {
                    loadMoreReview(filter,(it.current_page+1).toString())
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getBookingDetailsListAPI() {
        if (!NetworkMonitorCheck._isConnected.value) {
            showErrorDialog(requireContext(), resources.getString(R.string.no_internet_dialog_msg))
            return
        }

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                bookingViewModel.getBookingDetailsList(sessionManager.getUserId().toString(),
                    bookingId,sessionManager.getGustLatitude(),sessionManager.getGustLongitude()).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            val list = it.data?.first
                            Log.d("DATACHECK","list : $list")
                            if (list!=null) { // Check if list is not null or empty
                                val data = list.data
                                hostId = list.data.host_id.toString()
                                binding.textUserName.text = data.host_name ?: "N/A"
                                binding.tvNamePlace.text = data.property_name ?: "N/A"
                                binding.tvStatus.text = data.status
                                binding.textMiles.text = (data.distance_miles ?: "N/A").toString()+" miles away"
                                binding.textRatingStar.text = "$${truncateToTwoDecimalPlaces(data.total_rating ?: "0")}"
                                binding.time.text = data.charges?.booking_hours.toString()
                                binding.money.text = "$${truncateToTwoDecimalPlaces(data.charges?.booking_amount ?: "0.00")}"
                                binding.tvCleaningFee.text = "$${truncateToTwoDecimalPlaces(data.charges?.cleaning_fee ?: "0.00")}"
                                binding.tvCleaningFee.text = "$${truncateToTwoDecimalPlaces(data.charges?.cleaning_fee ?: "0.00")}"
                                binding.tvCleaningFee.text = "$${truncateToTwoDecimalPlaces(data.charges?.cleaning_fee ?: "0.00")}"
                                binding.tvServiceFee.text = "$${truncateToTwoDecimalPlaces(data.charges?.zyvo_service_fee ?: "0.00")}"
                                binding.tvTaxes.text = "$${truncateToTwoDecimalPlaces(data.charges?.taxes ?: "0.00")}"
                                binding.tvAddOn.text = "$${truncateToTwoDecimalPlaces(data.charges?.add_on?.toString() ?: "0.00")}"
                                binding.tvTotalPrice.text = "$${truncateToTwoDecimalPlaces(data.charges?.total ?: "0.00")}"
                                binding.tvBookingDate.text = data.booking_detail?.date ?: "N/A"
                                binding.bookingFromTo.text = data.booking_detail?.start_end_time ?: "N/A"
                                binding.tvBookingTotalTime.text = data.booking_detail?.time ?: "N/A"
                                binding.tvParkingContent.text = (data.parking_rules ?: "N/A").toString()
                                binding.tvHostContent.text = (data.host_rules ?: "N/A").toString()
                                binding.tvLocationName.text = data.location ?: "N/A"
                                propertyId = data.property_id ?: 0
                                data?.total_rating?.let {
                                    binding.endRatingTv.text = it
                                }

                                //image loading from glide
                                Glide.with(requireContext())
                                    .load(AppConstant.BASE_URL + data.host_profile_image)
                                    .error(R.drawable.ic_circular_img_user)
                                    .into(binding.imageProfilePicture)

                                data?.first_property_image?.let {
                                    //image loading from glide
                                    Glide.with(requireContext())
                                        .load(AppConstant.BASE_URL + it)
                                        .error(R.drawable.ic_circular_img_user)
                                        .into(binding.imgProfileHotel)
                                }

                                data.property_images?.let {
                                    binding.llHotelViews.setOnClickListener {
                                        openImageDialog(data.property_images)
                                    }

                                    binding.proImageMore.setOnClickListener {
                                        openImageDialog(data.property_images)
                                    }
                                    data.property_images?.let {
                                        if (it.isNotEmpty()){
                                            if (it.size==1){
                                                binding.cvTwoAndThreeImage.visibility = View.GONE
                                                binding.cvOneImage.visibility = View.VISIBLE
                                                binding.llThreeImage.visibility = View.GONE
                                                binding.llTwoImage.visibility = View.GONE
                                                binding.proImageMore.visibility = View.GONE
                                                Glide.with(requireActivity()).load(AppConstant.BASE_URL + it.get(0)).into(binding.proImageViewOne)
                                            }
                                            if (it.size==2){
                                                binding.cvTwoAndThreeImage.visibility = View.VISIBLE
                                                binding.cvOneImage.visibility = View.GONE
                                                binding.llThreeImage.visibility = View.GONE
                                                binding.llTwoImage.visibility = View.VISIBLE
                                                binding.proImageMore.visibility = View.GONE
                                                Glide.with(requireActivity()).load(AppConstant.BASE_URL + it.get(0)).into(binding.proImageViewTwoAndThree)
                                                Glide.with(requireActivity()).load(AppConstant.BASE_URL + it.get(1)).into(binding.proImageTwo)
                                            }
                                            if (it.size==3){
                                                binding.cvTwoAndThreeImage.visibility = View.VISIBLE
                                                binding.cvOneImage.visibility = View.GONE
                                                binding.llThreeImage.visibility = View.VISIBLE
                                                binding.llTwoImage.visibility = View.GONE
                                                binding.proImageMore.visibility = View.GONE
                                                Glide.with(requireActivity()).load(AppConstant.BASE_URL + it.get(0)).into(binding.proImageViewTwoAndThree)
                                                Glide.with(requireActivity()).load(AppConstant.BASE_URL + it.get(1)).into(binding.prImageTwo)
                                                Glide.with(requireActivity()).load(AppConstant.BASE_URL + it.get(2)).into(binding.prImageThree)
                                            }
                                            if (it.size==4){
                                                binding.cvTwoAndThreeImage.visibility = View.VISIBLE
                                                binding.cvOneImage.visibility = View.GONE
                                                binding.llThreeImage.visibility = View.VISIBLE
                                                binding.llTwoImage.visibility = View.GONE
                                                binding.proImageMore.visibility = View.VISIBLE
                                                Glide.with(requireActivity()).load(AppConstant.BASE_URL + it.get(0)).into(binding.proImageViewTwoAndThree)
                                                Glide.with(requireActivity()).load(AppConstant.BASE_URL + it.get(1)).into(binding.prImageTwo)
                                                Glide.with(requireActivity()).load(AppConstant.BASE_URL + it.get(2)).into(binding.prImageThree)
                                            }
                                        }
                                    }
                                }
                                // Safe parsing of latitude & longitude
                                latitude = data.latitude?.toDoubleOrNull() ?: 0.0
                                longitude = data.longitude.toDoubleOrNull() ?: 0.0

                                pagination = Gson().fromJson(it.data.second.getAsJsonObject("pagination"),
                                    Pagination::class.java)
                                val listType = object : TypeToken<List<Review>>() {}.type
                                if(pagination == null){
                                    Log.d("TESTING_PAGINATION","Pagination is Null")
                                    binding.showMoreReview.visibility = View.GONE
                                }
                                pagination?.let {
                                    Log.d("TESTING_PAGINATION", "Total :- "+ pagination!!.total +" Current Page:- "+pagination!!.current_page)
                                    if (pagination!!.total <= pagination!!.current_page) {
                                        binding.showMoreReview.visibility = View.GONE
                                    }
                                }

                                val localreviewList:MutableList<Review> = Gson().fromJson(it.data.second.getAsJsonArray("data"), listType)
                                reviewList.addAll(localreviewList)
                                // Update reviews dynamically
                                reviewList?.let {
                                    if (it.isNotEmpty()){
                                        adapterReview.updateAdapter(it)
                                        binding.textK.text = "("+ formatConvertCount(reviewList.size.toString()) +")"
                                        binding.tvReviewsCount.text = "Reviews "+"("+formatConvertCount(reviewList.size.toString()) +")"
                                    }
                                }
                                data?.amenities?.let {
                                    if (it.isNotEmpty()) {
                                        val propertyIncludedAdapter = PropertyIncludedAdapter(requireContext(),
                                            it)
                                        binding.recyclerIncludeBooking.adapter = propertyIncludedAdapter

                                    }
                                }
                               // data.activities?.let { it1 -> adapterInclude.updateItems(it1) }
                               // data.amenities?.let { it1 -> adapterAddon.updateAdapter(it1) }
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


    private fun callingJoinChannelApi(){
        lifecycleScope.launch {
            val session= SessionManager(requireContext())
            val userId = session.getUserId()

            if(userId != null) {

                LoadingUtils.showDialog(requireContext(),true)

                  var channelName :String =""
                if (userId < Integer.parseInt(hostId)) {
                    channelName = "ZYVOOPROJ_" + userId + "_" + hostId +"_"+propertyId
                }

                else {
                    channelName = "ZYVOOPROJ_" + hostId + "_" + userId +"_"+propertyId
                }

                bookingViewModel.joinChatChannel(userId,Integer.parseInt(hostId),channelName,"guest").collect{
                    when(it){
                        is NetworkResult.Success ->{
                            LoadingUtils.hideDialog()
                            var loggedInId = SessionManager(requireContext()).getUserId()
                            if(it.data?.receiver_id?.toInt() == loggedInId){

                                var userImage :String =  it.data?.receiver_avatar.toString()
                                Log.d("TESTING_PROFILE_HOST",userImage)
                                var friendImage :String = it.data?.sender_avatar.toString()
                                Log.d("TESTING_PROFILE_HOST",friendImage)
                                var friendName :String = ""
                                if(it.data?.sender_name != null){
                                    friendName = it.data.sender_name
                                }
                                var userName = ""
                                userName = it.data?.receiver_name.toString()
                                val intent = Intent(requireContext(), ChatActivity::class.java)
                                intent.putExtra("user_img",userImage).toString()
                                SessionManager(requireContext()).getUserId()?.let { it1 -> intent.putExtra(AppConstant.USER_ID, it1.toString()) }
                                Log.d("TESTING","REVIEW HOST"+channelName)
                                intent.putExtra(AppConstant.CHANNEL_NAME,channelName)
                                intent.putExtra(AppConstant.FRIEND_ID,hostId)
                                intent.putExtra("friend_img",friendImage).toString()
                                intent.putExtra("friend_name",friendName).toString()
                                intent.putExtra("user_name",userName)
                                intent.putExtra("sender_id", hostId)
                                startActivity(intent)
                            }
                            else if(it.data?.sender_id?.toInt() == loggedInId){
                                var userImage :String =  it.data?.sender_avatar.toString()
                                Log.d("TESTING_PROFILE_HOST",userImage)
                                var friendImage :String = it.data?.receiver_avatar.toString()
                                Log.d("TESTING_PROFILE_HOST",friendImage)
                                var friendName :String = ""
                                if(it.data?.receiver_name != null){
                                    friendName = it.data.receiver_name
                                }
                                var userName = ""
                                userName = it.data?.sender_name.toString()
                                val intent = Intent(requireContext(), ChatActivity::class.java)
                                intent.putExtra("user_img",userImage).toString()
                                SessionManager(requireContext()).getUserId()?.let { it1 -> intent.putExtra(AppConstant.USER_ID, it1.toString()) }
                                Log.d("TESTING","REVIEW HOST"+channelName)
                                intent.putExtra(AppConstant.CHANNEL_NAME,channelName)
                                intent.putExtra(AppConstant.FRIEND_ID,hostId)
                                intent.putExtra("friend_img",friendImage).toString()
                                intent.putExtra("friend_name",friendName).toString()
                                intent.putExtra("user_name",userName)
                                intent.putExtra("sender_id", hostId)
                                startActivity(intent)
                            }

                        }
                        is NetworkResult.Error ->{
                            LoadingUtils.hideDialog()

                        }
                        else ->{
                            LoadingUtils.hideDialog()
                        }
                    }
                }
            }
        }
    }



    private fun reviewPublishAPI(responseRate: Int, communication: Int, onTime: Int, etMessage: String,
                                 dialog: Dialog) {
        if (!NetworkMonitorCheck._isConnected.value) {
            showErrorDialog(requireContext(), getString(R.string.no_internet_dialog_msg))
            return
        }
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                bookingViewModel.getReviewPublishAPI(
                    sessionManager.getUserId().toString(),
                    bookingId.toString(), propertyId.toString(),responseRate.toString(),
                    communication.toString(), onTime.toString(),
                    etMessage
                ).collect { result ->
                    withContext(Dispatchers.Main) {
                        when (result) {
                            is NetworkResult.Success -> {
                                dialog.dismiss()
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

    private fun sortReviewsBy(option: String) {
        when (option) {
            "Highest" -> reviewList.sortByDescending { it.review_rating }
            "Lowest" -> reviewList.sortBy { it.review_rating }
            "Recent" -> reviewList.sortByDescending { it.review_date }
        }
        adapterReview.updateAdapter(reviewList)
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
    private fun loadMoreReview(filter :String,
                               page :String) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                bookingViewModel.filterPropertyReviews(propertyId.toString(),
                    filter,page).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                val listType = object : TypeToken<List<Review>>() {}.type
                                val localreviewList:MutableList<Review> = Gson().fromJson(resp.first, listType)
                                reviewList.addAll(localreviewList)
                                pagination = Gson().fromJson(resp.second,
                                    Pagination::class.java)

                                if(pagination == null){
                                    Log.d("TESTING_PAGINATION","Pagination is Null")
                                    binding.showMoreReview.visibility = View.GONE
                                }
                                pagination?.let {
                                    Log.d("TESTING_PAGINATION", "Total :- "+ pagination!!.total +" Current Page:- "+pagination!!.current_page)
                                    if (pagination!!.total <= pagination!!.current_page) {
                                        binding.showMoreReview.visibility = View.GONE
                                    }
                                }
                                reviewList?.let {
                                    if (it.isNotEmpty()){
                                        adapterReview.updateAdapter(it)
                                        binding.textK.text = "("+ formatConvertCount(reviewList.size.toString()) +" )"
                                        binding.tvReviewsCount.text = "Reviews "+"("+formatConvertCount(reviewList.size.toString()) +")"
                                    }
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


    @SuppressLint("MissingInflatedId")
    fun dialogReview(){
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(true)
            setContentView(R.layout.dialog_review)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            val responseRate =  findViewById<RatingBar>(R.id.ratingbar)
            val communication =  findViewById<RatingBar>(R.id.ratingbar2)
            val onTime =  findViewById<RatingBar>(R.id.ratingbar3)
            val textPublishReview =  findViewById<TextView>(R.id.textPublishReview)
            val etMessage =  findViewById<TextView>(R.id.etMessage)
            textPublishReview.setOnClickListener{
                reviewPublishAPI(responseRate.rating.toInt(),
                    communication.rating.toInt(),
                    onTime.rating.toInt(),
                    etMessage.text.toString(),dialog)
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }}

    private fun initialization() {

        adapterAddon = AdapterAddOn(requireContext(), getAddOnList().subList(0, 4))

        adapterReview = AdapterProReview(requireContext(), reviewList)

        binding.recyclerAddOn.adapter = adapterAddon
        binding.recyclerAddOn.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerAddOn.isNestedScrollingEnabled = false

        adapterInclude = BookingIncludeAdapter(mutableListOf())
     //   binding.recyclerIncludeBooking.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    //    binding.recyclerIncludeBooking.adapter = adapterInclude

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
            Log.d(ErrorDialog.TAG,""+latitude+" "+longitude)
            mMap?.addMarker(MarkerOptions().position(place).title("Marker in place"))
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 10f))
        }
    }

    fun shapeTopBottomRightCorners(shapeableImageView: ShapeableImageView) {
        val shapeAppearanceModel = ShapeAppearanceModel.Builder()
            .setTopRightCorner(CornerFamily.ROUNDED, 20f) // Set top-right corner radius to 60dp
            .setBottomRightCorner(
                CornerFamily.ROUNDED,
                20f
            ) // Set bottom-right corner radius to 60dp
            .setTopLeftCorner(CornerFamily.CUT, 0f) // Keep top-left corner sharp
            .setBottomLeftCorner(CornerFamily.CUT, 0f) // Keep bottom-left corner sharp
            .build()

        shapeableImageView.setShapeAppearanceModel(shapeAppearanceModel)
    }
    fun shapeTopBottomRightLeftCorners(shapeableImageView: ShapeableImageView) {

        val shapeAppearanceModel = ShapeAppearanceModel.Builder()
            .setTopRightCorner(CornerFamily.ROUNDED, 20f)
            .setBottomRightCorner(CornerFamily.ROUNDED, 20f)
            .setTopLeftCorner(CornerFamily.CUT, 20f)
            .setBottomLeftCorner(CornerFamily.CUT, 20f)
            .build()

        shapeableImageView.setShapeAppearanceModel(shapeAppearanceModel)

    }

    fun shapeTopBottomLeftCorner(shapeableImageView: ShapeableImageView) {
        val shapeAppearanceModel = ShapeAppearanceModel.Builder()
            .setTopRightCorner(CornerFamily.ROUNDED, 0f) // Set top-right corner radius to 60dp
            .setBottomRightCorner(
                CornerFamily.ROUNDED,
                0f
            ) // Set bottom-right corner radius to 60dp
            .setTopLeftCorner(CornerFamily.CUT, 20f) // Keep top-left corner sharp
            .setBottomLeftCorner(CornerFamily.CUT, 20f) // Keep bottom-left corner sharp
            .build()

        shapeableImageView.setShapeAppearanceModel(shapeAppearanceModel)
    }

    private fun openImageDialog(imageUrls: List<String>) {

        val dialogFragment = ViewImageDialogFragment()
        imageUrls.let {
            val bundle = Bundle().apply {
                putStringArrayList("image_list", java.util.ArrayList(it))
            }
            dialogFragment.arguments = bundle
            dialogFragment.show(requireActivity().supportFragmentManager, "exampleDialog")
        }
    }


}