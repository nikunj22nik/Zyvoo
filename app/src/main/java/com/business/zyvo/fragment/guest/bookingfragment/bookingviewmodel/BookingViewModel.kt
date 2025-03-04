package com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.BookingDetailModel
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.BookingModel
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.ReviewModel
import com.business.zyvo.repository.ZyvoRepository
import com.business.zyvo.utils.NetworkMonitor
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(private val repository: ZyvoRepository, val networkMonitor: NetworkMonitor) : ViewModel() {

    val isLoading = MutableLiveData<Boolean>()

    suspend fun getBookingList(userid: String) : Flow<NetworkResult<MutableList<BookingModel>>>  {
        return repository.getBookingList(userid).onEach {
            when (it) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> { }
                else -> { }
            }
        }
    }

    suspend fun getBookingDetailsList(userid: String,booking_id:Int) : Flow<NetworkResult<BookingDetailModel>>  {
        return repository.getBookingDetailsList(userid,booking_id).onEach {
            when (it) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> { }
                else -> { }
            }
        }
    }

    suspend fun getReviewPublishAPI(userId: Int, booking_id: Int, property_id: Int, response_rate: Int, communication: Int, on_time: Int, review_message: String):
            Flow<NetworkResult<ReviewModel>> {
        return repository.reviewPublish(userId,booking_id,property_id,response_rate,communication,on_time,review_message).onEach {
            when(it){
                is NetworkResult.Loading -> {
                    isLoading.value = true
                } is NetworkResult.Success -> {
                isLoading.value = false
            } else -> {
                isLoading.value = false
            }
            }
        }
    }

}