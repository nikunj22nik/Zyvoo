package com.business.zyvo.fragment.host


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RatingBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.BuildConfig
import com.business.zyvo.DateManager.DateManager
import com.business.zyvo.LoadingUtils
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.activity.ChatActivity
import com.business.zyvo.adapter.AdapterAddOn
import com.business.zyvo.adapter.host.AdapterIncludeInBooking
import com.business.zyvo.adapter.host.AdapterReviewHost
import com.business.zyvo.databinding.FragmentReviewBookingBinding
import com.business.zyvo.fragment.both.MapDialogFragment
import com.business.zyvo.fragment.both.viewImage.ViewImageDialogFragment
import com.business.zyvo.model.host.ReviewerProfileModel
import com.business.zyvo.model.host.hostdetail.HostDetailModel
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog.formatConvertCount
import com.business.zyvo.utils.ErrorDialog.showToast
import com.business.zyvo.utils.ErrorDialog.truncateToTwoDecimalPlaces
import com.business.zyvo.utils.NetworkMonitorCheck
import com.business.zyvo.utils.PrepareData
import com.business.zyvo.viewmodel.host.HostBookingsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.gson.Gson
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener
import com.skydoves.powerspinner.PowerSpinnerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ReviewBookingHostFragment : Fragment(), OnMapReadyCallback {


    private var _binding: FragmentReviewBookingBinding? = null
    private val binding get() = _binding!!
    private var bookingId: Int = -1

    lateinit var adapterAddon: AdapterAddOn
    lateinit var adapterReview: AdapterReviewHost
    private lateinit var mapView: MapView
    private lateinit var viewModel: HostBookingsViewModel
    private var mMap: GoogleMap? = null
    lateinit var navController: NavController
    lateinit var adapterIncludeInBooking: AdapterIncludeInBooking
    private var autoOpenDialog2Job: Job? = null

    var latitude: Double = 0.00
    var longitude: Double = 0.00
    var reviewlist: MutableList<Pair<Int, String>> = mutableListOf()
    var reviewListStr: MutableList<String> = mutableListOf()
    lateinit var spinnerView: PowerSpinnerView
    var propertyId: Int = -1
    var channelName: String = ""
    var guestId: Int = -1
    var friendImage: String = ""
    var currentLatitude: String = "37.0902"
    var currentLongitude: String = "95.7129"
    private var extensioId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.containsKey(AppConstant.BOOKING_ID)) {
                bookingId = it.getInt(AppConstant.BOOKING_ID)

                extensioId = it.getString(AppConstant.EXTENSION_ID)?:""
                Log.d("IdTag",bookingId.toString())
                Log.d("IdTag",extensioId.toString())
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_review_booking, container, false
        )

        adapterIncludeInBooking = AdapterIncludeInBooking(mutableListOf(), requireContext())

        val gridLayoutManager =
            GridLayoutManager(requireContext(), PrepareData.numberOFColumn(requireActivity()))

        binding.recyclerIncludeBooking.layoutManager = gridLayoutManager
        binding.recyclerIncludeBooking.adapter = adapterIncludeInBooking

        binding.textMessageTheHostButton.text = "Message the Guest"
        binding.rlNeedHelp.visibility = View.GONE

        binding.imageVerifyCheck.visibility = View.GONE
        binding.imageStar1.visibility = View.VISIBLE
        binding.textRatingStar1.visibility = View.VISIBLE

        viewModel = ViewModelProvider(this)[HostBookingsViewModel::class.java]

        if (bookingId != -1) {
            Log.d("TESTING", "Booking Id is " + bookingId)
        }

        callingBookingDetailApi()

        binding.mapView.setOnClickListener {
            callingMapScreenApi()
        }

        return binding.root
    }
    //onView Create is called

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        initialization()
        binding.llFavorite.visibility = View.GONE
        binding.llShare.visibility = View.GONE
        binding.imageBackIcon.setOnClickListener {
            navController.navigateUp()
        }

        binding.textReportIssueButton.setOnClickListener {
            dialogReportIssue()
        }

        binding.textReviewBookingButton.setOnClickListener {
            dialogReview()
        }

        binding.textApproveBookingButton.setOnClickListener {
            acceptBooking()
        }

        binding.textDeclineTheHostButton.setOnClickListener {
            declineBooking()
        }

        binding.textMessageTheHostButton.setOnClickListener {
            callingJoinChannelApi()
        }

        binding.imageShare.setOnClickListener {
            shareApp()
        }
        binding.textReviewClick.setOnClickListener {
            showPopupWindow(it, 0)
        }
        showingMoreText()


        binding.rlParking.setOnClickListener {
            if (binding.tvParkingRule.visibility == View.VISIBLE) {
                binding.tvParkingRule.visibility = View.GONE
            } else {
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

        callingReportReason()

    }

    private fun callingJoinChannelApi() {
        lifecycleScope.launch {

            val session = SessionManager(requireContext())

            val userId = session.getUserId()

            if (userId != null) {

                LoadingUtils.showDialog(requireContext(), true)

                Log.d("TESTING_GUEST_ID", guestId.toString() + "GuestId IN Channel")

                Log.d("TESTING_GUEST_ID", channelName.toString() + " channel Name")

                viewModel.joinChatChannel(guestId, userId, channelName, "host").collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            LoadingUtils.hideDialog()
                             Log.d("checkDataResponse",it.data?.receiver_name.toString()+it.data?.sender_name.toString())
                         //   var userImage: String = it.data?.sender_avatar.toString()
                            var userImage: String = it.data?.receiver_avatar.toString()

                            Log.d("TESTING_PROFILE_HOST", userImage)

                           // var friendImage: String = it.data?.receiver_avatar.toString()
                            var friendImage: String = it.data?.sender_avatar.toString()

                            Log.d("TESTING_PROFILE_HOST", friendImage)

                            var friendName: String = ""

//                            if (it.data?.receiver_name != null) {
//                                friendName = it.data.receiver_name
//                            }
                            if (it.data?.sender_name != null) {
                                friendName = it.data.sender_name
                            }
                            var userName = ""

                          //  userName = it.data?.sender_name.toString()
                            userName = it.data?.receiver_name.toString()

                            val intent = Intent(requireContext(), ChatActivity::class.java)

                            intent.putExtra("user_img", userImage).toString()

                            SessionManager(requireContext()).getUserId()?.let { it1 ->
                                intent.putExtra(
                                    AppConstant.USER_ID,
                                    it1.toString()
                                )
                            }

                            Log.d("TESTING", "REVIEW HOST" + channelName)

                            intent.putExtra(AppConstant.CHANNEL_NAME, channelName)

                            intent.putExtra(AppConstant.FRIEND_ID, guestId.toString())

                            intent.putExtra("friend_img", friendImage).toString()

                            intent.putExtra("friend_name", friendName).toString()

                            intent.putExtra("user_name", userName)
                            intent.putExtra("sender_id", it.data?.sender_id)

                            startActivity(intent)
                        }

                        is NetworkResult.Error -> {
                            LoadingUtils.hideDialog()

                        }

                        else -> {
                            LoadingUtils.hideDialog()
                        }
                    }
                }
            }
        }
    }

    private fun callingReportReason(loadingShowing: Boolean = false) {
        lifecycleScope.launch {
            if (loadingShowing) {
                LoadingUtils.showDialog(requireContext(), false)
            }
            viewModel.reportListReason().collect {
                when (it) {
                    is NetworkResult.Success -> {
                        if (loadingShowing) {
                            LoadingUtils.hideDialog()
                        }
                    }

                    is NetworkResult.Error -> {
                        if (loadingShowing) {
                            LoadingUtils.hideDialog()
                        }
                    }

                    else -> {

                    }
                }
            }
        }

        viewModel.reviewListLiveData.observe(viewLifecycleOwner, Observer { reviewList ->
            reviewlist = reviewList
            Log.d("TESTING", "ReviewList Inside observer " + reviewList.size)
            reviewListStr.clear()
            reviewList.forEach {
                reviewListStr.add(it.second)
            }
            if (::spinnerView.isInitialized) {
                spinnerView.setItems(reviewListStr)
            }
        })

    }

    private fun callingBookingDetailApi() {
        lifecycleScope.launch {
            var sessionManager = SessionManager(requireContext())
            var latitude = sessionManager.getLatitude()
            var longitude = sessionManager.getLongitude()
            LoadingUtils.showDialog(requireContext(), false)
            if (latitude.equals("") || longitude.equals("")) {
                viewModel.hostBookingDetails(bookingId, null, null,
                    extensioId).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.second?.let { it1 ->

                                {
                                    LoadingUtils.hideDialog()
                                    guestId = it1.guest_id
                                    Log.d("TESTING_GUEST_ID", "GuestId IN Api")
                                    propertyId = it1.property_id
                                    showingDataToUi(it1)
                                    getReviewHost()
                                    Log.d("TESTING_GUEST_ID", "Property Id in first" + propertyId)
                                }
                            }
                        }

                        is NetworkResult.Error -> {
                            LoadingUtils.hideDialog()
                            LoadingUtils.showErrorDialog(requireContext(), it.message.toString())
                        }

                        else -> {
                            LoadingUtils.hideDialog()
                        }
                    }
                }
            } else {
                viewModel.hostBookingDetails(bookingId, latitude, longitude,
                    extensioId).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            LoadingUtils.hideDialog()
                            it.data?.let { it1 ->
                                propertyId = it.data?.second?.property_id!!
                                showingDataToUi(it1.second)
                                getReviewHost()
                                guestId = it1.second.guest_id
                            }

                            Log.d("TESTING_GUEST_ID", "Property Id in Second" + propertyId)

                        }

                        is NetworkResult.Error -> {
                            LoadingUtils.hideDialog()
                            LoadingUtils.showErrorDialog(requireContext(), it.message.toString())
                        }

                        else -> {
                            LoadingUtils.hideDialog()
                        }
                    }
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun showingDataToUi(data: HostDetailModel) {

        binding.tvNamePlace.setText(data.property_title)
        binding.textRatingStar.setText(String.format("%.1f", (data.reviews_total_rating ?: "0").toString().toFloat()))
        binding.textK.setText(" ( " + formatConvertCount(data?.reviews_total_count?:"") + " )")
        binding.textMiles.setText(data.distance_miles + " miles away")
        binding.time.setText(data.booking_hour)
        binding.money.setText("$" + truncateToTwoDecimalPlaces(data.booking_amount?:"0"))

        adapterIncludeInBooking.updateAdapter(data.amenities)

        if (data.guest_id < data.host_id) {
            channelName = "ZYVOOPROJ_" + data.guest_id + "_" + data.host_id + "_" + bookingId
        } else {
            channelName = "ZYVOOPROJ_" + data.host_id + "_" + data.guest_id + "_" + bookingId
        }

        data.cleaning_fee?.let {
            if (it != "") {
                binding.tvCleaningFee.setText("$" + truncateToTwoDecimalPlaces(it))
            } else {
                binding.tvCleaningFee.setText("$" + "0")
            }

        } ?: binding.tvCleaningFee.setText("$0")

        data.service_fee?.let { binding.tvServiceFee.setText("$" + truncateToTwoDecimalPlaces(it)) }
            ?: binding.tvServiceFee.setText("$0")


        data.guest_avatar?.let {
            friendImage = BuildConfig.MEDIA_URL + it
            Glide.with(requireContext()).load(friendImage).into(binding.imageProfilePicture)
        }

        data.tax?.let {
            binding.tvTaxes.setText("$" + truncateToTwoDecimalPlaces(it))
        } ?: binding.tvTaxes.setText("$0")

        data.add_on_total?.let {
            if (it != "") {
                binding.tvAddOn.setText("$" + truncateToTwoDecimalPlaces(it))
            } else {
                binding.tvAddOn.setText("$" + "0")
            }

        } ?: binding.tvAddOn.setText("$0")

        data.discount?.let {
            if (it != "") {
                binding.tvDiscount.setText("$-" + truncateToTwoDecimalPlaces(it))
            } else {
                binding.tvDiscount.setText("$" + "0")
            }
        }?: binding.tvDiscount.setText("$0")

        if (binding.tvDiscount.text.toString().equals("$0") || binding.tvDiscount.text.toString().equals("$-0")){
            binding.llDiscountLabel.visibility = View.GONE
        }
        data.booking_total_amount?.takeIf { it.isNotEmpty() }?.let {
            binding.tvTotalPrice.text = "$${truncateToTwoDecimalPlaces(it)}"
        } ?: run {
            binding.tvTotalPrice.text = "$0"
        }

        data.guest_name?.let {
            binding.textUserName.setText(it)
        } ?: binding.textUserName.setText("")

        data.guest_rating?.let {
            binding.textRatingStar1.setText(it)
        } ?: binding.textRatingStar1.setText("")

        data.property_title?.let {
            binding.tvTitle.setText(it)
        } ?: binding.tvTitle.setText("")

        data.booking_status?.let {
            binding.tvStatus.setText(it.replaceFirstChar { it.uppercase() })
            when (data.booking_status) {
                "Confirmed" ->  binding.tvStatus.setBackgroundResource(R.drawable.blue_button_bg)
                "Awaiting Payment" -> binding.tvStatus.setBackgroundResource(R.drawable.yellow_button_bg)
                "Cancelled" -> binding.tvStatus.setBackgroundResource(R.drawable.grey_button_bg)
                else -> binding.tvStatus.setBackgroundResource(R.drawable.button_bg) // Optional fallback
            }
            if (it.equals("Pending")) {
                binding.llTopButtons.visibility = View.GONE
                binding.llTopButtons1.visibility = View.VISIBLE
                binding.tvStatus.setBackgroundResource(R.drawable.grey_button_bg)
            } else {
                binding.llTopButtons.visibility = View.VISIBLE
                binding.llTopButtons1.visibility = View.GONE
            }
        } ?: binding.tvStatus.setText("")

        data.booking_date?.let {
            binding.tvBookingDate.setText(it)
        } ?: binding.tvBookingDate.setText("")

        data.booking_hour?.let {
            binding.tvBookingTotalTime.setText(it + " Hours")
        } ?: binding.tvBookingTotalTime.setText("")

        binding.bookingFromTo.text =
            "From " + data.booking_start_time + " to " + data.booking_end_time

        data.reviews_total_count?.let {
            binding.tvReviewsCount.setText("Reviews (" + it + " )")
        }

        data.reviews_total_rating?.let {
            val formattedRating = String.format("%.1f", it.toFloat())
            binding.endRatingTv.text = formattedRating
        }

        data.parking_rules?.let {
            binding.tvParkingContent.setText(it)
        } ?: binding.tvParkingContent.setText("")

        data.address?.let {
            binding.tvLocationName.setText(it)
        }

        data.host_rules?.let {
            binding.tvHostContent.setText(it)
        }

        binding.llHotelViews.setOnClickListener {
            val dialogFragment = ViewImageDialogFragment()
            data?.images.let {
                val bundle = Bundle().apply {
                    putStringArrayList("image_list", java.util.ArrayList(it))
                }
                dialogFragment.arguments = bundle
                dialogFragment.show(requireActivity().supportFragmentManager, "exampleDialog")
            }
        }
        binding.proImageMore.setOnClickListener {
            val dialogFragment = ViewImageDialogFragment()
            data?.images.let {
                val bundle = Bundle().apply {
                    putStringArrayList("image_list", java.util.ArrayList(it))
                }
                dialogFragment.arguments = bundle
                dialogFragment.show(requireActivity().supportFragmentManager, "exampleDialog")
            }
        }

        data?.images?.let {
            if (it.isNotEmpty()) {
                if (it.size == 1) {
                    Glide.with(requireContext()).load(BuildConfig.MEDIA_URL + it[0])
                        .into(binding.imgProfileHotel)
                    binding.cvTwoAndThreeImage.visibility = View.GONE
                    binding.cvOneImage.visibility = View.VISIBLE
                    binding.llThreeImage.visibility = View.GONE
                    binding.llTwoImage.visibility = View.GONE
                    binding.proImageMore.visibility = View.GONE
                    Glide.with(requireActivity())
                        .load(BuildConfig.MEDIA_URL + it.get(0))
                        .into(binding.proImageViewOne)
                }
                if (it.size == 2) {
                    Glide.with(requireContext()).load(BuildConfig.MEDIA_URL + it[0])
                        .into(binding.imgProfileHotel)
                    binding.cvTwoAndThreeImage.visibility = View.VISIBLE
                    binding.cvOneImage.visibility = View.GONE
                    binding.llThreeImage.visibility = View.GONE
                    binding.llTwoImage.visibility = View.VISIBLE
                    binding.proImageMore.visibility = View.GONE
                    Glide.with(requireActivity())
                        .load(BuildConfig.MEDIA_URL + it.get(0))
                        .into(binding.proImageViewTwoAndThree)
                    Glide.with(requireActivity())
                        .load(BuildConfig.MEDIA_URL + it.get(1)).into(binding.proImageTwo)
                }
                if (it.size == 3) {
                    Glide.with(requireContext()).load(BuildConfig.MEDIA_URL + it[0])
                        .into(binding.imgProfileHotel)
                    binding.cvTwoAndThreeImage.visibility = View.VISIBLE
                    binding.cvOneImage.visibility = View.GONE
                    binding.llThreeImage.visibility = View.VISIBLE
                    binding.llTwoImage.visibility = View.GONE
                    binding.proImageMore.visibility = View.GONE
                    Glide.with(requireActivity())
                        .load(BuildConfig.MEDIA_URL + it.get(0))
                        .into(binding.proImageViewTwoAndThree)
                    Glide.with(requireActivity())
                        .load(BuildConfig.MEDIA_URL + it.get(1)).into(binding.prImageTwo)
                    Glide.with(requireActivity())
                        .load(BuildConfig.MEDIA_URL + it.get(2)).into(binding.prImageThree)
                }
                if (it.size >= 4) {
                    Glide.with(requireContext()).load(BuildConfig.MEDIA_URL + it[0])
                        .into(binding.imgProfileHotel)
                    binding.cvTwoAndThreeImage.visibility = View.VISIBLE
                    binding.cvOneImage.visibility = View.GONE
                    binding.llThreeImage.visibility = View.VISIBLE
                    binding.llTwoImage.visibility = View.GONE
                    binding.proImageMore.visibility = View.VISIBLE
                    Glide.with(requireActivity())
                        .load(BuildConfig.MEDIA_URL + it.get(0))
                        .into(binding.proImageViewTwoAndThree)
                    Glide.with(requireActivity())
                        .load(BuildConfig.MEDIA_URL + it.get(1)).into(binding.prImageTwo)
                    Glide.with(requireActivity())
                        .load(BuildConfig.MEDIA_URL + it.get(2)).into(binding.prImageThree)
                }
            }
        }
        /*data.images?.let {

            binding.llHotelViews.setOnClickListener {
                openImageDialog(data.images)
            }

            binding.llTwoImgView.setOnClickListener {
                openImageDialog(data.images)
            }

            binding.llSingleImg.setOnClickListener {
                openImageDialog(data.images)
            }

            if (it.size >= 3) {
                Glide.with(requireContext()).load(BuildConfig.MEDIA_URL + it[0])
                    .into(binding.imgProfileHotel)
                binding.llHotelViews.visibility = View.VISIBLE
                binding.llTwoImgView.visibility = View.GONE
                binding.llSingleImg.visibility = View.GONE
                shapeTopBottomLeftCorner(binding.shapeableImageView)

                Glide.with(requireContext()).load(BuildConfig.MEDIA_URL + it.get(0)).apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_img_not_found) // Placeholder image
                        .error(R.drawable.ic_img_not_found) // Error image
                )
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.shapeableImageView)
                Glide.with(requireContext()).load(BuildConfig.MEDIA_URL + it.get(1)).apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_img_not_found) // Placeholder image
                        .error(R.drawable.ic_img_not_found) // Error image
                )
                    .transition(DrawableTransitionOptions.withCrossFade()).into(binding.img1)
                Glide.with(requireContext()).load(BuildConfig.MEDIA_URL + it.get(2)).apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_img_not_found) // Placeholder image
                        .error(R.drawable.ic_img_not_found) // Error image
                )
                    .transition(DrawableTransitionOptions.withCrossFade()).into(binding.img2)
            }
            else if (it.size == 2) {
                Glide.with(requireContext()).load(BuildConfig.MEDIA_URL + it.get(0))
                    .into(binding.imgProfileHotel)

                binding.llHotelViews.visibility = View.GONE
                binding.llTwoImgView.visibility = View.VISIBLE
                binding.llSingleImg.visibility = View.GONE
                shapeTopBottomLeftCorner(binding.shapeableImageView21)
                shapeTopBottomRightCorners(binding.shapeableImageView22)
                Glide.with(requireContext()).load(BuildConfig.MEDIA_URL + it.get(0)).apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_img_not_found) // Placeholder image
                        .error(R.drawable.ic_img_not_found) // Error image
                )
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.shapeableImageView21)
                Glide.with(requireContext()).load(BuildConfig.MEDIA_URL + it.get(1)).apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_img_not_found) // Placeholder image
                        .error(R.drawable.ic_img_not_found) // Error image
                )
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.shapeableImageView22)
            }
            else if (it.size == 1) {
                Glide.with(requireContext()).load(BuildConfig.MEDIA_URL + it.get(0))
                    .into(binding.imgProfileHotel)

                binding.llHotelViews.visibility = View.GONE
                binding.llTwoImgView.visibility = View.GONE
                binding.llSingleImg.visibility = View.VISIBLE
                shapeTopBottomRightLeftCorners(binding.shapeableImageView11)
                Glide.with(requireContext()).load(BuildConfig.MEDIA_URL + it.get(0)).apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_img_not_found) // Placeholder image
                        .error(R.drawable.ic_img_not_found) // Error image
                )
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.shapeableImageView11)
            }
            else {
                binding.shapeableImageView11.visibility = View.GONE
            }
        }*/

        data.latitude?.let {
            latitude = it.toDouble()
            currentLatitude = latitude.toString()


            longitude = data.longitude?.let {
                it.toDouble()

            } ?: 0.00
            currentLongitude = longitude.toString()

            val newYork = LatLng(latitude, longitude)
            mMap?.clear()
            mMap?.addMarker(MarkerOptions().position(newYork).title(data.property_title)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_icon)))
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(newYork, 10f))

        }
        data?.extension_details?.let {
            binding.llbookingExt.visibility = View.VISIBLE
            it.extension_date?.let {
                binding.tvExtBookingDate.text = it
            }
            it.extension_hours?.let {
                binding.tvBookingExtTotalTime.text = "$it Hours"
            }
            it.extension_amount?.let {
                binding.tvExtTotalPrice.text = "$it"
            }
            binding.bookingExtFromTo.text =
                "From " + it.extension_start_time + " to " + it.extension_end_time

        }?:run {
            binding.llbookingExt.visibility = View.GONE
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
            adapterAddon.updateAdapter(getAddOnList())
            // binding.underlinedTextView.visibility =View.GONE
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

            adapterAddon.updateAdapter(getAddOnList().subList(0, 4))
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

            binding.textReviewClick.text = "Sort by: Highest Review"
            adapterReview.updateAdapter(viewModel.highestReviewList)
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemLowestReview).setOnClickListener {
            binding.textReviewClick.text = "Sort by: Lowest Review"
            adapterReview.updateAdapter(viewModel.lowestReviewList)
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemRecentReview).setOnClickListener {
            binding.textReviewClick.text = "Sort by: Recent Review"
            adapterReview.updateAdapter(viewModel.orgReviewList)
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
        popupWindow.showAsDropDown(
            anchorView,
            xOffset,
            yOffset,
            Gravity.END
        )  // Adjust the Y offset dynamically
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

    @SuppressLint("MissingInflatedId")
    fun dialogReview() {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(true)
            setContentView(R.layout.dialog_review)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
            val ratingbar = findViewById<RatingBar>(R.id.ratingbar)
            val ratingbar2 = findViewById<RatingBar>(R.id.ratingbar2)
            val ratingbar3 = findViewById<RatingBar>(R.id.ratingbar3)
            val imageCross = findViewById<ImageView>(R.id.imageCross)
            val textPublishReview = findViewById<TextView>(R.id.textPublishReview)
            val etMessage = findViewById<TextView>(R.id.etMessage)
            var message: String = ""

            imageCross.setOnClickListener {
                dismiss()
            }
            etMessage.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(
                    charSequence: CharSequence?, start: Int, before: Int, count: Int
                ) {
                    message = charSequence.toString()
                }

                override fun afterTextChanged(editable: Editable?) {

                }
            })


            textPublishReview.setOnClickListener {
                var responseRate = ratingbar.rating
                var communicationRate = ratingbar2.rating
                var onTimeRate = ratingbar3.rating
                var sessionManager = SessionManager(requireContext())
                var userId = sessionManager.getUserId()
                if (etMessage.text.isNotEmpty() && responseRate.toInt()!=0 &&
                    communicationRate.toInt()!=0 &&
                    onTimeRate.toInt()!=0){
                    dismiss()
                    lifecycleScope.launch {
                        if (NetworkMonitorCheck._isConnected.value) {
                            if (userId != null) {
                                LoadingUtils.showDialog(requireContext(), false)
                                viewModel.reviewGuest(
                                    userId,
                                    bookingId,
                                    propertyId,
                                    responseRate.toInt(),
                                    communicationRate.toInt(),
                                    onTimeRate.toInt(),
                                    message
                                ).collect {
                                    when (it) {
                                        is NetworkResult.Success -> {
                                            LoadingUtils.hideDialog()
                                            LoadingUtils.showSuccessDialog(
                                                requireContext(),
                                                it.data.toString()
                                            )
                                        }

                                        is NetworkResult.Error -> {
                                            LoadingUtils.hideDialog()
                                            LoadingUtils.showErrorDialog(
                                                requireContext(),
                                                it.message.toString()
                                            )
                                        }

                                        else -> {
                                            LoadingUtils.hideDialog()
                                        }
                                    }
                                }
                            }
                        } else {
                            LoadingUtils.showErrorDialog(
                                requireContext(),
                                "Please Check Internet Connection"
                            )
                        }
                    }
                }else{
                    LoadingUtils.showErrorDialog(
                        requireContext(),
                        AppConstant.message
                    )
                }
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }


    private fun dialogReportIssue() {
        var dateManager = DateManager(requireContext())
        val dialog = Dialog(requireContext(), R.style.BottomSheetDialog)
        dialog?.apply {
            setCancelable(true)
            setContentView(R.layout.report_violation)
//            window?.attributes = WindowManager.LayoutParams().apply {
//                copyFrom(window?.attributes)
//                width = WindowManager.LayoutParams.MATCH_PARENT
//                height = WindowManager.LayoutParams.MATCH_PARENT
//            }

            val crossButton: ImageView = findViewById(R.id.img_cross)
            val submit: RelativeLayout = findViewById(R.id.rl_submit_report)
            val txtSubmit: TextView = findViewById(R.id.txt_submit)
            var additionalDetail: EditText = findViewById<EditText>(R.id.et_addiotnal_detail)
            spinnerView = findViewById(R.id.spinnerView1)
            Log.d("TESTING", "Size of review list is " + reviewListStr.size)
            if (reviewListStr.size > 0) {
                spinnerView.setItems(reviewListStr)
            } else {
                callingReportReason(true)
            }
            var selectedPosition = -1
            var additionalTxt: String = ""
            additionalDetail.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence?, start: Int, before: Int, count: Int
                ) {
                }

                override fun onTextChanged(
                    charSequence: CharSequence?, start: Int, before: Int, count: Int
                ) {

                    if (charSequence != null) {
                        // Get the current text as a String
                        val currentText = charSequence.toString()

                    }
                }

                override fun afterTextChanged(editable: Editable?) {
                    if (editable != null) {
                        println("Updated text: ${editable.toString()}")
                        additionalTxt = editable.toString()
                    }
                }
            })



            spinnerView.setOnSpinnerItemSelectedListener(object :
                OnSpinnerItemSelectedListener<String> {
                override fun onItemSelected(
                    oldIndex: Int,
                    oldItem: String?,
                    newIndex: Int,
                    newItem: String
                ) {
                    selectedPosition = newIndex
                    Log.d("TESTING_INDEX", "Selected index is " + selectedPosition)

                }

            })


            submit.setOnClickListener {
                if (selectedPosition == -1) {
                    Toast.makeText(requireContext(), "Please Select reason", Toast.LENGTH_LONG)
                        .show()
                    return@setOnClickListener
                }else if (additionalDetail.text.isEmpty()){
                    showToast(requireActivity(),AppConstant.additional)
                    return@setOnClickListener
                }
                lifecycleScope.launch {
                    val sessionManager = SessionManager(requireContext())
                    val userId = sessionManager.getUserId()
                    if (userId != null) {
                        LoadingUtils.showDialog(requireContext(), false)
                        viewModel.hostReportViolationSend(
                            userId,
                            bookingId,
                            propertyId,
                            reviewlist[selectedPosition].first,
                            additionalTxt
                        ).collect {
                            when (it) {
                                is NetworkResult.Success -> {
                                    LoadingUtils.hideDialog()
                                    dialog.dismiss()
                                    openDialogNotification()
                                }

                                is NetworkResult.Error -> {
                                    LoadingUtils.hideDialog()
                                    dialog.dismiss()
                                }

                                else -> {
                                    LoadingUtils.hideDialog()
                                }
                            }
                        }
                    }
                }
            }

            crossButton.setOnClickListener {
                dialog.dismiss()
            }

            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),  // Width 90% of screen
                ViewGroup.LayoutParams.WRAP_CONTENT                   // Height wrap content
            )
            window?.setBackgroundDrawableResource(android.R.color.transparent) // Optional


            show()
        }

    }

    private fun callingMapScreenApi() {

        val mapDialog = MapDialogFragment()
        val args = Bundle()
        args.putString("axis", "latitude")
        args.putString("latitude", currentLatitude)
        args.putString("longitude", currentLongitude)
        mapDialog.arguments = args
        mapDialog.show(requireActivity().supportFragmentManager, "MapDialog")
    }

    private fun openDialogNotification() {

        val dialog = Dialog(requireContext(), com.business.zyvo.R.style.BottomSheetDialog)
        dialog?.apply {
            setCancelable(true)
            setContentView(com.business.zyvo.R.layout.dialog_notification_report_submit)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            autoOpenDialog2Job = viewLifecycleOwner.lifecycleScope.launch {
                delay(3000) // 3 seconds
                dismiss()
                openDialogSuccess()
            }


            var cross: ImageView = findViewById<ImageView>(com.business.zyvo.R.id.img_cross)
            var okBtn: RelativeLayout = findViewById<RelativeLayout>(R.id.rl_okay)
//            okBtn.setOnClickListener {
//                dialog.dismiss()
//            }
            cross.setOnClickListener {
                dialog.dismiss()
            }

            okBtn.setOnClickListener {
                autoOpenDialog2Job?.cancel()
                openDialogSuccess()
                dialog.dismiss()
            }
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),  // Width 90% of screen
                ViewGroup.LayoutParams.WRAP_CONTENT                   // Height wrap content
            )
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            show()
        }
    }

    private fun openDialogSuccess() {

        val dialog = Dialog(requireContext(), com.business.zyvo.R.style.BottomSheetDialog)
        dialog?.apply {
            setCancelable(true)
            setContentView(com.business.zyvo.R.layout.dialog_success_report_submit)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }

            var cross: ImageView = findViewById<ImageView>(com.business.zyvo.R.id.img_cross)
            var okBtn: RelativeLayout = findViewById<RelativeLayout>(com.business.zyvo.R.id.rl_okay)
            okBtn.setOnClickListener {
                dialog.dismiss()
            }
            cross.setOnClickListener {
                dialog.dismiss()
            }

            okBtn.setOnClickListener {
                dialog.dismiss()
            }
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),  // Width 90% of screen
                ViewGroup.LayoutParams.WRAP_CONTENT                   // Height wrap content
            )
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            show()
        }
    }

    fun initialization() {

        adapterAddon = AdapterAddOn(requireContext(), getAddOnList().subList(0, 4))

        adapterReview = AdapterReviewHost(requireContext(), mutableListOf())

        binding.recyclerAddOn.adapter = adapterAddon
        binding.recyclerAddOn.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerAddOn.isNestedScrollingEnabled = false

        binding.recyclerReviews.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.recyclerReviews.isNestedScrollingEnabled = false

        binding.recyclerReviews.adapter = adapterReview



        binding.showMoreReview.setOnClickListener {
            getReviewHost()
        }

        binding.tvLocationName.paintFlags =
            binding.tvLocationName.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        recyclerPagination()

    }

    private fun recyclerPagination() {

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

        binding.rlNeedHelp.visibility = View.GONE
        binding.imageVerifyCheck.visibility = View.GONE
        binding.imageStar1.visibility = View.VISIBLE
        binding.textRatingStar1.visibility = View.VISIBLE

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

    override fun onMapReady(p0: GoogleMap) {

        mMap = p0
        val newYork = LatLng(40.7128, -74.0060)
        mMap?.addMarker(MarkerOptions().position(newYork).title("Marker in New York")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_icon)))
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(newYork, 10f))

        //        val location =  LatLng(latitude, longitude)
        //        Log.d("TESTING","Map Ready LATITUDE IS "+latitude +"Map Ready LONGITUDE "+longitude)
        //        // Add a marker on the map at the given location
        //        location?.let { it1 -> MarkerOptions().position(it1).title("Marker in San Francisco") }
        //            ?.let { it2 -> mMap?.addMarker(it2) }

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

    private fun getReviewHost() {
        Log.d("TESTING_ZYVOO", "Here Inside GetReviewHost")
        lifecycleScope.launch {
            if (viewModel.currentPage < viewModel.totalPage && viewModel.hashMapPageNumber.containsKey(
                    viewModel.currentPage
                ) == false
            ) {
                LoadingUtils.showDialog(requireContext(), false)
                viewModel.filterPropertyReviewsHost(
                    propertyId, "highest_review", viewModel.currentPage
                ).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            LoadingUtils.hideDialog()
                            var pair = it.data
                            var jsonArr = pair?.first
                            var jsonObj = pair?.second
                            var list = mutableListOf<ReviewerProfileModel>()

                            jsonArr?.forEach {
                                val model: ReviewerProfileModel =
                                    Gson().fromJson(it.toString(), ReviewerProfileModel::class.java)
                                list.add(model)
                                viewModel.orgReviewList = list
                                viewModel.sortedByDescending(list)
                                viewModel.sortByAscendingOrder(list)
                            }


                            var totalPage = jsonObj?.get("total_pages")?.asInt
                            var currentPage = jsonObj?.get("current_page")?.asInt

                            if (currentPage != null) {
                                viewModel.currentPage = currentPage + 1;
                            }

                            viewModel.hashMapPageNumber.put(viewModel.currentPage, true)


                            if (totalPage != null) {
                                viewModel.totalPage = totalPage
                            }

                            Log.d(
                                "TESTING_GUEST_ID",
                                "Total Page :-" + viewModel.totalPage + " Current Page:- " + viewModel.currentPage
                            )

                            if (viewModel.totalPage <= viewModel.currentPage) {
                                binding.showMoreReview.visibility = View.GONE
                            }

                            adapterReview.updateAdapter(list)

                        }

                        is NetworkResult.Error -> {
                            LoadingUtils.hideDialog()
                        }

                        else -> {
                            LoadingUtils.hideDialog()
                        }
                    }
                }

            }

        }
    }


    @SuppressLint("MissingInflatedId")
    private fun acceptBooking() {
        val dialog = Dialog(requireContext(), R.style.BottomSheetDialog)
        dialog.apply {
            setCancelable(true)
            setContentView(R.layout.accept_booking_dialog)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }

            val tvShareMessage: EditText = findViewById(R.id.tvShareMessage)
            val rlAcceptRequestBtn: RelativeLayout = findViewById(R.id.rlAcceptRequestBtn)

            rlAcceptRequestBtn.setOnClickListener {
                val msg = tvShareMessage.text.toString()
                setUpAdapterMyBookings(bookingId,"approve",msg,"",
                    extensioId)
                dialog.dismiss()
            }
//            window?.setLayout(
//                (resources.displayMetrics.widthPixels * 0.9).toInt(),  // Width 90% of screen
//                ViewGroup.LayoutParams.WRAP_CONTENT                   // Height wrap content
//            )
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            show()
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun declineBooking() {
        val dialog = Dialog(requireContext(), R.style.BottomSheetDialog)
        dialog.apply {
            setCancelable(true)
            setContentView(R.layout.decline_booking_dialog)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
            val tvShareMessage: EditText = findViewById(R.id.tvShareMessage1)
            val doubt: RelativeLayout = findViewById(R.id.doubt)
            val tvAvailableDay: RelativeLayout = findViewById(R.id.tv_available_day)
            val tvOtherReason: RelativeLayout = findViewById(R.id.tv_other_reason)
            val otherReasonEt: EditText = findViewById(R.id.other_reason_et)
            val rlDeclineRequestBtn: RelativeLayout = findViewById(R.id.rlDeclineRequestBtn)

            var reason: String = "other"
            doubt.setOnClickListener {
                reason = "I'm overbooked"
                doubt.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box)
                tvAvailableDay.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
                tvOtherReason.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
            }
            otherReasonEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun onTextChanged(
                    charSequence: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    reason = charSequence.toString()
                    doubt.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
                    tvAvailableDay.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
                    tvOtherReason.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box)
                }

                override fun afterTextChanged(editable: Editable) {}
            });

            tvAvailableDay.setOnClickListener {
                reason = "Maintenance day"
                doubt.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
                tvOtherReason.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
                tvAvailableDay.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box)
            }

            rlDeclineRequestBtn.setOnClickListener {
                val msg = tvShareMessage.text.toString()

                setUpAdapterMyBookings(bookingId,"decline",msg,reason,
                    extensioId)
                dialog.dismiss()
            }
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            show()
        }
    }


    private fun setUpAdapterMyBookings(
        bookingId: Int,
        status: String,
        message: String,
        reason: String,
        extensioId:String
    ) {

        lifecycleScope.launch {
            LoadingUtils.showDialog(requireContext(), false)

            viewModel.approveDeclineBooking(bookingId, status, message, reason,
                extensioId)
                .collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            LoadingUtils.hideDialog()

                            callingBookingDetailApi()
                            LoadingUtils.showSuccessDialog(
                                requireContext(),
                                it.data.toString()
                            )

                        }

                        is NetworkResult.Error -> {
                            LoadingUtils.hideDialog()
                            LoadingUtils.showErrorDialog(
                                requireContext(),
                                it.message.toString()
                            )
                        }

                        else -> {

                        }
                    }
                }

        }
    }

}


