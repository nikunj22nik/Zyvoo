package com.business.zyvo.fragment.guest.hostDetails

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.OnClickListener
import com.business.zyvo.OnClickListener1
import com.business.zyvo.OnItemClickListener
import com.business.zyvo.R
import com.business.zyvo.activity.guest.propertydetails.RestaurantDetailActivity
import com.business.zyvo.adapter.HostListingAdapter

import com.business.zyvo.adapter.guest.AdapterReview
import com.business.zyvo.databinding.FragmentHostDetailsBinding
import com.business.zyvo.fragment.both.viewImage.ViewImageDialogFragment
import com.business.zyvo.fragment.guest.hostDetails.model.HostListingModel
import com.business.zyvo.fragment.guest.hostDetails.model.Property
import com.business.zyvo.fragment.guest.hostDetails.viewModel.HostDetailsViewModel
import com.business.zyvo.fragment.host.payments.model.GetBookingResponse
import com.business.zyvo.fragment.host.payments.viewModel.PaymentsViewModel
import com.business.zyvo.session.SessionManager
import com.business.zyvo.viewmodel.HostListingViewModel
import com.business.zyvo.viewmodel.ImagePopViewModel
import com.business.zyvo.viewmodel.WishlistViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


@AndroidEntryPoint
class HostDetailsFragment : Fragment(), OnClickListener, OnClickListener1 {
    private var _binding: FragmentHostDetailsBinding? = null
    private val binding get() = _binding!!
    // lateinit var adapterReview: AdapterReview

    lateinit var navController: NavController
    private lateinit var adapter: HostListingAdapter
    var hostID: String = "-1"
    lateinit var sessionManager: SessionManager
var propertyList : MutableList<Property> = mutableListOf()

    val viewModel: HostDetailsViewModel by lazy {
        ViewModelProvider(this)[HostDetailsViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            hostID = it.getString(AppConstant.HOST_ID) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHostDetailsBinding.inflate(
            LayoutInflater.from(requireContext()),
            container,
            false
        )
        sessionManager = SessionManager(requireContext())
        lifecycleScope.launch {
            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    LoadingUtils.showDialog(requireContext(), false)
                } else {
                    LoadingUtils.hideDialog()
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialization()
        Log.d("hostId", hostID)
        binding.textAboutDescription.setCollapsedTextColor(R.color.green_color_bar)
        navController = Navigation.findNavController(view)
        binding.imgBack.setOnClickListener {
            navController.navigateUp()
        }
        hostDetailsList()
    }

    private fun hostDetailsList() {
        lifecycleScope.launch {
            viewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        hostDetailsListApi()
                    }

                }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun hostDetailsListApi() {
        lifecycleScope.launch {
            viewModel.hostListing(
                hostID,
                sessionManager.getGustLatitude(),
                sessionManager.getGustLongitude()
            ).collect {
                when (it) {

                    is NetworkResult.Success -> {
                        val model = Gson().fromJson(it.data, HostListingModel::class.java)
                        Log.d("modelResponse", it.data.toString())
                        Log.d("modelResponse", model.toString())
                        model.data?.let {
                            Glide.with(requireContext())
                                .load(AppConstant.BASE_URL + it.host?.profile_picture)
                                .error(R.drawable.ic_circular_img_user)
                                .into(binding.imageProfilePicture)

                            binding.textListing.setText(it.host?.name+" Listings")
                            binding.textHostName.setText(it.host?.name)
                            if (it.about_host?.host_profession != null && it.about_host?.host_profession.isNotEmpty()){
                            if (it.about_host?.host_profession?.get(0) != null && it.about_host?.host_profession[1] != null) {
                                binding.textMyWorkName.setText(it.about_host.host_profession[0] + " ," + it.about_host.host_profession[1])
                            } else if (it.about_host?.host_profession?.get(0) != null) {
                                binding.textMyWorkName.setText(it.about_host.host_profession[0])
                            }
                        }

                            if (it.about_host?.location != null) {
                                binding.textLocationName.setText(it.about_host.location)
                            }
                            if (it.about_host?.language != null && it.about_host?.language.isNotEmpty()){
                                if (it.about_host.language.get(0) != null && it.about_host?.language[1] != null) {
                                    binding.textLanguagesName.setText(it.about_host.language[0] + " ," + it.about_host.language[1])
                                } else if (it.about_host?.language?.get(0) != null) {
                                    binding.textLanguagesName.setText(it.about_host.language[0])
                                }
                            }


                            if (it.about_host?.description != null) {
                                binding.textAboutDescription.setText(it.about_host?.description)
                            }

                            it.properties?.let { it1 -> adapter.updateItem(it1)
                            }
                            if (it.properties != null){
                                propertyList = it.properties
                            }

                            binding.textViewMore.setOnClickListener {
                                val gson = Gson()
                                val jsonList = gson.toJson(propertyList)

                                val bundle = Bundle()
                                bundle.putString(AppConstant.propertyList, jsonList)

                                findNavController().navigate(R.id.listingFragment,bundle)
                            }

                        }


                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(requireContext(), it.message!!)

                    }

                    else -> {

                    }

                }
            }
        }


    }


    fun initialization() {
        adapter = HostListingAdapter(requireContext(), 1, mutableListOf(), this)
        binding.recyclerViewBooking.adapter = adapter


//        binding.recyclerReviews.isNestedScrollingEnabled = false
//        adapterReview = AdapterReview(requireContext(), mutableListOf())
//        binding.recyclerReviews.adapter = adapterReview

        binding.showMoreReview.setOnClickListener {

            //   adapterReview.updateAdapter(7)

//            adapterReview.updateAdapter(7)

        }
    }

    override fun itemClick(obj: Int) {

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