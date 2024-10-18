package com.yesitlab.zyvo.fragment.both.turnNotifications

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.databinding.FragmentTurnNotificationsBinding


class TurnNotificationsFragment : Fragment() {
private lateinit var  binding: FragmentTurnNotificationsBinding

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
        binding = FragmentTurnNotificationsBinding.inflate(LayoutInflater.from(requireActivity()),container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textNotnow.setOnClickListener{
            findNavController().navigate(R.id.turnLocationFragment)
        }
        binding.btnNotification.setOnClickListener{
            findNavController().navigate(R.id.turnLocationFragment)
        }
    }

}