package com.business.zyvo.activity.guest.propertydetails
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.CircularSeekBar.OnSeekBarChangeListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.business.zyvo.DateManager.DateManager
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.OnClickListener
import com.business.zyvo.R
import com.business.zyvo.activity.guest.checkout.CheckOutPayActivity
import com.business.zyvo.activity.guest.propertydetails.model.AddOn
import com.business.zyvo.activity.guest.propertydetails.model.Pagination
import com.business.zyvo.activity.guest.propertydetails.model.PropertyData
import com.business.zyvo.activity.guest.propertydetails.model.Review
import com.business.zyvo.activity.guest.propertydetails.viewmode.PropertyDetailsViewModel
import com.business.zyvo.adapter.WishlistAdapter
import com.business.zyvo.adapter.guest.AdapterProAddOn
import com.business.zyvo.adapter.guest.AdapterProAddOn.onItemClickListener
import com.business.zyvo.adapter.guest.AdapterProReview
import com.business.zyvo.adapter.guest.PropertyIncludedAdapter
import com.business.zyvo.databinding.ActivityRestaurantDetailBinding
import com.business.zyvo.fragment.both.viewImage.ViewImageDialogFragment
import com.business.zyvo.fragment.guest.home.model.WishlistItem
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.ErrorDialog.convertHoursToHrMin
import com.business.zyvo.utils.ErrorDialog.formatConvertCount
import com.business.zyvo.utils.ErrorDialog.showToast
import com.business.zyvo.utils.NetworkMonitorCheck
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale


@AndroidEntryPoint
class RestaurantDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var binding: ActivityRestaurantDetailBinding
    lateinit var adapterAddon: AdapterProAddOn
    lateinit var adapterReview: AdapterProReview
    private lateinit var mapView: MapView
    private val viewModel: PropertyDetailsViewModel by viewModels()
    private var mMap: GoogleMap? = null
    private var propertyId = ""
    private var propertyMile = ""
    var session: SessionManager?=null
    var propertyData:PropertyData?=null
    var pagination:Pagination?=null
    var reviewList: MutableList<Review> = mutableListOf()
    var addOnList: MutableList<AddOn> = mutableListOf()
    var filter = "highest_review"
    private var wishlistItem: MutableList<WishlistItem> = mutableListOf()
    private var currentMonth: YearMonth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        YearMonth.now()
    } else {
        TODO("VERSION.SDK_INT < O")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private var selectedDate: LocalDate? = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        intent.extras?.let {
            propertyId = intent.extras?.getString("propertyId")!!
            propertyMile = intent.extras?.getString("propertyMile")!!
          //  var status: String = intent.getStringExtra("key_name").toString()
           // Log.d(ErrorDialog.TAG, status)
        }
        Log.d(ErrorDialog.TAG, "I am in Restaurent Details")
        binding = ActivityRestaurantDetailBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        session = SessionManager(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.circularSeekBar.rotateToDot(7)

        disableScrollViewScrollForChildView(binding.rlCircularProgress, binding.scrollView)
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Observe the isLoading state
        lifecycleScope.launch {
            viewModel.isLoading.observe(this@RestaurantDetailActivity) { isLoading ->
                if (isLoading) {
                    LoadingUtils.showDialog(this@RestaurantDetailActivity, false)
                } else {
                    LoadingUtils.hideDialog()
                }
            }
        }

        initialization()
        share()
        updateCalendar()
        clickListeners1()

        binding.imageInfo.setOnClickListener {
            showPopupWindowForPets(it)
        }

        binding.imageInfo1.setOnClickListener {
            showPopupWindowForPets(it)
        }
        binding.imageShare.setOnClickListener {
            shareApp()
        }

        binding.rlTextReviewClick.setOnClickListener {
            showPopupWindow(it,0)
        }

        getHomePropertyDetails()
    }

    @SuppressLint("SetTextI18n")
    private fun getHomePropertyDetails() {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.getHomePropertyDetails(session?.getUserId().toString(),
                    propertyId).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                propertyData = Gson().fromJson(resp.first.getAsJsonObject("data"), PropertyData::class.java)

                                pagination = Gson().fromJson(resp.first.getAsJsonObject("pagination") , Pagination::class.java)

                                if(pagination == null){
                                    binding.showMoreReview.visibility = View.GONE
                                }
                                if(propertyData?.reviews_total_count.equals("0")) binding.showMoreReview.visibility = View.GONE

                                pagination?.let {
                                    Log.d("PAGES_TOTAL","TOTAL PAGES :- "+it.total +" "+"Current Pages:- "+ it.current_page)

                                    if (it.total <= it.current_page) {
                                        binding.showMoreReview.visibility = View.GONE
                                    }

                                }

                                val listType = object : TypeToken<List<Review>>() {}.type
                                reviewList = Gson().fromJson(resp.second.getAsJsonArray("data"), listType)
                                setPropertyData()
                            }
                        }
                        is NetworkResult.Error -> {
                            showErrorDialog(this@RestaurantDetailActivity, it.message!!)
                        }

                        else -> {
                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                        }
                    }
                }
            }
        }else{
            showErrorDialog(this,
                resources.getString(R.string.no_internet_dialog_msg))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setPropertyData() {
        try {
            propertyData?.let {
                propertyData?.property_title?.let {
                    binding.proName.text = it
                }
                propertyData?.reviews_total_rating?.let {
                    binding.proRating.text = it
                    binding.proTotalrating.text = it
                }
                propertyData?.reviews_total_count?.let {
                    binding.proreviewCount.text = "("+ formatConvertCount(it) +" reviews)"
                    binding.proTotalReview.text = "Reviews "+"("+formatConvertCount(it) +")"
                }
                propertyData?.min_booking_hours?.let {
                    binding.proBookingMin.text = convertHoursToHrMin(it.toDouble())
                }
                propertyData?.property_size?.let {
                    binding.proSqr.text = "$it sqft"
                }
                propertyData?.is_in_wishlist?.let {
                    if (it==1){
                        binding.proAddWishLists.visibility = View.VISIBLE
                        binding.proNoWishLists.visibility = View.GONE
                    }else{
                        binding.proAddWishLists.visibility = View.GONE
                        binding.proNoWishLists.visibility = View.VISIBLE
                    }
                }

                propertyData?.images?.let {
                    if (it.isNotEmpty()){
                        if (it.size==1){
                            binding.cvTwoAndThreeImage.visibility = View.GONE
                            binding.cvOneImage.visibility = View.VISIBLE
                            binding.llThreeImage.visibility = View.GONE
                            binding.llTwoImage.visibility = View.GONE
                            binding.proImageMore.visibility = View.GONE
                            Glide.with(this@RestaurantDetailActivity).load(AppConstant.BASE_URL + it.get(0)).into(binding.proImageViewOne)
                        }
                        if (it.size==2){
                            binding.cvTwoAndThreeImage.visibility = View.VISIBLE
                            binding.cvOneImage.visibility = View.GONE
                            binding.llThreeImage.visibility = View.GONE
                            binding.llTwoImage.visibility = View.VISIBLE
                            binding.proImageMore.visibility = View.GONE
                            Glide.with(this@RestaurantDetailActivity).load(AppConstant.BASE_URL + it.get(0)).into(binding.proImageViewTwoAndThree)
                            Glide.with(this@RestaurantDetailActivity).load(AppConstant.BASE_URL + it.get(1)).into(binding.proImageTwo)
                        }
                        if (it.size==3){
                            binding.cvTwoAndThreeImage.visibility = View.VISIBLE
                            binding.cvOneImage.visibility = View.GONE
                            binding.llThreeImage.visibility = View.VISIBLE
                            binding.llTwoImage.visibility = View.GONE
                            binding.proImageMore.visibility = View.GONE
                            Glide.with(this@RestaurantDetailActivity).load(AppConstant.BASE_URL + it.get(0)).into(binding.proImageViewTwoAndThree)
                            Glide.with(this@RestaurantDetailActivity).load(AppConstant.BASE_URL + it.get(1)).into(binding.prImageTwo)
                            Glide.with(this@RestaurantDetailActivity).load(AppConstant.BASE_URL + it.get(2)).into(binding.prImageThree)
                        }
                        if (it.size>=4){
                            binding.cvTwoAndThreeImage.visibility = View.VISIBLE
                            binding.cvOneImage.visibility = View.GONE
                            binding.llThreeImage.visibility = View.VISIBLE
                            binding.llTwoImage.visibility = View.GONE
                            binding.proImageMore.visibility = View.VISIBLE
                            Glide.with(this@RestaurantDetailActivity).load(AppConstant.BASE_URL + it.get(0)).into(binding.proImageViewTwoAndThree)
                            Glide.with(this@RestaurantDetailActivity).load(AppConstant.BASE_URL + it.get(1)).into(binding.prImageTwo)
                            Glide.with(this@RestaurantDetailActivity).load(AppConstant.BASE_URL + it.get(2)).into(binding.prImageThree)
                        }
                    }
                }
                propertyData?.hourly_rate?.let {
                    binding.proPriceHr.text = "$it/hr"
                    binding.textPrice.text = it
                    val totalPrice = binding.textHr.text.toString().replace(" hour","")
                        .toInt()* it.toFloat()
                    binding.textPrice.text = totalPrice.toString()

                }
                propertyData?.bulk_discount_hour?.let {
                    binding.textHourDiscount.text = "$it+ hour discount"
                }
                propertyData?.bulk_discount_rate?.let {
                    binding.proDiscount.text = "$it% Off"
                }

                propertyData?.property_description?.let {
                    binding.tvReadMoreLess.apply {
                        text =it
                        post {
                            setTrimLength(20) // Set max character length before collapsing
                            setCollapsedText("Read More") // Text for collapsed state
                            setExpandedText("Read Less") // Text for expanded state
                        }
                        binding.tvReadMoreLess.setCollapsedTextColor(R.color.green_color_bar)
                    }
                }
                propertyData?.amenities?.let {
                    if (it.isNotEmpty()) {
                        val propertyIncludedAdapter = PropertyIncludedAdapter(this, it)
                        binding.proRvIncluded.adapter = propertyIncludedAdapter

                    }
                }
                propertyData?.parking_rules?.let {
                    binding.proParkingRule.text = it
                }
                propertyData?.host_rules?.let {
                    binding.proHostRule.text = it
                }
                propertyData?.add_ons?.let {
                    if (it.isNotEmpty()) {
                        addOnList = it.toMutableList()
                        adapterAddon.updateAdapter(addOnList)
                        Log.d("CheckAddOn",addOnList.toString())
                    }
                }
                propertyData?.address?.let {
                    binding.tvLocationName.text = it
                }
                // Add a marker in New York and move the camera
                    if (!propertyData?.latitude.equals("") && !propertyData?.longitude.equals("")){
                        val newYork = LatLng(propertyData?.latitude!!.toDouble(),
                            propertyData?.longitude!!.toDouble())
                        mMap?.addMarker(MarkerOptions().position(newYork).title("Marker in ${propertyData?.address}"))
                        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(newYork, 12f))
                        //    mMap?.clear()
                        // Apply custom style to the map
                        val success: Boolean = mMap!!.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style)
                        )
                        if (!success) {
                            Log.e(ErrorDialog.TAG, "Style parsing failed.")
                        }
                    }

                reviewList?.let {
                    if (it.isNotEmpty()){
                        adapterReview.updateAdapter(it)
                    }
                }

            }
        }catch (e:Exception){
            Log.d(ErrorDialog.TAG,e.message.toString())
        }

    }

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    private fun showPopupWindow(anchorView: View, position: Int) {
        // Inflate the custom layout for the popup menu
        val popupView =
            LayoutInflater.from(this).inflate(R.layout.popup_positive_review, null)

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
    private fun shareApp() {
        val appPackageName = packageName
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
            "Highest" -> reviewList.sortByDescending { it.review_rating }
            "Lowest" -> reviewList.sortBy { it.review_rating }
            "Recent" -> reviewList.sortByDescending { it.review_date }
        }
        adapterReview.updateAdapter(reviewList)
    }

    private fun showPopupWindowForPets(anchorView: View) {
        // Inflate the popup layout
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_layout_pets, null)

        // Create the PopupWindow
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Show the popup window at the bottom right of the TextView

        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.showAsDropDown(anchorView, anchorView.width, 0)
    }


    fun clickListeners1() {
        binding.rlParking.setOnClickListener {
            if (binding.rlParkingView.visibility == View.VISIBLE) {
                binding.rlParkingView.visibility = View.GONE
            } else {
                binding.rlParkingView.visibility = View.VISIBLE
            }
        }
        binding.rlHostRule.setOnClickListener {
            if (binding.rlHostRuleView.visibility == View.VISIBLE) {
                binding.rlHostRuleView.visibility = View.GONE
            } else {
                binding.rlHostRuleView.visibility = View.VISIBLE
            }
        }

        binding.proNoWishLists.setOnClickListener {
            showAddWishlistDialog()
        }
        binding.proAddWishLists.setOnClickListener {
            removeItemFromWishlist(propertyId)
        }
        binding.llHotelViews.setOnClickListener {
            val dialogFragment = ViewImageDialogFragment()
            propertyData?.images.let {
                val bundle = Bundle().apply {
                    putStringArrayList("image_list",java.util.ArrayList(it))
                }
                dialogFragment.arguments =bundle
                dialogFragment.show(supportFragmentManager, "exampleDialog")
            }
        }
        binding.proImageMore.setOnClickListener {
            val dialogFragment = ViewImageDialogFragment()
            propertyData?.images.let {
                val bundle = Bundle().apply {
                    putStringArrayList("image_list",java.util.ArrayList( it))
                }
                dialogFragment.arguments =bundle
                dialogFragment.show(supportFragmentManager, "exampleDialog")
            }
        }
        binding.circularSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener
        {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(progress: String) {
                try {
                    binding.textHr.text = "$progress hour"
                    propertyData?.hourly_rate?.let {
                        val totalPrice = progress.toInt()* it.toFloat()
                        binding.textPrice.text = totalPrice.toString()
                        var selectedTime = binding.textstart.text

                        // Define the time formatter (12-hour format with AM/PM)
                        val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
                        } else {
                            TODO("VERSION.SDK_INT < O")
                        }
                        Log.d(ErrorDialog.TAG,selectedTime.toString())
                        // Parse the start time string into a LocalTime object
                        val startTime = LocalTime.parse(selectedTime, formatter)

                        val endTime = startTime.plusHours(binding.textHr.text.toString().replace(" hour","")
                            .toLong())
                        // Format the end time back to a string
                        val formattedEndTime = endTime.format(formatter)
                        binding.textend.text = formattedEndTime.uppercase()
                    }
                }catch (e:Exception){
                    Log.d(ErrorDialog.TAG,e.message!!)
                }
            }

        })

    }

    private fun showAddWishlistDialog() {
        val dialog = Dialog(this, R.style.BottomSheetDialog)
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_add_wishlist)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            val dialogAdapter = WishlistAdapter(this@RestaurantDetailActivity,
                true, wishlistItem,object:
                OnClickListener {
                override fun itemClick(obj: Int) {

                }

            })

            dialogAdapter.setOnItemClickListener(object :WishlistAdapter.onItemClickListener{
                override fun onItemClick(position: Int, wish: WishlistItem) {
                    saveItemInWishlist(propertyId, position,wish.wishlist_id.toString(),
                        dialog)
                }

            })

            val rvWishList: RecyclerView = findViewById<RecyclerView>(R.id.rvWishList)
            rvWishList.adapter = dialogAdapter

            findViewById<ImageView>(R.id.imageCross).setOnClickListener {
                dismiss()
            }
            findViewById<TextView>(R.id.textCreateWishList).setOnClickListener {
                createWishListDialog()
                dismiss()
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
            getWisList(dialogAdapter)
        }
    }

    private fun saveItemInWishlist(property_id: String,pos: Int,
                                   wishlist_id: String,dialog: Dialog) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.saveItemInWishlist(session?.getUserId().toString(),
                    property_id,
                    wishlist_id).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                showToast(this@RestaurantDetailActivity,resp.first)
                                dialog.dismiss()
                                binding.proAddWishLists.visibility = View.VISIBLE
                                binding.proNoWishLists.visibility = View.GONE

                            }
                        }
                        is NetworkResult.Error -> {
                            showErrorDialog(this@RestaurantDetailActivity, it.message!!)
                        }
                        else -> {
                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                        }
                    }
                }
            }
        }else{
            showErrorDialog(this,
                resources.getString(R.string.no_internet_dialog_msg))
        }
    }

    private fun createWishListDialog() {
        val dialog = Dialog(this, R.style.BottomSheetDialog)
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
            val etDescription = findViewById<EditText>(R.id.etDescription)

            val tvMaxCount = findViewById<TextView>(R.id.textMaxCount)
            setupCharacterCountListener(etDescription, tvMaxCount, 50)
            val etName =    findViewById<EditText>(R.id.etName)
            findViewById<ImageView>(R.id.imageCross).setOnClickListener {
                dismiss()
            }
            findViewById<TextView>(R.id.textCreate).setOnClickListener {
                if (etName.text.isEmpty()){
                    etName.error = AppConstant.name
                    etName.requestFocus()
                    showToast(this@RestaurantDetailActivity,AppConstant.name)
                }else if (etDescription.text.isEmpty()){
                    etDescription.error = AppConstant.description
                    etDescription.requestFocus()
                    showToast(this@RestaurantDetailActivity,AppConstant.description)
                }else{
                    createWishlist(etName.text.toString(),etDescription.text.toString(),
                        propertyId,dialog)
                }
            }
            findViewById<TextView>(R.id.textClear).setOnClickListener {
                dismiss()
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

    private fun createWishlist(name: String,
                               description: String,
                               property_id: String,
                               dialog: Dialog) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.createWishlist(session?.getUserId().toString(),
                    name,description,property_id).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                showToast(this@RestaurantDetailActivity,resp.first)
                                binding.proAddWishLists.visibility = View.VISIBLE
                                binding.proNoWishLists.visibility = View.GONE
                                dialog.dismiss()
                            }
                        }
                        is NetworkResult.Error -> {
                            showErrorDialog(this@RestaurantDetailActivity, it.message!!)
                        }
                        else -> {
                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                        }
                    }
                }
            }
        }else{
            showErrorDialog(this,
                resources.getString(R.string.no_internet_dialog_msg))
        }
    }
    private fun getWisList(dialogAdapter: WishlistAdapter) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.getWisList(session?.getUserId().toString()).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                val listType = object : TypeToken<List<WishlistItem>>() {}.type
                                val wish: MutableList<WishlistItem> = Gson().fromJson(resp, listType)
                                wishlistItem = wish
                                if (wishlistItem.isNotEmpty()) {
                                    dialogAdapter.updateItem(wishlistItem)
                                }
                            }
                        }
                        is NetworkResult.Error -> {
                            showErrorDialog(this@RestaurantDetailActivity, it.message!!)
                        }

                        else -> {
                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                        }
                    }
                }
            }
        }else{
            showErrorDialog(this,
                resources.getString(R.string.no_internet_dialog_msg))
        }
    }

    private fun setupCharacterCountListener(
        editText: EditText,
        textView: TextView,
        maxLength: Int
    ) {
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


    fun share() {
        binding.llShare.setOnClickListener {
            shareText(this, "Here is Zyvoo Promo Code")
        }
    }

    fun shareText(context: Context, text: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain" // Set the type to plain text
            putExtra(Intent.EXTRA_TEXT, text) // Add the text to be shared
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    fun initialization() {
        adapterAddon = AdapterProAddOn(this, addOnList,object: onItemClickListener{
            override fun onItemClick(list: MutableList<AddOn>, position: Int) {
            }

        })
        adapterReview = AdapterProReview(this, reviewList)
        binding.recyclerAddOn.adapter = adapterAddon
        binding.recyclerAddOn.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerAddOn.isNestedScrollingEnabled = false
        binding.recyclerReviews.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerReviews.isNestedScrollingEnabled = false
        binding.recyclerReviews.adapter = adapterReview
        val textView = binding.tvShowMore
        textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.tvLocationName.paintFlags =
            binding.tvLocationName.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.tvShowMore.setOnClickListener {
            adapterAddon.toggleList()
            binding.tvShowMore.text = if (adapterAddon.itemCount == addOnList.size) "Show Less" else "Show More"
        }
        binding.startBooking.setOnClickListener {
            if (binding.tvBookingTxt.text.toString().equals("Start Booking")) {
                binding.tvBookingTxt.setText("Proceed to Checkout")
                binding.tvDay.setBackgroundResource(R.drawable.bg_inner_manage_place)
                binding.tvHour.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.cv1.visibility = View.GONE
                binding.calendarLayout.visibility = View.VISIBLE
                binding.llday.visibility = View.VISIBLE
                binding.llHr.visibility = View.GONE
                binding.textend.setFocusable(false);
                binding.textend.setClickable(false);
            } else if (binding.textHr.text.isEmpty()) {
                showToast(this,AppConstant.hours)
            } else if (binding.textPrice.text.isEmpty()) {
                showToast(this,AppConstant.price)
            } else if (binding.textstart.text.isEmpty()) {
                showToast(this,AppConstant.stTime)
            } else if (binding.textend.text.isEmpty()) {
                showToast(this,AppConstant.edTime)
            } else{
                val intent = Intent(this@RestaurantDetailActivity, CheckOutPayActivity::class.java)
                intent.putExtra("hour",binding.textHr.text.toString().replace(" hour",""))
                intent.putExtra("price",binding.textPrice.text.toString())
                intent.putExtra("stTime",binding.textstart.text.toString())
                intent.putExtra("edTime",binding.textend.text.toString())
                intent.putExtra("propertyData",Gson().toJson(propertyData))
                intent.putExtra("propertyMile",propertyMile)
                intent.putExtra("date",selectedDate.toString())
                startActivity(intent)
            }
        }
        clickListeners()
        selectTime()
    }

    fun selectTime() {
        binding.rlView1.setOnClickListener {
                DateManager(this).showTimePickerDialog(this) { selectedTime ->
                    try {
                        binding.textstart.setText(selectedTime)
                        // Define the time formatter (12-hour format with AM/PM)
                        val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
                        } else {
                            TODO("VERSION.SDK_INT < O")
                        }
                        Log.d(ErrorDialog.TAG,selectedTime)
                        // Parse the start time string into a LocalTime object
                        val startTime = LocalTime.parse(selectedTime, formatter)
                        // Add 2 hours to get the end time
                        val endTime = startTime.plusHours(binding.textHr.text.toString().replace(" hour","")
                            .toLong())
                        // Format the end time back to a string
                        val formattedEndTime = endTime.format(formatter)
                        binding.textend.text = formattedEndTime.uppercase()
                    }catch (e:Exception){
                        Log.d(ErrorDialog.TAG,e.message!!)
                    }
                }
        }
//        binding.rlView2.setOnClickListener {
//                DateManager(this).showTimePickerDialog(this) { selectedTime ->
//                    binding.textend.setText(selectedTime)
//                }
//        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun disableScrollViewScrollForChildView(childView: View, scrollView: ScrollView) {
        childView.setOnTouchListener { _, event ->
            // Prevent ScrollView from intercepting touch events when interacting with this child view
            scrollView.requestDisallowInterceptTouchEvent(true)

            // Let the child view handle its own touch event
            childView.onTouchEvent(event)

            true  // Consume the event to stop ScrollView from scrolling
        }
        // Reset ScrollView intercept when touch is released or canceled
        childView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                scrollView.requestDisallowInterceptTouchEvent(false)  // Allow ScrollView to intercept again
            }

            childView.onTouchEvent(event)

            true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun clickListeners() {

        binding.tvHour.setBackgroundResource(R.drawable.bg_inner_manage_place)

        binding.tvDay.setBackgroundResource(R.drawable.bg_outer_manage_place)

        binding.showMoreReview.setOnClickListener {
            pagination?.let {
                if (it.current_page!=it.total_pages && it.current_page<it.total_pages) {
                    loadMoreReview(filter,(it.current_page+1).toString())
                }
            }
        }

        binding.tvDay.setOnClickListener {
            binding.tvDay.setBackgroundResource(R.drawable.bg_inner_manage_place)
            binding.tvHour.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.cv1.visibility = View.GONE
            binding.calendarLayout.visibility = View.VISIBLE
            binding.llday.visibility = View.VISIBLE
            binding.llHr.visibility = View.GONE
        }

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.tvHour.setOnClickListener {
            binding.tvDay.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvHour.setBackgroundResource(R.drawable.bg_inner_manage_place)
            binding.cv1.visibility = View.VISIBLE
            binding.calendarLayout.visibility = View.GONE
            binding.llday.visibility = View.GONE
            binding.llHr.visibility = View.VISIBLE
        }

    }

    private fun loadMoreReview(filter :String,
                               page :String) {
        if (NetworkMonitorCheck._isConnected.value)   {

            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.filterPropertyReviews(propertyId,
                    filter,page).collect {
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
                                    if(it.total <= it.current_page){
                                        binding.reviewMoreView.visibility = View.GONE
                                    }
                                }
                                reviewList.addAll(localreviewList)
                                reviewList?.let {
                                    if (it.isNotEmpty()){
                                        adapterReview.updateAdapter(it)
                                        binding.proreviewCount.text = "("+ formatConvertCount(reviewList.size.toString()) +" reviews)"
                                        binding.proTotalReview.text = "Reviews "+"("+formatConvertCount(reviewList.size.toString()) +")"
                                    }
                                }

                            }
                        }
                        is NetworkResult.Error -> {
                            showErrorDialog(this@RestaurantDetailActivity, it.message!!)
                        }

                        else -> {
                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                        }
                    }
                }
            }
        }else{
            showErrorDialog(this,
                resources.getString(R.string.no_internet_dialog_msg))
        }


    }

    private fun removeItemFromWishlist(property_id: String) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.removeItemFromWishlist(session?.getUserId().toString(),
                    property_id).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                showToast(this@RestaurantDetailActivity,resp.first)
                                binding.proAddWishLists.visibility = View.GONE
                                binding.proNoWishLists.visibility = View.VISIBLE

                            }
                        }
                        is NetworkResult.Error -> {
                            showErrorDialog(this@RestaurantDetailActivity, it.message!!)
                        }
                        else -> {
                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                        }
                    }
                }
            }
        }else{
            showErrorDialog(this,
                resources.getString(R.string.no_internet_dialog_msg))
        }
    }


    private fun shareText(text: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("text/plain")
        shareIntent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(shareIntent, "Share via"))
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


    override fun onMapReady(googleMap: GoogleMap) {
        try {
            mMap = googleMap
        } catch (e: Resources.NotFoundException) {
            Log.e(ErrorDialog.TAG, "Can't find style. Error: ", e)
        }
    }

    private fun updateCalendar() {
        // Updates the calendar layout with the current and next month views.

        val calendarLayout = binding.calendarLayout
        calendarLayout.removeAllViews()
        val topMonths = mutableListOf<YearMonth>()
        val bottomMonths = mutableListOf<YearMonth>()
        // Separate months into top and bottom lists
        val allMonths = (1..12).map {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                currentMonth.plusMonths(it.toLong())
            } else {
                TODO("VERSION.SDK_INT < O")
            }
        }
        allMonths.forEachIndexed { index, month ->
            if (index % 2 == 0) {
                topMonths.add(month)
//            } else {
//                bottomMonths.add(month)
            }
        }

        addMonthView(calendarLayout, currentMonth)

        // addMonthView(calendarLayout, currentMonth.plusMonths(1))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun addMonthView(parentLayout: LinearLayout, yearMonth: YearMonth) {
        // Adds a view for the specified month to the parent layout.
        val monthView = layoutInflater.inflate(R.layout.calendar_month, parentLayout, false)

        val monthTitle = monthView.findViewById<TextView>(R.id.month_title)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            monthTitle.text =
                "${yearMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }}"
            //${yearMonth.year}"
        }
        val daysLayout = monthView.findViewById<LinearLayout>(R.id.days_layout)
        daysLayout.removeAllViews()
        listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
            val dayView =
                layoutInflater.inflate(R.layout.calendar_day_name, daysLayout, false) as TextView
            dayView.text = day
            daysLayout.addView(dayView)
        }

        val weeksLayout = monthView.findViewById<LinearLayout>(R.id.weeks_layout)
        weeksLayout.removeAllViews()
        val weeks = generateCalendarWeeks(yearMonth)
        weeks.forEach { week ->
            val weekLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            week.forEach { date ->
                val dateView =
                    layoutInflater.inflate(R.layout.calendar_day, weekLayout, false) as TextView
                if (date != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // dateView.text = date.dayOfMonth.toString()
                        dateView.text = date.dayOfMonth.toString().padStart(2, '0')
                    }
                    dateView.setOnClickListener {
                        selectedDate = date
                        updateCalendar()
                        // Toast.makeText(requireContext(), "Selected Date: ${date.dayOfMonth} ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.year}", Toast.LENGTH_SHORT).show()
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        when (date) {
//                            LocalDate.now() -> dateView.setBackgroundResource(R.drawable.current_bg_date)
//                          //  selectedDate -> dateView.setBackgroundResource(R.drawable.selected_bg)
//
//                            else -> dateView.setBackgroundResource(android.R.color.transparent)
//                        }

                        when (date) {
                            //LocalDate.now() -> dateView.setBackgroundResource(R.drawable.current_bg_date)
                            selectedDate -> dateView.setBackgroundResource(R.drawable.current_bg_date)
                            else -> dateView.setBackgroundResource(R.drawable.date_bg)
                        }
                    }
                    dateView.setTextColor(
                        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                date.month == yearMonth.month
                            } else {
                                TODO("VERSION.SDK_INT < O")
                            }
                        ) Color.BLACK
                        else Color.GRAY
                    )
                }
                weekLayout.addView(dateView)
            }
            weeksLayout.addView(weekLayout)
        }
        // Display llPreviousAndNextMonth only for the current month
        if (yearMonth == currentMonth) {
            val previous = monthView.findViewById<ImageButton>(R.id.button_previous)
            previous.setOnClickListener {
                currentMonth = currentMonth.minusMonths(1)
                updateCalendar()
            }
            val next = monthView.findViewById<ImageButton>(R.id.button_next)
            next.setOnClickListener {
                currentMonth = currentMonth.plusMonths(1)
                updateCalendar()
            }
            parentLayout.addView(monthView)
        } else {
            // If not the current month, hide the previous and next buttons
//            val llPreviousAndNextMonth = monthView.findViewById<LinearLayout>(R.id.llPreviousAndNextMonth)
//            llPreviousAndNextMonth.visibility = View.GONE
//            parentLayout.addView(monthView)
        }
    }

    private fun generateCalendarWeeks(yearMonth: YearMonth): List<List<LocalDate?>> {
        // Generates a list of weeks, each containing dates for the specified month.

        val weeks = ArrayList<List<LocalDate?>>()

        var week = ArrayList<LocalDate?>()

        val firstDayOfMonth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            yearMonth.atDay(1).dayOfWeek.value % 7
        } else {
            TODO("VERSION.SDK_INT < O")
        }

        // Add null values for the days before the start of the month

        for (i in 0 until firstDayOfMonth) {
            week.add(null)
        }

        val daysInMonth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            yearMonth.lengthOfMonth()
        } else {
            TODO("VERSION.SDK_INT < O")
        }

        for (day in 1..daysInMonth) {
            week.add(yearMonth.atDay(day))
            if (week.size == 7) {
                weeks.add(week)
                week = ArrayList()
            }
        }

        // Add null values for the days after the end of the month

        while (week.size < 7) {
            week.add(null)
        }

        if (week.isNotEmpty()) {
            weeks.add(week)
        }

        return weeks
    }

}