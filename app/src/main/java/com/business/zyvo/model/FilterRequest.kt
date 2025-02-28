package com.business.zyvo.model

data class FilterRequest(
    val user_id: Int?,
    val latitude: Double?,
    val longitude: Double?,
    val place_type: String?,
    val minimum_price: Double?,
    val maximum_price: Double?,
    val location: String?,
    val date: String?,
    val time: Int?,
    val property_size: Int?,
    val bedroom: Int?,
    val bathroom: Int?,
    val people_count: Int?,
    val instant_booking: Int?,
    val self_check_in: Int?,
    val allows_pets: Int?,
    val activities: List<String>?,
    val amenities: List<String>?,
    val languages: List<String>?
)

