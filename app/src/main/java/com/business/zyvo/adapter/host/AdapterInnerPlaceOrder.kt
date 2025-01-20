package com.business.zyvo.adapter.host

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.databinding.AdapterInnerPlaceOrderBinding


class AdapterInnerPlaceOrder(private var list: List<String>) :
    RecyclerView.Adapter<AdapterInnerPlaceOrder.ViewHolder>(){
        var firstRow = false

    class ViewHolder(var binding: AdapterInnerPlaceOrderBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterInnerPlaceOrderBinding = AdapterInnerPlaceOrderBinding.inflate(inflater,parent,false);
        return AdapterInnerPlaceOrder.ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(firstRow){
            holder.binding.rlDateView.visibility = View.VISIBLE
            holder.binding.cardOne.visibility = View.GONE
            holder.binding.tvDate.setText(list.get(position).toString())
        }
        else{
             holder.binding.rlDateView.visibility = View.GONE
             holder.binding.cardOne.visibility = View.VISIBLE
             if(list.get(position).length ==0){
                holder.binding.llContent.visibility = View.GONE
                holder.binding.subtraO14.visibility = View.GONE
                val tint = ColorStateList.valueOf(android.graphics.Color.parseColor("#008000")) // Red color
                holder.binding.subtraO14.setBackgroundTintList(tint)
             }
             else{
                 holder.binding.llContent.visibility = View.VISIBLE
                 holder.binding.subtraO14.visibility = View.VISIBLE
                 val tint = ColorStateList.valueOf(android.graphics.Color.parseColor("#008000")) // Red color
                 holder.binding.subtraO14.setBackgroundTintList(tint)
             }
        }
    }

}