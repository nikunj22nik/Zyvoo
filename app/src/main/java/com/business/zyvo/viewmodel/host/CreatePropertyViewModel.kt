package com.business.zyvo.viewmodel.host

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.model.host.GetPropertyDetail
import com.business.zyvo.model.host.PropertyDetailsSave
import com.business.zyvo.repository.ZyvoRepository
import com.business.zyvo.utils.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
    var numberSelectMap = mutableMapOf<Int,Int>()
    var propertyMap = mutableMapOf<Int,Int>()

    init {
        numberSelectMap.put(1,1)
        numberSelectMap.put(2,2)
        numberSelectMap.put(3,3)
        numberSelectMap.put(4,4)
        numberSelectMap.put(5,5)
        numberSelectMap.put(7,7)
        numberSelectMap.put(8,8)

        propertyMap.put(250,1)
        propertyMap.put(350,2)
        propertyMap.put(450,3)
        propertyMap.put(550,4)
        propertyMap.put(650,5)
        propertyMap.put(750,6)

    }

     suspend fun propertyImageDelete(imageId: Int) : Flow<NetworkResult<String>> {
        return repository.propertyImageDelete(imageId).onEach {

        }
     }


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


    suspend fun propertyDetail(propertyId: Int) : Flow<NetworkResult<GetPropertyDetail>>{
        return repository.getPropertyDetails(propertyId).onEach {
        }
    }

    suspend fun updateProperty(requestBody: PropertyDetailsSave) :Flow<NetworkResult<String>>{
        return repository.updateProperty(requestBody)
    }

}