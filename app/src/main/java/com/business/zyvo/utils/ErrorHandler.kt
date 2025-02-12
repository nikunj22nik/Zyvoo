package com.business.zyvo.utils

import android.net.http.HttpException
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import com.business.zyvo.AppConstant
import com.business.zyvo.NetworkResult
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

object ErrorHandler {




    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun emitError(e: Exception) :String {
        when (e) {
            is HttpException -> {
                Log.e("ERROR", "HTTP exception: ${e.message}")
               return e.message ?: AppConstant.unKnownError
            }
            is IOException -> {
                Log.e("ERROR", "IO exception: ${e.message} :: ${e.localizedMessage}")
                return e.message ?: AppConstant.unKnownError

            }
            else -> {
                Log.e("ERROR", "Unexpected error: ${e.message} :: ${e.stackTraceToString()}")
                return e.message ?: AppConstant.unKnownError
            }
        }
    }


     fun handleErrorBody(errorBody: String?) :String {
        try {
            if (errorBody.isNullOrEmpty()) {
              return   AppConstant.unKnownError
            }
            val jsonObj = JSONObject(errorBody)
            val errorMessage = jsonObj.optString("message", AppConstant.unKnownError)
            return errorMessage

        } catch (e: JSONException) {
            e.printStackTrace()
            return AppConstant.unKnownError
        }
    }


}