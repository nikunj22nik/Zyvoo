package com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.BookingDetailModel
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.BookingModel
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.ReviewModel
import com.business.zyvo.model.host.ChannelModel
import com.business.zyvo.repository.ZyvoRepository
import com.business.zyvo.utils.NetworkMonitor
import com.google.gson.JsonArray
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

    suspend fun joinChatChannel(
        senderId :Int, receiverId :Int, groupChannel :String, user_type: String
    ) : Flow<NetworkResult<ChannelModel>>{
        return repository.joinChatChannel(senderId, receiverId, groupChannel, user_type).onEach {

        }
    }

    suspend fun getBookingDetailsList(userid: String,booking_id:Int,
        latitude:String,longitude:String) : Flow<NetworkResult<Pair<BookingDetailModel,JsonObject>>>  {
        return repository.getBookingDetailsList(userid,booking_id,latitude,longitude).onEach {
            when (it) {
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

    suspend fun getReviewPublishAPI(userId: String, booking_id: String, property_id: String,
                                    response_rate: String, communication: String,
                                    on_time: String, review_message: String):
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

    suspend fun filterPropertyReviews(propertyId :String,
                                      filter :String,
                                      page :String):
            Flow<NetworkResult<Pair<JsonArray, JsonObject>>> {
        return repository.filterPropertyReviews(propertyId,
            filter,page).onEach {
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

    suspend fun cancelBooking( userId : String,
                               booking_id : String):
            Flow<NetworkResult<Pair<String, String>>>{
        return repository.cancelBooking( userId,
            booking_id,).onEach {
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