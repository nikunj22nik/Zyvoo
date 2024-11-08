package com.yesitlab.zyvo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yesitlab.zyvo.OnClickListener
import com.yesitlab.zyvo.databinding.LayoutImageMostUseBinding
import com.yesitlab.zyvo.model.AllGuidesModel

class AdapterAllGuides(
    var context: Context,
    private var items: ArrayList<AllGuidesModel>,  // Assuming a list of Strings for simplicity
    private val maxItemsToShow: Int? = null  // null means "show all items",
    ,private  val listener : OnClickListener
) : RecyclerView.Adapter<AdapterAllGuides.ItemViewHolder>() {

    inner class ItemViewHolder(var binding: LayoutImageMostUseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentItem: AllGuidesModel) {
            binding.imageHead.setImageResource(currentItem.image)
            binding.textDescription.setText(currentItem.text)

            binding.root.setOnClickListener{
                listener.itemClick(position)
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
    fun updateItem(items: ArrayList<AllGuidesModel>) {
        this.items = items
        notifyDataSetChanged()
    }

}