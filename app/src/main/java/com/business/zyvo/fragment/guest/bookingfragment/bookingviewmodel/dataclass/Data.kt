package com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass

data class Data(
    val activities: MutableList<String>? = mutableListOf(),
    val add_ons: List<AddOn>? = emptyList(),
    val amenities: MutableList<String>? = mutableListOf(),
    val booking_detail: BookingDetail? = null,
    val booking_id: Int? = 0,
    val charges: Charges? = null,
    val distance_miles: Any? = null,
    val first_property_image: String? = "",
    val host_id: Int? = 0,
    val host_name: String? = "",
    val host_profile_image: String? = "",
    val host_rules: List<String>? = emptyList(),
    val latitude: String? = "0.0",
    val location: String? = "",
    val longitude: String = "0.0",
    val parking_rules: List<String>? = emptyList(),
    val property_id: Int? = 0,
    val property_images: List<String>? = emptyList(),
    val property_name: String? = "",
    val rating: Double? = 0.0,
    val refund_policies: List<Any>? = emptyList(),
    val reviews: MutableList<Review>? = mutableListOf(),
    val status: String? = "",
    val total_rating: String? = "0"
)
