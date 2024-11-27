package com.yesitlab.zyvo.fragment.host

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.adapter.LoggedScreenAdapter
import com.yesitlab.zyvo.adapter.host.MyPlacesHostAdapter
import com.yesitlab.zyvo.databinding.FragmentMyPlacesBinding
import com.yesitlab.zyvo.model.HostMyPlacesModel
import com.yesitlab.zyvo.model.LogModel
import com.yesitlab.zyvo.model.ViewpagerModel


class MyPlacesFragment : Fragment(),View.OnClickListener {

    lateinit var binding :FragmentMyPlacesBinding
    lateinit var adapter : MyPlacesHostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMyPlacesBinding.inflate(LayoutInflater.from(requireContext()))

        adapter = MyPlacesHostAdapter(requireContext(), loadDummyData())
        binding.recyclerMyPlaces.adapter = adapter
        binding.imgFilter.setOnClickListener(this)

        binding.rlAddNewPlace.setOnClickListener(this)


        binding.rlAddNewPlace1.setOnClickListener(this)

        return binding.root
    }


    fun loadDummyData() : MutableList<HostMyPlacesModel>{
        val images = mutableListOf<HostMyPlacesModel>(
            HostMyPlacesModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h",loadImages()),
            HostMyPlacesModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h",loadImages()),
            HostMyPlacesModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h",loadImages()),
            HostMyPlacesModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h",loadImages()),
            HostMyPlacesModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h",loadImages()),
            HostMyPlacesModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h",loadImages()),
            HostMyPlacesModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h",loadImages()),
            HostMyPlacesModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h",loadImages()),
            HostMyPlacesModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h",loadImages()),
            HostMyPlacesModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h",loadImages()),
            HostMyPlacesModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h",loadImages())
        )
        return images
    }

    private fun loadImages() :MutableList<ViewpagerModel>{
        val images = mutableListOf<ViewpagerModel>(ViewpagerModel(R.drawable.ic_image_for_viewpager),
            ViewpagerModel(R.drawable.ic_image_for_viewpager), ViewpagerModel(R.drawable.ic_image_for_viewpager),
            ViewpagerModel(R.drawable.ic_image_for_viewpager), ViewpagerModel(R.drawable.image_hotel), ViewpagerModel(R.drawable.image_hotel),
            ViewpagerModel(R.drawable.image_hotel)
        )
        return images
    }


    private fun showPopupWindow(anchorView: View, position: Int) {
        // Inflate the custom layout for the popup menu
        val popupView =
            LayoutInflater.from(context).inflate(R.layout.popup_earning, null)

        // Create PopupWindow with the custom layout
        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        // Set click listeners for each menu item in the popup layout
        popupView.findViewById<TextView>(R.id.itemAllConversations).setOnClickListener {

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemArchived).setOnClickListener {

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
        popupWindow.showAsDropDown(anchorView, xOffset, yOffset, Gravity.END)  // Adjust the Y offset dynamically
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.img_filter ->{
                showPopupWindow(binding.imgFilter,0)
            }
            R.id.rl_add_new_place ->{
                findNavController().navigate(R.id.host_fragment_property_to_host_manage_property_frag)
            }

            R.id.rl_add_new_place_1 ->{
                findNavController().navigate(R.id.host_fragment_property_to_host_manage_property_frag)
            }
        }
    }


}