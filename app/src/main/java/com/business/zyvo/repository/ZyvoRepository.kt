package com.business.zyvo.repository

import com.business.zyvo.NetworkResult
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Field

interface ZyvoRepository {


    suspend fun signUpPhoneNumber(phoneNumber :String,code:String) : NetworkResult<Pair<String, String>>




}