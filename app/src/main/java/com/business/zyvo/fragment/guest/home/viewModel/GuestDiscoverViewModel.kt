package com.business.zyvo.fragment.guest.home.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.model.LogModel
import com.business.zyvo.repository.ZyvoRepository
import com.business.zyvo.utils.NetworkMonitor
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class GuestDiscoverViewModel @Inject constructor(private val repository: ZyvoRepository,
                                                 val networkMonitor: NetworkMonitor): ViewModel(){
    val isLoading = MutableLiveData<Boolean>()

    suspend fun getHomeData(userId: String,latitude: String,longitude: String):
            Flow<NetworkResult<JsonArray>> {
        return repository.getHomeData(userId,
            latitude,
            longitude).onEach {
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


    suspend fun getWisList(userId: String):
            Flow<NetworkResult<JsonArray>> {
        return repository.getWisList(userId).onEach {
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


    suspend fun createWishlist( userId: String,
                                name: String,
                                description: String,
                                property_id: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.createWishlist(userId,
            name,description,property_id).onEach {
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


    suspend fun removeItemFromWishlist( userId: String,
                                        property_id: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.removeItemFromWishlist(userId,
            property_id).onEach {
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


    suspend fun saveItemInWishlist( userId: String,
                                        property_id: String,
                                    wishlist_id: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.saveItemInWishlist(userId,
            property_id,
            wishlist_id).onEach {
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

    suspend fun getFilterHomeDataApi( userId: Int?, latitude: Double?, longitude: Double?, place_type: String?, minimum_price: Double?, maximum_price: Double?,
                                      location: String?, date: String?, time: Int?, people_count: Int?, property_size: Int?, bedroom: Int?, bathroom: Int?, instant_booking: Int?,
                                      self_check_in: Int?, allows_pets: Int?, activities: List<String>?, amenities: List<String>?, languages: List<String>?): Flow<NetworkResult<JsonArray>> {
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