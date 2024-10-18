package com.yesitlab.zyvo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yesitlab.zyvo.OnClickListener1
import com.yesitlab.zyvo.databinding.LayoutAddTextBinding
import com.yesitlab.zyvo.databinding.LayoutMyWorkBinding
import com.yesitlab.zyvo.model.AddLocationModel
import com.yesitlab.zyvo.model.AddWorkModel

class AddWorkAdapter(
    var context: Context,
    var list: MutableList<AddWorkModel>,
    var listner: OnClickListener1
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val uploadWorkCode = 1
    private val uploadWorkFixed = 0
     private  var textAddNew : TextView? = null


    inner class WorkViewHolder(var binding: LayoutMyWorkBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
//            binding.root.setOnClickListener{
//                listner.itemClick(adapterPosition)
//            }
        }

        fun bind(workItem: AddWorkModel) {
            if (list.size >= 3) {
                textAddNew?.visibility = View.GONE
            } else {
                textAddNew?.visibility  = View.VISIBLE
            }

            binding.textMyWorkName.text = workItem.name
            binding.imageCross.setOnClickListener {
                listner.itemClick(
                    adapterPosition,
                    "work"
                ) // Handle delete (or any other action) for this location
            }
        }

    }

//    inner class WorkViewHolderFixed(var binding: LayoutAddTextBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//        init {
//            binding.root.setOnClickListener {
//                listner.itemClick(adapterPosition,"work") // Handle "Add New" button click
//            }
//        }
//        fun bind() {
////            binding.root.setOnClickListener {
////                listner.itemClick(adapterPosition, "work") // Handle "Add New" button click
////            }
//        }
//
//    }

    inner class WorkViewHolderFixed(var binding: LayoutAddTextBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
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
                    list.add(0, AddWorkModel(enteredText))
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
        return if (viewType == uploadWorkCode) {
            val binding = LayoutMyWorkBinding.inflate(LayoutInflater.from(context), parent, false)
            WorkViewHolder(binding)
        } else {
            val binding = LayoutAddTextBinding.inflate(LayoutInflater.from(context), parent, false)
            WorkViewHolderFixed(binding)
        }
    }

    override fun getItemCount() = list.size
    override fun getItemViewType(position: Int): Int {
//        return super.getItemViewType(position)
        return if (list.size - 1 != position) {
            uploadWorkCode
        } else {
            uploadWorkFixed
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is WorkViewHolder) {
            val workItem = list[position]
            holder.bind(workItem)
        } else if (holder is WorkViewHolderFixed) {
            holder.bind()

            textAddNew = holder.binding.textAddNew
        }
    }

    fun updateWork(newList: MutableList<AddWorkModel>) {

        this.list = newList
        notifyDataSetChanged()
    }


}