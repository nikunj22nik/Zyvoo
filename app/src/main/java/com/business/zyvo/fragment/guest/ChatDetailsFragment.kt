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

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatDetailsBinding.inflate(LayoutInflater.from(requireContext()), container, false)

        binding.lifecycleOwner = viewLifecycleOwner

        val session :SessionManager = SessionManager(requireContext())

        arguments?.let {
            profileImage = it.getString("user_img").toString()
            providertoken = session.getChatToken().toString()
            userId = it.getInt(AppConstant.USER_ID)
            groupName = it.getString(AppConstant.CHANNEL_NAME).toString()
            friendId = it.getInt(AppConstant.FRIEND_ID)
            friendprofileimage = it.getString("friend_img").toString()
        }


        val sessionManager = SessionManager(requireContext())
        val userType = sessionManager.getUserId()
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

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemReport).setOnClickListener {

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemDelete).setOnClickListener {

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemBlock).setOnClickListener {

            popupWindow.dismiss()
        }


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

            -(popupWeight + 20)
        } else {

            -(popupWeight + 20)
        }
        // Calculate the Y offset to make the popup appear above the three-dot icon
        val screenHeight = context?.resources?.displayMetrics?.heightPixels
        val anchorY = location[1]


        val spaceAbove = anchorY
        val spaceBelow = screenHeight?.minus((anchorY + anchorView.height))


        val yOffset = if (popupHeight > spaceBelow!!) {

            -(popupHeight + 20)
        } else {

            20
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

        }

    }
}