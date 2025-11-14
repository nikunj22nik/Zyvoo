package com.business.zyvo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.business.zyvo.AppConstant
import com.business.zyvo.OnClickListener1
import com.google.android.material.tabs.TabLayoutMediator
import com.business.zyvo.OnItemClickListener
import com.business.zyvo.OnLogClickListener
import com.business.zyvo.databinding.LayoutLoggedRecyclerviewBinding
import com.business.zyvo.fragment.guest.hostDetails.model.Property
import com.business.zyvo.model.HostListingModel
import com.business.zyvo.model.ViewpagerModel
import com.business.zyvo.viewmodel.ImagePopViewModel



class HostListingAdapter(private val context: Context, private val maxItemsToShow: Int? = null, private var list: MutableList<Property>,
                         var mListener: OnClickListener1
): RecyclerView.Adapter<HostListingAdapter.HostViewHolder>() {


    inner class HostViewHolder(var binding: LayoutLoggedRecyclerviewBinding): RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SuspiciousIndentation")
        fun bind(currentItem: Property) {

            binding.imageAddWish.visibility = View.GONE
            binding.textInstantBook.visibility = View.GONE
            binding.imageReward.visibility = View.GONE
            binding.rlBtmView.visibility = View.GONE





            val viewPagerAdapter = ViewPagerAdapter(mutableListOf(),context, object :
                OnLogClickListener {
                override fun itemClick(items: MutableList<ViewpagerModel>) {
                }
            })

            viewPagerAdapter.setOnItemClickListener(object : ViewPagerAdapter.onItemClickListener{
                override fun onItemClick() {
                    currentItem.distance_miles?.let {
                        mListener.itemClick(currentItem.property_id?: 0,
                            it
                        )
                    }
                }
            })

            binding.viewpager2.adapter = viewPagerAdapter
            binding.viewpager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL

            currentItem.property_images?.let { viewPagerAdapter.updateItem(it) }
            TabLayoutMediator(binding.tabLayoutForIndicator, binding.viewpager2) { _, _ -> }.attach()

            binding.textHotelName.text = currentItem.title
            binding.textRating.text = currentItem.reviews_total_rating
            binding.textTotal.text = "("+ currentItem.reviews_total_count +")"
            binding.textMiles.text = currentItem.distance_miles +" miles away"
            binding.textPricePerHours.text = "$" +currentItem.hourly_rate?.toDouble()?.toInt().toString() +"/h"

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HostViewHolder {
        val binding = LayoutLoggedRecyclerviewBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return  HostViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return maxItemsToShow?.let { minOf(it, list.size) } ?: list.size
    }

    override fun onBindViewHolder(holder: HostViewHolder, position: Int) {
       val currentItem = list.get(position)

        holder.bind(currentItem)
    }

    fun updateItem(newList : MutableList<Property>){
        this.list = newList
        notifyDataSetChanged()
    }

}