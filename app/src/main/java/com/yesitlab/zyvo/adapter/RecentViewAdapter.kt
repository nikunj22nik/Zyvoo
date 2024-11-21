package com.yesitlab.zyvo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yesitlab.zyvo.databinding.LayoutRecentViewBinding
import com.yesitlab.zyvo.model.WishListModel

class RecentViewAdapter(
    var context: Context,
    var list: MutableList<WishListModel>,
    var edit: Boolean
) :
    RecyclerView.Adapter<RecentViewAdapter.RecentViewtViewHolder>() {

    inner class RecentViewtViewHolder(var binding: LayoutRecentViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentItem: WishListModel) {
            binding.imageWishList.setImageResource(currentItem.image)

            binding.textTitle.setText(currentItem.text1)


            binding.imageCross.setOnClickListener {
                removeItem(position)
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

    fun updateItem(newItem: MutableList<WishListModel>) {
        this.list = newItem
        notifyDataSetChanged()
    }

    fun updateEditMode(isEdit: Boolean) {
        this.edit = isEdit
        notifyDataSetChanged() // Notify the adapter to refresh the views
    }

}