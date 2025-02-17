package com.business.zyvo.adapter.guest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.databinding.AdapterReviewsBinding
import com.business.zyvo.model.host.HostReviewModel

class AdapterReview(var context : Context, var list :MutableList<HostReviewModel> = mutableListOf()) :
RecyclerView.Adapter<AdapterReview.ViewHolder>()
{
    var count =4

    class ViewHolder(var binding: AdapterReviewsBinding) : RecyclerView.ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding:AdapterReviewsBinding = AdapterReviewsBinding.inflate(inflater,parent,false);
        return AdapterReview.ViewHolder(binding)
    }

    override fun getItemCount(): Int {
     return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position == list.size-1) {

        }
    }

    fun updateAdapter(list :MutableList<HostReviewModel>){
        this.list = list
        notifyDataSetChanged()
    }

  }