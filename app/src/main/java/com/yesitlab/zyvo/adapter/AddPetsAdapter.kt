package com.yesitlab.zyvo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yesitlab.zyvo.OnClickListener1
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.databinding.LayoutAddHobbiesBinding
import com.yesitlab.zyvo.databinding.LayoutAddPetsBinding
import com.yesitlab.zyvo.databinding.LayoutAddTextBinding

import com.yesitlab.zyvo.model.AddPetsModel

class AddPetsAdapter(var context: Context, var list : MutableList<AddPetsModel>, var listner : OnClickListener1): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val uploadPetsCode = 1 // Represents normal location entries
    private val uploadPetsFixed = 0
    private  var textAddNew : TextView? = null



    inner class PetsViewHolder(var binding: LayoutAddPetsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
//            binding.root.setOnClickListener{
//                listner.itemClick(adapterPosition)
//            }
        }

        fun bind(petsItem: AddPetsModel) {
            if (list.size >= 3) {
                textAddNew?.visibility = View.GONE
            } else {
                textAddNew?.visibility  = View.VISIBLE
            }
            binding.textMyWorkName.text = petsItem.name
            binding.imageCross.setOnClickListener {
                listner.itemClick(
                    adapterPosition,
                    "Pets"
                ) // Handle delete (or any other action) for this location
            }
        }

    }



    inner class PetsViewHolderFixed(var binding: LayoutAddTextBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {

            binding.imageIcon.setImageResource(R.drawable.ic_add_pets_icon)
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
                    list.add(0, AddPetsModel(enteredText))
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
        return if (viewType == uploadPetsCode) {
            val binding = LayoutAddPetsBinding.inflate(LayoutInflater.from(context), parent, false)
            PetsViewHolder(binding)
        } else {
            val binding = LayoutAddTextBinding.inflate(LayoutInflater.from(context), parent, false)
            PetsViewHolderFixed(binding)
        }
    }

    override fun getItemCount() = list.size
    override fun getItemViewType(position: Int): Int {
//        return super.getItemViewType(position)
        return if (list.size - 1 != position) {
            uploadPetsCode
        } else {
            uploadPetsFixed
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PetsViewHolder) {
            val hobbiesItem = list[position]
            holder.bind(hobbiesItem)
        } else if (holder is PetsViewHolderFixed) {
            holder.bind()

            textAddNew = holder.binding.textAddNew
        }
    }

    fun updatePets(newList: MutableList<AddPetsModel>) {

        this.list = newList
        notifyDataSetChanged()
    }
}