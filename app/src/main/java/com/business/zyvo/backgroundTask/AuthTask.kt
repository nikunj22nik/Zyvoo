package com.business.zyvo.backgroundTask

import com.business.zyvo.NetworkResult
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.json.JSONObject

class AuthTask {

     companion object {

         fun processSignUpData(apiResponse: JsonObject): NetworkResult<Pair<String, String>> {
             val obj = apiResponse.get("data").asJsonObject
             val otp = obj.get("otp").asLong
             val tempId = obj.get("temp_id").asInt
             val pair = Pair(otp.toString(),tempId.toString())
             return NetworkResult.Success(pair)
         }

         fun processLoginData(apiResponse: JsonObject): NetworkResult<Pair<String, String>> {
             val obj = apiResponse.get("data").asJsonObject
             val otp = obj.get("otp").asLong
             val tempId = obj.get("user_id").asInt
             val pair = Pair(otp.toString(),tempId.toString())
             return NetworkResult.Success(pair)

         }

         fun processData(apiResponse: JsonObject): NetworkResult<JsonObject> {
             val obj = apiResponse.get("data").asJsonObject
             return NetworkResult.Success(obj)

         }

         fun processDataArray(apiResponse: JsonObject): NetworkResult<JsonArray> {
             val obj = apiResponse.get("data").asJsonArray
             return NetworkResult.Success(obj)

         }

         fun processPrivacyData(apiResponse: JsonObject):NetworkResult<String>{
             val obj = apiResponse.get("data").asJsonObject
             val text = obj.get("text").asString
             return  NetworkResult.Success(text)
         }

         fun processTermAndConditionData(apiResponse: JsonObject):NetworkResult<String>{
             val obj = apiResponse.get("data").asJsonObject
             val text = obj.get("text").asString
             return  NetworkResult.Success(text)
         }

     }


}