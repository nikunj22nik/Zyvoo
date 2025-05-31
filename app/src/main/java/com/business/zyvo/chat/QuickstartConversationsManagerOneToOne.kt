package com.business.zyvo.chat

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
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
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


interface QuickstartConversationsManagerListenerOneTowOne {
    fun receivedNewMessage()
    fun messageSentCallback()
    fun reloadMessages()
    fun reloadLastMessages()
    fun showError(value:String)
    fun onlineOffline(value:String)
    fun notInit()
}

interface TokenResponseListenerOneTowOne {
    fun receivedTokenResponse(success: Boolean, exception: Exception?)
}

interface AccessTokenListenerOneTowOne {
    fun receivedAccessToken(token: String?, exception: Exception?)
}
class QuickstartConversationsManagerOneTowOne {
    companion object {
        public const val TAG = "TwilioConversations"
    }

    private  var DEFAULT_CONVERSATION_NAME = "general"

    var messages: ArrayList<Message> = ArrayList()

    var conversationsClient: ConversationsClient? = null
     var conversation: Conversation? = null
    private var conversationsManagerListener: QuickstartConversationsManagerListenerOneTowOne? = null
    private var tokenURL: String = ""

     var identity: String? = null

    private var userid: String? = null
    var tvOnline: TextView? = null

    private data class TokenResponse(val token: String)


    fun initializeWithAccessTokenBase(context: Context, token: String){
        val props = ConversationsClient.Properties.newBuilder().createProperties()
        ConversationsClient.create(context, token, props, mConversationsClientCallback)
    }


    fun initializeWithAccessToken(
        context: Context?,
        token: String?,
        DEFAULT_CONVERSATION_NAME: String?,
        identity: String?,
        userid: String?,
    ) {
        this.DEFAULT_CONVERSATION_NAME = DEFAULT_CONVERSATION_NAME!!
        this.identity = identity
        this.userid = userid
        val props = ConversationsClient.Properties.newBuilder().createProperties()
        ConversationsClient.create(context!!, token!!, props, mConversationsClientCallback)
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
                Log.d(TAG, "list ${data.uniqueName}")
                data.getLastMessages(data.lastMessageIndex.toInt()) { result ->
                    try {
                        messages.addAll(result)
                        conversationsManagerListener?.reloadLastMessages()
                    } catch (e: Exception) {
                        conversationsManagerListener?.reloadLastMessages()
                        Log.e(TAG, "Failed to process messages: ${e.message}")
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
            messages.clear()
            if (conversationsClient==null){
                Log.d(TAG, "Loaded conversation: $DEFAULT_CONVERSATION_NAME")
                conversationsManagerListener?.notInit()
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

    @SuppressLint("SuspiciousIndentation")
     fun lastReadTime(conversation: Conversation,
                             identity:String){
            val participant: Participant = conversation.getParticipantByIdentity(identity)
            if (participant == null) {
                Log.e(TAG, "Participant with identity $identity not found in conversation.")
                return
            }
            participant?.let {
                val lastSeenTimestamp = it.getLastReadTimestamp()
                if (lastSeenTimestamp != null) {
                    val lastSeenDate =
                        getFormattedLastSeen(lastSeenTimestamp) // Convert to readable date
                    conversationsManagerListener?.onlineOffline(lastSeenDate)
                    Log.d(TAG, "Last seen at: $lastSeenDate")
                }
            }

    }

    fun getFormattedLastSeen(lastSeen: String?): String {
        if (lastSeen.isNullOrEmpty()) return "Last seen unavailable"

        val lastSeenInstant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant.parse(lastSeen)
        } else {
            TODO("VERSION.SDK_INT < O")
        } // Parse ISO date
        val now = Instant.now() // Get current time
        val lastSeenDateTime =
            lastSeenInstant.atZone(ZoneId.systemDefault()) // Convert to local timezone
        val nowDateTime = now.atZone(ZoneId.systemDefault())

        val duration = Duration.between(lastSeenInstant, now)
        val minutesAgo = duration.toMinutes()
        val hoursAgo = duration.toHours()

        return when {
            minutesAgo < 1 -> "Online" // If within a minute
            minutesAgo < 60 -> "$minutesAgo minutes ago" // If within an hour
            hoursAgo < 24 && lastSeenDateTime.dayOfMonth == nowDateTime.dayOfMonth -> "$hoursAgo hours ago" // Today
            hoursAgo < 48 && lastSeenDateTime.dayOfMonth == nowDateTime.minusDays(1).dayOfMonth -> "Yesterday" // Yesterday
            else -> lastSeenDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) // Older dates
        }
    }
    fun formatIsoDate(isoDate: String): String {
        val instant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant.parse(isoDate)
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault()) // Convert to local timezone
        return formatter.format(instant)
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
                    this@QuickstartConversationsManagerOneTowOne.conversation = conversation
                    conversation.addListener(mDefaultConversationListener)
                    loadPreviousMessages(conversation)
                }

                override fun onError(errorInfo: ErrorInfo) {
                    Log.e(TAG, "Error joining conversation: ${errorInfo.message}")
                    conversationsManagerListener?.showError(errorInfo.message)
                }
            })
        }
    }





    fun sendMessage(messageBody: String) {
        Log.d(TAG,"Null")
        if (conversation != null) {
            Log.d(TAG,"Not Null")
            val options = Message.options().withBody(messageBody)
            options.withAttributes(conversation!!.attributes)
            conversation!!.sendMessage(options) {
                if (conversationsManagerListener != null) {
                    if (conversation != null) {
                        conversation!!.setAllMessagesRead {
                            conversationsManagerListener!!.messageSentCallback()
                        }
                    }
                }
            }
        }
    }

    @Throws(FileNotFoundException::class)
    fun sendMessageImage(messageBody: String?, file: File) {
        if (conversation != null) {
            val options = Message.options().withMedia(FileInputStream(messageBody),
                "jpg/png")
                .withMediaFileName(file.name)
                .withMediaProgressListener(object : ProgressListener {
                    override fun onStarted() {
                    }

                    override fun onProgress(bytes: Long) {
                    }

                    override fun onCompleted(mediaSid: String) {
                    }
                })
            conversation?.sendMessage(options) {
                if (conversationsManagerListener != null) {
                    conversationsManagerListener?.messageSentCallback()
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
            conversation?.sendMessage(options) {
                if (conversationsManagerListener != null) {
                    conversationsManagerListener?.messageSentCallback()
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
                        this@QuickstartConversationsManagerOneTowOne.conversation = conversation
                        this@QuickstartConversationsManagerOneTowOne.conversation!!.addListener(mDefaultConversationListener)
                        this@QuickstartConversationsManagerOneTowOne.loadPreviousMessages(conversation)
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
                               Log.d(TAG, "identity: $identity")
                               super.onError(errorInfo)
                               joinConversation(conversation)
                               Log.e(TAG, "error  .." + errorInfo?.message)
                           }
                       })
                }

                override   fun onError(errorInfo: ErrorInfo) {
                    Log.e(TAG, "Error retrieving conversation: ${errorInfo.message}")
                    conversationsManagerListener?.showError(errorInfo.message)

                }
            })
    }

    private fun joinConversation(conversation: Conversation) {
        Log.d(TAG, "Joining Conversation: " + conversation.uniqueName)
        if (conversation.status == Conversation.ConversationStatus.JOINED) {
            this@QuickstartConversationsManagerOneTowOne.conversation = conversation
            Log.d(TAG, "Already joined default conversation")
            this@QuickstartConversationsManagerOneTowOne.conversation!!.addListener(
                mDefaultConversationListener
            )
            return
        }

        conversation.join(object : StatusListener {
            override fun onSuccess() {
                this@QuickstartConversationsManagerOneTowOne.conversation = conversation
                Log.d(TAG, "Joined default conversation")
                this@QuickstartConversationsManagerOneTowOne.conversation!!.addListener(mDefaultConversationListener)
                this@QuickstartConversationsManagerOneTowOne.loadPreviousMessages(conversation)
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
                Log.d(TAG, "${messages.size}")
                conversation.setAllMessagesRead {
                    conversationsManagerListener?.reloadMessages()
                }
            }
        }else{
            Log.d(TAG, "NO message")
            conversationsManagerListener?.reloadMessages()
        }

        conversationsClient?.let { client->
            identity?.let {
                Log.d(TAG,identity.toString())
                subscribeToUserStatus(client,it)
            }

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
                loadChannels()
                //loadChatList()
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
        }
    }

    private val mConversationsClientCallback = object : CallbackListener<ConversationsClient> {
        override fun onSuccess(conversationsClient: ConversationsClient) {
            this@QuickstartConversationsManagerOneTowOne.conversationsClient = conversationsClient
            conversationsClient.addListener(mConversationsClientListener)
            Log.d(TAG, "Success creating Twilio Conversations Client")
        }

        override fun onError(errorInfo: ErrorInfo) {
            conversationsManagerListener?.showError(errorInfo.message)
            Log.e(TAG, "Error creating Twilio Conversations Client: ${errorInfo.message}")
        }
    }

    private val mDefaultConversationListener = object : ConversationListener {
        override fun onMessageAdded(message: Message) {
            Log.d(TAG, "Message added")
            messages.add(message)
            if (message.author == userid) {
                conversation!!.setAllMessagesRead {

                }
            }
            if (conversationsManagerListener != null) {
                conversationsManagerListener!!.receivedNewMessage()

            }
        }

        override fun onMessageUpdated(message: Message, updateReason: Message.UpdateReason) {
            conversation!!.setAllMessagesRead {

            }
        }

        override fun onMessageDeleted(message: Message) {
            Log.d(TAG, "Message deleted")
        }

        override fun onParticipantAdded(participant: Participant) {
            Log.d(TAG, "Participant added: ${participant.identity}")
        }

        override fun onParticipantUpdated(participant: Participant, updateReason: Participant.UpdateReason) {
            try {
                Log.d(TAG, "Participant updated: ${participant.identity} $updateReason")
               /* if (conversation != null && identity != null) {
                    lastReadTime(conversation!!, identity!!)
                }*/
            }catch (e:Exception){
                Log.e(TAG, "Error in onParticipantUpdated: ${e.localizedMessage}", e)
            }
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
            conversation!!.setAllMessagesRead {

            }
        }
    }

     fun subscribeToUserStatus(conversationsClient: ConversationsClient, identity: String) {
      conversationsClient.getAndSubscribeUser(identity,object : CallbackListener<User> {
          override fun onSuccess(result: User?) {
              result?.let {
                  val statusText = if (result.isOnline) "Online" else "Offline"
                  conversationsManagerListener?.onlineOffline(statusText)
                  Log.d(TAG, "Last seen at: $statusText")
              }
          }

      })
    }





}