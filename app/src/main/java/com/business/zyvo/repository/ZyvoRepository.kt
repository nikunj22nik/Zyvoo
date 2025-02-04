package com.business.zyvo.repository

import com.business.zyvo.NetworkResult
import com.business.zyvo.model.host.PropertyDetailsSave
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.Field

interface ZyvoRepository {


    suspend fun signUpPhoneNumber(phoneNumber :String,code:String) : Flow<NetworkResult<Pair<String, String>>>

    suspend fun loginPhoneNumber(phoneNumber :String,code:String) : Flow<NetworkResult<Pair<String,String>>>

    suspend fun otpVerifyLoginPhone(userId :String,otp:String) : Flow<NetworkResult<JsonObject>>

    suspend fun otpVerifySignupPhone(tempId :String,otp:String) : Flow<NetworkResult<JsonObject>>

    suspend fun loginEmail(email :String,password:String) : Flow<NetworkResult<JsonObject>>

    suspend fun signupEmail(email :String,password:String) : Flow<NetworkResult<Pair<String,String>>>

    suspend fun otpVerifySignupEmail(temp_id :String,otp:String) : Flow<NetworkResult<JsonObject>>

    suspend fun forgotPassword(email :String) : Flow<NetworkResult<Pair<String, String>>>

    suspend fun otpVerifyForgotPassword(userId :String,otp:String) : Flow<NetworkResult<JsonObject>>

    suspend fun resetPassword(userId :String,password:String,
                              passwordConfirmation:String) : Flow<NetworkResult<JsonObject>>

    suspend fun getUserProfile(userId :String) : Flow<NetworkResult<JsonObject>>

    suspend fun addPropertyData(property: PropertyDetailsSave) :Flow<NetworkResult<Pair<String, Int>>>
}