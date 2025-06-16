package com.business.zyvo.adapter.guest

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.business.zyvo.OnClickListener1
import com.business.zyvo.databinding.LayoutLoggedRecyclerviewBinding
import com.business.zyvo.fragment.guest.home.model.HomePropertyData
import com.business.zyvo.fragment.guest.home.model.OnViewPagerImageClickListener
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.ErrorDialog.formatConvertCount
import com.business.zyvo.utils.ErrorDialog.truncateToTwoDecimalPlaces
import java.text.DecimalFormat

class HomeScreenAdapter(
    private val context: Context, private var list: MutableList<HomePropertyData>,
    private val listener2: OnClickListener1,private val mListener:onItemClickListener
) : RecyclerView.Adapter<HomeScreenAdapter.LoggedViewHolder>() {


    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    inner class LoggedViewHolder(val binding: LayoutLoggedRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoggedViewHolder {
        val binding = LayoutLoggedRecyclerviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoggedViewHolder(binding)
    }

    override fun getItemCount() = list?.size ?: 0

    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    override fun onBindViewHolder(holder: LoggedViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val currentItem = list[position]

        holder.binding.imageAddWish.setOnClickListener{
                listener2.itemClick(position,"Add Wish")
        }
        holder.binding.imageWishFull.setOnClickListener{
            listener2.itemClick(position,"Remove Wish")
        }

        holder.binding.cl1.setOnClickListener {
            mListener.onItemClick(position)
            Log.d(ErrorDialog.TAG, "cl1 clicked at position $position")
        }
        // Setup ViewPager and its adapter
        currentItem.images.let {
            Log.d("checkImageSize", currentItem.images.toMutableList().size.toString())
            if (currentItem.images.toMutableList().size == 1 ) holder.binding.tabLayoutForIndicator.visibility = View.GONE else holder.binding.tabLayoutForIndicator.visibility = View.VISIBLE
            val viewPagerAdapter = GuestViewPagerAdapter(
                currentItem.images.toMutableList(),
                context,
                object : OnViewPagerImageClickListener {
                    override fun itemClick(items: Int) {
                        mListener.onItemClick(position)
                    }
                })
            holder.binding.viewpager2.adapter = viewPagerAdapter
            holder.binding.viewpager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            TabLayoutMediator(holder.binding.tabLayoutForIndicator, holder.binding.viewpager2) { _, _ -> }.attach()
        }
        holder.binding.textInstantBook.setOnClickListener {

        }

        // Set TextView data
        currentItem.title?.let {
            holder.binding.textHotelName.text = it
        }

        currentItem.rating?.let {
            val formattedRating = String.format("%.1f", it.toFloat())
            holder.binding.textRating.text = formattedRating
           // holder.binding.textRating.text = String.format("%.1f", it)
        }
        currentItem.review_count?.let {
            holder.binding.textTotal.text = "("+formatConvertCount(it)+")"
        }

        if (currentItem.distance_miles.isNullOrBlank()) {
            holder.binding.textMiles.visibility = View.GONE
        } else {
            holder.binding.textMiles.text = "${currentItem.distance_miles} miles away"
            holder.binding.textMiles.visibility = View.VISIBLE
        }
      /*  currentItem.distance_miles?.let {
            holder.binding.textMiles.text = "$it miles away"
        }*/
        currentItem.hourly_rate?.let {
            holder.binding.textPricePerHours.text = "$${truncateToTwoDecimalPlaces(it)} / h"
        }

        currentItem.is_instant_book?.let {
            if (it==1){
                holder.binding.textInstantBook.visibility = View.VISIBLE
            }else{
                holder.binding.textInstantBook.visibility = View.GONE
            }
        }
        currentItem.is_star_host?.let {
            if (it=="true"){
                holder.binding.imageReward.visibility = View.VISIBLE
            }else{
                holder.binding.imageReward.visibility = View.GONE
            }
        }

        currentItem.is_in_wishlist?.let {
            if (it==1){
                holder.binding.imageAddWish.visibility = View.GONE
                holder.binding.imageWishFull.visibility = View.VISIBLE
            }else{
                holder.binding.imageAddWish.visibility = View.VISIBLE
                holder.binding.imageWishFull.visibility = View.GONE
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: MutableList<HomePropertyData>) {
        this.list = newList
        notifyDataSetChanged()
    }

}

