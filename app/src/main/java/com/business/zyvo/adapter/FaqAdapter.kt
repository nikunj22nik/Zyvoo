package com.business.zyvo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.R
import com.business.zyvo.databinding.ItemFaqBinding
import com.business.zyvo.fragment.both.faq.model.FaqModel

class FaqAdapter(var context : Context , var list : MutableList<FaqModel>) : RecyclerView.Adapter<FaqAdapter.FaqViewHolder>(){


    inner class FaqViewHolder( var binding : ItemFaqBinding) :RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val binding = ItemFaqBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return FaqViewHolder(binding)
    }

    override fun getItemCount()= list.size

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
       val currentItem = list[position]
        holder.binding.textFaqQuestion.text = currentItem.question
        holder.binding.textFaqAnswer.text = currentItem.answer
        var open = true
        holder.binding.imageIcon.setOnClickListener {
            if (open){
                open = false
                holder.binding.imageIcon.setImageResource(R.drawable.ic_hide_faq)
                holder.binding.textFaqAnswer.visibility = View.VISIBLE
            }else{
                open = true
                holder.binding.imageIcon.setImageResource(R.drawable.ic_show_faq)
                holder.binding.textFaqAnswer.visibility = View.GONE
            }
        }

    }


    fun updateItem(newList: MutableList<FaqModel>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

}