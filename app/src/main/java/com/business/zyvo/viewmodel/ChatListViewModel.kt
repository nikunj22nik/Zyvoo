package com.business.zyvo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.R
import com.business.zyvo.model.ChatListModel
import com.business.zyvo.repository.ZyvoRepository
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
           ChatListModel(R.drawable.ic_mia_pic,"Host by Mia","3 minutes ago","Hello can we talk aboutsfdfsdf")
       )
        _list.value = listItem
    }

}