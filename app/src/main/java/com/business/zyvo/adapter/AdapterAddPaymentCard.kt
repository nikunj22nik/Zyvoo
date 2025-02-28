package com.business.zyvo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.activity.guest.checkout.model.UserCards
import com.business.zyvo.databinding.LayoutPaymentCardlistBinding
import com.business.zyvo.model.AddPaymentCardModel
import com.stripe.android.model.CardBrand

class AdapterAddPaymentCard(var context : Context, var list: MutableList<UserCards> ,val setpreferred:SetPreferred)
    : RecyclerView.Adapter<AdapterAddPaymentCard.PaymentCardViewHolder>() {


    inner  class PaymentCardViewHolder(var binding: LayoutPaymentCardlistBinding)
        : RecyclerView.ViewHolder(binding.root)
    {
        fun bind(currentItem: UserCards) {
       binding.textCardNumber.setText("......"+currentItem.last4)
            try {
                Log.d("*******","name :- "+currentItem.brand)
                val icon: Int = CardBrand.valueOf(currentItem.brand!!).icon
                binding.textCardNumber.setCompoundDrawablesWithIntrinsicBounds(context.getDrawable(icon), null, null, null)
            }catch (e:java.lang.Exception){
            }

            if (currentItem.is_preferred){
                binding.textPreferred.visibility = View.VISIBLE
            }else{
                binding.textPreferred.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                setpreferred.set(position)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentCardViewHolder {
       var binding = LayoutPaymentCardlistBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return  PaymentCardViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: PaymentCardViewHolder, position: Int) {
        var currentItem = list[position]
        holder.bind(currentItem)



    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateItem(list: MutableList<UserCards>)
    {
        this.list = list
        notifyDataSetChanged()
    }


}

interface SetPreferred {
    fun set(position: Int)

}
