package com.business.zyvo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.databinding.AdapterActicityTextBinding

class AdapterActivityText(var context : Context, var list : MutableList<String>) : RecyclerView.Adapter<AdapterActivityText.AdapterActivityTextViewHolder>() {

    inner class AdapterActivityTextViewHolder(val binding: AdapterActicityTextBinding) : RecyclerView.ViewHolder(binding.root)


    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: AdapterActivityText.onItemClickListener) {
        mListener = listener
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterActivityTextViewHolder {
        val binding = AdapterActicityTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdapterActivityTextViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: AdapterActivityTextViewHolder, position: Int) {

        holder.binding.tvStays.setText(list.get(position))

        holder.binding.llMain.setOnClickListener {
           mListener.onItemClick(position)
        }

    }


    fun updateAdapter(list:MutableList<String>){
        this.list = list
        notifyDataSetChanged()
    }


}