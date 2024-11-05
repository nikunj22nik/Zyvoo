package com.yesitlab.zyvo.adapter

import android.content.Context
import android.view.LayoutInflater

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yesitlab.zyvo.OnClickListener
import com.yesitlab.zyvo.databinding.LayoutNotificationsBinding
import com.yesitlab.zyvo.model.NotificationScreenModel
import com.yesitlab.zyvo.setResizableText

class AdapterNotificationScreen(var context: Context,var list: ArrayList<NotificationScreenModel>, private val listener: OnClickListener) : RecyclerView.Adapter<AdapterNotificationScreen.NotificationViewHolder>() {

    inner class NotificationViewHolder(var binding: LayoutNotificationsBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: NotificationScreenModel) {
            // Bind the data from the NotificationScreenModel to the UI components
            binding.textNotificationsTitle.text = notification.text1
            binding.textDescription.setResizableText(notification.text2, 4, true)

//            // Set an OnClickListener if needed
//            binding.root.setOnClickListener {
//                // Trigger the click listener from the adapter
//                listner.onClick(binding.root)
//            }

            binding.imageCross.setOnClickListener {
                // Remove the item from the list and notify the adapter
                val position = adapterPosition // Get the current item position
                if (position != RecyclerView.NO_POSITION) { // Check if the position is valid
                    listener.itemClick(position) // Notify the listener to remove the item
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
       val binding = LayoutNotificationsBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return NotificationViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
       val currentItem = list[position]


        holder.bind(currentItem)

    }
    fun updateItem(list: ArrayList<NotificationScreenModel>){
        this.list = list
        notifyDataSetChanged()
    }

}