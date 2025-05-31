package com.business.zyvo.fragment.both.notificationfragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.business.zyvo.AppConstant
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.NotificationListener
import com.business.zyvo.OnClickListener

import com.business.zyvo.R
import com.business.zyvo.adapter.AdapterNotificationScreen
import com.business.zyvo.adapter.GuestNotificationAdapter
import com.business.zyvo.databinding.FragmentNotificationBinding
import com.business.zyvo.model.NotificationScreenModel
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.NetworkMonitorCheck
import com.business.zyvo.viewmodel.NotificationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationFragment : Fragment(),OnClickListener,NotificationListener,View.OnClickListener {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationViewModel by viewModels()
    private lateinit var adapterNotificationScreen : AdapterNotificationScreen
    private lateinit var adapterGuestNotification : GuestNotificationAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var navController: NavController
    private var notificationId : Int = 0
    private  var list: MutableList<NotificationScreenModel> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        // Set up RecyclerView and Adapter

            adapterNotificationScreen =
                AdapterNotificationScreen(requireContext(), arrayListOf(), this)
           // binding.recyclerView.adapter = adapterNotificationScreen  // Ensure the RecyclerView ID is correct

            adapterGuestNotification = GuestNotificationAdapter(requireContext(), arrayListOf(), this)
         //   binding.recyclerView.adapter = adapterGuestNotification

        sessionManager = SessionManager(requireContext())
        if (sessionManager.getUserType() == AppConstant.Guest) {
            binding.recyclerView.adapter = adapterGuestNotification
        } else {
            binding.recyclerView.adapter = adapterNotificationScreen
        }


        // Bind the ViewModel to the layout
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner  // Important for LiveData to work in the layout

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize view-related components and observers
        navController = Navigation.findNavController(view)
        binding.imageBackButton.setOnClickListener(this)

        arguments?.let {
            if(it.containsKey(AppConstant.Host)){
                Log.d("TESTING","I am here in the host")
                val sessionManager = SessionManager(requireContext())
                val userId = sessionManager.getUserId()
                Log.d("TESTING","UserId is "+userId)
                if (userId != null) {
                    callingNotificationApiHost(userId)
                }
            }
        }

        if(SessionManager(requireContext()).getUserType().equals(AppConstant.Guest)){
            getNotificationAPI()
        }
        adapterNotificationScreen.setOnItemClickListener(object : AdapterNotificationScreen.onItemClickListener{
            override fun onItemClick(obj: NotificationScreenModel) {
                var sessionManager = SessionManager(requireContext())
                Log.d("TESTING","booking id is "+obj.notificationId)
                var userId = sessionManager.getUserId()
                callingNotificationDelete(obj,userId)
            }
        })

        adapterGuestNotification.setOnItemClickListener(object : GuestNotificationAdapter.OnItemClickListener{
            override fun onItemClick(obj: NotificationRootModel, notificationId: Int) {
                getRemoveGuestNotificationAPI(notificationId)
            }
        })
    }
    private fun getNotificationAPI() {
        if (!NetworkMonitorCheck._isConnected.value) {
            showErrorDialog(requireContext(), getString(R.string.no_internet_dialog_msg))
            return
        }

        lifecycleScope.launch {
            try {
                viewModel.getGuestNotification(sessionManager.getUserId().toString()).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            LoadingUtils.hideDialog()
                            val list = result.data.orEmpty()
                            Log.d("API_RESPONSE", "Raw Response: $list")

                            if (list.isNotEmpty()) {
                                notificationId = list.first().notification_id
                                Log.d("checkDataList",list.toString())
                                adapterGuestNotification.updateItem(list.toMutableList())
                            } else {
                                notificationId = 0
                                adapterGuestNotification.updateItem(mutableListOf())
                                Log.d("API_RESPONSE", "Empty list received.")
                            }
                        }

                        is NetworkResult.Error -> {
                            LoadingUtils.hideDialog()
                            Log.e("API_ERROR", "Server Error: ${result.message}")
                            showErrorDialog(requireContext(), result.message ?: "Unknown error")
                        }

                        else -> {
                            LoadingUtils.hideDialog()
                            Log.v("API_ERROR", "Unexpected error: ${result.message ?: "Unknown error"}")
                        }
                    }
                }
            } catch (e: Exception) {
                LoadingUtils.hideDialog()
                Log.e("API_EXCEPTION", "Unexpected API error", e)
                showErrorDialog(requireContext(), "Something went wrong. Please try again.")
            }
        }
    }

    private fun getReadGuestNotificationAPI(notificationId: Int) {
        if (!NetworkMonitorCheck._isConnected.value) {
            showErrorDialog(requireContext(), resources.getString(R.string.no_internet_dialog_msg))
            return
        }

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                viewModel.getMarkGuestNotification(sessionManager.getUserId().toString(), notificationId).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            LoadingUtils.hideDialog()
                            val list = result.data // Ensure non-null list
                            Log.d("API_RESPONSE", "Raw Response: $list")
                          //  Toast.makeText(requireContext(),"Marked Read",Toast.LENGTH_LONG).show()
                        }
                        is NetworkResult.Error -> {
                            Log.e("API_ERROR", "Server Error: ${result.message}")
                            showErrorDialog(requireContext(), result.message ?: "Unknown error")
                        }
                        else -> {
                            Log.v("API_ERROR", "Unexpected error: ${result.message ?: "Unknown error"}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("API_EXCEPTION", "Unexpected API error", e)
                showErrorDialog(requireContext(), "Something went wrong. Please try again.")
            }
        }
    }

    private fun getRemoveGuestNotificationAPI(notificationId: Int) {
        if (!NetworkMonitorCheck._isConnected.value) {
            showErrorDialog(requireContext(), resources.getString(R.string.no_internet_dialog_msg))
            return
        }

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                viewModel.getRemoveGuestNotification(sessionManager.getUserId().toString(),notificationId
                ).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            LoadingUtils.hideDialog()
                            val list = result.data // Ensure non-null list
                            Log.d("API_RESPONSE", "Raw Response: $list")
                            Toast.makeText(requireContext(),"Notification Removed",Toast.LENGTH_SHORT).show()
                        }
                        is NetworkResult.Error -> {
                            Log.e("API_ERROR", "Server Error: ${result.message}")
                            showErrorDialog(requireContext(), result.message ?: "Unknown error")
                        }
                        else -> {
                            Log.v("API_ERROR", "Unexpected error: ${result.message ?: "Unknown error"}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("API_EXCEPTION", "Unexpected API error", e)
                showErrorDialog(requireContext(), "Something went wrong. Please try again.")
            }
        }
    }


    private fun callingNotificationDelete(obj: NotificationScreenModel, userId: Int?) {
        lifecycleScope.launch {
            if(NetworkMonitorCheck._isConnected.value) {
                LoadingUtils.showDialog(requireContext(), false)
                if (userId != null) {

                    viewModel.deleteNotificationHost(userId, obj.notificationId).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                LoadingUtils.hideDialog()
                                val newList = mutableListOf<NotificationScreenModel>()
                                list.forEach {
                                    if (it.notificationId != obj.notificationId) {
                                        newList.add(it)
                                    }
                                }
                                adapterNotificationScreen.updateItem(newList)
                                list = newList
                            }

                            is NetworkResult.Error -> {
                                LoadingUtils.hideDialog()
                                ErrorDialog.showErrorDialog(requireContext(), it.message.toString())

                            }
                            else -> {
                                LoadingUtils.hideDialog()

                            }
                        }
                    }
                }
            }else{
                showErrorDialog(requireContext(),"Please Check Your Internet")
            }
        }

    }

    private fun callingNotificationApiHost(userId :Int){
        lifecycleScope.launch {
            if(NetworkMonitorCheck._isConnected.value) {
                LoadingUtils.showDialog(requireContext(), false)
                viewModel.getNotificationHost(userId).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            Log.d("Testing", "list here is " + it.data?.size)
                            if (it.data != null){
                                Log.d("checkDataList",it.data.toString())
                                adapterNotificationScreen.updateItem(it.data)

                                list = it.data

                            }

                            LoadingUtils.hideDialog()
                        }

                        is NetworkResult.Error -> {
                            LoadingUtils.hideDialog()
                        }

                        else -> {
                            LoadingUtils.hideDialog()
                        }
                    }
                }
            }else{
                showErrorDialog(requireContext(),"Please Check Your Internet")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Clean up view references and other UI-related resources
    }

    override fun itemClick(obj: Int) {
      //  viewModel.removeItemAt(obj) // Update the adapter with the new list
    }

    override fun onClick(p0: View?) {
       when(p0?.id){
           R.id.imageBackButton->{
               navController.navigateUp()
           }
       }
    }

    override fun onClick(obj: Int) {
        getReadGuestNotificationAPI(obj)
    }
}
