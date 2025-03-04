package com.business.zyvo.fragment.guest.home.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.repository.ZyvoRepository
import com.business.zyvo.utils.NetworkMonitor
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class FilterViewModel @Inject constructor(private val repository: ZyvoRepository, val networkMonitor: NetworkMonitor) : ViewModel() {

    val isLoading = MutableLiveData<Boolean>()



}