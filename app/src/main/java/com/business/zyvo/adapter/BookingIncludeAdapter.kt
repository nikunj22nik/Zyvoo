package com.business.zyvo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.databinding.ItemIncludeLayoutBinding

class BookingIncludeAdapter(
    private var list: MutableList<String>
) : RecyclerView.Adapter<BookingIncludeAdapter.IncludeViewHolder>() {

    inner class IncludeViewHolder(val binding: ItemIncludeLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.titleTextView.text = title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncludeViewHolder {
        val binding = ItemIncludeLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IncludeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IncludeViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    fun updateItems(newList: List<String>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
