package com.yesitlab.zyvo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yesitlab.zyvo.databinding.AdapterLocationSearchBinding
import com.yesitlab.zyvo.databinding.LayoutLoggedRecyclerviewBinding
import com.yesitlab.zyvo.model.AddHobbiesModel

class AdapterLocationSearch(var context : Context,var list : MutableList<String> ) : RecyclerView.Adapter<AdapterLocationSearch.AdapterLocationViewHolder>()
{
    inner class AdapterLocationViewHolder(val binding: AdapterLocationSearchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterLocationViewHolder {
        val binding = AdapterLocationSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdapterLocationViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder(holder: AdapterLocationViewHolder, position: Int) {
        holder.binding.tvLocation.setText(list.get(position))
    }

    fun updateAdapter(list : MutableList<String>){
        this.list = list
        notifyDataSetChanged()
    }


}