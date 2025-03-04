package com.business.zyvo.fragment.guest

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.business.zyvo.OnClickListener
import com.business.zyvo.OnClickListener1
import com.business.zyvo.R
import com.business.zyvo.activity.GuesMain
import com.business.zyvo.adapter.AdapterChatList
import com.business.zyvo.databinding.FragmentChatBinding
import com.business.zyvo.model.ChannelListModel
import com.business.zyvo.model.ChatListModel
import com.business.zyvo.viewmodel.ChatListViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ChatFragment : Fragment() ,View.OnClickListener{

    private var _binding: FragmentChatBinding? = null

    private val binding get() = _binding!!

    private lateinit var adapterChatList: AdapterChatList

    private val viewModel: ChatListViewModel by viewModels()

    var objects: Int = 0

    private var chatList: MutableList<ChannelListModel> = mutableListOf()

    private var filteredList: MutableList<ChannelListModel> = chatList.toMutableList()

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
        _binding =
            FragmentChatBinding.inflate(LayoutInflater.from(requireContext()), container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageFilter.setOnClickListener(this)
        adapterChatList = AdapterChatList(requireContext(), chatList,object : OnClickListener{
            override fun itemClick(obj: Int) {
             findNavController().navigate(R.id.chatDetailsFragment)
            }

        },object : OnClickListener1 {
            override fun itemClick(obj: Int, text: String) {
               when(text){
                   "image"->{
                       findNavController().navigate(R.id.hostDetailsFragment)
                   }
               }
            }

        })
        binding.recyclerViewChat.adapter = adapterChatList
        viewModel.list.observe(viewLifecycleOwner, Observer { list ->
           // chatList = list
         //   adapterChatList.updateItem(list)
        })

        binding.etSearchButton.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

               // val searchText = p0.toString().trim()

            }

            override fun afterTextChanged(p0: Editable?) {
                val query = p0.toString()
                filter(query)

            }
        })
        // Add text change listener for search bar
//        binding.etSearchButton.addTextChangedListener { text ->
//           // Call filter function in adapter
//        }

    }


    fun filter(query: String) {
//        filteredList = if (query.isEmpty()) {
//            chatList.toMutableList()
//        } else {
//            chatList.filter {
//                it.textUserName.contains(query, ignoreCase = true) ||
//                        it.textDescription.contains(query, ignoreCase = true)
//            }.toMutableList()
//        }

        adapterChatList.updateItem(filteredList)

    }


    private fun showPopupWindow(anchorView: View, position: Int) {
        // Inflate the custom layout for the popup menu
        val popupView =
            LayoutInflater.from(context).inflate(R.layout.popup_filter_all_conversations, null)

        // Create PopupWindow with the custom layout
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set click listeners for each menu item in the popup layout
        popupView.findViewById<TextView>(R.id.itemAllConversations).setOnClickListener {

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemArchived).setOnClickListener {

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemUnread).setOnClickListener {

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(p0: View?) {
       when(p0?.id){
           R.id.imageFilter->{
               showPopupWindow(binding.imageFilter,0)
           }
       }
    }
    override fun onResume() {
        super.onResume()
        (activity as? GuesMain)?.inboxColor()
    }

}