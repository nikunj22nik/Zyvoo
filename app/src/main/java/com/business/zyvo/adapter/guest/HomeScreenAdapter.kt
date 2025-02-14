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
import com.business.zyvo.OnLogClickListener
import com.business.zyvo.adapter.ViewPagerAdapter
import com.business.zyvo.databinding.LayoutLoggedRecyclerviewBinding
import com.business.zyvo.fragment.guest.home.model.HomePropertyData
import com.business.zyvo.model.ViewpagerModel
import com.business.zyvo.utils.ErrorDialog.formatConvertCount

class HomeScreenAdapter(
    private val context: Context, private var list: MutableList<HomePropertyData>,
    private val listener2: OnClickListener1
) : RecyclerView.Adapter<HomeScreenAdapter.LoggedViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
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
            Log.d("Adapter", "cl1 clicked at position $position")
        }
        // Setup ViewPager and its adapter
        currentItem.images.let {
            val viewPagerAdapter = ViewPagerAdapter(
                currentItem.images.toMutableList(),
                context,
                object : OnLogClickListener {
                    override fun itemClick(items: MutableList<ViewpagerModel>) {
                        //  listener.itemClick(position)
                        mListener.onItemClick(position)
                    }
                })
            holder.binding.viewpager2.adapter = viewPagerAdapter
            holder.binding.viewpager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            TabLayoutMediator(holder.binding.tabLayoutForIndicator, holder.binding.viewpager2) { _, _ -> }.attach()
        }

        // Set TextView data
        currentItem.title?.let {
            holder.binding.textHotelName.text = it
        }

        currentItem.rating?.let {
            holder.binding.textRating.text = it
        }
        currentItem.review_count?.let {
            holder.binding.textTotal.text = "("+formatConvertCount(it)+")"
        }

        currentItem.distance_miles?.let {
            holder.binding.textMiles.text = "$it miles away"
        }
        currentItem.hourly_rate?.let {
            holder.binding.textPricePerHours.text = "$it/h"
        }

        currentItem.is_instant_book?.let {
            if (it==1){
                holder.binding.textInstantBook.visibility = View.VISIBLE
                holder.binding.imageReward.visibility = View.VISIBLE
            }else{
                holder.binding.textInstantBook.visibility = View.GONE
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

