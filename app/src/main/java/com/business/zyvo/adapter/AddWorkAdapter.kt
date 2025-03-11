package com.business.zyvo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.OnClickListener1
import com.business.zyvo.databinding.LayoutAddTextBinding
import com.business.zyvo.databinding.LayoutMyWorkBinding
import com.business.zyvo.model.AddWorkModel
import com.business.zyvo.onItemClickData

class AddWorkAdapter(
    var context: Context,
    var list: MutableList<AddWorkModel>,
    var listner: OnClickListener1,
    var listner2: onItemClickData
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

        @SuppressLint("NotifyDataSetChanged")
        fun bind() {


            if (list.size >= 3) {
                binding.textAddNew.visibility = View.GONE
            } else {
                binding.textAddNew.visibility = View.VISIBLE
            }
            // Handle click on "Add New"
            binding.textAddNew.setOnClickListener {
                binding.textAddNew.visibility = View.GONE
                binding.rlEnterData.visibility = View.VISIBLE
            }

            // Handle click on the check button after entering data
            binding.imageCheckedButton.setOnClickListener {
                val enteredText = binding.etType.text.toString()
                if (enteredText.isNotEmpty()) {
                    listner2.itemClick(position,"work",enteredText)
                    // Add the new work item to the list
                    list.add(list.size -1 , AddWorkModel(enteredText))
                    notifyDataSetChanged() // Notify adapter to update RecyclerView

                    // Reset the UI: Hide the input field and show "Add New" button again
                    binding.rlEnterData.visibility = View.GONE
                    binding.textAddNew.visibility = View.VISIBLE

                    // Clear EditText field for the next input
                    binding.etType.text.clear()
                }else{
                    Toast.makeText(context,"Please Enter Work",Toast.LENGTH_LONG).show()
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


        if (list.isEmpty()) {
            list.add(AddWorkModel("Add New")) // Placeholder for "Add New"
        }
        notifyDataSetChanged()
    }


}