package com.business.zyvo.backgroundTask

import com.business.zyvo.NetworkResult
import com.business.zyvo.model.HostMyPlacesModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MyPlacesTask {

    suspend fun getAllMyPlace(apiResponse: JsonObject) :NetworkResult<MutableList<HostMyPlacesModel>> = withContext(Dispatchers.IO){

       var arr =  apiResponse.get("data").asJsonArray
        var resultList = mutableListOf<HostMyPlacesModel>()

        arr.forEach {
            resultList.add(Gson().fromJson(it, HostMyPlacesModel::class.java))
        }

         NetworkResult.Success<MutableList<HostMyPlacesModel>>(resultList)
    }

}