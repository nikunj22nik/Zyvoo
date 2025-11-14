package com.business.zyvo.adapter

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.business.zyvo.AppConstant
import com.business.zyvo.BuildConfig
import com.business.zyvo.R
import com.business.zyvo.chat.QuickstartConversationsManager
import com.business.zyvo.chat.QuickstartConversationsManagerOneTowOne
//import com.business.zyvo.activity.QuickstartConversationsManager

import com.business.zyvo.databinding.LayoutMessageChatingBinding
import com.business.zyvo.model.ChatMessageModel
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.PrepareData


class ChatDetailsAdapter(var context: Context, var quickstartConversationsManager: QuickstartConversationsManagerOneTowOne, var user_id: String, var profile_image:String, var friend_profile_image:String, var friend_name:String, var userName :String =""): RecyclerView.Adapter<ChatDetailsAdapter.ChatViewHolder>() {

    var userId :String =""
    init {
        var sessionManager = SessionManager(context)
        userId = sessionManager.getUserId().toString()
    }


    inner  class  ChatViewHolder(var binding: LayoutMessageChatingBinding): RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {

        val binding = LayoutMessageChatingBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ChatViewHolder(binding)
    }

    override fun getItemCount() =  quickstartConversationsManager.messages.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {

        val message = quickstartConversationsManager.messages[position]

        quickstartConversationsManager?.readConversastion()

        Log.d(ErrorDialog.TAG,message.messageBody.toString())

        Log.d(ErrorDialog.TAG,message.author)
        Log.d(ErrorDialog.TAG,userId.toString())

        if(message.author.equals(userId,true)){
            if (message.conversationSid==quickstartConversationsManager?.conversation?.sid) {
                Glide.with(context).load(BuildConfig.MEDIA_URL + profile_image)
                    .into(holder.binding.imageProfilePicture)
                holder.binding.textUserName.text = userName
            }
         } else{
            if (message.conversationSid==quickstartConversationsManager?.conversation?.sid) {
                Glide.with(context).load(BuildConfig.MEDIA_URL + friend_profile_image)
                    .into(holder.binding.imageProfilePicture)
                holder.binding.textUserName.text = friend_name
            }
        }

        val messagetype = String.format(message.type.toString())
        if (message.conversationSid==quickstartConversationsManager?.conversation?.sid) {
            holder.binding.textMessage.text = message.messageBody
            holder.binding.textDate.text = PrepareData.getMyPrettyDate(message.dateCreated)
        }
        if (messagetype.equals("TEXT",true)){
            holder.binding.textDate.visibility=View.VISIBLE
            holder.binding.textMessage.visibility=View.VISIBLE
            holder.binding.imglayout.visibility=View.GONE
        }
        else{
            holder.binding.textMessage.visibility=View.GONE
            holder.binding.imglayout.visibility=View.VISIBLE
            if (!message.mediaFileName.isNullOrEmpty() && message.mediaFileName!!.contains(".pdf", ignoreCase = true)){
                Glide.with(context)
                    .load(R.drawable.pdf_icon)
                    .addListener(object : RequestListener<Drawable?> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable?>,
                            isFirstResource: Boolean
                        ): Boolean {
                            holder.binding.progess.visibility=View.GONE
                            return  false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable?>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            holder.binding.progess.visibility=View.GONE
                            return  false
                        }
                    })
                    .placeholder(R.drawable.ic_img_not_found)
                    .error(R.drawable.ic_img_not_found)
                    .into(holder.binding.img)
            }else{
                message.getMediaContentTemporaryUrl { mediaContentUrl ->
                    Log.d("TAG", mediaContentUrl!!)
                    Glide.with(holder.itemView.context)
                        .load(mediaContentUrl)
                        .addListener(object : RequestListener<Drawable?> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable?>,
                                isFirstResource: Boolean
                            ): Boolean {
                                holder.binding.progess.visibility=View.GONE
                                return  false
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                model: Any,
                                target: Target<Drawable?>?,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                holder.binding.progess.visibility=View.GONE
                                return  false
                            }
                        })
                        .placeholder(R.drawable.ic_img_not_found)
                        .error(R.drawable.ic_img_not_found)
                        .into(holder.binding.img)

                }
            }

        }

    }



}