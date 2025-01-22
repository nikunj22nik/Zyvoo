package com.business.zyvo.remote

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ZyvoApi {

    @POST("signup_phone_number")
    @FormUrlEncoded
    suspend fun signUpPhoneNumber(@Field("phone_number")phoneNumber :String,
                                  @Field("country_code")countryCode : String) : Response<JsonObject>

}