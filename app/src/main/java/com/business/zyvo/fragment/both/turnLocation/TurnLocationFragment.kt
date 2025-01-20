package com.business.zyvo.fragment.both.turnLocation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.business.zyvo.R
import com.business.zyvo.databinding.FragmentTurnLocationBinding


class TurnLocationFragment : Fragment() {
    lateinit var binding : FragmentTurnLocationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTurnLocationBinding.inflate(LayoutInflater.from(requireActivity()),container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLocation.setOnClickListener {findNavController().navigate(R.id.completeProfileFragment)  }
        binding.textNotnow.setOnClickListener { findNavController().navigate(R.id.completeProfileFragment)  }
    }

}