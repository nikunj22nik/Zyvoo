package com.yesitlab.zyvo.adapter.host

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yesitlab.zyvo.databinding.AdapterBankNameBinding
import com.yesitlab.zyvo.databinding.LanguageAdapterBinding
import com.yesitlab.zyvo.model.CountryLanguage

class BankNameAdapter(private val context: Context, private var list: List<CountryLanguage>) :
    RecyclerView.Adapter<BankNameAdapter.ViewHolder>(){

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }
    fun setOnItemClickListener(listener: BankNameAdapter.onItemClickListener) {
        mListener = listener
    }

    class ViewHolder(var binding: AdapterBankNameBinding) : RecyclerView.ViewHolder(binding.root){}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankNameAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding:AdapterBankNameBinding = AdapterBankNameBinding.inflate(inflater,parent,false);
        return BankNameAdapter.ViewHolder(binding)
    }

    override fun getItemCount(): Int {
      return 2
    }

    override fun onBindViewHolder(holder: BankNameAdapter.ViewHolder, position: Int) {
        if(position ==0) {
            holder.binding.rlPreferred.visibility = View.VISIBLE
        }

    }


}