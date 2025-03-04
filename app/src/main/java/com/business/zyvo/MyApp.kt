package com.business.zyvo

import android.app.Application
import com.business.zyvo.chat.QuickstartConversationsManager
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.NetworkMonitor
import com.business.zyvo.utils.NetworkMonitorCheck
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject

@HiltAndroidApp
class MyApp :Application() {
    @Inject
    lateinit var networkMonitor: NetworkMonitor
    lateinit var conversationsManager: QuickstartConversationsManager


    override fun onCreate() {
        super.onCreate()
        // Initialize global state here
        NetworkMonitorCheck.observeNetworkStatus(networkMonitor)
        var sessionManager = SessionManager(this)
        val token =SessionManager(this).getChatToken()
        token?.let {
            conversationsManager = QuickstartConversationsManager()
            conversationsManager.initializeWithAccessTokenBase(
                this,
               it.toString()
            )
        }
    }


    fun resetQuickConversationManager(){
        val token =SessionManager(this).getChatToken()
        token?.let {
            conversationsManager = QuickstartConversationsManager()
            conversationsManager.initializeWithAccessTokenBase(
                this,
                it.toString()
            )
        }
    }



}