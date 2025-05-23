package com.business.zyvo.fragment.guest.hostDetails.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.repository.ZyvoRepository
import com.business.zyvo.utils.NetworkMonitor
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject



    @HiltViewModel
    class HostDetailsViewModel @Inject constructor(private val repository: ZyvoRepository,
                                                val networkMonitor: NetworkMonitor
    ) : ViewModel(){

        val isLoading = MutableLiveData<Boolean>()


        suspend fun hostListing(
            hostId :String,
            latitude :String,
            longitude :String):
                Flow<NetworkResult<Pair<JsonObject, JsonObject>>> {
            return repository.hostListing(hostId,latitude,longitude).onEach {
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

        suspend fun filterHostReviews(propertyId :String, latitude :String,
                                      longitude :String, filter :String, page :String):
                Flow<NetworkResult<Pair<JsonArray, JsonObject>>> {
            return repository.filterHostReviews(propertyId,latitude,longitude, filter, page).onEach {
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