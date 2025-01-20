package com.business.zyvo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.OnClickListener
import com.business.zyvo.OnClickListener1
import com.business.zyvo.R
import com.business.zyvo.databinding.LayoutChatListBinding
import com.business.zyvo.model.ChatListModel

class AdapterChatList(
    var context: Context,
    var list: MutableList<ChatListModel>,
    var listener: OnClickListener, var listener1: OnClickListener1?
) : RecyclerView.Adapter<AdapterChatList.ChatListViewHolder>() {
    // Track the selected position

    private var selectedPosition = -1

    inner class ChatListViewHolder(var binding: LayoutChatListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(currentItem: ChatListModel) {
            binding.imageProfilePicture.setImageResource(currentItem.image)
            binding.textTime.setText(currentItem.textTime)
            binding.textUserName.setText(currentItem.textUserName)
            binding.textDescription.setText(currentItem.textDescription)

            binding.imageProfilePicture.setOnClickListener {
                listener1?.itemClick(position,"image")
            }

            // Change the background color based on selectedPosition
            if (position == selectedPosition) {
                // Set the selected background color
                binding.clMain.setBackgroundResource(R.drawable.chat_list_bg)
            } else {
                // Set the default background color
                binding.clMain.setBackgroundResource(R.drawable.chat_list_unselected_bg)
            }

            // Set click listener to change selected position
            binding.root.setOnClickListener {
                // Update selected position
                val previousPosition = selectedPosition
                selectedPosition = position

                // Notify adapter to update both old and new selected items
                notifyItemChanged(previousPosition) // Reset old selection
                notifyItemChanged(selectedPosition) // Highlight new selection

                // Optional: Trigger any listener action
                listener.itemClick(position)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val binding =
            LayoutChatListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatListViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        val currentItem = list[position]


        holder.binding.imageThreeDots.setOnClickListener {

            showPopupWindow(it, position)

        }
        holder.binding.clMain.setOnClickListener{

        }

        holder.bind(currentItem)
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
            removeItem(position)
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
        val screenWidht = context.resources.displayMetrics.widthPixels
        val anchorX = location[1]
        val spaceEnd = screenWidht - (anchorX + anchorView.width)

        val xOffset = if (popupWeight > spaceEnd) {
            // If there is not enough space below, show it above
            -(popupWeight + 20) // Adjust this value to add a gap between the popup and the anchor view
        } else {
            // Otherwise, show it below
            // 20 // This adds a small gap between the popup and the anchor view
            -(popupWeight + 20)
        }
        // Calculate the Y offset to make the popup appear above the three-dot icon
        val screenHeight = context.resources.displayMetrics.heightPixels
        val anchorY = location[1]

        // Calculate the available space above the anchorView
        val spaceAbove = anchorY
        val spaceBelow = screenHeight - (anchorY + anchorView.height)

        // Determine the Y offset
        val yOffset = if (popupHeight > spaceBelow) {
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

//    fun filter(query: String) {
//        filteredList = if (query.isEmpty()) {
//            list.toMutableList()
//        } else {
//            list.filter {
//                it.textUserName.contains(query, ignoreCase = true) || it.textDescription.contains(query, ignoreCase = true)
//            }.toMutableList()
//        }
//        notifyDataSetChanged()
//    }




    private fun removeItem(position: Int) {
        if (position >= 0 && position < list.size) {
            list.removeAt(position)            // Remove item from list
            notifyItemRemoved(position)        // Notify adapter about item removal
            notifyItemRangeChanged(position, list.size) // Optional: refresh range
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItem(list: MutableList<ChatListModel>) {
        this.list = list
        selectedPosition = -1
        notifyDataSetChanged()
    }

}