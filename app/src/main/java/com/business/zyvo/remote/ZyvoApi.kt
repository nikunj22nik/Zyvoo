package com.business.zyvo.remote

import com.business.zyvo.model.host.PropertyDetailsSave
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
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
                                @Part profile_picture: MultipartBody.Part?) : Response<JsonObject>

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

    @POST("add_live_place")
    @FormUrlEncoded
    suspend fun addLivePlace(@Field("user_id")user_id :String,
                           @Field("place_name")place_name :String) : Response<JsonObject>

    @POST("delete_live_place")
    @FormUrlEncoded
    suspend fun deleteLivePlace(@Field("user_id")user_id :String,
                             @Field("index")index :String) : Response<JsonObject>





}