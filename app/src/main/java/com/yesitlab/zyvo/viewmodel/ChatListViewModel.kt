package com.yesitlab.zyvo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.model.ChatListModel
import com.yesitlab.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(private val repository: ZyvoRepository): ViewModel(){
    private  val _list = MutableLiveData<MutableList<ChatListModel>>()
    val list : LiveData<MutableList<ChatListModel>> get() = _list

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
}