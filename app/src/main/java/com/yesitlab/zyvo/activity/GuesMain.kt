package com.yesitlab.zyvo.activity

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.databinding.ActivityGuesMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GuesMain : AppCompatActivity() ,OnClickListener {
    lateinit var binding : ActivityGuesMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGuesMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.navigationDiscover.setOnClickListener(this)
        binding.navigationInbox.setOnClickListener(this)
        binding.navigationBookings.setOnClickListener(this)
        binding.navigationWishlist.setOnClickListener(this)
        binding.icProfile.setOnClickListener(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
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


}
private fun showBottomNavigation() {
    binding.lay1.visibility = View.VISIBLE
}

private fun hideBottomNavigation() {
    binding.lay1.visibility = View.GONE
}


    private fun inboxColor(){

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


    private fun wishlistColor(){

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

    private fun profileColor(){

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
    private fun bookingResume(){

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
    private fun discoverResume(){
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
        when(p0?.id){
            R.id.navigationDiscover->{
                discoverResume()
                findNavController(R.id.fragmentContainerView_main).navigate(R.id.guest_fragment)
            }
            R.id.navigationInbox->{
                inboxColor()
               findNavController(R.id.fragmentContainerView_main).navigate(R.id.chatFragment)
            }
            R.id.navigationBookings->{
                bookingResume()
                findNavController(R.id.fragmentContainerView_main).navigate(R.id.myBookingsFragment)
            }
            R.id.navigationWishlist->{
                wishlistColor()
               findNavController(R.id.fragmentContainerView_main).navigate(R.id.wishlistFragment)
            }
            R.id.icProfile->{
                profileColor()
                findNavController(R.id.fragmentContainerView_main).navigate(R.id.profileFragment)
            }
        }
    }



}