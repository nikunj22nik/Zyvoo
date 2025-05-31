package com.business.zyvo.adapter.host

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.business.zyvo.AppConstant
import com.google.android.material.tabs.TabLayoutMediator
import com.business.zyvo.OnLogClickListener
import com.business.zyvo.R
import com.business.zyvo.adapter.ViewPagerAdapter
import com.business.zyvo.databinding.MyPlacesHostAdapterBinding
import com.business.zyvo.model.HostMyPlacesModel
import com.business.zyvo.model.ViewpagerModel
import com.business.zyvo.utils.CommonAuthWorkUtils

class MyPlacesHostAdapter(private val context: Context, private var list: MutableList<HostMyPlacesModel>) :
    RecyclerView.Adapter<MyPlacesHostAdapter.ViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: HostMyPlacesModel,type:String)
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

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val currentItem = list[position]

        Log.d("TESTING_ID","Here in a adapter "+list.get(position).property_id.toString())
//is_instant_book
       var commonAuthWorkUtils = CommonAuthWorkUtils(context,null)

        if(!commonAuthWorkUtils.isScreenLarge(context)){
            holder.binding.cl1.layoutParams.height = 750
        }

        if (position == 1 || position == 3){
            holder.binding.imageReward.visibility = View.GONE
        }
        else{
            holder.binding.imageReward.visibility = View.GONE
        }

        if (currentItem.is_instant_book == 0){
            holder.binding.textInstantBook.visibility = View.GONE
        }
        else{
            holder.binding.textInstantBook.visibility = View.VISIBLE
        }
        // Setup ViewPager and its adapter
        val viewPagerAdapter = ViewPagerAdapter(mutableListOf(),context, object : OnLogClickListener {
            override fun itemClick(items: MutableList<ViewpagerModel>) {
                //  listener.itemClick(position)
//                mListener.onItemClick(position)


            }
        })
        viewPagerAdapter.setOnItemClickListener(object :ViewPagerAdapter.onItemClickListener{
            override fun onItemClick() {
                mListener.onItemClick(list.get(position),AppConstant.placeOpenActivity)
            }
        })

        holder.binding.viewpager2.adapter = viewPagerAdapter
        holder.binding.viewpager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        holder.binding.viewpager2.setOffscreenPageLimit(1);
        // Observe ViewModel inside fragment and update adapter with images
        viewPagerAdapter.updateItem(list.get(position).property_images.toMutableList())
        // Tab layout mediator (no need to re-bind it every time)
        TabLayoutMediator(holder.binding.tabLayoutForIndicator, holder.binding.viewpager2) { _, _ -> }.attach()
        // Set TextView data
        holder.binding.textHotelName.text = currentItem.title
        var rating = currentItem.property_rating +"("+ currentItem.property_review_count+")"
        var price ="$"+currentItem.hourly_rate+" / h"
        holder.binding.textRating.text = rating
        holder.binding.textPricePerHours.text = price

        currentItem.distance_miles?.let {   holder.binding.textMiles.text = currentItem.distance_miles +" miles away" }

        if(list.get(position).fname ==null){
            holder.binding.rlBtmView.visibility = View.GONE
        }

        if(list.get(position).distance_miles == null){
            holder.binding.textMiles.visibility = View.GONE
        }

        holder.binding.imageAddWish.setOnClickListener {
            showPopupWindow(holder.binding.imageAddWish,position)
        }

        holder.binding.cl1.setOnClickListener {
            Log.d("Testing","Inside of onclick listener")
            mListener.onItemClick(list.get(position),AppConstant.placeOpenActivity)
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
        popupView.findViewById<TextView>(R.id.itemEdit).setOnClickListener {
             mListener.onItemClick(list.get(position),AppConstant.EDIT)
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemDelete).setOnClickListener {
            dialogDelete(position)
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




    fun dialogDelete(position :Int) {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_delete_property)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }

            findViewById<ImageView>(R.id.imgCross).setOnClickListener {
                dismiss()
            }


            findViewById<RelativeLayout>(R.id.yes_btn).setOnClickListener {
                mListener.onItemClick(list.get(position),AppConstant.DELETE)
                dismiss()
            }
            findViewById<RelativeLayout>(R.id.rl_cancel_btn).setOnClickListener {

                dismiss()
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }




}