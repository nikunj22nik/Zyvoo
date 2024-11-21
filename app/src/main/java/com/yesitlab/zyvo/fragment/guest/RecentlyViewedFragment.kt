package com.yesitlab.zyvo.fragment.guest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.databinding.FragmentRecentlyViewedBinding

class RecentlyViewedFragment : Fragment() {
    lateinit var  binding : FragmentRecentlyViewedBinding

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
        binding = FragmentRecentlyViewedBinding.inflate(LayoutInflater.from(requireContext()),container,false)

        return binding.root
    }


}