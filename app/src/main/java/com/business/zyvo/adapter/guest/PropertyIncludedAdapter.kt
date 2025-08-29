package com.business.zyvo.adapter.guest

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.databinding.LayoutIncludedBinding
import com.business.zyvo.model.ActivityModel

class PropertyIncludedAdapter(var context : Context, var list :List<String>) :
RecyclerView.Adapter<PropertyIncludedAdapter.ViewHolder>()
{

    class ViewHolder(var binding: LayoutIncludedBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding:LayoutIncludedBinding = LayoutIncludedBinding.inflate(inflater,parent,false);
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
     return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       list.get(position)?.let {
           val formatted = it.replace("-", "- ")
           holder.binding.tvincluded.text = formatted
       }
    }

  }