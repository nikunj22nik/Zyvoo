package com.business.zyvo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.fragment.both.notificationfragment.NotificationRootModel
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.BookingModel
import com.business.zyvo.model.NotificationScreenModel
import com.business.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel  @Inject constructor(private val repository: ZyvoRepository):
    ViewModel() {

    private val _list = MutableLiveData<ArrayList<NotificationScreenModel>>()
    val list: LiveData<ArrayList<NotificationScreenModel>> get() = _list
    private val _title = MutableLiveData<String>()
    val title: LiveData<String> get() = _title

    init {
        _title.value = "Notifications"
        load()
    }

    val isLoading = MutableLiveData<Boolean>()

    suspend fun getGuestNotification(userid: String) : Flow<NetworkResult<MutableList<NotificationRootModel>>>  {
        return repository.getGuestNotification(userid).onEach {
            when (it) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> { }
                else -> { }
            }
        }
    }

    suspend fun getMarkGuestNotification(userid: String,notification_id : Int) : Flow<NetworkResult<NotificationRootModel>>  {
        return repository.getMarkGuestNotification(userid,notification_id).onEach {
            when (it) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> { }
                else -> { }
            }
        }
    }

    suspend fun getRemoveGuestNotification(userid: String,notification_id : Int) : Flow<NetworkResult<NotificationRootModel>>  {
        return repository.getMarkGuestNotification(userid,notification_id).onEach {
            when (it) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> { }
                else -> { }
            }
        }
    }


    private fun load() {
        val listItem = arrayListOf<NotificationScreenModel>(
            NotificationScreenModel(
                "You got a booking",
                "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,"
            ),
            NotificationScreenModel(
                "You got a booking",
                "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,"
            ),
            NotificationScreenModel(
                "You got a booking",
                "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,"
            ),
            NotificationScreenModel(
                "You got a booking",
                "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,"
            ),
            NotificationScreenModel(
                "You got a booking",
                "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,"
            ),
            NotificationScreenModel(
                "You got a booking",
                "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,"
            )
        )
        _list.value = listItem
    }


    suspend fun getNotificationHost(userId: Int): Flow<NetworkResult<MutableList<NotificationScreenModel>>> {
        return repository.getNotificationHost(userId).onEach {
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

    suspend fun deleteNotificationHost(userId: Int, notificationId: Int)
    : Flow<NetworkResult<String>> {
        return repository.deleteNotificationHost(userId, notificationId).onEach {
            when (it) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> { }
                else -> { }
            }
        }
    }

}

