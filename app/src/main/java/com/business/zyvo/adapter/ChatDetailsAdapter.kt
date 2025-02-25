package com.business.zyvo.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.chat.QuickstartConversationsManager
import com.business.zyvo.databinding.LayoutMessageChatingBinding
import com.business.zyvo.model.ChatMessageModel


class ChatDetailsAdapter(var context: Context, var quickstartConversationsManager: QuickstartConversationsManager, var user_id: String, var profile_image:String, var friend_profile_image:String, var typelogin:String): RecyclerView.Adapter<ChatDetailsAdapter.ChatViewHolder>() {

    inner  class  ChatViewHolder(var binding: LayoutMessageChatingBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(currentItem: ChatMessageModel) {
            binding.imageProfilePicture.setImageResource(currentItem.profilePicture)
            binding.textUserName.setText(currentItem.userName)
            binding.textMessage.setText(currentItem.messageText)
            binding.textDate.setText(currentItem.messageDate)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {

        val binding = LayoutMessageChatingBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ChatViewHolder(binding)
    }

    override fun getItemCount() =  quickstartConversationsManager.messages.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
//       val currentItem = list[position]

      //  holder.bind(currentItem)

        val message = quickstartConversationsManager.messages[position]
        Log.d("TESTING_CHAT",message.messageBody.toString())

    }

    fun updateItem(newList : MutableList<ChatMessageModel>){
//        this.list = newList
//        notifyDataSetChanged()
    }

}