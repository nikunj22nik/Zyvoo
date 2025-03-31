package com.business.zyvo.fragment.host.payments.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.repository.ZyvoRepository
import com.business.zyvo.utils.NetworkMonitor
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class PaymentsViewModel @Inject constructor(private val repository: ZyvoRepository,
                                            val networkMonitor: NetworkMonitor
) : ViewModel(){

    val isLoading = MutableLiveData<Boolean>()

    suspend fun getPayoutMethods( userId: String):
            Flow<NetworkResult<JsonObject>> {
        return repository.getPayoutMethods(userId).onEach {
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


    suspend fun setPrimaryPayoutMethod( userId: String, payoutMethodId: String):
            Flow<NetworkResult<String>>{
        return repository.setPrimaryPayoutMethod(userId,payoutMethodId).onEach {
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

    suspend fun deletePayoutMethod( userId: String, payoutMethodId: String):
            Flow<NetworkResult<String>>{
        return repository.deletePayoutMethod(userId,payoutMethodId).onEach {
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


    suspend fun setPreferredCard( userId: String, card_id: String):
            Flow<NetworkResult<Pair<String,String>>>{
        return repository.setPreferredCard(userId,card_id).onEach {

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


    suspend fun paymentWithdrawalList(
        userId: String,
        startDate: String,
        endDate: String,
        filterStatus: String
    ): Flow<NetworkResult<JsonObject>>{
        return repository.paymentWithdrawalList(userId,startDate,endDate,filterStatus).onEach {
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

    suspend fun payoutBalance(
        userId: String,
    ): Flow<NetworkResult<Pair<String, String>>>{
        return repository.payoutBalance(userId).onEach {
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
    suspend fun withdrawFunds(
        userId: String,
    ): Flow<NetworkResult<Pair<String, String>>>{
        return repository.withdrawFunds(userId).onEach {
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


    suspend fun requestWithdrawal(
        userId : String,
        amount : String,
        withdrawalType : String,
    ): Flow<NetworkResult<JsonObject>>{
        return repository.requestWithdrawal(  userId,
            amount,
            withdrawalType).onEach {
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