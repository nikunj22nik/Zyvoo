package com.yesitlab.zyvo.adapter

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
import com.yesitlab.zyvo.OnClickListener
import com.yesitlab.zyvo.OnClickListener1
import com.yesitlab.zyvo.OnLogClickListener
import com.yesitlab.zyvo.databinding.LayoutLoggedRecyclerviewBinding
import com.yesitlab.zyvo.model.LogModel
import com.yesitlab.zyvo.model.ViewpagerModel
import com.yesitlab.zyvo.viewmodel.ImagePopViewModel

class LoggedScreenAdapter(
    private val context: Context, private var list: MutableList<LogModel>, private val listener: OnClickListener,
    private val lifecycleOwner: LifecycleOwner, private val imagePopViewModel: ImagePopViewModel, private val listener2: OnClickListener1
) : RecyclerView.Adapter<LoggedScreenAdapter.LoggedViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: LoggedScreenAdapter.onItemClickListener) {
        mListener = listener
    }


    inner class LoggedViewHolder(val binding: LayoutLoggedRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoggedViewHolder {
        val binding = LayoutLoggedRecyclerviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoggedViewHolder(binding)
    }

    override fun getItemCount() = list.size

    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: LoggedViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val currentItem = list[position]



            holder.binding.imageAddWish.setOnClickListener{
                listener2.itemClick(position,"Add Wish")
            }




        holder.binding.cl1.setOnClickListener {
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

//        holder.binding.clHead.setOnClickListener {
//            mListener.onItemClick(position)
//        }

        holder.binding.viewpager2.adapter = viewPagerAdapter
        holder.binding.viewpager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        // Observe ViewModel inside fragment and update adapter with images
        imagePopViewModel.imageList.observe(lifecycleOwner, Observer { images ->
            viewPagerAdapter.updateItem(images)
        })
        // Tab layout mediator (no need to re-bind it every time)
        TabLayoutMediator(holder.binding.tabLayoutForIndicator, holder.binding.viewpager2) { _, _ -> }.attach()
        // Set TextView data
        holder.binding.textHotelName.text = currentItem.textHotelName
        holder.binding.textRating.text = currentItem.textRating
        holder.binding.textTotal.text = currentItem.textTotal
        holder.binding.textMiles.text = currentItem.textMiles
        holder.binding.textPricePerHours.text = currentItem.textPricePerHours
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: MutableList<LogModel>) {
        this.list = newList
        notifyDataSetChanged()
    }

}

