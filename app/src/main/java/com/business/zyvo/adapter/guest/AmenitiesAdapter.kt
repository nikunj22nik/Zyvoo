package com.business.zyvo.adapter.guest

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.R
import com.business.zyvo.adapter.AdapterActivityText
import com.business.zyvo.databinding.AdapterAmentiesBinding


class AmenitiesAdapter(var context : Context, var list :MutableList<Pair<String,Boolean>>) :RecyclerView.Adapter<AmenitiesAdapter.ViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(list :MutableList<Pair<String,Boolean>>)
    }

    fun setOnItemClickListener(listener: AmenitiesAdapter.onItemClickListener) {
        mListener = listener
    }


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


        holder.binding.radioBtn.setText(list.get(position).first)
        Log.d("TESTING","array value "+  list.get(position).second)

        holder.binding.img.setOnClickListener {
            var pair = list.get(position)
//            Log.d("TESTING", holder.binding.img.drawable.toString())
//            Log.d("TESTING",R.drawable.ic_uncheked_radio)

           if(!pair.second){
               holder.binding.img.setImageResource(R.drawable.ic_checked_radio)
               val newValue =Pair(pair.first,true)
               list.set(position,newValue)
               mListener.onItemClick(list)
           }else{
               holder.binding.img.setImageResource(R.drawable.ic_uncheked_radio)
               val newValue =Pair(pair.first,false)
               list.set(position,newValue)
               mListener.onItemClick(list)
           }
        }
    }

    fun updateAdapter(list :MutableList<Pair<String,Boolean>>){
        this.list = list
        notifyDataSetChanged()
    }


}