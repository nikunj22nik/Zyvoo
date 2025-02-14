package com.business.zyvo.fragment.host

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RatingBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.business.zyvo.AppConstant
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.business.zyvo.DateManager.DateManager
import com.business.zyvo.LoadingUtils
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.adapter.AdapterAddOn
import com.business.zyvo.adapter.guest.AdapterReview
import com.business.zyvo.adapter.host.AdapterIncludeInBooking
import com.business.zyvo.databinding.FragmentReviewBookingBinding
import com.business.zyvo.model.host.hostdetail.HostDetailModel
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.PrepareData
import com.business.zyvo.viewmodel.host.HostBookingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReviewBookingHostFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentReviewBookingBinding? = null
    private val binding get() = _binding!!
    private var bookingId: Int = -1
    lateinit var adapterAddon: AdapterAddOn
    lateinit var adapterReview: AdapterReview
    private lateinit var mapView: MapView
    private lateinit var viewModel: HostBookingsViewModel
    private var mMap: GoogleMap? = null
    lateinit var navController: NavController
    lateinit var adapterIncludeInBooking: AdapterIncludeInBooking


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.containsKey(AppConstant.BOOKING_ID)) {
                bookingId = it.getInt(AppConstant.BOOKING_ID)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_review_booking, container, false
        )

        adapterIncludeInBooking = AdapterIncludeInBooking(mutableListOf(), requireContext())

        val gridLayoutManager = GridLayoutManager(requireContext(), PrepareData.numberOFColumn(requireActivity()))

        binding.recyclerIcludeBooking.layoutManager = gridLayoutManager
        binding.recyclerIcludeBooking.adapter = adapterIncludeInBooking


        binding.textMessageTheHostButton.text = "Message the Guest"
        binding.rlNeedHelp.visibility = View.GONE

        binding.imageVerifyCheck.visibility = View.GONE
        binding.imageStar1.visibility = View.VISIBLE
        binding.textRatingStar1.visibility = View.VISIBLE

        viewModel = ViewModelProvider(this)[HostBookingsViewModel::class.java]

        if (bookingId != -1) {
            Log.d("TESTING","Booking Id is "+ bookingId)
          callingBookingDetailApi()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        initialization()

        binding.imageBackIcon.setOnClickListener {
            navController.navigateUp()
        }


        binding.textReportIssueButton.setOnClickListener {
            dialogReportIssue()
        }

        binding.textReviewBookingButton.setOnClickListener {
            dialogReview()
        }

        binding.textMessageTheHostButton.setOnClickListener {
            findNavController().navigate(R.id.hostChatFragment)
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



    }



    private fun callingBookingDetailApi() {
        lifecycleScope.launch {
            var sessionManager = SessionManager(requireContext())
            var latitude = sessionManager.getLatitude()
            var longitude = sessionManager.getLongitude()
            LoadingUtils.showDialog(requireContext(), false)
            if (latitude.equals("") || longitude.equals("")) {
                viewModel.hostBookingDetails(bookingId, null, null).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            LoadingUtils.hideDialog()
                            it.data?.second?.let { it1 -> showingDataToUi(it1) }
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
                viewModel.hostBookingDetails(bookingId, latitude, longitude).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            LoadingUtils.hideDialog()
                            it.data?.let { it1 -> showingDataToUi(it1.second) }
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
            // viewModel.hostBookingDetails()
        }

    }


    private fun showingDataToUi(data: HostDetailModel) {

        binding.tvNamePlace.setText(data.property_title)
        binding.textRatingStar.setText(data.guest_rating)
        binding.textK.setText(data.reviews_total_rating)
        binding.textMiles.setText(data.distance_miles + " miles away")
        binding.time.setText(data.booking_hour)
        binding.money.setText("$ " + data.booking_amount)

        data.cleaning_fee?.let {
            binding.tvCleaningFee.setText("$" + it)
        } ?: binding.tvCleaningFee.setText("$ 0")

        data.service_fee?.let {
            binding.tvServiceFee.setText("$" + it)
        } ?: binding.tvServiceFee.setText("$ 0")

        data.tax?.let {
            binding.tvTaxes.setText("$ " + it)
        } ?: binding.tvTaxes.setText("$ 0")

        data.add_on_total?.let {
            binding.tvAddOn.setText("$ " + it)
        } ?: binding.tvAddOn.setText("$ 0")

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
            binding.tvStatus.setText(it)
        } ?: binding.tvStatus.setText("")

        data.booking_date?.let {
            binding.tvBookingDate.setText(it)
        } ?: binding.tvBookingDate.setText("")

        data.booking_hour?.let {
            binding.tvBookingTotalTime.setText(it)
        } ?: binding.tvBookingTotalTime.setText("")

        binding.bookingFromTo.setText("From " + data.booking_start_time + " to " + data.booking_end_time)


        data.parking_rules?.let {
            binding.tvParkingContent.setText(it)
        }?: binding.tvParkingContent.setText("")


        data.host_rules?.let {
            binding.tvHostContent.setText(it)
        }?: binding.tvHostContent.setText("")

        adapterIncludeInBooking.updateAdapter(data.amenities)

        binding.tvLocationName.setText(data.address)

        data.latitude?.let {
            val location = data.longitude?.let { it1 -> LatLng(data.latitude.toDouble(), it1.toDouble()) }

            // Add a marker on the map at the given location
            location?.let { it1 -> MarkerOptions().position(it1).title("Marker in San Francisco") }
                ?.let { it2 -> mMap?.addMarker(it2) }
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
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemLowestReview).setOnClickListener {
            binding.textReviewClick.text = "Sort by: Lowest Review"
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemRecentReview).setOnClickListener {
            binding.textReviewClick.text = "Sort by: Recent Review"
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
            setCancelable(false)
            setContentView(R.layout.dialog_review)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            var ratingbar = findViewById<RatingBar>(R.id.ratingbar)
            var ratingbar2 = findViewById<RatingBar>(R.id.ratingbar2)
            var ratingbar3 = findViewById<RatingBar>(R.id.ratingbar3)
            var textPublishReview = findViewById<TextView>(R.id.textPublishReview)
            var etMessage = findViewById<TextView>(R.id.etMessage)



            textPublishReview.setOnClickListener {

                dismiss()
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

            submit.setOnClickListener {
                if (txtSubmit.text.toString().trim().equals("Submitted") == false) {
                    txtSubmit.setText("Submitted")
                } else {
                    dialog.dismiss()
                    openDialogNotification()
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

    private fun openDialogNotification() {

        val dialog = Dialog(requireContext(), R.style.BottomSheetDialog)
        dialog?.apply {
            setCancelable(true)
            setContentView(R.layout.dialog_notification_report_submit)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }

            var cross: ImageView = findViewById<ImageView>(R.id.img_cross)
            var okBtn: RelativeLayout = findViewById<RelativeLayout>(R.id.rl_okay)
            okBtn.setOnClickListener {
                dialog.dismiss()
            }
            cross.setOnClickListener {
                dialog.dismiss()
            }

            okBtn.setOnClickListener {
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

        val dialog = Dialog(requireContext(), R.style.BottomSheetDialog)
        dialog?.apply {
            setCancelable(true)
            setContentView(R.layout.dialog_success_report_submit)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }

            var cross: ImageView = findViewById<ImageView>(R.id.img_cross)
            var okBtn: RelativeLayout = findViewById<RelativeLayout>(R.id.rl_okay)
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

        adapterReview = AdapterReview(requireContext(), mutableListOf())





        binding.recyclerAddOn.adapter = adapterAddon

        binding.recyclerAddOn.layoutManager = GridLayoutManager(requireContext(), 2)

        binding.recyclerAddOn.isNestedScrollingEnabled = false

        binding.recyclerReviews.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.recyclerReviews.isNestedScrollingEnabled = false

        binding.recyclerReviews.adapter = adapterReview




        binding.tvLocationName.paintFlags =
            binding.tvLocationName.paintFlags or Paint.UNDERLINE_TEXT_FLAG


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
        mMap?.addMarker(MarkerOptions().position(newYork).title("Marker in New York"))
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(newYork, 10f))

    }

}