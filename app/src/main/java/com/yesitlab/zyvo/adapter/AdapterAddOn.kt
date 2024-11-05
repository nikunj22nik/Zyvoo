package com.yesitlab.zyvo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.databinding.AdapterAdOnBinding

class AdapterAddOn(var context: Context, var list : MutableList<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ViewHolder(var binding: AdapterAdOnBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding:AdapterAdOnBinding = AdapterAdOnBinding.inflate(inflater,parent,false);
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
      var tvName = holder.itemView.findViewById<TextView>(R.id.tv_name)
      tvName.setText(list.get(position))
    }


    fun updateAdapter(list : MutableList<String>){
        this.list = list
        notifyDataSetChanged()
    }

 }