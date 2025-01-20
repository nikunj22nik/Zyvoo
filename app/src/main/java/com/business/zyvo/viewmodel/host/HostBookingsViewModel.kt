package com.business.zyvo.viewmodel.host

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.R
import com.business.zyvo.model.MyBookingsModel
import com.business.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HostBookingsViewModel @Inject constructor(private var repository: ZyvoRepository): ViewModel() {

    var _list = MutableLiveData<MutableList<MyBookingsModel>>()

    val list : LiveData<MutableList<MyBookingsModel>> get() = _list

    init {
        load()
    }

    private fun load() {
        val listValue = mutableListOf<MyBookingsModel>(
            MyBookingsModel(R.drawable.ic_mia_pic,"Katelyn Francy", "Finished", "October 22, 2023"),
            MyBookingsModel(R.drawable.ic_img_girl_dumm,"Mike Jm.", "Confirmed", "October 22, 2023"),
            MyBookingsModel(R.drawable.ic_mia_pic,"Mia Williams", "Waiting payment", "October 22, 2023"),
            MyBookingsModel(R.drawable.ic_img_girl_dumm,"Cabin in Peshastin", "Canceled", "October 22, 2023"),
            MyBookingsModel(R.drawable.ic_img_girl_dumm,"Cabin in Peshastin", "Canceled", "October 22, 2023")

        )

        _list.value = listValue
    }
}