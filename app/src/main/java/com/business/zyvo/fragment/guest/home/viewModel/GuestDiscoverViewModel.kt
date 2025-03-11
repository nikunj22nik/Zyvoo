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

    suspend fun getFilterHomeDataApi( userId: String?, latitude: String?, longitude: String?, place_type: String?, minimum_price: String?, maximum_price: String?,
                                      location: String?, date: String?, time: String?, people_count: String?, property_size: String?, bedroom: String?, bathroom: String?, instant_booking: String?,
                                      self_check_in: String?, allows_pets: String?, activities: List<String>?, amenities: List<String>?, languages: List<String>?): Flow<NetworkResult<JsonArray>> {
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


    suspend fun getHomeDataSearchFilter(
        user_id : String,
        latitude : String,
        longitude : String,
        date : String,
        hour : String,
        start_time : String,
        end_time : String,
        activity : String):
            Flow<NetworkResult<JsonArray>> {
        return repository.getHomeDataSearchFilter(user_id, latitude, longitude,date,
            hour,start_time, end_time, activity).onEach {
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



    suspend fun getUserBookings(
        user_id : String,
        booking_date : String,
        booking_start : String):
            Flow<NetworkResult<JsonObject>> {
        return repository.getUserBookings(user_id,booking_date, booking_start).onEach {
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