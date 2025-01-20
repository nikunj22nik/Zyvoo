package com.business.zyvo.fragment.guest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.business.zyvo.adapter.RecentViewAdapter

import com.business.zyvo.databinding.FragmentRecentlyViewedBinding
import com.business.zyvo.viewmodel.WishlistViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecentlyViewedFragment : Fragment() {
    lateinit var  binding : FragmentRecentlyViewedBinding
lateinit var  navController: NavController
    private  val viewModel : WishlistViewModel by viewModels()
    private var adapter : RecentViewAdapter? = null
var edit : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRecentlyViewedBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupObservers()
        setupEditClick()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        binding.imageBackIcon.setOnClickListener {
            navController.navigateUp()
        }
    }

    private fun setupRecyclerView() {
        adapter = RecentViewAdapter(requireContext(), mutableListOf(), edit)
        binding.rvWishList.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.list.observe(viewLifecycleOwner, Observer { items ->
            adapter?.updateItem(items) // Update the adapter with the new list
        })
    }

    private fun setupEditClick() {
        binding.textEdit.setOnClickListener {
            // Toggle the edit state
            edit = !edit
            adapter?.updateEditMode(edit) // Notify the adapter about the new edit state
        }
    }

}