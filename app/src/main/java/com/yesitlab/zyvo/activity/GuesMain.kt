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

        binding.navigationHome.setOnClickListener(this)
        binding.navigationEvent.setOnClickListener(this)
        binding.navigationSearch.setOnClickListener(this)
        binding.navigationSettings.setOnClickListener(this)
        binding.icProfile.setOnClickListener(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }






    }

    private fun eventColor(){
        binding.imageHome.setColorFilter(R.color.unClickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint
        binding.imageEvent.setColorFilter(R.color.clickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint
        binding.imageSearch.setColorFilter(R.color.unClickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint
        binding.imageSettings.setColorFilter(R.color.unClickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint
        binding.imageProfile.setColorFilter(R.color.unClickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint

        //text Color
        binding.tvHome.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvEvent.setTextColor(ContextCompat.getColor(this, R.color.clickedColor))
        binding.tvSearch.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvSettings.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvProfile.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))

    }


    private fun settingsColor(){
        binding.imageHome.setColorFilter(R.color.unClickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint
        binding.imageEvent.setColorFilter(R.color.unClickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint
        binding.imageSearch.setColorFilter(R.color.unClickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint
        binding.imageSettings.setColorFilter(R.color.clickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint
        binding.imageProfile.setColorFilter(R.color.unClickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint

        //text Color
        binding.tvHome.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvEvent.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvSearch.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvSettings.setTextColor(ContextCompat.getColor(this, R.color.clickedColor))
        binding.tvProfile.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))

    }

    private fun profileColor(){
        binding.imageHome.setColorFilter(R.color.unClickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint
        binding.imageEvent.setColorFilter(R.color.unClickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint
        binding.imageSearch.setColorFilter(R.color.unClickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint
        binding.imageSettings.setColorFilter(R.color.unClickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint
        binding.imageProfile.setColorFilter(R.color.clickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint

        //text Color
        binding.tvHome.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvEvent.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvSearch.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvSettings.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvProfile.setTextColor(ContextCompat.getColor(this, R.color.clickedColor))


    }
    private fun searchResume(){
        binding.imageHome.setColorFilter(R.color.unClickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint
        binding.imageEvent.setColorFilter(R.color.unClickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint
        binding.imageSearch.setColorFilter(R.color.clickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint
        binding.imageSettings.setColorFilter(R.color.unClickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint
        binding.imageProfile.setColorFilter(R.color.unClickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint


        //text Color
        binding.tvHome.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvEvent.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvSearch.setTextColor(ContextCompat.getColor(this, R.color.clickedColor))
        binding.tvSettings.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvProfile.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))

    }
    private fun discoverResume(){
        //image color

        binding.imageHome.setColorFilter(R.color.clickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint
        binding.imageEvent.setColorFilter(R.color.unClickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint
        binding.imageSearch.setColorFilter(R.color.unClickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint
        binding.imageSettings.setColorFilter(R.color.unClickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint
        binding.imageProfile.setColorFilter(R.color.unClickedColor, PorterDuff.Mode.SRC_IN) // Apply the tint


        //text Color
        binding.tvHome.setTextColor(ContextCompat.getColor(this, R.color.clickedColor))
        binding.tvEvent.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvSearch.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvSettings.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))
        binding.tvProfile.setTextColor(ContextCompat.getColor(this, R.color.unClickedColor))

    }



    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.navigation_home->{
                discoverResume()
               // findNavController(R.id.fragmentContainerView_main).navigate(R.id.profileFragment)
            }
            R.id.navigation_event->{
                eventColor()
              //  findNavController(R.id.fragmentContainerView_main).navigate(R.id.profileFragment)
            }
            R.id.navigation_search->{
                searchResume()
              //  findNavController(R.id.fragmentContainerView_main).navigate(R.id.profileFragment)
            }
            R.id.navigation_settings->{
                settingsColor()
               // findNavController(R.id.fragmentContainerView_main).navigate(R.id.profileFragment)
            }
            R.id.ic_profile->{
                profileColor()
                findNavController(R.id.fragmentContainerView_main).navigate(R.id.profileFragment)
            }
        }
    }



}