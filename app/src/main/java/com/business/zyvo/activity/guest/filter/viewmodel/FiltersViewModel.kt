package com.business.zyvo.activity.guest.filter.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.repository.ZyvoRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
@HiltViewModel
class FiltersViewModel @Inject constructor(private var repository: ZyvoRepository): ViewModel(){
    val isLoading = MutableLiveData<Boolean>()

    suspend fun getPropertyPriceRange():
            Flow<NetworkResult<JsonObject>> {
        return repository.getPropertyPriceRange().onEach {
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