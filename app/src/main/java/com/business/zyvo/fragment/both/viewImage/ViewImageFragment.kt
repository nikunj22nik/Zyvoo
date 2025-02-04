package com.business.zyvo.fragment.both.viewImage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.business.zyvo.R
import com.business.zyvo.adapter.ViewPagerAdapter
import com.business.zyvo.databinding.FragmentViewImageBinding
import com.business.zyvo.viewmodel.ImagePopViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ViewImageFragment : Fragment() ,OnClickListener{

    private var _binding: FragmentViewImageBinding? = null
    private val binding get() = _binding!!
    private var adapter: ViewPagerAdapter? = null
    private val imagePopViewModel: ImagePopViewModel by lazy {
        ViewModelProvider(this)[ImagePopViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
binding.imageBack.setOnClickListener(this)
        // Initialize the ViewPager adapter after binding has been set
        adapter = ViewPagerAdapter(mutableListOf(),requireContext(),null)
        binding.viewpager.adapter = adapter
        binding.viewpager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        // Observe the data in ViewModel and update the adapter
        imagePopViewModel.imageList.observe(viewLifecycleOwner, Observer { images ->
           // adapter?.updateItem(images)
        })

        // Set up the TabLayout mediator
        TabLayoutMediator(binding.tabLayoutForIndicator, binding.viewpager) { tab, position ->
            // Tab configuration if needed
        }.attach()

        // Handle back press within the fragment
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle back press logic here
                findNavController().navigateUp()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(p0: View?) {
       when(p0?.id){
           R.id.imageBack->{
               findNavController().navigateUp()
           }
       }
    }
}