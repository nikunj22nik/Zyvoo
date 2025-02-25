package com.business.zyvo.fragment.host.placeOpen.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.repository.ZyvoRepository
import com.business.zyvo.utils.NetworkMonitor
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class PlaceOpenViewModel @Inject constructor(
    private val repository: ZyvoRepository,
    val networkMonitor: NetworkMonitor
) : ViewModel() {

    val isLoading = MutableLiveData<Boolean>()

    suspend fun propertyBookingDetails(
        property_id: String,
        user_id: String,
        start_date: String,
        end_date: String,
        latitude: String,
        longitude: String
    ):
            Flow<NetworkResult<JsonObject>> {
        return repository.propertyBookingDetails(
            property_id,
            user_id,
            start_date,
            end_date,
            latitude,
            longitude
        ).onEach {
            when (it) {
                is NetworkResult.Loading -> {
                    isLoading.value = true
                }

                is NetworkResult.Success -> {
                    isLoading.value = false
                }

                else -> {
                    isLoading.value = false
                }
            }
        }
    }

    suspend fun togglePropertyBooking(
        property_id: String,
        user_id: String
    ):
            Flow<NetworkResult<JsonObject>> {
        return repository.togglePropertyBooking(
            property_id,
            user_id
        ).onEach {
            when (it) {
                is NetworkResult.Loading -> {
                    isLoading.value = true
                }

                is NetworkResult.Success -> {
                    isLoading.value = false
                }

                else -> {
                    isLoading.value = false
                }
            }
        }
    }


}