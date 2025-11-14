package com.business.zyvo.adapter.guest

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.AppConstant
import com.business.zyvo.R
import com.business.zyvo.databinding.AdapterAmentiesBinding

class AmenitiesAdapter(var context: Context, var list: MutableList<Pair<String, Boolean>>) : RecyclerView.Adapter<AmenitiesAdapter.ViewHolder>() {
    private lateinit var mListener: onItemClickListener
    private var isExpanded = false
    private var DEFAULT_VISIBLE_COUNT = 6

    interface onItemClickListener {
        fun onItemClick(list: MutableList<Pair<String, Boolean>>)
    }

    fun setOnItemClickListener(listener: AmenitiesAdapter.onItemClickListener) {
        mListener = listener
    }

    class ViewHolder(var binding: AdapterAmentiesBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterAmentiesBinding = AdapterAmentiesBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return if (isExpanded) list.size else minOf(DEFAULT_VISIBLE_COUNT, list.size)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position % 2 == 0) {
            holder.binding.layout.gravity = Gravity.START
        } else {
            holder.binding.layout.gravity = Gravity.END
        }

        if (list[position].second) {
            holder.binding.img.setImageResource(R.drawable.ic_checked_radio)
        } else {
            holder.binding.img.setImageResource(R.drawable.ic_uncheked_radio)
        }

        holder.binding.radioBtn.text = list[position].first
        Log.d("TESTING", "array value " + list[position].second)

        holder.binding.img.setOnClickListener {
            val currentItem = list[position]
            val newValue = !currentItem.second
            if (newValue) {
                when (currentItem.first) {
                    AppConstant.SMOKING_ALLOWED -> {
                        // Unselect Non-Smoking Property if it's selected
                        val nonSmokingIndex = list.indexOfFirst { it.first == AppConstant.NON_SMOKING_PROPERTY }
                        if (nonSmokingIndex != -1 && list[nonSmokingIndex].second) {
                            list[nonSmokingIndex] = list[nonSmokingIndex].copy(second = false)
                        }
                    }
                    AppConstant.NON_SMOKING_PROPERTY -> {
                        // Unselect Smoking Allowed if it's selected
                        val smokingIndex = list.indexOfFirst { it.first == AppConstant.SMOKING_ALLOWED }
                        if (smokingIndex != -1 && list[smokingIndex].second) {
                            list[smokingIndex] = list[smokingIndex].copy(second = false)
                        }
                    }
                }
            }

            list[position] = currentItem.copy(second = newValue)
            notifyDataSetChanged()
            mListener.onItemClick(list)
        }
    }

    fun toggleExpand() {
        isExpanded = !isExpanded
        notifyDataSetChanged()
    }

    fun changeDefaultCount( value:Int) {
        DEFAULT_VISIBLE_COUNT = value
        notifyDataSetChanged()
    }

    fun updateAdapter(list: MutableList<Pair<String, Boolean>>) {
        this.list = list
        notifyDataSetChanged()
    }
}

