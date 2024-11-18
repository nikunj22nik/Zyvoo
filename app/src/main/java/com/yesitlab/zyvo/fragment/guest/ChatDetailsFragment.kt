package com.yesitlab.zyvo.fragment.guest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer

import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.adapter.ChatDetailsAdapter
import com.yesitlab.zyvo.databinding.FragmentChatDetailsBinding
import com.yesitlab.zyvo.viewmodel.ChatDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatDetailsFragment : Fragment() {

    private var _binding : FragmentChatDetailsBinding? = null
    private val binding get() = _binding!!
    var chatDetailsAdapter : ChatDetailsAdapter? = null
    private  val viewModel : ChatDetailsViewModel by viewModels()
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
          //  param1 = it.getString(ARG_PARAM1)
           // param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        _binding = FragmentChatDetailsBinding.inflate(LayoutInflater.from(requireContext()),container,false)
       // requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE


        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        chatDetailsAdapter = ChatDetailsAdapter(requireContext(), mutableListOf())
        binding.rvChatting.adapter = chatDetailsAdapter
        viewModel.list.observe(viewLifecycleOwner, Observer {
            chatDetailsAdapter!!.updateItem(it)
        })
        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}