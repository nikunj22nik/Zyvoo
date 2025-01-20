package com.business.zyvo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.model.NotificationScreenModel
import com.business.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel  @Inject constructor(private val repository: ZyvoRepository):
    ViewModel(){

        private val _list  = MutableLiveData<ArrayList<NotificationScreenModel>>()
    val  list : LiveData<ArrayList<NotificationScreenModel>> get() = _list
    private val _title = MutableLiveData<String>()
    val title: LiveData<String> get() = _title

    init {
        _title.value = "Notifications"
        load()
    }


    private  fun load(){
       val listItem = arrayListOf<NotificationScreenModel>(
           NotificationScreenModel("You got a booking","Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,"),
           NotificationScreenModel("You got a booking","Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,"),
           NotificationScreenModel("You got a booking","Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,"),
           NotificationScreenModel("You got a booking","Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,"),
           NotificationScreenModel("You got a booking","Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,"),
           NotificationScreenModel("You got a booking","Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,")
       )
        _list.value = listItem
    }
    // Method to remove item at a specific position
    fun removeItemAt(position: Int) {
        _list.value?.removeAt(position)
        _list.value = _list.value // Trigger observer by setting the value again
    }

}