package com.business.zyvo.viewmodel.host

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.R
import com.business.zyvo.model.ChatMessageModel
import com.business.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

 @HiltViewModel
class HostChatDetailsViewModel @Inject constructor(private var repository: ZyvoRepository): ViewModel() {

    private  var _list = MutableLiveData<MutableList<ChatMessageModel>>()
    val list : LiveData<MutableList<ChatMessageModel>> get() =  _list

    init {
        load()
    }

    private fun load() {
        val listItem = mutableListOf<ChatMessageModel>(
            ChatMessageModel(R.drawable.ic_mia_pic,"Mia","Jul 20, 2023, 11:32 AM","Hi welcome to our house!"),
            ChatMessageModel(R.drawable.ic_mia_pic,"Mia","Jul 20, 2023, 11:32 AM","Hi welcome to our house!"),
            ChatMessageModel(R.drawable.ic_mia_pic,"Mia","Jul 20, 2023, 11:32 AM","Hi welcome to our house!"),
            ChatMessageModel(R.drawable.ic_mia_pic,"Mia","Jul 20, 2023, 11:32 AM","Hi welcome to our house!"),
            ChatMessageModel(R.drawable.ic_mia_pic,"Mia","Jul 20, 2023, 11:32 AM","Hi welcome to our house!"),
            ChatMessageModel(R.drawable.ic_mia_pic,"Mia","Jul 20, 2023, 11:32 AM","Hi welcome to our house!"),
        )
        _list.value = listItem
    }


}