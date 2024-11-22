package com.yesitlab.zyvo.fragment.both.loggedScreen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.yesitlab.zyvo.OnClickListener
import com.yesitlab.zyvo.OnClickListener1
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.activity.guest.RestaurantDetailActivity
import com.yesitlab.zyvo.adapter.LoggedScreenAdapter
import com.yesitlab.zyvo.databinding.FragmentLoggedScreenBinding
import com.yesitlab.zyvo.utils.CommonAuthWorkUtils
import com.yesitlab.zyvo.viewmodel.ImagePopViewModel
import com.yesitlab.zyvo.viewmodel.LoggedScreenViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoggedScreenFragment : Fragment(), OnClickListener, View.OnClickListener , OnClickListener1 {

    private lateinit var binding: FragmentLoggedScreenBinding

    private lateinit var adapter: LoggedScreenAdapter

    private var commonAuthWorkUtils: CommonAuthWorkUtils? = null

    private val loggedScreenViewModel: LoggedScreenViewModel by lazy {
        ViewModelProvider(this)[LoggedScreenViewModel::class.java]
    }

    private val imagePopViewModel: ImagePopViewModel by lazy {
        ViewModelProvider(this)[ImagePopViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val navController = findNavController()
        commonAuthWorkUtils = CommonAuthWorkUtils(requireActivity(),navController)
        binding = FragmentLoggedScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textLogin.setOnClickListener(this)
        // Set up adapter with lifecycleOwner passed
        adapter = LoggedScreenAdapter(
            requireContext(),
            mutableListOf(),
            this,
            viewLifecycleOwner,
            imagePopViewModel,this
        )
        binding.recyclerViewBooking.adapter = adapter

        // Observe ViewModel data and update adapter
        loggedScreenViewModel.imageList.observe(viewLifecycleOwner, Observer { images ->
            adapter.updateData(images)
        })


        adapter.setOnItemClickListener(object :LoggedScreenAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                Log.d("TESTING_ZYVOO","I AM HERE IN DEVELOPMENT")
                var intent = Intent(requireContext(), RestaurantDetailActivity::class.java)
                startActivity(intent)
            }
        })

        // Handle back press
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun itemClick(obj: Int) {
        findNavController().navigate(R.id.viewImageFragment)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.textLogin -> {
                commonAuthWorkUtils?.dialogLogin(requireContext())
            }
        }
    }

    override fun itemClick(obj: Int, text: String) {
        TODO("Not yet implemented")
    }

}

