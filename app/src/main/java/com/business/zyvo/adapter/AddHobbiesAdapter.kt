package com.business.zyvo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.OnClickListener1
import com.business.zyvo.R
import com.business.zyvo.databinding.LayoutAddHobbiesBinding
import com.business.zyvo.databinding.LayoutAddTextBinding
import com.business.zyvo.model.AddHobbiesModel


class AddHobbiesAdapter(var context: Context, var list : MutableList<AddHobbiesModel>,var listner : OnClickListener1): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val uploadHobbiesCode = 1 // Represents normal location entries
    private val uploadHobbiesFixed = 0
    private  var textAddNew : TextView? = null


    inner class HobbiesViewHolder(var binding: LayoutAddHobbiesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
//            binding.root.setOnClickListener{
//                listner.itemClick(adapterPosition)
//            }
        }

        fun bind(hobbiesItem: AddHobbiesModel) {

            if (list.size >= 3) {
                textAddNew?.visibility = View.GONE
            } else {
                textAddNew?.visibility  = View.VISIBLE
            }

            binding.textMyWorkName.text = hobbiesItem.name
            binding.imageCross.setOnClickListener {
                listner.itemClick(
                    adapterPosition,
                    "Hobbies"
                ) // Handle delete (or any other action) for this location
            }
        }

    }



    inner class HobbiesViewHolderFixed(var binding: LayoutAddTextBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {

            binding.imageIcon.setImageResource(R.drawable.ic_add_hobbies_icon)
            // Handle click on "Add New"
            binding.textAddNew.setOnClickListener {
                // Show the input layout and hide the "Add New" button
                binding.textAddNew.visibility = View.GONE
                binding.rlEnterData.visibility = View.VISIBLE
            }

            // Handle click on the check button after entering data
            binding.imageCheckedButton.setOnClickListener {
                val enteredText = binding.etType.text.toString()
                if (enteredText.isNotEmpty()) {
                    // Add the new work item to the list
                    list.add(0, AddHobbiesModel(enteredText))
                    notifyDataSetChanged() // Notify adapter to update RecyclerView

                    // Reset the UI: Hide the input field and show "Add New" button again
                    binding.rlEnterData.visibility = View.GONE
                    binding.textAddNew.visibility = View.VISIBLE

                    // Clear EditText field for the next input
                    binding.etType.text.clear()
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == uploadHobbiesCode) {
            val binding = LayoutAddHobbiesBinding.inflate(LayoutInflater.from(context), parent, false)
            HobbiesViewHolder(binding)
        } else {
            val binding = LayoutAddTextBinding.inflate(LayoutInflater.from(context), parent, false)
            HobbiesViewHolderFixed(binding)
        }
    }

    override fun getItemCount() = list.size
    override fun getItemViewType(position: Int): Int {
//        return super.getItemViewType(position)
        return if (list.size - 1 != position) {
            uploadHobbiesCode
        } else {
            uploadHobbiesFixed
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HobbiesViewHolder) {
            val hobbiesItem = list[position]
            holder.bind(hobbiesItem)
        } else if (holder is HobbiesViewHolderFixed) {
            holder.bind()
            textAddNew = holder.binding.textAddNew
        }
    }

    fun updateHobbies(newList: MutableList<AddHobbiesModel>) {

        this.list = newList
        notifyDataSetChanged()
    }
}