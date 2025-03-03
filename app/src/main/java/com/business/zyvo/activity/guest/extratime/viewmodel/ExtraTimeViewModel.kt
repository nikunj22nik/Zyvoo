package com.business.zyvo.activity.guest.extratime.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.activity.guest.checkout.model.ReqAddOn
import com.business.zyvo.activity.guest.propertydetails.model.AddOn
import com.business.zyvo.model.AddPaymentCardModel
import com.business.zyvo.repository.ZyvoRepository
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ExtraTimeViewModel  @Inject constructor(private val repository: ZyvoRepository) : ViewModel(){
    val isLoading = MutableLiveData<Boolean>()
    private val _paymentCardList = MutableLiveData<MutableList<AddPaymentCardModel>>()
    val paymentCardList : LiveData<MutableList<AddPaymentCardModel>> get() =  _paymentCardList



    suspend fun listReportReasons( ):
            Flow<NetworkResult<JsonArray>>{
        return repository.listReportReasons().onEach {
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

    suspend fun reportViolation( userId : String,
                                 booking_id : String,
                                 property_id : String,
                                 report_reasons_id : String,
                                 additional_details:String):
            Flow<NetworkResult<Pair<String, String>>>{
        return repository.reportViolation( userId,
            booking_id,
            property_id,
            report_reasons_id,
            additional_details).onEach {
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

    suspend fun cancelBooking( userId : String,
                                 booking_id : String):
            Flow<NetworkResult<Pair<String, String>>>{
        return repository.cancelBooking( userId,
            booking_id,).onEach {
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