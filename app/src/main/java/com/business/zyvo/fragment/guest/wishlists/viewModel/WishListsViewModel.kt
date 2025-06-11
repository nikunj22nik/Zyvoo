package com.business.zyvo.fragment.guest.wishlists.viewModel

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
class WishListsViewModel @Inject constructor(
    private val repository: ZyvoRepository,
    val networkMonitor: NetworkMonitor
) : ViewModel() {

    val isLoading = MutableLiveData<Boolean>()

    suspend fun getWisList(userId: String):
            Flow<NetworkResult<JsonArray>> {
        return repository.getWisList(userId).onEach {
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

    suspend fun deleteWishlist(
        userId: String,
        wishlist_id: String
    ):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.deleteWishlist(
            userId,
            wishlist_id
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


    suspend fun getSavedItemWishList(
        userId: Int, wishListId: Int,latitude : String,longitude : String
    ): Flow<NetworkResult<JsonObject>> {
        return repository.getSavedItemWishList(userId, wishListId,latitude,longitude).onEach {
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

     suspend fun removeItemFromWishlist(
        userId: String,
        property_id: String
    ): Flow<NetworkResult<Pair<String, String>>>{
        return repository.removeItemFromWishlist(userId,property_id).onEach {
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