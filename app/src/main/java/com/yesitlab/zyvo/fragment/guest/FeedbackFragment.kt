package com.yesitlab.zyvo.fragment.guest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import com.yesitlab.zyvo.AppConstant
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.databinding.FragmentFeedbackBinding


class FeedbackFragment : Fragment() {
    private  var _binding : FragmentFeedbackBinding? = null
    private val  binding  get() = _binding!!
    private var close = 0
    lateinit var navController: NavController
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //param1 = it.getString(ARG_PARAM1)
          //  param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFeedbackBinding.inflate(LayoutInflater.from(requireContext()),container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.spinnerFeedback.setItems(
            listOf(AppConstant.Host, AppConstant.Guest)
        )


        binding.spinnerFeedback.setOnFocusChangeListener { _, b ->
            close = if (b) {
                1
            } else {
                0
            }
        }

        when (close) {
            0 -> {
                binding.spinnerFeedback.dismiss()
            }
            1 -> {
                binding.spinnerFeedback.show()
            }
        }

        binding.spinnerFeedback.setIsFocusable(true)

        binding.spinnerFeedback.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->
           if (newItem== "Please select"){
               binding.etAddDetails.visibility = View.GONE
               binding.textAddDetails.visibility = View.GONE
           }else{
               binding.etAddDetails.visibility = View.VISIBLE
               binding.textAddDetails.visibility = View.VISIBLE
           }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}