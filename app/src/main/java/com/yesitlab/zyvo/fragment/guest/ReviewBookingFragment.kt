package com.yesitlab.zyvo.fragment.guest

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RatingBar
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.yesitlab.zyvo.ObservableTextView
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.adapter.AdapterAddOn
import com.yesitlab.zyvo.adapter.guest.AdapterReview
import com.yesitlab.zyvo.databinding.FragmentReviewBookingBinding
import java.time.YearMonth


class ReviewBookingFragment : Fragment() , OnMapReadyCallback {
  private var _binding : FragmentReviewBookingBinding? = null
    private  val binding get() = _binding!!
    private var param1: String? = null
    private var param2: String? = null

    lateinit var adapterAddon: AdapterAddOn
    lateinit var adapterReview: AdapterReview
    private lateinit var mapView: MapView
    private var mMap: GoogleMap? = null
lateinit var  navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
           // param1 = it.getString(ARG_PARAM1)
        //    param2 = it.getString(ARG_PARAM2)
        }
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
            if(binding.tvHostRule.visibility == View.VISIBLE){
                binding.tvHostRule.visibility= View.GONE
            }else{
                binding.tvHostRule.visibility = View.VISIBLE
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

            binding.textReviewClick.text ="Sort by: Highest Review"
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemLowestReview).setOnClickListener {
            binding.textReviewClick.text ="Sort by: Lowest Review"
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemRecentReview).setOnClickListener {
            binding.textReviewClick.text ="Sort by: Recent Review"
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
            var ratingbar =  findViewById<RatingBar>(R.id.ratingbar)
            var ratingbar2 =  findViewById<RatingBar>(R.id.ratingbar2)
            var ratingbar3 =  findViewById<RatingBar>(R.id.ratingbar3)
            var textPublishReview =  findViewById<TextView>(R.id.textPublishReview)
            var etMessage =  findViewById<TextView>(R.id.etMessage)



            textPublishReview.setOnClickListener{

                dismiss()
            }


            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }}

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
    override fun onMapReady(p0: GoogleMap) {

        mMap = p0
        val newYork = LatLng(40.7128, -74.0060)
        mMap?.addMarker(MarkerOptions().position(newYork).title("Marker in New York"))
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(newYork, 10f))

    }

}