package com.business.zyvo.adapter.host

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.OnClickListener
import com.business.zyvo.R
import com.business.zyvo.model.host.ItemRadio

class RadioTextAdapter(
    private val items: List<ItemRadio>,
    private val listener: OnClickListener,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<RadioTextAdapter.ViewHolder>() {

    private var lastSelectedPosition = items.indexOfFirst { it.isSelected }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.itemText)
        val radioButton: RadioButton = view.findViewById(R.id.itemRadioButton)

        fun bind(item: ItemRadio, position: Int) {
            textView.text = item.text
            radioButton.isChecked = item.isSelected
            var radioButton = itemView.findViewById<RadioButton>(R.id.itemRadioButton)

            radioButton.setOnClickListener {
                if (lastSelectedPosition != -1 && lastSelectedPosition != position) {
                    items[lastSelectedPosition].isSelected = false
                    notifyItemChanged(lastSelectedPosition)
                }

                // Set the current item as selected
                item.isSelected = true
                notifyItemChanged(position)

                lastSelectedPosition = position
                listener.itemClick(position)
                onItemClick(item.text)
            }

            itemView.setOnClickListener {
                // Unselect the previously selected item
                if (lastSelectedPosition != -1 && lastSelectedPosition != position) {
                    items[lastSelectedPosition].isSelected = false
                    notifyItemChanged(lastSelectedPosition)
                }

                // Set the current item as selected
                item.isSelected = true
                notifyItemChanged(position)

                lastSelectedPosition = position
                listener.itemClick(position)
                onItemClick(item.text)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_text_with_radio, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size
}
