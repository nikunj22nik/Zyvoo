package com.business.zyvo.fragment.guest.bookingviewmodel.dataclass

data class Data(
    val addons: MutableList<String>?,
    val booking_detail: BookingDetail? = null,
    val booking_id: Int? = 0,
    val charges: Charges? = null,
    val distance: String? = "N/A",
    val host_id: Int? = 0,
    val host_name: String? = "Unknown",
    val host_rule: String? = "N/A",
    val included_in_booking: List<String>? = emptyList(),
    val latitude: String? = "0.0",
    val location: String? = "Unknown",
    val longitude: String? = "0.0",
    val parking: String? = "Not specified",
    val property_name: String? = "Unknown",
    val rating: Double? = 0.0,
    val refund_policies: Any? = null,
    val reviews: List<Review>? = emptyList(),
    val status: String? = "Unknown",
    val total_rating: String? = "0"
)


