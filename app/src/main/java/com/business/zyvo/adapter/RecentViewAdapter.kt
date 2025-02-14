package com.business.zyvo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.OnClickListener
import com.business.zyvo.databinding.LayoutRecentViewBinding
import com.business.zyvo.fragment.guest.home.model.WishlistItem
import com.business.zyvo.model.WishListModel

class RecentViewAdapter(
    var context: Context,
    var list: MutableList<WishlistItem>,
    var edit: Boolean,
    var listener: OnClickListener?
) :
    RecyclerView.Adapter<RecentViewAdapter.RecentViewtViewHolder>() {

    inner class RecentViewtViewHolder(var binding: LayoutRecentViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentItem: WishlistItem) {
            currentItem.last_saved_property_image.let {
                Glide.with(context).load(AppConstant.BASE_URL + it).into( binding.imageWishList)
            }
            currentItem.wishlist_name.let {
                binding.textTitle.setText(it)
            }

            binding.imageCross.setOnClickListener {
                listener?.itemClick(position)
            }

            if (edit) {
                binding.imageCross.visibility = View.VISIBLE
            } else {
                binding.imageCross.visibility = View.GONE
            }

        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewtViewHolder {
        val binding =
            LayoutRecentViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentViewtViewHolder(binding)

    }

    override fun getItemCount() = list.size
    override fun onBindViewHolder(holder: RecentViewtViewHolder, position: Int) {
        val currentItem = list.get(position)

        holder.bind(currentItem)
    }

    private fun removeItem(position: Int) {
        list.removeAt(position) // Remove the item from the list
        notifyItemRemoved(position) // Notify the adapter
        notifyItemRangeChanged(
            position,
            list.size
        ) // Optional: updates positions of remaining items
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItem(newItem: MutableList<WishlistItem>) {
        this.list = newItem
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateEditMode(isEdit: Boolean) {
        this.edit = isEdit
        notifyDataSetChanged() // Notify the adapter to refresh the views
    }

}