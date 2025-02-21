package com.business.zyvo.activity.guest.checkout.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.model.AddPaymentCardModel
import com.business.zyvo.repository.ZyvoRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class CheckOutPayViewModel  @Inject constructor(private val repository: ZyvoRepository) : ViewModel(){
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


}