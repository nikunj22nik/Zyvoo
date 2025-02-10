package com.business.zyvo.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

object NetworkMonitorCheck {
    val _isConnected = MutableStateFlow(true)

    private val _showNoInternetDialog = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val showNoInternetDialog: Flow<Unit> = _showNoInternetDialog.asSharedFlow()

    private var hasShownDialog = false

    fun observeNetworkStatus(networkMonitor: NetworkMonitor) {
        CoroutineScope(Dispatchers.IO).launch {
            networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { connected ->
                    _isConnected.value = connected
                    if (!connected && !hasShownDialog) {
                        hasShownDialog = true
                        _showNoInternetDialog.emit(Unit)
                    } else if (connected) {
                        hasShownDialog = false
                    }
                }
        }
    }
}