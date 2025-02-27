package com.business.zyvo.remote

import com.business.zyvo.model.host.PropertyDetailsSave
import com.google.android.gms.common.annotation.KeepForSdkWithMembers
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST

import retrofit2.http.PUT

import retrofit2.http.Part


interface ZyvoApi {

    @POST("signup_phone_number")
    @FormUrlEncoded
    suspend fun signUpPhoneNumber(@Field("phone_number")phoneNumber :String,
                                  @Field("country_code")countryCode : String) : Response<JsonObject>

    @POST("login_phone_number")
    @FormUrlEncoded
    suspend fun loginPhoneNumber(@Field("phone_number")phoneNumber :String,
                                  @Field("country_code")countryCode : String) : Response<JsonObject>

    @POST("otp_verify_login_phone")
    @FormUrlEncoded
    suspend fun otpVerifyLoginPhone(@Field("user_id")user_id :String,
                                 @Field("otp")otp : String) : Response<JsonObject>

    @POST("otp_verify_signup_phone")
    @FormUrlEncoded
    suspend fun otpVerifySignupPhone(@Field("temp_id")temp_id :String,
                                    @Field("otp")otp : String) : Response<JsonObject>

    @POST("login_email")
    @FormUrlEncoded
    suspend fun loginEmail(@Field("email")email :String,
                                    @Field("password")password : String) : Response<JsonObject>


    @POST("signup_email")
    @FormUrlEncoded
    suspend fun signupEmail(@Field("email")email :String,
                           @Field("password")password : String) : Response<JsonObject>

    @POST("otp_verify_signup_email")
    @FormUrlEncoded
    suspend fun otpVerifySignupEmail(@Field("temp_id")temp_id :String,
                                     @Field("otp")otp : String) : Response<JsonObject>


    @POST("forgot_password")
    @FormUrlEncoded
    suspend fun forgotPassword(@Field("email")email :String) : Response<JsonObject>

    @POST("otp_verify_forgot_password")
    @FormUrlEncoded
    suspend fun otpVerifyForgotPassword(@Field("user_id")user_id :String,
                                        @Field("otp")otp :String) : Response<JsonObject>


    @POST("reset_password")
    @FormUrlEncoded
    suspend fun resetPassword(@Field("user_id")user_id :String,
                                        @Field("password")password :String,
                              @Field("password_confirmation")password_confirmation :String) : Response<JsonObject>

    @POST("get_user_profile")
    @FormUrlEncoded
    suspend fun getUserProfile(@Field("user_id")user_id :String) : Response<JsonObject>

    @POST("store_property_details")
    suspend fun addProperty(@Body addProperty : PropertyDetailsSave) : Response<JsonObject>

    @POST("get_properties_lists")
    @FormUrlEncoded
    suspend fun getMyPlacesApi(@Field("user_id")user_id : Int,@Field("latitude")latitude :Double?,@Field("longitude")lng:Double?) : Response<JsonObject>

    @POST("get_properties_lists")
    @FormUrlEncoded
    suspend fun getMyPlacesWithOutLatLangApi(@Field("user_id")user_id : Int) : Response<JsonObject>


    @POST("get_property_details")
    @FormUrlEncoded
    suspend fun getPropertyDetails(@Field("property_id") propertyId :Int) : Response<JsonObject>


    @POST("delete_property")
    @FormUrlEncoded
    suspend fun deleteProperty(@Field("property_id")propertyId: Int) : Response<JsonObject>

    @POST("earnings")
    @FormUrlEncoded
    suspend fun totalEarning(@Field("host_id")hostId :Int, @Field("type")type:String) : Response<JsonObject>

    @PUT("update_property_details")
    suspend fun updatePropertyDetail(@Body addProperty : PropertyDetailsSave) : Response<JsonObject>


    @POST("add_live_place")
    @FormUrlEncoded
    suspend fun addLivePlace(@Field("user_id")user_id :String,
                             @Field("place_name")place_name :String) : Response<JsonObject>

    @POST("delete_live_place")
    @FormUrlEncoded
    suspend fun deleteLivePlace(@Field("user_id")user_id :String,
                             @Field("index")index :Int) : Response<JsonObject>

    @POST("add_my_work")
    @FormUrlEncoded
    suspend fun addMyWork(@Field("user_id")user_id :String,
                          @Field("work_name")work_name :String) : Response<JsonObject>
    @POST("delete_my_work")
    @FormUrlEncoded
    suspend fun deleteMyWork(@Field("user_id")user_id :String,
                             @Field("index")work_index :Int) : Response<JsonObject>

    @POST("add_language")
    @FormUrlEncoded
    suspend fun addLanguage(@Field("user_id")user_id :String,
                            @Field("language_name")language_name :String) : Response<JsonObject>

    @POST("delete_language")
    @FormUrlEncoded
    suspend fun deleteLanguage(@Field("user_id")user_id :String,
                               @Field("index")index :Int) : Response<JsonObject>

    @POST("add_hobby")
    @FormUrlEncoded
    suspend fun addHobbies(@Field("user_id")user_id :String,
                           @Field("hobby_name")hobbies_name :String) : Response<JsonObject>

    @POST("delete_hobby")
    @FormUrlEncoded
    suspend fun deleteHobbies(@Field("user_id")user_id :String,
                              @Field("index")index :Int) : Response<JsonObject>

    @POST("add_pet")
    @FormUrlEncoded
    suspend fun addPets(@Field("user_id")user_id :String,
                        @Field("pet_name")pet_name :String) : Response<JsonObject>

    @POST("delete_pet")
    @FormUrlEncoded
    suspend fun deletePets(@Field("user_id")user_id :String,
                           @Field("index")index :Int) : Response<JsonObject>

    @POST("add_street_address")
    @FormUrlEncoded
    suspend fun addStreetAddress(@Field("user_id")user_id :String,
                                 @Field("street_address")street_address :String) : Response<JsonObject>

    @POST("add_city")
    @FormUrlEncoded
    suspend fun addCity(@Field("user_id")user_id :String,
                        @Field("city")city :String) : Response<JsonObject>

    @POST("add_state")
    @FormUrlEncoded
    suspend fun addState(@Field("user_id")user_id :String,
                        @Field("state")state :String) : Response<JsonObject>

    @POST("add_zip_code")
    @FormUrlEncoded
    suspend fun addZipCode(@Field("user_id")user_id :String,
                        @Field("zip_code")zipCode :String) : Response<JsonObject>

    @POST("get_payment_methods")
    @FormUrlEncoded
    suspend fun getPaymentMethods(@Field("user_id")user_id :String ): Response<JsonObject>

    @POST("get_home_data_filter")
    @FormUrlEncoded
    suspend fun getFilteredHomeData(
        @Field("user_id") user_id: Int?,
        @Field("latitude") latitude: Double?,
        @Field("longitude") longitude: Double?,
        @Field("place_type") place_type: String?,
        @Field("minimum_price") minimum_price: Double?,
        @Field("maximum_price") maximum_price: Double?,
        @Field("location") location: String?,
        @Field("date") date: String?,
        @Field("time") time: Int?,
        @Field("people_count") people_count: Int?,
        @Field("property_size") property_size: Int?,
        @Field("bedroom") bedroom: Int?,
        @Field("bathroom") bathroom: Int?,
        @Field("instant_booking") instant_booking: Int?,
        @Field("self_check_in") self_check_in: Int?,
        @Field("allows_pets") allows_pets: Int?,
        @Field("activities[]") activities: List<String>?,
        @Field("amenities[]") amenities: List<String>?,
        @Field("languages[]") languages: List<String>?): Response<JsonObject>


    @POST("update_password")
    @FormUrlEncoded
    suspend fun updatePassword(@Field("user_id")user_id :String,
                              @Field("password")password :String,
                              @Field("password_confirmation")password_confirmation :String) : Response<JsonObject>

    @POST("verify_identity")
    @FormUrlEncoded
    suspend fun  verifyIdentity(@Field("user_id")user_id :String,
                                @Field("identity_verify")identity_verify :String) : Response<JsonObject>

    @POST("social_login")
    @FormUrlEncoded
    suspend fun  getSocialLogin(@Field("fname")fname :String,
                                @Field("lname")lname :String,
                                @Field("email")email :String,
                                @Field("social_id")social_id :String,
                                @Field("fcm_token")fcm_token :String,
                                @Field("device_type")device_type :String) : Response<JsonObject>

    @POST("get_booking_list")
    @FormUrlEncoded
    suspend fun  bookingList(@Field("user_id")user_id :String) : Response<JsonObject>

    @POST("get_booking_details_list")
    @FormUrlEncoded
    suspend fun  bookingDetailsList(@Field("user_id")user_id :String,
                             @Field("booking_id")booking_id :Int) : Response<JsonObject>

    @POST("review_host")
    @FormUrlEncoded
    suspend fun  getReviewPublish(@Field("user_id")user_id :String,
                                @Field("booking_id")booking_id :Int,
                                @Field("property_id")property_id :Int,
                                @Field("response_rate")response_rate :String,
                                @Field("communication")communication :String,
                                @Field("on_time")on_time :String,
                                  @Field("review_message")review_message:String) : Response<JsonObject>


    @POST("complete_profile")
    @Multipart
    suspend fun completeProfile(@Part("user_id")user_id : RequestBody,
                                @Part("first_name")first_name :RequestBody,
                                @Part("last_name")last_name :RequestBody,
                                @Part("about_me")about_me :RequestBody,
                                @Part ("where_live[]") whereLive:List<@JvmSuppressWildcards RequestBody>,
                                @Part ("works[]") works :List<@JvmSuppressWildcards RequestBody>,
                                @Part ("languages[]") languages :List<@JvmSuppressWildcards RequestBody>,
                                @Part ("hobbies[]") hobbies :List<@JvmSuppressWildcards RequestBody>,
                                @Part ("pets[]") pets :List<@JvmSuppressWildcards RequestBody>,
                                @Part("street_address")street_address :RequestBody,
                                @Part("city")city :RequestBody,
                                @Part("state")state :RequestBody,
                                @Part("zip_code")zip_code :RequestBody,
                                @Part profile_picture: MultipartBody.Part?,
                                @Part("identity_verify")identity_verify:RequestBody) : Response<JsonObject>

    @POST("email_verification")
    @FormUrlEncoded
    suspend fun emailVerification(@Field("user_id")user_id :String,
                             @Field("email")email :String) : Response<JsonObject>


    @POST("phone_verification")
    @FormUrlEncoded
    suspend fun phoneVerification(@Field("user_id")user_id :String,
                                  @Field("country_code")country_code :String,
                                  @Field("phone_number")phone_number :String) : Response<JsonObject>


    @POST("otp_verify_email_verification")
    @FormUrlEncoded
    suspend fun otpVerifyEmailVerification(@Field("user_id")user_id :String,
                                  @Field("otp")otp :String) : Response<JsonObject>

    @POST("otp_verify_phone_verification")
    @FormUrlEncoded
    suspend fun otpVerifyPhoneVerification(@Field("user_id")user_id :String,
                                           @Field("otp")otp :String) : Response<JsonObject>

    @POST("upload_profile_image")
    @Multipart
    suspend fun uploadProfileImage( @Part("user_id")user_id :RequestBody,
                                    @Part profile_image: MultipartBody.Part?) : Response<JsonObject>

    @POST("add_update_name")
    @FormUrlEncoded
    suspend fun addUpdateName(@Field("user_id")user_id :String,
                              @Field("first_name")first_name :String,
                              @Field("last_name")last_name :String) : Response<JsonObject>

    @POST("add_about_me")
    @FormUrlEncoded
    suspend fun addAboutme(@Field("user_id")user_id :String,
                              @Field("about_me")about_me :String) : Response<JsonObject>


    @POST("get_host_booking_list")
    @FormUrlEncoded
    suspend fun getHostBookingList(@Field("user_id")userid:Int): Response<JsonObject>


    @POST("delete_live_place")
    @FormUrlEncoded
    suspend fun deleteLivePlace(@Field("user_id")user_id :String, @Field("index")index :String) : Response<JsonObject>


    @POST("approve_decline_booking")
    @FormUrlEncoded
    suspend fun approveDeclineBooking(
        @Field("booking_id") bookingId :Int,
        @Field("status") status :String,
        @Field("message") message :String,
        @Field("declined_reason") declineReason :String
    ) : Response<JsonObject>


    @GET("get_privacy_policy")
    suspend fun getPrivacyPolicy():Response<JsonObject>

    @GET("get_term_condition")
    suspend fun getTermCondition():Response<JsonObject>

    @POST("feedback")
    @FormUrlEncoded
    suspend fun feedback(
        @Field("user_id") user_id : String,
        @Field("type") type : String,
        @Field("details") details : String
    ) :Response<JsonObject>

    @GET("get_faq")
    suspend fun getFaq():Response<JsonObject>




    @POST("property_image_delete")
    @FormUrlEncoded
    suspend fun propertyImageDelete(@Field("image_id")imageId :Int) : Response<JsonObject>

    @POST("host_booking_details")
    @FormUrlEncoded
    suspend fun hostBookingDetails(@Field("booking_id") bookingId:Int, @Field("latitude")latitude :String?,
                                   @Field("longitude") longitude :String?) : Response<JsonObject>

    @POST("contact_us")
    @FormUrlEncoded
    suspend fun contactUs(
        @Field("user_id") user_id : String,
        @Field("name") name : String,
        @Field("email") email : String,
        @Field("message") message : String,
    ) :Response<JsonObject>


    @POST("get_help_center")
    @FormUrlEncoded
    suspend fun getHelpCenter(
        @Field("user_id") user_id : String,
        @Field("user_type") user_type : String
    ) :Response<JsonObject>

    @POST("get_article_details")
    @FormUrlEncoded
    suspend fun getArticleDetails(
        @Field("article_id") article_id : String
    ) :Response<JsonObject>

    @POST("get_guide_details")
    @FormUrlEncoded
    suspend fun getGuideDetails(
        @Field("guide_id") guide_id : String
    ) :Response<JsonObject>


    @POST("get_home_data")
    @FormUrlEncoded
    suspend fun getHomeData(
        @Field("user_id") user_id : String,
        @Field("latitude") latitude : String,
        @Field("longitude") longitude : String
    ) :Response<JsonObject>

    @POST("get_wishlist")
    @FormUrlEncoded
    suspend fun getWisList(
        @Field("user_id") user_id : String) :Response<JsonObject>

    @POST("create_wishlist")
    @FormUrlEncoded
    suspend fun createWishlist(
        @Field("user_id") user_id : String,
        @Field("name") name : String,
        @Field("description") description : String,
        @Field("property_id") property_id : String) :Response<JsonObject>

    @POST("delete_wishlist")
    @FormUrlEncoded
    suspend fun deleteWishlist(
        @Field("user_id") user_id : String,
        @Field("wishlist_id") wishlist_id : String) :Response<JsonObject>

    @POST("remove_item_from_wishlist")
    @FormUrlEncoded
    suspend fun removeItemFromWishlist(
        @Field("user_id") user_id : String,
        @Field("property_id") property_id : String) :Response<JsonObject>


    @POST("save_item_in_wishlist")
    @FormUrlEncoded
    suspend fun saveItemInWishlist(
        @Field("user_id") user_id : String,
        @Field("property_id") property_id : String,
        @Field("wishlist_id") wishlist_id : String) :Response<JsonObject>

}