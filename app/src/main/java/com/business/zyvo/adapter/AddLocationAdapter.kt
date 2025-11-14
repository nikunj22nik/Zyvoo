package com.business.zyvo.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.AppConstant
import com.business.zyvo.OnClickListener1
import com.business.zyvo.databinding.LayoutAddTextBinding
import com.business.zyvo.databinding.LayoutWhereILiveBinding
import com.business.zyvo.model.AddLocationModel
import com.business.zyvo.onItemClickData


class AddLocationAdapter(
    var context: Context,
    var list: MutableList<AddLocationModel>,
    var listener: OnClickListener1,
    var listner2: onItemClickData
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val uploadLocationCode = 1 // Represents normal location entries
    private val uploadLocationFixed = 0 // Represents the "Add New" button
    private var count: Int = 0
    private var textAddNew: TextView? = null


    // ViewHolder for Location Items
    inner class LocationViewHolder(var binding: LayoutWhereILiveBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(location: AddLocationModel) {
            binding.textLocationName.text = location.name // Bind location name to TextView
            binding.imageCross.setOnClickListener {
                listener.itemClick(
                    adapterPosition,
                    AppConstant.LOCATION
                ) // Handle delete (or any other action) for this location
            }
        }
    }

    // ViewHolder for "Add New" Button
    inner class LocationViewHolderFixed(var binding: LayoutAddTextBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {

            binding.root.setOnClickListener {
                listner2.itemClick(position, AppConstant.LOCATION, "")// Handle "Add New" button click
            }
        }

        fun bind() {

            if (list.size >= 3) {

                binding.textAddNew.visibility = View.GONE
            } else {
                binding.textAddNew.visibility = View.VISIBLE
            }

        }
    }

    // Create the appropriate ViewHolder based on the view type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == uploadLocationCode) {
            val binding =
                LayoutWhereILiveBinding.inflate(LayoutInflater.from(context), parent, false)
            LocationViewHolder(binding)
        } else {
            val binding = LayoutAddTextBinding.inflate(LayoutInflater.from(context), parent, false)
            LocationViewHolderFixed(binding)
        }
    }

    // Bind the data to the ViewHolder based on its type
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is LocationViewHolder) {
            val locationItem = list[position]
            holder.bind(locationItem) // Bind location data to the holder

        } else if (holder is LocationViewHolderFixed) {
            holder.bind() // Bind "Add New" button (if needed)


        }
    }


    override fun getItemCount(): Int {
        return list.size
    }

    // Determine the view type (whether it's a location or the "Add New" button)
    override fun getItemViewType(position: Int): Int {


        return if (list.isEmpty() || position < list.size - 1) {
            uploadLocationCode // Normal location entries
        } else {
            uploadLocationFixed // "Add New" button
        }
    }

    // Update the list with new locations
    fun updateLocations(newList: MutableList<AddLocationModel>) {
        this.list = newList

        // Ensure "Add New" button is always present if the list is empty
        if (list.isEmpty()) {
            list.add(AddLocationModel(AppConstant.ADD_NEW)) // Placeholder for "Add New"
        }
        notifyDataSetChanged()
    }

}
