package com.business.zyvo.repository

import android.util.Log
import com.business.zyvo.AppConstant
import com.business.zyvo.NetworkResult
import com.business.zyvo.backgroundTask.AuthTask
import com.business.zyvo.backgroundTask.MyPlacesTask
import com.business.zyvo.model.HostMyPlacesModel
import com.business.zyvo.model.host.GetPropertyDetail
import com.business.zyvo.model.host.PropertyDetailsSave
import com.business.zyvo.remote.ZyvoApi
import com.business.zyvo.utils.ErrorDialog
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ZyvoRepositoryImpl @Inject constructor(private val api:ZyvoApi):ZyvoRepository {

    /*override suspend fun signUpPhoneNumber(phoneNumber: String,code:String): NetworkResult<Pair<String, String>> {
        try{
            api.signUpPhoneNumber(phoneNumber,code).apply {
                if(isSuccessful){
                  body()?.let {
                        if(it.get("success").asBoolean){
                            return AuthTask.processSignUpData(it)
                        }
                        else{
                            return NetworkResult.Error(it.get("message").asString)
                        }
                    } ?: return NetworkResult.Error("There was an unknown error. Check your connection, and try again.")
                }
                else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        return NetworkResult.Error(jsonObj?.getString("message") ?: "There was an unknown error. Check your connection and try again.")
                    }
                    catch (e: JSONException) {
                        e.printStackTrace()
                        return NetworkResult.Error("There was an unknown error. Check your connection, and try again.")
                    }
                }
            }
        }
        catch (e: HttpException) {
            return NetworkResult.Error("There was an unknown error. Check your connection, and try again.")
        }
        catch (e: IOException) {
            return NetworkResult.Error("There was an unknown error. Check your connection, and try again.")
        }
        catch (e: Exception) {
            return NetworkResult.Error("There was an unknown error. Check your connection, and try again.")
        }

    }*/

    override suspend fun signUpPhoneNumber(
        phoneNumber: String,
        code: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.signUpPhoneNumber(
                phoneNumber,
                code,
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&& resp.get("success").asBoolean) {
                            emit(AuthTask.processSignUpData(resp))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: "There was an unknown error. Check your connection, and try again."))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(e.message!!))
                    }
                }
            }
        } catch (e: HttpException) {
            Log.e(ErrorDialog.TAG,"http exception - ${e.message}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: IOException) {
            Log.e(ErrorDialog.TAG,"io exception - ${e.message} :: ${e.localizedMessage}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: Exception) {
            Log.e(ErrorDialog.TAG,"exception - ${e.message} :: \n ${e.stackTraceToString()}")
            emit(NetworkResult.Error(e.message!!))
        }
    }

    override suspend fun loginPhoneNumber(
        phoneNumber: String,
        code: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.loginPhoneNumber(
                phoneNumber,
                code,
                ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&&
                            resp.get("success").asBoolean) {
                            emit(AuthTask.processLoginData(resp))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: "There was an unknown error. Check your connection, and try again."))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                    }
                }
            }
        } catch (e: HttpException) {
            Log.e(ErrorDialog.TAG,"http exception - ${e.message}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: IOException) {
            Log.e(ErrorDialog.TAG,"io exception - ${e.message} :: ${e.localizedMessage}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: Exception) {
            Log.e(ErrorDialog.TAG,"exception - ${e.message} :: \n ${e.stackTraceToString()}")
            emit(NetworkResult.Error(e.message!!))
        }
    }

    override suspend fun otpVerifyLoginPhone(
        userId: String,
        otp: String
    ): Flow<NetworkResult<JsonObject>> = flow{
        emit(NetworkResult.Loading())
        try {
            api.otpVerifyLoginPhone(
                userId,
                otp,
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&&
                            resp.get("success").asBoolean) {
                            emit(AuthTask.processData(resp))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: "There was an unknown error. Check your connection, and try again."))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                    }
                }
            }
        } catch (e: HttpException) {
            Log.e(ErrorDialog.TAG,"http exception - ${e.message}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: IOException) {
            Log.e(ErrorDialog.TAG,"io exception - ${e.message} :: ${e.localizedMessage}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: Exception) {
            Log.e(ErrorDialog.TAG,"exception - ${e.message} :: \n ${e.stackTraceToString()}")
            emit(NetworkResult.Error(e.message!!))
        }
    }

    override suspend fun otpVerifySignupPhone(
        tempId: String,
        otp: String
    ): Flow<NetworkResult<JsonObject>> = flow{
        emit(NetworkResult.Loading())
        try {
            api.otpVerifySignupPhone(
                tempId,
                otp,
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&&
                            resp.get("success").asBoolean) {
                            emit(AuthTask.processData(resp))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: "There was an unknown error. Check your connection, and try again."))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                    }
                }
            }
        } catch (e: HttpException) {
            Log.e(ErrorDialog.TAG,"http exception - ${e.message}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: IOException) {
            Log.e(ErrorDialog.TAG,"io exception - ${e.message} :: ${e.localizedMessage}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: Exception) {
            Log.e(ErrorDialog.TAG,"exception - ${e.message} :: \n ${e.stackTraceToString()}")
            emit(NetworkResult.Error(e.message!!))
        }
    }

    override suspend fun loginEmail(
        email: String,
        password: String
    ): Flow<NetworkResult<JsonObject>>  = flow{
        emit(NetworkResult.Loading())
        try {
            api.loginEmail(
                email,
                password,
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&&
                            resp.get("success").asBoolean) {
                            emit(AuthTask.processData(resp))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: "There was an unknown error. Check your connection, and try again."))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                    }
                }
            }
        } catch (e: HttpException) {
            Log.e(ErrorDialog.TAG,"http exception - ${e.message}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: IOException) {
            Log.e(ErrorDialog.TAG,"io exception - ${e.message} :: ${e.localizedMessage}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: Exception) {
            Log.e(ErrorDialog.TAG,"exception - ${e.message} :: \n ${e.stackTraceToString()}")
            emit(NetworkResult.Error(e.message!!))
        }
    }

    override suspend fun signupEmail(email: String, password: String):Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.signupEmail(email, password,).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&& resp.get("success").asBoolean) {
                            emit(AuthTask.processSignUpData(resp))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: "There was an unknown error. Check your connection, and try again."))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                    }
                }
            }
        }
        catch (e: HttpException) {
            Log.e(ErrorDialog.TAG,"http exception - ${e.message}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: IOException) {
            Log.e(ErrorDialog.TAG,"io exception - ${e.message} :: ${e.localizedMessage}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: Exception) {
            Log.e(ErrorDialog.TAG,"exception - ${e.message} :: \n ${e.stackTraceToString()}")
            emit(NetworkResult.Error(e.message!!))
        }
    }

    override suspend fun otpVerifySignupEmail(
        tempId: String,
        otp: String
    ): Flow<NetworkResult<JsonObject>> = flow{
        emit(NetworkResult.Loading())
        try {
            api.otpVerifySignupEmail(
                tempId,
                otp,
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&&
                            resp.get("success").asBoolean) {
                            emit(AuthTask.processData(resp))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: "There was an unknown error. Check your connection, and try again."))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                    }
                }
            }
        } catch (e: HttpException) {
            Log.e(ErrorDialog.TAG,"http exception - ${e.message}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: IOException) {
            Log.e(ErrorDialog.TAG,"io exception - ${e.message} :: ${e.localizedMessage}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: Exception) {
            Log.e(ErrorDialog.TAG,"exception - ${e.message} :: \n ${e.stackTraceToString()}")
            emit(NetworkResult.Error(e.message!!))
        }
    }

    override suspend fun forgotPassword(
        email: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow{
        emit(NetworkResult.Loading())
        try {
            api.forgotPassword(
                email
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&&
                            resp.get("success").asBoolean) {
                            emit(AuthTask.processLoginData(resp))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: "There was an unknown error. Check your connection, and try again."))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                    }
                }
            }
        } catch (e: HttpException) {
            Log.e(ErrorDialog.TAG,"http exception - ${e.message}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: IOException) {
            Log.e(ErrorDialog.TAG,"io exception - ${e.message} :: ${e.localizedMessage}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: Exception) {
            Log.e(ErrorDialog.TAG,"exception - ${e.message} :: \n ${e.stackTraceToString()}")
            emit(NetworkResult.Error(e.message!!))
        }
    }

    override suspend fun otpVerifyForgotPassword(
        userId: String,
        otp: String
    ): Flow<NetworkResult<JsonObject>> = flow{
        emit(NetworkResult.Loading())
        try {
            api.otpVerifyForgotPassword(
                userId,
                otp,
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&&
                            resp.get("success").asBoolean) {
                            emit(NetworkResult.Success(resp))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: "There was an unknown error. Check your connection, and try again."))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                    }
                }
            }
        } catch (e: HttpException) {
            Log.e(ErrorDialog.TAG,"http exception - ${e.message}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: IOException) {
            Log.e(ErrorDialog.TAG,"io exception - ${e.message} :: ${e.localizedMessage}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: Exception) {
            Log.e(ErrorDialog.TAG,"exception - ${e.message} :: \n ${e.stackTraceToString()}")
            emit(NetworkResult.Error(e.message!!))
        }
    }


    override suspend fun resetPassword(
        userId: String,
        password: String,
        passwordConfirmation: String
    ): Flow<NetworkResult<JsonObject>> = flow{
        emit(NetworkResult.Loading())
        try {
            api.resetPassword(
                userId,
                password,
                passwordConfirmation
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&&
                            resp.get("success").asBoolean) {
                            emit(NetworkResult.Success(resp))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: "There was an unknown error. Check your connection, and try again."))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                    }
                }
            }
        } catch (e: HttpException) {
            Log.e(ErrorDialog.TAG,"http exception - ${e.message}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: IOException) {
            Log.e(ErrorDialog.TAG,"io exception - ${e.message} :: ${e.localizedMessage}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: Exception) {
            Log.e(ErrorDialog.TAG,"exception - ${e.message} :: \n ${e.stackTraceToString()}")
            emit(NetworkResult.Error(e.message!!))
        }
    }

    override suspend fun getUserProfile(
        userId: String
    ): Flow<NetworkResult<JsonObject>>  = flow{
        emit(NetworkResult.Loading())
        try {
            api.getUserProfile(
                userId).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&&
                            resp.get("success").asBoolean) {
                            emit(AuthTask.processData(resp))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: "There was an unknown error. Check your connection, and try again."))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                    }
                }
            }
        } catch (e: HttpException) {
            Log.e(ErrorDialog.TAG,"http exception - ${e.message}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: IOException) {
            Log.e(ErrorDialog.TAG,"io exception - ${e.message} :: ${e.localizedMessage}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: Exception) {
            Log.e(ErrorDialog.TAG,"exception - ${e.message} :: \n ${e.stackTraceToString()}")
            emit(NetworkResult.Error(e.message!!))
        }
    }

    override suspend fun addPropertyData(property: PropertyDetailsSave): Flow<NetworkResult<Pair<String, Int>>> = flow {
        try {
            api.addProperty(property).apply {

                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&&
                            resp.get("success").asBoolean) {
                            emit(NetworkResult.Success(Pair<String,Int>("Property Added Successfully",1)))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: "There was an unknown error. Check your connection, and try again."))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                    }
                }
            }
        } catch (e: HttpException) {
            Log.e(ErrorDialog.TAG,"http exception - ${e.message}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: IOException) {
            Log.e(ErrorDialog.TAG,"io exception - ${e.message} :: ${e.localizedMessage}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: Exception) {
            Log.e(ErrorDialog.TAG,"exception - ${e.message} :: \n ${e.stackTraceToString()}")
            emit(NetworkResult.Error(e.message!!))
        }

    }

    override suspend fun getPropertyList(userId:Int,latitude: Double?, longitude: Double?): Flow<NetworkResult<Pair<MutableList<HostMyPlacesModel>,String>>> = flow {


        try {
            // Create a coroutine scope to run concurrent tasks
            val responseDeferred = CoroutineScope(Dispatchers.IO).async {
                // Make API call concurrently based on latitude and longitude
                if (latitude != null && longitude != null) {
                    api.getMyPlacesApi(userId, latitude, longitude)
                } else {
                    api.getMyPlacesWithOutLatLangApi(userId)
                }
            }

            val responseDeferred1 = CoroutineScope(Dispatchers.IO).async {
                api.totalEarning(userId,"total")
            }

            // Await for the result of the API call
            val response = responseDeferred.await()
            val response1 = responseDeferred1.await()

            // Handle the result from the API response
            if (response.isSuccessful && response1.isSuccessful) {
                response.body()?.let { resp ->
                    if (resp.has("success") && resp.get("success").asBoolean) {
                        val v1 = MyPlacesTask.getAllMyPlace(resp)

                        Log.d("TESTING_ZYVOO_Size", response.body().toString())

                        if(response1.body()!=null){
                            response1.body()?.let { resp ->
                                if (resp.has("success") && resp.get("success").asBoolean) {
                                    var obj = resp.get("data").asJsonObject
                                    var amountStr =   obj.get("amount").asString
                                    emit(NetworkResult.Success(Pair<MutableList<HostMyPlacesModel>,String>(v1,amountStr)))
                                } else {
                                    emit(NetworkResult.Success(Pair<MutableList<HostMyPlacesModel>,String>(v1,"0.00")))
                                }
                            }
                                ?: emit(NetworkResult.Success(Pair<MutableList<HostMyPlacesModel>,String>(v1,"0.00")))

                        }else{
                            emit(NetworkResult.Success(Pair<MutableList<HostMyPlacesModel>,String>(v1,"0.00")))
                        }

                    } else {
                        emit(NetworkResult.Error(resp.get("message").asString))
                    }
                } ?: emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
            } else {
                // Handle the error from the API response
                try {
                    val jsonObj = response.errorBody()?.string()?.let { JSONObject(it) }
                    emit(NetworkResult.Error(
                        jsonObj?.getString("message")
                            ?: "There was an unknown error. Check your connection, and try again."
                    ))
                } catch (e: JSONException) {
                    e.printStackTrace()
                    emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                }
            }
        } catch (e: HttpException) {
            Log.e(ErrorDialog.TAG, "http exception - ${e.message}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: IOException) {
            Log.e(ErrorDialog.TAG, "io exception - ${e.message} :: ${e.localizedMessage}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: Exception) {
            Log.e(ErrorDialog.TAG, "exception - ${e.message} :: \n ${e.stackTraceToString()}")
            emit(NetworkResult.Error(e.message!!))
        }


    }

    override suspend fun getPropertyDetails(propertyId: Int): Flow<NetworkResult<GetPropertyDetail>> = flow {
        try {
            api.getPropertyDetails(propertyId).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("success") && resp.get("success").asBoolean) {
                                emit(MyPlacesTask.getMyPropertyDetails(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        }
                            ?: emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: "There was an unknown error. Check your connection, and try again."
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                        }
                    }
                }
        } catch (e: HttpException) {
            Log.e(ErrorDialog.TAG,"http exception - ${e.message}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: IOException) {
            Log.e(ErrorDialog.TAG,"io exception - ${e.message} :: ${e.localizedMessage}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: Exception) {
            Log.e(ErrorDialog.TAG,"exception - ${e.message} :: \n ${e.stackTraceToString()}")
            emit(NetworkResult.Error(e.message!!))
        }

    }

    override suspend fun deleteProperty(propertyId: Int): Flow<NetworkResult<String>> = flow {
        try {
            api.deleteProperty(propertyId).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            emit(NetworkResult.Success<String>("Property Deleted Successfully"))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    }
                        ?: emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message")
                                    ?: "There was an unknown error. Check your connection, and try again."
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                    }
                }
            }
        } catch (e: HttpException) {
            Log.e(ErrorDialog.TAG,"http exception - ${e.message}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: IOException) {
            Log.e(ErrorDialog.TAG,"io exception - ${e.message} :: ${e.localizedMessage}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: Exception) {
            Log.e(ErrorDialog.TAG,"exception - ${e.message} :: \n ${e.stackTraceToString()}")
            emit(NetworkResult.Error(e.message!!))
        }

    }

    override suspend fun earning(hostId: Int, type: String): Flow<NetworkResult<String>> = flow {
        try {
            api.totalEarning(hostId,type).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if(resp.has("success") && resp.get("success").asBoolean) {
                          var obj = resp.get("data").asJsonObject
                          var amountStr =   obj.get("amount").asString
                          emit(NetworkResult.Success(amountStr))

                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    }
                        ?: emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message")
                                    ?: "There was an unknown error. Check your connection, and try again."
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                    }
                }
            }
        } catch (e: HttpException) {
            Log.e(ErrorDialog.TAG,"http exception - ${e.message}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: IOException) {
            Log.e(ErrorDialog.TAG,"io exception - ${e.message} :: ${e.localizedMessage}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: Exception) {
            Log.e(ErrorDialog.TAG,"exception - ${e.message} :: \n ${e.stackTraceToString()}")
            emit(NetworkResult.Error(e.message!!))
        }
    }

    override suspend fun updateProperty(requestBody: PropertyDetailsSave): Flow<NetworkResult<String>> = flow {
        try {
            api.updatePropertyDetail(requestBody).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if(resp.has("success") && resp.get("success").asBoolean) {
                            emit(NetworkResult.Success("Property Updated Successfully"))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    }
                        ?: emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message")
                                    ?: "There was an unknown error. Check your connection, and try again."
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
                    }
                }
            }
          }
        catch (e: HttpException) {
            Log.e(ErrorDialog.TAG,"http exception - ${e.message}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: IOException) {
            Log.e(ErrorDialog.TAG,"io exception - ${e.message} :: ${e.localizedMessage}")
            emit(NetworkResult.Error(e.message!!))
        } catch (e: Exception) {
            Log.e(ErrorDialog.TAG,"exception - ${e.message} :: \n ${e.stackTraceToString()}")
            emit(NetworkResult.Error(e.message!!))
        }
    }

}