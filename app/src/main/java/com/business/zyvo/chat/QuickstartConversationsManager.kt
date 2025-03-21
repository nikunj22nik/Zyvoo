package com.business.zyvo.chat

import android.content.Context
import android.util.Log
import android.widget.TextView
import com.google.gson.Gson
import com.twilio.conversations.Attributes
import com.twilio.conversations.CallbackListener
import com.twilio.conversations.Conversation
import com.twilio.conversations.ConversationListener
import com.twilio.conversations.ConversationsClient
import com.twilio.conversations.ConversationsClientListener
import com.twilio.conversations.ErrorInfo
import com.twilio.conversations.Message
import com.twilio.conversations.Participant
import com.twilio.conversations.ProgressListener
import com.twilio.conversations.StatusListener
import com.twilio.conversations.User
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException


interface QuickstartConversationsManagerListener {
    fun receivedNewMessage()
    fun messageSentCallback()
    fun reloadMessages()
    fun reloadLastMessages()
    fun showError()
}

interface TokenResponseListener {
    fun receivedTokenResponse(success: Boolean, exception: Exception?)
}

interface AccessTokenListener {
    fun receivedAccessToken(token: String?, exception: Exception?)
}
class QuickstartConversationsManager {
    companion object {
        public const val TAG = "TwilioConversations"
    }

    private  var DEFAULT_CONVERSATION_NAME = "general"

    val messages: ArrayList<Message> = ArrayList()

    var conversationsClient: ConversationsClient? = null
    private var conversation: Conversation? = null
    private var conversationsManagerListener: QuickstartConversationsManagerListenerOneTowOne? = null
    private var tokenURL: String = ""

    private var identity: String? = null

    private var userid: String? = null
    private var tvOnline: TextView? = null

    private data class TokenResponse(val token: String)


    fun initializeWithAccessTokenBase(context: Context, token: String){
        val props = ConversationsClient.Properties.newBuilder().createProperties()
        ConversationsClient.create(context, token, props, mConversationsClientCallback)
    }


   fun loadChatList(){

       loadAllChannel(conversationsClient!!)

   }



    private fun loadAllChannel(conversationsClient: ConversationsClient){
        messages.clear()
        try {
            val conversations = conversationsClient.myConversations
            for (data in conversations) {
                val lastMessageIndex = data.lastMessageIndex
                if (lastMessageIndex != null && lastMessageIndex >= 0) {
                Log.d("*******", "list ${data.uniqueName}")
                data.getLastMessages(data.lastMessageIndex.toInt()) { result ->
                    try {
                        messages.addAll(result)
                        conversationsManagerListener?.reloadLastMessages()
                    } catch (e: Exception) {
                        conversationsManagerListener?.reloadLastMessages()
                        Log.e("*******", "Failed to process messages: ${e.message}")
                    }
                }
                    }
            }
        } catch (e: Exception) {
            conversationsManagerListener?.reloadLastMessages()
            Log.e("*******", "msg ${e.message}")
        }
    }


    fun loadConversationById(groupId: String,identity:String,userid:String) {
        try {
            this.identity = identity
            this.userid = userid
            this.DEFAULT_CONVERSATION_NAME = groupId
            if (conversationsClient==null){
                messages.clear()
                Log.d(TAG, "Loaded conversation: $DEFAULT_CONVERSATION_NAME")
            }else{
                val client = conversationsClient ?: return
                client.getConversation(DEFAULT_CONVERSATION_NAME, object : CallbackListener<Conversation> {
                    override fun onSuccess(conversation: Conversation) {
                        Log.d(TAG, "Loaded conversation: $DEFAULT_CONVERSATION_NAME")

                        handleConversation(conversation)
                    }
                    override fun onError(errorInfo: ErrorInfo) {
                        Log.e(TAG, "Error retrieving conversation: ${errorInfo.message}")
                        createConversation()
                    }
                })
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun handleConversation(conversation: Conversation) {
        messages.clear()
        if (conversation.status == Conversation.ConversationStatus.JOINED) {
            this.conversation = conversation
            conversation.addListener(mDefaultConversationListener)
            loadPreviousMessages(conversation)
        } else {
            conversation.join(object : StatusListener {
                override fun onSuccess() {
                    Log.d(TAG, "Joined conversation: ${conversation.uniqueName}")
                    this@QuickstartConversationsManager.conversation = conversation
                    conversation.addListener(mDefaultConversationListener)
                    loadPreviousMessages(conversation)
                }

                override fun onError(errorInfo: ErrorInfo) {
                    Log.e(TAG, "Error joining conversation: ${errorInfo.message}")
                }
            })
        }
    }




    private fun retrieveToken(listener: AccessTokenListenerOneTowOne) {
      /*  val client = OkHttpClient()
        val request = Request.Builder().url(tokenURL).build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                Log.d(TAG, "Response from server: $responseBody")
                val tokenResponse = Gson().fromJson(responseBody, TokenResponse::class.java)
                val accessToken = tokenResponse.token
                Log.d(TAG, "Retrieved access token from server: $accessToken")
                listener.receivedAccessToken(accessToken, null)
            }
        } catch (ex: IOException) {
            Log.e(TAG, ex.localizedMessage, ex)
            listener.receivedAccessToken(null, ex)
        }*/
    }

    fun sendMessage(messageBody: String) {
        if (conversation != null) {
            val options = Message.options().withBody(messageBody)
            options.withAttributes(conversation!!.attributes)
            conversation!!.sendMessage(options) {
                if (conversationsManagerListener != null) {
                    conversationsManagerListener!!.messageSentCallback()
                }
            }
        }
    }

    @Throws(FileNotFoundException::class)
    fun sendMessageImage(messageBody: String?, file: File) {
        if (conversation != null) {
            val options = Message.options().withMedia(FileInputStream(messageBody), "jpg/png")
                .withMediaFileName(file.name)
                .withMediaProgressListener(object : ProgressListener {
                    override fun onStarted() {
                    }

                    override fun onProgress(bytes: Long) {
                    }

                    override fun onCompleted(mediaSid: String) {
                    }
                })
            conversation!!.sendMessage(options) {
                if (conversationsManagerListener != null) {
                    conversationsManagerListener!!.messageSentCallback()
                }
            }
        }
    }

    @Throws(FileNotFoundException::class)
    fun sendMessagefile(messageBody: String?, file: File) {
        if (conversation != null) {
            val options =
                Message.options().withMedia(FileInputStream(messageBody), "application/pdf")
                    .withMediaFileName(file.name)
                    .withMediaProgressListener(object : ProgressListener {
                        override fun onStarted() {
                        }

                        override fun onProgress(bytes: Long) {
                        }

                        override fun onCompleted(mediaSid: String) {

                        }
                    })
            conversation!!.sendMessage(options) {
                if (conversationsManagerListener != null) {
                    conversationsManagerListener!!.messageSentCallback()
                }
            }
        }
    }

    private fun loadChannels() {
        val client = conversationsClient ?: return
        client.getConversation(DEFAULT_CONVERSATION_NAME, object : CallbackListener<Conversation> {
            override fun onSuccess(conversation: Conversation) {
                when (conversation.status) {
                    Conversation.ConversationStatus.JOINED/*, Conversation.ConversationStatus.NOT_PARTICIPATING*/ -> {
                        Log.d(TAG, "Already Exists in Conversation: $DEFAULT_CONVERSATION_NAME")
                        this@QuickstartConversationsManager.conversation = conversation
                        this@QuickstartConversationsManager.conversation!!.addListener(mDefaultConversationListener)
                        this@QuickstartConversationsManager.loadPreviousMessages(conversation)
                    }
                    else -> {
                        Log.d(TAG, "Joining Conversation: $DEFAULT_CONVERSATION_NAME")
                        joinConversation(conversation)
                    }
                }
            }

            override fun onError(errorInfo: ErrorInfo) {
                Log.e(TAG, "Error retrieving conversation: ${errorInfo.message}")
                createConversation()
            }
        })
    }
    private fun createConversation() {
        Log.d(TAG, "Creating Conversation: $DEFAULT_CONVERSATION_NAME")
        conversationsClient!!.conversationBuilder()
            .withUniqueName(DEFAULT_CONVERSATION_NAME).build( object : CallbackListener<Conversation> {
                override  fun onSuccess(conversation: Conversation) {
                    Log.d(TAG, "Joining Conversation: $DEFAULT_CONVERSATION_NAME")
                    val attributes: Attributes = conversation.attributes
                    conversation.addParticipantByIdentity(identity,attributes,
                       object :StatusListener {
                           override fun onSuccess() {
                               Log.d(TAG, "add")
                               joinConversation(conversation)
                           }

                           override fun onError(errorInfo: ErrorInfo?) {
                               super.onError(errorInfo)
                               joinConversation(conversation)
                               Log.e(TAG, "error  .." + errorInfo?.message)
                           }
                       })
                }

                override   fun onError(errorInfo: ErrorInfo) {
                    Log.e(TAG, "Error retrieving conversation: ${errorInfo.message}")
                    conversationsManagerListener?.showError()

                }
            })
    }

    private fun joinConversation(conversation: Conversation) {
        Log.d(TAG, "Joining Conversation: " + conversation.uniqueName)
        if (conversation.status == Conversation.ConversationStatus.JOINED) {
            this@QuickstartConversationsManager.conversation = conversation
            Log.d(TAG, "Already joined default conversation")
            this@QuickstartConversationsManager.conversation!!.addListener(
                mDefaultConversationListener
            )
            return
        }

        conversation.join(object : StatusListener {
            override fun onSuccess() {
                this@QuickstartConversationsManager.conversation = conversation
                Log.d(TAG, "Joined default conversation")
                this@QuickstartConversationsManager.conversation!!.addListener(mDefaultConversationListener)
                this@QuickstartConversationsManager.loadPreviousMessages(conversation)
            }

            override fun onError(errorInfo: ErrorInfo) {
                Log.e(TAG, "Error joining conversation: ${errorInfo.message}")
            }
        })
    }

    private fun loadPreviousMessages(conversation: Conversation) {
        if (conversation.lastMessageIndex != null) {
            conversation.getLastMessages(conversation.lastMessageIndex.toInt() + 1
            ) { result ->
                messages.clear()
                messages.addAll(result)
                Log.d("*******", "${messages.size}")
                conversation.setAllMessagesRead {
                    conversationsManagerListener?.reloadMessages()
                }
            }
        }else{
            conversationsManagerListener?.reloadMessages()
        }
    }

    private val mConversationsClientListener = object : ConversationsClientListener {
        override fun onConversationAdded(conversation: Conversation) {}

        override fun onConversationUpdated(conversation: Conversation, updateReason: Conversation.UpdateReason) {
            if (conversation.lastMessageIndex != null) {
                conversation.getMessageByIndex(
                    conversation.lastMessageIndex.toInt().toLong()
                ) { result ->
                    Log.d("*******", "onConversationUpdated")
                   // messages.add(result!!)
                    if (conversationsManagerListener != null) {
                        conversationsManagerListener!!.receivedNewMessage()
                    }
                }
            }
        }

        override fun onConversationDeleted(conversation: Conversation) {}

        override fun onConversationSynchronizationChange(conversation: Conversation) {}

        override fun onError(errorInfo: ErrorInfo) {}

        override fun onUserUpdated(user: User, updateReason: User.UpdateReason) {}

        override fun onUserSubscribed(user: User) {}

        override fun onUserUnsubscribed(user: User) {}

        override fun onClientSynchronization(synchronizationStatus: ConversationsClient.SynchronizationStatus) {
            if (synchronizationStatus == ConversationsClient.SynchronizationStatus.COMPLETED) {
            //    loadChannels()
                loadChatList()
            }
        }

        override fun onNewMessageNotification(s: String, s1: String, l: Long) {}

        override fun onAddedToConversationNotification(s: String) {}

        override fun onRemovedFromConversationNotification(s: String) {}

        override fun onNotificationSubscribed() {}

        override fun onNotificationFailed(errorInfo: ErrorInfo) {}

        override fun onConnectionStateChange(connectionState: ConversationsClient.ConnectionState) {}

        override fun onTokenExpired() {}

        override fun onTokenAboutToExpire() {
            /*retrieveToken(object : AccessTokenListenerOneTowOne {
                override fun receivedAccessToken(token: String?, exception: Exception?) {
                    if (token != null) {
                        conversationsClient?.updateToken(token, object : StatusListener {
                            override fun onSuccess() {
                                Log.d(TAG, "Refreshed access token.")
                            }
                        })
                    }
                }
            })*/
        }
    }

    private val mConversationsClientCallback = object : CallbackListener<ConversationsClient> {
        override fun onSuccess(conversationsClient: ConversationsClient) {
            this@QuickstartConversationsManager.conversationsClient = conversationsClient
            conversationsClient.addListener(mConversationsClientListener)
            Log.d(TAG, "Success creating Twilio Conversations Client")
        }

        override fun onError(errorInfo: ErrorInfo) {
            conversationsManagerListener?.showError()
            Log.e(TAG, "Error creating Twilio Conversations Client: ${errorInfo.message}")
        }
    }

    private val mDefaultConversationListener = object : ConversationListener {
        override fun onMessageAdded(message: Message) {
            Log.d(TAG, "Message added")
            messages.add(message)
            if (message.author == userid) {
                conversation!!.setAllMessagesRead { }
            }
            if (conversationsManagerListener != null) {
                conversationsManagerListener!!.receivedNewMessage()

            }
        }

        override fun onMessageUpdated(message: Message, updateReason: Message.UpdateReason) {
            conversation!!.setAllMessagesRead { }
        }

        override fun onMessageDeleted(message: Message) {
            Log.d(TAG, "Message deleted")
        }

        override fun onParticipantAdded(participant: Participant) {
            Log.d(TAG, "Participant added: ${participant.identity}")
//            tvOnline!!.text = "Online"
        }

        override fun onParticipantUpdated(participant: Participant, updateReason: Participant.UpdateReason) {
            Log.d(TAG, "Participant updated: ${participant.identity} $updateReason")
//            tvOnline!!.text = "Online"
        }

        override fun onParticipantDeleted(participant: Participant) {
            Log.d(TAG, "Participant deleted: ${participant.identity}")

        }

        override fun onTypingStarted(conversation: Conversation, participant: Participant) {
            Log.d(TAG, "Started Typing: ${participant.identity}")
//            tvOnline!!.text = "Typing ..."

        }

        override fun onTypingEnded(conversation: Conversation, participant: Participant) {
            Log.d(TAG, "Ended Typing: ${participant.identity}")
//            tvOnline!!.text = ""
        }

        override fun onSynchronizationChanged(conversation: Conversation) {}
    }


    fun setListener(listener: QuickstartConversationsManagerListenerOneTowOne) {
        this.conversationsManagerListener = listener
    }

    fun readConversastion() {
        if (conversation != null) {
            conversation!!.setAllMessagesRead { }
        }
    }



}