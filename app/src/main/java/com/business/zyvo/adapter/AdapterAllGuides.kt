package com.business.zyvo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.BuildConfig
import com.business.zyvo.OnClickListener
import com.business.zyvo.OnClickListener1
import com.business.zyvo.R
import com.business.zyvo.databinding.LayoutImageMostUseBinding
import com.business.zyvo.fragment.guest.helpCenter.model.Guide
import com.business.zyvo.model.AllGuidesModel

class AdapterAllGuides(
    var context: Context,
    private var items: MutableList<Guide>,  // Assuming a list of Strings for simplicity
    private val maxItemsToShow: Int? = null  // null means "show all items",
    , private val listener: OnClickListener1
) : RecyclerView.Adapter<AdapterAllGuides.ItemViewHolder>() {

    inner class ItemViewHolder(var binding: LayoutImageMostUseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentItem: Guide) {
            if (currentItem.cover_image != null) {
                Glide.with(context)
                    .load(BuildConfig.MEDIA_URL + currentItem.cover_image)
                    .centerCrop()
                    .error(R.drawable.ic_img_not_found)
                    .placeholder(R.drawable.ic_img_not_found)
                    .into(binding.imageHead)
            }


            if (currentItem.title != null) {
                binding.textDescription.setText(currentItem.title)
            }


            binding.root.setOnClickListener {
                if (currentItem.id != null){
                    listener.itemClick(currentItem.id, AppConstant.GUIDES)
                }

            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            LayoutImageMostUseBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return maxItemsToShow?.let { minOf(it, items.size) } ?: items.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = items[position]

        holder.bind(currentItem)
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateItem(items: MutableList<Guide>) {
        this.items = items
        notifyDataSetChanged()
    }

}