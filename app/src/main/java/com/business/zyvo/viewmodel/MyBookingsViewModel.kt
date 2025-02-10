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

    init {
        load()
    }

    private fun load() {
//       val listValue = mutableListOf<MyBookingsModel>(
//           MyBookingsModel(R.drawable.image_hotel,"Cabin in Peshastin", "Finished", "October 22, 2023"),
//           MyBookingsModel(R.drawable.image_hotel,"Cabin in Peshastin", "Confirmed", "October 22, 2023"),
//           MyBookingsModel(R.drawable.image_hotel,"Cabin in Peshastin", "Waiting payment", "October 22, 2023"),
//           MyBookingsModel(R.drawable.image_hotel,"Cabin in Peshastin", "Canceled", "October 22, 2023"),
//       )

      //  _list.value = listValue
    }
}