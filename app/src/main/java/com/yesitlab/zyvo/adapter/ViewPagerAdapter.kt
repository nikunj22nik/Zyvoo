package com.yesitlab.zyvo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.yesitlab.zyvo.OnLogClickListener
import com.yesitlab.zyvo.databinding.LayoutImageBinding
import com.yesitlab.zyvo.model.ViewpagerModel

class ViewPagerAdapter(private var list: MutableList<ViewpagerModel>, var context: Context,
                       var listner: OnLogClickListener?
) : RecyclerView.Adapter<ViewPagerAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: LayoutImageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = list[position]
        holder.binding.image.setImageResource(currentItem.image)


        holder.itemView.setOnClickListener {

            listner?.itemClick(list)
        }


    }

    override fun getItemCount() = list.size

    fun updateItem(newList: MutableList<ViewpagerModel>) {
        this.list = newList
        notifyDataSetChanged()
    }
}
