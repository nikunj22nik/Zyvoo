package com.business.zyvo.backgroundTask

import android.util.Log
import com.business.zyvo.NetworkResult
import com.business.zyvo.model.NotificationScreenModel
import com.business.zyvo.model.host.HostReviewModel
import com.business.zyvo.model.host.PaginationModel
import com.business.zyvo.model.host.hostdetail.HostDetailModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BookingDetails {

   suspend fun getReviewsData(apiResponse: JsonObject) : NetworkResult<Pair<PaginationModel, MutableList<HostReviewModel>>> = withContext(Dispatchers.IO) {
        val obj = apiResponse.get("data").asJsonArray
        var reviewList = mutableListOf<HostReviewModel>()
        obj.forEach {
            val hostDetailsModel = Gson().fromJson(obj, HostReviewModel::class.java)
            reviewList.add(hostDetailsModel)
        }
        var paginationObj = apiResponse.get("pagination").asJsonObject
        var pgModel =  Gson().fromJson(paginationObj, PaginationModel::class.java)
        var p = Pair<PaginationModel, MutableList<HostReviewModel>>(pgModel,reviewList)
        NetworkResult.Success(p)
    }

    suspend fun getNotificationHost(apiResponse: JsonObject) : NetworkResult<MutableList<NotificationScreenModel>> = withContext(Dispatchers.IO) {
        val obj = apiResponse.get("data").asJsonArray
        val result = mutableListOf<NotificationScreenModel>()
        obj.forEach {
            var jsonObject = it.asJsonObject
            var notificationId = jsonObject.get("notification_id").asInt
            var title = jsonObject.get("title").asString
            var message = jsonObject.get("message").asString
            var notificationModel = NotificationScreenModel(title,message,notificationId)
            result.add(notificationModel)
        }
        Log.d("TESTING","Size of the result "+result.size.toString())
        NetworkResult.Success(result)
    }




}