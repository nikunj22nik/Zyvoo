package com.business.zyvo.adapter.guest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.databinding.AdapterReviewsBinding

class AdapterReview(var context : Context, var list :MutableList<String>) :
RecyclerView.Adapter<AdapterReview.ViewHolder>()
{
    var count =4

    class ViewHolder(var binding: AdapterReviewsBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding:AdapterReviewsBinding = AdapterReviewsBinding.inflate(inflater,parent,false);
        return AdapterReview.ViewHolder(binding)
    }

    override fun getItemCount(): Int {
     return count
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position == count-1) {
            holder.binding.v1.visibility = View.GONE
        }
    }

    fun updateAdapter(currentCount :Int){
        count = currentCount
        notifyDataSetChanged()
    }

  }