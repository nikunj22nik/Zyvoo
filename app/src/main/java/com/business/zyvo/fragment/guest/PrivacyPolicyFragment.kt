package com.business.zyvo.fragment.guest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.business.zyvo.R
import com.business.zyvo.databinding.FragmentPrivacyPolicyBinding


class PrivacyPolicyFragment : Fragment(), OnClickListener {
    private var _binding : FragmentPrivacyPolicyBinding? = null
    private val binding get() = _binding!!

    private var privacy: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
          //  param1 = it.getString(ARG_PARAM1)
            privacy = it.getInt("privacy")


        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPrivacyPolicyBinding.inflate(LayoutInflater.from(requireContext()),container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
binding.imageBackButton.setOnClickListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.imageBackButton->{
                if (privacy == 0){
                    findNavController().navigate(R.id.profileFragment)
                }else{
                    findNavController().navigate(R.id.hostProfileFragment)
                }


            }
        }
    }


}