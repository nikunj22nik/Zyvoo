package com.business.zyvo.backgroundTask

import com.business.zyvo.NetworkResult
import com.google.gson.JsonObject
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

 class AuthTask {

     companion object {

         suspend fun processSignUpData(apiResponse: JsonObject): NetworkResult<Pair<String, String>> {

            var obj = apiResponse.get("data").asJsonObject
            var otp = obj.get("otp").asLong
            var tempId = obj.get("temp_id").asInt
            var pair = Pair<String,String>(otp.toString(),tempId.toString())
            return NetworkResult.Success<Pair<String,String>>(pair)

         }

     }


}