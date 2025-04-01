package com.business.zyvo.di

import android.annotation.SuppressLint
import android.content.Context
import com.business.zyvo.chat.QuickstartConversationsManager
import com.business.zyvo.chat.QuickstartConversationsManagerOneTowOne

object ConversationsManagerSingleton {
     @SuppressLint("StaticFieldLeak")
     var instanceOneToOne: QuickstartConversationsManagerOneTowOne?=null
    @SuppressLint("StaticFieldLeak")
    var instanceMain: QuickstartConversationsManager?=null
    var instanceHost: com.business.zyvo.fragment.host.QuickstartConversationsManager?=null

    fun init(context: Context, token: String) {
        if (instanceOneToOne == null) { // Initialize only once
            instanceOneToOne = QuickstartConversationsManagerOneTowOne()
            instanceOneToOne?.initializeWithAccessTokenBase(context, token)
        }
        if (instanceMain == null) { // Initialize only once
            instanceMain = QuickstartConversationsManager()
            instanceMain?.initializeWithAccessTokenBase(context, token)
        }
        if (instanceHost == null) { // Initialize only once
            instanceHost =  com.business.zyvo.fragment.host.QuickstartConversationsManager()
            instanceHost?.initializeWithAccessToken(context, token,"","")
        }
    }
    fun getInstance(): QuickstartConversationsManagerOneTowOne? {
        return instanceOneToOne
    }
    fun getInstance2(): QuickstartConversationsManager? {
        return instanceMain
    }
    fun getInstance3(): com.business.zyvo.fragment.host.QuickstartConversationsManager? {
        return instanceHost
    }
    fun isInitialized(): Boolean = instanceOneToOne != null

    fun clearInstance() { // Reset instance if needed
        instanceOneToOne = null
        instanceMain = null
        instanceHost = null
    }
}