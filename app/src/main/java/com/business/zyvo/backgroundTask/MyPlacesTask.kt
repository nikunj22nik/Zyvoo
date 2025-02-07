package com.business.zyvo.backgroundTask


import com.business.zyvo.NetworkResult
import com.business.zyvo.model.HostMyPlacesModel
import com.business.zyvo.model.host.GetPropertyDetail
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MyPlacesTask {

    suspend fun getAllMyPlace(apiResponse: JsonObject) :MutableList<HostMyPlacesModel> = withContext(Dispatchers.IO){

        val arr =  apiResponse.get("data").asJsonArray
        val resultList = mutableListOf<HostMyPlacesModel>()
        arr.forEach {
            resultList.add(Gson().fromJson(it, HostMyPlacesModel::class.java))
        }
        var list = mutableListOf<HostMyPlacesModel>()

        list = resultList

        list
    }

   suspend fun  getMyPropertyDetails(resp: JsonObject): NetworkResult<GetPropertyDetail> = withContext(Dispatchers.IO){

        val obj = resp.get("data").asJsonObject
     NetworkResult.Success<GetPropertyDetail> (Gson().fromJson(obj, GetPropertyDetail::class.java))

    }

}