package com.business.zyvo.activity.guest.propertydetails.viewmode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.model.WishListModel
import com.business.zyvo.repository.ZyvoRepository
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@HiltViewModel
class PropertyDetailsViewModel @Inject constructor(private var repository: ZyvoRepository): ViewModel(){
    val isLoading = MutableLiveData<Boolean>()
    private var _list = MutableLiveData<MutableList<WishListModel>>()
    val list : LiveData<MutableList<WishListModel>> get() = _list


    suspend fun getHomePropertyDetails(userId: String,propertyId :String):
            Flow<NetworkResult<Pair<JsonObject, JsonObject>>>{
        return repository.getHomePropertyDetails(userId,
            propertyId).onEach {
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

    suspend fun filterPropertyReviews(propertyId :String, filter :String, page :String): Flow<NetworkResult<Pair<JsonArray, JsonObject>>> {
        return repository.filterPropertyReviews(propertyId, filter, page).onEach {
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

}