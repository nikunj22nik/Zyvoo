package com.business.zyvo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.OnClickListener
import com.business.zyvo.R
import com.business.zyvo.databinding.LayoutAllBookingsBinding
import com.business.zyvo.model.MyBookingsModel

class MyBookingsAdapter(
    var context: Context,
    var list: MutableList<MyBookingsModel>,
    var listner: OnClickListener
) : RecyclerView.Adapter<MyBookingsAdapter.MyBookingsViewHolder>()
{

    lateinit var textStatus : TextView
    lateinit var text : String

    inner class MyBookingsViewHolder(var binding: LayoutAllBookingsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentItem: MyBookingsModel) {
            textStatus = binding.textStatus
            binding.imagePicture.setImageResource(currentItem.image)


            when (list.get(position).textStatus) {
                "Confirmed" -> binding.textStatus.setBackgroundResource(R.drawable.blue_button_bg)
                "Waiting payment" -> binding.textStatus.setBackgroundResource(R.drawable.yellow_button_bg)
                "Canceled" -> binding.textStatus.setBackgroundResource(R.drawable.grey_button_bg)
                else -> binding.textStatus.setBackgroundResource(R.drawable.button_bg) // Optional fallback
            }
            binding.root.setOnClickListener{
                listner.itemClick(position)
            }

            binding.textName.setText(currentItem.textName)
            binding.textDate.setText(currentItem.textDate)
            binding.textStatus.setText(currentItem.textStatus)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBookingsViewHolder {
        val binding =
            LayoutAllBookingsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyBookingsViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: MyBookingsViewHolder, position: Int) {
        val currentItem = list[position]

        holder.bind(currentItem)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItem(newList : MutableList<MyBookingsModel>){
        this.list = newList
        notifyDataSetChanged()
    }


}