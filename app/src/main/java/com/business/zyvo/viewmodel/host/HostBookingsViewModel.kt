package com.business.zyvo.viewmodel.host

import android.net.http.HttpException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.model.MyBookingsModel
import com.business.zyvo.model.host.HostReviewModel
import com.business.zyvo.model.host.PaginationModel
import com.business.zyvo.model.host.hostdetail.HostDetailModel
import com.business.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.onEach
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class HostBookingsViewModel @Inject constructor(private var repository: ZyvoRepository) :
    ViewModel() {

    var _list = MutableLiveData<MutableList<MyBookingsModel>>()

    var currentPage =1
    var filter = "recent_review"

    val list: LiveData<MutableList<MyBookingsModel>> get() = _list


    suspend fun load(userid: Int): Flow<NetworkResult<MutableList<MyBookingsModel>>> {
        return repository.getHostBookingList(userid).onEach {
            when (it) {
                is NetworkResult.Success -> {}
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

}