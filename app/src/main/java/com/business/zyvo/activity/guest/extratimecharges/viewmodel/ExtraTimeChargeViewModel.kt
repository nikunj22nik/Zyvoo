package com.business.zyvo.activity.guest.extratimecharges.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.activity.guest.checkout.model.ReqAddOn
import com.business.zyvo.activity.guest.propertydetails.model.AddOn
import com.business.zyvo.model.AddPaymentCardModel
import com.business.zyvo.repository.ZyvoRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ExtraTimeChargeViewModel  @Inject constructor(private val repository: ZyvoRepository) : ViewModel(){
    val isLoading = MutableLiveData<Boolean>()



    suspend fun getUserCards( userId: String):
            Flow<NetworkResult<JsonObject>>{
        return repository.getUserCards(userId).onEach {
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

    suspend fun sameAsMailingAddress( userId: String):
            Flow<NetworkResult<JsonObject>>{
        return repository.sameAsMailingAddress(userId).onEach {
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

    suspend fun saveCardStripe( userId: String, token_stripe: String):
            Flow<NetworkResult<Pair<String,String>>>{
        return repository.saveCardStripe(userId,token_stripe).onEach {
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

}