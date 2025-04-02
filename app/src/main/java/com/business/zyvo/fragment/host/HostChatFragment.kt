package com.business.zyvo.fragment.host

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.business.zyvo.AppConstant
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.MyApp
import com.business.zyvo.NetworkResult
import com.business.zyvo.OnClickListener
import com.business.zyvo.R
import com.business.zyvo.TimeUtils
import com.business.zyvo.activity.ChatActivity
import com.business.zyvo.activity.GuesMain
import com.business.zyvo.adapter.AdapterChatList
import com.business.zyvo.databinding.FragmentChatBinding
import com.business.zyvo.model.ChannelListModel
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog.showToast
import com.business.zyvo.utils.NetworkMonitorCheck
import com.business.zyvo.viewmodel.host.ChatListHostViewModel
import com.skydoves.powerspinner.PowerSpinnerView
import com.twilio.conversations.CallbackListener
import com.twilio.conversations.Conversation
import com.twilio.conversations.ErrorInfo
import com.twilio.conversations.StatusListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class HostChatFragment : Fragment() , View.OnClickListener, QuickstartConversationsManagerListener {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapterChatList: AdapterChatList
    private val viewModel: ChatListHostViewModel by viewModels()
    var objects: Int = 0
    private var chatList :MutableList<ChannelListModel>  = mutableListOf()
    private  var userId :Int =-1
    private var filteredList: MutableList<ChannelListModel> = chatList.toMutableList()
    private var quickstartConversationsManager = QuickstartConversationsManager()


    private var map:HashMap<String,ChannelListModel> = HashMap()


    private var loggedInUserId : Int =-1
    val addedConversations = mutableSetOf<String>() // Track added conversation uniqueNames


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TESTING_ZYVOO_Proj", "onCreate OF CHAT")
        var sessionManager = SessionManager(requireContext())
        loggedInUserId = sessionManager.getUserId()!!

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        Log.d("TESTING_ZYVOO_Proj", "onCreateView OF CHAT")

        _binding = FragmentChatBinding.inflate(LayoutInflater.from(requireContext()), container, false)

        try {

            val myApp = activity?.application as? MyApp
            quickstartConversationsManager = myApp?.conversationsManagerFragment ?: QuickstartConversationsManager()
            quickstartConversationsManager.setListener(this)
        } catch (e: Exception) {
            Log.e("ChatActivity", "Error setting QuickstartConversationsManager listener", e)
        }


        // Observe the isLoading state
        lifecycleScope.launch {
            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    LoadingUtils.showDialog(requireContext(), false)
                } else {
                    LoadingUtils.hideDialog()
                }
            }
        }

        var sessionManager = SessionManager(requireContext())
        sessionManager.getUserId().let {
             if (it != null) {
                 userId = it
             }
         }
        searchFuntionality()
        return binding.root
    }

    private fun searchFuntionality() {

        binding.etSearchButton.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapterChatList.filter.filter(p0.toString().trim())
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageFilter.setOnClickListener(this)
        adapterChatList = AdapterChatList(requireContext(), chatList,object : OnClickListener {
            override fun itemClick(obj: Int) {
             //   findNavController().navigate(R.id.hostChatDetailsFragment)
            }

        },null)

        adapterChatListClick()

        binding.recyclerViewChat.adapter = adapterChatList
        viewModel.list.observe(viewLifecycleOwner, Observer { list ->

          //  adapterChatList.updateItem(list)
        })

        binding.etSearchButton.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
               // val searchText = p0.toString().trim()
            }
            override fun afterTextChanged(p0: Editable?) {
                val query = p0.toString()
                filter(query)
            }
        })

       // callingGetChatUser("")

    }


    private fun adapterChatListClick(){
        adapterChatList.setOnItemClickListener(object : AdapterChatList.onItemClickListener{
            override fun onItemClick(data: ChannelListModel, index: Int,type:String) {
                try {
                    when (type) {
                        AppConstant.DELETE ->{
                                Log.d("TESTING", data.group_name.toString())
                                quickstartConversationsManager.conversationsClient?.getConversation(
                                    data.group_name,
                                    object : CallbackListener<Conversation> {
                                        override fun onSuccess(conversation: Conversation) {
                                            conversation.leave(object : StatusListener {
                                                override fun onSuccess() {
                                                    Log.d("TESTING", "Delete Chat here")
                                                    chatList.remove(data)
                                                    adapterChatList.updateItem(chatList)
                                                }

                                                override fun onError(errorInfo: ErrorInfo) {
                                                    Log.e("TESTING", "Error deleting conversation: ${errorInfo.message}")
                                                }
                                            })

                                        }

                                        override fun onError(errorInfo: ErrorInfo) {
                                            Log.e(
                                                QuickstartConversationsManager.TAG,
                                                "Error retrieving conversation: " + errorInfo.message
                                            )
                                        }
                                    })
                        }
                        AppConstant.BLOCK -> {
                            Log.d("TESTING", "Blocking user for group: ${data.group_name}")
                            chatUserBlock(data, index)
                        }

                        AppConstant.MUTE -> {
                            Log.d("TESTING", "Muting chat for group: ${data.group_name}")
                            muteChat(data, index)
                        }

                        AppConstant.REPORT -> {
                            Log.d("TESTING", "Reporting chat for group: ${data.group_name}")
                        }
                        AppConstant.ARCHIVED -> {
                            Log.d("TESTING", "Reporting chat for group: ${data.group_name}")
                            toggleArchiveUnarchive(data,index)
                        }
                        else ->{
                            val intent = Intent(requireContext(), ChatActivity::class.java)
                            var channelName: String = data.group_name.toString()
                            if (data.receiver_id.equals(userId.toString())) {
                                intent.putExtra("user_img", data.receiver_image).toString()
                                SessionManager(requireContext()).getUserId()
                                    ?.let { it1 -> intent.putExtra(AppConstant.USER_ID, it1) }
                                Log.d("TESTING", "REVIEW HOST" + channelName)
                                intent.putExtra(AppConstant.CHANNEL_NAME, channelName)
                                intent.putExtra(AppConstant.FRIEND_ID, data.sender_id)
                                intent.putExtra("friend_img", data.sender_profile).toString()
                                intent.putExtra("friend_name", data.sender_name).toString()
                                intent.putExtra("user_name", data.receiver_name)
                                intent.putExtra("is_blocked", data.is_blocked)
                                intent.putExtra("is_favorite", data.is_favorite)
                                intent.putExtra("is_muted", data.is_muted)
                                intent.putExtra("is_archived", data.is_archived)
                            } else {
                                intent.putExtra("user_img", data.sender_profile).toString()
                                SessionManager(requireContext()).getUserId()
                                    ?.let { it1 -> intent.putExtra(AppConstant.USER_ID, it1) }
                                Log.d("TESTING", "REVIEW HOST" + channelName)
                                intent.putExtra(AppConstant.CHANNEL_NAME, channelName)
                                intent.putExtra(AppConstant.FRIEND_ID, data.receiver_id)
                                intent.putExtra("friend_img", data.receiver_image).toString()
                                intent.putExtra("friend_name", data.receiver_name).toString()
                                intent.putExtra("user_name", data.sender_name)
                                intent.putExtra("is_blocked", data.is_blocked)
                                intent.putExtra("is_favorite", data.is_favorite)
                                intent.putExtra("is_muted", data.is_muted)
                                intent.putExtra("is_archived", data.is_archived)
                            }

                            intent.putExtra("sender_id", data.sender_id)
                            startActivity(intent)
                        }
                    }
                }catch(e:Exception){
                    Log.d("TESTING","INSIDE THE CATCH BLOCK")
                }

            }

        })
    }

    private fun dialogReportIssue() {
        val dialog =  Dialog(requireActivity(), R.style.BottomSheetDialog)
        dialog.apply {
            setCancelable(true)
            setContentView(R.layout.report_violation)
            val crossButton: ImageView = findViewById(R.id.img_cross)
            val submit : RelativeLayout = findViewById(R.id.rl_submit_report)
            val txtSubmit : TextView = findViewById(R.id.txt_submit)
            val et_addiotnal_detail : EditText = findViewById(R.id.et_addiotnal_detail)
            val powerSpinner : PowerSpinnerView = findViewById(R.id.spinnerView1)
            submit.setOnClickListener {
                if (txtSubmit.text.toString().trim().equals("Submitted") == false) {
                    txtSubmit.text = "Submitted"
                }else if(et_addiotnal_detail.text.isEmpty()){
                    showToast(requireActivity(),AppConstant.additional)
                } else{
                    /*reportViolation(session?.getUserId().toString(),
                            *//*"44"*//*bookingId!!,
                            propertyData?.property_id.toString(),
                            reportReasonsMap.get(powerSpinner.text.toString()).toString(),
                            et_addiotnal_detail.text.toString(),dialog)*/
                }
            }
            // Handle item click
            powerSpinner.setOnSpinnerItemSelectedListener<String> { _, _, position, item ->

            }
            crossButton.setOnClickListener {
                dialog.dismiss()
            }

            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),  // Width 90% of screen
                ViewGroup.LayoutParams.WRAP_CONTENT                   // Height wrap content
            )
            window?.setBackgroundDrawableResource(android.R.color.transparent) // Optional


            show()
            //   listReportReasons(powerSpinner)
        }

    }

    fun filter(query: String) {
//        filteredList = if (query.isEmpty()) {
//            chatList.toMutableList()
//        } else {
//            chatList.filter {
//
//            }
//        }
//
//        adapterChatList.updateItem(filteredList)

    }


    private fun callingGetChatUser(archive_status:String) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch {
                var sessionManager = SessionManager(requireContext())
                LoadingUtils.showDialog(requireContext(),false)
                var userId = sessionManager.getUserId()
                if (userId != null) {
                    var sessionManager = SessionManager(requireContext())
                    var userType = sessionManager.getUserType()
                    if (userType != null) {
                        viewModel.getChatUserChannelList(userId,userType,archive_status).collect {
                            when (it) {
                                is NetworkResult.Success -> {
                                    if(it.data ==null  || it.data.size ==0){
                                        LoadingUtils.hideDialog()
                                    }
                                    it.data?.let {
                                        viewModel.chatChannel = it
                                        it.forEach {
                                            map.put(it.group_name.toString(),it)
                                        }
                                        Log.d("*******",map.size.toString() +" Map Size is ")
                                        Log.d("*******","Chat token is "+sessionManager.getChatToken())
                                        val myApp = activity?.application as? MyApp
                                        quickstartConversationsManager = myApp?.conversationsManagerFragment ?: QuickstartConversationsManager()
                                        if (myApp?.conversationsManagerFragment == null &&
                                            quickstartConversationsManager.conversationsClient ==null) {
                                            quickstartConversationsManager.initializeWithAccessToken(requireContext(),
                                                sessionManager.getChatToken(),"general",
                                               sessionManager.getUserId().toString())
                                            quickstartConversationsManager.setListener(this@HostChatFragment)
                                        }else{
                                            Log.d("*******","loadChatList")
                                            quickstartConversationsManager.loadChatList()
                                        }

                                    }
                                }

                                is NetworkResult.Error -> {
                                    LoadingUtils.hideDialog()
                                    Toast.makeText(requireContext(),it.message.toString(),Toast.LENGTH_LONG).show()
                                }

                                else -> {

                                }
                            }
                        }
                    }
                }
            }
        }else{
            showErrorDialog(requireContext(),
                resources.getString(R.string.no_internet_dialog_msg))
        }

    }

    private fun chatUserBlock(data: ChannelListModel, index: Int) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch {
                LoadingUtils.showDialog(requireContext(), false)
                val id = data.sender_id?.toInt()
                val group_name = data.group_name
                val block = if (data.is_blocked == 0) 1 else 0
                if (id != null && group_name != null) {
                    viewModel.blockUser(id, group_name.toString(), block).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                it.data?.let {
                                    Log.d("******", "ChatUserBlock")
                                    val chat = chatList.get(index)
                                    chat.is_blocked = block
                                    chatList.set(index, chat)
                                    showToast(
                                        requireContext(),
                                        it.get("message").asString
                                    )
                                }
                            }

                            is NetworkResult.Error -> {
                                LoadingUtils.hideDialog()
                                Toast.makeText(
                                    requireContext(),
                                    it.message.toString(),
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            else -> {

                            }
                        }
                    }
                }
            }
        }else{
            showErrorDialog(requireContext(),
                resources.getString(R.string.no_internet_dialog_msg))
        }
    }

    private fun muteChat(data: ChannelListModel, index: Int) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch {
                LoadingUtils.showDialog(requireContext(), false)
                val  group_name = data.group_name
                val muted = if (data.is_muted == 0) 1 else 0
                if (userId!=null && group_name!=null) {
                    viewModel.muteChat(userId, group_name.toString(),muted).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                it.data?.let {
                                    Log.d("******", "muteChat")
                                    val chat = chatList.get(index)
                                    chat.is_muted = muted
                                    chatList.set(index, chat)
                                    showToast(
                                        requireContext(),
                                        it.get("message").asString
                                    )
                                }
                            }

                            is NetworkResult.Error -> {
                                LoadingUtils.hideDialog()
                                Toast.makeText(
                                    requireContext(),
                                    it.message.toString(),
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            else -> {

                            }
                        }
                    }
                }
            }
        }else{
            showErrorDialog(requireContext(),
                resources.getString(R.string.no_internet_dialog_msg))
        }

    }

    private fun toggleArchiveUnarchive(data: ChannelListModel, index: Int) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch {
                LoadingUtils.showDialog(requireContext(), false)
                val  group_name = data.group_name
                val archived = if (data.is_archived == 0) 1 else 0
                userId?.let { id->
                    group_name?.let {
                        viewModel.toggleArchiveUnarchive(id, it).collect {
                            when (it) {
                                is NetworkResult.Success -> {
                                    it.data?.let {
                                        Log.d("******", "toggleArchiveUnarchive")
                                        val chat = chatList.get(index)
                                        chat.is_archived = archived
                                        chatList.set(index, chat)
                                        showToast(
                                            requireContext(),
                                            it.get("message").asString
                                        )
                                    }
                                }
                                is NetworkResult.Error -> {
                                    LoadingUtils.hideDialog()
                                    Toast.makeText(
                                        requireContext(),
                                        it.message.toString(),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                else -> {

                                }
                            }
                        }
                    }

                }

            }
        }else{
            showErrorDialog(requireContext(),
                resources.getString(R.string.no_internet_dialog_msg))
        }

    }

    private fun showPopupWindow(anchorView: View, position: Int) {
        // Inflate the custom layout for the popup menu
        val popupView =
            LayoutInflater.from(context).inflate(R.layout.popup_filter_all_conversations, null)

        // Create PopupWindow with the custom layout
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set click listeners for each menu item in the popup layout
        popupView.findViewById<TextView>(R.id.itemAllConversations).setOnClickListener {
            chatList.sortWith { o1, o2 ->
                if (o1?.date == null || o2?.date == null) 0 else o2.date.compareTo(o1.date)
            }
            adapterChatList.updateItem(chatList)
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemArchived).setOnClickListener {
            val localtchat:MutableList<ChannelListModel> =  mutableListOf()
            popupWindow.dismiss()
            val filteredChats = chatList.filter { it.is_archived == 1 }
            localtchat.addAll(filteredChats)
            localtchat.sortWith { o1, o2 ->
                if (o1?.date == null || o2?.date == null) 0 else o2.date.compareTo(o1.date)
            }

            adapterChatList.updateItem(localtchat)
        }
        popupView.findViewById<TextView>(R.id.itemUnread).setOnClickListener {
            popupWindow.dismiss()
            quickstartConversationsManager.let { quick ->
                quick.conversationsClient?.let {
                    LoadingUtils.showDialog(requireContext(),false)
                    quick.getUnreadConversations(it
                    ) { unreadConversations ->
                        LoadingUtils.hideDialog()
                        Log.d("******", "Unread conversations count: " + unreadConversations.size)
                        val localtchat:MutableList<ChannelListModel> =  mutableListOf()
                        for (conversation in unreadConversations) {
                            Log.d("******", "Unread conversation: " + conversation.uniqueName)
                            if (map.containsKey(conversation.uniqueName)) {
                                val filteredChats = chatList.filter { it.group_name == conversation.uniqueName }
                                localtchat.addAll(filteredChats)
                            }
                        }
                        localtchat.sortWith { o1, o2 ->
                            if (o1?.date == null || o2?.date == null) 0 else o2.date.compareTo(o1.date)
                        }

                        adapterChatList.updateItem(localtchat)
                    }
                }
            }

        }


        // Get the location of the anchor view (three-dot icon)
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        // Get the height of the PopupView after inflating it
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = popupView.measuredHeight
        val popupWeight = popupView.measuredWidth
        val screenWidht = context?.resources?.displayMetrics?.widthPixels
        val anchorX = location[1]
        val spaceEnd = screenWidht?.minus((anchorX + anchorView.width))

        val xOffset = if (popupWeight > spaceEnd!!) {
            // If there is not enough space below, show it above
            -(popupWeight + 20) // Adjust this value to add a gap between the popup and the anchor view
        } else {
            // Otherwise, show it below
            // 20 // This adds a small gap between the popup and the anchor view
            -(popupWeight + 20)
        }
        // Calculate the Y offset to make the popup appear above the three-dot icon
        val screenHeight = context?.resources?.displayMetrics?.heightPixels
        val anchorY = location[1]

        // Calculate the available space above the anchorView
        val spaceAbove = anchorY
        val spaceBelow = screenHeight?.minus((anchorY + anchorView.height))

        // Determine the Y offset
        val yOffset = if (popupHeight > spaceBelow!!) {
            // If there is not enough space below, show it above
            -(popupHeight + 20) // Adjust this value to add a gap between the popup and the anchor view
        } else {
            // Otherwise, show it below
            20 // This adds a small gap between the popup and the anchor view
        }

        // Show the popup window anchored to the view (three-dot icon)
        popupWindow.elevation = 8.0f  // Optional: Add elevation for shadow effect
        popupWindow.showAsDropDown(anchorView, xOffset, yOffset, Gravity.END)  // Adjust the Y offset dynamically
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.imageFilter->{
                showPopupWindow(binding.imageFilter,0)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? GuesMain)?.inboxColor()
        callingGetChatUser("")
    }

    override fun receivedNewMessage() {
        try {
            Log.d("*******","receivedNewMessage()")
            requireActivity().runOnUiThread {
                try {
                    var position =0
                    quickstartConversationsManager.messages.forEach {
                        Log.d("message ","*******"+it.messageBody  +" auther "+ it.conversation.uniqueName)
                        for(i in 0..chatList.size -1){
                            position =i
                            if(it.conversation.uniqueName.equals(chatList.get(i).group_name)){
                                var obj = map.get(it.conversation.uniqueName)
                                obj?.lastMessage = it.messageBody
                                obj?.lastMessageTime = TimeUtils.updateLastMsgTime(it.dateCreated)
                                obj?.isOnline = false
                                obj?.date=it.dateCreated
                                Log.d("TESTING_DATE",it.dateCreated.toString())
                                if (obj != null) {
                                    chatList.set(position,obj)
                                    map.put(it.conversation.uniqueName,obj)
                                }
                            }
                        }
                    }


                    chatList.sortWith { o1, o2 ->
                        if (o1?.date == null || o2?.date == null) 0 else o2.date.compareTo(o1.date)
                    }

                    adapterChatList.updateItem(chatList)

                }catch (e:Exception){
                    Log.d("******","msg :- "+e.message)
                }
            }
        }catch (e:Exception){
            Log.d("******","msg :- "+e.message)
        }

    }

    override fun messageSentCallback() {
        LoadingUtils.hideDialog()
        Log.d("*******", "messageSentCallback" )
    }


    fun updateAdapter(){
        requireActivity().runOnUiThread {
            try {
                chatList.clear()
                Log.d("*******", ""+quickstartConversationsManager.messages.size + " SIZE OF MESSAGE")
                if (quickstartConversationsManager.messages.size > 0 || quickstartConversationsManager.messages != null) {

                    quickstartConversationsManager.messages.forEach {
                        Log.d("*******","m 8888"+it.messageBody)
                      //  Log.d("*******","j 8888"+it.participant.conversation.state)
                        Log.d("*******","u 88888"+it.conversation.uniqueName)
                    }
                    for (conversation  in quickstartConversationsManager.conversationsClient?.myConversations!!) {
                        try {
                            if (map.containsKey(conversation.uniqueName)) {
                                var obj = map.get(conversation.uniqueName)
                                obj?.lastMessage ="hello" //i.messageBody
                                obj?.lastMessageTime = TimeUtils.updateLastMsgTime(conversation.dateCreated)
                                obj?.isOnline = false
                                obj?.date=conversation.dateCreated

                                if (obj != null) {
                                    chatList.add(obj)
                                    map.put(conversation.uniqueName,obj)
                                }
                            }
                        } catch (e: Exception) {
                            Log.d("*******", "data :-" + e.message)
                        }

                    }
                }
            }catch (e:Exception){
                Log.d("******","msg :- "+e.message)
            }

            chatList.sortWith { o1, o2 ->
                if (o1?.date == null || o2?.date == null) 0 else o2.date.compareTo(o1.date)
            }

            adapterChatList.updateItem(chatList)
        }
    }

    override fun reloadMessages() {
        Log.d("*******", "reloadMessages" )
        LoadingUtils.hideDialog()
        requireActivity().runOnUiThread {
            try {
                chatList.clear()
                Log.d("*******", "${chatList.size}")
                quickstartConversationsManager.messages?.takeIf { it.isNotEmpty() }?.forEach { message ->
                    try {
                        map.let { map ->
                            if (map.containsKey(message.conversation.uniqueName)) {
                                val obj = map.get(message.conversation.uniqueName)
                                obj?.let {
                                    obj.lastMessage = message.messageBody
                                    obj.lastMessageTime =
                                        TimeUtils.updateLastMsgTime(message.dateCreated)
                                    obj.isOnline = false
                                    obj.date = message.dateCreated
                                    // Only add if it's a new conversation
                                    if (obj != null) {
                                        chatList.add(obj)
                                        map[message.conversation.uniqueName] = obj
                                        addedConversations.add(message.conversation.uniqueName) // Mark as added
                                        Log.d(
                                            "*******",
                                            "Added conversation: ${message.conversation.uniqueName}"
                                        )
                                    }
                                }
                            }
                        }
                    }catch (e:Exception){
                        Log.e("*******", "Error processing message: ${e.message}", e)
                    }
                }
                /*if (quickstartConversationsManager.messages.size > 0 || quickstartConversationsManager.messages != null) {
                    for (i in quickstartConversationsManager.messages) {
                        try {
                            if (map.containsKey(i.conversation.uniqueName)) {
                                var obj = map.get(i.conversation.uniqueName)
                                obj?.lastMessage = i.messageBody
                                obj?.lastMessageTime = TimeUtils.updateLastMsgTime(i.dateCreated)
                                obj?.isOnline = false
                                obj?.date=i.dateCreated
                                // Fetch user identity (assuming it's in i.conversation)
                                if (obj != null*//* && i.conversation.uniqueName !in addedConversations*//*) {
                                    chatList.add(obj)
                                    map.put(i.conversation.uniqueName, obj)
                                    addedConversations.add(i.conversation.uniqueName) // Mark as added
                                    Log.d("*******", "" + TextUtils.join(",",addedConversations))
                                    Log.d("*******", "" + chatList.size)
                                }
                            }
                        } catch (e: Exception) {
                            Log.d("*******", "data :-" + e.message)
                        }

                    }
                }*/
            }catch (e:Exception){
                Log.d("******","msg :- "+e.message)
            }
            chatList.sortWith { o1, o2 ->
                if (o1?.date == null || o2?.date == null) 0 else o2.date.compareTo(o1.date)
            }

            adapterChatList.updateItem(chatList)
        }
    }

    override fun showError(message: String?) {
        if (message != null && isAdded && !requireActivity().isFinishing) {
            LoadingUtils.hideDialog()
            LoadingUtils.showSuccessDialog(requireContext(), message)
        }
    }

    override fun notInit() {
        Log.d("******","notInit")
        quickstartConversationsManager.initializeWithAccessToken(requireContext(),
            SessionManager(requireContext()).getChatToken(),"general",
            SessionManager(requireContext()).getUserId().toString())
        quickstartConversationsManager.setListener(this@HostChatFragment)
    }


}