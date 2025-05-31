package com.business.zyvo.fragment.guest.profile.model

data class UserProfile(
    var name:String ="",
    val first_name: String?,
    val last_name: String?,
    val profile_image: String?,
    val email_verified: Int?,
    val phone_verified: Int?,
    val identity_verified: Int?,
    var about_me: String?,
    val where_live: List<String>,
    val my_work: List<String>,
    val languages: List<String>,
    val hobbies: List<String>,
    val pets: List<String>,
    var email: String?,
    var phone_number: String?,
    val street: String?,
    val city: String?,
    val state: String?,
    val zip_code: String?,
    val payment_methods: List<String>
)
