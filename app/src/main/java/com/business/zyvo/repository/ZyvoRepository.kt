package com.business.zyvo.repository

import android.net.Network
import com.business.zyvo.NetworkResult
import com.business.zyvo.activity.guest.checkout.model.ReqAddOn
import com.business.zyvo.activity.guest.propertydetails.model.AddOn

import com.business.zyvo.model.HostMyPlacesModel
import com.business.zyvo.model.host.GetPropertyDetail

import com.business.zyvo.fragment.both.completeProfile.model.CompleteProfileReq
import com.business.zyvo.fragment.both.faq.model.FaqModel


import com.business.zyvo.model.ChannelListModel

import com.business.zyvo.fragment.both.notificationfragment.NotificationRootModel
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.BookingDetailModel
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.BookingModel
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.ReviewModel

import com.business.zyvo.model.MyBookingsModel
import com.business.zyvo.model.NotificationScreenModel
import com.business.zyvo.model.StateModel
import com.business.zyvo.model.host.ChannelModel
import com.business.zyvo.model.host.CountryModel
import com.business.zyvo.model.host.HostReviewModel
import com.business.zyvo.model.host.PaginationModel

import com.business.zyvo.model.host.PropertyDetailsSave

import com.business.zyvo.model.host.hostdetail.HostDetailModel

import com.google.gson.JsonArray

import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

import retrofit2.http.Field

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path


interface ZyvoRepository {


    suspend fun signUpPhoneNumber(phoneNumber: String, code: String)
    : Flow<NetworkResult<Pair<String, String>>>

    suspend fun loginPhoneNumber(
        phoneNumber: String,
        code: String,
        fcmToken :String
    ): Flow<NetworkResult<Pair<String, String>>>

    suspend fun otpVerifyLoginPhone(userId: String,
                                    otp: String,
                                    fcmToken:String): Flow<NetworkResult<JsonObject>>

    suspend fun otpVerifySignupPhone(tempId: String, otp: String,
                                     fcmToken :String): Flow<NetworkResult<JsonObject>>

    suspend fun loginEmail(email: String, password: String,fcmToken :String): Flow<NetworkResult<JsonObject>>

    suspend fun signupEmail(
        email: String,
        password: String
    ): Flow<NetworkResult<Pair<String, String>>>

    suspend fun otpVerifySignupEmail(temp_id: String, otp: String,fcmToken:String): Flow<NetworkResult<JsonObject>>

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




    suspend fun getTermCondition(): Flow<NetworkResult<Pair<String,String>>>

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

    suspend fun getFilteredHomeData(userId: String?, latitude: String?, longitude: String?,
                                    place_type: String?, minimum_price: String?,
        maximum_price: String?, location: String?, date: String?, time: String?,
                                    people_count: String?, property_size: String?, bedroom: String?,
        bathroom: String?, instant_booking: String?, self_check_in: String?,
                                    allows_pets: String?, activities: List<String>?,
        amenities: List<String>?, languages: List<String>?): Flow<NetworkResult<JsonArray>>

    suspend fun getBookingList(userId: String) :  Flow<NetworkResult<MutableList<BookingModel>>>
    suspend fun getBookingDetailsList(userId: String,booking_id: Int,latitude:String,longitude:String) : Flow<NetworkResult<Pair<BookingDetailModel,JsonObject>>>

    suspend fun reviewPublish(userId: String,booking_id:String,
                              property_id: String,response_rate: String,communication:String,on_time:String,review_message: String) : Flow<NetworkResult<ReviewModel>>

    suspend fun verifyIdentity(userId: String,identity_verify: String) : Flow<NetworkResult<Pair<String,String>>>

    suspend fun getGuestNotification(userId: String) : Flow<NetworkResult<MutableList<NotificationRootModel>>>

    suspend fun getMarkGuestNotification(userId: String,notification_id: Int) : Flow<NetworkResult<NotificationRootModel>>

    suspend fun getRemoveGuestNotification(userId: String,notification_id: Int) : Flow<NetworkResult<NotificationRootModel>>


//    suspend fun getPrivacyPolicy() : Flow<NetworkResult<String>>
//
//
//    suspend fun  getTermCondition() : Flow<NetworkResult<String>>

    suspend fun getSocialLogin(fname:String,lname:String,email:String,social_id:String,fcm_token:String,device_type:String) : Flow<NetworkResult<JsonObject>>

    suspend fun getPrivacyPolicy() : Flow<NetworkResult<Pair<String,String>>>


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


    suspend fun getNotificationHost( userId :Int) : Flow<NetworkResult<MutableList<NotificationScreenModel>>>

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




    suspend fun reviewGuest(
         userId :Int,
         bookingId :Int,
         propertyId :Int,
         responseRate :Int,
         communication :Int,
         onTime :Int,
         reviewMessage :String
    )  : Flow<NetworkResult<String>>



    suspend fun logout(userId :String) : Flow<NetworkResult<String>>


    suspend fun getChatToken(userId :Int,role :String) :Flow<NetworkResult<String>>



   suspend fun propertyBookingDetails(
       property_id :String,
       user_id :String,
       start_date :String,
       end_date :String,
       latitude :String,
       longitude :String
   ): Flow<NetworkResult<JsonObject>>


    suspend fun togglePropertyBooking(
        property_id :String,
        user_id :String
    ): Flow<NetworkResult<JsonObject>>



    suspend fun joinChatChannel(
       senderId :Int,
       receiverId :Int,
       groupChannel :String,
       user_type: String
    ) : Flow<NetworkResult<ChannelModel>>


    suspend fun getUserChannel(
        @Field("user_id") userId :Int
        ,getUserChannel: String,
        archive_status:String
    ) : Flow<NetworkResult<MutableList<ChannelListModel>>>



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
                             addons: Map<String, String>,
                             service_fee : String,
                             tax : String,
                             discount_amount : String) :   Flow<NetworkResult<JsonObject>>

    suspend fun reportViolation(userId : String,
                                booking_id : String,
                                property_id : String,
                                report_reasons_id : String,
                                additional_details:String) :  Flow<NetworkResult<Pair<String,String>>>

    suspend fun listReportReasons() : Flow<NetworkResult<JsonArray>>

    suspend fun cancelBooking(userId : String,
                                booking_id : String) :  Flow<NetworkResult<Pair<String,String>>>


    suspend fun addPayOut(
        userId: RequestBody,
        firstName: RequestBody,
        lastName: RequestBody,
        email: RequestBody,
        phoneNumber: RequestBody,
        dobList: List<MultipartBody.Part>,
        idType: RequestBody,
        ssnLast4: RequestBody,
        idNumber: RequestBody,
        address: RequestBody,
        country: RequestBody,
        state: RequestBody,
        city: RequestBody,
        postalCode: RequestBody,
        bankName: RequestBody,
        accountHolderName: RequestBody,
        accountNumber: RequestBody,
        accountNumberConfirmation: RequestBody,
        routingProperty: RequestBody,
        bankProofType : RequestBody,
        bank_proof_document: MultipartBody.Part?,
        verification_document_front: MultipartBody.Part?,
        verification_document_back: MultipartBody.Part?

    ): Flow<NetworkResult<String>>


    suspend fun getCountries() : Flow<NetworkResult<MutableList<CountryModel>>>


    suspend fun getState(@Path("value") value: String) : Flow<NetworkResult<MutableList<StateModel>>>


    suspend fun getCityName( country:String,  state :String)  :Flow<NetworkResult<MutableList<String>>>


    suspend fun addPayCard(
        userId: RequestBody,
        token: RequestBody,
        firstName: RequestBody,
        lastName: RequestBody,
        email: RequestBody,
        dobList: List<MultipartBody.Part>,
        ssnLast4: RequestBody,
        phoneNumber: RequestBody,
        address: RequestBody,
        city: RequestBody,
        state: RequestBody,
        country: RequestBody,
        postalCode: RequestBody,
        idType: RequestBody,
        idNumber: RequestBody,
        verification_document_front: MultipartBody.Part?,
        verification_document_back: MultipartBody.Part?
    ): Flow<NetworkResult<String>>


    suspend fun getPayoutMethods(
        userId :String
    ) : Flow<NetworkResult<JsonObject>>

    suspend fun setPrimaryPayoutMethod(
    userId :String,
    payoutMethodId :String,
    ) : Flow<NetworkResult<String>>

    suspend fun deletePayoutMethod(
    userId :String,
    payoutMethodId :String,
    ) : Flow<NetworkResult<String>>


    suspend fun filterPropertyReviewsHost(
        propertyId :Int, filter :String, page :Int
    ) : Flow<NetworkResult<Pair<JsonArray, JsonObject>>>



    suspend fun getBookingExtensionTimeAmount(userId : String,
                                              booking_id : String,
                                              extension_time : String,
                                              service_fee : String,
                                              tax : String,
                                              cleaning_fee:String,
                                              extension_total_amount : String,
                                              extension_booking_amount : String,
                                              discount_amount : String) :   Flow<NetworkResult<JsonObject>>


    suspend fun getHomeDataSearchFilter(
        user_id : String,
        latitude : String,
        longitude : String,
        date : String,
        hour : String,
        start_time : String,
        end_time : String,
        activity : String
    ): Flow<NetworkResult<JsonArray>>

    suspend fun getUserBookings(
        user_id : String,
        booking_date : String,
        booking_start : String
    ): Flow<NetworkResult<JsonObject>>

    suspend fun getHostUnreadBookings(
        @Field("user_id") userId :Int
    ) : Flow<NetworkResult<Int>>


    suspend fun markHostBooking(
        @Field("user_id") userId :Int
    ) : Flow<NetworkResult<String>>

    suspend fun paymentWithdrawalList(
        userId : String,
        startDate : String,
        endDate : String,
        filterStatus : String,
    ): Flow<NetworkResult<JsonObject>>


    suspend fun payoutBalance(
        userId : String
    ): Flow<NetworkResult<Pair<String, String>>>


    suspend fun requestWithdrawal(
        userId : String,
        amount : String,
        withdrawalType : String,
    ): Flow<NetworkResult<JsonObject>>


    suspend fun getSavedItemWishList(
        @Field("user_id") userId :Int,
        @Field("wishlist_id") wishListId :Int
    ) :Flow<NetworkResult<JsonObject>>


    suspend fun updatePhoneNumber(
        @Field("user_id") userId :Int,
        @Field("phone_number") phoneNumber :String,
        @Field("country_code") countryCode :String
    ) :Flow<NetworkResult<String>>

    suspend fun otpVerifyUpdatePhoneNumber(
        @Field("user_id") userId :Int,
        @Field("otp") otp :String
    ) : Flow<NetworkResult<String>>
     suspend fun otpResetPassword(
        @Field("user_id") userId :Int
    ) : Flow<NetworkResult<Pair<String,String>>>

    suspend fun updateEmail(
        @Field("user_id") userId :Int,
        @Field("email") email :String
    ) :Flow<NetworkResult<String>>

    suspend fun otpVerifyUpdateEmail(
        @Field("user_id") userId :Int,
        @Field("otp") otp :String
    ):Flow<NetworkResult<String>>

    @POST("withdraw_funds")
    @FormUrlEncoded
    suspend fun withdrawFunds(
        @Field("user_id") userId :String
    ) : Flow<NetworkResult<Pair<String, String>>>

    suspend fun blockUser(
        senderId :Int,
        group_channel :String,
        blockUnblock:Int
    ) :Flow<NetworkResult<JsonObject>>

    suspend fun markFavoriteChat(
        senderId :Int,
        group_channel :String,
        favorite:Int
    ) :Flow<NetworkResult<JsonObject>>

    suspend fun sendChatNotification(
        senderId :String,
        receiverId :String,
        group_channel :String,
    ) :Flow<NetworkResult<JsonObject>>

    suspend fun muteChat(
        userId :Int,
        group_channel :String,
        mute:Int
    ) :Flow<NetworkResult<JsonObject>>

    suspend fun toggleArchiveUnarchive(
        userId :Int,
        group_channel :String
    ) :Flow<NetworkResult<JsonObject>>


    suspend fun reportChat(
        reporter_id :String,
        reported_user_id :String,
        reason :String,
        message :String,
        group_channel:String
    ) :Flow<NetworkResult<JsonObject>>


    suspend fun hostListing(
        hostId :String,
        latitude :String,
        longitude :String
    ) :Flow<NetworkResult<Pair<JsonObject, JsonObject>>>

    suspend fun filterHostReviews(
        hostId :String,
        latitude :String,
        longitude :String,
        filter: String,
        page:String
    ) :Flow<NetworkResult<Pair<JsonArray, JsonObject>>>

}



