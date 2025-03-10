package com.business.zyvo.viewmodel.host

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.model.ChannelListModel
import com.business.zyvo.model.ChatListModel
import com.business.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

 @HiltViewModel
class ChatListHostViewModel @Inject constructor(private val repository: ZyvoRepository): ViewModel(){
    private  val _list = MutableLiveData<MutableList<ChatListModel>>()
    val list : LiveData<MutableList<ChatListModel>> get() = _list
     var chatChannel :MutableList<ChannelListModel> = mutableListOf()

    init {
        load()
    }

    private fun load() {
        val listItem = mutableListOf<ChatListModel>(
            ChatListModel(R.drawable.ic_mia_pic,"Host by Mia","3 minutes ago","Hello can we talk aboutsfdfsdf"),
            ChatListModel(R.drawable.ic_mia_pic,"Host by Mia","3 minutes ago","Hello can we talk aboutsfdfsdf"),
            ChatListModel(R.drawable.ic_mia_pic,"Host by Mia","3 minutes ago","Hello can we talk aboutsfdfsdf"),
            ChatListModel(R.drawable.ic_mia_pic,"Host by Mia","3 minutes ago","Hello can we talk aboutsfdfsdf"),
            ChatListModel(R.drawable.ic_mia_pic,"Host by Mia","3 minutes ago","Hello can we talk aboutsfdfsdf"),
            ChatListModel(R.drawable.ic_mia_pic,"Host by Mia","3 minutes ago","Hello can we talk aboutsfdfsdf"),
            ChatListModel(R.drawable.ic_mia_pic,"Host by Mia","3 minutes ago","Hello can we talk aboutsfdfsdf"),
            ChatListModel(R.drawable.ic_support_team_image,"Support Team","3 minutes ago","Hello can we talk aboutsfdfsdf")
        )
        _list.value = listItem
    }

    // Method to remove item at a specific position
    fun removeItemAt(position: Int) {
        _list.value?.let {
            if (position in it.indices) {
                it.removeAt(position)
                _list.value = it // Trigger observer to update the adapter

            }

        }
    }

     suspend fun getChatUserChannelList(userId :Int,type :String) : Flow<NetworkResult<MutableList<ChannelListModel>>> {
         return repository.getUserChannel(userId,type).onEach {

         }
     }
}