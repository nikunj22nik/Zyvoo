package com.yesitlab.zyvo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yesitlab.zyvo.OnClickListener
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.databinding.LayoutWishlistBinding
import com.yesitlab.zyvo.model.WishListModel

class WishlistAdapter(var context: Context, private val isSmall: Boolean, var list: MutableList<WishListModel>, var listener: OnClickListener?) :
    RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    inner class WishlistViewHolder(var binding: LayoutWishlistBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentItem: WishListModel) {
            binding.imageWishList.setImageResource(currentItem.image)
            binding.textSaved.setText(currentItem.text2)
            binding.textTitle.setText(currentItem.text1)

            binding.root.setOnClickListener {
                listener?.itemClick(position)
            }


          //   Adjust size based on isSmall flag using dimensions from dimens.xml
            val resources = itemView.context.resources
            val (imageWidth, imageHeight, textSize) = if (isSmall) {
                Triple(
                    resources.getDimensionPixelSize(R.dimen.imageWishListWidthSmall),
                    resources.getDimensionPixelSize(R.dimen.imageWishListHeightSmall),
                    resources.getDimension(R.dimen.text_small_size)
                )
            } else {
                Triple(
                    resources.getDimensionPixelSize(R.dimen.imageWishListWidthLarge),
                    resources.getDimensionPixelSize(R.dimen.imageWishListHeightLarge),
                    resources.getDimension(R.dimen.text_large_size)
                )
            }

            // Apply dimensions to ImageView
            binding.imageWishList.layoutParams.apply {
                width = imageWidth
                height = imageHeight
            }
            binding.imageWishList.requestLayout()

            // Apply text size to TextViews
           // binding.textSaved.textSize = textSize
         //   binding.textTitle.textSize = textSize

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
    fun updateItem(newItem : MutableList<WishListModel>){
        this.list = newItem
        notifyDataSetChanged()
    }
}