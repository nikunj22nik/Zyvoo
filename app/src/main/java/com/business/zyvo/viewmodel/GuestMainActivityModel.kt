package com.business.zyvo.viewmodel

import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class GuestMainActivityModel @Inject constructor(private val repository: ZyvoRepository):
    ViewModel(){

    suspend fun getChatToken(userId :Int,role:String) : Flow<NetworkResult<String>>{
        return repository.getChatToken(userId, role)
    }

}