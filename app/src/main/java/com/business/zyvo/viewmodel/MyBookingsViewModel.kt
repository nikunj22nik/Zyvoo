package com.business.zyvo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.R
import com.business.zyvo.model.MyBookingsModel
import com.business.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MyBookingsViewModel @Inject constructor(private var repository: ZyvoRepository): ViewModel() {

    var _list = MutableLiveData<MutableList<MyBookingsModel>>()

    val list : LiveData<MutableList<MyBookingsModel>> get() = _list


}