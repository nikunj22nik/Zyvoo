package com.business.zyvo.repository

import com.business.zyvo.NetworkResult
import com.business.zyvo.activity.guest.checkout.model.ReqAddOn
import com.business.zyvo.activity.guest.propertydetails.model.AddOn

import com.business.zyvo.model.HostMyPlacesModel
import com.business.zyvo.model.host.GetPropertyDetail

import com.business.zyvo.fragment.both.completeProfile.model.CompleteProfileReq
import com.business.zyvo.fragment.both.faq.model.FaqModel
import com.business.zyvo.model.MyBookingsModel
import com.business.zyvo.model.NotificationScreenModel
import com.business.zyvo.model.host.HostReviewModel
import com.business.zyvo.model.host.PaginationModel

import com.business.zyvo.model.host.PropertyDetailsSave

import com.business.zyvo.model.host.hostdetail.HostDetailModel

import com.google.gson.JsonArray

import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.Field

interface ZyvoRepository {


    suspend fun signUpPhoneNumber(
        phoneNumber: String,
        code: String
    ): Flow<NetworkResult<Pair<String, String>>>

    suspend fun loginPhoneNumber(
        phoneNumber: String,
        code: String
    ): Flow<NetworkResult<Pair<String, String>>>

    suspend fun otpVerifyLoginPhone(userId: String, otp: String): Flow<NetworkResult<JsonObject>>

    suspend fun otpVerifySignupPhone(tempId: String, otp: String): Flow<NetworkResult<JsonObject>>

    suspend fun loginEmail(email: String, password: String): Flow<NetworkResult<JsonObject>>

    suspend fun signupEmail(
        email: String,
        password: String
    ): Flow<NetworkResult<Pair<String, String>>>

    suspend fun otpVerifySignupEmail(temp_id: String, otp: String): Flow<NetworkResult<JsonObject>>

    suspend fun forgotPassword(email: String): Flow<NetworkResult<Pair<String, String>>>

    suspend fun otpVerifyForgotPassword(
        userId: String,
        otp: String
    ): Flow<NetworkResult<JsonObject>>

    suspend fun resetPassword(
        userId: String, password: String,
        passwordConfirmation: String
    ): Flow<NetworkResult<JsonObject>>

    suspend fun getUserProfile(userId: String): Flow<NetworkResult<JsonObject>>


    // Host Api's
    suspend fun addPropertyData(property: PropertyDetailsSave): Flow<NetworkResult<Pair<String, Int>>>


    suspend fun getPropertyList(
        userId: Int,
        latitude: Double?,
        longitude: Double?
    ): Flow<NetworkResult<Pair<MutableList<HostMyPlacesModel>, String>>>

    suspend fun getPropertyDetails(propertyId: Int): Flow<NetworkResult<GetPropertyDetail>>

    suspend fun deleteProperty(propertyId: Int): Flow<NetworkResult<String>>

    suspend fun earning(hostId: Int, type: String): Flow<NetworkResult<String>>

    suspend fun updateProperty(requestBody: PropertyDetailsSave): Flow<NetworkResult<String>>


    suspend fun completeProfile(property: CompleteProfileReq): Flow<NetworkResult<Pair<String, String>>>

    suspend fun phoneVerification(
        userId: String,
        code: String,
        number: String
    ): Flow<NetworkResult<Pair<String, String>>>

    suspend fun emailVerification(
        userId: String,
        email: String
    ): Flow<NetworkResult<Pair<String, String>>>


    suspend fun otpVerifyEmailVerification(
        userId: String,
        otp: String
    ): Flow<NetworkResult<Pair<String, String>>>

    suspend fun otpVerifyPhoneVerification(
        userId: String,
        otp: String
    ): Flow<NetworkResult<Pair<String, String>>>

    suspend fun uploadProfileImage(
        userId: String,
        bytes: ByteArray
    ): Flow<NetworkResult<Pair<String, String>>>

    suspend fun addUpdateName(
        userId: String,
        first_name: String,
        last_name: String
    ): Flow<NetworkResult<Pair<String, String>>>

    suspend fun addAboutMe(
        userId: String,
        about_me: String
    ): Flow<NetworkResult<Pair<String, String>>>






    suspend fun deleteLivePlace(
        userId: String,
        index: String
    ): Flow<NetworkResult<Pair<String, String>>>

    suspend fun approveDeclineBooking(
        bookingId: Int,
        status: String,
        message: String,
        reason: String
    ): Flow<NetworkResult<String>>

    suspend fun deleteLivePlace(
        userId: String,
        index: Int
    ): Flow<NetworkResult<Pair<String, String>>>

    suspend fun addMyWork(
        userId: String,
        workName: String
    ): Flow<NetworkResult<Pair<String, String>>>




    suspend fun addLanguage(
        userId: String,
        language_name: String
    ): Flow<NetworkResult<Pair<String, String>>>

    suspend fun deleteLanguage(
        userId: String,
        index: Int
    ): Flow<NetworkResult<Pair<String, String>>>

    suspend fun addHobbies(
        userId: String,
        hobbies_name: String
    ): Flow<NetworkResult<Pair<String, String>>>

    suspend fun deleteHobbies(userId: String, index: Int): Flow<NetworkResult<Pair<String, String>>>

    suspend fun addPets(userId: String, pet_name: String): Flow<NetworkResult<Pair<String, String>>>

    suspend fun deletePets(userId: String, index: Int): Flow<NetworkResult<Pair<String, String>>>

    suspend fun addStreetAddress(
        userId: String,
        street_address: String
    ): Flow<NetworkResult<Pair<String, String>>>

    suspend fun addCity(userId: String, city: String): Flow<NetworkResult<Pair<String, String>>>

    suspend fun addState(userId: String, state: String): Flow<NetworkResult<Pair<String, String>>>

    suspend fun addZipCode(
        userId: String,
        zip_code: String
    ): Flow<NetworkResult<Pair<String, String>>>

    suspend fun updatePassword(
        userId: String,
        password: String,
        password_confirmation: String
    ): Flow<NetworkResult<Pair<String, String>>>


    suspend fun getPrivacyPolicy(): Flow<NetworkResult<String>>


    suspend fun getTermCondition(): Flow<NetworkResult<String>>

    suspend fun feedback(
        user_id: String,
        type: String,
        details: String
    ): Flow<NetworkResult<String>>

    suspend fun getFaq(): Flow<NetworkResult<MutableList<FaqModel>>>




    suspend fun getHostBookingList(userid:Int) : Flow<NetworkResult<MutableList<MyBookingsModel>>>


    suspend fun addLivePlace(userId: String,place_name: String) : Flow<NetworkResult<Pair<String,String>>>



//    suspend fun deleteLivePlace(userId: String,index: Int) : Flow<NetworkResult<Pair<String,String>>>



    suspend fun deleteMyWork(userId: String,work_index: Int) : Flow<NetworkResult<Pair<String,String>>>




//    suspend fun addHobbies(userId: String,hobbies_name: String) : Flow<NetworkResult<Pair<String,String>>>
//
//    suspend fun deleteHobbies(userId: String,index: Int) : Flow<NetworkResult<Pair<String,String>>>
//
//    suspend fun addPets(userId: String,pet_name: String) : Flow<NetworkResult<Pair<String,String>>>
//
//    suspend fun deletePets(userId: String,index: Int) : Flow<NetworkResult<Pair<String,String>>>
//
//    suspend fun addStreetAddress(userId: String,street_address: String) : Flow<NetworkResult<Pair<String,String>>>
//
//    suspend fun addCity(userId: String,city: String) : Flow<NetworkResult<Pair<String,String>>>
//
//    suspend fun addState(userId: String,state: String) : Flow<NetworkResult<Pair<String,String>>>
//
//    suspend fun addZipCode(userId: String,zip_code: String) : Flow<NetworkResult<Pair<String,String>>>
//
//    suspend fun updatePassword(userId: String,password: String,password_confirmation: String) : Flow<NetworkResult<Pair<String,String>>>

    suspend fun getPaymentMethods(userId: String) : Flow<NetworkResult<Pair<String,String>>>

    suspend fun getFiltereHomeData(userId: String,latitude: String,longitude: String,place_type:String,minimum_price:String,
                                   maximum_price:String,location:String,date:String,time:String,people_count:String,property_size:String,bedroom:String,
                                   bathroom:String,instant_booking:String,self_check_in:String,allows_pets:String,activities:List<String>,amenities:List<String>,languages:List<String>) : Flow<NetworkResult<Pair<String,String>>>

    suspend fun verifyIdentity(userId: String,identity_verify: String) : Flow<NetworkResult<Pair<String,String>>>

//    suspend fun getPrivacyPolicy() : Flow<NetworkResult<String>>
//
//
//    suspend fun  getTermCondition() : Flow<NetworkResult<String>>

//    suspend fun feedback(user_id : String,type: String,details: String) : Flow<NetworkResult<String>>
//
//    suspend fun  getFaq() : Flow<NetworkResult<MutableList<FaqModel>>>






    suspend fun hostBookingDetails(bookingId:Int,latitude :String?,longitude :String?) :  Flow<NetworkResult<Pair<String,HostDetailModel>>>

    suspend fun contactUs(user_id : String,name : String,email: String,message: String) : Flow<NetworkResult<String>>

    suspend fun getHelpCenter(user_id : String,user_type : String) : Flow<NetworkResult<JsonObject>>

    suspend fun getArticleDetails(article_id : String) : Flow<NetworkResult<JsonObject>>

    suspend fun getGuideDetails(guide_id : String) : Flow<NetworkResult<JsonObject>>

    suspend fun propertyFilterReviews(
      propertyId :Int, filter: String, page :Int
    ) : Flow<NetworkResult<Pair<PaginationModel,MutableList<HostReviewModel>>>>


    suspend fun getNotificationHost(@Field("user_id") userId :Int) : Flow<NetworkResult<MutableList<NotificationScreenModel>>>

    suspend fun deleteNotificationHost(userId: Int, notificationId :Int) : Flow<NetworkResult<String>>

    suspend fun hostReportViolation(
         userId :Int,
         bookingId :Int,
        propertyId :Int,
        reportReasonId :Int,
        additionalDetails :String
    )

    suspend fun reportListReason() : Flow<NetworkResult<MutableList<Pair<Int,String>>>>




    suspend fun getHomeData(
        userId: String,
        latitude: String,
        longitude: String
    ): Flow<NetworkResult<JsonArray>>

    suspend fun getWisList(userId: String): Flow<NetworkResult<JsonArray>>

    suspend fun createWishlist(
        userId: String,
        name: String,
        description: String,
        property_id: String
    ): Flow<NetworkResult<Pair<String, String>>>

    suspend fun deleteWishlist(
        userId: String,
        wishlist_id: String
    ): Flow<NetworkResult<Pair<String, String>>>

    suspend fun removeItemFromWishlist(
        userId: String,
        property_id: String
    ): Flow<NetworkResult<Pair<String, String>>>

    suspend fun saveItemInWishlist(
        userId: String,
        property_id: String,
        wishlist_id: String
    ): Flow<NetworkResult<Pair<String, String>>>


    suspend fun getArticleList(search_term: String): Flow<NetworkResult<JsonObject>>

    suspend fun getGuideList(search_term: String): Flow<NetworkResult<JsonObject>>

    suspend fun hostReportViolationSend(userId :Int,
                                         bookingId :Int,
                                        propertyId :Int,
                                       reportReasonId :Int,
                                       additionalDetail :String) : Flow<NetworkResult<String>>



    suspend fun getHomePropertyDetails(userId :String,
                                       propertyId :String) : Flow<NetworkResult<Pair<JsonObject, JsonObject>>>


    suspend fun filterPropertyReviews(propertyId :String,
                                      filter :String,
                                      page :String) : Flow<NetworkResult<Pair<JsonArray, JsonObject>>>


    suspend fun getUserCards(userId :String) : Flow<NetworkResult<JsonObject>>

    //Shrawan Call Api
    suspend fun sameAsMailingAddress(userId :String) : Flow<NetworkResult<JsonObject>>

    suspend fun saveCardStripe(userId :String,
                               token_stripe :String) :  Flow<NetworkResult<Pair<String,String>>>

    suspend fun setPreferredCard(userId :String,
                                 card_id :String) :  Flow<NetworkResult<Pair<String,String>>>

    suspend fun bookProperty(userId : String,
                             property_id : String,
                             booking_date : String,
                             booking_start : String,
                             booking_end : String,
                             booking_amount : String,
                             total_amount : String,
                             customer_id : String,
                             card_id : String,
                             addons: Map<String, String> ) :   Flow<NetworkResult<JsonObject>>

    suspend fun reportViolation(userId : String,
                                booking_id : String,
                                property_id : String,
                                report_reasons_id : String,
                                additional_details:String) :  Flow<NetworkResult<Pair<String,String>>>

    suspend fun listReportReasons() : Flow<NetworkResult<JsonArray>>

    suspend fun cancelBooking(userId : String,
                                booking_id : String) :  Flow<NetworkResult<Pair<String,String>>>
}



