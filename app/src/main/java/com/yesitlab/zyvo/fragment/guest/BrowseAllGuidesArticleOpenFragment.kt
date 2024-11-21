package com.yesitlab.zyvo.fragment.guest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.databinding.FragmentBrowseAllGuidesArticleOpenBinding




class BrowseAllGuidesArticleOpenFragment : Fragment() {
lateinit var binding : FragmentBrowseAllGuidesArticleOpenBinding


    private lateinit var navController : NavController

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
        binding = FragmentBrowseAllGuidesArticleOpenBinding.inflate(LayoutInflater.from(requireContext()),container,false)


        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        binding.backButton.setOnClickListener{
            navController.navigateUp()
        }

    }


}