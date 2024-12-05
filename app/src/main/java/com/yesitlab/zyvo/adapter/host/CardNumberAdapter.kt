package com.yesitlab.zyvo.adapter.host

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yesitlab.zyvo.databinding.AdapterBankNameBinding
import com.yesitlab.zyvo.databinding.AdapterCardNumbersBinding
import com.yesitlab.zyvo.model.CountryLanguage

class CardNumberAdapter (private val context: Context, private var list: List<CountryLanguage>) :
    RecyclerView.Adapter<CardNumberAdapter.ViewHolder>(){

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: CardNumberAdapter.onItemClickListener) {
        mListener = listener
    }

    class ViewHolder(var binding: AdapterCardNumbersBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding:AdapterCardNumbersBinding = AdapterCardNumbersBinding.inflate(inflater,parent,false);
        return CardNumberAdapter.ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return 2
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }


}