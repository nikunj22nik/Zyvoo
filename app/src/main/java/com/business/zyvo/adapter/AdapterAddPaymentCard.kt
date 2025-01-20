package com.business.zyvo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.databinding.LayoutPaymentCardlistBinding
import com.business.zyvo.model.AddPaymentCardModel

class AdapterAddPaymentCard(var context : Context, var list: MutableList<AddPaymentCardModel> ) : RecyclerView.Adapter<AdapterAddPaymentCard.PaymentCardViewHolder>() {


    inner  class PaymentCardViewHolder(var binding: LayoutPaymentCardlistBinding) : RecyclerView.ViewHolder(binding.root)
    {
        fun bind(currentItem: AddPaymentCardModel) {
       binding.textCardNumber.setText(currentItem.textString)

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentCardViewHolder {
       var binding = LayoutPaymentCardlistBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return  PaymentCardViewHolder(binding)
    }

    override fun getItemCount() = 4

    override fun onBindViewHolder(holder: PaymentCardViewHolder, position: Int) {
        var currentItem = list[position]
        holder.bind(currentItem)

        if (position == 0){
            holder.binding.textPreferred.visibility = View.VISIBLE
        }else{
            holder.binding.textPreferred.visibility = View.GONE
        }

    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateItem(list: MutableList<AddPaymentCardModel>)
    {
        this.list = list
        notifyDataSetChanged()
    }


}