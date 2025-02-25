package com.business.zyvo.fragment.host.placeOpen.model

data class PropertyResponse(
    val success: Boolean,
    val message: String,
    val code: Int,
    val data: PropertyData
)

data class PropertyData(
    val property_id: Int,
    val property_title: String,
    val property_status: String,
    val property_image: String,
    val reviews_total_rating: String,
    val reviews_total_count: String,
    val distance_miles: String,
    val bookings: List<Booking>
)

data class Booking(
    val booking_id: Int,
    val guest_name: String,
    val booking_status: String,
    val booking_date: String,
    val booking_start_end: String
)
