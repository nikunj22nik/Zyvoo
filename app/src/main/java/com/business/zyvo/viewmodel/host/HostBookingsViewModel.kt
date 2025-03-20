package com.business.zyvo.viewmodel.host

import android.net.http.HttpException
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.business.zyvo.AppConstant
import com.business.zyvo.LoadingUtils
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.model.MyBookingsModel
import com.business.zyvo.model.host.ChannelModel
import com.business.zyvo.model.host.HostReviewModel
import com.business.zyvo.model.host.PaginationModel
import com.business.zyvo.model.host.ReviewerProfileModel
import com.business.zyvo.model.host.hostdetail.HostDetailModel
import com.business.zyvo.model.host.hostdetail.Review
import com.business.zyvo.repository.ZyvoRepository
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.onEach
import retrofit2.http.Field
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class HostBookingsViewModel @Inject constructor(private var repository: ZyvoRepository) :
    ViewModel() {

    var _list = MutableLiveData<MutableList<MyBookingsModel>>()
    var reviewlist :MutableList<Pair<Int,String>> = mutableListOf()
    var currentPage =1
    var totalPage = Integer.MAX_VALUE
    var reviewerProfileList = mutableListOf<ReviewerProfileModel>()
    var hashMapPageNumber = HashMap<Int,Boolean>()
    var filter = "recent_review"
  //  pending,waiting_payment,confirmed,cancelled,finished
   var reviewListLiveData = MutableLiveData<MutableList<Pair<Int,String>>>()
    var pendingList = mutableListOf<MyBookingsModel>()
    var waitingPaymentList = mutableListOf<MyBookingsModel>()
    var confirmedList = mutableListOf<MyBookingsModel>()
    var cancelledList = mutableListOf<MyBookingsModel>()
    var finishedList = mutableListOf<MyBookingsModel>()
    var finalList = mutableListOf<MyBookingsModel>()
    var highestReviewList = mutableListOf<ReviewerProfileModel>()
    var lowestReviewList = mutableListOf<ReviewerProfileModel>()
    var orgReviewList = mutableListOf<ReviewerProfileModel>()


    val list: LiveData<MutableList<MyBookingsModel>> get() = _list


    suspend fun filterPropertyReviewsHost(
        propertyId :Int, filter :String, page :Int
    ) : Flow<NetworkResult<Pair<JsonArray, JsonObject>>> {
        return  repository.filterPropertyReviewsHost(propertyId, filter, page).onEach {
            when(it){
                is NetworkResult.Success ->{
                }
                is NetworkResult.Error ->{

                }
                else ->{

                }
            }

        }

    }


    suspend fun load(userid: Int): Flow<NetworkResult<MutableList<MyBookingsModel>>> {
        return repository.getHostBookingList(userid).onEach {
            when (it) {
                is NetworkResult.Success -> {
                    var data = it.data
                    pendingList.clear()
                    pendingList.clear()
                    waitingPaymentList.clear()
                    confirmedList.clear()
                    cancelledList.clear()
                    finishedList.clear()
                    finalList.clear()
                    it.data?.let {
                        finalList = it
                        _list.value = it
                    }
                    data?.forEach {
                        when(it.booking_status) {

                           AppConstant.PENDING ->{
                             pendingList.add(it)
                           }
                            AppConstant.WAITING_PAYMENT ->{
                                waitingPaymentList.add(it)
                            }
                            AppConstant.CONFIRMED ->{
                                confirmedList.add(it)
                            }
                            AppConstant.CANCEL ->{
                                cancelledList.add(it)
                            }
                            AppConstant.FINISHED ->{
                                finishedList.add(it)
                            }


                        }
                    }
                }
                is NetworkResult.Error -> {}
                else -> {}
            }
        }
    }

    suspend fun approveDeclineBooking(
        bookingId: Int, status: String, message: String,
        reason: String
    ): Flow<NetworkResult<String>> {
        return repository.approveDeclineBooking(bookingId, status, message, reason).onEach { }
    }

    suspend fun hostBookingDetails(
        bookingId: Int,
        latitude: String?,
        longitude: String?
    ): Flow<NetworkResult<Pair<String, HostDetailModel>>> {
        return repository.hostBookingDetails(bookingId, latitude, longitude).onEach {

        }
    }

    suspend fun propertyFilterReviews(
        propertyId: Int, filter: String, page: Int
    ): Flow<NetworkResult<Pair<PaginationModel, MutableList<HostReviewModel>>>> {


        return repository.propertyFilterReviews(propertyId, filter, page).onEach {

        }
    }

    suspend fun reportListReason() : Flow<NetworkResult<MutableList<Pair<Int,String>>>>{
        return repository.reportListReason().onEach {
            when(it){
                is NetworkResult.Success ->{
                     it.data?.let {
                         Log.d("TESTING","ReviewList Inside ViewModel "+it.size)
                         reviewlist= it
                         reviewListLiveData.value = reviewlist
                     }

                }
                is NetworkResult.Error ->{

                }
                else ->{

                }
            }
        }
    }


    suspend fun hostReportViolationSend(
        userId: Int,
        bookingId: Int,
        propertyId: Int,
        reportReasonId: Int,
        additionalDetail: String
    ): Flow<NetworkResult<String>>{
        return repository.hostReportViolationSend(userId, bookingId, propertyId, reportReasonId, additionalDetail).onEach {

        }
    }


    suspend fun reviewGuest(
        userId: Int, bookingId: Int, propertyId: Int, responseRate: Int, communication: Int,
        onTime: Int, reviewMessage: String
    ) : Flow<NetworkResult<String>>{
        return repository.reviewGuest(userId, bookingId, propertyId, responseRate, communication, onTime, reviewMessage).onEach {

        }
    }

    suspend fun joinChatChannel(
        senderId :Int, receiverId :Int, groupChannel :String, user_type: String
    ) : Flow<NetworkResult<ChannelModel>>{
        return repository.joinChatChannel(senderId, receiverId, groupChannel, user_type).onEach {

        }
    }
    suspend fun markHostBooking( userId :Int) : Flow<NetworkResult<String>>{
        return repository.markHostBooking(userId).onEach {

        }
    }

    fun sortedByDescending (list :MutableList<ReviewerProfileModel>){

        val sortedReviews = list.sortedWith { a, b ->
            val ratingA = a.review_rating?.toDoubleOrNull() ?: Double.MIN_VALUE // Treat null/invalid as a small value
            val ratingB = b.review_rating?.toDoubleOrNull() ?: Double.MIN_VALUE
            ratingB.compareTo(ratingA) // Compare decimal ratings (reverse the order for high to low)
        }

        highestReviewList= sortedReviews.toMutableList()

        highestReviewList.forEach {
            Log.d("TESTING_REVIEW","Review Rating Highest"+it.review_rating.toString())
        }

    }

    fun sortByAscendingOrder(list :MutableList<ReviewerProfileModel>){

        val sortedReviews1 = list.sortedWith { a, b ->
            val ratingA = a.review_rating?.toDoubleOrNull() ?: Double.MAX_VALUE // Treat null/invalid as a large value
            val ratingB = b.review_rating?.toDoubleOrNull() ?: Double.MAX_VALUE
            ratingA.compareTo(ratingB) // Compa

        }

        lowestReviewList = sortedReviews1.toMutableList()

        lowestReviewList.forEach {
            Log.d("TESTING_REVIEW","Review Rating Lowest"+it.review_rating.toString())
        }

    }


}