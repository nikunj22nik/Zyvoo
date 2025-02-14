package com.business.zyvo.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.OnClickListener1
import com.business.zyvo.databinding.LayoutAddTextBinding
import com.business.zyvo.databinding.LayoutWhereILiveBinding
import com.business.zyvo.model.AddLocationModel
import com.business.zyvo.onItemClickData

class AddLocationAdapter(
    var context: Context,
    var list: MutableList<AddLocationModel>,
    var listener: OnClickListener1,
    var listner2 : onItemClickData
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val uploadLocationCode = 1 // Represents normal location entries
    private val uploadLocationFixed = 0 // Represents the "Add New" button
    private var count : Int = 0
    private  var textAddNew : TextView? = null


    // ViewHolder for Location Items
    inner class LocationViewHolder(var binding: LayoutWhereILiveBinding) : RecyclerView.ViewHolder(binding.root) {


        fun bind(location: AddLocationModel) {

            if (list.size >= 3) {
                textAddNew?.visibility = View.GONE
            } else {
                textAddNew?.visibility  = View.VISIBLE
            }

            binding.textLocationName.text = location.name // Bind location name to TextView


            binding.imageCross.setOnClickListener {

                listener.itemClick(adapterPosition, "location") // Handle delete (or any other action) for this location
            }
        }
    }

    // ViewHolder for "Add New" Button
    inner class LocationViewHolderFixed(var binding: LayoutAddTextBinding) : RecyclerView.ViewHolder(binding.root) {
        init {

            binding.root.setOnClickListener {
               listner2.itemClick(position,"location","")// Handle "Add New" button click
            }
        }

        fun bind() {

            // Logic for hiding/showing "Add New" button based on count


            // No specific data binding is needed for the "Add New" button (unless you want dynamic text)
        }
    }

    // Create the appropriate ViewHolder based on the view type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == uploadLocationCode) {
            val binding = LayoutWhereILiveBinding.inflate(LayoutInflater.from(context), parent, false)
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



           // Toast.makeText(context,list.size.toString(),Toast.LENGTH_LONG).show()
        } else if (holder is LocationViewHolderFixed) {
            holder.bind() // Bind "Add New" button (if needed)
            textAddNew = holder.binding.textAddNew


        }
    }


    override fun getItemCount(): Int {
        // If the list size is exactly 3, don't show any items (or return 0)
//        return if (list.size >= 3) {
//            count = 3
//            list.size // Adjust this according to your logic; you might want to return the actual size or an indicator for hiding.
//        } else {
//            list.size // Return the current size of the list
//        }
return  list.size
      //  return if (list.size < 3) list.size + 1 else list.size

        // Toast.makeText(context, list.size.toString(), Toast.LENGTH_SHORT).show()
    }



    // Determine the view type (whether it's a location or the "Add New" button)
    override fun getItemViewType(position: Int): Int {
        return if (list.size - 1 != position) {
            uploadLocationCode // Normal location entries
        } else {
            uploadLocationFixed // "Add New" button
        }
    }

    // Update the list with new locations
    fun updateLocations(newList: MutableList<AddLocationModel>) {
        this.list = newList
        Log.d("TESTING_ZYVOO","list size : - "+list.size )
        notifyDataSetChanged()
    }
}
