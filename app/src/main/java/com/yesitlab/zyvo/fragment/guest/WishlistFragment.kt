package com.yesitlab.zyvo.fragment.guest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import com.yesitlab.zyvo.OnClickListener
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.adapter.WishlistAdapter
import com.yesitlab.zyvo.databinding.FragmentWishlistBinding
import com.yesitlab.zyvo.viewmodel.MyBookingsViewModel
import com.yesitlab.zyvo.viewmodel.WishlistViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WishlistFragment : Fragment() {
    private  var _binding :  FragmentWishlistBinding? = null
    private  val binding  get() =  _binding!!

    private  val viewModel : WishlistViewModel by viewModels()
private var adapter : WishlistAdapter? = null

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
          //  param1 = it.getString(ARG_PARAM1)
        //    param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding =  FragmentWishlistBinding.inflate(LayoutInflater.from(requireContext()),container,false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        adapter = WishlistAdapter(requireContext(),false, mutableListOf(),object : OnClickListener{
            override fun itemClick(obj: Int) {
             findNavController().navigate(R.id.recentlyViewedFragment)
            }

        })
        binding.rvWishList.adapter = adapter

        viewModel.list.observe(viewLifecycleOwner, Observer {
            adapter!!.updateItem(it)
        })



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}