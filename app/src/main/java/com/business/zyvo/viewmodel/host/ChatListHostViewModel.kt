package com.business.zyvo.viewmodel.host

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.BookingDetailModel
import com.business.zyvo.model.ChannelListModel
import com.business.zyvo.model.ChatListModel
import com.business.zyvo.repository.ZyvoRepository
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

 @HiltViewModel
class ChatListHostViewModel @Inject constructor(private val repository: ZyvoRepository): ViewModel(){
    private  val _list = MutableLiveData<MutableList<ChatListModel>>()
    val list : LiveData<MutableList<ChatListModel>> get() = _list
     var chatChannel :MutableList<ChannelListModel> = mutableListOf()
     val isLoading = MutableLiveData<Boolean>()

    fun removeItemAt(position: Int) {
        _list.value?.let {
            if (position in it.indices) {
                it.removeAt(position)
                _list.value = it // Trigger observer to update the adapter

            }

        }
    }

     suspend fun getChatUserChannelList(userId :Int,type :String,archive_status:String) : Flow<NetworkResult<MutableList<ChannelListModel>>> {
         return repository.getUserChannel(userId,type,archive_status).onEach {

         }
     }

     suspend fun blockUser(senderId: Int,group_channel:String,block:Int) :Flow<NetworkResult<JsonObject>> {
         return repository.blockUser(senderId,group_channel,block).onEach {
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
                             message :String,
                             group_channel:String) :Flow<NetworkResult<JsonObject>> {
         return repository.reportChat(reporter_id,reported_user_id,
             reason,message,group_channel).onEach {
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

     suspend fun deleteChat(user_id :String,
                               user_type :String,
                               group_channel :String) :Flow<NetworkResult<JsonObject>> {
         return repository.deleteChat(user_id,user_type, group_channel).onEach {
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
}