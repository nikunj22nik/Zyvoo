package com.yesitlab.zyvo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yesitlab.zyvo.databinding.AdapterActicityTextBinding
import com.yesitlab.zyvo.databinding.AdapterActivitiesTextViewBinding
import com.yesitlab.zyvo.databinding.AdapterLocationSearchBinding

class AdapterActivityText(var context : Context, var list : MutableList<String>) : RecyclerView.Adapter<AdapterActivityText.AdapterActivityTextViewHolder>() {

    inner class AdapterActivityTextViewHolder(val binding: AdapterActicityTextBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterActivityTextViewHolder {
        val binding = AdapterActicityTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdapterActivityTextViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: AdapterActivityTextViewHolder, position: Int) {
        holder.binding.tvStays.setText(list.get(position))
    }

    fun updateAdapter(list:MutableList<String>){
        this.list = list
        notifyDataSetChanged()
    }


}