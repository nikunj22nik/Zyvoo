package com.business.zyvo.repository

import android.util.Log
import com.business.zyvo.AppConstant
import com.business.zyvo.NetworkResult
import com.business.zyvo.backgroundTask.AuthTask
import com.business.zyvo.fragment.both.completeProfile.model.CompleteProfileReq
import com.business.zyvo.model.host.PropertyDetailsSave
import com.business.zyvo.remote.ZyvoApi
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.ErrorDialog.createMultipartList
import com.business.zyvo.utils.ErrorDialog.createRequestBody
import com.business.zyvo.utils.ErrorDialog.toMultiPartFile
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ZyvoRepositoryImpl @Inject constructor(private val api:ZyvoApi):ZyvoRepository {

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
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: AppConstant.unKnownError))
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
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: AppConstant.unKnownError))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
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
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: AppConstant.unKnownError))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
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
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: AppConstant.unKnownError))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
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
            api.signupEmail(email, password).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&& resp.get("success").asBoolean) {
                            emit(AuthTask.processSignUpData(resp))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: AppConstant.unKnownError))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
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
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: AppConstant.unKnownError))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
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
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: AppConstant.unKnownError))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
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
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: AppConstant.unKnownError))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
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
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: AppConstant.unKnownError))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
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
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: AppConstant.unKnownError))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
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
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: AppConstant.unKnownError))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
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

    override suspend fun completeProfile(completeProfileReq: CompleteProfileReq):
            Flow<NetworkResult<Pair<String, String>>> =flow {
        try {
            var multipart:  MultipartBody.Part? = null
            if (completeProfileReq.bytes != null) {
                multipart = toMultiPartFile("profile_image", "image.jpg", completeProfileReq.bytes)
            }
            val user_id: RequestBody = createRequestBody(completeProfileReq.user_id.toString())
            val first_name: RequestBody = createRequestBody(completeProfileReq.first_name)
            val last_name: RequestBody = createRequestBody(completeProfileReq.last_name)
            val about_me: RequestBody = createRequestBody(completeProfileReq.about_me)
            val where_live: List<RequestBody> = createMultipartList(completeProfileReq.where_live)
            val works: List<RequestBody> = createMultipartList(completeProfileReq.works)
            val languages: List<RequestBody> = createMultipartList(completeProfileReq.languages)
            val hobbies: List<RequestBody> = createMultipartList(completeProfileReq.hobbies)
            val pets: List<RequestBody> = createMultipartList(completeProfileReq.pets)
            val street_address: RequestBody = createRequestBody(completeProfileReq.street_address)
            val city: RequestBody = createRequestBody(completeProfileReq.city)
            val state: RequestBody = createRequestBody(completeProfileReq.state)
            val zip_code: RequestBody = createRequestBody(completeProfileReq.zip_code)
            api.completeProfile(user_id,
                first_name,last_name,about_me,where_live,
                works,languages,hobbies,pets,street_address,city,state,zip_code,multipart).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&&
                            resp.get("success").asBoolean) {
                            emit(NetworkResult.Success(Pair("Profile update Successfully","200")))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: AppConstant.unKnownError))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
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

    override suspend fun phoneVerification(
        userId: String,
        code: String,
        number: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.phoneVerification(
                userId,
                code,
                number
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&& resp.get("success").asBoolean) {
                            emit(AuthTask.processLoginData(resp))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: AppConstant.unKnownError))
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

    override suspend fun emailVerification(
        userId: String,
        email: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.emailVerification(
                userId,
                email,
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&& resp.get("success").asBoolean) {
                            emit(AuthTask.processLoginData(resp))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: AppConstant.unKnownError))
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

    override suspend fun otpVerifyEmailVerification(
        userId: String,
        otp: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.otpVerifyEmailVerification(
                userId,
                otp,
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&& resp.get("success").asBoolean) {
                            emit(NetworkResult.Success(Pair<String,String>("Email verified successfully.","200")))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: AppConstant.unKnownError))
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

    override suspend fun otpVerifyPhoneVerification(
        userId: String,
        otp: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow{
        emit(NetworkResult.Loading())
        try {
            api.otpVerifyPhoneVerification(
                userId,
                otp,
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&& resp.get("success").asBoolean) {
                            emit(NetworkResult.Success(Pair<String,String>("Phone Number verified successfully.","200")))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: AppConstant.unKnownError))
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

    override suspend fun uploadProfileImage(
        userId: String,
        bytes: ByteArray
    ): Flow<NetworkResult<Pair<String, String>>> = flow{
        try {
            var multipart:  MultipartBody.Part? = null
            if (bytes != null) {
                multipart = toMultiPartFile("profile_image", "image.jpg", bytes)
            }
            val user_id: RequestBody = createRequestBody(userId)
            api.uploadProfileImage(user_id,multipart).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&&
                            resp.get("success").asBoolean) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString,"200")))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: AppConstant.unKnownError))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
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

    override suspend fun addUpdateName(
        userId: String,
        first_name: String,
        last_name: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        try {
            api.addUpdateName(userId,first_name,
                last_name).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&&
                            resp.get("success").asBoolean) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString,"200")))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: AppConstant.unKnownError))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
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

    override suspend fun addAboutMe(
        userId: String,
        about_me: String
    ): Flow<NetworkResult<Pair<String, String>>>  = flow{
        try {
            api.addAboutme(userId,about_me).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&&
                            resp.get("success").asBoolean) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString,"200")))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(NetworkResult.Error(jsonObj?.getString("message") ?: AppConstant.unKnownError))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
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

}