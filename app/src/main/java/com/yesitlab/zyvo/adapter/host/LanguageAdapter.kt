package com.yesitlab.zyvo.adapter.host

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.databinding.LanguageAdapterBinding
import com.yesitlab.zyvo.model.CountryLanguage

class LanguageAdapter(private val context: Context, private var list: List<CountryLanguage>) :
RecyclerView.Adapter<LanguageAdapter.ViewHolder>()
{
    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: LanguageAdapter.onItemClickListener) {
        mListener = listener
    }

    class ViewHolder(var binding: LanguageAdapterBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding:LanguageAdapterBinding = LanguageAdapterBinding.inflate(inflater,parent,false);
        return LanguageAdapter.ViewHolder(binding)
    }

    override fun getItemCount(): Int {
      return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvLanguage.setText(list.get(position).language)
        holder.binding.tvCountryName.setText(list.get(position).country)

        holder.binding.mainBg.setOnClickListener {
            holder.binding.mainBg.setBackgroundResource(R.drawable.blue_button_bg)
        }
    }

}