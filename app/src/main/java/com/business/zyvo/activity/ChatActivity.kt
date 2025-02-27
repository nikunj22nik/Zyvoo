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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.business.zyvo.AppConstant
import com.business.zyvo.LoadingUtils
import com.business.zyvo.adapter.ChatDetailsAdapter
import com.business.zyvo.chat.QuickstartConversationsManager
import com.business.zyvo.chat.QuickstartConversationsManagerListener
import com.business.zyvo.databinding.FragmentChatDetailsBinding
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.CompressImage
import com.business.zyvo.utils.NetworkMonitorCheck
import com.business.zyvo.utils.PrepareData
import com.business.zyvo.viewmodel.ChatDetailsViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.jaiselrahman.filepicker.model.MediaFile
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileNotFoundException



class ChatActivity : AppCompatActivity(), QuickstartConversationsManagerListener {

    lateinit var binding: FragmentChatDetailsBinding
    lateinit var adapter: ChatDetailsAdapter

    private var quickstartConversationsManager = QuickstartConversationsManager()
    lateinit var sessionManagement: SessionManager
    private lateinit var viewModel: ChatDetailsViewModel
    var profileImage :String =""
    var providertoken :String =""
    var groupName :String =""
    var friendprofileimage :String =""
    var userId :Int =-1
    var friendId :Int =-1
    var friend_name :String =""
    private var file: File? = null
    var PERMISSIONS: Array<String> = arrayOf(
        permission.CAMERA,
        permission.READ_EXTERNAL_STORAGE,
        permission.WRITE_EXTERNAL_STORAGE,
    )

    private val REQUEST_IMAGE_CODE_ASK_PERMISSIONS = 123

    private val mediaFiles: ArrayList<MediaFile?> = ArrayList<MediaFile?>()
    private val REQUEST_Folder = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentChatDetailsBinding.inflate(LayoutInflater.from(this))
        sessionManagement = SessionManager(this)

        if (intent.extras != null) {
            profileImage = intent?.extras?.getString("user_img").toString()
            providertoken = sessionManagement.getChatToken().toString()
            userId = intent?.extras?.getInt(AppConstant.USER_ID)?.toInt() ?: 0
            groupName = intent?.extras?.getString(AppConstant.CHANNEL_NAME).toString()
            friendId = intent?.extras?.getInt(AppConstant.FRIEND_ID)?.toInt() ?: 0
            friendprofileimage = intent?.extras?.getString("friend_img").toString()
            friend_name = intent?.extras?.getString("friend_name").toString()
        }

        initialize()
        setContentView(binding.root)


    }

    private fun initialize() {

        loadChat()

        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.imgFile.setOnClickListener {
            /*if (hasPermissions(this@ChatActivity, *PERMISSIONS)) {
                browsePicture()
            } else {
                ActivityCompat.requestPermissions(this@ChatActivity, PERMISSIONS, REQUEST_IMAGE_CODE_ASK_PERMISSIONS)
            }*/
            browsePicture()
        }

        binding.sendBtn.setOnClickListener {
            /*if(binding.etmassage.text.toString().trim().isNotEmpty()){
                quickstartConversationsManager.sendMessage(binding.etmassage.text.toString())
            }
            else{
                LoadingUtils.showErrorDialog(this,"Please Check Your Internet Connection")
            }*/
            quickstartConversationsManager.sendMessage(binding.etmassage.text.toString())
        }

    }

    private fun browsePicture() {
        val items = arrayOf<CharSequence>("Choose from gallery", "Take a photo", "File", "Cancel")
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
                } catch (e: Exception) {
                    Log.v("Exception", e.message!!)
                }
            } else if (items[item] == "File") {
                try {
                    fileIntent()
                } catch (e: Exception) {
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

    private fun loadChat(){
        if (NetworkMonitorCheck._isConnected.value) {
            LoadingUtils.showDialog(this,false)
            quickstartConversationsManager.setListener(this@ChatActivity)
            quickstartConversationsManager.initializeWithAccessToken(this, providertoken, groupName, friendId.toString(), userId.toString(),binding.tvStatus)
        }
        else{
            LoadingUtils.showErrorDialog(this, "Please Check Your Internet Connection")
        }
    }

    override fun receivedNewMessage() {
        LoadingUtils.hideDialog()
        binding.etmassage.text.clear()
        runOnUiThread {
            if (quickstartConversationsManager.messages.size == 1) {
                loadRecyclerview(quickstartConversationsManager)
            } else {
                adapter.notifyItemRangeChanged(quickstartConversationsManager.messages.size, 1)
                binding.rvChatting.scrollToPosition(quickstartConversationsManager.messages.size - 1)
                binding.rvChatting.visibility = View.VISIBLE
            }
        }
    }

    private fun loadRecyclerview(quickstartConversationsManager: QuickstartConversationsManager) {
        adapter = ChatDetailsAdapter(this, quickstartConversationsManager, userId.toString(),profileImage,friendprofileimage,friend_name)
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

    override fun showError() {
        TODO("Not yet implemented")
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n  which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
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
                Log.v("pdf", files[0].name.toString())
                val file = CompressImage.from(this, files[0].uri)
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
            val file = PrepareData.getPath(this@ChatActivity, uri!!)?.let { File(it) }
            LoadingUtils.showDialog(this,false)
            if (file != null) {
                quickstartConversationsManager.sendMessageImage(uri.path, file)
            }
        }
    }

}


