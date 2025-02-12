package com.business.zyvo.repository

import com.business.zyvo.NetworkResult

import com.business.zyvo.model.HostMyPlacesModel
import com.business.zyvo.model.host.GetPropertyDetail

import com.business.zyvo.fragment.both.completeProfile.model.CompleteProfileReq
import com.business.zyvo.fragment.both.faq.model.FaqModel
import com.business.zyvo.model.MyBookingsModel

import com.business.zyvo.model.host.PropertyDetailsSave
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
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


    // Host Api's
    suspend fun addPropertyData(property: PropertyDetailsSave) :Flow<NetworkResult<Pair<String, Int>>>


    suspend fun  getPropertyList(userId:Int,latitude: Double?, longitude: Double?) : Flow<NetworkResult<Pair<MutableList<HostMyPlacesModel>,String>>>

    suspend fun getPropertyDetails(propertyId :Int) : Flow<NetworkResult<GetPropertyDetail>>

    suspend fun deleteProperty(propertyId: Int) : Flow<NetworkResult<String>>

    suspend fun earning(hostId:Int,type:String) : Flow<NetworkResult<String>>

    suspend fun updateProperty(requestBody: PropertyDetailsSave) :Flow<NetworkResult<String>>


    suspend fun completeProfile(property: CompleteProfileReq) :Flow<NetworkResult<Pair<String, String>>>

    suspend fun phoneVerification(userId :String,code :String,number:String) : Flow<NetworkResult<Pair<String,String>>>

    suspend fun emailVerification(userId: String,email :String) : Flow<NetworkResult<Pair<String,String>>>


    suspend fun otpVerifyEmailVerification(userId :String,otp :String) : Flow<NetworkResult<Pair<String,String>>>

    suspend fun otpVerifyPhoneVerification(userId: String,otp :String) : Flow<NetworkResult<Pair<String,String>>>

    suspend fun uploadProfileImage(userId: String,bytes: ByteArray) : Flow<NetworkResult<Pair<String,String>>>

    suspend fun addUpdateName(userId: String,first_name: String, last_name: String) : Flow<NetworkResult<Pair<String,String>>>

    suspend fun addAboutMe(userId: String,about_me: String) : Flow<NetworkResult<Pair<String,String>>>



    suspend fun getHostBookingList(userid:Int) : Flow<NetworkResult<MutableList<MyBookingsModel>>>


    suspend fun addLivePlace(userId: String,place_name: String) : Flow<NetworkResult<Pair<String,String>>>

    suspend fun deleteLivePlace(userId: String,index: String) : Flow<NetworkResult<Pair<String,String>>>

    suspend fun approveDeclineBooking(bookingId :Int, status :String, message :String,reason :String) : Flow<NetworkResult<String>>




    suspend fun deleteLivePlace(userId: String,index: Int) : Flow<NetworkResult<Pair<String,String>>>

    suspend fun addMyWork(userId: String,workName: String) : Flow<NetworkResult<Pair<String,String>>>

    suspend fun getPrivacyPolicy() : Flow<NetworkResult<String>>

    suspend fun  getTermCondition() : Flow<NetworkResult<String>>

    suspend fun feedback(user_id : String,type: String,details: String) : Flow<NetworkResult<String>>

    suspend fun  getFaq() : Flow<NetworkResult<MutableList<FaqModel>>>
}


