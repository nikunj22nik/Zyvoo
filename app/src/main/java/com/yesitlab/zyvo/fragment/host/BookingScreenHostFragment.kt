package com.yesitlab.zyvo.fragment.host

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.databinding.FragmentBookingScreenHostBinding


import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.yesitlab.zyvo.OnClickListener

import com.yesitlab.zyvo.activity.GuesMain
import com.yesitlab.zyvo.activity.HostMainActivity
import com.yesitlab.zyvo.adapter.MyBookingsAdapter
import com.yesitlab.zyvo.adapter.host.HostBookingsAdapter
import com.yesitlab.zyvo.databinding.FragmentMyBookingsBinding
import com.yesitlab.zyvo.model.MyBookingsModel
import com.yesitlab.zyvo.viewmodel.MyBookingsViewModel
import com.yesitlab.zyvo.viewmodel.NotificationViewModel
import com.yesitlab.zyvo.viewmodel.host.HostBookingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookingScreenHostFragment : Fragment(), OnClickListener, View.OnClickListener {
    private var _binding: FragmentBookingScreenHostBinding? = null
    private val binding get() = _binding!!
    private var adapterMyBookingsAdapter: HostBookingsAdapter? = null
    private val viewModel: HostBookingsViewModel by viewModels()
    private var list: MutableList<MyBookingsModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookingScreenHostBinding.inflate(
            LayoutInflater.from(requireContext()),
            container,
            false
        )
        // Inflate the layout for this fragment
        adapterMyBookingsAdapter = HostBookingsAdapter(requireContext(), mutableListOf(), this)
        binding.recyclerViewChat.adapter = adapterMyBookingsAdapter

        viewModel.list.observe(viewLifecycleOwner, Observer { list ->
            adapterMyBookingsAdapter!!.updateItem(list)
        })


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageFilter.setOnClickListener(this)

    }

    private fun showPopupWindow(anchorView: View, position: Int) {
        // Inflate the custom layout for the popup menu
        val popupView =
            LayoutInflater.from(context).inflate(R.layout.popup_all_booking_host_filter, null)

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
        popupView.findViewById<TextView>(R.id.itemBookingRequests).setOnClickListener {

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemWaitingPayment).setOnClickListener {

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun itemClick(obj: Int) {

        Toast.makeText(requireContext(),obj.toString(),Toast.LENGTH_LONG).show()
//        findNavController().navigate(R.id.reviewBookingFragment)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imageFilter -> {
                showPopupWindow(binding.imageFilter, 0)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        (activity as? HostMainActivity)?.bookingResume()
    }


}