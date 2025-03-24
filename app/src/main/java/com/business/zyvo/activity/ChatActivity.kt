

package com.business.zyvo.activity


import android.Manifest.permission
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.LoadingUtils
import com.business.zyvo.MyApp
import com.business.zyvo.R
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
import com.business.zyvo.chat.QuickstartConversationsManager
import com.business.zyvo.chat.QuickstartConversationsManagerListenerOneTowOne
import com.business.zyvo.chat.QuickstartConversationsManagerOneTowOne


class ChatActivity : AppCompatActivity(),QuickstartConversationsManagerListenerOneTowOne {

    lateinit var binding: ActivityChatBinding
    private var adapter: ChatDetailsAdapter?=null
    private var quickstartConversationsManager = QuickstartConversationsManagerOneTowOne()
    lateinit var sessionManagement: SessionManager
    private lateinit var viewModel: ChatDetailsViewModel
    var profileImage :String =""
    var providertoken :String =""
    var groupName :String =""
    var friendprofileimage :String =""
    var userId :String=""
    var friendId :String=""
    var friend_name :String =""
    private var file: File? = null
    var userName :String = ""
    var PERMISSIONS: Array<String> = arrayOf(permission.CAMERA, permission.READ_EXTERNAL_STORAGE, permission.WRITE_EXTERNAL_STORAGE,)
    private val REQUEST_IMAGE_CODE_ASK_PERMISSIONS = 123
    private val mediaFiles: ArrayList<MediaFile?> = ArrayList<MediaFile?>()
    private val REQUEST_Folder = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(LayoutInflater.from(this))
        sessionManagement = SessionManager(this)

        binding.etmassage.setOnClickListener {
            binding.etmassage.requestFocus() // Request focus explicitly
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etmassage, InputMethodManager.SHOW_IMPLICIT)
        }

        if (intent.extras != null) {
            profileImage = intent?.extras?.getString("user_img").toString()
            providertoken = sessionManagement.getChatToken().toString()
            userId = intent?.extras?.getString(AppConstant.USER_ID).toString()
            groupName = intent?.extras?.getString(AppConstant.CHANNEL_NAME).toString()
            Log.d("TESTING_Group_NAME",groupName)
            friendId = intent?.extras?.getString(AppConstant.FRIEND_ID).toString()
            friendprofileimage = intent?.extras?.getString("friend_img").toString()
            friend_name = intent?.extras?.getString("friend_name").toString()
            userName = intent?.extras?.getString("user_name").toString()
            binding.tvStatus.setText(friend_name)

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
            try {
                quickstartConversationsManager = (application as MyApp).conversationsManagerOneTowOne
                if (quickstartConversationsManager?.conversationsClient!=null){
                    quickstartConversationsManager.setListener(this)
                    quickstartConversationsManager.loadConversationById(groupName, friendId, userId)
                }else{
                    quickstartConversationsManager.initializeWithAccessTokenBase(this@ChatActivity
                        ,sessionManagement.getChatToken().toString())
                }
            } catch (e: Exception) {
                Log.e("ChatActivity", "Error setting QuickstartConversationsManager listener", e)
            }
        }else{
            LoadingUtils.showSuccessDialog(this,"Please check your internet connection")
        }


        initialize()

        setContentView(binding.root)
    }

    private fun initialize() {

        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.imgFile.setOnClickListener {
//            if (hasPermissions(this@ChatActivity, *PERMISSIONS)) {
//                Log.d("TESTING_PERMISSION","PERMISSION GRANTED")
                browsePicture()
//            } else {
//                ActivityCompat.requestPermissions(this@ChatActivity, PERMISSIONS, REQUEST_IMAGE_CODE_ASK_PERMISSIONS)
//                Log.d("TESTING_PERMISSION","PERMISSION NOT GRANTED")
//            }
        }

        binding.sendBtn.setOnClickListener {
            if(NetworkMonitorCheck._isConnected.value) {
                if (binding.etmassage.text.toString().trim().isNotEmpty()) {
                    Log.d("TESTING","I am here in sending message")
                    quickstartConversationsManager.sendMessage(binding.etmassage.text.toString())
                } else {
                    LoadingUtils.showErrorDialog(this, "Message can't be empty")
                }
            }else{
                LoadingUtils.showSuccessDialog(this,"Please check your internet connection")
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
            .crop()
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
            .crop()
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
                Log.d("TESTING","RECEIVE NEW MESSAGE 2")
                if (adapter!=null) {
                    adapter?.notifyItemRangeChanged(quickstartConversationsManager.messages.size, 1)
                    binding.rvChatting.scrollToPosition(quickstartConversationsManager.messages.size - 1)
                    binding.rvChatting.visibility = View.VISIBLE
                }
            }
        }
    }


    private fun loadRecyclerview(quickstartConversationsManager: QuickstartConversationsManagerOneTowOne) {
        adapter = ChatDetailsAdapter(this, quickstartConversationsManager, userId.toString(),profileImage,friendprofileimage,friend_name,userName)
        binding.rvChatting.adapter = adapter
        binding.rvChatting.scrollToPosition(quickstartConversationsManager.messages.size - 1)
        binding.rvChatting.visibility = View.VISIBLE
    }


    override fun messageSentCallback() {
    }


    override fun reloadMessages() {
        LoadingUtils.hideDialog()
        runOnUiThread {
            if (quickstartConversationsManager.messages.size > 0) {
                loadRecyclerview(quickstartConversationsManager)
                Log.d("@Error","massage found")
            } else {
                Log.d("@Error","not massage found")
            }
        }
    }

    override fun reloadLastMessages() {
        Log.d("*******","reloadLastMessages")
        quickstartConversationsManager.loadConversationById(groupName, friendId, userId)
    }

    override fun showError() {

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



}