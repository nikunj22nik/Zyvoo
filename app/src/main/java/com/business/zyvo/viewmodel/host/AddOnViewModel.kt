package com.business.zyvo.viewmodel.host

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.model.host.AddOnModel
import com.business.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddOnViewModel @Inject constructor(var repository: ZyvoRepository): ViewModel() {

    private  val _list = MutableLiveData<MutableList<AddOnModel>>()
    val list : LiveData<MutableList<AddOnModel>> get() = _list

    init {
        load()
    }

    private fun load() {
        val listItem = mutableListOf<AddOnModel>(

        )
        _list.value = listItem
    }



}