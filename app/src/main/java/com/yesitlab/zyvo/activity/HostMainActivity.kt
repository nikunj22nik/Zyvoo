package com.yesitlab.zyvo.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.databinding.ActivityGuesMainBinding
import com.yesitlab.zyvo.databinding.ActivityHostMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HostMainActivity : AppCompatActivity() ,View.OnClickListener{

    lateinit var binding : ActivityHostMainBinding

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

        binding.imageProperties.setImageResource(R.drawable.ic_select_home)

        binding.tvProperties.setTextColor(ContextCompat.getColor(this, R.color.clickedColor))

        binding.navigationProperties.setOnClickListener(this)

        binding.navigationInbox.setOnClickListener(this)

        binding.navigationBookings.setOnClickListener(this)

        binding.icProfile.setOnClickListener(this)

    }


    fun inboxColor(){

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




    fun profileColor(){

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
    fun bookingResume(){

        Log.d("TESTING_ZYVOO","i AM HERE IN A BOOKING")
        binding.imageProperties.setImageResource(R.drawable.ic_home_zyvoo)
        binding.imageInbox.setImageResource(R.drawable.ic_chat)
        binding.imageBooking.setImageResource(R.drawable.ic_booking_1_selected)

        binding.imageProfile.setImageResource(R.drawable.ic_profile)

        //text Color
        binding.tvProperties.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvInbox.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvBookings.setTextColor(ContextCompat.getColor(this, R.color.clickedColor))

        binding.tvProfile.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))

    }
    fun homeResume(){
        //image color
        binding.imageProperties.setImageResource(R.drawable.ic_select_home)
        binding.imageInbox.setImageResource(R.drawable.ic_chat)
        binding.imageBooking.setImageResource(R.drawable.ic_booking_1)

        binding.imageProfile.setImageResource(R.drawable.ic_profile)

        //text Color
        binding.tvProperties.setTextColor(ContextCompat.getColor(this, R.color.clickedColor))
        binding.tvInbox.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvBookings.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))

        binding.tvProfile.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))

    }



    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.navigationProperties->{
                homeResume()
                findNavController(R.id.fragmentContainerView_main).navigate(R.id.host_fragment_properties)
            }
            R.id.navigationInbox->{
                inboxColor()
              //  findNavController(R.id.fragmentContainerView_main).navigate(R.id.chatFragment)
            }
            R.id.navigationBookings->{
                bookingResume()
                findNavController(R.id.fragmentContainerView_main).navigate(R.id.bookingScreenHostFragment)
            }
            R.id.icProfile->{
                profileColor()
                findNavController(R.id.fragmentContainerView_main).navigate(R.id.hostProfileFragment)
            }
        }
    }
}