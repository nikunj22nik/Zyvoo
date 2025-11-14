package com.business.zyvo

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.business.zyvo.chat.QuickstartConversationsManager
import com.business.zyvo.chat.QuickstartConversationsManagerFragment
import com.business.zyvo.chat.QuickstartConversationsManagerOneTowOne
import com.business.zyvo.di.ConversationsManagerSingleton.instanceHost
import com.business.zyvo.di.ConversationsManagerSingleton.instanceMain
import com.business.zyvo.di.ConversationsManagerSingleton.instanceOneToOne
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.AppContextProvider
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.NetworkMonitor
import com.business.zyvo.utils.NetworkMonitorCheck
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject

@HiltAndroidApp
class MyApp :Application() {
    @Inject
    lateinit var networkMonitor: NetworkMonitor
      var conversationsManager: QuickstartConversationsManager?=null
     var conversationsManagerOneTowOne: QuickstartConversationsManagerOneTowOne?=null
     var conversationsManagerFragment: com.business.zyvo.fragment.host.QuickstartConversationsManager?=null

    override fun onCreate() {
        super.onCreate()
        try {
            FacebookSdk.setClientToken("4c4b7980b87baf696da16619cb364744")
            FacebookSdk.sdkInitialize(applicationContext)
            FacebookSdk.setAutoInitEnabled(true)
            FacebookSdk.fullyInitialize()

            Log.d("FB_LOGIN", "App: Facebook SDK Initialized: ${FacebookSdk.isInitialized()}")
        } catch (e: Exception) {
            Log.e("FB_LOGIN", "App: Facebook initialization failed: ${e.message}")
        }

        AppEventsLogger.activateApp(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Initialize global state here
        NetworkMonitorCheck.observeNetworkStatus(networkMonitor)
        AppContextProvider.initialize(this)
        val token =SessionManager(this).getChatToken()
        initializeTwilioClient(token!!)

        AppsFlyerLib.getInstance().init("yBV8TF8buEhf3rgFccBSyd", object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
                // Handle deep link data here
            }

            override fun onConversionDataFail(error: String?) {}

            override fun onAppOpenAttribution(data: MutableMap<String, String>?) {}

            override fun onAttributionFailure(error: String?) {}
        }, this)

        AppsFlyerLib.getInstance().start(this)
    }

   fun initializeTwilioClient(token: String) {
       if (conversationsManager != null &&
           conversationsManagerOneTowOne != null &&
           conversationsManagerFragment != null
       ) {
           Log.d(ErrorDialog.TAG, "Twilio clients already initialized")
           return
       }

       conversationsManager = QuickstartConversationsManager().apply {
           initializeWithAccessTokenBase(this@MyApp, token)
       }

       conversationsManagerOneTowOne = QuickstartConversationsManagerOneTowOne().apply {
           initializeWithAccessTokenBase(this@MyApp, token)
       }

       conversationsManagerFragment = com.business.zyvo.fragment.host.QuickstartConversationsManager().apply {
           initializeWithAccessToken(this@MyApp, token, "general", "")
       }

       Log.d("TwilioInit", "Chat token initialization successful")
   }


    fun clearInstance() { // Reset instance if needed
        conversationsManager = null
        conversationsManagerOneTowOne = null
        conversationsManagerFragment = null
    }


}