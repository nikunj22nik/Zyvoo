package com.business.zyvo.backgroundTask

import com.business.zyvo.NetworkResult
import com.business.zyvo.model.HostMyPlacesModel
import com.business.zyvo.model.host.hostdetail.HostDetailModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object HostDetailsTask {

    suspend fun processPrivacyData(apiResponse: JsonObject): NetworkResult<Pair<String,HostDetailModel>> =
        withContext(Dispatchers.IO) {

            val obj = apiResponse.get("data").asJsonObject
            val hostDetailsModel = Gson().fromJson(obj, HostDetailModel::class.java)
            val p = Pair<String,HostDetailModel>("",hostDetailsModel)

            NetworkResult.Success(p)

        }
}