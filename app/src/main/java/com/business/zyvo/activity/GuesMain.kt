package com.business.zyvo.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.business.zyvo.AppConstant
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.MyApp
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.activity.guest.propertydetails.RestaurantDetailActivity
import com.business.zyvo.databinding.ActivityGuesMainBinding
import com.business.zyvo.di.ConversationsManagerSingleton
import com.business.zyvo.model.ChannelListModel
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.NetworkMonitorCheck
import com.business.zyvo.viewmodel.GuestMainActivityModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GuesMain : AppCompatActivity(), OnClickListener,
    com.business.zyvo.chat.QuickstartConversationsManagerListener {

    lateinit var binding: ActivityGuesMainBinding
    lateinit var guestViewModel: GuestMainActivityModel
    private var quickstartConversationsManager = com.business.zyvo.chat.QuickstartConversationsManager()
    private var map:HashMap<String, ChannelListModel> = HashMap()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  enableEdgeToEdge()
        binding = ActivityGuesMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        guestViewModel = ViewModelProvider(this)[GuestMainActivityModel::class.java]
        binding.navigationDiscover.setOnClickListener(this)
        binding.navigationInbox.setOnClickListener(this)
        binding.navigationBookings.setOnClickListener(this)
        binding.navigationWishlist.setOnClickListener(this)
        binding.icProfile.setOnClickListener(this)
        /*try {
            quickstartConversationsManager = (application as MyApp).conversationsManager!!
            if (quickstartConversationsManager!=null) {
                quickstartConversationsManager.setListener(this)
            }
            Log.e("******", "Error setting QuickstartConversationsManager listener Add")
        } catch (e: Exception) {
            Log.e("******", "Error setting QuickstartConversationsManager listener", e)
        }*/
        try {
            val app = application as? MyApp
            if (app?.conversationsManager != null) {
                quickstartConversationsManager = app.conversationsManager!!
                quickstartConversationsManager.setListener(this)
            } else {
                Log.e("******", "ConversationsManager is null")
            }
        } catch (e: ClassCastException) {
            Log.e("******", "Application is not of type MyApp", e)
        } catch (e: Exception) {
            Log.e("******", "Error setting QuickstartConversationsManager listener", e)
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                if (isKeyboardVisible) imeInsets.bottom else systemBars.bottom
            )
            insets
        }
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView_main) as NavHostFragment
        val navController = navHostFragment.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.chatDetailsFragment -> hideBottomNavigation()
                else -> showBottomNavigation()
            }
        }
        observeButtonState()
        var sessionManager = SessionManager(this)
        sessionManager.setUserType(AppConstant.Guest)
        callingGetUserToken()
        askNotificationPermission()
        handlingDeepLink()


    }

    private fun handlingDeepLink() {
        // Get the intent that started this activity
        // Check if the intent contains a URI (deep link)
        if (intent.extras!=null){
            val propertyId = intent?.extras?.getString("propertyId")
            val propertyMile = intent?.extras?.getString("propertyMile")
            if (propertyId!=null && propertyMile!=null){
            val intent = Intent(this, RestaurantDetailActivity::class.java)
                intent.putExtra("propertyId",propertyId)
                intent.putExtra("propertyMile",propertyMile)
                startActivity(intent)
            }
        }

    }

    private fun callingGetUserToken() {
        val sessionManager = SessionManager(this)
        val userId = sessionManager.getUserId()
        userId?.let {
            lifecycleScope.launch {
                Log.d("TESTING_TOKEN", "HERE INSISE THE TOKEN")
                guestViewModel.getChatToken1(it, "guest").collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            Log.d("TESTING_TOKEN", "HERE SUCEESS THE TOKEN" +it.data)
                            sessionManager.setChatToken(it.data.toString())
                           val app = application as MyApp
                            if (app?.conversationsManager==null) {
                                val chatToken = sessionManager.getChatToken()
                                if (!chatToken.isNullOrEmpty()) {
                                    app?.initializeTwilioClient(chatToken)
                                    try {
                                        app?.conversationsManager?.let { manager ->
                                            quickstartConversationsManager = manager
                                            quickstartConversationsManager.setListener(this@GuesMain)
                                            Log.e("******", "initializeConversationsManager")
                                        } ?: Log.e("******", "ConversationsManager is still null after initialization")
                                    } catch (e: Exception) {
                                        Log.e("******", "Error setting QuickstartConversationsManager listener", e)
                                    }
                                }
                            }
                           /* if (app.conversationsManager==null) {
                                app.initializeTwilioClient(sessionManager.getChatToken()!!)
                                try {
                                    quickstartConversationsManager = app.conversationsManager!!
                                    if (quickstartConversationsManager!=null) {
                                        quickstartConversationsManager.setListener(this@GuesMain)
                                    }
                                } catch (e: Exception) {
                                    Log.e("******", "Error setting QuickstartConversationsManager listener", e)
                                }
                                Log.e("******", "initializeConversationsManager")

                            }*/
                        }

                        is NetworkResult.Error -> {
                            Log.d("TESTING_TOKEN", "HERE ERROR THE TOKEN")

                        }

                        else -> {

                        }
                    }
                }
            }
        }

    }
    private fun initializeConversationsManager(token: String) {
        if (!ConversationsManagerSingleton.isInitialized()) {
            ConversationsManagerSingleton.init(this@GuesMain, token)
            Log.d("******", "Twilio Conversations initialized.")
        }
    }
    fun hideView() {
        binding.lay1.visibility = View.GONE
    }

    // Function to show the view again when the fragment is destroyed or replaced
    fun showView() {
        binding.lay1.visibility = View.VISIBLE
    }
    override fun onResume() {
        super.onResume()
        Log.d(ErrorDialog.TAG, "I am in the on resume")
        if (intent != null) {
            val status: String = intent?.extras?.getString("key_name").toString()
            if (status!=null && status.equals("12345")) {
                Log.d(ErrorDialog.TAG, "I" + status)
                intent.removeExtra("key_name")
                bookingResume()
                findNavController(R.id.fragmentContainerView_main).navigate(R.id.myBookingsFragment)
            }

        }
        callingGetChatUser()
    }

    private fun callingGetChatUser() {
        lifecycleScope.launch {
            var sessionManager = SessionManager(this@GuesMain)
            var userId = sessionManager.getUserId()
            if (userId != null) {
                var sessionManager = SessionManager(this@GuesMain)
                var type = sessionManager.getUserType()
                if (type != null) {
                    guestViewModel.getChatUserChannelList(userId,type).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                it.data?.let {
                                    guestViewModel.chatChannel = it
                                    it.forEach {
                                        map.put(it.group_name.toString(),it)
                                    }
                                    Log.d("*******",map.size.toString() +" Map Size is ")
                                    if (quickstartConversationsManager.conversationsClient==null){
                                        quickstartConversationsManager.initializeWithAccessTokenBase(this@GuesMain
                                            ,sessionManager.getChatToken().toString())
                                    }else{
                                        quickstartConversationsManager.loadChatList()
                                    }
                                }
                            }

                            is NetworkResult.Error -> {

                            }

                            else -> {

                            }
                        }
                    }
                }
            }
        }
    }

    private fun showBottomNavigation() {
        binding.lay1.visibility = View.VISIBLE
    }

    private fun hideBottomNavigation() {
        binding.lay1.visibility = View.GONE
    }


    fun inboxColor() {

        binding.imageDiscover.setImageResource(R.drawable.ic_discover_1_unselected)
        binding.imageInbox.setImageResource(R.drawable.ic_chat_selected)
        binding.imageBooking.setImageResource(R.drawable.ic_booking_1)
        binding.imageWishlist.setImageResource(R.drawable.ic_wishlist)
        binding.imageProfile.setImageResource(R.drawable.ic_profile)


        //text Color
        binding.tvDiscover.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvInbox.setTextColor(ContextCompat.getColor(this, R.color.clickedColor))
        binding.tvBookings.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvWishlist.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvProfile.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))

    }


    fun wishlistColor() {

        binding.imageDiscover.setImageResource(R.drawable.ic_discover_1_unselected)
        binding.imageInbox.setImageResource(R.drawable.ic_chat)
        binding.imageBooking.setImageResource(R.drawable.ic_booking_1)
        binding.imageWishlist.setImageResource(R.drawable.ic_wishlist_selected)
        binding.imageProfile.setImageResource(R.drawable.ic_profile)
        //text Color
        binding.tvDiscover.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvInbox.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvBookings.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvWishlist.setTextColor(ContextCompat.getColor(this, R.color.clickedColor))
        binding.tvProfile.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))

    }

    fun profileColor() {

        binding.imageDiscover.setImageResource(R.drawable.ic_discover_1_unselected)
        binding.imageInbox.setImageResource(R.drawable.ic_chat)
        binding.imageBooking.setImageResource(R.drawable.ic_booking_1)
        binding.imageWishlist.setImageResource(R.drawable.ic_wishlist)
        binding.imageProfile.setImageResource(R.drawable.ic_profile_selected)
        //text Color
        binding.tvDiscover.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvInbox.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvBookings.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvWishlist.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvProfile.setTextColor(ContextCompat.getColor(this, R.color.clickedColor))


    }

    fun bookingResume() {

        Log.d("TESTING_ZYVOO", "i AM HERE IN A BOOKING")
        binding.imageDiscover.setImageResource(R.drawable.ic_discover_1_unselected)
        binding.imageInbox.setImageResource(R.drawable.ic_chat)
        binding.imageBooking.setImageResource(R.drawable.ic_booking_1_selected)
        binding.imageWishlist.setImageResource(R.drawable.ic_wishlist)
        binding.imageProfile.setImageResource(R.drawable.ic_profile)

        //text Color
        binding.tvDiscover.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvInbox.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvBookings.setTextColor(ContextCompat.getColor(this, R.color.clickedColor))
        binding.tvWishlist.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvProfile.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))

    }

    fun discoverResume() {
        //image color


        binding.imageDiscover.setImageResource(R.drawable.ic_discover_1)
        binding.imageInbox.setImageResource(R.drawable.ic_chat)
        binding.imageBooking.setImageResource(R.drawable.ic_booking_1)
        binding.imageWishlist.setImageResource(R.drawable.ic_wishlist)
        binding.imageProfile.setImageResource(R.drawable.ic_profile)

        //text Color
        binding.tvDiscover.setTextColor(ContextCompat.getColor(this, R.color.clickedColor))
        binding.tvInbox.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvBookings.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvWishlist.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvProfile.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))

    }


    override fun onClick(p0: View?) {
       SessionManager(this).setFilterRequest("")
        when (p0?.id) {
            R.id.navigationDiscover -> {
                discoverResume()
                findNavController(R.id.fragmentContainerView_main).navigate(R.id.guest_fragment)
            }

            R.id.navigationInbox -> {
                inboxColor()
                findNavController(R.id.fragmentContainerView_main).navigate(R.id.hostChatFragment)
            }

            R.id.navigationBookings -> {
                bookingResume()
                findNavController(R.id.fragmentContainerView_main).navigate(R.id.myBookingsFragment)
            }

            R.id.navigationWishlist -> {
                wishlistColor()
                findNavController(R.id.fragmentContainerView_main).navigate(R.id.wishlistFragment)
            }

            R.id.icProfile -> {
                profileColor()
                findNavController(R.id.fragmentContainerView_main).navigate(R.id.profileFragment)
            }
        }
    }

    private fun observeButtonState() {
        lifecycleScope.launch {
            NetworkMonitorCheck._isConnected.collect { isConnected ->
                if (!isConnected) {
                    showErrorDialog(
                        this@GuesMain,
                        resources.getString(R.string.no_internet_dialog_msg)
                    )
                }
            }
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                var sessionManager = SessionManager(this)
                sessionManager.setNotificationOnOffStatus(true)
            }
            else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }else{
            var sessionManager = SessionManager(this)
            sessionManager.setNotificationOnOffStatus(true)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            var sessionManager = SessionManager(this)
            sessionManager.setNotificationOnOffStatus(true)
        }else{
            var sessionManager = SessionManager(this)
            sessionManager.setNotificationOnOffStatus(false)
        }
    }


    override fun receivedNewMessage() {
        Log.d("*******","receivedNewMessage gdsg")
        getTotalUnreadMessages()
    }

    override fun messageSentCallback() {

    }


    override fun reloadMessages() {
        Log.d("*******", "Start executing reload message" )
        LoadingUtils.hideDialog()
    }

    override fun reloadLastMessages() {
        Log.d("*******","reloadLastMessages")
        getTotalUnreadMessages()

    }

 /*   private fun getTotalUnreadMessages(){
        var totalUnreadCount:Long = 0
        runOnUiThread {
            try {
                quickstartConversationsManager?.conversationsClient?.myConversations!!.forEach {
                    Log.d("*******","m 8888"+it.friendlyName +" M "+it.uniqueName)
                }

                if (quickstartConversationsManager?.conversationsClient?.myConversations!!.size > 0) {
                    for (i in quickstartConversationsManager?.conversationsClient?.myConversations!!) {
                        try {
                            if (map.containsKey(i.uniqueName)) {
                                Log.d("*******","m 8888"+i.friendlyName +" M "+i.uniqueName)
                                i.getUnreadMessagesCount { re->
                                    if (re!=null) {
                                        Log.d("*******", re.toString())
                                        totalUnreadCount += re
                                        Log.d("*******", "total " + totalUnreadCount.toString())
                                        binding.tvChatNumber.text = "$totalUnreadCount"
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.d("massage error", "data :-" + e.message)
                        }
                    }
                  //  binding.tvChatNumber.text = "$totalUnreadCount"
                }
            }catch (e:Exception){
                Log.d("******","msg :- "+e.message)
            }
        }
    }*/

    private fun getTotalUnreadMessages() {
        val conversations = quickstartConversationsManager?.conversationsClient?.myConversations

        if (conversations.isNullOrEmpty()) {
            Log.d("******", "No conversations found")
            binding.tvChatNumber.text = "0"
            return
        }

        var totalUnreadCount: Long = 0
        var completed = 0
        val relevantConversations = conversations.filter { map.containsKey(it.uniqueName) }

        if (relevantConversations.isEmpty()) {
            Log.d("******", "No relevant conversations in map")
            binding.tvChatNumber.text = "0"
            return
        }

        for (conv in relevantConversations) {
            try {
                conv.getUnreadMessagesCount { count ->
                    if (count != null) {
                        totalUnreadCount += count
                    }
                    completed++

                    if (completed == relevantConversations.size) {
                        // Safely update UI once all async calls complete
                        runOnUiThread {
                            Log.d("******", "Final total unread: $totalUnreadCount")
                            binding.tvChatNumber.text = "$totalUnreadCount"
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("******", "Error while getting unread count: ${e.message}")
                completed++
                if (completed == relevantConversations.size) {
                    runOnUiThread {
                        binding.tvChatNumber.text = "$totalUnreadCount"
                    }
                }
            }
        }
    }


    override fun showError() {

    }




}