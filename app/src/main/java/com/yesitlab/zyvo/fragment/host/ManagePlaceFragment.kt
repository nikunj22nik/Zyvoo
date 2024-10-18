package com.yesitlab.zyvo.fragment.host

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.databinding.FragmentManagePlaceBinding


class ManagePlaceFragment : Fragment() {

    lateinit var binding :FragmentManagePlaceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentManagePlaceBinding.inflate(inflater,container,false)


        return binding.root
    }


}