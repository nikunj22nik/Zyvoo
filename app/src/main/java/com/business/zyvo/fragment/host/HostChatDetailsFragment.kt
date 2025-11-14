package com.business.zyvo.fragment.host

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.business.zyvo.R
import com.business.zyvo.adapter.ChatDetailsAdapter
import com.business.zyvo.databinding.FragmentChatDetailsBinding
import com.business.zyvo.viewmodel.host.HostChatDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint

 @AndroidEntryPoint

class HostChatDetailsFragment : Fragment(), View.OnClickListener {

     private var _binding: FragmentChatDetailsBinding? = null
     private val binding get() = _binding!!
     private var chatDetailsAdapter: ChatDetailsAdapter? = null
     private val viewModel: HostChatDetailsViewModel by viewModels()
     lateinit var navController : NavController
     var profileimage:String =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatDetailsBinding.inflate(
            LayoutInflater.from(requireContext()),
            container,
            false
        )

        binding.rvChatting.adapter = chatDetailsAdapter
        viewModel.list.observe(viewLifecycleOwner, Observer {

        })
        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageThreeDots.setOnClickListener(this)

        navController = Navigation.findNavController(view)
        binding.imgBack.setOnClickListener{
            navController.navigateUp()
        }

        binding.imageProfilePicture.setOnClickListener {

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

        val spaceAbove = anchorY
        val spaceBelow = screenHeight?.minus((anchorY + anchorView.height))

        val yOffset = if (popupHeight > spaceBelow!!) {
            // If there is not enough space below, show it above
            -(popupHeight + 20) // Adjust this value to add a gap between the popup and the anchor view
        } else {
            // Otherwise, show it below
            20 // This adds a small gap between the popup and the anchor view
        }
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