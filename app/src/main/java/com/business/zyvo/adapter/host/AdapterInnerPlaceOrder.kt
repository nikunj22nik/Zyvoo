package com.business.zyvo.adapter.host

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.databinding.AdapterInnerPlaceOrderBinding
/*
class AdapterInnerPlaceOrder(
    private var list: List<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_DATE = 0
    private val VIEW_TYPE_CONTENT = 1
    var firstRow = false

    // To store the scroll position
    private var scrollXPosition = 0

    override fun getItemViewType(position: Int): Int {
        return if (firstRow) VIEW_TYPE_DATE else VIEW_TYPE_CONTENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_TYPE_DATE -> {
                val binding = AdapterInnerPlaceOrderBinding.inflate(inflater, parent, false)
                DateViewHolder(binding)
            }
            else -> {
                val binding = AdapterInnerPlaceOrderBinding.inflate(inflater, parent, false)
                ContentViewHolder(binding)
            }
        }
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DateViewHolder -> holder.bind(list[position])
            is ContentViewHolder -> holder.bind(list[position], scrollXPosition)
        }
    }

    inner class DateViewHolder(private val binding: AdapterInnerPlaceOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(date: String) {
            binding.rlDateView.visibility = View.VISIBLE
            binding.cardOne.visibility = View.GONE
            binding.tvDate.text = date
        }
    }

    inner class ContentViewHolder(private val binding: AdapterInnerPlaceOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String, scrollPosition: Int) {
            binding.rlDateView.visibility = View.GONE
            binding.cardOne.visibility = View.VISIBLE

            if (item.isEmpty()) {
                binding.llContent.visibility = View.GONE
                binding.subtraO14.visibility = View.GONE
            } else {
                binding.llContent.visibility = View.VISIBLE
                binding.subtraO14.visibility = View.VISIBLE
            }

            // Set background tint
            val tint = ColorStateList.valueOf(Color.parseColor("#008000"))
            binding.subtraO14.backgroundTintList = tint

            // Ensure all rows sync scrolling
            binding.horizontalScrollView.scrollTo(scrollPosition, 0)

            binding.horizontalScrollView.viewTreeObserver.addOnScrollChangedListener {
                scrollXPosition = binding.horizontalScrollView.scrollX
                notifyDataSetChanged()
            }
        }
    }
}


 */
/*
class AdapterInnerPlaceOrder(private var list: List<String>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_DATE = 0
    private val VIEW_TYPE_CONTENT = 1

    var firstRow = false

    override fun getItemViewType(position: Int): Int {
        return if (firstRow) VIEW_TYPE_DATE else VIEW_TYPE_CONTENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_TYPE_DATE -> {
                val binding = AdapterInnerPlaceOrderBinding.inflate(inflater, parent, false)
                DateViewHolder(binding)
            }
            else -> {
                val binding = AdapterInnerPlaceOrderBinding.inflate(inflater, parent, false)
                ContentViewHolder(binding)
            }
        }
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DateViewHolder -> holder.bind(list[position])
            is ContentViewHolder -> holder.bind(list[position])
        }
    }


    class DateViewHolder(private val binding: AdapterInnerPlaceOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(date: String) {
            binding.rlDateView.visibility = View.VISIBLE
            binding.cardOne.visibility = View.GONE
            binding.tvDate.text = date
        }
    }


    class ContentViewHolder(private val binding: AdapterInnerPlaceOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.rlDateView.visibility = View.GONE
            binding.cardOne.visibility = View.VISIBLE

            if (item.isEmpty()) {
                binding.llContent.visibility = View.GONE
                binding.subtraO14.visibility = View.GONE
            } else {
                binding.llContent.visibility = View.VISIBLE
                binding.subtraO14.visibility = View.VISIBLE
            }

            // Set background tint
            val tint = ColorStateList.valueOf(Color.parseColor("#008000"))
            binding.subtraO14.backgroundTintList = tint
        }
    }
}

 */


class AdapterInnerPlaceOrder(private var list: List<String>) :
    RecyclerView.Adapter<AdapterInnerPlaceOrder.ViewHolder>(){
        var firstRow = false
    var scrollXPosition = 0
    class ViewHolder(var binding: AdapterInnerPlaceOrderBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterInnerPlaceOrderBinding = AdapterInnerPlaceOrderBinding.inflate(inflater,parent,false);
        return AdapterInnerPlaceOrder.ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(firstRow){
            holder.binding.rlDateView.visibility = View.VISIBLE
            holder.binding.cardOne.visibility = View.GONE
            holder.binding.tvDate.setText(list.get(position).toString())
        }
        else{
             holder.binding.rlDateView.visibility = View.GONE
             holder.binding.cardOne.visibility = View.VISIBLE
             if(list.get(position).length ==0){
                holder.binding.llContent.visibility = View.GONE
                holder.binding.subtraO14.visibility = View.GONE
                val tint = ColorStateList.valueOf(android.graphics.Color.parseColor("#008000")) // Red color
                holder.binding.subtraO14.setBackgroundTintList(tint)
             }
             else{
                 holder.binding.llContent.visibility = View.VISIBLE
                 holder.binding.subtraO14.visibility = View.VISIBLE
                 val tint = ColorStateList.valueOf(android.graphics.Color.parseColor("#008000")) // Red color
                 holder.binding.subtraO14.setBackgroundTintList(tint)
             }


        }

        // Ensure all rows sync scrolling
        holder.binding.horizontalScrollView.scrollTo(scrollXPosition, 0)

        holder.binding.horizontalScrollView.viewTreeObserver.addOnScrollChangedListener {
            scrollXPosition =  holder.binding.horizontalScrollView.scrollX
            notifyDataSetChanged()
        }


    }

}

