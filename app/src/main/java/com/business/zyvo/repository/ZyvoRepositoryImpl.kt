package com.business.zyvo.repository

import com.business.zyvo.NetworkResult
import com.business.zyvo.backgroundTask.AuthTask
import com.business.zyvo.remote.ZyvoApi
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ZyvoRepositoryImpl @Inject constructor(private val api:ZyvoApi):ZyvoRepository {

    override suspend fun signUpPhoneNumber(phoneNumber: String,code:String): NetworkResult<Pair<String, String>> {
        try{
            api.signUpPhoneNumber(phoneNumber,code).apply {
                if(isSuccessful){
                  body()?.let {
                        if(it.get("success").asBoolean){
                            return AuthTask.processSignUpData(it)
                        }
                        else{
                            return NetworkResult.Error<Pair<String, String>>(it.get("message").asString)
                        }
                    } ?: return NetworkResult.Error<Pair<String, String>>("There was an unknown error. Check your connection, and try again.")
                }
                else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        return NetworkResult.Error<Pair<String, String>>(jsonObj?.getString("message") ?: "There was an unknown error. Check your connection and try again.")
                    }
                    catch (e: JSONException) {
                        e.printStackTrace()
                        return NetworkResult.Error<Pair<String, String>>("There was an unknown error. Check your connection, and try again.")
                    }
                }
            }
        }
        catch (e: HttpException) {
            return NetworkResult.Error<Pair<String, String>>("There was an unknown error. Check your connection, and try again.")
        }
        catch (e: IOException) {
            return NetworkResult.Error<Pair<String, String>>("There was an unknown error. Check your connection, and try again.")
        }
        catch (e: Exception) {
            return NetworkResult.Error<Pair<String, String>>("There was an unknown error. Check your connection, and try again.")
        }

    }

}