package com.yesitlab.zyvo.fragment.guest

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
import com.yesitlab.zyvo.OnItemClickListener
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.adapter.HostListingAdapter
import com.yesitlab.zyvo.databinding.FragmentListingBinding
import com.yesitlab.zyvo.fragment.both.viewImage.ViewImageDialogFragment
import com.yesitlab.zyvo.viewmodel.HostListingViewModel
import com.yesitlab.zyvo.viewmodel.ImagePopViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListingFragment : Fragment(),OnItemClickListener {
private var _binding : FragmentListingBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HostListingAdapter
    private val hostListingViewModel: HostListingViewModel by lazy {
        ViewModelProvider(this)[HostListingViewModel::class.java]
    }

    private val imagePopViewModel: ImagePopViewModel by lazy {
        ViewModelProvider(this)[ImagePopViewModel::class.java]
    }
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
           // param1 = it.getString(ARG_PARAM1)
            //param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding= DataBindingUtil.inflate(inflater,R.layout.fragment_listing,container,false)

        adapter = HostListingAdapter(requireContext(),null, mutableListOf(),
            viewLifecycleOwner, imagePopViewModel, this)

        setRetainInstance(true)

        binding.recyclerViewBooking.adapter = adapter


        hostListingViewModel.imageList.observe(viewLifecycleOwner, Observer {
                images -> adapter.updateItem(images)
        })



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)


        binding.imageBackButton.setOnClickListener {
            navController.navigateUp()
        }
    }

    override fun onItemClick(position: Int) {
        val dialogFragment = ViewImageDialogFragment()
        dialogFragment.show(parentFragmentManager, "exampleDialog")

    }

}