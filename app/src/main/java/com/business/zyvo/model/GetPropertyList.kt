package com.business.zyvo.model

data class GetPropertyList(
    val address: String,
    val distance_miles: Any,
    val fname: Any,
    val host_id: Int,
    val hourly_rate: String,
    val is_instant_book: Int,
    val latitude: Any,
    val lname: Any,
    val longitude: Any,
    val profile_image: Any,
    val property_id: Int,
    val property_images: List<String>,
    val property_rating: String,
    val property_review_count: String,
    val title: String
)