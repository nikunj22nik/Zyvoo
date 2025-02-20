package com.business.zyvo.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import com.business.zyvo.AppConstant
import com.business.zyvo.NetworkResult
import com.business.zyvo.backgroundTask.AuthTask
import com.business.zyvo.backgroundTask.BookingDetails
import com.business.zyvo.backgroundTask.HostDetailsTask

import com.business.zyvo.backgroundTask.MyPlacesTask
import com.business.zyvo.model.HostMyPlacesModel
import com.business.zyvo.model.host.GetPropertyDetail

import com.business.zyvo.fragment.both.completeProfile.model.CompleteProfileReq
import com.business.zyvo.fragment.both.faq.model.FaqModel
import com.business.zyvo.model.MyBookingsModel
import com.business.zyvo.model.NotificationScreenModel
import com.business.zyvo.model.host.HostReviewModel
import com.business.zyvo.model.host.PaginationModel

import com.business.zyvo.model.host.PropertyDetailsSave
import com.business.zyvo.model.host.hostdetail.HostDetailModel
import com.business.zyvo.remote.ZyvoApi
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.ErrorDialog.createMultipartList
import com.business.zyvo.utils.ErrorDialog.createRequestBody
import com.business.zyvo.utils.ErrorDialog.toMultiPartFile
import com.business.zyvo.utils.ErrorHandler
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class ZyvoRepositoryImpl @Inject constructor(private val api: ZyvoApi) : ZyvoRepository {

    override suspend fun signUpPhoneNumber(
        phoneNumber: String,
        code: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.signUpPhoneNumber(
                phoneNumber, code,
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            emit(AuthTask.processSignUpData(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(e.message!!))
                    }
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
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(AuthTask.processLoginData(resp))
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

    override suspend fun otpVerifyLoginPhone(
        userId: String,
        otp: String
    ): Flow<NetworkResult<JsonObject>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.otpVerifyLoginPhone(
                userId,
                otp,
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(AuthTask.processData(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun otpVerifySignupPhone(
        tempId: String,
        otp: String
    ): Flow<NetworkResult<JsonObject>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.otpVerifySignupPhone(
                tempId,
                otp,
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(AuthTask.processData(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun loginEmail(
        email: String,
        password: String
    ): Flow<NetworkResult<JsonObject>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.loginEmail(
                email,
                password,
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(AuthTask.processData(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun signupEmail(
        email: String,
        password: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.signupEmail(email, password).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            emit(AuthTask.processSignUpData(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun otpVerifySignupEmail(
        tempId: String,
        otp: String
    ): Flow<NetworkResult<JsonObject>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.otpVerifySignupEmail(
                tempId,
                otp,
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(AuthTask.processData(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun forgotPassword(
        email: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.forgotPassword(
                email
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(AuthTask.processLoginData(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun otpVerifyForgotPassword(
        userId: String,
        otp: String
    ): Flow<NetworkResult<JsonObject>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.otpVerifyForgotPassword(
                userId,
                otp,
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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


    override suspend fun resetPassword(
        userId: String,
        password: String,
        passwordConfirmation: String
    ): Flow<NetworkResult<JsonObject>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.resetPassword(
                userId,
                password,
                passwordConfirmation
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun getUserProfile(
        userId: String
    ): Flow<NetworkResult<JsonObject>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.getUserProfile(
                userId
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(AuthTask.processData(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun addPropertyData(property: PropertyDetailsSave): Flow<NetworkResult<Pair<String, Int>>> =
        flow {

            try {
                api.addProperty(property).apply {

                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("success") &&
                                resp.get("success").asBoolean
                            ) {
                                emit(
                                    NetworkResult.Success(
                                        Pair<String, Int>(
                                            "Property Added Successfully",
                                            1
                                        )
                                    )
                                )
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message") ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
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

    override suspend fun completeProfile(completeProfileReq: CompleteProfileReq):
            Flow<NetworkResult<Pair<String, String>>> = flow {
        try {
            var multipart: MultipartBody.Part? = null
            if (completeProfileReq.bytes != null) {
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
            api.completeProfile(
                user_id,
                first_name, last_name, about_me, where_live,
                works, languages, hobbies, pets, street_address, city, state, zip_code, multipart
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(Pair("Profile update Successfully", "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun phoneVerification(
        userId: String,
        code: String,
        number: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.phoneVerification(userId, code, number).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            emit(AuthTask.processLoginData(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(e.message!!))
                    }
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

    override suspend fun emailVerification(
        userId: String,
        email: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.emailVerification(userId, email).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            emit(AuthTask.processLoginData(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(e.message!!))
                    }
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

    override suspend fun otpVerifyEmailVerification(
        userId: String, otp: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.otpVerifyEmailVerification(userId, otp).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            emit(
                                NetworkResult.Success(
                                    Pair<String, String>(
                                        "Email verified successfully.",
                                        "200"
                                    )
                                )
                            )
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(e.message!!))
                    }
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

    override suspend fun otpVerifyPhoneVerification(
        userId: String,
        otp: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.otpVerifyPhoneVerification(
                userId,
                otp,
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            emit(
                                NetworkResult.Success(
                                    Pair<String, String>(
                                        "Phone Number verified successfully.",
                                        "200"
                                    )
                                )
                            )
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(e.message!!))
                    }
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

    override suspend fun uploadProfileImage(
        userId: String,
        bytes: ByteArray
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            var multipart: MultipartBody.Part? = null
            if (bytes != null) {
                multipart = toMultiPartFile("profile_image", "image.jpg", bytes)
            }
            val user_id: RequestBody = createRequestBody(userId)
            api.uploadProfileImage(user_id, multipart).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun addUpdateName(
        userId: String,
        first_name: String,
        last_name: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.addUpdateName(
                userId, first_name,
                last_name
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun addAboutMe(
        userId: String,
        about_me: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.addAboutme(userId, about_me).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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


    override suspend fun deleteLivePlace(
        userId: String,
        index: Int
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.deleteLivePlace(userId, index).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun addMyWork(
        userId: String,
        workName: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.addMyWork(userId, workName).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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


    override suspend fun addCity(
        userId: String,
        city: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.addCity(userId, city).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun addState(
        userId: String,
        state: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.addState(userId, state).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun addZipCode(
        userId: String,
        zip_code: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.addZipCode(userId, zip_code).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun updatePassword(
        userId: String,
        password: String,
        password_confirmation: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.updatePassword(userId, password, password_confirmation).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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


    override suspend fun getHostBookingList(userid: Int): Flow<NetworkResult<MutableList<MyBookingsModel>>> =
        flow {
            emit(NetworkResult.Loading())
            try {
                Log.d("TESTING", "Host Booking Api is here")

                api.getHostBookingList(userid).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("success") && resp.get("success").asBoolean) {
                                val arr = resp.get("data").asJsonArray
                                val result = mutableListOf<MyBookingsModel>()
                                arr.forEach {
                                    val model: MyBookingsModel =
                                        Gson().fromJson(it.toString(), MyBookingsModel::class.java)
                                    result.add(model)
                                }
                                Log.d("TESTING", "Size of Booking Array is " + result.size)

                                emit(NetworkResult.Success(result))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message") ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
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


    override suspend fun getPropertyList(
        userId: Int,
        latitude: Double?,
        longitude: Double?
    ): Flow<NetworkResult<Pair<MutableList<HostMyPlacesModel>, String>>> = flow {


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
                api.totalEarning(userId, "total")
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

                        if (response1.body() != null) {
                            response1.body()?.let { resp ->
                                if (resp.has("success") && resp.get("success").asBoolean) {
                                    var obj = resp.get("data").asJsonObject
                                    var amountStr = obj.get("amount").asString
                                    emit(
                                        NetworkResult.Success(
                                            Pair<MutableList<HostMyPlacesModel>, String>(
                                                v1,
                                                amountStr
                                            )
                                        )
                                    )
                                } else {
                                    emit(
                                        NetworkResult.Success(
                                            Pair<MutableList<HostMyPlacesModel>, String>(
                                                v1,
                                                "0.00"
                                            )
                                        )
                                    )
                                }
                            }
                                ?: emit(
                                    NetworkResult.Success(
                                        Pair<MutableList<HostMyPlacesModel>, String>(
                                            v1,
                                            "0.00"
                                        )
                                    )
                                )

                        } else {
                            emit(
                                NetworkResult.Success(
                                    Pair<MutableList<HostMyPlacesModel>, String>(
                                        v1,
                                        "0.00"
                                    )
                                )
                            )
                        }

                    } else {
                        emit(NetworkResult.Error(resp.get("message").asString))
                    }
                }
                    ?: emit(NetworkResult.Error("There was an unknown error. Check your connection, and try again."))
            } else {
                // Handle the error from the API response
                try {
                    val jsonObj = response.errorBody()?.string()?.let { JSONObject(it) }
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

    override suspend fun getPropertyDetails(propertyId: Int): Flow<NetworkResult<GetPropertyDetail>> =
        flow {
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

    override suspend fun earning(hostId: Int, type: String): Flow<NetworkResult<String>> = flow {
        try {
            api.totalEarning(hostId, type).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            var obj = resp.get("data").asJsonObject
                            var amountStr = obj.get("amount").asString
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

    override suspend fun updateProperty(requestBody: PropertyDetailsSave): Flow<NetworkResult<String>> =
        flow {
            try {
                api.updatePropertyDetail(requestBody).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("success") && resp.get("success").asBoolean) {
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


    override suspend fun addLivePlace(
        userId: String,
        place_name: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        try {
            emit(NetworkResult.Loading())
            api.addLivePlace(userId, place_name).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun deleteLivePlace(
        userId: String,
        index: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.deleteLivePlace(userId, index).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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


    override suspend fun getPrivacyPolicy(): Flow<NetworkResult<String>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.getPrivacyPolicy().apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(AuthTask.processPrivacyData(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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


    override suspend fun getTermCondition(): Flow<NetworkResult<String>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.getTermCondition().apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(AuthTask.processTermAndConditionData(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun feedback(
        user_id: String,
        type: String,
        details: String
    ): Flow<NetworkResult<String>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.feedback(user_id, type, details).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {

                            if (resp.has("message") && !resp.get("message").isJsonNull)
                                emit(NetworkResult.Success(resp.get("message").asString))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun getFaq(): Flow<NetworkResult<MutableList<FaqModel>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.getFaq().apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {

                            if (resp.has("data") && !resp.get("data").isJsonNull) {
                                val faqList = mutableListOf<FaqModel>()
                                resp.getAsJsonArray("data").forEach { element ->
                                    val jsonObj = element.asJsonObject
                                    val faq = FaqModel(
                                        question = jsonObj.get("question").asString,
                                        answer = jsonObj.get("answer").asString
                                    )
                                    faqList.add(faq)
                                }
                                emit(NetworkResult.Success(faqList))
                            }
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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


    override suspend fun contactUs(
        user_id: String,
        name: String,
        email: String,
        message: String
    ): Flow<NetworkResult<String>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.contactUs(user_id, name, email, message).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {

                            if (resp.has("message") && !resp.get("message").isJsonNull)
                                emit(NetworkResult.Success(resp.get("message").asString))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun getHelpCenter(
        user_id: String,
        user_type: String
    ): Flow<NetworkResult<JsonObject>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.getHelpCenter(user_id, user_type).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun getArticleDetails(article_id: String): Flow<NetworkResult<JsonObject>> =
        flow {
            emit(NetworkResult.Loading())
            try {
                api.getArticleDetails(article_id).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("success") &&
                                resp.get("success").asBoolean
                            ) {

                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message") ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
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

    override suspend fun getGuideDetails(guide_id: String): Flow<NetworkResult<JsonObject>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.getGuideDetails(guide_id).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {

                            emit(NetworkResult.Success(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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


    override suspend fun deleteMyWork(
        userId: String,
        work_index: Int
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.deleteMyWork(userId, work_index).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun addLanguage(
        userId: String,
        language_name: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.addLanguage(userId, language_name).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun deleteLanguage(
        userId: String,
        language_index: Int
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.deleteLanguage(userId, language_index).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun addHobbies(
        userId: String,
        hobbies_name: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.addHobbies(userId, hobbies_name).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun deleteHobbies(
        userId: String,
        index: Int
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.deleteHobbies(userId, index).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun addPets(
        userId: String,
        pet_name: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.addPets(userId, pet_name).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    emit(
                        NetworkResult.Error(
                            ErrorHandler.handleErrorBody(
                                this.errorBody()?.string()
                            )
                        )
                    )
                }
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(ErrorHandler.emitError(e)))
        }
    }

    override suspend fun deletePets(
        userId: String,
        index: Int
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.deletePets(userId, index).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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

    override suspend fun addStreetAddress(
        userId: String,
        street_address: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.addStreetAddress(userId, street_address).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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


    override suspend fun approveDeclineBooking(
        bookingId: Int,
        status: String,
        message: String,
        reason: String
    ): Flow<NetworkResult<String>> = flow {
        try {
            api.approveDeclineBooking(bookingId, status, message, reason).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            val obj = resp.get("message").asString
                            emit(NetworkResult.Success(obj))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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


    override suspend fun getHomeData(
        userId: String,
        latitude: String,
        longitude: String
    ): Flow<NetworkResult<JsonArray>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.getHomeData(
                userId, latitude, longitude
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(AuthTask.processDataArray(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {

                    emit(
                        NetworkResult.Error(
                            ErrorHandler.handleErrorBody(
                                this.errorBody()?.string()
                            )
                        )
                    )
                }
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(ErrorHandler.emitError(e)))
        }
    }


    override suspend fun hostBookingDetails(
        bookingId: Int,
        latitude: String?,
        longitude: String?
    ): Flow<NetworkResult<Pair<String, HostDetailModel>>> = flow {
        try {
            api.hostBookingDetails(bookingId, latitude, longitude).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            emit(HostDetailsTask.processPrivacyData(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    emit(
                        NetworkResult.Error(
                            ErrorHandler.handleErrorBody(
                                this.errorBody()?.string()
                            )
                        )
                    )
                }
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(ErrorHandler.emitError(e)))
        }
    }

    override suspend fun getWisList(
        userId: String
    ): Flow<NetworkResult<JsonArray>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.getWisList(
                userId
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(AuthTask.processDataArray(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
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


    override suspend fun createWishlist(
        userId: String,
        name: String,
        description: String,
        property_id: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.createWishlist(
                userId, name,
                description, property_id
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(e.message!!))
                    }
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


    override suspend fun deleteWishlist(
        userId: String,
        wishlist_id: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.deleteWishlist(
                userId,
                wishlist_id
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(e.message!!))
                    }
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

    override suspend fun removeItemFromWishlist(
        userId: String,
        property_id: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.removeItemFromWishlist(
                userId,
                property_id
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(e.message!!))
                    }
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


    override suspend fun saveItemInWishlist(
        userId: String,
        property_id: String,
        wishlist_id: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.saveItemInWishlist(
                userId,
                property_id,
                wishlist_id
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(e.message!!))
                    }
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

    override suspend fun getArticleList(search_term: String): Flow<NetworkResult<JsonObject>> =
        flow {
            emit(NetworkResult.Loading())
            try {
                api.getArticleList(search_term).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("success") && resp.get("success").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message") ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(e.message!!))
                        }
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

    override suspend fun getGuideList(search_term: String): Flow<NetworkResult<JsonObject>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.getGuideList(search_term).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            emit(NetworkResult.Success(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message") ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(e.message!!))
                    }
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


<<<<<<< HEAD
    override suspend fun propertyFilterReviews(
        propertyId :Int, filter: String, page :Int
    ) : Flow<NetworkResult<Pair<PaginationModel,MutableList<HostReviewModel>>>> = flow{
        try {
            api.propertyFilterReviews(propertyId, filter, page).apply {
=======
    override suspend fun getHomePropertyDetails(
        userId: String,propertyId :String
    ): Flow<NetworkResult<Pair<JsonObject, JsonObject>>>  = flow{
        emit(NetworkResult.Loading())
        try {
            api.getHomePropertyDetails(
                userId,propertyId).apply {
>>>>>>> shrawanbranch
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&&
                            resp.get("success").asBoolean) {
<<<<<<< HEAD
                            emit(BookingDetails.getReviewsData(resp))
=======
                            val response = api.filterPropertyReviews(propertyId,"highest_review","1")
                            if (response.isSuccessful) {
                                response.body()?.let { reviewResp ->
                                    // ✅ Emit both responses as Pair
                                    emit(NetworkResult.Success(Pair(resp, reviewResp)))
                                } ?: emit(NetworkResult.Error("Reviews response is empty"))
                            }else {
                                emit(NetworkResult.Error("Failed to load reviews"))
                            }
>>>>>>> shrawanbranch
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
<<<<<<< HEAD
                    emit(NetworkResult.Error(ErrorHandler.handleErrorBody(this.errorBody()?.string())))
                }
            }
        }
        catch (e: Exception) {
            emit(NetworkResult.Error(ErrorHandler.emitError(e)))
        }
    }

    override suspend fun getNotificationHost(userId: Int): Flow<NetworkResult<MutableList<NotificationScreenModel>>> =flow{
        try {
            api.getNotificationHost(userId).apply {
                if(isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            emit(BookingDetails.getNotificationHost(resp))
=======
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


    override suspend fun filterPropertyReviews(
        propertyId: String,filter :String,
        page :String
    ): Flow<NetworkResult<Pair<JsonArray, JsonObject>>>  = flow{
        emit(NetworkResult.Loading())
        try {
            api.filterPropertyReviews(
                propertyId,filter,page).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&&
                            resp.get("success").asBoolean) {
                            emit(AuthTask.processDataArrayAndObject(resp))
>>>>>>> shrawanbranch
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
<<<<<<< HEAD
                    emit(NetworkResult.Error(ErrorHandler.handleErrorBody(this.errorBody()?.string())))
                }
            }
        }
        catch (e: Exception) {
            emit(NetworkResult.Error(ErrorHandler.emitError(e)))
        }
    }

    override suspend fun deleteNotificationHost(
        userId: Int,
        notificationId: Int
    ): Flow<NetworkResult<String>> =flow{
        try {
            api.deleteNotificationHost(userId,notificationId).apply {
                if(isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            var obj = resp.get("message").asString
                            emit(NetworkResult.Success(obj))
=======
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

    override suspend fun getUserCards(
        userId: String
    ):  Flow<NetworkResult<JsonObject>>  = flow{
        emit(NetworkResult.Loading())
        try {
            api.getUserCards(
                userId).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success")&&
                            resp.get("success").asBoolean) {
                            emit(AuthTask.processData(resp))
>>>>>>> shrawanbranch
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
<<<<<<< HEAD
                } else {
                    emit(NetworkResult.Error(ErrorHandler.handleErrorBody(this.errorBody()?.string())))
                }
            }
        }
        catch (e: Exception) {
            emit(NetworkResult.Error(ErrorHandler.emitError(e)))
        }

    }

    override suspend fun hostReportViolation(
        userId: Int,
        bookingId: Int,
        propertyId: Int,
        reportReasonId: Int,
        additionalDetails: String
    ) {

    }


    override suspend fun reportListReason() : Flow<NetworkResult<MutableList<Pair<Int,String>>>> = flow{
       try {
           api.reportListReason().apply {
               if(isSuccessful) {
                   body()?.let { resp ->
                       if (resp.has("success") && resp.get("success").asBoolean) {
                           var obj = resp.get("message").asString
                           val data = resp.get("data").asJsonArray
                           val result = mutableListOf<Pair<Int,String>>()
                           data.forEach {
                                  var newObj = it.asJsonObject
                                  var id = newObj.get("id").asInt
                                   var reason = newObj.get("reason").asString
                                   var p = Pair<Int,String>(id,reason)
                                    result.add(p)
                           }
                           emit(NetworkResult.Success<MutableList<Pair<Int,String>>>(result))
                       }
                       else {
                           emit(NetworkResult.Error(resp.get("message").asString))
                       }
                   } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
               } else {
                   emit(NetworkResult.Error(ErrorHandler.handleErrorBody(this.errorBody()?.string())))
               }
           }
       }
       catch (e: Exception) {
           emit(NetworkResult.Error(ErrorHandler.emitError(e)))
       }
    }


    override suspend fun hostReportViolationSend(
        userId: Int,
        bookingId: Int,
        propertyId: Int,
        reportReasonId: Int,
        additionalDetail: String
    ): Flow<NetworkResult<String>> = flow {
        try {
            api.reportListReason().apply {
                if(isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            var obj = resp.get("message").asString

                            emit(NetworkResult.Success<String>(obj))
                        }
                        else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    emit(NetworkResult.Error(ErrorHandler.handleErrorBody(this.errorBody()?.string())))
                }
            }
        }
        catch (e: Exception) {
            emit(NetworkResult.Error(ErrorHandler.emitError(e)))
        }

=======
                } else if (code()==400){
                   // emit(NetworkResult.Error("You have not added any payment method yet. Please add a card to proceed."))
                }else{
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
>>>>>>> shrawanbranch
    }


}