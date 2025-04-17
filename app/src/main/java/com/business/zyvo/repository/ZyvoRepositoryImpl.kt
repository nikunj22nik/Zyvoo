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
import com.business.zyvo.fragment.both.completeProfile.model.CompleteProfileReq
import com.business.zyvo.fragment.both.faq.model.FaqModel


import com.business.zyvo.model.ChannelListModel

import com.business.zyvo.fragment.both.notificationfragment.NotificationRootModel
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.BookingDetailModel
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.BookingModel
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.ReviewModel

import com.business.zyvo.model.HostMyPlacesModel
import com.business.zyvo.model.MyBookingsModel

import com.business.zyvo.model.NotificationScreenModel
import com.business.zyvo.model.StateModel
import com.business.zyvo.model.host.ChannelModel
import com.business.zyvo.model.host.CountryModel
import com.business.zyvo.model.host.HostReviewModel
import com.business.zyvo.model.host.PaginationModel


import com.business.zyvo.model.host.GetPropertyDetail

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
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException

import retrofit2.Response
import retrofit2.http.Path

import retrofit2.http.Field

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
        code: String,
        fcmToken :String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.loginPhoneNumber(
                phoneNumber,
                code,fcmToken,"android"
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
        otp: String,
        fcmToken :String
    ): Flow<NetworkResult<JsonObject>> = flow {
        emit(NetworkResult.Loading())
        try {
            Log.d("TESTING","FCM Token in VerifyLogin is :- "+fcmToken)
            api.otpVerifyLoginPhone(
                userId,
                otp,
                fcmToken, "android"
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
        otp: String,
        fcmToken :String
    ): Flow<NetworkResult<JsonObject>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.otpVerifySignupPhone(
                tempId,
                otp,
                fcmToken,
                "android"
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
        password: String,
        fcmToken :String
    ): Flow<NetworkResult<JsonObject>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.loginEmail(
                email,
                password,
                fcmToken,
                "android"
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
        otp: String,
        fcmToken :String
    ): Flow<NetworkResult<JsonObject>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.otpVerifySignupEmail(
                tempId,
                otp,
                fcmToken,
                "android"
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
                var multipart: MultipartBody.Part? = null
                if (completeProfileReq.bytes != null) {
                    multipart =
                        toMultiPartFile("profile_image", "image.jpg", completeProfileReq.bytes)

                }
                val user_id: RequestBody = createRequestBody(completeProfileReq.user_id.toString())
                val first_name: RequestBody = createRequestBody(completeProfileReq.first_name)
                val last_name: RequestBody = createRequestBody(completeProfileReq.last_name)
                val about_me: RequestBody = createRequestBody(completeProfileReq.about_me)
                val where_live: List<RequestBody> =
                    createMultipartList(completeProfileReq.where_live)
                val works: List<RequestBody> = createMultipartList(completeProfileReq.works)
                val languages: List<RequestBody> = createMultipartList(completeProfileReq.languages)
                val hobbies: List<RequestBody> = createMultipartList(completeProfileReq.hobbies)
                val pets: List<RequestBody> = createMultipartList(completeProfileReq.pets)
                val street_address: RequestBody =
                    createRequestBody(completeProfileReq.street_address)
                val city: RequestBody = createRequestBody(completeProfileReq.city)
                val state: RequestBody = createRequestBody(completeProfileReq.state)
                val zip_code: RequestBody = createRequestBody(completeProfileReq.zip_code)
                val identity_verify: RequestBody =
                    createRequestBody(completeProfileReq.identityVerified.toString())
                api.completeProfile(
                    user_id,
                    first_name,
                    last_name,
                    about_me,
                    where_live,
                    works,
                    languages,
                    hobbies,
                    pets,
                    street_address,
                    city,
                    state,
                    zip_code,
                    multipart,
                    identity_verify
                ).apply {

                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("success") &&
                                resp.get("success").asBoolean
                            ) {
                                emit(
                                    NetworkResult.Success(
                                        Pair(
                                            "Profile update Successfully",
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
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
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

    override suspend fun getPaymentMethods(userId: String): Flow<NetworkResult<Pair<String, String>>> =
        flow {
            try {
                api.getPaymentMethods(userId).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("success") &&
                                resp.get("success").asBoolean
                            ) {
                                emit(
                                    NetworkResult.Success(
                                        Pair(
                                            resp.get("message").asString,
                                            "200"
                                        )
                                    )
                                )
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


     override suspend fun getFilteredHomeData(
         userId: String?,
         latitude: String?,
         longitude: String?,
         place_type: String?,
         minimum_price: String?,
         maximum_price: String?,
         location: String?,
         date: String?,
         time: String?,
         people_count: String?,
         property_size: String?,
         bedroom: String?,
         bathroom: String?,
         instant_booking: String?,
         self_check_in: String?,
         allows_pets: String?,
         activities: List<String>?,
         amenities: List<String>?,
         languages: List<String>?
     ): Flow<NetworkResult<JsonArray>> = flow {
         emit(NetworkResult.Loading())
         try {
             api.getFilteredHomeData(
                 userId,
                 latitude,
                 longitude,
                 place_type,
                 minimum_price,
                 maximum_price,
                 location,
                 date,
                 time,
                 people_count,
                 property_size,
                 bedroom,
                 bathroom,
                 instant_booking,
                 self_check_in,
                 allows_pets,
                 activities,
                 amenities,
                 languages
             ).apply {
                 if (isSuccessful) {
                     body()?.let { resp ->
                         if (resp.has("success")&&
                             resp.get("success").asBoolean) {
                             emit(AuthTask.processDataArray(resp))
                         }
                         else {
                             emit(NetworkResult.Error(resp.get("message").asString))
                         }
                     } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                 }else {
                     emit(NetworkResult.Error(ErrorHandler.handleErrorBody(this.errorBody()?.string())))
                 }
             }
         }
         catch (e: Exception) {
             emit(NetworkResult.Error(ErrorHandler.emitError(e)))
         }
     }


    override suspend fun getBookingList(
        userId: String
    ): Flow<NetworkResult<MutableList<BookingModel>>> = flow {
        emit(NetworkResult.Loading())
        try {
            val response = api.bookingList(userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.has("success") && body.get("success").asBoolean) {
                    emit(AuthTask.processedData(body))
                } else {
                    emit(
                        NetworkResult.Error(
                            body?.get("message")?.asString ?: AppConstant.unKnownError
                        )
                    )
                }
            } else {
                emit(
                    NetworkResult.Error(
                        ErrorHandler.handleErrorBody(
                            response.errorBody()?.string()
                        )
                    )
                )
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(ErrorHandler.emitError(e)))
        }
    }

     override suspend fun getBookingDetailsList(
         userId: String,
         booking_id: Int, latitude:String,longitude:String
     ): Flow<NetworkResult<Pair<BookingDetailModel,JsonObject>>> = flow {
         emit(NetworkResult.Loading())
         try {
             val response = api.bookingDetailsList(userId,booking_id,
                 latitude, longitude)
             if (response.isSuccessful) {
                 val body = response.body()
                 if (body != null && body.has("success") && body.get("success").asBoolean) {
                     val data:JsonObject = body.getAsJsonObject("data")
                     val propertyId = data.get("property_id").asString
                     val response =
                         api.filterPropertyReviews(propertyId, "highest_review", "1")
                     if (response.isSuccessful) {
                         response.body()?.let { reviewResp ->
                             // ✅ Emit both responses as Pair
                             emit(NetworkResult.Success(Pair(AuthTask.processSingleDataChange(body)!!, reviewResp)))
                         } ?: emit(NetworkResult.Error("Reviews response is empty"))
                     } else {
                         emit(NetworkResult.Error("Failed to load reviews"))
                     }
                     // emit(AuthTask.processSingleData(body))
                 } else {
                     emit(NetworkResult.Error(body?.get("message")?.asString ?: AppConstant.unKnownError))
                 }
             } else {
                 emit(NetworkResult.Error(ErrorHandler.handleErrorBody(response.errorBody()?.string())))
             }
         } catch (e: Exception) {
             emit(NetworkResult.Error(ErrorHandler.emitError(e)))
         }
     }




    override suspend fun reviewPublish(
        userId: String,
        booking_id: String,
        property_id: String,
        response_rate: String,
        communication: String,
        on_time: String,
        review_message: String
    ): Flow<NetworkResult<ReviewModel>> = flow {
        emit(NetworkResult.Loading())
        try {
            val response = api.getReviewPublish(
                userId,
                booking_id,
                property_id,
                response_rate,
                communication,
                on_time,
                review_message
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.has("success") && body.get("success").asBoolean) {
                    Log.d("value44444", "List $body")

                    emit(AuthTask.reviewData(body))
                } else {
                    emit(
                        NetworkResult.Error(
                            body?.get("message")?.asString ?: AppConstant.unKnownError
                        )
                    )
                }
            } else {
                emit(
                    NetworkResult.Error(
                        ErrorHandler.handleErrorBody(
                            response.errorBody()?.string()
                        )
                    )
                )
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(ErrorHandler.emitError(e)))
        }
    }


    override suspend fun verifyIdentity(
        userId: String,
        identity_verify: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        try {
            api.verifyIdentity(userId, identity_verify).apply {
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

    override suspend fun getGuestNotification(userId: String): Flow<NetworkResult<MutableList<NotificationRootModel>>> =
        flow {
            try {
                val response = api.getGuestNotification(userId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.has("success") && body.get("success").asBoolean) {
                        emit(AuthTask.notificationData(body))

                    } else {
                        emit(
                            NetworkResult.Error(
                                body?.get("message")?.asString ?: AppConstant.unKnownError
                            )
                        )
                    }
                } else {
                    emit(
                        NetworkResult.Error(
                            ErrorHandler.handleErrorBody(
                                response.errorBody()?.string()
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                emit(NetworkResult.Error(ErrorHandler.emitError(e)))
            }
        }

    override suspend fun getMarkGuestNotification(
        userId: String,
        notification_id: Int
    ): Flow<NetworkResult<NotificationRootModel>> = flow {
        try {
            val response = api.getMarkGuestNotification(userId, notification_id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.has("success") && body.get("success").asBoolean) {
                    Log.d("@@@@", "$body")
                    emit(AuthTask.markNotification(body))
                    Log.d("@@@@@", "$body")
                } else {
                    emit(
                        NetworkResult.Error(
                            body?.get("message")?.asString ?: AppConstant.unKnownError
                        )
                    )
                }
            } else {
                emit(
                    NetworkResult.Error(
                        ErrorHandler.handleErrorBody(
                            response.errorBody()?.string()
                        )
                    )
                )
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(ErrorHandler.emitError(e)))
        }
    }

    override suspend fun getRemoveGuestNotification(
        userId: String,
        notification_id: Int
    ): Flow<NetworkResult<NotificationRootModel>> = flow {
        try {
            val response = api.getRemoveGuestNotification(userId, notification_id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.has("success") && body.get("success").asBoolean) {
                    Log.d("@@@@", "$body")
                    emit(AuthTask.markNotification(body))
                    Log.d("@@@@@", "$body")
                } else {
                    emit(
                        NetworkResult.Error(
                            body?.get("message")?.asString ?: AppConstant.unKnownError
                        )
                    )
                }
            } else {
                emit(
                    NetworkResult.Error(
                        ErrorHandler.handleErrorBody(
                            response.errorBody()?.string()
                        )
                    )
                )
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(ErrorHandler.emitError(e)))
        }
    }

    override suspend fun getSocialLogin(
        fname: String,
        lname: String,
        email: String,
        social_id: String,
        fcm_token: String,
        device_type: String
    ): Flow<NetworkResult<JsonObject>> = flow {
        try {
            api.getSocialLogin(
                fname, lname, email, social_id, fcm_token, device_type
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


//    override suspend fun addZipCode(
//        userId: String,
//        zip_code: String
//    ): Flow<NetworkResult<Pair<String, String>>> = flow {
//        emit(NetworkResult.Loading())
//        try {
//            api.addZipCode(userId, zip_code).apply {
//                if (isSuccessful) {
//                    body()?.let { resp ->
//                        if (resp.has("success") &&
//                            resp.get("success").asBoolean
//                        ) {
//                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
//                        } else {
//                            emit(NetworkResult.Error(resp.get("message").asString))
//                        }
//                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
//                } else {
//                    try {
//                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
//                        emit(
//                            NetworkResult.Error(
//                                jsonObj?.getString("message") ?: AppConstant.unKnownError
//                            )
//                        )
//                    } catch (e: JSONException) {
//                        e.printStackTrace()
//                        emit(NetworkResult.Error(AppConstant.unKnownError))
//                    }
//                }
//            }
//        } catch (e: HttpException) {
//            Log.e(ErrorDialog.TAG, "http exception - ${e.message}")
//            emit(NetworkResult.Error(e.message!!))
//        } catch (e: IOException) {
//            Log.e(ErrorDialog.TAG, "io exception - ${e.message} :: ${e.localizedMessage}")
//            emit(NetworkResult.Error(e.message!!))
//        } catch (e: Exception) {
//            Log.e(ErrorDialog.TAG, "exception - ${e.message} :: \n ${e.stackTraceToString()}")
//            emit(NetworkResult.Error(e.message!!))
//        }
//    }
//
//    override suspend fun updatePassword(
//        userId: String,
//        password: String,
//        password_confirmation: String
//    ): Flow<NetworkResult<Pair<String, String>>> = flow {
//        emit(NetworkResult.Loading())
//        try {
//            api.updatePassword(userId, password, password_confirmation).apply {
//                if (isSuccessful) {
//                    body()?.let { resp ->
//                        if (resp.has("success") &&
//                            resp.get("success").asBoolean
//                        ) {
//                            emit(NetworkResult.Success(Pair(resp.get("message").asString, "200")))
//                        } else {
//                            emit(NetworkResult.Error(resp.get("message").asString))
//                        }
//                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
//                } else {
//                    try {
//                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
//                        emit(
//                            NetworkResult.Error(
//                                jsonObj?.getString("message") ?: AppConstant.unKnownError
//                            )
//                        )
//                    } catch (e: JSONException) {
//                        e.printStackTrace()
//                        emit(NetworkResult.Error(AppConstant.unKnownError))
//                    }
//                }
//            }
//        } catch (e: HttpException) {
//            Log.e(ErrorDialog.TAG, "http exception - ${e.message}")
//            emit(NetworkResult.Error(e.message!!))
//        } catch (e: IOException) {
//            Log.e(ErrorDialog.TAG, "io exception - ${e.message} :: ${e.localizedMessage}")
//            emit(NetworkResult.Error(e.message!!))
//        } catch (e: Exception) {
//            Log.e(ErrorDialog.TAG, "exception - ${e.message} :: \n ${e.stackTraceToString()}")
//            emit(NetworkResult.Error(e.message!!))
//        }
//    }
//
//
//    override suspend fun getHostBookingList(userid: Int): Flow<NetworkResult<MutableList<MyBookingsModel>>> =
//        flow {
//            emit(NetworkResult.Loading())
//            try {
//                Log.d("TESTING", "Host Booking Api is here")
//
//                api.getHostBookingList(userid).apply {
//                    if (isSuccessful) {
//                        body()?.let { resp ->
//                            if (resp.has("success") && resp.get("success").asBoolean) {
//                                val arr = resp.get("data").asJsonArray
//                                val result = mutableListOf<MyBookingsModel>()
//                                arr.forEach {
//                                    val model: MyBookingsModel =
//                                        Gson().fromJson(it.toString(), MyBookingsModel::class.java)
//                                    result.add(model)
//                                }
//                                Log.d("TESTING", "Size of Booking Array is " + result.size)
//
//                                emit(NetworkResult.Success(result))
//                            } else {
//                                emit(NetworkResult.Error(resp.get("message").asString))
//                            }
//                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
//                    } else {
//                        try {
//                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
//                            emit(
//                                NetworkResult.Error(
//                                    jsonObj?.getString("message") ?: AppConstant.unKnownError
//                                )
//                            )
//                        } catch (e: JSONException) {
//                            e.printStackTrace()
//                            emit(NetworkResult.Error(AppConstant.unKnownError))
//                        }
//                    }
//                }
//            } catch (e: HttpException) {
//                Log.e(ErrorDialog.TAG, "http exception - ${e.message}")
//                emit(NetworkResult.Error(e.message!!))
//            } catch (e: IOException) {
//                Log.e(ErrorDialog.TAG, "io exception - ${e.message} :: ${e.localizedMessage}")
//                emit(NetworkResult.Error(e.message!!))
//            } catch (e: Exception) {
//                Log.e(ErrorDialog.TAG, "exception - ${e.message} :: \n ${e.stackTraceToString()}")
//                emit(NetworkResult.Error(e.message!!))
//            }
//        }
//

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


    override suspend fun getPrivacyPolicy(): Flow<NetworkResult<Pair<String,String>>> = flow {
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


    override suspend fun getTermCondition(): Flow<NetworkResult<Pair<String,String>>> = flow {
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

    override suspend fun propertyFilterReviews(
        propertyId: Int,
        filter: String,
        page: Int
    ): Flow<NetworkResult<Pair<PaginationModel, MutableList<HostReviewModel>>>> = flow {
        try {
            api.propertyFilterReviews(propertyId, filter, page).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(BookingDetails.getReviewsData(resp))
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


    override suspend fun getHomePropertyDetails(
        userId: String, propertyId: String
    ): Flow<NetworkResult<Pair<JsonObject, JsonObject>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.getHomePropertyDetails(
                userId, propertyId
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            val response =
                                api.filterPropertyReviews(propertyId, "highest_review", "1")
                            if (response.isSuccessful) {
                                response.body()?.let { reviewResp ->
                                    // ✅ Emit both responses as Pair
                                    emit(NetworkResult.Success(Pair(resp, reviewResp)))
                                } ?: emit(NetworkResult.Error("Reviews response is empty"))
                            } else {
                                emit(NetworkResult.Error("Failed to load reviews"))
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


    override suspend fun filterPropertyReviews(
        propertyId: String, filter: String,
        page: String
    ): Flow<NetworkResult<Pair<JsonArray, JsonObject>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.filterPropertyReviews(
                propertyId, filter, page
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
                            emit(AuthTask.processDataArrayAndObject(resp))
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

    override suspend fun getUserCards(
        userId: String
    ): Flow<NetworkResult<JsonObject>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.getUserCards(
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
                } else if (code() == 400) {
                    emit(NetworkResult.Error("You have not added any payment method yet. Please add a card to proceed."))
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

    override suspend fun logout(userId: String): Flow<NetworkResult<String>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.logout(
                userId
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") &&
                            resp.get("success").asBoolean
                        ) {
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

    override suspend fun propertyBookingDetails(
        property_id: String,
        user_id: String,
        start_date: String,
        end_date: String,
        latitude: String,
        longitude: String
    ): Flow<NetworkResult<JsonObject>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.propertyBookingDetails(
                property_id,
                user_id,
                start_date,
                end_date,
                latitude,
                longitude
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

    override suspend fun togglePropertyBooking(
        property_id: String,
        user_id: String
    ): Flow<NetworkResult<JsonObject>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.togglePropertyBooking(
                property_id,
                user_id
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


    override suspend fun getNotificationHost(userId: Int): Flow<NetworkResult<MutableList<NotificationScreenModel>>> =
        flow {
            try {
                api.getNotificationHost(userId).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("success") && resp.get("success").asBoolean) {
                                emit(BookingDetails.getNotificationHost(resp))
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


    override suspend fun deleteNotificationHost(
        userId: Int,
        notificationId: Int
    ): Flow<NetworkResult<String>> = flow {
        try {
            api.deleteNotificationHost(userId, notificationId).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            var obj = resp.get("message").asString
                            emit(NetworkResult.Success(obj))
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


    override suspend fun hostReportViolation(
        userId: Int,
        bookingId: Int,
        propertyId: Int,
        reportReasonId: Int,
        additionalDetails: String
    ) {

    }


    override suspend fun reportListReason(): Flow<NetworkResult<MutableList<Pair<Int, String>>>> =
        flow {
            try {
                api.reportListReason().apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("success") && resp.get("success").asBoolean) {
                                var obj = resp.get("message").asString
                                val data = resp.get("data").asJsonArray
                                val result = mutableListOf<Pair<Int, String>>()
                                data.forEach {
                                    var newObj = it.asJsonObject
                                    var id = newObj.get("id").asInt
                                    var reason = newObj.get("reason").asString
                                    var p = Pair<Int, String>(id, reason)
                                    result.add(p)
                                }
                                emit(
                                    NetworkResult.Success<MutableList<Pair<Int, String>>>(
                                        result
                                    )
                                )
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


    override suspend fun hostReportViolationSend(
        userId: Int, bookingId: Int, propertyId: Int, reportReasonId: Int,
        additionalDetail: String
    ): Flow<NetworkResult<String>> = flow {
        try {
            api.hostReportViolationSend(userId,bookingId,propertyId,reportReasonId,additionalDetail).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            var obj = resp.get("message").asString

                            emit(NetworkResult.Success<String>(obj))
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


    override suspend fun reviewGuest(
        userId: Int, bookingId: Int, propertyId: Int, responseRate: Int, communication: Int,
        onTime: Int, reviewMessage: String
    ): Flow<NetworkResult<String>> = flow {
        try {
            api.reviewGuest(
                userId,
                bookingId,
                propertyId,
                responseRate,
                communication,
                onTime,
                reviewMessage
            ).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            var obj = resp.get("message").asString
                            emit(NetworkResult.Success<String>(obj))
                        }
                    }
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

    //Shrawan Call Api
    override suspend fun sameAsMailingAddress(
        userId: String
    ): Flow<NetworkResult<JsonObject>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.sameAsMailingAddress(
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

    override suspend fun saveCardStripe(
        userId: String,
        token_stripe: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.saveCardStripe(
                userId, token_stripe
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


    override suspend fun getChatToken(userId: Int, role: String): Flow<NetworkResult<String>> =
        flow {
            try {
                api.getChatToken(userId, role).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("success") && resp.get("success").asBoolean) {
                                var obj = resp.get("data").asJsonObject
                                var token = obj.get("token").asString
                                Log.d("TESTING_TOKEN","Token inside api "+token)
                                emit(NetworkResult.Success<String>(token))
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

    override suspend fun setPreferredCard(
        userId: String,
        card_id: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.setPreferredCard(
                userId,
                card_id
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

    override suspend fun joinChatChannel(
        senderId: Int, receiverId: Int, groupChannel: String, user_type: String
    ): Flow<NetworkResult<ChannelModel>> = flow {
        try {
            api.joinChatChannel(senderId, receiverId, groupChannel, user_type).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            var obj = resp.get("data").asJsonObject
                            val model: ChannelModel =
                                Gson().fromJson(obj.toString(), ChannelModel::class.java)
                            emit(NetworkResult.Success<ChannelModel>(model))
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


     override suspend fun bookProperty(
         userId : String,
         property_id : String,
         booking_date : String,
         booking_start : String,
         booking_end : String,
         booking_amount : String,
         total_amount : String,
         customer_id : String,
         card_id : String,
         addons: Map<String, String>,
         service_fee : String,
         tax : String,
         discount_amount : String
     ): Flow<NetworkResult<JsonObject>> = flow {
         emit(NetworkResult.Loading())
         try {
             api.bookProperty(
                 userId,
                 property_id,
                 booking_date,
                 booking_start,
                 booking_end,
                 booking_amount,
                 total_amount,
                 customer_id,
                 card_id,
                 addons,
                 service_fee,
                 tax,
                 discount_amount
             ).apply {
                 if (isSuccessful) {
                     body()?.let { resp ->
                         if (resp.has("success") && resp.get("success").asBoolean) {
                             emit(AuthTask.processData(resp))
                         } else {
                             emit(NetworkResult.Error(resp.get("message").asString))
                         }
                     } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                 } else {

                     emit(NetworkResult.Error(ErrorHandler.handleErrorBody(this.errorBody()?.string())))
                 }
             }
         } catch (e: Exception) {
             emit(NetworkResult.Error(ErrorHandler.emitError(e)))
         }
     }

    override suspend fun getUserChannel(
        userId: Int,
        type: String,
        archive_status:String
    ): Flow<NetworkResult<MutableList<ChannelListModel>>> = flow {
        try {
            api.getUserChannel(userId, type,archive_status).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            var obj = resp.get("data").asJsonArray

                            var resultList = mutableListOf<ChannelListModel>()

                            obj.forEach {
                                val model: ChannelListModel =
                                    Gson().fromJson(it.toString(), ChannelListModel::class.java)
                                resultList.add(model)
                            }
                            emit(NetworkResult.Success<MutableList<ChannelListModel>>(resultList))
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


    override suspend fun reportViolation(
        userId: String,
        booking_id: String,
        property_id: String,
        report_reasons_id: String,
        additional_details: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.reportViolation(
                userId,
                booking_id,
                property_id,
                report_reasons_id,
                additional_details
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


    override suspend fun listReportReasons(): Flow<NetworkResult<JsonArray>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.listReportReasons().apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
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


    override suspend fun cancelBooking(
        userId: String,
        booking_id: String
    ): Flow<NetworkResult<Pair<String, String>>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.cancelBooking(
                userId,
                booking_id
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

     override suspend fun addPayOut(
         userId: RequestBody,
         firstName: RequestBody,
         lastName: RequestBody,
         email: RequestBody,
         phoneNumber: RequestBody,
         dobList: List<MultipartBody.Part>,
         idType: RequestBody,
         ssnLast4: RequestBody,
         idNumber: RequestBody,
         address: RequestBody,
         country: RequestBody,
         state: RequestBody,
         city: RequestBody,
         postalCode: RequestBody,
         bankName: RequestBody,
         accountHolderName: RequestBody,
         accountNumber: RequestBody,
         accountNumberConfirmation: RequestBody,
         routingProperty: RequestBody,
         bankProofType: RequestBody,
         bank_proof_document: MultipartBody.Part?,
         verification_document_front: MultipartBody.Part?,
         verification_document_back: MultipartBody.Part?

     ): Flow<NetworkResult<String>> = flow {
         emit(NetworkResult.Loading())
         try {
             api.addPayoutBank(
                 userId,
                 firstName,
                 lastName,
                 email,
                 phoneNumber,
                 dobList,
                 idType,
                 ssnLast4,
                 idNumber,
                 address,
                 country,
                 state,
                 city,
                 postalCode,
                 bankName,
                 accountHolderName,
                 accountNumber,
                 accountNumberConfirmation,
                 routingProperty,
                 bankProofType,
                 bank_proof_document,
                 verification_document_front,
                 verification_document_back
             ).apply {
                 if (isSuccessful) {
                     body()?.let { resp ->
                         if (resp.has("success") && resp.get("success").asBoolean) {
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


     override suspend fun getBookingExtensionTimeAmount(
         userId : String,
         booking_id : String,
         extension_time : String,
         service_fee : String,
         tax : String,
         cleaning_fee:String,
         extension_total_amount : String,
         extension_booking_amount : String,
         discount_amount : String
     ): Flow<NetworkResult<JsonObject>> = flow {
         emit(NetworkResult.Loading())
         try {
             api.getBookingExtensionTimeAmount(
                 userId,
                 booking_id,
                 extension_time,
                 service_fee,
                 tax,
                 cleaning_fee,
                 extension_total_amount,
                 extension_booking_amount,
                 discount_amount
             ).apply {
                 if (isSuccessful) {
                     body()?.let { resp ->
                         if (resp.has("success") && resp.get("success").asBoolean) {
                             emit(AuthTask.processData(resp))
                         } else {
                             emit(NetworkResult.Error(resp.get("message").asString))
                         }
                     } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                 } else {

                     emit(NetworkResult.Error(ErrorHandler.handleErrorBody(this.errorBody()?.string())))
                 }
             }
         } catch (e: Exception) {
             emit(NetworkResult.Error(ErrorHandler.emitError(e)))
         }
     }

     override suspend fun getHomeDataSearchFilter(
         user_id : String,
         latitude : String,
         longitude : String,
         date : String,
         hour : String,
         start_time : String,
         end_time : String,
         activity : String
     ): Flow<NetworkResult<JsonArray>> = flow {
         emit(NetworkResult.Loading())
         try {
             api.getHomeDataSearchFilter(
                 user_id, latitude, longitude,date,hour,start_time, end_time, activity
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



    override suspend fun getCountries(): Flow<NetworkResult<MutableList<CountryModel>>> = flow {
        try {
            api.getCountries().apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            var list = resp.get("data").asJsonArray
                            var result = mutableListOf<CountryModel>()

                            list.forEach {
                                val model: CountryModel =
                                    Gson().fromJson(it.toString(), CountryModel::class.java)
                                result.add(model)
                            }

                            emit(NetworkResult.Success(result))
                        }
                    }?:emit(NetworkResult.Error(AppConstant.unKnownError))
                }else {
                    emit(NetworkResult.Error(ErrorHandler.handleErrorBody(this.errorBody()?.string())))
                }
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(ErrorHandler.emitError(e)))
        }
    }




    override suspend fun getState( value: String) : Flow<NetworkResult<MutableList<StateModel>>> = flow {
        try {
            api.getValue(value).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            var list = resp.get("data").asJsonArray
                            var result = mutableListOf<StateModel>()

                            list.forEach {
                                val model: StateModel = Gson().fromJson(it.toString(), StateModel::class.java)
                                result.add(model)
                            }

                            emit(NetworkResult.Success(result))

                        }
                    }?:emit(NetworkResult.Error(AppConstant.unKnownError))
                }else {
                    emit(NetworkResult.Error(ErrorHandler.handleErrorBody(this.errorBody()?.string())))
                }
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(ErrorHandler.emitError(e)))
        }
    }


    override suspend fun getUserBookings(
        user_id : String,
        booking_date : String,
        booking_start : String
    ): Flow<NetworkResult<JsonObject>> = flow {
        emit(NetworkResult.Loading())
        try {
            api.getUserBookings(
                user_id,booking_date, booking_start).apply {
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



     override suspend fun getCityName(country:String, state :String)  :Flow<NetworkResult<MutableList<String>>> = flow{
        try {
            api.getCityName(country,state).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("success") && resp.get("success").asBoolean) {
                            var list = resp.get("data").asJsonArray
                            var result = mutableListOf<String>()

                            list.forEach {
                                var obj = it.asJsonObject
                                result.add(obj.get("name").asString)
                            }
                            Log.d("TESTING_REPO","Size of city list "+result.size.toString())

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


     override suspend fun filterPropertyReviewsHost(
         propertyId :Int, filter :String, page :Int
     ) : Flow<NetworkResult<Pair<JsonArray, JsonObject>>> = flow{
         try {
             api.filterPropertyReviewsHost(propertyId, filter, page).apply {
                 if (isSuccessful) {
                     body()?.let { resp ->
                         if (resp.has("success") && resp.get("success").asBoolean) {
                             var list = resp.get("data").asJsonArray
                             var obj = resp.get("pagination").asJsonObject
                             var p = Pair<JsonArray, JsonObject>(list, obj)
                             emit(NetworkResult.Success(p))
                         //  emit(NetworkResult.Success(result))
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
         }
             catch (e: HttpException) {
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

     override suspend fun addPayCard(
         userId: RequestBody,
         token: RequestBody,
         firstName: RequestBody,
         lastName: RequestBody,
         email: RequestBody,
         dobList: List<MultipartBody.Part>,
         ssnLast4: RequestBody,
         phoneNumber: RequestBody,
         address: RequestBody,
         city: RequestBody,
         state: RequestBody,
         country: RequestBody,
         postalCode: RequestBody,
         idType: RequestBody,
         idNumber: RequestBody,
         verification_document_front: MultipartBody.Part?,
         verification_document_back: MultipartBody.Part?
     ): Flow<NetworkResult<String>> = flow {
         emit(NetworkResult.Loading())
         try {
             api.addPayoutCard(
                 userId,
                 token,
                 firstName,
                 lastName,
                 email,
                 dobList,
                 ssnLast4,
                 phoneNumber,
                 address,
                 city,
                 state,
                 country,
                 postalCode,
                 idType,
                 idNumber,
                 verification_document_front,
                 verification_document_back
             ).apply {
                 if (isSuccessful) {
                     body()?.let { resp ->
                         if (resp.has("success") && resp.get("success").asBoolean) {
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

     override suspend fun getPayoutMethods(userId: String): Flow<NetworkResult<JsonObject>>
             = flow {
         emit(NetworkResult.Loading())
         try {
             api.getPayoutMethods(
                 userId
             ).apply {
                 if (isSuccessful) {
                     body()?.let { resp ->
                         if (resp.has("success") && resp.get("success").asBoolean) {
                             emit(NetworkResult.Success(resp))

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

     override suspend fun setPrimaryPayoutMethod(
         userId: String,
         payoutMethodId: String
     ): Flow<NetworkResult<String>>
             = flow {
         emit(NetworkResult.Loading())
         try {
             api.setPrimaryPayoutMethod(
                 userId,payoutMethodId
             ).apply {
                 if (isSuccessful) {
                     body()?.let { resp ->
                         if (resp.has("success") && resp.get("success").asBoolean) {
                             if (resp.has("message") &&  !resp.get("message").isJsonNull){
                                 emit(NetworkResult.Success(resp.get("message").asString))
                             }


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

     override suspend fun deletePayoutMethod(
         userId: String,
         payoutMethodId: String
     ): Flow<NetworkResult<String>> = flow {
         emit(NetworkResult.Loading())
         try {
             api.deletePayoutMethod(
                 userId,payoutMethodId
             ).apply {
                 if (isSuccessful) {
                     body()?.let { resp ->
                         if (resp.has("success") && resp.get("success").asBoolean) {
                             if (resp.has("message") &&     !resp.get("message").isJsonNull) {
                                 emit(NetworkResult.Success(resp.get("message").asString))
                             }

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

     override suspend fun getHostUnreadBookings(
         @Field("user_id") userId :Int
     ) : Flow<NetworkResult<Int>> = flow{
         try {
             api.getHostUnreadBookings(userId).apply {
                 if (isSuccessful) {
                     body()?.let { resp ->
                         if (resp.has("success") && resp.get("success").asBoolean) {
                             var obj = resp.get("data").asJsonObject

                             if(obj.has("unread_booking_count")){
                               emit(NetworkResult.Success(obj.get("unread_booking_count").asInt))
                             }
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

     override suspend fun markHostBooking(@Field("user_id") userId :Int) : Flow<NetworkResult<String>>  = flow {
         try {
             api.markHostBooking(userId).apply {
                 if (isSuccessful) {
                     body()?.let { resp ->
                         if (resp.has("success") && resp.get("success").asBoolean) {
                             emit(NetworkResult.Success(resp.get("message").asString))
                         } else {
                             emit(NetworkResult.Error(resp.get("message").asString))
                         }
                     } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                 }
                 else {
                     emit(NetworkResult.Error(ErrorHandler.handleErrorBody(this.errorBody()?.string())))
                 }
             }
         } catch (e: Exception) {
             emit(NetworkResult.Error(ErrorHandler.emitError(e)))
         }

     }

     override suspend fun paymentWithdrawalList(
         userId: String,
         startDate: String,
         endDate: String,
         filterStatus: String
     ): Flow<NetworkResult<JsonObject>> = flow {
         emit(NetworkResult.Loading())
         try {
             api.paymentWithdrawalList(
                 userId,startDate,endDate,filterStatus
             ).apply {
                 if (isSuccessful) {
                     body()?.let { resp ->
                         if (resp.has("success") && resp.get("success").asBoolean) {

                                 emit(NetworkResult.Success(resp))

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

     override suspend fun payoutBalance(userId: String): Flow<NetworkResult<Pair<String, String>>> = flow {
         emit(NetworkResult.Loading())
         try {
             api.payoutBalance(
                 userId
             ).apply {
                 if (isSuccessful) {
                     body()?.let { resp ->
                         if (resp.has("success") && resp.get("success").asBoolean) {

                             if (resp.has("data") && !resp.get("data").isJsonNull) {
                                 val data = resp.getAsJsonObject("data")

                                 val next_payout = if (data.has("next_payout") && !data.get("next_payout").isJsonNull) {
                                     data.get("next_payout").asString
                                 } else {
                                     ""
                                 }

                                 val next_payout_date = if (data.has("next_payout_date") && !data.get("next_payout_date").isJsonNull) {
                                     data.get("next_payout_date").asString
                                 } else {
                                     ""
                                 }

                                 emit(NetworkResult.Success(Pair(next_payout,next_payout_date)))
                             }

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

     override suspend fun requestWithdrawal(
         userId: String,
         amount: String,
         withdrawalType: String
     ): Flow<NetworkResult<JsonObject>> = flow {
         emit(NetworkResult.Loading())
         try {
             api.requestWithdrawal(
                 userId,
                 amount,
                 withdrawalType
             ).apply {
                 if (isSuccessful) {
                     body()?.let { resp ->
                         if (resp.has("success") && resp.get("success").asBoolean) {

                             emit(NetworkResult.Success(resp))

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

     override suspend fun getSavedItemWishList(
         userId: Int,
         wishListId: Int
     ): Flow<NetworkResult<JsonObject>> = flow{
         try {
             api.getSavedItemWishList(userId, wishListId).apply {
                 if (isSuccessful) {
                     body()?.let { resp ->
                         if (resp.has("success") && resp.get("success").asBoolean) {
                             emit(NetworkResult.Success(resp))
                         } else {
                             emit(NetworkResult.Error(resp.get("message").asString))
                         }
                     } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                 } else {

                     emit(NetworkResult.Error(ErrorHandler.handleErrorBody(this.errorBody()?.string())))
                 }
             }
         } catch (e: Exception) {
             emit(NetworkResult.Error(ErrorHandler.emitError(e)))
         }
     }

     override suspend fun updatePhoneNumber(
         userId :Int,
          phoneNumber :String,
          countryCode :String
     ) :Flow<NetworkResult<String>> = flow{
         try {
             api.updatePhoneNumber(userId, phoneNumber,countryCode).apply {
                 if (isSuccessful) {
                     body()?.let { resp ->
                         if (resp.has("success") && resp.get("success").asBoolean) {
                           var obj = resp.get("data").asJsonObject
                             var otp = obj.get("otp").asInt
                             emit(NetworkResult.Success(otp.toString()))
                         } else {
                             emit(NetworkResult.Error(resp.get("message").asString))
                         }
                     } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                 } else {

                     emit(NetworkResult.Error(ErrorHandler.handleErrorBody(this.errorBody()?.string())))
                 }
             }
         } catch (e: Exception) {
             emit(NetworkResult.Error(ErrorHandler.emitError(e)))
         }
     }


     override suspend fun otpVerifyUpdatePhoneNumber(
          userId :Int,
          otp :String
     ) : Flow<NetworkResult<String>> = flow{
         try {
             api.otpVerifyUpdatePhoneNumber(userId,otp).apply {
                 if (isSuccessful) {
                     body()?.let { resp ->
                         if (resp.has("success") && resp.get("success").asBoolean) {
                            var obj = resp.get("message").asString
                             emit(NetworkResult.Success(obj))
                         } else {
                             emit(NetworkResult.Error(resp.get("message").asString))
                         }
                     } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                 } else {

                     emit(NetworkResult.Error(ErrorHandler.handleErrorBody(this.errorBody()?.string())))
                 }
             }
         } catch (e: Exception) {
             emit(NetworkResult.Error(ErrorHandler.emitError(e)))
         }
     }

   override  suspend fun updateEmail(
         @Field("user_id") userId :Int,
         @Field("email") email :String
     ) :Flow<NetworkResult<String>> = flow{
         try {
             api.updateEmail(userId,email).apply {
                 if (isSuccessful) {
                     body()?.let { resp ->
                         if (resp.has("success") && resp.get("success").asBoolean) {
                            var data = resp.get("data").asJsonObject
                            var otp = data.get("otp").asInt
                             emit(NetworkResult.Success(otp.toString()))
                         } else {
                             emit(NetworkResult.Error(resp.get("message").asString))
                         }
                     } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                 } else {

                     emit(NetworkResult.Error(ErrorHandler.handleErrorBody(this.errorBody()?.string())))
                 }
             }
         } catch (e: Exception) {
             emit(NetworkResult.Error(ErrorHandler.emitError(e)))
         }
     }

     override  suspend fun otpVerifyUpdateEmail(
          userId :Int,
         otp :String
     ):Flow<NetworkResult<String>> = flow{
         try {
             api.otpVerifyUpdateEmail(userId,otp).apply {
                 if (isSuccessful) {
                     body()?.let { resp ->
                         if (resp.has("success") && resp.get("success").asBoolean) {

                             emit(NetworkResult.Success(resp.get("message").asString))
                         } else {
                             emit(NetworkResult.Error(resp.get("message").asString))
                         }
                     } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                 } else {

                     emit(NetworkResult.Error(ErrorHandler.handleErrorBody(this.errorBody()?.string())))
                 }
             }
         } catch (e: Exception) {
             emit(NetworkResult.Error(ErrorHandler.emitError(e)))
         }
     }

     override suspend fun withdrawFunds(userId: String): Flow<NetworkResult<Pair<String, String>>> = flow {
         emit(NetworkResult.Loading())
         try {
             api.withdrawFunds(
                 userId
             ).apply {
                 if (isSuccessful) {
                     body()?.let { resp ->
                         if (resp.has("success") && resp.get("success").asBoolean) {

                             if (resp.has("data") && !resp.get("data").isJsonNull) {
                                 val data = resp.getAsJsonObject("data")

                                 val availableBalance = if (data.has("available_balance") && !data.get("available_balance").isJsonNull) {
                                     data.get("available_balance").asString
                                 } else {
                                     ""
                                 }

                                 val instantAvailableBalance = if (data.has("instant_available_balance") && !data.get("instant_available_balance").isJsonNull) {
                                     data.get("instant_available_balance").asString
                                 } else {
                                     ""
                                 }

                                 emit(NetworkResult.Success(Pair(availableBalance,instantAvailableBalance)))
                             }

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


     override suspend fun blockUser(
         senderId :Int,
         group_channel :String,
         blockUnblock:Int
     ): Flow<NetworkResult<JsonObject>> =
         flow {
             try {
                 api.blockUser(senderId,group_channel,blockUnblock).apply {
                     if (isSuccessful) {
                         body()?.let { resp ->
                             if (resp.has("success") && resp.get("success").asBoolean) {
                                 emit(NetworkResult.Success(resp))
                             } else {
                                 emit(NetworkResult.Error(resp.get("message").asString))
                             }
                         }
                             ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                     } else {
                         try {
                             val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                             emit(
                                 NetworkResult.Error(
                                     jsonObj?.getString("message")
                                         ?: AppConstant.unKnownError
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


     override suspend fun markFavoriteChat(
         senderId :Int,
         group_channel :String,
         favorite:Int
     ): Flow<NetworkResult<JsonObject>> =
         flow {
             try {
                 api.markFavoriteChat(senderId,group_channel,favorite).apply {
                     if (isSuccessful) {
                         body()?.let { resp ->
                             if (resp.has("success") && resp.get("success").asBoolean) {
                                 emit(NetworkResult.Success(resp))
                             } else {
                                 emit(NetworkResult.Error(resp.get("message").asString))
                             }
                         }
                             ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                     } else {
                         try {
                             val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                             emit(
                                 NetworkResult.Error(
                                     jsonObj?.getString("message")
                                         ?: AppConstant.unKnownError
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


     override suspend fun sendChatNotification(
         senderId :String,
         receiverId :String,
     ): Flow<NetworkResult<JsonObject>> =
         flow {
             try {
                 api.sendChatNotification(senderId,receiverId).apply {
                     if (isSuccessful) {
                         body()?.let { resp ->
                             if (resp.has("success") && resp.get("success").asBoolean) {
                                 emit(NetworkResult.Success(resp))
                             } else {
                                 emit(NetworkResult.Error(resp.get("message").asString))
                             }
                         }
                             ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                     } else {
                         try {
                             val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                             emit(
                                 NetworkResult.Error(
                                     jsonObj?.getString("message")
                                         ?: AppConstant.unKnownError
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


     override suspend fun muteChat(
         userId :Int,
         group_channel :String,
         mute:Int
     ): Flow<NetworkResult<JsonObject>> =
         flow {
             try {
                 api.muteChat(userId,group_channel,mute).apply {
                     if (isSuccessful) {
                         body()?.let { resp ->
                             if (resp.has("success") && resp.get("success").asBoolean) {
                                 emit(NetworkResult.Success(resp))
                             } else {
                                 emit(NetworkResult.Error(resp.get("message").asString))
                             }
                         }
                             ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                     } else {
                         try {
                             val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                             emit(
                                 NetworkResult.Error(
                                     jsonObj?.getString("message")
                                         ?: AppConstant.unKnownError
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

     override suspend fun toggleArchiveUnarchive(
         userId :Int, group_channel :String): Flow<NetworkResult<JsonObject>> =
         flow {
             try {
                 api.toggleArchiveUnarchive(userId,group_channel).apply {
                     if (isSuccessful) {
                         body()?.let { resp ->
                             if (resp.has("success") && resp.get("success").asBoolean) {
                                 emit(NetworkResult.Success(resp))
                             } else {
                                 emit(NetworkResult.Error(resp.get("message").asString))
                             }
                         }
                             ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                     } else {
                         try {
                             val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                             emit(
                                 NetworkResult.Error(
                                     jsonObj?.getString("message")
                                         ?: AppConstant.unKnownError
                                 )
                             )
                         } catch (e: JSONException) {
                             e.printStackTrace()
                             emit(NetworkResult.Error(AppConstant.unKnownError))
                         }
                     }
                 }
             }
             catch (e: HttpException) {
                 Log.e(ErrorDialog.TAG, "http exception - ${e.message}")
                 emit(NetworkResult.Error(e.message!!))
             }
             catch (e: IOException) {
                 Log.e(ErrorDialog.TAG, "io exception - ${e.message} :: ${e.localizedMessage}")
                 emit(NetworkResult.Error(e.message!!))
             }
             catch (e: Exception) {
                 Log.e(ErrorDialog.TAG, "exception - ${e.message} :: \n ${e.stackTraceToString()}")
                 emit(NetworkResult.Error(e.message!!))
             }
         }


     override suspend fun reportChat(
         reporter_id :String,
         reported_user_id :String,
         reason :String,
         message :String
     ): Flow<NetworkResult<JsonObject>> =
         flow {
             try {
                 api.reportChat(reporter_id,reported_user_id,
                     reason,message).apply {
                     if (isSuccessful) {
                         body()?.let { resp ->
                             if (resp.has("success") && resp.get("success").asBoolean) {
                                 emit(NetworkResult.Success(resp))
                             } else {
                                 emit(NetworkResult.Error(resp.get("message").asString))
                             }
                         }
                             ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                     } else {
                         try {
                             val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                             emit(
                                 NetworkResult.Error(
                                     jsonObj?.getString("message")
                                         ?: AppConstant.unKnownError
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

    override suspend fun otpResetPassword(
         @Field("user_id") userId :Int
     ) : Flow<NetworkResult<Pair<String,String>>> = flow{
         try {
             api.otpResetPassword(userId).apply {
                 if (isSuccessful) {
                     body()?.let { resp ->
                         if (resp.has("success") && resp.get("success").asBoolean) {
                            var obj = resp.get("data").asJsonObject
                             var code = obj.get("otp").asInt
                             var type = obj.get("type").asString
                             emit(NetworkResult.Success(Pair<String,String>(code.toString(),type)))

                         } else {
                             emit(NetworkResult.Error(resp.get("message").asString))
                         }
                     }
                         ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                 } else {
                     try {
                         val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                         emit(
                             NetworkResult.Error(
                                 jsonObj?.getString("message")
                                     ?: AppConstant.unKnownError
                             )
                         )
                     }
                     catch (e: JSONException) {
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


 }

