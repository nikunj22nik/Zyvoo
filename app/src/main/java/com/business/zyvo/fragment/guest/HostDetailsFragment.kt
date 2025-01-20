package com.business.zyvo.fragment.guest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.business.zyvo.OnClickListener
import com.business.zyvo.OnItemClickListener
import com.business.zyvo.R
import com.business.zyvo.adapter.HostListingAdapter

import com.business.zyvo.adapter.guest.AdapterReview
import com.business.zyvo.databinding.FragmentHostDetailsBinding
import com.business.zyvo.fragment.both.viewImage.ViewImageDialogFragment
import com.business.zyvo.viewmodel.HostListingViewModel
import com.business.zyvo.viewmodel.ImagePopViewModel
import com.business.zyvo.viewmodel.WishlistViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HostDetailsFragment : Fragment() , OnClickListener, OnItemClickListener {
    lateinit var binding : FragmentHostDetailsBinding;
    lateinit var adapterReview: AdapterReview

    lateinit var navController :NavController
    private lateinit var adapter: HostListingAdapter
    private val hostListingViewModel: HostListingViewModel by lazy {
        ViewModelProvider(this)[HostListingViewModel::class.java]
    }

    private val imagePopViewModel: ImagePopViewModel by lazy {
        ViewModelProvider(this)[ImagePopViewModel::class.java]
    }

    private  val viewModel : WishlistViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        //    param1 = it.getString(ARG_PARAM1)
        //    param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHostDetailsBinding.inflate(LayoutInflater.from(requireContext()),container, false)




        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialization()

        binding.textAboutDescription.setCollapsedTextColor(com.business.zyvo.R.color.green_color_bar)
        binding.textViewMore.setOnClickListener {
            findNavController().navigate(R.id.listingFragment)
        }

        navController = Navigation.findNavController(view)
        binding.imgBack.setOnClickListener{
            navController.navigateUp()
        }
    }

    fun initialization(){
        adapter = HostListingAdapter(requireContext(),3, mutableListOf(),
            viewLifecycleOwner, imagePopViewModel,this)



        setRetainInstance(true)

        binding.recyclerViewBooking.adapter = adapter


        hostListingViewModel.imageList.observe(viewLifecycleOwner, Observer {
                images -> adapter.updateItem(images)
        })


        binding.recyclerReviews.isNestedScrollingEnabled = false

        adapterReview = AdapterReview(requireContext(), mutableListOf())

        binding.recyclerReviews.adapter = adapterReview

        clickListeners()
    }

    override fun itemClick(obj: Int) {
        TODO("Not yet implemented")
    }




    private fun clickListeners() {



        binding.showMoreReview.setOnClickListener {
            adapterReview.updateAdapter(7)
        }




    }

    override fun onItemClick(position: Int) {
        val dialogFragment = ViewImageDialogFragment()
        dialogFragment.show(parentFragmentManager, "exampleDialog")

    }

}