package com.business.zyvo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.model.ChatMessageModel
import com.business.zyvo.repository.ZyvoRepository
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ChatDetailsViewModel @Inject constructor(private var repository: ZyvoRepository): ViewModel() {
    val isLoading = MutableLiveData<Boolean>()

    suspend fun blockUser(senderId: Int,group_channel:String,blockUnblock:Int) : Flow<NetworkResult<JsonObject>> {
        return repository.blockUser(senderId,group_channel,blockUnblock).onEach {
            when (it) {
                is NetworkResult.Loading -> {
                    isLoading.value = true
                } is NetworkResult.Success -> {
                isLoading.value = false
            } else -> {
                isLoading.value = false
            }
            }
        }
    }


    suspend fun markFavoriteChat(senderId: Int,group_channel:String,favorite:Int) : Flow<NetworkResult<JsonObject>> {
        return repository.markFavoriteChat(senderId, group_channel, favorite).onEach {
            when (it) {
                is NetworkResult.Loading -> {
                    isLoading.value = true
                }

                is NetworkResult.Success -> {
                    isLoading.value = false
                }

                else -> {
                    isLoading.value = false
                }
            }
        }
    }

        suspend fun sendChatNotification(senderId: String,receiverId:String) : Flow<NetworkResult<JsonObject>> {
            return repository.sendChatNotification(senderId,receiverId).onEach {
                when (it) {
                    is NetworkResult.Loading -> {
                    } is NetworkResult.Success -> {
                } else -> {
                }
                }
            }
    }
    suspend fun muteChat(userId: Int,group_channel:String,mute:Int) :Flow<NetworkResult<JsonObject>> {
        return repository.muteChat(userId,group_channel,mute).onEach {
            when (it) {
                is NetworkResult.Loading -> {
                    isLoading.value = true
                } is NetworkResult.Success -> {
                isLoading.value = false
            } else -> {
                isLoading.value = false
            }
            }
        }
    }

    suspend fun toggleArchiveUnarchive(userId: Int,group_channel:String) :Flow<NetworkResult<JsonObject>> {
        return repository.toggleArchiveUnarchive(userId,group_channel).onEach {
            when (it) {
                is NetworkResult.Loading -> {
                    isLoading.value = true
                } is NetworkResult.Success -> {
                isLoading.value = false
            } else -> {
                isLoading.value = false
            }
            }
        }
    }

    suspend fun reportChat( reporter_id :String,
                            reported_user_id :String,
                            reason :String,
                            message :String) :Flow<NetworkResult<JsonObject>> {
        return repository.reportChat(reporter_id,reported_user_id,
            reason,message).onEach {
            when (it) {
                is NetworkResult.Loading -> {
                    isLoading.value = true
                } is NetworkResult.Success -> {
                isLoading.value = false
            } else -> {
                isLoading.value = false
            }
            }
        }
    }

    suspend fun listReportReasons( ):
            Flow<NetworkResult<JsonArray>>{
        return repository.listReportReasons().onEach {
            when(it){
                is NetworkResult.Loading -> {
                    isLoading.value = true
                } is NetworkResult.Success -> {
                isLoading.value = false
            } else -> {
                isLoading.value = false
            }
            }
        }
    }

}