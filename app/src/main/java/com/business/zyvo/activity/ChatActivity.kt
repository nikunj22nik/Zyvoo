

package com.business.zyvo.activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.MyApp
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.activity.guest.extratime.model.ReportReason
import com.business.zyvo.adapter.ChatDetailsAdapter
import com.business.zyvo.databinding.ActivityChatBinding
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.CompressImage
import com.business.zyvo.utils.NetworkMonitorCheck
import com.business.zyvo.utils.PrepareData
import com.business.zyvo.viewmodel.ChatDetailsViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.jaiselrahman.filepicker.model.MediaFile
import java.io.File
import java.io.FileNotFoundException
import com.business.zyvo.chat.QuickstartConversationsManagerListenerOneTowOne
import com.business.zyvo.chat.QuickstartConversationsManagerOneTowOne
import com.business.zyvo.fragment.host.QuickstartConversationsManager
import com.business.zyvo.model.ChannelListModel
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.ErrorDialog.showToast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.skydoves.powerspinner.PowerSpinnerView
import com.twilio.conversations.CallbackListener
import com.twilio.conversations.Conversation
import com.twilio.conversations.ErrorInfo
import com.twilio.conversations.Message
import com.twilio.conversations.StatusListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatActivity : AppCompatActivity(),QuickstartConversationsManagerListenerOneTowOne {

    lateinit var binding: ActivityChatBinding
    private var adapter: ChatDetailsAdapter?=null
    private var quickstartConversationsManager = QuickstartConversationsManagerOneTowOne()
    lateinit var sessionManagement: SessionManager
    var profileImage :String =""
    var providertoken :String =""
    var groupName :String =""
    var friendprofileimage :String =""
    var userId :String=""
    var sender_id :String=""
    var friendId :String=""
    var friend_name :String =""
    var userName :String = ""
    private val mediaFiles: ArrayList<MediaFile?> = ArrayList<MediaFile?>()
    private val REQUEST_Folder = 2
    private var is_blocked = 0
    private var is_favorite = 0
    private  var is_muted = 0
    private var is_archived = 0
    private var isOnline = false
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 30000L // 30 seconds
    private var isPolling = false // Flag to track polling state
    private var previousScreenMessage =""
    private val viewModel: ChatDetailsViewModel by lazy {
        ViewModelProvider(this)[ChatDetailsViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(LayoutInflater.from(this))
        sessionManagement = SessionManager(this)

        // Observe the isLoading state
        lifecycleScope.launch {
            viewModel.isLoading.observe(this@ChatActivity) { isLoading ->
                if (isLoading) {
                    LoadingUtils.showDialog(this@ChatActivity, false)
                } else {
                    LoadingUtils.hideDialog()
                }
            }
        }

        binding.etmassage.setOnClickListener {
            binding.etmassage.requestFocus() // Request focus explicitly
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etmassage, InputMethodManager.SHOW_IMPLICIT)
        }
        if (intent.extras != null) {
            profileImage = intent?.extras?.getString("user_img").toString()
            providertoken = sessionManagement.getChatToken().toString()
            userId = intent?.extras?.getInt(AppConstant.USER_ID).toString()
            groupName = intent?.extras?.getString(AppConstant.CHANNEL_NAME).toString()
            Log.d(ErrorDialog.TAG,groupName)
            friendId = intent?.extras?.getString(AppConstant.FRIEND_ID).toString()
            friendprofileimage = intent?.extras?.getString("friend_img","")?:""
            friend_name = intent?.extras?.getString("friend_name","")?:""
            userName = intent?.extras?.getString("user_name","")?:""
            is_blocked = intent?.extras?.getInt("is_blocked",0)?:0
            is_favorite = intent?.extras?.getInt("is_favorite",0)?:0
            is_muted = intent?.extras?.getInt("is_muted",0)?:0
            is_archived = intent?.extras?.getInt("is_archived",0)?:0
            userName = intent?.extras?.getString("user_name","")?:""
            sender_id = intent?.extras?.getString("sender_id","")?:""
            binding.tvStatus.setText(friend_name)
            Log.d(ErrorDialog.TAG,"$friend_name $userName $friendId $userId $groupName")
            if(intent?.extras?.containsKey("message") == true){
                Log.d("ZYVOO-TESTING"," Message Send in CHAT SCREEN")
                previousScreenMessage = intent?.extras?.getString("message").toString()

            }
            if (is_blocked==1){
                binding.rvChatting1.visibility = View.GONE
                binding.rvChattingblock.visibility = View.VISIBLE
            }else{
                binding.rvChatting1.visibility = View.VISIBLE
                binding.rvChattingblock.visibility = View.GONE
            }


            if (is_favorite==1){
                binding.imageUnFavourite.visibility = View.GONE
                binding.imageFavourite.visibility = View.VISIBLE
            }else{
                binding.imageUnFavourite.visibility = View.VISIBLE
                binding.imageFavourite.visibility = View.GONE
            }


            Glide.with(this).load(AppConstant.BASE_URL+ friendprofileimage).error(R.drawable.ic_img_not_found).into(binding.imageProfilePicture)

        }

        binding.etmassage.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etmassage, InputMethodManager.SHOW_IMPLICIT)
        }


        if (NetworkMonitorCheck._isConnected.value) {
            LoadingUtils.showDialog(this,false)

          /*  quickstartConversationsManager.initializeWithAccessToken(this@ChatActivity, providertoken, groupName, friendId.toString(), userId.toString(),"host")
            quickstartConversationsManager.setListener(this)*/
           /* try {
                quickstartConversationsManager = (application as MyApp).conversationsManagerOneTowOne!!
                if (quickstartConversationsManager!=null){
                    quickstartConversationsManager.setListener(this)
                    quickstartConversationsManager.loadConversationById(groupName, friendId, userId)
                }else{
                    quickstartConversationsManager.initializeWithAccessToken(this@ChatActivity,
                        providertoken, groupName, friendId, userId)
                    quickstartConversationsManager.setListener(this)
                }
            } catch (e: Exception) {
                Log.e("ChatActivity", "Error setting QuickstartConversationsManager listener", e)
            }*/
            try {
                val myApp = application as? MyApp
                quickstartConversationsManager = myApp?.conversationsManagerOneTowOne ?: QuickstartConversationsManagerOneTowOne()
                quickstartConversationsManager.setListener(this)
                if (myApp?.conversationsManagerOneTowOne == null) {
                    quickstartConversationsManager.initializeWithAccessToken(
                        this@ChatActivity, providertoken, groupName, friendId, userId
                    )
                    myApp?.conversationsManagerOneTowOne = quickstartConversationsManager
                }
                quickstartConversationsManager.loadConversationById(groupName, friendId, userId)
            } catch (e: Exception) {
                Log.e("ChatActivity", "Error setting QuickstartConversationsManager listener", e)
            }

        }else{
            LoadingUtils.showSuccessDialog(this,"Please check your internet connection")
        }

        binding.imageThreeDots.setOnClickListener {
            showPopupWindow(it)
        }

        binding.rvChattingblock.setOnClickListener {
            chatUserBlock(groupName,0)
        }
        binding.imageUnFavourite.setOnClickListener {
            markFavoriteChat(groupName,1)
        }

        binding.imageFavourite.setOnClickListener {
            markFavoriteChat(groupName,0)
        }

        initialize()

        setContentView(binding.root)
    }

    private fun initialize() {

        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.imgFile.setOnClickListener {
            if (is_blocked==2){
                showErrorDialog(this, "You are blocked by $friend_name.")
            }else {
                browsePicture()
            }
        }

        binding.sendBtn.setOnClickListener {
            if (is_blocked==2){
                showErrorDialog(this, "You are blocked by $friend_name.")
            }else {
                if (NetworkMonitorCheck._isConnected.value) {
                    if (binding.etmassage.text.toString().trim().isNotEmpty()) {
                        quickstartConversationsManager.sendMessage(binding.etmassage.text.toString())
                    } else {
                        LoadingUtils.showErrorDialog(this, "Message can't be empty")
                    }
                } else {
                    LoadingUtils.showSuccessDialog(this, "Please check your internet connection")
                }
            }
        }

    }

    private fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission!!
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }


    private fun browsePicture() {
        val items = arrayOf<CharSequence>("Choose from gallery", "Take a photo", "Cancel")
        val builder = AlertDialog.Builder(this@ChatActivity)
        builder.setTitle("Select Image")
        builder.setItems(items) { dialog: DialogInterface, item: Int ->
            if (items[item] == "Choose from gallery") {
                try {
                    galleryIntent()
                } catch (e: Exception) {
                    Log.v("Exception", e.message!!)
                }
            } else if (items[item] == "Take a photo") {
                try {
                    cameraIntent()
                }
                catch (e: Exception) {
                    Log.v("Exception", e.message!!)
                }
            } else if (items[item] == "File") {
                try {
                    fileIntent()
                }
                catch (e: Exception) {
                    Log.v("Exception", e.message!!)
                }
            } else if (items[item] == "Cancel") {
                dialog.dismiss()
            }
        }
        builder.show()
    }


    private fun cameraIntent() {
        ImagePicker.with(this)
            .crop(4f,4f)
            .cameraOnly() //Crop image(Optional), Check Customization for more option
            .compress(1024) //Final image size will be less than 1 MB(Optional)
            .maxResultSize(
                1080,
                1080
            ) //Final image resolution will be less than 1080 x 1080(Optional)
            .start()
    }


    private fun galleryIntent() {
        ImagePicker.with(this)
            .crop(4f,4f)
            .galleryOnly() //Crop image(Optional), Check Customization for more option
            .compress(1024) //Final image size will be less than 1 MB(Optional)
            .maxResultSize(
                1080,
                1080
            ) //Final image resolution will be less than 1080 x 1080(Optional)
            .start()
    }


    private fun fileIntent(){
        val intent = Intent(this@ChatActivity, FilePickerActivity::class.java)
        intent.putExtra(
            FilePickerActivity.CONFIGS, Configurations.Builder()
                .setCheckPermission(true)
                .setSelectedMediaFiles(mediaFiles)
                .setShowFiles(true)
                .setShowImages(false)
                .setShowAudios(false)
                .setShowVideos(false)
                .setIgnoreNoMedia(false)
                .enableVideoCapture(false)
                .enableImageCapture(false)
                .setIgnoreHiddenFile(false)
                .setMaxSelection(1)
                .build()
        )
        startActivityForResult(intent, REQUEST_Folder)
    }


    override fun receivedNewMessage() {
        LoadingUtils.hideDialog()
        binding.etmassage.text.clear()
        runOnUiThread {
            if (quickstartConversationsManager.messages.size == 1) {
                Log.d("TESTING","RECEIVE NEW MESSAGE 1")
                loadRecyclerview(quickstartConversationsManager)
            } else {
                Log.d("TESTING","RECEIVE NEW MESSAGE 2${quickstartConversationsManager.messages.size}")
                if (adapter!=null) {
                    adapter?.notifyItemRangeChanged(quickstartConversationsManager.messages.size, 1)
                    binding.rvChatting.scrollToPosition(quickstartConversationsManager.messages.size - 1)
                    binding.rvChatting.visibility = View.VISIBLE
                }
            }
        }
    }


    private fun loadRecyclerview(quickstartConversationsManager: QuickstartConversationsManagerOneTowOne) {
        Log.d("TESTING","RECEIVE NEW MESSAGE 1")
        adapter = ChatDetailsAdapter(this, quickstartConversationsManager, userId.toString(),profileImage,friendprofileimage,friend_name,userName)
        binding.rvChatting.adapter = adapter
        binding.rvChatting.scrollToPosition(quickstartConversationsManager.messages.size - 1)
        binding.rvChatting.visibility = View.VISIBLE
    }


    override fun messageSentCallback() {
        Log.d("ZYVOO-TESTING","messageSentCallback")
        if (!isOnline) {
            sendChatNotification()
        }
    }


    override fun reloadMessages() {
        Log.d(ErrorDialog.TAG,"reloadLastMessages")
        LoadingUtils.hideDialog()
        runOnUiThread {
            if (quickstartConversationsManager.messages.size > 0) {
                loadRecyclerview(quickstartConversationsManager)
                if(previousScreenMessage.length > 0) {
                    quickstartConversationsManager.sendMessage(previousScreenMessage)
                    Log.d(ErrorDialog.TAG,previousScreenMessage+" massage found")
                }
              //  Log.d(ErrorDialog.TAG,previousScreenMessage+" massage found")
            } else {
                if(previousScreenMessage.length > 0) {
                    quickstartConversationsManager.sendMessage(previousScreenMessage)
                }
                Log.d(ErrorDialog.TAG,"not massage found")
            }
        }
    }

    override fun reloadLastMessages() {
        LoadingUtils.hideDialog()
        Log.d(ErrorDialog.TAG,"reloadLastMessages")
        Log.d(ErrorDialog.TAG,previousScreenMessage+" Previous Screen message")
       /* if(previousScreenMessage!= null && previousScreenMessage.length >0) {
            quickstartConversationsManager.sendMessage(previousScreenMessage)
        }*/
        quickstartConversationsManager.loadConversationById(groupName, friendId, userId)
    }

    override fun showError(message:String) {
        if (!(isFinishing) && !(isDestroyed)) {
            LoadingUtils.hideDialog()
           // LoadingUtils.showSuccessDialog(this, message)
        }
    }

    override fun onlineOffline(value:String) {
        value?.let {
            Log.d("*******",it)
            if (it.equals("Online")){
                isOnline = true
            }else{
                isOnline = false

            }
            binding.tvLastMessage?.text = it
            startUserStatusPolling()

        }
    }

    override fun notInit() {
        quickstartConversationsManager?.initializeWithAccessToken(
                this@ChatActivity, providertoken, groupName, friendId, userId)
        quickstartConversationsManager?.setListener(this)
    }

    private fun startUserStatusPolling() {
        if (isPolling) return // Prevent duplicate polling
        isPolling = true
        handler.post(object : Runnable {
            override fun run() {
                quickstartConversationsManager.conversationsClient?.let { conversation ->
                    quickstartConversationsManager.identity?.let { identity ->
                        quickstartConversationsManager.subscribeToUserStatus(conversation, identity)
                        handler.postDelayed(this, updateInterval) // Continue polling
                    }
                }
            }
        })
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ImagePicker.REQUEST_CODE) {
            if (data != null) {
                try {
                    onCaptureImageResult(data)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
        if (requestCode == this.REQUEST_Folder) {
            if (data != null) {
                onSelectFromFolderResult(data)
            }
        }
    }


    private fun onSelectFromFolderResult(data: Intent?) {
        if (data != null) {
            try {
                val selectedImage = data.data
                val files = data.getParcelableArrayListExtra<MediaFile>(FilePickerActivity.MEDIA_FILES)
                Log.v("pdf", files!![0].uri.toString())
                Log.v("pdf", files!![0].name.toString())
                val file = CompressImage.from(this, files!![0].uri)
                val path = file!!.absolutePath
                LoadingUtils.showDialog(this,false)
                quickstartConversationsManager.sendMessagefile(path, file)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    @Throws(FileNotFoundException::class)
    private fun onCaptureImageResult(data: Intent) {
        if (data.data != null) {
            val uri = data.data
            val file = File(PrepareData.getPath(this@ChatActivity, uri!!))
            LoadingUtils.showDialog(this,false)
            quickstartConversationsManager.sendMessageImage(uri.path, file)
        }
    }


    private fun showPopupWindow(anchorView: View) {
        // Inflate the custom layout for the popup menu
        val popupView =
            LayoutInflater.from(this).inflate(R.layout.layout_custom_popup_menu, null)

        // Create PopupWindow with the custom layout
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupView.findViewById<TextView>(R.id.itemReport).setOnClickListener {
            popupWindow.dismiss()
            dialogReportIssue()
        }
        popupView.findViewById<TextView>(R.id.itemDelete).setOnClickListener {
            popupWindow.dismiss()
            deleteConvertion()

        }
        // Find the "block" item inside the popup
        val blockItem = popupView.findViewById<View>(R.id.itemBlock)
        val unblockItem = popupView.findViewById<View>(R.id.itemUnBlock)
        val itemMute = popupView.findViewById<View>(R.id.itemMute)
        val itemunMute = popupView.findViewById<View>(R.id.itemunMute)
        val itemArchived = popupView.findViewById<View>(R.id.itemArchived)
        val itemUnArchived = popupView.findViewById<View>(R.id.itemUnArchived)
        if (is_blocked==1){
            blockItem.visibility = View.GONE
            unblockItem.visibility = View.VISIBLE
        }else{
            blockItem.visibility = View.VISIBLE
            unblockItem.visibility = View.GONE
        }
        if (is_muted==1){
            itemMute.visibility = View.GONE
            itemunMute.visibility = View.VISIBLE
        }else{
            itemMute.visibility = View.VISIBLE
            itemunMute.visibility = View.GONE
        }

        if (is_archived==1){
            itemArchived.visibility = View.GONE
            itemUnArchived.visibility = View.VISIBLE
        }else{
            itemArchived.visibility = View.VISIBLE
            itemUnArchived.visibility = View.GONE
        }
        blockItem.setOnClickListener {
            chatUserBlock(groupName,1)
            popupWindow.dismiss()
        }

        unblockItem.setOnClickListener {
            chatUserBlock(groupName,0)
            popupWindow.dismiss()
        }

        itemMute.setOnClickListener {
            muteChat(groupName,1)
            popupWindow.dismiss()
        }

        itemunMute.setOnClickListener {
            muteChat(groupName,0)
            popupWindow.dismiss()
        }

        itemArchived.setOnClickListener {
            popupWindow.dismiss()
            toggleArchiveUnarchive(groupName)
        }

        itemUnArchived.setOnClickListener {
            popupWindow.dismiss()
            toggleArchiveUnarchive(groupName)
        }

        // Get the location of the anchor view (three-dot icon)
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        // Get the height of the PopupView after inflating it
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = popupView.measuredHeight
        val popupWeight = popupView.measuredWidth
        val screenWidht = resources.displayMetrics.widthPixels
        val anchorX = location[1]
        val spaceEnd = screenWidht - (anchorX + anchorView.width)

        val xOffset = if (popupWeight > spaceEnd) {
            // If there is not enough space below, show it above
            -(popupWeight + 20) // Adjust this value to add a gap between the popup and the anchor view
        } else {
            // Otherwise, show it below
            // 20 // This adds a small gap between the popup and the anchor view
            -(popupWeight + 20)
        }
        // Calculate the Y offset to make the popup appear above the three-dot icon
        val screenHeight = resources.displayMetrics.heightPixels
        val anchorY = location[1]

        // Calculate the available space above the anchorView
        val spaceAbove = anchorY
        val spaceBelow = screenHeight - (anchorY + anchorView.height)

        // Determine the Y offset
        val yOffset = if (popupHeight > spaceBelow) {
            // If there is not enough space below, show it above
            -(popupHeight + 20) // Adjust this value to add a gap between the popup and the anchor view
        } else {
            // Otherwise, show it below
            20 // This adds a small gap between the popup and the anchor view
        }

        // Show the popup window anchored to the view (three-dot icon)
        popupWindow.elevation = 8.0f  // Optional: Add elevation for shadow effect
        popupWindow.showAsDropDown(
            anchorView,
            xOffset,
            yOffset,
            Gravity.END
        )  // Adjust the Y offset dynamically
    }

    private fun deleteConvertion() {
        if (groupName.isNullOrBlank()) {
            Log.e("TESTING", "Group name is null or empty.")
            return
        }

        val manager = quickstartConversationsManager
        val client = manager?.conversationsClient

        if (client == null) {
            Log.e("TESTING", "Conversations client is not initialized.")
            return
        }
        try {
            client.getConversation(
                groupName,
                object : CallbackListener<Conversation> {
                    override fun onSuccess(conversation: Conversation) {
                        conversation.leave(object : StatusListener {
                            override fun onSuccess() {
                                Log.d("TESTING", "Delete Chat here")
                                finish()
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
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun dialogReportIssue() {
        var reportReasonsMap: HashMap<String, Int> = HashMap()
        val dialog =  Dialog(this, R.style.BottomSheetDialog)
        dialog.apply {
            setCancelable(true)
            setContentView(R.layout.report_violation)
            val crossButton: ImageView = findViewById(R.id.img_cross)
            val submit : RelativeLayout = findViewById(R.id.rl_submit_report)
            val txtSubmit : TextView = findViewById(R.id.txt_submit)
            val et_addiotnal_detail : EditText = findViewById(R.id.et_addiotnal_detail)
            val powerSpinner : PowerSpinnerView = findViewById(R.id.spinnerView1)
            submit.setOnClickListener {
               /* if (txtSubmit.text.toString().trim().equals("Submitted") == false) {
                    txtSubmit.text = "Submitted"
                }else*/
//                if(et_addiotnal_detail.text.isEmpty()){
//                    showToast(this@ChatActivity,AppConstant.additional)
//                }
//                else
                    if(powerSpinner.text.toString().isEmpty()){
                    showToast(this@ChatActivity,AppConstant.spinner)
                }
                else{
                   friendId?.let {
                        reportChat(it,
                            reportReasonsMap.get(powerSpinner.text.toString()).toString(),
                            et_addiotnal_detail.text.toString(),dialog)
                    }
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
            listReportReasons(powerSpinner,reportReasonsMap)
        }

    }

    private fun reportChat(reported_user_id: String, reason: String, message: String, dialog: Dialog) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch {
                LoadingUtils.showDialog(this@ChatActivity, false)
                userId?.let { id->
                    viewModel.reportChat(id.toString(),reported_user_id,
                        reason,message,groupName).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                it.data?.let {
                                    Log.d("******", "toggleArchiveUnarchive")
                                    dialog.dismiss()
                                    showToast(
                                        this@ChatActivity,
                                        it.get("message").asString
                                    )
                                }
                            }
                            is NetworkResult.Error -> {
                                LoadingUtils.hideDialog()
                                Toast.makeText(
                                    this@ChatActivity,
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
            showErrorDialog(this,
                resources.getString(R.string.no_internet_dialog_msg))
        }

    }
    private fun listReportReasons(
        powerSpinner: PowerSpinnerView,
        reportReasonsMap: HashMap<String, Int>
    ) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.listReportReasons().collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let { resp ->
                                val items: MutableList<String> = ArrayList()

                                val listType = object : TypeToken<List<ReportReason>>() {}.type
                                val reportReason:MutableList<ReportReason> = Gson().fromJson(resp, listType)
                                // Populate the HashMap
                                reportReason.forEach { reason ->
                                    reportReasonsMap[reason.reason] = reason.id
                                    items.add(reason.reason)
                                }
                                powerSpinner.setItems(items)
                            }
                        }
                        is NetworkResult.Error -> {
                            showErrorDialog(this@ChatActivity, it.message!!)
                        }

                        else -> {
                            Log.v(ErrorDialog.TAG, "error::" + it.message)
                        }
                    }
                }
            }
        }else{
            showErrorDialog(this,
                resources.getString(R.string.no_internet_dialog_msg))
        }
    }

    private fun chatUserBlock(groupName: String,value:Int) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch {
                sender_id?.let {
                    LoadingUtils.showDialog(this@ChatActivity, false)
                    viewModel.blockUser(it.toInt(), groupName,value).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                it.data?.let {
                                    Log.d("******", "ChatUserBlock")
                                    is_blocked = value
                                    if (is_blocked==1){
                                        binding.rvChatting1.visibility = View.GONE
                                        binding.rvChattingblock.visibility = View.VISIBLE
                                    }else{
                                        binding.rvChatting1.visibility = View.VISIBLE
                                        binding.rvChattingblock.visibility = View.GONE
                                    }
                                }
                            }

                            is NetworkResult.Error -> {
                                LoadingUtils.hideDialog()
                                Toast.makeText(
                                    this@ChatActivity,
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
            showErrorDialog(this,
                resources.getString(R.string.no_internet_dialog_msg))
        }

        }

    private fun muteChat(groupName: String,value:Int) {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch {
                val userId: Int? = sessionManagement.getUserId()
                if (userId != null && userId != -1) {
                    LoadingUtils.showDialog(this@ChatActivity, false)
                    viewModel.muteChat(userId, groupName,value).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                it.data?.let {
                                    Log.d("******", "muteChat")
                                    is_muted = value
                                }
                            }

                            is NetworkResult.Error -> {
                                LoadingUtils.hideDialog()
                                Toast.makeText(
                                    this@ChatActivity,
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
            showErrorDialog(this,
                resources.getString(R.string.no_internet_dialog_msg))
        }

    }

    private fun toggleArchiveUnarchive(groupName: String) {
        if (NetworkMonitorCheck._isConnected.value) {
        lifecycleScope.launch {
            LoadingUtils.showDialog(this@ChatActivity, false)
            val archived = if (is_archived == 0) 1 else 0
            userId?.let { id ->
                groupName?.let {
                    viewModel.toggleArchiveUnarchive(id.toInt(), it).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                it.data?.let {
                                    Log.d("******", "toggleArchiveUnarchive")
                                    is_archived = archived
                                }
                            }

                            is NetworkResult.Error -> {
                                LoadingUtils.hideDialog()
                                Toast.makeText(
                                    this@ChatActivity,
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
            showErrorDialog(this,
                resources.getString(R.string.no_internet_dialog_msg))
        }
    }

    private fun markFavoriteChat(groupName: String,value:Int) {
        Log.d("checkDetails","sender_id "+sender_id.toString()+" groupName"+groupName +" value"+value)
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch {
                sender_id?.let {
                    LoadingUtils.showDialog(this@ChatActivity, false)

                    viewModel.markFavoriteChat(it.toInt(), groupName, value).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                it.data?.let {
                                    Log.d("******", "ChatUserBlock")
                                    is_favorite = value
                                    if (is_favorite==1){
                                        binding.imageUnFavourite.visibility = View.GONE
                                        binding.imageFavourite.visibility = View.VISIBLE
                                    }else{
                                        binding.imageUnFavourite.visibility = View.VISIBLE
                                        binding.imageFavourite.visibility = View.GONE
                                    }
                                  //  binding.imageUnFavourite.visibility = View.GONE
                                  //  binding.imageFavourite.visibility = View.VISIBLE

                                }
                            }

                            is NetworkResult.Error -> {
                                LoadingUtils.hideDialog()
                                Toast.makeText(
                                    this@ChatActivity,
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
            showErrorDialog(this,
                resources.getString(R.string.no_internet_dialog_msg))
        }
        }

    private fun sendChatNotification() {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch {
                val userId: Int? = sessionManagement.getUserId()
                userId?.let { senderId->
                    friendId?.let { friendId->
                        viewModel.sendChatNotification(senderId.toString(),friendId,groupName).collect {
                            when (it) {
                                is NetworkResult.Success -> {
                                    it.data?.let {
                                        Log.d("******", "sendChatNotification")
                                    }
                                }

                                is NetworkResult.Error -> {
                                    LoadingUtils.hideDialog()
                                    /*Toast.makeText(
                                        this@ChatActivity,
                                        it.message.toString(),
                                        Toast.LENGTH_LONG
                                    ).show()*/
                                }

                                else -> {

                                }
                            }
                        }
                    }
                }
            }
        }else{
            showErrorDialog(this,
                resources.getString(R.string.no_internet_dialog_msg))
        }


    }

    private fun stopUserStatusPolling() {
        handler?.let {
            it.removeCallbacksAndMessages(null)
            Log.d("******","stopUserStatusPolling")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopUserStatusPolling()
        isPolling = false
    }

}