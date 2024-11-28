package com.yesitlab.zyvo.fragment.host

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.databinding.FragmentBookingScreenHostBinding


class BookingScreenHostFragment : Fragment() {
lateinit var binding: FragmentBookingScreenHostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBookingScreenHostBinding.inflate(LayoutInflater.from(requireActivity()),container,false)
        return binding.root
    }



}