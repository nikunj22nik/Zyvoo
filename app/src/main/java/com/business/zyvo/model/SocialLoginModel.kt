package com.business.zyvo.model

data class SocialLoginModel(
    val code: Int,
    val `data`: Data,
    val message: String,
    val success: Boolean
)

data class Data(
    val device_type: String? = null,
    val fcm_token: String? = null,
    val is_login_first: Boolean? = null,
    val is_profile_complete: Boolean = false,
    val social_id: String = "",
    val token: String = "",
    val user_id: Int = 0,
    val user_image : String? = ""
)