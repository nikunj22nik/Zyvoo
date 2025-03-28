package com.business.zyvo.model

data class HostMyPlacesModel(
    val address: String,
    val distance_miles: String?,
    val fname: String?,
    val host_id: Int,
    val hourly_rate: String,
    val is_instant_book: Int,
    val latitude:String?,
    val lname: Any,
    val longitude: String?,
    val profile_image: Any,
    val property_id: Int,
    val property_status: String?,
    val property_images : List<String>,
    val property_rating : String,
    val property_review_count : String,
    val title: String


)