package com.business.zyvo.remote

import com.business.zyvo.model.host.PropertyDetailsSave
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

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





}