package com.business.zyvo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.BuildConfig
import com.business.zyvo.OnClickListener
import com.business.zyvo.OnClickListener1
import com.business.zyvo.R
import com.business.zyvo.databinding.LayoutChatListBinding
import com.business.zyvo.model.ChannelListModel
import com.business.zyvo.session.SessionManager

class AdapterChatList(
    var context: Context, var list: MutableList<ChannelListModel>, var listener: OnClickListener,
    var listener1: OnClickListener1?)
    : RecyclerView.Adapter<AdapterChatList.ChatListViewHolder>(), Filterable {
    // Track the selected position
    private lateinit var  sessionManager: SessionManager
    private lateinit var mListener: onItemClickListener
    private var filteredList: List<ChannelListModel> = list

    interface onItemClickListener {
        fun onItemClick(data:ChannelListModel , index:Int,type:String)
    }

    fun setOnItemClickListener(listener: AdapterChatList.onItemClickListener) {
        mListener = listener
    }

    init {
         sessionManager = SessionManager(context)
    }

    private var selectedPosition = -1

    inner class ChatListViewHolder(var binding: LayoutChatListBinding) :
        RecyclerView.ViewHolder(binding.root) {

            fun bind(currentItem: ChannelListModel) {
                var userID = sessionManager.getUserId()
                if(userID.toString().equals(currentItem.sender_id)){
                Glide.with(context).
                load(BuildConfig.MEDIA_URL+"/"+currentItem.receiver_image).into(binding.imageProfilePicture)
                binding.textTime.setText(currentItem.lastMessageTime)
                binding.textUserName.setText(currentItem.receiver_name +"\n"+ "("+currentItem.property_title+")")
                binding.textDescription.setText(currentItem.lastMessage)
                Log.d("TESTING_PROFILE", BuildConfig.MEDIA_URL+"/"+currentItem.receiver_image)
            }
            else{
                Glide.with(context).
                load(BuildConfig.MEDIA_URL+"/"+currentItem.sender_profile).into(binding.imageProfilePicture)
               Log.d("TESTING_PROFILE",BuildConfig.MEDIA_URL+currentItem.sender_profile)
                binding.textTime.setText(currentItem.lastMessageTime)
                binding.textUserName.setText(currentItem.sender_name + "\n"+ "("+currentItem.property_title+")")
                binding.textDescription.setText(currentItem.lastMessage)
            }


            binding.imageProfilePicture.setOnClickListener {
                mListener.onItemClick(currentItem,position,AppConstant.Image)
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
                // Optional: Trigger any listener action
                mListener.onItemClick(currentItem,position,AppConstant.MOVE)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val binding =
            LayoutChatListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatListViewHolder(binding)
    }

    override fun getItemCount() = filteredList.size

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        val currentItem = filteredList[position]


        holder.binding.imageThreeDots.setOnClickListener {

            showPopupWindow(it, position,currentItem)

        }
        holder.binding.clMain.setOnClickListener{
              mListener.onItemClick(currentItem,position,AppConstant.MOVE)
        }

        holder.bind(currentItem)
    }


    private fun showPopupWindow(anchorView: View, position: Int, currentItem: ChannelListModel) {
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
            mListener.onItemClick(currentItem,position,AppConstant.REPORT)
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemDelete).setOnClickListener {
            // Handle delete action
            // listner.onDelete(position)
            popupWindow.dismiss()
        mListener.onItemClick(currentItem,position,AppConstant.DELETE)
        }
        // Find the "block" item inside the popup
        val blockItem = popupView.findViewById<View>(R.id.itemBlock)
        val unblockItem = popupView.findViewById<View>(R.id.itemUnBlock)
        val itemMute = popupView.findViewById<View>(R.id.itemMute)
        val itemunMute = popupView.findViewById<View>(R.id.itemunMute)

        val itemArchived = popupView.findViewById<View>(R.id.itemArchived)
        val itemUnArchived = popupView.findViewById<View>(R.id.itemUnArchived)

        if (currentItem.is_blocked==1){
            blockItem.visibility = View.GONE
            unblockItem.visibility = View.VISIBLE
        }else{
            blockItem.visibility = View.VISIBLE
            unblockItem.visibility = View.GONE
        }
        if (currentItem.is_muted==1){
            itemMute.visibility = View.GONE
            itemunMute.visibility = View.VISIBLE
        }else{
            itemMute.visibility = View.VISIBLE
            itemunMute.visibility = View.GONE
        }

        if (currentItem.is_archived==1){
            itemArchived.visibility = View.GONE
            itemUnArchived.visibility = View.VISIBLE
        }else{
            itemArchived.visibility = View.VISIBLE
            itemUnArchived.visibility = View.GONE
        }
        blockItem.setOnClickListener {
            mListener.onItemClick(currentItem,position,AppConstant.BLOCK)
            popupWindow.dismiss()

        }

        unblockItem.setOnClickListener {
            mListener.onItemClick(currentItem,position,AppConstant.BLOCK)
            popupWindow.dismiss()
        }

        itemMute.setOnClickListener {
            mListener.onItemClick(currentItem,position,AppConstant.MUTE)
            popupWindow.dismiss()

        }

        itemunMute.setOnClickListener {
            mListener.onItemClick(currentItem,position,AppConstant.MUTE)
            popupWindow.dismiss()

        }

        itemArchived.setOnClickListener {
            popupWindow.dismiss()
            mListener.onItemClick(currentItem,position,AppConstant.ARCHIVED)
        }

        itemUnArchived.setOnClickListener {
            popupWindow.dismiss()
            mListener.onItemClick(currentItem,position,AppConstant.ARCHIVED)
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
         //  mListener.onItemClick(list.get(position),position,"Delete");
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItem(list: MutableList<ChannelListModel>) {
        this.list = list
        this.filteredList = list
        selectedPosition = -1
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.toLowerCase()

                val results = FilterResults()
                if (query.isNullOrBlank()) {
                    results.values = list
                } else {
                    val filteredItems = list.filter {
                        it.receiver_name?.toLowerCase()?.contains(query) == true || it.sender_name?.toLowerCase()?.contains(query) == true
                    }
                    results.values = filteredItems
                }
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as List<ChannelListModel>
                notifyDataSetChanged()
            }
        }
    }

}