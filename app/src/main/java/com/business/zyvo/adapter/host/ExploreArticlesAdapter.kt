package com.business.zyvo.adapter.host

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.business.zyvo.databinding.AdapterExploreArticlesBinding

import com.business.zyvo.model.TransactionModel

class ExploreArticlesAdapter(private var transactionsList: MutableList<TransactionModel>) :
    RecyclerView.Adapter<ExploreArticlesAdapter.ViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: ExploreArticlesAdapter.onItemClickListener) {
        mListener = listener
    }


    class ViewHolder(var binding: AdapterExploreArticlesBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreArticlesAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterExploreArticlesBinding = AdapterExploreArticlesBinding.inflate(inflater,parent,false);
        return ExploreArticlesAdapter.ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun onBindViewHolder(holder: ExploreArticlesAdapter.ViewHolder, position: Int) {

        holder.binding.main.setOnClickListener {
            mListener.onItemClick(position)
        }
    }


}