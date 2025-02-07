package com.business.zyvo.viewmodel.host

import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.model.HostMyPlacesModel
import com.business.zyvo.model.host.GetPropertyDetail
import com.business.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@HiltViewModel
class MyPlaceViewModel @Inject constructor(private var repository: ZyvoRepository): ViewModel() {

   suspend fun getMyPlaces(userId: Int, latitude: Double?, longitude: Double?) : Flow<NetworkResult<Pair<MutableList<HostMyPlacesModel>,String>>>{
        return repository.getPropertyList(userId,latitude,longitude).onEach {
        }
    }

    suspend fun deleteProperty(propertyId :Int) :Flow<NetworkResult<String>>{
        return repository.deleteProperty(propertyId).onEach {  }
    }

     suspend fun earning(hostId: Int, type: String): Flow<NetworkResult<String>> {
         return repository.earning(hostId, type).onEach {

         }
     }

}