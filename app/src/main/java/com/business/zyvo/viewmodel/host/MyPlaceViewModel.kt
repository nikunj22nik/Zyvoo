package com.business.zyvo.viewmodel.host

import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.model.HostMyPlacesModel
import com.business.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@HiltViewModel
class MyPlaceViewModel @Inject constructor(private var repository: ZyvoRepository): ViewModel() {

   suspend fun getMyPlaces(userId: Int, latitude: Double?, longitude: Double?) : Flow<NetworkResult<MutableList<HostMyPlacesModel>>>{
        return repository.getPropertyList(userId,latitude,longitude).onEach {
        }
    }

}