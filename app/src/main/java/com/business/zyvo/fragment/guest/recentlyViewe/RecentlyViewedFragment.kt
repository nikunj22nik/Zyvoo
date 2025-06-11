package com.business.zyvo.fragment.guest.recentlyViewe

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.business.zyvo.AppConstant
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.OnClickListener
import com.business.zyvo.OnClickListener1
import com.business.zyvo.R
import com.business.zyvo.activity.guest.propertydetails.RestaurantDetailActivity
import com.business.zyvo.adapter.RecentViewAdapter
import com.business.zyvo.adapter.guest.HomeScreenAdapter
import com.business.zyvo.adapter.guest.HomeScreenAdapter.onItemClickListener

import com.business.zyvo.databinding.FragmentRecentlyViewedBinding
import com.business.zyvo.fragment.guest.home.model.HomePropertyData
import com.business.zyvo.fragment.guest.home.model.WishlistItem
import com.business.zyvo.fragment.guest.home.viewModel.GuestDiscoverViewModel
import com.business.zyvo.fragment.guest.wishlists.viewModel.WishListsViewModel
import com.business.zyvo.model.MyBookingsModel
import com.business.zyvo.model.WishListDetailModel
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
class RecentlyViewedFragment : Fragment() , OnClickListener1, onItemClickListener {
    private var  _binding : FragmentRecentlyViewedBinding? = null
    private val binding get() = _binding!!
    lateinit var  navController: NavController
    //private var adapter : RecentViewAdapter? = null
    var edit : Boolean = false
    private var wishlistItem: MutableList<WishlistItem> = mutableListOf()
    var session: SessionManager?=null
    var wishId :String ="-1"
    private lateinit var homeadapter: HomeScreenAdapter
    private var homePropertyData: MutableList<HomePropertyData> = mutableListOf()

    private val viewModel: WishListsViewModel by lazy {
        ViewModelProvider(this)[WishListsViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentRecentlyViewedBinding.inflate(inflater, container, false)
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

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        binding.imageBackIcon.setOnClickListener {
            navController.navigateUp()
        }

        arguments?.let {
            if(it.containsKey(AppConstant.WISH)){
                wishId = it.getString(AppConstant.WISH).toString()
                val name = it.getString("name")
                name.let {
                    binding.textHeader.text = "$it Wishlist"
                }
            }
        }

    }

    private fun setupRecyclerView() {
       /* adapter = RecentViewAdapter(requireContext(), wishlistItem, edit,
            object: OnClickListener
            {
                override fun itemClick(obj: Int) {
                    deleteWishlist(wishlistItem?.get(obj)?.last_saved_property_id.toString(),obj)
                }

            })
        binding.rvWishList.adapter = adapter*/
        homeadapter = HomeScreenAdapter(requireContext(), homePropertyData,
            this,this)

        setRetainInstance(true);

        binding.rvWishList.adapter = homeadapter
    }

    private fun setupEditClick() {
        binding.textEdit.setOnClickListener {
            // Toggle the edit state
            edit = !edit
         //   adapter?.updateEditMode(edit) // Notify the adapter about the new edit state
        }
    }

    private fun getWisList() {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                if(wishId.equals("-1")){
                 Toast.makeText(requireContext(),"Something Went Wrong",Toast.LENGTH_LONG).show()
                }else {
                    var userId = SessionManager(requireContext()).getUserId()
                    lifecycleScope.launch {
                        if (userId != null) {
                            LoadingUtils.showDialog(requireContext(),false)
                            viewModel.getSavedItemWishList(userId, Integer.parseInt(wishId),session!!.getLatitude(),session!!.getLongitude()).collect {
                              when(it){
                                  is NetworkResult.Success ->{
                                      LoadingUtils.hideDialog()
                                      var list = mutableListOf<WishlistItem>()
                                      var obj = it.data
                                      var arr = obj?.get("data")?.asJsonArray
                                      val listType = object : TypeToken<List<HomePropertyData>>() {}.type
                                      val properties: MutableList<HomePropertyData> = Gson().fromJson(arr, listType)

                                     /* arr?.forEach {
                                          var newObj = it.asJsonObject
                                         var key = newObj.get("images").asJsonArray
                                          var url =""
                                          if(key.size()> 0) {
                                              url =  key.get(0).asString
                                              Log.d("TESTING_URL", "URL IS "+ url)
                                          }
                                          var wishItem = WishlistItem(
                                              newObj.get("wishlist_item_id").asInt,
                                              newObj.get("title").asString,
                                          0,
                                          newObj.get("property_id").asInt,
                                              url
                                          )
                                          list.add(wishItem)
                                      }
                                      wishlistItem = list*/
                                      homePropertyData = properties
                                      Log.d("checkHomePropertyData",it.data.toString())

                                      homeadapter?.updateData(homePropertyData)
                                  }
                                  is NetworkResult.Error ->{
                                      LoadingUtils.hideDialog()

                                  }
                                  else ->{
                                      LoadingUtils.hideDialog()

                                  }
                              }
                            }
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
                viewModel.removeItemFromWishlist(session?.getUserId().toString(),wishlist_id
                    ).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                showToast(requireContext(),resp.first)
                                wishlistItem.removeAt(pos) // Remove the item from the list
                                //adapter?.notifyItemRemoved(pos) // Notify the adapter
                             //   adapter?.notifyItemRangeChanged(pos, wishlistItem.size) // Optional: updates positions of remaining items
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

    override fun itemClick(obj: Int, text: String) {
        when(text){
            "Remove Wish"->{
                removeItemFromWishlist(homePropertyData?.get(obj)?.property_id.toString(),obj)
            }
        }
    }
    private fun removeItemFromWishlist(property_id: String,pos: Int) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.removeItemFromWishlist(session?.getUserId().toString(),
                    property_id).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                showToast(requireContext(),resp.first)
                                homePropertyData.removeAt(pos)
                                homeadapter.updateData(homePropertyData)

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

    override fun onItemClick(position: Int) {
        Log.d(ErrorDialog.TAG,"I AM HERE IN DEVELOPMENT")
        val intent = Intent(requireContext(), RestaurantDetailActivity::class.java)
        intent.putExtra("propertyId",homePropertyData?.get(position)?.property_id.toString())
        intent.putExtra("propertyMile",homePropertyData?.get(position)?.distance_miles.toString())
        startActivity(intent)
    }
}