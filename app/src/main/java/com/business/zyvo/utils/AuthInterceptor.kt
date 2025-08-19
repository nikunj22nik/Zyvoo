package com.business.zyvo.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.business.zyvo.activity.AuthActivity
import com.business.zyvo.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val context: Context): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        val sessionManager = SessionManager(context)

        // Add Authorization header if token is available
        sessionManager.getAuthToken()?.let { token ->
            if (token.isNotEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
                //requestBuilder.addHeader("Authorization", "Bearer $token timezone${ErrorDialog.getUpdatedTimeZoneId()}")
            }
        }
        requestBuilder.addHeader("Timezone", ErrorDialog.getUpdatedTimeZoneId())

      // Log.d("AuthInterceptor", "Final request headers: ${requestBuilder.get()}")
        // Add static API key
        //requestBuilder.addHeader("x-api-key", "bGS6lzFqvvSQ8ALbOxatm7/Vk7mLQyzqaS34Q4oR1ew=")
        val response = chain.proceed(requestBuilder.build())
        // Check for 401 Unauthorized response
        if (response.code == 401) {
            handleTokenExpiration(sessionManager)
            ErrorDialog
        }
        return response
    }

    private fun handleTokenExpiration(sessionManager: SessionManager) {
        // Clear session
        sessionManager.logOut()
        // Redirect to login screen
        var intent  = Intent(context, AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

}
