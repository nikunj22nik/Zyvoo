package com.yesitlab.zyvo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.kotlin.addressComponents
import com.yesitlab.zyvo.OnClickListener
import com.yesitlab.zyvo.databinding.LayoutArticlesBinding
import com.yesitlab.zyvo.model.AllArticlesModel

class AdapterAllArticles(var context: Context,
    private var list: ArrayList<AllArticlesModel>,
    private val maxItemsToShow: Int? = null,
    private  val listener : OnClickListener
) : RecyclerView.Adapter<AdapterAllArticles.ItemViewHolder>() {

    inner class ItemViewHolder(var binding: LayoutArticlesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentItem: AllArticlesModel) {
            binding.textTitle.setText(currentItem.text)
            binding.textDescription.setText(currentItem.text1)

            binding.root.setOnClickListener{
                listener.itemClick(adapterPosition)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            LayoutArticlesBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return maxItemsToShow?.let { minOf(it, list.size) } ?: list.size

    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = list[position]

        holder.bind(currentItem)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItem(list: ArrayList<AllArticlesModel>) {
        this.list = list
        notifyDataSetChanged()
    }

}