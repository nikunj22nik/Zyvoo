package com.business.zyvo.fragment.host

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.business.zyvo.R
import com.business.zyvo.databinding.FragmentBrowseGuideArtcileDetailBinding


class BrowseGuideArtcileDetailFragment : Fragment() {

    lateinit var binding :FragmentBrowseGuideArtcileDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBrowseGuideArtcileDetailBinding.inflate(LayoutInflater.from(requireActivity()),container,false)

        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvContactUs.setOnClickListener {
            findNavController().navigate(R.id.contact_us)
        }

        return binding.root
    }


}