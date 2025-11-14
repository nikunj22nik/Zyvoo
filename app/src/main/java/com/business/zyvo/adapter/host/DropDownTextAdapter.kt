package com.business.zyvo.adapter.host

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.OnClickListener
import com.business.zyvo.R
import com.business.zyvo.model.host.ItemDropDown
import com.business.zyvo.model.host.ItemRadio

class DropDownTextAdapter(
    private val items: List<ItemDropDown>,
    private val listener: OnClickListener,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<DropDownTextAdapter.ViewHolder>() {


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.itemText)

        fun bind(item: ItemDropDown, position: Int) {
            textView.text = item.text
            itemView.setOnClickListener {
                notifyItemChanged(position)
                listener.itemClick(position)
                onItemClick(item.text)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_popup, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size
}
