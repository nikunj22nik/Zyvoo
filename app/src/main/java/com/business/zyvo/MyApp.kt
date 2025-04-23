package com.business.zyvo

import android.app.Application
import android.util.Log
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
import com.business.zyvo.utils.NetworkMonitor
import com.business.zyvo.utils.NetworkMonitorCheck
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject

@HiltAndroidApp
class MyApp :Application() {
    @Inject
    lateinit var networkMonitor: NetworkMonitor
      var conversationsManager: QuickstartConversationsManager?=null
     var conversationsManagerOneTowOne: QuickstartConversationsManagerOneTowOne?=null
    //lateinit var conversationsManagerFragment:  QuickstartConversationsManagerFragment
     var conversationsManagerFragment: com.business.zyvo.fragment.host.QuickstartConversationsManager?=null

    override fun onCreate() {
        super.onCreate()
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

     fun initializeTwilioClient(token:String) {
         conversationsManager=QuickstartConversationsManager()
         conversationsManagerOneTowOne=QuickstartConversationsManagerOneTowOne()
         conversationsManagerFragment= com.business.zyvo.fragment.host.QuickstartConversationsManager()
         token.let {
             conversationsManager?.initializeWithAccessTokenBase(this, it)
             conversationsManagerOneTowOne?.initializeWithAccessTokenBase(this, it)
             conversationsManagerFragment?.initializeWithAccessToken(this, it,
                 "general", "")
             Log.d("******" ,"Chat token initialization")
         }
    }

    fun clearInstance() { // Reset instance if needed
        conversationsManager = null
        conversationsManagerOneTowOne = null
        conversationsManagerFragment = null
    }


}