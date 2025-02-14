package com.business.zyvo.adapter.host

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.databinding.AdapterInnerPlaceOrderBinding
import com.business.zyvo.databinding.RecyclerIncludeInBookingBinding

class AdapterIncludeInBooking(private var list: List<String>,context: Context) : RecyclerView.Adapter<AdapterIncludeInBooking.ViewHolder>(){


    class ViewHolder(var binding: RecyclerIncludeInBookingBinding) : RecyclerView.ViewHolder(binding.root){}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterIncludeInBooking.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: RecyclerIncludeInBookingBinding = RecyclerIncludeInBookingBinding.inflate(inflater,parent,false);
        return AdapterIncludeInBooking.ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdapterIncludeInBooking.ViewHolder, position: Int) {
      holder.binding.tvAmenities.setText(list.get(position))
    }

    override fun getItemCount(): Int {
      return list.size
    }

    fun updateAdapter(list: List<String>){
        this.list = list;
        notifyDataSetChanged()
    }


}