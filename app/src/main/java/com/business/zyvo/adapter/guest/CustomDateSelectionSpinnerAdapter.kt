package com.business.zyvo.adapter.guest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener
import com.skydoves.powerspinner.PowerSpinnerInterface
import com.skydoves.powerspinner.PowerSpinnerView
import com.business.zyvo.R

class CustomDateSelectionSpinnerAdapter(
    private val context: Context, private var items: List<String>, override var index: Int, override var onSpinnerItemSelectedListener: OnSpinnerItemSelectedListener<String>?,
    override val spinnerView: PowerSpinnerView)
    : RecyclerView.Adapter<CustomDateSelectionSpinnerAdapter.ViewHolder>(), PowerSpinnerInterface<String> {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.spinnerItemText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.spinner_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textView.text = item
        holder.textView.isSelected = position == index
        holder.itemView.setOnClickListener {
            notifyItemSelected(position)
        }

    }

    override fun getItemCount(): Int = items.size

    // Retrieve the item at a given position
//    override fun getSpinnerItem(position: Int): String = items[position]


    // Update the selected item index and notify the spinner
    override fun notifyItemSelected(index: Int) {
//        this.index = index
//        spinnerView.dismiss() // Close the dropdown after selection
//        onSpinnerItemSelectedListener?.onItemSelected(index, items[index] )
//        notifyDataSetChanged()

        val oldIndex = this.index
        val oldItem = if (oldIndex != -1) items[oldIndex] else null
        val newItem = items[index]

        this.index = index
        spinnerView.dismiss()
        onSpinnerItemSelectedListener?.onItemSelected(oldIndex, oldItem, index, newItem)
        notifyDataSetChanged() // Refresh the adapter to update selection visuals
    }

    // Set new items to the adapter
    override fun setItems(itemList: List<String>) {
        items = itemList
        notifyDataSetChanged()
    }


}


