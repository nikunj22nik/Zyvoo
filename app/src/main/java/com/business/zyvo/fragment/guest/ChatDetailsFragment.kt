package com.business.zyvo.fragment.guest

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.business.zyvo.AppConstant
import com.business.zyvo.LoadingUtils

import com.business.zyvo.R
import com.business.zyvo.activity.GuesMain
import com.business.zyvo.activity.HostMainActivity
import com.business.zyvo.adapter.ChatDetailsAdapter

import com.business.zyvo.databinding.FragmentChatDetailsBinding
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.NetworkMonitorCheck
import com.business.zyvo.viewmodel.ChatDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class ChatDetailsFragment : Fragment(), View.OnClickListener{

    private var _binding: FragmentChatDetailsBinding? = null


    private val binding get() = _binding!!
    private var chatDetailsAdapter: ChatDetailsAdapter? = null
    private val viewModel: ChatDetailsViewModel by viewModels()
    var channelName :String=""
    var userId :Int =-1
    var friendId :Int =-1
    lateinit var navController : NavController
    var profileImage :String =""
    var providertoken :String =""
    var groupName :String =""
    var friendprofileimage :String =""
    private lateinit var linearLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //  param1 = it.getString(ARG_PARAM1)
            // param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatDetailsBinding.inflate(LayoutInflater.from(requireContext()), container, false)
        // requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

    //    binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

     //   var context: Context, var quickstartConversationsManager: QuickstartConversationsManager, var user_id: String, var profile_image:String, var friend_profile_image:String, var typelogin:String

        var session :SessionManager = SessionManager(requireContext())

        arguments?.let {
            profileImage = it.getString("user_img").toString()
            providertoken = session.getChatToken().toString()
            userId = it.getInt(AppConstant.USER_ID)
            groupName = it.getString(AppConstant.CHANNEL_NAME).toString()
            friendId = it.getInt(AppConstant.FRIEND_ID)
            friendprofileimage = it.getString("friend_img").toString()
        }

        viewModel.list.observe(viewLifecycleOwner, Observer {
        //    chatDetailsAdapter!!.updateItem(it)
        })

        var sessionManager = SessionManager(requireContext())
        var userType = sessionManager.getUserId()
        if(userType?.equals(AppConstant.Host) == true) {
            val rootView = inflater.inflate(R.layout.activity_host_main, container, false)
            linearLayout = rootView.findViewById(R.id.lay1)
        }
        else{
            val rootView = inflater.inflate(R.layout.activity_gues_main, container, false)
            linearLayout = rootView.findViewById(R.id.lay1)
        }

        if (NetworkMonitorCheck._isConnected.value) {

          //  quickstartConversationsManager.initializeWithAccessToken(requireContext(), providertoken, groupName, friendId.toString(), userId.toString())
        }

        else{
            LoadingUtils.showErrorDialog(requireContext(), "Something Went Wrong!!")
        }

        binding.sendBtn.setOnClickListener{
//            if(binding.etMsg.text.toString().length >0){
//                if (NetworkMonitorCheck._isConnected.value) {
//                    quickstartConversationsManager.sendMessage(binding.etMsg.text.toString())
//                    binding.etMsg.text.clear()
//                }
//            }
//            else{
//                LoadingUtils.showErrorDialog(requireContext(),"Please Check Your Internet Connection")
//            }
        }

        return binding.getRoot()
    }



    override fun onStart() {
        super.onStart()
        var session = SessionManager(requireContext())
    }

    override fun onPause() {
        super.onPause()
        var session = SessionManager(requireContext())

        if(session.getUserType().equals(AppConstant.Host)) {
            (activity as? HostMainActivity)?.showView()
        }else{
            (activity as? GuesMain)?.showView()
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageThreeDots.setOnClickListener(this)
       // binding.imageFilter.setOnClickListener(this)

        navController = Navigation.findNavController(view)

        binding.imgBack.setOnClickListener{
            navController.navigateUp()
        }

        binding.imageProfilePicture.setOnClickListener {
            findNavController().navigate(R.id.hostDetailsFragment)
        }

    }


    private fun showAllBookingPopupWindow(anchorView: View, position: Int) {
        // Inflate the custom layout for the popup menu
        val popupView =
            LayoutInflater.from(context).inflate(R.layout.popup_all_booking_filter, null)

        // Create PopupWindow with the custom layout
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set click listeners for each menu item in the popup layout
        popupView.findViewById<TextView>(R.id.itemAllBookings).setOnClickListener {

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemConfirmed).setOnClickListener {

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemPending).setOnClickListener {

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemFinished).setOnClickListener {

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemCancelled).setOnClickListener {

            popupWindow.dismiss()
        }

        // Get the location of the anchor view (three-dot icon)
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        // Get the height of the PopupView after inflating it
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = popupView.measuredHeight
        val popupWeight = popupView.measuredWidth
        val screenWidht = context?.resources?.displayMetrics?.widthPixels
        val anchorX = location[1]
        val spaceEnd = screenWidht?.minus((anchorX + anchorView.width))

        val xOffset = if (popupWeight > spaceEnd!!) {
            // If there is not enough space below, show it above
            -(popupWeight + 20) // Adjust this value to add a gap between the popup and the anchor view
        } else {
            // Otherwise, show it below
            // 20 // This adds a small gap between the popup and the anchor view
            -(popupWeight + 20)
        }
        // Calculate the Y offset to make the popup appear above the three-dot icon
        val screenHeight = context?.resources?.displayMetrics?.heightPixels
        val anchorY = location[1]

        // Calculate the available space above the anchorView
        val spaceAbove = anchorY
        val spaceBelow = screenHeight?.minus((anchorY + anchorView.height))

        // Determine the Y offset
        val yOffset = if (popupHeight > spaceBelow!!) {
            // If there is not enough space below, show it above
            -(popupHeight + 20) // Adjust this value to add a gap between the popup and the anchor view
        } else {
            // Otherwise, show it below
            20 // This adds a small gap between the popup and the anchor view
        }

        // Show the popup window anchored to the view (three-dot icon)
        popupWindow.elevation = 8.0f  // Optional: Add elevation for shadow effect
        popupWindow.showAsDropDown(
            anchorView,
            xOffset,
            yOffset,
            Gravity.END
        )  // Adjust the Y offset dynamically
    }


    private fun showPopupWindow(anchorView: View, position: Int) {
        // Inflate the custom layout for the popup menu
        val popupView =
            LayoutInflater.from(context).inflate(R.layout.layout_custom_popup_menu, null)

        // Create PopupWindow with the custom layout
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set click listeners for each menu item in the popup layout
        popupView.findViewById<TextView>(R.id.itemMute).setOnClickListener {
            // Handle mute action
            // listner.onMute(position)
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemReport).setOnClickListener {
            // Handle report action
            // listner.onReport(position)
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemDelete).setOnClickListener {
            // Handle delete action
            // listner.onDelete(position)

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemBlock).setOnClickListener {
            // Handle block action
            // listner.onBlock(position)
            popupWindow.dismiss()
        }

        // Get the location of the anchor view (three-dot icon)
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        // Get the height of the PopupView after inflating it
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = popupView.measuredHeight
        val popupWeight = popupView.measuredWidth
        val screenWidht = requireContext()?.resources?.displayMetrics?.widthPixels
        val anchorX = location[1]
        val spaceEnd = screenWidht?.minus((anchorX + anchorView.width))

        val xOffset = if (popupWeight > spaceEnd!!) {
            // If there is not enough space below, show it above
            -(popupWeight + 20) // Adjust this value to add a gap between the popup and the anchor view
        } else {
            // Otherwise, show it below
            // 20 // This adds a small gap between the popup and the anchor view
            -(popupWeight + 20)
        }
        // Calculate the Y offset to make the popup appear above the three-dot icon
        val screenHeight = context?.resources?.displayMetrics?.heightPixels
        val anchorY = location[1]

        // Calculate the available space above the anchorView
        val spaceAbove = anchorY
        val spaceBelow = screenHeight?.minus((anchorY + anchorView.height))

        // Determine the Y offset
        val yOffset = if (popupHeight > spaceBelow!!) {
            // If there is not enough space below, show it above
            -(popupHeight + 20) // Adjust this value to add a gap between the popup and the anchor view
        } else {
            // Otherwise, show it below
            20 // This adds a small gap between the popup and the anchor view
        }

        // Show the popup window anchored to the view (three-dot icon)
        popupWindow.elevation = 8.0f  // Optional: Add elevation for shadow effect
        popupWindow.showAsDropDown(
            anchorView,
            xOffset,
            yOffset,
            Gravity.END
        )  // Adjust the Y offset dynamically
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imageThreeDots -> {
                showPopupWindow(binding.imageThreeDots, 0)
            }


//            R.id.imageFilter -> {
//
//                showAllBookingPopupWindow(binding.imageFilter, 0)
//            }
        }

    }





}