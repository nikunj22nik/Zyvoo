package com.business.zyvo.fragment.guest

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.business.zyvo.AppConstant
import com.business.zyvo.OnClickListener1
import com.business.zyvo.OnItemClickListener
import com.business.zyvo.R
import com.business.zyvo.activity.guest.propertydetails.RestaurantDetailActivity
import com.business.zyvo.adapter.HostListingAdapter
import com.business.zyvo.databinding.FragmentListingBinding
import com.business.zyvo.fragment.both.viewImage.ViewImageDialogFragment
import com.business.zyvo.fragment.guest.hostDetails.model.Property
import com.business.zyvo.viewmodel.HostListingViewModel
import com.business.zyvo.viewmodel.ImagePopViewModel
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListingFragment : Fragment(), OnClickListener1 {
    private var _binding: FragmentListingBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HostListingAdapter

    var list: MutableList<Property> = mutableListOf()
    var data: String = ""
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            data = it.getString(AppConstant.propertyList) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_listing, container, false)

        adapter = HostListingAdapter(
            requireContext(), null, mutableListOf(),
            this
        )

        setRetainInstance(true)

        binding.recyclerViewBooking.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        if (data != "") {
            val gson = Gson()
            val type = object : TypeToken<MutableList<Property>>() {}.type
            list = gson.fromJson(data, type)
            adapter.updateItem(list)
        }


        binding.imageBackButton.setOnClickListener {
            navController.navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun itemClick(propertyId: Int, miles: String) {
        if (propertyId != 0){
            val intent = Intent(requireActivity(), RestaurantDetailActivity::class.java)
            intent.putExtra("propertyId",propertyId.toString())
            intent.putExtra("propertyMile",miles)
            startActivity(intent)
        }
    }

}