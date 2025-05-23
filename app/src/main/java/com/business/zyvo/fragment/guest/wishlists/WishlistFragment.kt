package com.business.zyvo.fragment.guest.wishlists

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.business.zyvo.AppConstant
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.OnClickListener
import com.business.zyvo.R
import com.business.zyvo.activity.GuesMain
import com.business.zyvo.adapter.WishlistAdapter
import com.business.zyvo.adapter.guest.HomeScreenAdapter
import com.business.zyvo.databinding.FragmentWishlistBinding
import com.business.zyvo.fragment.guest.home.model.HomePropertyData
import com.business.zyvo.fragment.guest.home.model.WishlistItem
import com.business.zyvo.fragment.guest.wishlists.viewModel.WishListsViewModel
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.ErrorDialog.showToast
import com.business.zyvo.utils.NetworkMonitorCheck
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WishlistFragment : Fragment() {
    private  var _binding :  FragmentWishlistBinding? = null
    private  val binding  get() =  _binding!!
    private  val viewModel : WishListsViewModel by viewModels()
    private var adapter : WishlistAdapter? = null
    var edit : Boolean = false
    private var wishlistItem: MutableList<WishlistItem> = mutableListOf()
    var session: SessionManager?=null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding =  FragmentWishlistBinding.inflate(LayoutInflater.from(requireContext()),container,false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        session = SessionManager(requireActivity())
        initView()
        getWisList()

        return binding.root
    }

    private fun initView() {
        adapter = WishlistAdapter(requireContext(),false, wishlistItem,edit,object : OnClickListener{
            override fun itemClick(obj: Int) {
                wishlistItem.let {
                    deleteWishlist(it.get(obj).wishlist_id.toString(),obj)
                }
            }
        })

        adapter!!.setOnItemClickListener(object : WishlistAdapter.onItemClickListener{
            override fun onItemClick(position: Int, wish: WishlistItem) {
                var wishListId = wish.wishlist_id
                var bundle = Bundle()
                bundle.putString(AppConstant.WISH , wishListId.toString())
                bundle.putString("name",wish.wishlist_name)
                findNavController().navigate(R.id.recentlyViewedFragment,bundle)
            }

        })

        binding.rvWishList.adapter = adapter

        lifecycleScope.launch {

            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    LoadingUtils.showDialog(requireContext(), false)
                } else {
                    LoadingUtils.hideDialog()
                }
            }

        }

        binding.textEdit.setOnClickListener {
            // Toggle the edit state
            edit = !edit
            binding.textEdit.text = if (edit) "Done" else "Edit"
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
                viewModel.deleteWishlist(session?.getUserId().toString(),wishlist_id
                ).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                showToast(requireContext(),resp.first)
                                wishlistItem.removeAt(pos) // Remove the item from the list
                                adapter?.notifyItemRemoved(pos) // Notify the adapter
                                adapter?.notifyItemRangeChanged(pos,
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onResume() {
        super.onResume()
        (activity as? GuesMain)?.wishlistColor()
    }

}