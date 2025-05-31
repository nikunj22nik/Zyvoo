package com.business.zyvo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.NotificationListener
import com.business.zyvo.databinding.LayoutNotificationsBinding
import com.business.zyvo.fragment.both.notificationfragment.NotificationRootModel
import com.business.zyvo.setResizableText

class GuestNotificationAdapter(var context: Context, var list: MutableList<NotificationRootModel>, private val listener: NotificationListener) : RecyclerView.Adapter<GuestNotificationAdapter.NotificationViewHolder>() {

    private var itemClickListener: OnItemClickListener? = null
    private var clickedNotificationId = 0

    interface OnItemClickListener {
        fun onItemClick(obj: NotificationRootModel, notificationId: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.itemClickListener = listener
    }

    inner class NotificationViewHolder(private val binding: LayoutNotificationsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: NotificationRootModel) {
            binding.textNotificationsTitle.text = notification.title
            binding.textDescription.setResizableText(notification.message, 4, true)

            binding.imageCross.setOnClickListener {
                val position = bindingAdapterPosition // Use bindingAdapterPosition for safety
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener?.onItemClick(list[position],list[position].notification_id)
                    list.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, list.size)
                }
            }

            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    clickedNotificationId = list[position].notification_id
                    listener.onClick(clickedNotificationId)
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = LayoutNotificationsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(list[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItem(newList: MutableList<NotificationRootModel>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
