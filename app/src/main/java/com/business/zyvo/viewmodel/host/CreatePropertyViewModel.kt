package com.business.zyvo.viewmodel.host

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.model.host.PropertyDetailsSave
import com.business.zyvo.repository.ZyvoRepository
import com.business.zyvo.utils.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class CreatePropertyViewModel @Inject constructor(
    private val repository: ZyvoRepository,
    val networkMonitor: NetworkMonitor
) : ViewModel() {

    val isLoading = MutableLiveData<Boolean>()

    var firstScreen: Boolean = true
    var secondScreen: Boolean = false
    var thirdScreen: Boolean = false
    var pageAfterPageWork :Boolean = false


    suspend fun addProperty(code: PropertyDetailsSave): Flow<NetworkResult<Pair<String, Int>>> {
        return repository.addPropertyData(code).onEach {
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