package com.business.zyvo.fragment.guest.hostDetails

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.BuildConfig
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.OnClickListener
import com.business.zyvo.OnClickListener1
import com.business.zyvo.OnItemClickListener
import com.business.zyvo.R
import com.business.zyvo.activity.guest.propertydetails.RestaurantDetailActivity
import com.business.zyvo.activity.guest.propertydetails.model.Pagination
import com.business.zyvo.activity.guest.propertydetails.model.PropertyData
import com.business.zyvo.activity.guest.propertydetails.model.Review
import com.business.zyvo.adapter.HostListingAdapter
import com.business.zyvo.adapter.guest.AdapterProReview

import com.business.zyvo.adapter.guest.AdapterReview
import com.business.zyvo.databinding.FragmentHostDetailsBinding
import com.business.zyvo.fragment.both.viewImage.ViewImageDialogFragment
import com.business.zyvo.fragment.guest.hostDetails.model.HostData
import com.business.zyvo.fragment.guest.hostDetails.model.HostListingModel
import com.business.zyvo.fragment.guest.hostDetails.model.Property
import com.business.zyvo.fragment.guest.hostDetails.viewModel.HostDetailsViewModel
import com.business.zyvo.fragment.host.payments.model.GetBookingResponse
import com.business.zyvo.fragment.host.payments.viewModel.PaymentsViewModel
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.ErrorDialog.formatConvertCount
import com.business.zyvo.utils.NetworkMonitorCheck
import com.business.zyvo.viewmodel.HostListingViewModel
import com.business.zyvo.viewmodel.ImagePopViewModel
import com.business.zyvo.viewmodel.WishlistViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


@AndroidEntryPoint
class HostDetailsFragment : Fragment(), OnClickListener, OnClickListener1 {
    private var _binding: FragmentHostDetailsBinding? = null
    private val binding get() = _binding!!
    // lateinit var adapterReview: AdapterReview

    lateinit var navController: NavController
    private lateinit var adapter: HostListingAdapter
    lateinit var adapterReview: AdapterProReview
    var hostID: String = "-1"
    lateinit var sessionManager: SessionManager
    var pagination:Pagination?=null
    var propertyList : MutableList<Property> = mutableListOf()
    var reviewList: MutableList<Review> = mutableListOf()
    var filter = "highest_review"
    val viewModel: HostDetailsViewModel by lazy {
        ViewModelProvider(this)[HostDetailsViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            hostID = it.getString(AppConstant.HOST_ID) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHostDetailsBinding.inflate(
            LayoutInflater.from(requireContext()),
            container,
            false
        )
        sessionManager = SessionManager(requireContext())
        lifecycleScope.launch {
            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    LoadingUtils.showDialog(requireContext(), false)
                } else {
                    LoadingUtils.hideDialog()
                }
            }
        }

        adapterReview = AdapterProReview(requireContext(), reviewList)
        binding.recyclerReviews.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerReviews.isNestedScrollingEnabled = false
        binding.recyclerReviews.adapter = adapterReview

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialization()
        Log.d("hostId", hostID)
        binding.textAboutDescription.setCollapsedTextColor(R.color.green_color_bar)
        navController = Navigation.findNavController(view)
        binding.imgBack.setOnClickListener {
            navController.navigateUp()
        }

        binding.textReviewClick.setOnClickListener {
            showPopupWindow(it,0)
        }
        hostDetailsList()
    }

    @SuppressLint("MissingInflatedId", "SetTextI18n")
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

            binding.textReviewClick?.text ="Sort by: Highest Review"
            sortReviewsBy("Highest")
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemLowestReview).setOnClickListener {
            binding.textReviewClick?.text ="Sort by: Lowest Review"
            sortReviewsBy("Lowest")
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemRecentReview).setOnClickListener {
            binding.textReviewClick?.text ="Sort by: Recent Review"
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

    private fun hostDetailsList() {
        lifecycleScope.launch {
            viewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        hostDetailsListApi()
                    }

                }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun hostDetailsListApi() {
        lifecycleScope.launch {
            viewModel.hostListing(
                hostID,
                sessionManager.getGustLatitude(),
                sessionManager.getGustLongitude()
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        it.data?.let { resp ->
                            pagination = Gson().fromJson(resp.second.getAsJsonObject("pagination") , Pagination::class.java)
                            val model = Gson().fromJson(resp.first.getAsJsonObject("data"),
                                HostData::class.java)
                            Log.d("modelResponse", it.data.toString())
                            Log.d("modelResponse", model.toString())
                            model.let {
                                Glide.with(requireContext())
                                    .load(BuildConfig.MEDIA_URL + it.host?.profile_picture)
                                    .error(R.drawable.ic_circular_img_user)
                                    .into(binding.imageProfilePicture)

                                binding.textListing.setText(it.host?.name+"'s Listings")
                                binding.textHostName.setText(it.host?.name)


                                it.about_host?.host_profession?.let { professions ->
                                    if (professions.isNotEmpty()) {
                                        binding.textMyWorkName.visibility = View.VISIBLE
                                        val firstProfession = professions.getOrNull(0)
                                        val secondProfession = professions.getOrNull(1)

                                        binding.textMyWorkName.text = when {
                                            firstProfession != null && secondProfession != null -> "$firstProfession, $secondProfession"
                                            firstProfession != null -> firstProfession
                                            else -> ""
                                        }
                                    }else{
                                        binding.textMyWorkName.visibility = View.GONE
                                    }
                                }


                                /*if (it.about_host?.host_profession != null
                                    && it.about_host?.host_profession.isNotEmpty()){
                                    if (it.about_host?.host_profession?.get(0) != null && it.about_host?.host_profession[1] != null) {
                                        binding.textMyWorkName.setText(it.about_host.host_profession[0] + " ," + it.about_host.host_profession[1])
                                    } else if (it.about_host?.host_profession?.get(0) != null) {
                                        binding.textMyWorkName.setText(it.about_host.host_profession[0])
                                    }
                                }*/

                                if (it.about_host?.location != null) {
                                    if (it.about_host?.location.isNotEmpty()){
                                        binding.textLocationName.visibility = View.VISIBLE
                                        binding.textLocationName.setText(it.about_host.location)
                                    }else{
                                        binding.textLocationName.visibility = View.GONE
                                    }

                                }else{
                                    binding.textLocationName.visibility = View.GONE
                                }
                                /*if (it.about_host?.language != null && it.about_host?.language.isNotEmpty()){
                                    if (it.about_host.language.get(0) != null && it.about_host?.language[1] != null) {
                                        binding.textLanguagesName.setText(it.about_host.language[0] + " ," + it.about_host.language[1])
                                    } else if (it.about_host?.language?.get(0) != null) {
                                        binding.textLanguagesName.setText(it.about_host.language[0])
                                    }
                                }*/

                                it.about_host?.language?.let { languages ->
                                    if (languages.isNotEmpty()) {
                                        binding.textLanguagesName.visibility = View.VISIBLE
                                        val firstLanguage = languages.getOrNull(0)
                                        val secondLanguage = languages.getOrNull(1)

                                        binding.textLanguagesName.text = when {
                                            firstLanguage != null && secondLanguage != null -> "$firstLanguage, $secondLanguage"
                                            firstLanguage != null -> firstLanguage
                                            else -> ""
                                        }
                                    }
                                    else{
                                        binding.textLanguagesName.visibility = View.GONE
                                    }
                                }


                                if (it.about_host?.description != null) {
                                    binding.textAboutDescription.setText(it.about_host?.description)
                                }

                                it.properties?.let { it1 -> adapter.updateItem(it1)
                                }
                                if (it.properties != null){
                                    propertyList = it.properties
                                }

                                binding.textViewMore.setOnClickListener {
                                    val gson = Gson()
                                    val jsonList = gson.toJson(propertyList)
                                    val bundle = Bundle()
                                    bundle.putString(AppConstant.propertyList, jsonList)
                                    findNavController().navigate(R.id.listingFragment,bundle)
                                }

                                it.total_host_review_count?.let {
                                    binding.tvReviewTotal.text = "Reviews"+" ("+ formatConvertCount(it).trim()+")"
                                }
                                it.total_host_review_rating?.let {
                                    binding.proTotalrating.text = it.trim()
                                }

                                if(pagination == null){
                                    binding.showMoreReview.visibility = View.GONE
                                }
                                if(it?.total_host_review_count.equals("0")) binding.showMoreReview.visibility = View.GONE

                                pagination?.let {
                                    Log.d("PAGES_TOTAL","TOTAL PAGES :- "+it.total_pages +" "+"Current Pages:- "+ it.current_page)
                                    if (it.total_pages <= it.current_page) {
                                        binding.showMoreReview.visibility = View.GONE
                                    }

                                }

                            }
                            val listType = object : TypeToken<List<Review>>() {}.type
                            reviewList = Gson().fromJson(resp.second.getAsJsonArray("data"), listType)
                            reviewList?.let {
                                if (it.isNotEmpty()){
                                    adapterReview.updateAdapter(it)
                                }
                            }
                        }
                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(requireContext(), it.message!!)

                    }
                    else -> {

                    }

                }
            }
        }


    }


    fun initialization() {
        adapter = HostListingAdapter(requireContext(), 1, mutableListOf(), this)
        binding.recyclerViewBooking.adapter = adapter


        binding.showMoreReview.setOnClickListener {
            pagination?.let {
                if (it.current_page!=it.total_pages && it.current_page<it.total_pages) {
                    loadMoreReview(filter,(it.current_page+1).toString())
                }
            }
        }
    }

    private fun loadMoreReview(filter :String,
                               page :String) {
        if (NetworkMonitorCheck._isConnected.value)   {
            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.filterHostReviews(hostID,
                    sessionManager.getGustLatitude(),
                    sessionManager.getGustLongitude(),
                    filter,
                    page).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                val listType = object : TypeToken<List<Review>>() {}.type
                                val localreviewList:MutableList<Review> = Gson().fromJson(resp.first, listType)
                                pagination = Gson().fromJson(resp.second,
                                    Pagination::class.java)
                                pagination?.let {
                                    Log.d("PAGES_TOTAL","TOTAL PAGES :- "+it.total +" "+"Current Pages:- "+ it.current_page)
                                }
                                if(pagination == null){
                                    binding.reviewMoreView.visibility = View.GONE
                                }

                                pagination?.let {
                                    if(it.total_pages <= it.current_page){
                                        binding.showMoreReview.visibility = View.GONE
                                    }
                                }
                                reviewList.addAll(localreviewList)
                                reviewList?.let {
                                    if (it.isNotEmpty()){
                                        adapterReview.updateAdapter(it)
                                        binding.tvReviewTotal.text = "Reviews"+" ("+ formatConvertCount(reviewList.size.toString()).trim()+")"
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

    override fun itemClick(obj: Int) {

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun itemClick(propertyId: Int, miles: String) {
        if (propertyId != 0){
            val intent = Intent(requireActivity(), RestaurantDetailActivity::class.java)
            intent.putExtra("propertyId",propertyId.toString())
            intent.putExtra("propertyMile",miles)
            startActivity(intent)
        }
    }
}