package com.business.zyvo.fragment.guest.bookingviewmodel.dataclass

data class BookingModel(
    val booking_date: String?,
    val booking_id: Int =-1,
    val booking_status: String?,
    val property_id: Int=-1,
    val property_image: String?,
    val property_name: String?
)

