package com.business.zyvo.adapter.host

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.OnClickListener1
import com.business.zyvo.databinding.LayoutAddOnHostBinding
import com.business.zyvo.databinding.LayoutAddOnTextHostBinding
import com.business.zyvo.model.host.AddOnModel


class AddOnAdapter(var context: Context, var list : MutableList<AddOnModel>, var listner : OnClickListener1): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val uploadOnCode = 1 // Represents normal location entries
    private val uploadOnFixed = 0
    private  var textAddNew : TextView? = null


    inner class AddOnViewHolder(var binding: LayoutAddOnHostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
//            binding.root.setOnClickListener{
//                listner.itemClick(adapterPosition)
//            }
        }

        fun bind(item: AddOnModel) {

            if (list.size >= 3) {
                textAddNew?.visibility = View.GONE
            } else {
                textAddNew?.visibility  = View.VISIBLE
            }

            binding.textName.text = item.name
            binding.textRupees.text = "$" +item.price
            binding.imageCross.setOnClickListener {
                listner.itemClick(
                    adapterPosition,
                    "add On Cross"
                ) // Handle delete (or any other action) for this location
            }
        }

    }



    inner class AddOnViewHolderFixed(var binding: LayoutAddOnTextHostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                listner.itemClick(adapterPosition,"add On") // Handle "Add New" button click
            }
        }
        fun bind() {
//            binding.root.setOnClickListener {
//                listner.itemClick(adapterPosition, "work") // Handle "Add New" button click
//            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == uploadOnCode) {
            val binding = LayoutAddOnHostBinding.inflate(LayoutInflater.from(context), parent, false)
            AddOnViewHolder(binding)
        } else {
            val binding = LayoutAddOnTextHostBinding.inflate(LayoutInflater.from(context), parent, false)
            AddOnViewHolderFixed(binding)
        }
    }

    override fun getItemCount() = list.size
    override fun getItemViewType(position: Int): Int {
//        return super.getItemViewType(position)
        return if (list.size - 1 != position) {
            uploadOnCode
        } else {
            uploadOnFixed
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AddOnViewHolder) {
            val item = list[position]
            holder.bind(item)
        } else if (holder is AddOnViewHolderFixed) {
            holder.bind()
            textAddNew = holder.binding.textAddNew
        }
    }

    fun updateAddOn(newList: MutableList<AddOnModel>) {

        this.list = newList
        notifyDataSetChanged()
    }
    // Update data in the adapter

}