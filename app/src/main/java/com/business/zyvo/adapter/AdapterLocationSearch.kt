package com.business.zyvo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.databinding.AdapterLocationSearchBinding

class AdapterLocationSearch(var context : Context,var list : MutableList<String> ) : RecyclerView.Adapter<AdapterLocationSearch.AdapterLocationViewHolder>()
{
    private var onItemClickListener: ((String) -> Unit)? = null
    inner class AdapterLocationViewHolder(val binding: AdapterLocationSearchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterLocationViewHolder {
        val binding = AdapterLocationSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdapterLocationViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder(holder: AdapterLocationViewHolder, position: Int) {

        val suggestion = list[position]
        holder.binding.tvLocation.setText(list.get(position))


        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(suggestion)
        }

//        holder.binding.tvLocation.setOnClickListener {
//            listner.itemClick(position,"selected location")
//        }
    }

    fun updateAdapter(list : MutableList<String>){
        this.list = list
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (String) -> Unit) {
        onItemClickListener = listener
    }

}