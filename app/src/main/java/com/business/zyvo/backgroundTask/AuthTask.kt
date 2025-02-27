package com.business.zyvo.backgroundTask

import android.util.Log
import com.business.zyvo.NetworkResult
import com.business.zyvo.fragment.guest.bookingviewmodel.dataclass.BookingDetailModel
import com.business.zyvo.fragment.guest.bookingviewmodel.dataclass.BookingModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject

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


         fun processedData(apiResponse: JsonObject): NetworkResult<MutableList<BookingModel>> {
             val obj = apiResponse.get("data").asJsonArray
             var list = mutableListOf<BookingModel>()
             obj.forEach {
                 if(it.isJsonObject){
                     var currObj = it.asJsonObject
                     list.add(Gson().fromJson(currObj, BookingModel::class.java))
                 }
             }
             return NetworkResult.Success(list)

         }

         fun processSingleData(apiResponse: JsonObject): NetworkResult<BookingDetailModel> {
             return try {
                 // Convert JsonObject into BookingModel
                 val bookingModel = Gson().fromJson(apiResponse, BookingDetailModel::class.java)
                 Log.d("value4448789844","List $bookingModel")
                 NetworkResult.Success(bookingModel) // Return the parsed object
             } catch (e: Exception) {
                 NetworkResult.Error("Error parsing JSON: ${e.message}") // Handle JSON parsing errors
             }
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