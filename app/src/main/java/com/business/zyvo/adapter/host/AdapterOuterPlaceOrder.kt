package com.business.zyvo.adapter.host

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.databinding.AdapterOuterPlaceOrderBinding

class AdapterOuterPlaceOrder(private var context : Context, private var list: MutableList<Pair<String, List<String>>>) :
    RecyclerView.Adapter<AdapterOuterPlaceOrder.ViewHolder>() {
    private var scrollPosition = 0


    class ViewHolder(var binding: AdapterOuterPlaceOrderBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterOuterPlaceOrder.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterOuterPlaceOrderBinding = AdapterOuterPlaceOrderBinding.inflate(inflater, parent, false);
        return AdapterOuterPlaceOrder.ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: AdapterOuterPlaceOrder.ViewHolder, position: Int) {
       val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
      //  val layoutManager = NoHorizontalScrollLayoutManager(context)
        holder.binding.recyclerInnerPlaceOrder.layoutManager = layoutManager

        var adapter = AdapterInnerPlaceOrder(list.get(position).second)
       if(position ==0){
           holder.binding.tvTime.visibility = View.INVISIBLE
            adapter.firstRow = true
       }
        else {
           holder.binding.tvTime.visibility = View.VISIBLE
           holder.binding.tvTime.setText(list.get(position).first)
           adapter.firstRow = false
       }

        holder.binding.recyclerInnerPlaceOrder.adapter = adapter

        // Set the current scroll position
        holder.binding.recyclerInnerPlaceOrder.scrollToPosition(scrollPosition)

        // Add a scroll listener to sync scrolling
        holder.binding.recyclerInnerPlaceOrder.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                scrollPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                syncScrollAll(holder.itemView.parent as RecyclerView, holder.binding.recyclerInnerPlaceOrder)
            }
        })


    }

    private fun syncScrollAll(parentRecyclerView: RecyclerView, scrolledRecyclerView: RecyclerView) {
        for (i in 0 until itemCount) {
            val viewHolder = parentRecyclerView.findViewHolderForAdapterPosition(i) as? ViewHolder
            viewHolder?.binding?.recyclerInnerPlaceOrder?.let { innerRecyclerView ->
                if (innerRecyclerView !== scrolledRecyclerView) {
                    innerRecyclerView.scrollToPosition(scrollPosition)
                }
            }
        }
    }




}