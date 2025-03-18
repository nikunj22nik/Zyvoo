package com.business.zyvo

import android.app.Application
import android.util.Log

import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.NetworkMonitor
import com.business.zyvo.utils.NetworkMonitorCheck
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject

@HiltAndroidApp
class MyApp :Application() {
    @Inject
    lateinit var networkMonitor: NetworkMonitor
//    lateinit var conversationsManager: QuickstartConversationsManager


    override fun onCreate() {
        super.onCreate()
        // Initialize global state here
        NetworkMonitorCheck.observeNetworkStatus(networkMonitor)
        var sessionManager = SessionManager(this)
        val token =SessionManager(this).getChatToken()
//        val token ="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImN0eSI6InR3aWxpby1mcGE7dj0xIn0.eyJqdGkiOiJTS2E3MzYxYjQwZjhkOWE5MTZiYTFkMTE0Y2Q1N2ZkMjQ2LTE3NDEwODI5NDciLCJpc3MiOiJTS2E3MzYxYjQwZjhkOWE5MTZiYTFkMTE0Y2Q1N2ZkMjQ2Iiwic3ViIjoiQUM5NTYxZDk4ZjM4YWQyYmFkYzllOWJmZGFmMjdhMzE5NyIsImV4cCI6MTc0MTA4NjU0NywiZ3JhbnRzIjp7ImlkZW50aXR5IjoiNzgiLCJjaGF0Ijp7InNlcnZpY2Vfc2lkIjoiSVMyZmVkZWQyZGI3NDQ0OWIzYjNjOTg3OTdhODMwYmVlOSJ9fX0.F-ojY82GfYc6UkdlalRKLMjWv3ylnH9jVwdIFIeYY0o"

//        token.let {
//            conversationsManager = QuickstartConversationsManager.getInstance()
//            conversationsManager.initializeWithAccessTokenBase(
//                this,
//                it.toString()
//            )
//            Log.d("Initialization" ,"Chat token initialization")
//        }
    }


//    fun resetQuickConversationManager(){
//        val token =SessionManager(this).getChatToken()
//        token?.let {
//            conversationsManager = QuickstartConversationsManager.getInstance()
//            conversationsManager.initializeWithAccessTokenBase(
//                this,
//                it.toString()
//            )
//
//            Log.d("Initialization" ,"Chat token re-initialization")
//        }
//    }



}