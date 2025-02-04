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
import com.google.android.material.tabs.TabLayoutMediator
import com.business.zyvo.OnItemClickListener
import com.business.zyvo.OnLogClickListener
import com.business.zyvo.databinding.LayoutLoggedRecyclerviewBinding
import com.business.zyvo.model.HostListingModel
import com.business.zyvo.model.ViewpagerModel
import com.business.zyvo.viewmodel.ImagePopViewModel



class HostListingAdapter(private val context: Context,  private val maxItemsToShow: Int? = null , private var list: MutableList<HostListingModel>, private val lifecycleOwner: LifecycleOwner, private val imagePopViewModel: ImagePopViewModel, var mListener: OnItemClickListener): RecyclerView.Adapter<HostListingAdapter.HostViewHolder>() {





    inner class HostViewHolder(var binding: LayoutLoggedRecyclerviewBinding): RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SuspiciousIndentation")
        fun bind(currentItem: HostListingModel) {

            binding.imageAddWish.visibility = View.GONE
            binding.textInstantBook.visibility = View.GONE
            binding.imageReward.visibility = View.GONE


            binding.cl1.setOnClickListener {
                //  listener.itemClick(position)
                mListener.onItemClick(position)
                Log.d("Adapter", "cl1 clicked at position $position")
            }
            // Disable user interaction on ViewPager2
            // holder.binding.viewpager2.isUserInputEnabled = false
//        holder.binding.viewpager2.setOnClickListener{
//            listener.itemClick(position)
//            Log.d("vipin","click")
//        }


            // Setup ViewPager and its adapter
            val viewPagerAdapter = ViewPagerAdapter(mutableListOf(),context, object :
                OnLogClickListener {
                override fun itemClick(items: MutableList<ViewpagerModel>) {
                    //  listener.itemClick(position)
                    mListener.onItemClick(position)
                }
            })

//        holder.binding.clHead.setOnClickListener {
//            mListener.onItemClick(position)
//        }

            binding.viewpager2.adapter = viewPagerAdapter
          binding.viewpager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL

            // Observe ViewModel inside fragment and update adapter with images
            imagePopViewModel.imageList.observe(lifecycleOwner, Observer { images ->
              //  viewPagerAdapter.updateItem(images)
            })
            // Tab layout mediator (no need to re-bind it every time)
            TabLayoutMediator(binding.tabLayoutForIndicator, binding.viewpager2) { _, _ -> }.attach()
            // Set TextView data
            binding.textHotelName.text = currentItem.textHotelName
            binding.textRating.text = currentItem.textRating
            binding.textTotal.text = currentItem.textTotal
            binding.textMiles.text = currentItem.textMiles
            binding.textPricePerHours.text = currentItem.textPricePerHours

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

    fun updateItem(newList : MutableList<HostListingModel>){
        this.list = newList
        notifyDataSetChanged()
    }

}