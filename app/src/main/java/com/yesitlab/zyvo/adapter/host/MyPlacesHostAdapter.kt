package com.yesitlab.zyvo.adapter.host

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.yesitlab.zyvo.OnClickListener
import com.yesitlab.zyvo.OnClickListener1
import com.yesitlab.zyvo.OnLogClickListener
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.adapter.LoggedScreenAdapter
import com.yesitlab.zyvo.adapter.ViewPagerAdapter
import com.yesitlab.zyvo.adapter.guest.AdapterReview
import com.yesitlab.zyvo.databinding.AdapterReviewsBinding
import com.yesitlab.zyvo.databinding.MyPlacesHostAdapterBinding
import com.yesitlab.zyvo.model.HostMyPlacesModel
import com.yesitlab.zyvo.model.LogModel
import com.yesitlab.zyvo.model.ViewpagerModel
import com.yesitlab.zyvo.viewmodel.ImagePopViewModel

class MyPlacesHostAdapter(
    private val context: Context, private var list: MutableList<HostMyPlacesModel>) :
    RecyclerView.Adapter<MyPlacesHostAdapter.ViewHolder>()
{

    private lateinit var mListener: onItemClickListener


    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: MyPlacesHostAdapter.onItemClickListener) {
        mListener = listener
    }


    class ViewHolder(var binding: MyPlacesHostAdapterBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPlacesHostAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding:MyPlacesHostAdapterBinding = MyPlacesHostAdapterBinding.inflate(inflater,parent,false);
        return MyPlacesHostAdapter.ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val currentItem = list[position]


        holder.binding.cl1.setOnClickListener {
            //  listener.itemClick(position)
            mListener.onItemClick(position)
            Log.d("Adapter", "cl1 clicked at position $position")
        }
        if (position == 1 || position == 3){
            holder.binding.textInstantBook.visibility = View.VISIBLE
            holder.binding.imageReward.visibility = View.VISIBLE
        }
        else{
            holder.binding.textInstantBook.visibility = View.GONE
            holder.binding.imageReward.visibility = View.GONE
        }

        // Setup ViewPager and its adapter
        val viewPagerAdapter = ViewPagerAdapter(mutableListOf(),context, object : OnLogClickListener {
            override fun itemClick(items: MutableList<ViewpagerModel>) {
                //  listener.itemClick(position)
                mListener.onItemClick(position)
            }
        })
        holder.binding.viewpager2.adapter = viewPagerAdapter
        holder.binding.viewpager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        // Observe ViewModel inside fragment and update adapter with images

            viewPagerAdapter.updateItem(list.get(position).newList)

        // Tab layout mediator (no need to re-bind it every time)
        TabLayoutMediator(holder.binding.tabLayoutForIndicator, holder.binding.viewpager2) { _, _ -> }.attach()
        // Set TextView data
        holder.binding.textHotelName.text = currentItem.textHotelName
        holder.binding.textRating.text = currentItem.textRating
        holder.binding.textTotal.text = currentItem.textTotal
        holder.binding.textMiles.text = currentItem.textMiles
        holder.binding.textPricePerHours.text = currentItem.textPricePerHours

        holder.binding.imageAddWish.setOnClickListener {
            showPopupWindow(holder.binding.imageAddWish,0)
        }

    }

    override fun getItemCount(): Int {
       return list.size
    }

    fun updateData(newList: MutableList<HostMyPlacesModel>) {
        this.list = newList
        notifyDataSetChanged()
    }

    private fun showPopupWindow(anchorView: View, position: Int) {
        // Inflate the custom layout for the popup menu
        val popupView =
            LayoutInflater.from(context).inflate(R.layout.popup_edit_delete, null)

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




}