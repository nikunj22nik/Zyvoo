package com.business.zyvo.activity.guest.extratimecharges.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.activity.guest.checkout.model.ReqAddOn
import com.business.zyvo.activity.guest.propertydetails.model.AddOn
import com.business.zyvo.model.AddPaymentCardModel
import com.business.zyvo.model.host.ChannelModel
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

    suspend fun getBookingExtensionTimeAmount( userId : String,
                              booking_id : String,
                              extension_time : String,
                              service_fee : String,
                              tax : String, cleaning_fee:String,
                              extension_total_amount : String,
                              extension_booking_amount : String,
                              discount_amount : String):  Flow<NetworkResult<JsonObject>>{
        return repository.getBookingExtensionTimeAmount(
            userId,
            booking_id,
            extension_time,
            service_fee,
            tax,
            cleaning_fee,
            extension_total_amount,
            extension_booking_amount,
            discount_amount
        ).onEach {
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

    suspend fun joinChatChannel(
        senderId :Int, receiverId :Int, groupChannel :String, user_type: String
    ) : Flow<NetworkResult<ChannelModel>>{
        return repository.joinChatChannel(senderId, receiverId, groupChannel, user_type).onEach {

        }
    }

}