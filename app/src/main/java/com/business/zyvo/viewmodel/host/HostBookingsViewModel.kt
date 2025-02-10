package com.business.zyvo.viewmodel.host

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.model.MyBookingsModel
import com.business.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HostBookingsViewModel @Inject constructor(private var repository: ZyvoRepository) :
    ViewModel() {

    var _list = MutableLiveData<MutableList<MyBookingsModel>>()

    val list: LiveData<MutableList<MyBookingsModel>> get() = _list


    suspend fun load(userid: Int) : Flow<NetworkResult<MutableList<MyBookingsModel>>> = flow {
        repository.getHostBookingList(userid).onEach {
            when (it) {
                is NetworkResult.Success -> {

                }
                is NetworkResult.Error -> {

                }
                else -> {

                }
            }
        }
    }

}