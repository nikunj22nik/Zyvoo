package com.yesitlab.zyvo.adapter.host

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yesitlab.zyvo.adapter.host.BankNameAdapter
import com.yesitlab.zyvo.databinding.AdapterBankNameBinding
import com.yesitlab.zyvo.databinding.AdapterExploreArticlesBinding
import com.yesitlab.zyvo.databinding.LayoutPaymentDetailsBinding
import com.yesitlab.zyvo.model.TransactionModel

class ExploreArticlesAdapter(private var transactionsList: MutableList<TransactionModel>) :
    RecyclerView.Adapter<ExploreArticlesAdapter.ViewHolder>() {

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

    }


}