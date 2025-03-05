package com.business.zyvo.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.business.zyvo.AppConstant
import com.business.zyvo.MyApp
import com.business.zyvo.NetworkResult
import com.business.zyvo.R

import com.business.zyvo.databinding.ActivityHostMainBinding
import com.business.zyvo.fragment.guest.home.viewModel.GuestDiscoverViewModel
import com.business.zyvo.session.SessionManager
import com.business.zyvo.viewmodel.GuestMainActivityModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HostMainActivity : AppCompatActivity(), View.OnClickListener{

    lateinit var binding: ActivityHostMainBinding
    lateinit var guestViewModel: GuestMainActivityModel
    private lateinit var quickstartConversationsManager: QuickstartConversationsManager

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




//        val currentDestination = findNavController().currentDestination
//        Log.d("NAVIGATION_DEBUG", "Current Destination: $currentDestination")
        callingGetUserToken()
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
                            it.data?.let { it1 -> sessionManager.setChatToken(it1)

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
                bookingResume()
                findNavController(R.id.fragmentContainerView_main).navigate(R.id.bookingScreenHostFragment)
            }

            R.id.icProfile -> {
                profileColor()
                findNavController(R.id.fragmentContainerView_main).navigate(R.id.hostProfileFragment)
            }
        }
    }


}