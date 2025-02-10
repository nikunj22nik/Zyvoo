package com.business.zyvo

import android.app.Application
import com.business.zyvo.utils.NetworkMonitor
import com.business.zyvo.utils.NetworkMonitorCheck
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject

@HiltAndroidApp
class MyApp :Application() {
    @Inject
    lateinit var networkMonitor: NetworkMonitor

    override fun onCreate() {
        super.onCreate()
        // Initialize global state here
        NetworkMonitorCheck.observeNetworkStatus(networkMonitor)
    }


}