package com.business.zyvo.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import com.business.zyvo.AppConstant
import com.business.zyvo.BookingRemoveListener
import com.business.zyvo.LoadingUtils
import com.business.zyvo.MyApp
import com.business.zyvo.NetworkResult
import com.business.zyvo.R

import com.business.zyvo.databinding.ActivityHostMainBinding
import com.business.zyvo.fragment.guest.home.viewModel.GuestDiscoverViewModel
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.NetworkMonitorCheck
import com.business.zyvo.viewmodel.GuestMainActivityModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HostMainActivity : AppCompatActivity(), View.OnClickListener ,BookingRemoveListener{

    lateinit var binding: ActivityHostMainBinding
    lateinit var guestViewModel: GuestMainActivityModel
    private lateinit var myReceiver: MyReceiver
    private lateinit var quickstartConversationsManager: QuickstartConversationsManager
    lateinit var tvCOUNT :TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHostMainBinding.inflate(LayoutInflater.from(this))

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        guestViewModel = ViewModelProvider(this)[GuestMainActivityModel::class.java]

        tvCOUNT = binding.tvBookingCount

        Log.d("TESTING_OBJ","TVCOUNT IS INITIALIZED")

        binding.imageProperties.setImageResource(R.drawable.ic_select_home)
        binding.tvProperties.setTextColor(ContextCompat.getColor(this, R.color.clickedColor))
        binding.navigationProperties.setOnClickListener(this)
        binding.navigationInbox1.setOnClickListener(this)
        binding.navigationBookings.setOnClickListener(this)
        binding.icProfile.setOnClickListener(this)


//        try {
//            quickstartConversationsManager = (application as MyApp).conversationsManager
//            quickstartConversationsManager.setListener(this) // Ensure this is only called after full initialization
//            loadChat()
//        } catch (e: Exception) {
//            Log.e("ChatActivity", "Error setting QuickstartConversationsManager listener", e)
//        }

//        callingGetUserToken()

        var sessionManager = SessionManager(this)
        sessionManager.setUserType(AppConstant.Host)

        myReceiver = MyReceiver()

        // Register the receiver with an IntentFilter
        val filter = IntentFilter("com.example.broadcast.ACTION_SEND_MESSAGE")
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, filter)




//        val currentDestination = findNavController().currentDestination
//        Log.d("NAVIGATION_DEBUG", "Current Destination: $currentDestination")
        callingGetUserToken()
        askNotificationPermission()

        callingBookingNumberApi()
    }


    private fun callingBookingNumberApi(){
        lifecycleScope.launch {
            var userId = SessionManager(this@HostMainActivity).getUserId()
            if (userId != null) {
                guestViewModel.getHostUnreadBookings(userId).collect{
                   when(it){
                       is NetworkResult.Success ->{
                           var number = it.data

                           binding.rlBookingCount.visibility = View.VISIBLE
                           binding.tvBookingCount.setText(number.toString())

                           Log.d("TESTING_DATA","Booking Count is "+number.toString())
                       }
                       is NetworkResult.Error ->{

                       }
                       else ->{

                       }
                   }
                }
            }

        }
    }

    fun resetBookingCountToZero(){

    }




    fun hideView() {
        binding.lay1.visibility = View.GONE
    }

    // Function to show the view again when the fragment is destroyed or replaced
    fun showView() {
        binding.lay1.visibility = View.VISIBLE
    }


    private fun callingGetUserToken() {
        var sessionManager = SessionManager(this)
        var userId = sessionManager.getUserId()
        userId?.let {
            lifecycleScope.launch {
                guestViewModel.getChatToken(it, "host").collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            Log.d("TESTING_TOKEN",it.data.toString()+" Token inside success")
                            sessionManager.setChatToken(it.data.toString())
//    it.data?.let { it1 -> sessionManager.setChatToken(it1)
//
//                            }
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


    fun inboxColor() {

        binding.imageProperties.setImageResource(R.drawable.ic_home_zyvoo)
        binding.imageInbox.setImageResource(R.drawable.ic_chat_selected)
        binding.imageBooking.setImageResource(R.drawable.ic_booking_1)
        binding.imageProfile.setImageResource(R.drawable.ic_profile)

        //text Color

        binding.tvProperties.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvInbox.setTextColor(ContextCompat.getColor(this, R.color.clickedColor))
        binding.tvBookings.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvProfile.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))

    }


    fun profileColor() {

        binding.imageProperties.setImageResource(R.drawable.ic_home_zyvoo)
        binding.imageInbox.setImageResource(R.drawable.ic_chat)
        binding.imageBooking.setImageResource(R.drawable.ic_booking_1)
        binding.imageProfile.setImageResource(R.drawable.ic_profile_selected)
        //text Color
        binding.tvProperties.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvInbox.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvBookings.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvProfile.setTextColor(ContextCompat.getColor(this, R.color.clickedColor))


    }

    fun bookingResume() {

        Log.d("TESTING_ZYVOO", "i AM HERE IN A BOOKING")
        binding.imageProperties.setImageResource(R.drawable.ic_home_zyvoo)
        binding.imageInbox.setImageResource(R.drawable.ic_chat)
        binding.imageBooking.setImageResource(R.drawable.ic_booking_1_selected)
        binding.imageProfile.setImageResource(R.drawable.ic_profile)
        binding.tvProperties.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvInbox.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvBookings.setTextColor(ContextCompat.getColor(this, R.color.clickedColor))
        binding.tvProfile.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))

    }

    fun homeResume() {
        //image color
        binding.imageProperties.setImageResource(R.drawable.ic_select_home)
        binding.imageInbox.setImageResource(R.drawable.ic_chat)
        binding.imageBooking.setImageResource(R.drawable.ic_booking_1)
        binding.imageProfile.setImageResource(R.drawable.ic_profile)
        binding.tvProperties.setTextColor(ContextCompat.getColor(this, R.color.clickedColor))
        binding.tvInbox.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvBookings.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvProfile.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.navigationProperties -> {
                homeResume()
                findNavController(R.id.fragmentContainerView_main).navigate(R.id.host_fragment_properties)
            }

            R.id.navigationInbox_1 -> {
                inboxColor()

                findNavController(R.id.fragmentContainerView_main).navigate(R.id.hostChatFragment)

            }

            R.id.navigationBookings -> {
                if(NetworkMonitorCheck._isConnected.value) {
                    binding.rlBookingCount.visibility = View.GONE
                    bookingResume()
                    findNavController(R.id.fragmentContainerView_main).navigate(R.id.bookingScreenHostFragment)
                }else{
                    LoadingUtils.showErrorDialog(this@HostMainActivity,"Please Check Your Internet Connection")
                }
            }

            R.id.icProfile -> {
                profileColor()
                findNavController(R.id.fragmentContainerView_main).navigate(R.id.hostProfileFragment)
            }
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
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

    override fun resetToZeroListener() {
        binding.tvBookingCount.setText(0)
    }

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the receiver to avoid memory leaks
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver)
    }

}