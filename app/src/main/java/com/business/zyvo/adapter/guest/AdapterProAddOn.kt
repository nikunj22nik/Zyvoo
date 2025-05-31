package com.business.zyvo.adapter.guest

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.R
import com.business.zyvo.activity.guest.propertydetails.model.AddOn
import com.business.zyvo.adapter.guest.AmenitiesAdapter.onItemClickListener
import com.business.zyvo.databinding.AdapterAdOnBinding

class AdapterProAddOn(var context: Context, var list : MutableList<AddOn>,
                     val listener: onItemClickListener) :
    RecyclerView.Adapter<AdapterProAddOn.ViewHolder>() {

    private var isExpanded = false  // Controls the "See More" state
    private var displayList: List<AddOn> = list.take(4) // Initially show 4 items
    class ViewHolder(var binding: AdapterAdOnBinding) : RecyclerView.ViewHolder(binding.root){}
    interface onItemClickListener {
        fun onItemClick(list :MutableList<AddOn>,position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding:AdapterAdOnBinding = AdapterAdOnBinding.inflate(inflater,parent,false);
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return displayList.size
    }

    override fun onBindViewHolder(holder:ViewHolder, position: Int) {
        displayList.get(position)?.name.let {
           holder.binding.tvName.text = it
        }
        displayList.get(position)?.price.let {
            val rPrice = it?.toDouble()?.toInt().toString()
            holder.binding.tvPrice.text = "$$rPrice / Item"
        }
        if(list.get(position).checked){
            holder.binding.laypout.setBackgroundResource(R.drawable.bg_four_side_selected_blue)
        }else{
            holder.binding.laypout.setBackgroundResource(R.drawable.bg_four_side_corner_grey_slight_more)
        }
        holder.binding.laypout.setOnClickListener {
            if (!list.get(position).checked) {
                holder.binding.laypout.setBackgroundResource(R.drawable.bg_four_side_selected_blue)
                val pair = list.get(position)
                pair.checked = true
                list.set(position,pair)
                listener?.onItemClick(list,position)
            }
            else{
                holder.binding.laypout.setBackgroundResource(R.drawable.bg_four_side_corner_grey_slight_more)
                val pair = list.get(position)
                pair.checked = false
                list.set(position,pair)
                listener?.onItemClick(list,position)
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(list : MutableList<AddOn>){
        this.list = list
        displayList = list.take(4)
        notifyDataSetChanged()
    }

    // Function to toggle list expansion
    @SuppressLint("NotifyDataSetChanged")
    fun toggleList() {
        isExpanded = !isExpanded
        displayList = if (isExpanded) list else list.take(4)
        notifyDataSetChanged()
    }

 }