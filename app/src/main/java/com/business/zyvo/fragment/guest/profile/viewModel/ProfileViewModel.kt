package com.business.zyvo.fragment.guest.profile.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.business.zyvo.NetworkResult
import com.business.zyvo.model.AddPaymentCardModel
import com.business.zyvo.repository.ZyvoRepository
import com.business.zyvo.utils.NetworkMonitor
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel  @Inject constructor(private val repository: ZyvoRepository,
                                            val networkMonitor: NetworkMonitor
) : ViewModel(){

    val isLoading = MutableLiveData<Boolean>()

    private val _paymentCardList = MutableLiveData<MutableList<AddPaymentCardModel>>()
    val paymentCardList : LiveData<MutableList<AddPaymentCardModel>> get() =  _paymentCardList


    init {
        loadPaymentDetail()
    }



    private fun loadPaymentDetail(){
        val paymentList = mutableListOf<AddPaymentCardModel>(
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888")
        )

        _paymentCardList.value = paymentList

    }

    suspend fun uploadProfileImage(userId: String,bytes: ByteArray):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.uploadProfileImage(userId,bytes).onEach {
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

    suspend fun getUserProfile(userId: String):
            Flow<NetworkResult<JsonObject>> {
        return repository.getUserProfile(userId).onEach {
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

    suspend fun addUpdateName(userId: String,first_name: String,
                              last_name: String):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.addUpdateName(userId,first_name,
            last_name).onEach {
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


    suspend fun addAboutMe(userId: String,about_me: String):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.addAboutMe(userId,
            about_me).onEach {
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

    suspend fun addLivePlace(userId: String,place_name: String):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.addLivePlace(userId,
            place_name).onEach {
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

    suspend fun deleteLivePlace(userId: String,index: String):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.deleteLivePlace(userId,
            index).onEach {
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