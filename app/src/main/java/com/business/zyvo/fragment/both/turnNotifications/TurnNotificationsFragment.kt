package com.business.zyvo.fragment.both.turnNotifications

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.business.zyvo.R
import com.business.zyvo.databinding.FragmentTurnNotificationsBinding


class TurnNotificationsFragment : Fragment() {
private var  _binding: FragmentTurnNotificationsBinding? = null
    private val binding get() = _binding!!

    var data:String = ""
    var type:String = ""
    var email:String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            data = requireArguments().getString("data")!!
            type = requireArguments().getString("type")!!
            email = requireArguments().getString("email")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentTurnNotificationsBinding.inflate(LayoutInflater.from(requireActivity()),container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textNotnow.setOnClickListener{
            val bundle = Bundle()
            bundle.putString("data",data)
            bundle.putString("type",type)
            bundle.putString("email",email)
            findNavController().navigate(R.id.turnLocationFragment,bundle)
        }
        binding.btnNotification.setOnClickListener{
            val bundle = Bundle()
            bundle.putString("data",data)
            bundle.putString("type",type)
            bundle.putString("email",email)
            findNavController().navigate(R.id.turnLocationFragment,bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}