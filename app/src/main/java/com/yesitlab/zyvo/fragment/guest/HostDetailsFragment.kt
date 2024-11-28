package com.yesitlab.zyvo.fragment.guest

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.OnMapReadyCallback
import com.yesitlab.zyvo.OnClickListener
import com.yesitlab.zyvo.OnClickListener1
import com.yesitlab.zyvo.OnItemClickListener
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.adapter.HostListingAdapter

import com.yesitlab.zyvo.adapter.LoggedScreenAdapter

import com.yesitlab.zyvo.adapter.guest.AdapterReview
import com.yesitlab.zyvo.databinding.FragmentGuestDiscoverBinding
import com.yesitlab.zyvo.databinding.FragmentHostDetailsBinding
import com.yesitlab.zyvo.fragment.both.viewImage.ViewImageDialogFragment
import com.yesitlab.zyvo.viewmodel.HostListingViewModel
import com.yesitlab.zyvo.viewmodel.ImagePopViewModel
import com.yesitlab.zyvo.viewmodel.WishlistViewModel
import com.yesitlab.zyvo.viewmodel.guest.GuestDiscoverViewModel
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