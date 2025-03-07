package com.business.zyvo.backgroundTask

import android.util.Log
import com.business.zyvo.NetworkResult
import com.business.zyvo.fragment.both.notificationfragment.NotificationRootModel
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.BookingDetailModel
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.BookingModel
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.ReviewModel
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

         fun notificationData(apiResponse: JsonObject): NetworkResult<MutableList<NotificationRootModel>> {
             val obj = apiResponse.get("data").asJsonArray
             var list = mutableListOf<NotificationRootModel>()
             obj.forEach {
                 if(it.isJsonObject){
                     var currObj = it.asJsonObject
                     list.add(Gson().fromJson(currObj, NotificationRootModel::class.java))
                 }
             }
             return NetworkResult.Success(list)

         }

         fun markNotification(apiResponse: JsonObject): NetworkResult<NotificationRootModel> {
             return try {
                 val markModel = Gson().fromJson(apiResponse, NotificationRootModel::class.java)
                 NetworkResult.Success(markModel)
             } catch (e: Exception) {
                 NetworkResult.Error("Error parsing JSON: ${e.message}") // Handle JSON parsing errors
             }
         }

         fun reviewData(apiResponse: JsonObject): NetworkResult<ReviewModel> {
             return try {
                 val reviewModel = Gson().fromJson(apiResponse, ReviewModel::class.java)
                 NetworkResult.Success(reviewModel)
             } catch (e: Exception) {
                 NetworkResult.Error("Error parsing JSON: ${e.message}") // Handle JSON parsing errors
             }
         }


         fun processTwoData(apiResponse: JsonObject):NetworkResult<Pair<JsonObject, JsonObject>>{
             val obj = apiResponse.get("data").asJsonObject
             val obj2 = apiResponse.get("pagination").asJsonObject
             val pair = Pair(obj,obj2)
             return NetworkResult.Success(pair)

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
         fun processSingleDataChange(apiResponse: JsonObject): BookingDetailModel?{
             return try {
                 // Convert JsonObject into BookingModel
                 val bookingModel = Gson().fromJson(apiResponse, BookingDetailModel::class.java)
                 Log.d("value4448789844","List $bookingModel")
                 bookingModel
             } catch (e: Exception) {
                 Log.e("processSingleData", "Error parsing JSON: ${e.message}", e)
                 null // Return null in case of an error
             }
         }
         

         fun processDataArray(apiResponse: JsonObject): NetworkResult<JsonArray> {
             val obj = apiResponse.get("data").asJsonArray
             return NetworkResult.Success(obj)

         }

         fun processDataArrayAndObject(apiResponse: JsonObject): NetworkResult<Pair<JsonArray, JsonObject>> {
             val obj = apiResponse.get("data").asJsonArray
             val obj2 = apiResponse.get("pagination").asJsonObject
             val pair = Pair(obj,obj2)
             return NetworkResult.Success(pair)

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