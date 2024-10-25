package com.yesitlab.zyvo.adapter.guest

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.yesitlab.zyvo.databinding.AdapterAmentiesBinding


class AmenitiesAdapter(var context : Context, var list :MutableList<String>) :RecyclerView.Adapter<AmenitiesAdapter.ViewHolder>() {

    class ViewHolder(var binding: AdapterAmentiesBinding) : RecyclerView.ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding:AdapterAmentiesBinding = AdapterAmentiesBinding.inflate(inflater,parent,false);
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position%2==0){
            holder.binding.layout.setGravity(Gravity.START)
        } else{
            holder.binding.layout.setGravity(Gravity.END)
        }
          holder.binding.radioBtn.setText(list.get(position))

    }

    fun updateAdapter(list :MutableList<String>){
        this.list = list
        notifyDataSetChanged()
    }


}