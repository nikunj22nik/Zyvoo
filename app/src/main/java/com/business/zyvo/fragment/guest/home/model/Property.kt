package com.business.zyvo.fragment.guest.home.model

data class Property(
    val property_id: Int,
    val user_id: Int,
    val host_id: Int,
    val host_fname: String,
    val host_lname: String,
    val title: String,
    val space_type: String,
    val property_size: Int,
    val max_guest_count: Int,
    val bedroom_count: Int,
    val bathroom_count: Int,
    val is_instant_book: Int,
    val has_self_checkin: Int,
    val allows_pets: Int,
    val cancellation_duration: Int,
    val street_address: String,
    val city: String,
    val state: String,
    val country: String,
    val zip_code: String,
    val latitude: String,
    val longitude: String,
    val min_booking_hours: String,
    val hourly_rate: String,
    val bulk_discount_hour: Int,
    val bulk_discount_rate: String,
    val cleaning_fee: String,
    val available_month: String,
    val available_day: String,
    val available_from: String,
    val available_to: String,
    val property_description: String,
    val parking_rules: String,
    val host_rules: String,
    val property_images: List<PropertyImage>,
    val activities: List<String>,
    val amenities: List<String>,
    val add_ons: List<AddOn>
)

data class PropertyImage(
    val id: Int,
    val image_url: String
)

data class AddOn(
    val name: String,
    val price: String
)
