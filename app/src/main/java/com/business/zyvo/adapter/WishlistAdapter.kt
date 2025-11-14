package com.business.zyvo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.BuildConfig
import com.business.zyvo.OnClickListener
import com.business.zyvo.R
import com.business.zyvo.adapter.host.ExploreArticlesAdapter
import com.business.zyvo.databinding.LayoutWishlistBinding
import com.business.zyvo.fragment.guest.home.model.WishlistItem
import com.business.zyvo.model.WishListModel

class WishlistAdapter(var context: Context, private val isSmall: Boolean,
                      var list: MutableList<WishlistItem>,
                      var edit: Boolean,
                      var listener: OnClickListener?) :
    RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int,wish: WishlistItem)
    }

    fun setOnItemClickListener(listener: WishlistAdapter.onItemClickListener) {
        mListener = listener
    }

    inner class WishlistViewHolder(var binding: LayoutWishlistBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentItem: WishlistItem) {

            currentItem.last_saved_property_image.let {
                Glide.with(context).load(BuildConfig.MEDIA_URL + it).into( binding.imageWishList)
            }

            currentItem.items_in_wishlist.let {
                binding.textSaved.text = "$it saved"
            }

            currentItem.wishlist_name.let {
                binding.textTitle.setText(it)
            }

            binding.root.setOnClickListener {
               // listener?.itemClick(position)
            mListener.onItemClick(position,currentItem)
            }
            binding.imageCross.setOnClickListener {
                listener?.itemClick(position)
            }


            binding.imageWishList.requestLayout()

            if (edit) {
                binding.imageCross.visibility = View.VISIBLE
            } else {
                binding.imageCross.visibility = View.GONE
            }

        }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val binding =
            LayoutWishlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WishlistViewHolder(binding)

    }

    override fun getItemCount() = list.size
    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        val currentItem = list.get(position)
        holder.bind(currentItem)
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateItem(newItem : MutableList<WishlistItem>){
        this.list = newItem
        notifyDataSetChanged()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateEditMode(isEdit: Boolean) {
        this.edit = isEdit
        notifyDataSetChanged() // Notify the adapter to refresh the views
    }
}