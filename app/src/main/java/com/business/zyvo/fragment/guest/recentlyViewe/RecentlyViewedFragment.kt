package com.business.zyvo.fragment.guest.recentlyViewe

import android.app.Dialog
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
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.OnClickListener
import com.business.zyvo.R
import com.business.zyvo.adapter.RecentViewAdapter

import com.business.zyvo.databinding.FragmentRecentlyViewedBinding
import com.business.zyvo.fragment.guest.home.model.WishlistItem
import com.business.zyvo.fragment.guest.home.viewModel.GuestDiscoverViewModel
import com.business.zyvo.fragment.guest.wishlists.viewModel.WishListsViewModel
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.ErrorDialog.showToast
import com.business.zyvo.utils.NetworkMonitorCheck
import com.business.zyvo.viewmodel.WishlistViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecentlyViewedFragment : Fragment() {
    lateinit var  binding : FragmentRecentlyViewedBinding
    lateinit var  navController: NavController
    private var adapter : RecentViewAdapter? = null
    var edit : Boolean = false
    private var wishlistItem: MutableList<WishlistItem> = mutableListOf()
    var session: SessionManager?=null

    private val viewModel: WishListsViewModel by lazy {
        ViewModelProvider(this)[WishListsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRecentlyViewedBinding.inflate(inflater, container, false)
        session = SessionManager(requireActivity())
        // Observe the isLoading state
        lifecycleScope.launch {
            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    LoadingUtils.showDialog(requireContext(), false)
                } else {
                    LoadingUtils.hideDialog()
                }
            }
        }
        setupRecyclerView()
        setupEditClick()
        getWisList()
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
        adapter = RecentViewAdapter(requireContext(), wishlistItem, edit,
            object: OnClickListener
            {
                override fun itemClick(obj: Int) {
                    deleteWishlist(wishlistItem?.get(obj)?.wishlist_id.toString(),obj)
                }

            })
        binding.rvWishList.adapter = adapter
    }

    private fun setupEditClick() {
        binding.textEdit.setOnClickListener {
            // Toggle the edit state
            edit = !edit
            adapter?.updateEditMode(edit) // Notify the adapter about the new edit state
        }
    }

    private fun getWisList() {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.getWisList(session?.getUserId().toString()).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                val listType = object : TypeToken<List<WishlistItem>>() {}.type
                                val wish: MutableList<WishlistItem> = Gson().fromJson(resp, listType)
                                wishlistItem = wish
                                if (wishlistItem.isNotEmpty()) {
                                    adapter?.updateItem(wishlistItem)
                                }
                            }
                        }
                        is NetworkResult.Error -> {
                            showErrorDialog(requireContext(), it.message!!)
                        }
                        else -> {
                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                        }
                    }
                }
            }
        }else{
            showErrorDialog(requireContext(),
                resources.getString(R.string.no_internet_dialog_msg))
        }
    }

    private fun deleteWishlist(wishlist_id: String, pos: Int) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.deleteWishlist(session?.getUserId().toString(),
                    wishlist_id).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                showToast(requireContext(),resp.first)
                                wishlistItem.removeAt(pos) // Remove the item from the list
                                adapter?.notifyItemRemoved(pos) // Notify the adapter
                                adapter?.notifyItemRangeChanged(
                                    pos,
                                    wishlistItem.size) // Optional: updates positions of remaining items
                            }
                        }
                        is NetworkResult.Error -> {
                            showErrorDialog(requireContext(), it.message!!)
                        }
                        else -> {
                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                        }
                    }
                }
            }
        }else{
            showErrorDialog(requireContext(),
                resources.getString(R.string.no_internet_dialog_msg))
        }
    }

}