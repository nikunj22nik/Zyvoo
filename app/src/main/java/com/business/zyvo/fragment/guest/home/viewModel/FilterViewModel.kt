package com.business.zyvo.fragment.guest.home.viewModel

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
class FilterViewModel @Inject constructor(private val repository: ZyvoRepository, val networkMonitor: NetworkMonitor) : ViewModel() {

    val isLoading = MutableLiveData<Boolean>()

    suspend fun getFilterHomeDataApi( userId: Int?,
                                      latitude: Double?,
                                      longitude: Double?,
                                      place_type: String?,
                                      minimum_price: Double?,
                                      maximum_price: Double?,
                                      location: String?,
                                      date: String?,
                                      time: Int?,
                                      people_count: Int?,
                                      property_size: Int?,
                                      bedroom: Int?,
                                      bathroom: Int?,
                                      instant_booking: Int?,
                                      self_check_in: Int?,
                                      allows_pets: Int?,
                                      activities: List<String>?,
                                      amenities: List<String>?,
                                      languages: List<String>?): Flow<NetworkResult<JsonObject>> {
        return repository.getFilteredHomeData(userId,latitude,longitude,place_type,minimum_price,maximum_price,
            location,date,time,people_count,property_size,bedroom,bathroom,instant_booking,self_check_in,allows_pets,activities,amenities,languages).onEach {
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